package dev.lutergs.lutergsbackend.infra.repository.push

import dev.lutergs.lutergsbackend.domain.push.TopicWriteRepository
import dev.lutergs.lutergsbackend.domain.push.topic.Topic
import dev.lutergs.lutergsbackend.infra.repository.push.database.TopicEntity
import dev.lutergs.lutergsbackend.infra.repository.push.database.TopicEntityRepository
import dev.lutergs.lutergsbackend.infra.repository.push.database.TopicSubscriberRelationEntityRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
class TopicWriteRepositoryImpl(
    private val topicEntityRepository: TopicEntityRepository,
    private val topicSubscriberRelationEntityRepository: TopicSubscriberRelationEntityRepository
): TopicWriteRepository {
    override fun saveTopic(topic: Topic): Mono<Topic> {
        return TopicEntity.fromNotRelatedTopic(topic)
            .let { this.topicEntityRepository.save(it) }
            .flatMap { Mono.just(it.toNotRelatedTopic()) }
    }

    override fun deleteTopic(uuid: UUID): Mono<Void> {
        val uuidString = uuid.toString()
        return this.topicSubscriberRelationEntityRepository.findAllByTopicUUID(uuidString)
            .flatMap { this.topicSubscriberRelationEntityRepository.delete(it) }
            .then(Mono.defer {
                this.topicEntityRepository.findDistinctFirstByUuid(uuidString)
                    .flatMap { this.topicEntityRepository.delete(it) }
            })
    }
}