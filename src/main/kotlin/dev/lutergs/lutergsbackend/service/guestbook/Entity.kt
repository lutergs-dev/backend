package dev.lutergs.lutergsbackend.service.guestbook

import java.time.LocalDateTime

data class Comment(
    val name: String,
    val password: String,
    val value: String,
    val createdAt: LocalDateTime
) {
    fun maskingPassword(): Comment {
        return Comment(
            name = name,
            password = "omg... password is hidden bro",
            value = value,
            createdAt = createdAt
        )
    }
}