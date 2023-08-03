package dev.lutergs.lutergsbackend.repository.pushRepositoryImpl

import dev.lutergs.lutergsbackend.service.pushnotification.Subscription
import dev.lutergs.lutergsbackend.service.pushnotification.Topic
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@Table("push_subscription")
class SubscriptionEntity {
    @Id                     var id: Long? = null
    @Column("endpoint")     var endpoint: String = ""
    @Column("auth")         var auth: String = ""
    @Column("key")          var key: String = ""

    fun toNotRelatedSubscription(): Subscription {
        return Subscription(this.auth, this.key, this.endpoint, null)
    }

    companion object {
        fun fromNotRelatedSubscription(subscription: Subscription): SubscriptionEntity {
            return SubscriptionEntity().apply {
                this.endpoint = subscription.endpoint
                this.auth = subscription.auth
                this.key = subscription.key
            }
        }
    }
}

@Table("push_topic")
class TopicEntity {
    @Id                     var id: Long? = null
    @Column("uuid")         var uuid: String = ""       // TODO : need unique key
    @Column("name")         var name: String = ""
    @Column("description")  var descroption: String = ""

    fun toNotRelatedTopic(): Topic {
        return Topic(this.uuid, this.name, this.descroption, null)
    }

    companion object {
        fun fromNotRelatedTopic(topic: Topic): TopicEntity {
            return TopicEntity().apply {
                this.uuid = topic.uuid
                this.name = topic.name
                this.descroption = topic.description
            }
        }
    }
}

@Table("push_topic_subscription_list")
class TopicSubscriptionListEntity {
    @Id                     var id: Long? = null
    @Column("sub_id")       var subscriptionId: Long = 0
    @Column("topic_id")     var topicId: Long = 0
}


@Repository
interface SubscriptionEntityRepository: ReactiveCrudRepository<SubscriptionEntity, Long> {
    fun findDistinctFirstByEndpoint(endpoint: String): Mono<SubscriptionEntity>
}

@Repository
interface TopicEntityRepository: ReactiveCrudRepository<TopicEntity, Long> {
    fun findDistinctFirstByUuid(uuid: String): Mono<TopicEntity>
}

@Repository
interface TopicSubscriptionListEntityRepository: ReactiveCrudRepository<TopicSubscriptionListEntity, Long> {
    fun findAllBySubscriptionId(subscriptionId: Long): Flux<TopicSubscriptionListEntity>
    fun findAllByTopicId(topicId: Long): Flux<TopicSubscriptionListEntity>
}


@Repository
class PushEntityRepository(
    private val subscriptionEntityRepository: SubscriptionEntityRepository,
    private val topicEntityRepository: TopicEntityRepository,
    private val topicSubscriptionListEntityRepository: TopicSubscriptionListEntityRepository
) {

    @Transactional
    fun toSubscription(subscriptionEntity: SubscriptionEntity): Mono<Subscription> {
        return this.topicSubscriptionListEntityRepository
            .findAllBySubscriptionId(subscriptionEntity.id!!)
            .flatMap { this.topicEntityRepository.findById(it.topicId) }
            .collectList()
            .flatMap { topicList ->
                Mono.just(Subscription(
                    subscriptionEntity.auth,
                    subscriptionEntity.key,
                    subscriptionEntity.endpoint,
                    topicList.map { it.toNotRelatedTopic() }
                ))
            }
    }

    @Transactional
    fun toTopic(topicEntity: TopicEntity): Mono<Topic> {
        return this.topicSubscriptionListEntityRepository
            .findAllByTopicId(topicEntity.id!!)
            .flatMap { this.subscriptionEntityRepository.findById(it.subscriptionId) }
            .collectList()
            .flatMap { subscriptionList ->
                Mono.just(Topic(
                    topicEntity.uuid,
                    topicEntity.name,
                    topicEntity.descroption,
                    subscriptionList.map { it.toNotRelatedSubscription() }
                ))
            }
    }

    @Transactional
    fun saveNotRelatedSubscription(subscription: Subscription): Mono<SubscriptionEntity> {
        return SubscriptionEntity.fromNotRelatedSubscription(subscription)
            .let {
                println(it.key)
                println(it.auth)
                println(it.endpoint)

                this.subscriptionEntityRepository.save(it)
            }
    }

    fun getTopics(): Flux<Topic> {
        return this.topicEntityRepository.findAll()
            .flatMap { Mono.just(it.toNotRelatedTopic()) }
    }

    fun saveNotRelatedTopic(topic: Topic): Mono<TopicEntity> {
        return TopicEntity.fromNotRelatedTopic(topic)
            .let { this.topicEntityRepository.save(it) }
    }

    fun findTopicByUUID(uuid: String): Mono<Topic> {
        return this.topicEntityRepository.findDistinctFirstByUuid(uuid)
            .flatMap { this.toTopic(it) }
    }

    fun findSubscriptionByEndpoint(endpoint: String): Mono<SubscriptionEntity> {
        return this.subscriptionEntityRepository.findDistinctFirstByEndpoint(endpoint)
    }

    @Transactional
    fun deleteSubscriptionEntity(subscriptionEntity: SubscriptionEntity): Flux<Void> {
        return this.topicSubscriptionListEntityRepository.findAllBySubscriptionId(subscriptionEntity.id!!)
            .flatMap { this.topicSubscriptionListEntityRepository.delete(it) }
            .flatMap { this.subscriptionEntityRepository.delete(subscriptionEntity) }
    }
}


