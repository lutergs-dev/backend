package dev.lutergs.lutergsbackend.infra.repository.push

import dev.lutergs.lutergsbackend.domain.push.SubscriberReadRepository
import dev.lutergs.lutergsbackend.domain.push.subscriber.Subscriber
import dev.lutergs.lutergsbackend.domain.push.topic.MessageHistory
import dev.lutergs.lutergsbackend.domain.push.topic.Topic
import dev.lutergs.lutergsbackend.infra.repository.push.database.PushMessageHistoryRepository
import dev.lutergs.lutergsbackend.infra.repository.push.database.SubscriberEntityRepository
import dev.lutergs.lutergsbackend.infra.repository.push.database.TopicEntityRepository
import dev.lutergs.lutergsbackend.infra.repository.push.database.TopicSubscriberRelationEntityRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.lang.IllegalStateException

@Repository
class SubscriberReadRepositoryImpl(
    private val subscriberEntityRepository: SubscriberEntityRepository,
    private val topicSubscriberRelationEntityRepository: TopicSubscriberRelationEntityRepository,
    private val topicEntityRepository: TopicEntityRepository,
    private val pushMessageHistoryRepository: PushMessageHistoryRepository
): SubscriberReadRepository {
    override fun getSubscriberByEndpoint(endpoint: String): Mono<Subscriber> {
        return this.subscriberEntityRepository.findDistinctFirstByEndpoint(endpoint)
            .flatMap { Mono.just(it.toNotRelatedSubscriber()) }
    }

    override fun getSubscriberByAuth(auth: String): Mono<Subscriber> {
        return this.subscriberEntityRepository.findDistinctFirstByAuth(auth)
            .flatMap { Mono.just(it.toNotRelatedSubscriber()) }
    }

    override fun getSubscribedTopic(subscriber: Subscriber): Mono<Subscriber> {
        return this.topicSubscriberRelationEntityRepository.findAllBySubscriberAuth(subscriberAuth = subscriber.auth)
            .flatMap { this.topicEntityRepository.findById(it.topicId) }
            .flatMap { Mono.just(it.toNotRelatedTopic()) }
            .collectList()
            .flatMap {
                Mono.just(Subscriber(
                auth = subscriber.auth,
                key = subscriber.key,
                endpoint = subscriber.endpoint,
                topics = it
            )) }
    }

    override fun getSubscribedTopicsMessageHistory(subscriber: Subscriber): Mono<Subscriber> {
        return takeIf { subscriber.topics.isNotEmpty() }
            ?.let { this.pushMessageHistoryRepository.findAllBySubscriberAuth(subscriber.auth) }
            ?.reduce(mutableMapOf<String, MutableList<MessageHistory>>()) { accumulator, value ->
                accumulator[value.topicUUID]
                    ?.add(value.toMessageHistory())
                    ?: accumulator.put(value.topicUUID, mutableListOf(value.toMessageHistory()))
                accumulator }
            ?.flatMap { historyByUUID -> Mono.fromCallable { Subscriber (
                auth = subscriber.auth,
                key = subscriber.key,
                endpoint = subscriber.endpoint,
                topics = subscriber.topics.map { Topic(
                    uuid = it.uuid,
                    name = it.name,
                    description = it.description,
                    subscribers = it.subscribers,
                    history = historyByUUID[it.uuid.toString()] ?: listOf()) } ) } }
            ?: Mono.error(IllegalStateException("topic 을 조회하지 않은 상태에서 history 조회를 요청했거나, 구독한 topic 이 없습니다."))
    }
}