package dev.lutergs.lutergsbackend.repository.pushRepositoryImpl

import com.fasterxml.jackson.databind.ObjectMapper
import dev.lutergs.lutergsbackend.service.pushnotification.PushMessage
import dev.lutergs.lutergsbackend.service.pushnotification.PushRepository
import dev.lutergs.lutergsbackend.service.pushnotification.Subscription
import dev.lutergs.lutergsbackend.service.pushnotification.Topic
import nl.martijndwars.webpush.Notification
import nl.martijndwars.webpush.PushAsyncService
import org.asynchttpclient.Response as AsyncResponse
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.security.Security


@Component
class PushRepositoryImpl(
    private val pushEntityRepository: PushEntityRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${custom.push.public-key}") publicKey: String,
    @Value("\${custom.push.private-key}") privateKey: String,
    @Value("\${custom.server.url.frontend-app}") frontendAppUrl: String
): PushRepository {

    private val logger = LoggerFactory.getLogger(this::class.java)
    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider());
        }
    }

    private val pushAsyncService = PushAsyncService(publicKey, privateKey, frontendAppUrl)

    @Transactional
    override fun saveNewSubscription(subscription: Subscription): Mono<Subscription> {
        return this.pushEntityRepository.findSubscriptionEntityByAuth(subscription.auth)
            .flatMap {
                it.key = subscription.key
                it.endpoint = subscription.endpoint
                Mono.just(it)
            }.defaultIfEmpty(SubscriptionEntity.fromNotRelatedSubscription(subscription))
            .flatMap { this.pushEntityRepository.saveNotRelatedSubscription(it) }
            .flatMap { Mono.just(it.toNotRelatedSubscription()) }
    }

    override fun findSubscriptionByAuth(auth: String, getSubscribedTopics: Boolean): Mono<Subscription> {
        return this.pushEntityRepository.findSubscriptionEntityByAuth(auth)
            .flatMap { subscriptionEntity ->
                if (getSubscribedTopics) {
                    subscriptionEntity
                        .toRelatedSubscription(
                            this.pushEntityRepository.getSubscribedTopics(subscriptionEntity)
                        )
                } else {
                    Mono.just(subscriptionEntity.toNotRelatedSubscription())
                }
            }
    }

    override fun getTopics(): Flux<Topic> {
        return this.pushEntityRepository.getTopics()
            .flatMap { Mono.just(it.toNotRelatedTopic()) }
    }

    override fun saveNewTopic(topic: Topic): Mono<Topic> {
        return this.pushEntityRepository.saveNotRelatedTopic(topic)
            .flatMap { Mono.just(it.toNotRelatedTopic()) }
    }

    @Transactional
    override fun findTopicByUUID(topicUUID: String): Mono<Topic> {
        return this.pushEntityRepository.findNotRelatedTopicByUUID(topicUUID)
            .flatMap { it.toRelatedTopic(this.pushEntityRepository.getRelatedSubscriptions(it)) }
    }

    override fun subscribeToTopic(subscription: Subscription, topic: Topic): Mono<Boolean> {
        // TODO : 어떻게 관리하는게 맞는가? 쿼리 호출 4번, 도메인 규칙 지킴 vs 쿼리 호출 2번, 도메인 규칙 깸
        //  일단 첫번째로 진행
        return Mono.zip(
            this.pushEntityRepository.findSubscriptionEntityByAuth(subscription.auth),
            this.pushEntityRepository.findNotRelatedTopicByUUID(topic.uuid)
        ).flatMap { tuple ->
            val (subscriptionEntity, topicEntity) = Pair(tuple.t1, tuple.t2)
            this.pushEntityRepository.findTopicSubscriptionList(subscriptionEntity, topicEntity)
                .hasElement()
                .flatMap { exist ->
                    if (exist) Mono.error(IllegalStateException("이미 subscription 이 topic 을 구독중입니다."))
                    else this.pushEntityRepository.pairSubscriptionAndTopic(subscriptionEntity, topicEntity)
                }.hasElement()
        }
    }

    override fun unsubscribeToTopic(subscription: Subscription, topic: Topic): Mono<Boolean> {
        return Mono.zip(
            this.pushEntityRepository.findSubscriptionEntityByAuth(subscription.auth),
            this.pushEntityRepository.findNotRelatedTopicByUUID(topic.uuid)
        ).flatMap { tuple ->
            val (subscriptionEntity, topicEntity) = Pair(tuple.t1, tuple.t2)
            this.pushEntityRepository.findTopicSubscriptionList(subscriptionEntity, topicEntity)
                .switchIfEmpty { Mono.error(IllegalStateException("subscription 이 topic 에 구독한 상태가 아닙니다.")) }
                .flatMap { this.pushEntityRepository.unPairSubscriptionAndTopic(it) }
                .hasElement()
        }
    }

    override fun sendTopicMessage(topic: Topic, pushMessage: PushMessage): Flux<Response> {
        return topic.subscriptions
            ?.map { PushSubscriptionEntity.fromPushSubscription(it) }
            ?.map { entity ->
                Pair(
                    Notification(
                        entity.endpoint,
                        entity.getUserPublicKey(),
                        entity.getAuthAsBytes(),
                        SendPushMessage.fromTopicAndPushMessage(topic, pushMessage)
                            .let { this.objectMapper.writeValueAsBytes(it) }
                    ),
                    entity.auth
                ) }
            ?.map { pair ->
                val notification = pair.first
                val auth = pair.second
                Mono.fromFuture(this.pushAsyncService.send(notification))
                    .flatMap { response ->
                        if (response.statusCode == 410) {
                            this.pushEntityRepository.findSubscriptionEntityByAuth(auth)
                                .flatMap { this.pushEntityRepository.deleteSubscriptionEntity(it)
                                    .collectList()
                                    .then(Mono.just(Response.fromResponseAndEndpoint(response, auth)))
                                }
                        } else { Mono.just(Response.fromResponseAndEndpoint(response, auth)) }
                    }
            }
            ?.let { Flux.concat(it) }
            ?: throw IllegalStateException("NOT valid subscription!")
    }
}

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