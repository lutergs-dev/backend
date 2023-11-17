package dev.lutergs.lutergsbackend.repository.pushRepositoryImpl

import dev.lutergs.lutergsbackend.service.pushnotification.Subscription
import dev.lutergs.lutergsbackend.service.pushnotification.Topic
import dev.lutergs.lutergsbackend.service.pushnotification.TopicType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.OffsetDateTime


@Table("push_subscription")
class SubscriptionEntity {
    @Id                     var id: Long? = null
    @Column("endpoint")     var endpoint: String = ""
    @Column("auth")         var auth: String = ""
    @Column("key")          var key: String = ""
    @Column("created_at")   var createdAt: OffsetDateTime = OffsetDateTime.now()
    @Column("last_live_at") var lastLiveAt: OffsetDateTime = OffsetDateTime.now()

    fun toNotRelatedSubscription(): Subscription {
        return Subscription(this.auth, this.key, this.endpoint, null)
    }

    fun toRelatedSubscription(topicEntities: List<TopicEntity>): Subscription {
        return Subscription(this.auth, this.key, this.endpoint, topicEntities.map { it.toNotRelatedTopic() })
    }

    fun toRelatedSubscription(topicEntities: Flux<TopicEntity>): Mono<Subscription> {
        return topicEntities.flatMap { Mono.just(it.toNotRelatedTopic()) }
            .collectList()
            .defaultIfEmpty(listOf())
            .flatMap { Mono.just(Subscription(this.auth, this.key, this.endpoint, it)) }
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
    @Column("uuid")         var uuid: String = ""
    @Column("name")         var name: String = ""
    @Column("description")  var description: String = ""
    @Column("type")         var type: String = ""
    @Column("deleted")      var deleted: String = ""
    @Column("created_at")   var createdAt: OffsetDateTime = OffsetDateTime.now()

    fun toNotRelatedTopic(): Topic {
        return Topic(this.uuid, this.name, this.description, TopicType.valueOf(this.type), null)
    }

    fun toRelatedTopic(subscriptionEntities: List<SubscriptionEntity>): Topic {
        return Topic(this.uuid, this.name, this.description, TopicType.valueOf(this.type), subscriptionEntities.map { it.toNotRelatedSubscription() })
    }

    fun toRelatedTopic(subscriptionEntities: Flux<SubscriptionEntity>): Mono<Topic> {
        return subscriptionEntities.flatMap { Mono.just(it.toNotRelatedSubscription()) }
            .collectList()
            .flatMap { Mono.just(Topic(this.uuid, this.name, this.description, TopicType.valueOf(this.type), it)) }
    }

    companion object {
        fun fromNotRelatedTopic(topic: Topic): TopicEntity {
            return TopicEntity().apply {
                this.uuid = topic.uuid
                this.name = topic.name
                this.description = topic.description
                this.type = topic.type.toString()
            }
        }

    }
}

@Table("push_topic_subscription_list")
class TopicSubscriptionListEntity {
    @Id                         var id: Long? = null
    @Column("subscription_id")  var subscriptionId: Long = 0
    @Column("topic_id")         var topicId: Long = 0
}


@Repository
interface SubscriptionEntityRepository: ReactiveCrudRepository<SubscriptionEntity, Long> {
    fun findDistinctFirstByEndpoint(endpoint: String): Mono<SubscriptionEntity>
    fun findDistinctFirstByAuth(auth: String): Mono<SubscriptionEntity>
}

@Repository
interface TopicEntityRepository: ReactiveCrudRepository<TopicEntity, Long> {
    fun findDistinctFirstByUuid(uuid: String): Mono<TopicEntity>
}

@Repository
interface TopicSubscriptionListEntityRepository: ReactiveCrudRepository<TopicSubscriptionListEntity, Long> {
    fun findAllBySubscriptionId(subscriptionId: Long): Flux<TopicSubscriptionListEntity>
    fun findAllByTopicId(topicId: Long): Flux<TopicSubscriptionListEntity>
    fun findDistinctFirstBySubscriptionIdAndTopicId(subscriptionId: Long, topicId: Long): Mono<TopicSubscriptionListEntity>
}


@Repository
class PushEntityRepository(
    private val subscriptionEntityRepository: SubscriptionEntityRepository,
    private val topicEntityRepository: TopicEntityRepository,
    private val topicSubscriptionListEntityRepository: TopicSubscriptionListEntityRepository
) {
    fun getSubscriptionEntities(): Flux<SubscriptionEntity> {
        return this.subscriptionEntityRepository.findAll()
    }

    fun saveNotRelatedSubscription(subscriptionEntity: SubscriptionEntity): Mono<SubscriptionEntity> {
        return this.subscriptionEntityRepository.save(subscriptionEntity)
    }

    fun getTopics(): Flux<TopicEntity> {
        return this.topicEntityRepository.findAll()
    }

    fun saveNotRelatedTopic(topic: Topic): Mono<TopicEntity> {
        return TopicEntity.fromNotRelatedTopic(topic)
            .let { this.topicEntityRepository.save(it) }
    }

    fun deleteTopic(topicEntity: TopicEntity): Mono<Void> {
        return this.topicSubscriptionListEntityRepository
            .findAllByTopicId(topicEntity.id!!)
            .flatMap { tsEntity -> this.subscriptionEntityRepository
                .findById(tsEntity.subscriptionId)
                .flatMap {
                    this.topicSubscriptionListEntityRepository
                        .delete(tsEntity)
                        .then(Mono.just(it))
                }
            }
            .flatMap { this.subscriptionEntityRepository.delete(it) }
            .then(this.topicEntityRepository.delete(topicEntity))
    }

    fun findNotRelatedTopicByUUID(uuid: String): Mono<TopicEntity> {
        return this.topicEntityRepository.findDistinctFirstByUuid(uuid)
    }

    fun getRelatedSubscriptions(topicEntity: TopicEntity): Flux<SubscriptionEntity> {
        return this.topicSubscriptionListEntityRepository
            .findAllByTopicId(topicEntity.id!!)
            .flatMap { this.subscriptionEntityRepository.findById(it.subscriptionId) }
    }

    fun getSubscribedTopics(subscriptionEntity: SubscriptionEntity): Flux<TopicEntity> {
        return this.topicSubscriptionListEntityRepository
            .findAllBySubscriptionId(subscriptionEntity.id!!)
            .flatMap { this.topicEntityRepository.findById(it.topicId) }
    }

    fun deleteSubscriptionEntity(subscriptionEntity: SubscriptionEntity): Mono<Void> {
        return this.topicSubscriptionListEntityRepository.findAllBySubscriptionId(subscriptionEntity.id!!)
            .flatMap { this.topicSubscriptionListEntityRepository.delete(it) }
            .then(this.subscriptionEntityRepository.delete(subscriptionEntity))
    }

    fun findSubscriptionEntityByAuth(auth: String): Mono<SubscriptionEntity> {
        return this.subscriptionEntityRepository.findDistinctFirstByAuth(auth)
    }

    fun pairSubscriptionAndTopic(subscriptionEntity: SubscriptionEntity, topicEntity: TopicEntity): Mono<TopicSubscriptionListEntity> {
        if (subscriptionEntity.id == null || topicEntity.id == null) throw IllegalStateException("영속성 조회가 되지 않았습니다.")
        return TopicSubscriptionListEntity()
            .apply {
                this.subscriptionId = subscriptionEntity.id!!
                this.topicId = topicEntity.id!!
            }.let {
                this.topicSubscriptionListEntityRepository.save(it)
            }
    }

    fun unPairSubscriptionAndTopic(topicSubscriptionListEntity: TopicSubscriptionListEntity): Mono<Void> {
        if (topicSubscriptionListEntity.id == null) throw IllegalStateException("영속성 조회가 되지 않았습니다.")
        return this.topicSubscriptionListEntityRepository.delete(topicSubscriptionListEntity)
    }

    fun findTopicSubscriptionList(subscriptionEntity: SubscriptionEntity, topicEntity: TopicEntity): Mono<TopicSubscriptionListEntity> {
        if (subscriptionEntity.id == null || topicEntity.id == null) throw IllegalStateException("영속성 조회가 되지 않았습니다.")
        return this.topicSubscriptionListEntityRepository.findDistinctFirstBySubscriptionIdAndTopicId(
            subscriptionEntity.id!!, topicEntity.id!!
        )
    }
}


