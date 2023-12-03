package dev.lutergs.lutergsbackend.infra.repository.push

import dev.lutergs.lutergsbackend.domain.push.SubscriberWriteRepository
import dev.lutergs.lutergsbackend.domain.push.subscriber.Subscriber
import dev.lutergs.lutergsbackend.infra.repository.push.database.SubscriberEntity
import dev.lutergs.lutergsbackend.infra.repository.push.database.SubscriberEntityRepository
import dev.lutergs.lutergsbackend.infra.repository.push.database.TopicSubscriberRelationEntityRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.*

@Repository
class SubscriberWriteRepositoryImpl(
    private val subscriberEntityRepository: SubscriberEntityRepository,
    private val topicSubscriberRelationEntityRepository: TopicSubscriberRelationEntityRepository
): SubscriberWriteRepository {
    override fun saveSubscriber(subscriber: Subscriber): Mono<Subscriber> {
        return this.subscriberEntityRepository.findDistinctFirstByEndpoint(subscriber.endpoint)
            .flatMap {          // endpoint 기준이기 때문에, 이미 endpoint 가 존재할 경우 auth, key 만 overwrite 함
                it.auth = subscriber.auth
                it.key = subscriber.key
                this.subscriberEntityRepository.save(it) }
            .switchIfEmpty { SubscriberEntity.fromNotRelatedSubscriber(subscriber)
                .let { this.subscriberEntityRepository.save(it) } }
            .flatMap { Mono.fromCallable { it.toNotRelatedSubscriber() } }
    }

    override fun subscribeToTopic(endpoint: String, auth: String, topicUUID: UUID): Mono<Void> {
        return this.subscriberEntityRepository.findDistinctFirstByEndpoint(endpoint)
            .filter { it.auth == auth }
            .switchIfEmpty { Mono.error(IllegalArgumentException("provided endpoint is not valid or wrong auth is given")) }
            .flatMap { this.topicSubscriberRelationEntityRepository.relateTopicAndSubscriber(it.auth, topicUUID.toString()) }
            .onErrorMap { when (it) {
                is DuplicateKeyException -> IllegalArgumentException("already subscribed given topic")
                else -> it
            } }
    }

    override fun unsubscribeToTopic(endpoint: String, auth: String, topicUUID: UUID): Mono<Void> {
        return this.subscriberEntityRepository.findDistinctFirstByEndpoint(endpoint)
            .filter { it.auth == auth }
            .switchIfEmpty { Mono.error(IllegalArgumentException("provided endpoint is not valid or wrong auth is given")) }
            .flatMap {
                println("${it.id}, ${it.auth}, ${it.key}")
                this.topicSubscriberRelationEntityRepository.findBySubscriberAuthAndTopicUUID(it.auth, topicUUID.toString())
            }
            .flatMap {
                println("${it.id}, ${it.topicId}, ${it.subscriptionId}")
                this.topicSubscriberRelationEntityRepository.delete(it)
            }
    }
}