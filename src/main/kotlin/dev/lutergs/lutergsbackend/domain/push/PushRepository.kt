package dev.lutergs.lutergsbackend.domain.push

import dev.lutergs.lutergsbackend.domain.push.subscriber.Subscriber
import dev.lutergs.lutergsbackend.domain.push.topic.Topic
import dev.lutergs.lutergsbackend.infra.impl.push.Response
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface SubscriberReadRepository {
    fun getSubscriberByEndpoint(endpoint: String): Mono<Subscriber>
    fun getSubscriberByAuth(auth: String): Mono<Subscriber>
    fun getSubscribedTopic(subscriber: Subscriber): Mono<Subscriber>
    fun getSubscribedTopicsMessageHistory(subscriber: Subscriber): Mono<Subscriber>
}

interface SubscriberWriteRepository {
    fun saveSubscriber(subscriber: Subscriber): Mono<Subscriber>
    fun subscribeToTopic(endpoint: String, auth: String, topicUUID: UUID): Mono<Void>
    fun unsubscribeToTopic(endpoint: String, auth: String, topicUUID: UUID): Mono<Void>
}

interface TopicReadRepository {
    fun getTopics(): Flux<Topic>
    fun getTopicByUUID(uuid: UUID): Mono<Topic>
    fun getTopicSubscribers(topic: Topic): Mono<Topic>
    fun getTopicHistory(topic: Topic): Mono<Topic>
}

interface TopicWriteRepository {
    fun saveTopic(topic: Topic): Mono<Topic>
    fun deleteTopic(uuid: UUID): Mono<Void>
}

interface PushMessageSender {
    fun sendMessagesToTopicSubscribers(topic: Topic, pushMessageRequest: PushMessageRequest): Flux<Response>
}

interface NewTopicMakeRequester {
    fun request(description: String): Mono<Boolean>
}