package dev.lutergs.lutergsbackend.infra.repository.push

import dev.lutergs.lutergsbackend.domain.push.TopicReadRepository
import dev.lutergs.lutergsbackend.domain.push.topic.Topic
import dev.lutergs.lutergsbackend.infra.repository.push.database.PushMessageHistoryRepository
import dev.lutergs.lutergsbackend.infra.repository.push.database.SubscriberEntityRepository
import dev.lutergs.lutergsbackend.infra.repository.push.database.TopicEntityRepository
import dev.lutergs.lutergsbackend.infra.repository.push.database.TopicSubscriberRelationEntityRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Repository
class TopicReadRepositoryImpl(
    private val topicEntityRepository: TopicEntityRepository,
    private val subscriberEntityRepository: SubscriberEntityRepository,
    private val topicSubscriberRelationEntityRepository: TopicSubscriberRelationEntityRepository,
    private val topicHistoryRepository: PushMessageHistoryRepository
): TopicReadRepository {
    override fun getTopics(): Flux<Topic> {
        return this.topicEntityRepository.findAll()
            .flatMap { Mono.fromCallable { it.toNotRelatedTopic() } }
    }

    override fun getTopicByUUID(uuid: UUID): Mono<Topic> {
        return this.topicEntityRepository.findDistinctFirstByUuid(uuid.toString())
            .flatMap { Mono.fromCallable { it.toNotRelatedTopic() } }
    }

    override fun getTopicSubscribers(topic: Topic): Mono<Topic> {
        return this.topicSubscriberRelationEntityRepository.findAllByTopicUUID(topic.uuid.toString())
            .flatMap { this.subscriberEntityRepository.findById(it.subscriptionId) }
            .flatMap { Mono.just(it.toNotRelatedSubscriber()) }
            .collectList()
            .flatMap { Mono.just(Topic(
                uuid = topic.uuid,
                name = topic.name,
                description = topic.description,
                subscribers = it,
                history = topic.history
            )) }
    }

    override fun getTopicHistory(topic: Topic): Mono<Topic> {
        return this.topicHistoryRepository.findAllByTopicUUID(topic.uuid.toString())
            .flatMap { Mono.fromCallable { it.toMessageHistory() } }
            .collectList()
            .flatMap { Mono.fromCallable { Topic(
                uuid = topic.uuid,
                name = topic.name,
                description = topic.description,
                subscribers = topic.subscribers,
                history = topic.history) } }
    }
}