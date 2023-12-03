package dev.lutergs.lutergsbackend.infra.impl.push

import com.fasterxml.jackson.databind.ObjectMapper
import dev.lutergs.lutergsbackend.domain.push.PushMessageRequest
import dev.lutergs.lutergsbackend.domain.push.PushMessageSender
import dev.lutergs.lutergsbackend.domain.push.topic.Topic
import dev.lutergs.lutergsbackend.infra.repository.push.database.PushMessageHistory
import dev.lutergs.lutergsbackend.infra.repository.push.database.PushMessageHistoryRepository
import dev.lutergs.lutergsbackend.infra.repository.push.database.SubscriberEntityRepository
import dev.lutergs.lutergsbackend.infra.repository.push.database.TopicSubscriberRelationEntityRepository
import dev.lutergs.lutergsbackend.utils.toLong
import nl.martijndwars.webpush.Notification
import nl.martijndwars.webpush.PushAsyncService
import nl.martijndwars.webpush.Utils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.security.KeyFactory
import java.security.Security
import java.time.OffsetDateTime
import java.util.Base64
import java.util.UUID
import org.asynchttpclient.Response as AsyncResponse

data class Response(
    val auth: String,
    val responseCode: Int,
    val responseBody: String
) {
    companion object {
        fun fromResponseAndEndpoint(response: AsyncResponse, endpoint: String): Response {
            return Response(endpoint, response.statusCode, response.responseBody)
        }
    }
}

@Component
class PushMessageSenderImpl(
    private val subscriberEntityRepository: SubscriberEntityRepository,
    private val topicSubscriberRelationEntityRepository: TopicSubscriberRelationEntityRepository,
    private val pushMessageHistoryRepository: PushMessageHistoryRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${custom.push.public-key}") publicKey: String,
    @Value("\${custom.push.private-key}") privateKey: String,
    @Value("\${custom.server.url.frontend-app}") frontendAppUrl: String
): PushMessageSender {

    init {
        Security.addProvider(BouncyCastleProvider())
        KeyFactory.getInstance(Utils.ALGORITHM, BouncyCastleProvider.PROVIDER_NAME)
    }

    private val base64Decoder = Base64.getDecoder()
    private val pushAsyncService = PushAsyncService(publicKey, privateKey, frontendAppUrl)

    override fun sendMessagesToTopicSubscribers(topic: Topic, pushMessageRequest: PushMessageRequest): Flux<Response> {
        val sendAt = OffsetDateTime.now()
        return Flux.fromIterable(topic.subscribers)
            .flatMap { Mono.just(PushSubscriptionEntity.fromPushSubscription(it)) }
            .flatMap { entity -> Mono.just(Pair(
                Notification.builder()
                    .endpoint(this.base64Decoder.decode(entity.endpoint).decodeToString())
                    .userPublicKey(entity.getUserPublicKey())
                    .userAuth(entity.getAuthAsBytes())
                    .payload(
                        SendPushMessage.fromTopicAndPushMessage(topic, pushMessageRequest)
                            .let { this.objectMapper.writeValueAsBytes(it) }
                    )
                    .build(),
                entity )) }
            .flatMap { pair ->
                val notification = pair.first
                val entity = pair.second
                Mono.fromFuture(this.pushAsyncService.send(notification))
                    .flatMap { response -> when(response.statusCode) {
                        410 -> Mono.fromCallable {
                            this.deleteSubscriber(entity.auth)
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe()
                            Response.fromResponseAndEndpoint(response, entity.endpoint) }
                        else -> Mono.fromCallable {
                            this.applyMessageToSubscriber(pushMessageRequest, topic.uuid, entity.auth, sendAt)
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe()
                            Response.fromResponseAndEndpoint(response, entity.endpoint) }
                    } }
            }
    }

    private fun deleteSubscriber(subscriberAuth: String): Mono<Void> {
        return this.subscriberEntityRepository.findDistinctFirstByAuth(subscriberAuth)
            .flatMap { subscriberEntity ->
                this.topicSubscriberRelationEntityRepository.findAllBySubscriptionId(subscriberEntity.id!!)
                    .flatMap { this.topicSubscriberRelationEntityRepository.delete(it) }
                    .then(this.subscriberEntityRepository.delete(subscriberEntity))
                    .then(this.pushMessageHistoryRepository.deleteAllBySubscriberAuth(subscriberEntity.auth))
            }
    }

    private fun applyMessageToSubscriber(
        pushMessageRequest: PushMessageRequest,
        topicUUID: UUID,
        subscriberAuth: String,
        sendAt: OffsetDateTime
    ): Mono<PushMessageHistory> {
        return PushMessageHistory()
            .apply {
                this.topicUUID = topicUUID.toString()
                this.subscriberAuth = subscriberAuth
                this.title = pushMessageRequest.title
                this.body = pushMessageRequest.body
                this.iconUrl = pushMessageRequest.iconUrl
                this.sendAt = sendAt.toLong() }
            .let { this.pushMessageHistoryRepository.insert(it) }
    }

}