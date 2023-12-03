package dev.lutergs.lutergsbackend.domain.push.topic

import java.util.*

data class TopicByUUIDQuery(
    val uuid: UUID,
    val getSubscribers: Boolean,
    val getHistory: Boolean
)

data class TopicAllQuery(
    val getSubscribers: Boolean
)

data class TopicCreateCommand(
    val name: String,
    val description: String
)

data class TopicDeleteCommand(
    val uuid: UUID
)