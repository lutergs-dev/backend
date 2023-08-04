package dev.lutergs.lutergsbackend.service.pushnotification

import dev.lutergs.lutergsbackend.repository.pushRepositoryImpl.Response
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PushRepository {
    fun saveNewSubscription(subscription: Subscription): Mono<Subscription>
    fun getTopics(): Flux<Topic>
    fun saveNewTopic(topic: Topic): Mono<Topic>
    fun findTopicByUUID(topicUUID: String): Mono<Topic>
    fun sendTopicMessage(topic: Topic, message: String): Flux<Response>
}