package dev.lutergs.lutergsbackend.domain.guestbook

import java.time.LocalDateTime
import java.util.*

data class Comment(
    val uuid: UUID,
    val name: String,
    val password: String,
    val value: String,
    val createdAt: LocalDateTime
) {
    fun maskingPassword(): Comment {
        return Comment(
            uuid = uuid,
            name = name,
            password = "omg... password is hidden bro",
            value = value,
            createdAt = createdAt
        )
    }
}