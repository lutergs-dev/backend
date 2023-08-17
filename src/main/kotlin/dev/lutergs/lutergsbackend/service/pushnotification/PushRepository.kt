package dev.lutergs.lutergsbackend.service.pushnotification

import dev.lutergs.lutergsbackend.repository.pushRepositoryImpl.Response
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PushRepository {
    fun getSubscriptions(): Flux<Subscription>
    fun saveNewSubscription(subscription: Subscription): Mono<Subscription>
    fun findSubscriptionByAuth(auth: String, getSubscribedTopics: Boolean): Mono<Subscription>
    fun getTopics(): Flux<Topic>
    fun saveNewTopic(topic: Topic): Mono<Topic>
    fun deleteTopic(topicUUID: String): Mono<Void>
    fun findTopicByUUID(topicUUID: String, getSubscribers: Boolean): Mono<Topic>
    fun subscribeToTopic(subscription: Subscription, topic: Topic): Mono<Boolean>
    fun unsubscribeFromTopic(subscription: Subscription, topic: Topic): Mono<Void>
    fun sendTopicMessage(topic: Topic, pushMessage: PushMessage): Flux<Response>
    fun sendSubscriptionMessage(subscription: Subscription, pushMessage: PushMessage, topicName: String): Mono<Response>
}

interface NewTopicMakeRequestRepository {
    fun request(description: String): Mono<Boolean>
}