package dev.lutergs.lutergsbackend.infra.repository.push.database

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface SubscriberEntityRepository: ReactiveCrudRepository<SubscriberEntity, Long> {
    fun findDistinctFirstByEndpoint(endpoint: String): Mono<SubscriberEntity>
    fun findDistinctFirstByAuth(auth: String): Mono<SubscriberEntity>
}

@Repository
interface TopicEntityRepository: ReactiveCrudRepository<TopicEntity, Long> {
    fun findDistinctFirstByUuid(uuid: String): Mono<TopicEntity>
}

@Repository
interface TopicSubscriberRelationEntityRepository: ReactiveCrudRepository<TopicSubscriberRelationEntity, Long> {

    @Query("SELECT PTSR.* " +
            "FROM PUSH_SUBSCRIBER PS " +
            "JOIN PUSH_TOPIC_SUBSCRIBER_RELATION PTSR ON PS.ID = PTSR.SUBSCRIBER_ID " +
            "WHERE PS.AUTH = :subscriberAuth")
    fun findAllBySubscriberAuth(@Param("subscriberAuth") subscriberAuth: String): Flux<TopicSubscriberRelationEntity>

    @Query("SELECT PTSR.* " +
            "FROM PUSH_TOPIC PT " +
            "JOIN PUSH_TOPIC_SUBSCRIBER_RELATION PTSR ON PT.ID = PTSR.TOPIC_ID " +
            "WHERE PT.UUID = :topicUUID")
    fun findAllByTopicUUID(@Param("topicUUID") topicUUID: String): Flux<TopicSubscriberRelationEntity>

    fun findAllBySubscriptionId(subscriptionId: Long): Flux<TopicSubscriberRelationEntity>
    @Modifying
    @Query("INSERT INTO PUSH_TOPIC_SUBSCRIBER_RELATION (TOPIC_ID, SUBSCRIBER_ID) " +
            "SELECT " +
            "    PT.ID AS TOPIC_ID," +
            "    PS.ID AS SUBSCRIBER_ID " +
            "FROM " +
            "    PUSH_SUBSCRIBER PS " +
            "JOIN " +
            "    PUSH_TOPIC PT ON PS.AUTH = :subscriberAuth " +
            "WHERE " +
            "    PT.UUID = :topicUUID")        // ; 붙이면 에러남.
    @Transactional
    fun relateTopicAndSubscriber(
        @Param("subscriberAuth")    subscriberAuth: String,
        @Param("topicUUID")         topicUUID: String
    ): Mono<Void>

    @Query("SELECT DISTINCT PTSR.* " +
            "FROM PUSH_TOPIC_SUBSCRIBER_RELATION PTSR " +
            "JOIN PUSH_SUBSCRIBER PS ON PTSR.SUBSCRIBER_ID = PS.ID " +
            "JOIN PUSH_TOPIC PT ON PTSR.TOPIC_ID = PT.ID " +
            "WHERE PS.AUTH = :subscriberAuth " +
            "AND PT.UUID = :topicUUID")
    fun findBySubscriberAuthAndTopicUUID(
        @Param("subscriberAuth")    subscriberAuth: String,
        @Param("topicUUID")         topicUUID: String
    ): Mono<TopicSubscriberRelationEntity>
}

@Repository
interface PushMessageHistoryRepository: ReactiveMongoRepository<PushMessageHistory, String> {
    fun deleteAllBySubscriberAuth(subscriberAuth: String): Mono<Void>
    fun findAllBySubscriberAuth(subscriberAuth: String): Flux<PushMessageHistory>
    fun findAllByTopicUUID(topicUUID: String): Flux<PushMessageHistory>
}