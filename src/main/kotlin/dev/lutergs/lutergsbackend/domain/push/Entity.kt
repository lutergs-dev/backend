package dev.lutergs.lutergsbackend.domain.push


data class PushMessageRequest(
    val title: String,
    val body: String,
    val iconUrl: String?
)

