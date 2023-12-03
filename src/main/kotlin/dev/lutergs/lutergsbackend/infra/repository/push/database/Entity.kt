package dev.lutergs.lutergsbackend.infra.repository.push.database

import dev.lutergs.lutergsbackend.domain.push.subscriber.Subscriber
import dev.lutergs.lutergsbackend.domain.push.topic.MessageHistory
import dev.lutergs.lutergsbackend.domain.push.topic.Topic
import dev.lutergs.lutergsbackend.utils.toOffsetDateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime
import java.util.*


@Table("PUSH_SUBSCRIBER")
class SubscriberEntity {
    @Id                     var id: Long? = null
    @Column("ENDPOINT")     var endpoint: String = ""
    @Column("AUTH")         var auth: String = ""
    @Column("KEY")          var key: String = ""
    @Column("CREATED_AT")   var createdAt: OffsetDateTime = OffsetDateTime.now()

    fun toNotRelatedSubscriber(): Subscriber {
        return Subscriber(this.auth, this.key, this.endpoint, listOf())
    }

    companion object {
        fun fromNotRelatedSubscriber(subscriber: Subscriber): SubscriberEntity {
            return SubscriberEntity().apply {
                this.endpoint = subscriber.endpoint
                this.auth = subscriber.auth
                this.key = subscriber.key
            }
        }
    }
}

@Table("PUSH_TOPIC")
class TopicEntity {
    @Id                     var id: Long? = null
    @Column("UUID")         var uuid: String = ""
    @Column("NAME")         var name: String = ""
    @Column("DESCRIPTION")  var description: String = ""
    @Column("CREATED_AT")   var createdAt: OffsetDateTime = OffsetDateTime.now()

    fun toNotRelatedTopic(history: List<MessageHistory> = listOf()): Topic {
        return Topic(UUID.fromString(this.uuid), this.name, this.description, listOf(), history)
    }

    companion object {
        fun fromNotRelatedTopic(topic: Topic): TopicEntity {
            return TopicEntity().apply {
                this.uuid = topic.uuid.toString()
                this.name = topic.name
                this.description = topic.description
            }
        }

    }
}

@Table("PUSH_TOPIC_SUBSCRIBER_RELATION")
class TopicSubscriberRelationEntity {
    @Id                         var id: Long? = null
    @Column("SUBSCRIBER_ID")    var subscriptionId: Long = 0
    @Column("TOPIC_ID")         var topicId: Long = 0
}


@Document("subscriber_receive_history")
class PushMessageHistory {

    @Id
    var id: String? = null

    @Field
    @Indexed(name = "expiration_index", expireAfter = "7d")
    var expireIn7d: String = ""

    var topicUUID: String = ""
    var subscriberAuth: String = ""
    var title: String = ""
    var body: String = ""
    var iconUrl: String? = null
    var sendAt: Long = 0

    fun toMessageHistory(): MessageHistory {
        return MessageHistory(
            title = this.title,
            body = this.body,
            iconUrl = this.iconUrl,
            sendAt = this.sendAt.toOffsetDateTime()
        )
    }
}