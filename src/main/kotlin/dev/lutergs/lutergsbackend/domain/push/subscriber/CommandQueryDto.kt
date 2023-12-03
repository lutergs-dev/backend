package dev.lutergs.lutergsbackend.domain.push.subscriber

import java.util.*


data class SubscriberIsValidQuery (
    val endpoint: String
)

data class SubscriberByEndpointQuery (
    val endpoint: String,
    val auth: String,
    val getSubscribedTopics: Boolean,
    val getTopicReceiveHistory: Boolean
) {
    init {
        if (!getSubscribedTopics && getTopicReceiveHistory) {
            throw IllegalStateException("토픽을 가져오지 않기로 했지만, 토픽 메시지 기록을 가져오기를 요청했습니다.")
        }
    }
}

data class SubscriberByAuthQuery(
    val auth: String,
    val getSubscribedTopics: Boolean,
    val getTopicReceiveHistory: Boolean
) {
    init {
        if (!getSubscribedTopics && getTopicReceiveHistory) {
            throw IllegalStateException("토픽을 가져오지 않기로 했지만, 토픽 메시지 기록을 가져오기를 요청했습니다.")
        }
    }
}

data class SubscriberCreateCommand(
    val endpoint: String,
    val auth: String,
    val key: String
)

data class SubscribeToTopicCommand(
    val endpoint: String,
    val auth: String,
    val topicUUID: UUID
)

data class UnsubscribeToTopicCommand(
    val endpoint: String,
    val auth: String,
    val topicUUID: UUID
)