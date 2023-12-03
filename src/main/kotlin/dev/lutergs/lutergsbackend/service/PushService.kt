package dev.lutergs.lutergsbackend.service

import dev.lutergs.lutergsbackend.domain.push.NewTopicMakeRequester
import dev.lutergs.lutergsbackend.domain.push.PushMessageSender
import dev.lutergs.lutergsbackend.domain.push.subscriber.*
import dev.lutergs.lutergsbackend.domain.push.topic.*
import dev.lutergs.lutergsbackend.infra.controller.*
import dev.lutergs.lutergsbackend.infra.impl.push.Response
import kotlinx.coroutines.awaitAll
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

class PushService(
    // CQRS separation
    private val subscriberProjection: SubscriberProjection,
    private val subscriberAggregate: SubscriberAggregate,
    private val topicProjection: TopicProjection,
    private val topicAggregate: TopicAggregate,
    private val pushMessageSender: PushMessageSender,
    private val newTopicMakeRequester: NewTopicMakeRequester
) {

    fun isSubscriberExists(endpoint: String): Mono<Boolean> {
        return SubscriberIsValidQuery(endpoint)
            .let { this.subscriberProjection.handle(it) }
    }

    fun getSubscriber(subscriberRequest: SubscriberRequest): Mono<Subscriber> {
        return SubscriberByEndpointQuery(
                endpoint = subscriberRequest.endpoint,
                auth = subscriberRequest.auth,
                getSubscribedTopics = true,
                getTopicReceiveHistory = subscriberRequest.getHistory)
            .let { this.subscriberProjection.handle(it) }
    }

    fun saveSubscriber(newSubscriberRequest: NewSubscriberRequest, endpoint: String): Mono<Subscriber> {
        return SubscriberCreateCommand(
            endpoint = endpoint,
            auth = newSubscriberRequest.auth,
            key = newSubscriberRequest.key
        ).let { this.subscriberAggregate.handle(it) }
    }

    fun getSubscribedTopics(subscriberRequest: SubscriberRequest): Flux<Topic> {
        return SubscriberByEndpointQuery(
                endpoint = subscriberRequest.endpoint,
                auth = subscriberRequest.auth,
                getSubscribedTopics = true,
                getTopicReceiveHistory = subscriberRequest.getHistory)
            .let { this.subscriberProjection.handle(it) }
            .flatMapMany { Flux.fromIterable(it.topics) }
    }

    fun subscribeToTopic(subscriberRequest: SubscriberRequest, topicUUID: UUID): Mono<Void> {
        // 이미 구독중이거나 등등의 오류는 CQRS 를 적용하면 아얘 다른 부분에서 처리해야 한다.
        return SubscribeToTopicCommand(
            endpoint = subscriberRequest.endpoint,
            auth = subscriberRequest.auth,
            topicUUID = topicUUID
        ).let { this.subscriberAggregate.handle(it) }
    }

    fun unsubscribeFromTopic(subscriberRequest: SubscriberRequest, topicUUID: UUID): Mono<Void> {
        // 이미 구독중이거나 등등의 오류는 CQRS 를 적용하면 아얘 다른 부분에서 처리해야 한다.
        return UnsubscribeToTopicCommand(
            endpoint = subscriberRequest.endpoint,
            auth = subscriberRequest.auth,
            topicUUID = topicUUID
        ).let { this.subscriberAggregate.handle(it) }
    }

    fun getTopics(): Flux<Topic> {
        return TopicAllQuery(false)
            .let { this.topicProjection.handle(it) }
    }

    fun getTopic(uuid: UUID, getHistory: Boolean): Mono<Topic> {
        return TopicByUUIDQuery(uuid, true, getHistory)
            .let { this.topicProjection.handle(it) }
    }

    fun createTopic(topicRequest: NewTopicRequest): Mono<Topic> {
        return TopicCreateCommand(
            name = topicRequest.topicName,
            description = topicRequest.description
        ).let { this.topicAggregate.handle(it) }
    }

    fun deleteTopic(uuid: UUID): Mono<Void> {
        return TopicDeleteCommand(uuid)
            .let { this.topicAggregate.handle(it) }
    }

    fun triggerTopic(triggerTopicRequest: TriggerTopicRequest, topicUUID: UUID): Flux<Response> {
        val pushMessage = triggerTopicRequest.toPushMessage()
        return TopicByUUIDQuery(
                uuid = topicUUID,
                getSubscribers = true,
                getHistory = false)
            .let { this.topicProjection.handle(it) }
            .flatMapMany { this.pushMessageSender.sendMessagesToTopicSubscribers(it, pushMessage) }
    }

    fun newTopicMakeRequest(newTopicMakeRequest: NewTopicMakeRequest): Mono<Boolean> {
        return this.newTopicMakeRequester.request(newTopicMakeRequest.description)
    }
}