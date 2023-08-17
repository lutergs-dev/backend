package dev.lutergs.lutergsbackend.service.pushnotification

import java.util.UUID


data class Subscription(
    val auth: String,
    val key: String,
    val endpoint: String,
    val topics: List<Topic>?
)

data class Topic(
    val uuid: String,
    val name: String,
    val description: String,
    val type: TopicType,
    val subscriptions: List<Subscription>?
)

enum class TopicType {
    FIXED,           // this topic is bind to all subscription and cannot unsubscribe
    UNSUBSCRIBABLE   // this topic can subscribe / unsubscribe
}

data class PushMessage(
    val title: String,
    val body: String,
    val iconUrl: String?
)

