package dev.lutergs.lutergsbackend.repository.pushRepositoryImpl

import com.fasterxml.jackson.databind.ObjectMapper
import dev.lutergs.lutergsbackend.service.pushnotification.PushMessage
import dev.lutergs.lutergsbackend.service.pushnotification.PushRepository
import dev.lutergs.lutergsbackend.service.pushnotification.Subscription
import dev.lutergs.lutergsbackend.service.pushnotification.Topic
import nl.martijndwars.webpush.Notification
import nl.martijndwars.webpush.PushAsyncService
import nl.martijndwars.webpush.Utils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.security.KeyFactory
import java.security.Security
import org.asynchttpclient.Response as AsyncResponse


//import com.oracle.svm.core.annotate.*;
//import org.graalvm.nativeimage.ImageSingletons;
//import org.graalvm.nativeimage.hosted.Feature;
//import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
//import org.graalvm.nativeimage.impl.RuntimeClassInitializationSupport;

@Component
class PushRepositoryImpl(
    private val pushEntityRepository: PushEntityRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${custom.push.public-key}") publicKey: String,
    @Value("\${custom.push.private-key}") privateKey: String,
    @Value("\${custom.server.url.frontend-app}") frontendAppUrl: String
): PushRepository {

    init {
        Security.addProvider(BouncyCastleProvider())
        KeyFactory.getInstance(Utils.ALGORITHM, BouncyCastleProvider.PROVIDER_NAME)
    }

    private val pushAsyncService = PushAsyncService(publicKey, privateKey, frontendAppUrl)

    override fun getSubscriptions(): Flux<Subscription> {
        return this.pushEntityRepository.getSubscriptionEntities().flatMap { Mono.just(it.toNotRelatedSubscription()) }
    }

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
        println("reach here!")
        return this.pushEntityRepository.getTopics()
            .flatMap {
                println("${it.id} ${it.name} ${it.description} ${it.uuid} ${it.deleted} ${it.createdAt}")
                Mono.just(it.toNotRelatedTopic())
            }
    }

    override fun saveNewTopic(topic: Topic): Mono<Topic> {
        return this.pushEntityRepository.saveNotRelatedTopic(topic)
            .flatMap { Mono.just(it.toNotRelatedTopic()) }
    }

    override fun deleteTopic(topicUUID: String): Mono<Void> {
        return this.pushEntityRepository.findNotRelatedTopicByUUID(topicUUID)
            .flatMap { this.pushEntityRepository.deleteTopic(it) }
    }

    @Transactional
    override fun findTopicByUUID(topicUUID: String, getSubscribers: Boolean): Mono<Topic> {
        return this.pushEntityRepository.findNotRelatedTopicByUUID(topicUUID)
            .flatMap {
                if (getSubscribers) it.toRelatedTopic(this.pushEntityRepository.getRelatedSubscriptions(it))
                else Mono.just(it.toNotRelatedTopic())
            }
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

    override fun unsubscribeFromTopic(subscription: Subscription, topic: Topic): Mono<Void> {
        return Mono.zip(
            this.pushEntityRepository.findSubscriptionEntityByAuth(subscription.auth),
            this.pushEntityRepository.findNotRelatedTopicByUUID(topic.uuid)
        ).flatMap { tuple ->
            val (subscriptionEntity, topicEntity) = Pair(tuple.t1, tuple.t2)
            this.pushEntityRepository.findTopicSubscriptionList(subscriptionEntity, topicEntity)
                .switchIfEmpty { Mono.error(IllegalStateException("subscription 이 topic 에 구독한 상태가 아닙니다.")) }
                .flatMap { this.pushEntityRepository.unPairSubscriptionAndTopic(it) }
        }
    }

    override fun sendMessageToTopicSubscriptions(topic: Topic, pushMessage: PushMessage): Flux<Response> {
        return topic.subscriptions
            ?.map { PushSubscriptionEntity.fromPushSubscription(it) }
            ?.map { entity -> Pair(
                Notification.builder()
                    .endpoint(entity.endpoint)
                    .userPublicKey(entity.getUserPublicKey())
                    .userAuth(entity.getAuthAsBytes())
                    .payload(SendPushMessage.fromTopicAndPushMessage(topic, pushMessage)
                        .let { this.objectMapper.writeValueAsBytes(it) })
                    .build(),
                entity.auth ) }
            ?.map { pair ->
                val notification = pair.first
                val auth = pair.second
                Mono.fromFuture(this.pushAsyncService.send(notification))
                    .flatMap { response ->
                        if (response.statusCode == 410) {
                            this.pushEntityRepository.findSubscriptionEntityByAuth(auth)
                                .flatMap { this.pushEntityRepository.deleteSubscriptionEntity(it)
                                    .then(Mono.just(Response.fromResponseAndEndpoint(response, auth)))
                                }
                        } else { Mono.just(Response.fromResponseAndEndpoint(response, auth)) }
                    }
            }
            ?.let { Flux.concat(it) }
            ?: throw IllegalStateException("NOT valid subscription!")
    }

    override fun sendMessageToSubscription(subscription: Subscription, pushMessage: PushMessage, topicName: String): Mono<Response> {
        return PushSubscriptionEntity.fromPushSubscription(subscription)
            .let { entity -> Pair(
                Notification.builder()
                    .endpoint(entity.endpoint)
                    .userPublicKey(entity.getUserPublicKey())
                    .userAuth(entity.getAuthAsBytes())
                    .payload(SendPushMessage.fromTopicNameAndPushMessage(topicName, pushMessage)
                        .let { this.objectMapper.writeValueAsBytes(it) })
                    .build(),
                entity.auth ) }
            .let { pair ->
                val notification = pair.first
                val auth = pair.second
                Mono.fromFuture(this.pushAsyncService.send(notification))
                    .flatMap { response ->
                        if (response.statusCode == 410) {
                            this.pushEntityRepository.findSubscriptionEntityByAuth(auth)
                                .flatMap { this.pushEntityRepository.deleteSubscriptionEntity(it)
                                    .then(Mono.just(Response.fromResponseAndEndpoint(response, auth)))
                                }
                        } else { Mono.just(Response.fromResponseAndEndpoint(response, auth)) }
                    } }
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


//class BouncyCastleFeature : Feature {
//    override fun afterRegistration(access: Feature.AfterRegistrationAccess) {
//        RuntimeClassInitialization.initializeAtBuildTime("org.bouncycastle")
//        val rci: RuntimeClassInitializationSupport = ImageSingletons.lookup(RuntimeClassInitializationSupport::class.java)
//        rci.rerunInitialization("org.bouncycastle.jcajce.provider.drbg.DRBG\$Default", "")
//        rci.rerunInitialization("org.bouncycastle.jcajce.provider.drbg.DRBG\$NonceAndIV", "")
//        Security.addProvider(BouncyCastleProvider())
//    }
//}