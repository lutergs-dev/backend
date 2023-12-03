package dev.lutergs.lutergsbackend.domain.push.subscriber

import dev.lutergs.lutergsbackend.domain.push.topic.Topic
import java.util.*

data class Subscriber(
    val auth: String,
    val key: String,
    val endpoint: String,
    val topics: List<Topic>
) {
    init { Base64.getDecoder().decode(this.key) }
}