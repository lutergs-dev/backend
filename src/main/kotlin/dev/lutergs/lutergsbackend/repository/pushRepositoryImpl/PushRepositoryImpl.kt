package dev.lutergs.lutergsbackend.repository.pushRepositoryImpl

import dev.lutergs.lutergsbackend.service.pushnotification.PushRepository
import dev.lutergs.lutergsbackend.service.pushnotification.Subscription
import dev.lutergs.lutergsbackend.service.pushnotification.Topic
import nl.martijndwars.webpush.Notification
import nl.martijndwars.webpush.PushAsyncService
import org.asynchttpclient.Response
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.security.Security


@Component
class PushRepositoryImpl(
    private val pushEntityRepository: PushEntityRepository,
    @Value("\${custom.push.public-key}") publicKey: String,
    @Value("\${custom.push.private-key}") privateKey: String
): PushRepository {
    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider());
        }
    }

    private val pushAsyncService = PushAsyncService(publicKey, privateKey)

    override fun saveNewSubscription(subscription: Subscription): Mono<Subscription> {
        return this.pushEntityRepository.saveNotRelatedSubscription(subscription)
            .flatMap { Mono.just(it.toNotRelatedSubscription()) }
    }

    override fun getTopics(): Flux<Topic> {
        return this.pushEntityRepository.getTopics()
    }

    override fun saveNewTopic(topic: Topic): Mono<Topic> {
        return this.pushEntityRepository.saveNotRelatedTopic(topic)
            .flatMap { Mono.just(it.toNotRelatedTopic()) }
    }

    override fun findTopicByUUID(topicUUID: String): Mono<Topic> {
        return this.pushEntityRepository.findTopicByUUID(topicUUID)
    }

    override fun sendTopicMessage(topic: Topic, message: String): Mono<List<Int>> {
        return topic.subscriptions
            ?.map { PushSubscriptionEntity.fromPushSubscription(it) }
            ?.map {
                Notification(
                    it.endpoint,
                    it.getUserPublicKey(),
                    it.getAuthAsBytes(),
                    message.toByteArray()
                ) }
            ?.map { notification ->
                Mono.fromFuture(this.pushAsyncService.send(notification))
                    .flatMap { response ->
                        if (response.statusCode == 410) {
                            this.pushEntityRepository.findSubscriptionByEndpoint(notification.endpoint)
                                .flatMap { this.pushEntityRepository.deleteSubscriptionEntity(it)
                                    .collectList()
                                    .then(Mono.just(response.statusCode))
                                }
                        } else { Mono.just(response.statusCode) }
                    }
            }
            ?.let { Flux.concat(it) }
            ?.collectList()
            ?: throw IllegalStateException("NOT valid subscription!")
    }
}