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
    val subscriptions: List<Subscription>?
)

