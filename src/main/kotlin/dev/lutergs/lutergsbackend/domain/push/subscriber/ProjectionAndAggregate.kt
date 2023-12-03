package dev.lutergs.lutergsbackend.domain.push.subscriber

import dev.lutergs.lutergsbackend.domain.push.SubscriberReadRepository
import dev.lutergs.lutergsbackend.domain.push.SubscriberWriteRepository
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty


class SubscriberProjection (
    private val subscriberReadRepository: SubscriberReadRepository
) {

    fun handle(query: SubscriberIsValidQuery): Mono<Boolean> {
        return this.subscriberReadRepository.getSubscriberByEndpoint(query.endpoint)
            .hasElement()
    }
    fun handle(query: SubscriberByEndpointQuery): Mono<Subscriber> {
        return this.subscriberReadRepository.getSubscriberByEndpoint(query.endpoint)
            .filter { it.auth == query.auth }
            .flatMap { this.getTopicsAndHistory(it, query.getSubscribedTopics, query.getTopicReceiveHistory) }
            .switchIfEmpty { Mono.error(IllegalArgumentException("provided endpoint is not valid or wrong auth is given")) }
    }

    fun handle(query: SubscriberByAuthQuery): Mono<Subscriber> {
        return this.subscriberReadRepository.getSubscriberByAuth(query.auth)
            .flatMap { this.getTopicsAndHistory(it, query.getSubscribedTopics, query.getTopicReceiveHistory) }
    }

    private fun getTopicsAndHistory(subscriber: Subscriber, getTopic: Boolean, getHistory: Boolean): Mono<Subscriber> {
        return if (getTopic) this.subscriberReadRepository.getSubscribedTopic(subscriber)
            .flatMap { if (getHistory) this.subscriberReadRepository.getSubscribedTopicsMessageHistory(it) else Mono.just(it)  }
        else Mono.just(subscriber)
    }
}

class SubscriberAggregate (
    private val subscriberWriteRepository: SubscriberWriteRepository
) {
    fun handle(command: SubscriberCreateCommand): Mono<Subscriber> {
        return Subscriber(command.auth, command.key, command.endpoint, listOf())
            .let { this.subscriberWriteRepository.saveSubscriber(it) }
    }

    fun handle(command: SubscribeToTopicCommand): Mono<Void> {
        return this.subscriberWriteRepository.subscribeToTopic(command.endpoint, command.auth, command.topicUUID)
    }

    fun handle(command: UnsubscribeToTopicCommand): Mono<Void> {
        return this.subscriberWriteRepository.unsubscribeToTopic(command.endpoint, command.auth, command.topicUUID)
    }
}