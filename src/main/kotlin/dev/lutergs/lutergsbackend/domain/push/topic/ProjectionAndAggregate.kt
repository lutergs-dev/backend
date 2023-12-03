package dev.lutergs.lutergsbackend.domain.push.topic

import dev.lutergs.lutergsbackend.domain.push.TopicReadRepository
import dev.lutergs.lutergsbackend.domain.push.TopicWriteRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

class TopicProjection(
    private val topicReadRepository: TopicReadRepository
) {
    fun handle(query: TopicByUUIDQuery): Mono<Topic> {
        return this.topicReadRepository.getTopicByUUID(query.uuid)
            .flatMap { if (query.getSubscribers) this.topicReadRepository.getTopicSubscribers(it) else Mono.just(it) }
            .flatMap { if (query.getHistory) this.topicReadRepository.getTopicHistory(it) else Mono.just(it) }
    }

    fun handle(query: TopicAllQuery): Flux<Topic> {
        return this.topicReadRepository.getTopics()
            .flatMap { if (query.getSubscribers) this.topicReadRepository.getTopicSubscribers(it) else Mono.just(it) }
    }
}

class TopicAggregate(
    private val topicWriteRepository: TopicWriteRepository
) {
    fun handle(command: TopicCreateCommand): Mono<Topic> {
        return Topic(
            uuid = UUID.randomUUID(),
            name = command.name,
            description = command.description,
            subscribers = listOf(),
            history = listOf()
        ).let { this.topicWriteRepository.saveTopic(it) }
    }

    fun handle(command: TopicDeleteCommand): Mono<Void> {
        return this.topicWriteRepository.deleteTopic(command.uuid)
    }
}