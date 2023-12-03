package dev.lutergs.lutergsbackend.domain.push.topic

import dev.lutergs.lutergsbackend.domain.push.subscriber.Subscriber
import java.time.OffsetDateTime
import java.util.*

data class Topic(
    val uuid: UUID,
    val name: String,
    val description: String,
    val subscribers: List<Subscriber>,
    val history: List<MessageHistory>
)

data class MessageHistory(
    val title: String,
    val body: String,
    val iconUrl: String?,
    val sendAt: OffsetDateTime
)