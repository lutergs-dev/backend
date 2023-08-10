package dev.lutergs.lutergsbackend.service.pushnotification

import dev.lutergs.lutergsbackend.repository.pushRepositoryImpl.Response
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PushRepository {
    fun saveNewSubscription(subscription: Subscription): Mono<Subscription>
    fun findSubscriptionByAuth(auth: String, getSubscribedTopics: Boolean): Mono<Subscription>
    fun getTopics(): Flux<Topic>
    fun saveNewTopic(topic: Topic): Mono<Topic>
    fun findTopicByUUID(topicUUID: String): Mono<Topic>
    fun subscribeToTopic(subscription: Subscription, topic: Topic): Mono<Boolean>
    fun unsubscribeToTopic(subscription: Subscription, topic: Topic): Mono<Boolean>
    fun sendTopicMessage(topic: Topic, pushMessage: PushMessage): Flux<Response>
}

interface NewTopicMakeRequestRepository {
    fun request(description: String): Mono<Boolean>
}