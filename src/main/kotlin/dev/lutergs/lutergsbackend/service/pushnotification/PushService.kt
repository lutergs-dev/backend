package dev.lutergs.lutergsbackend.service.pushnotification

import dev.lutergs.lutergsbackend.controller.*
import dev.lutergs.lutergsbackend.repository.pushRepositoryImpl.Response
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class PushService(
    private val pushRepository: PushRepository,
    private val newTopicMakeRequestRepository: NewTopicMakeRequestRepository
) {
    fun isValidSubscription(auth: String): Mono<Boolean> {
        return this.pushRepository.findSubscriptionByAuth(auth, false)
            .hasElement()
    }

    fun saveSubscription(subscriptionRequest: SubscriptionRequest): Mono<Subscription> {
        return subscriptionRequest.toSubscription()
            .let { this.pushRepository.saveNewSubscription(it) }
    }

    fun getTopics(): Flux<Topic> {
        return this.pushRepository.getTopics()
    }

    fun getTopic(uuid: String): Mono<Topic> {
        return this.pushRepository.findTopicByUUID(uuid, false)
    }

    fun createTopic(topicRequest: NewTopicRequest): Mono<Topic> {
        return topicRequest.toTopic()
            .let { this.pushRepository.saveNewTopic(it) }
    }

    fun deleteTopic(uuid: String): Mono<Void> {
        return this.pushRepository.deleteTopic(uuid)
    }

    fun getSubscribedTopics(auth: String): Mono<List<Topic>> {
        return this.pushRepository.findSubscriptionByAuth(auth, true)
            .switchIfEmpty{ Mono.error(IllegalArgumentException("subscription is not valid")) }
            .flatMap { Mono.just(it.topics!!) }
    }

    fun subscribeToTopic(subscriptionAuth: String, topicUUID: String): Mono<Boolean> {
        return this.pushRepository.findSubscriptionByAuth(subscriptionAuth, false)
            .switchIfEmpty { Mono.error(IllegalArgumentException("subscription is not valid")) }
            .flatMap { subscription ->
                this.pushRepository.findTopicByUUID(topicUUID, false)
                    .switchIfEmpty { Mono.error(IllegalArgumentException("provided topic UUID is not valid")) }
                    .flatMap { topic -> when(topic.type) {
                        TopicType.FIXED -> Mono.error(IllegalArgumentException("trying to subscribe FIXED topic"))
                        TopicType.UNSUBSCRIBABLE -> Mono.just(Pair(subscription, topic))
                    }} }
            .flatMap { this.pushRepository.subscribeToTopic(it.first, it.second) }
    }

    fun unsubscribeFromTopic(subscriptionAuth: String, topicUUID: String): Mono<Void> {
        return this.pushRepository.findSubscriptionByAuth(subscriptionAuth, false)
            .switchIfEmpty { Mono.error(IllegalArgumentException("subscription is not valid")) }
            .flatMap { subscription ->
                this.pushRepository.findTopicByUUID(topicUUID, false)
                    .switchIfEmpty { Mono.error(IllegalArgumentException("provided topic UUID is not valid")) }
                    .flatMap { topic -> when(topic.type) {
                        TopicType.FIXED -> Mono.error(IllegalArgumentException("trying to unsubscribe FIXED topic"))
                        TopicType.UNSUBSCRIBABLE -> Mono.just(Pair(subscription, topic))
                    }} }
            .flatMap { this.pushRepository.unsubscribeFromTopic(it.first, it.second) }
    }

    fun triggerTopic(triggerTopicRequest: TriggerTopicRequest): Flux<Response> {
        val pushMessage = triggerTopicRequest.toPushMessage()
        return this.pushRepository.findTopicByUUID(topicUUID = triggerTopicRequest.topicUUID, true)
            .flatMapMany { topic -> when(topic.type) {
                TopicType.FIXED -> this.pushRepository.getSubscriptions()
                    .flatMap { this.pushRepository.sendSubscriptionMessage(it, pushMessage, topic.name) }
                TopicType.UNSUBSCRIBABLE -> this.pushRepository.sendTopicMessage(topic, pushMessage)
            }}
    }

    fun newTopicMakeRequest(newTopicMakeRequest: NewTopicMakeRequest): Mono<Boolean> {
        return this.newTopicMakeRequestRepository.request(newTopicMakeRequest.description)
    }
}