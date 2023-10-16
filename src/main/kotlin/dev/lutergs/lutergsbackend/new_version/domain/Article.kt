package dev.lutergs.lutergsbackend.new_version.domain

import dev.lutergs.lutergsbackend.service.user.Email
import java.time.LocalDateTime



class User (
    val id: Long?,
    val email: Email,
    val createdAt: LocalDateTime
) {
    
}

class Article (
    val id: Long?,
    val title: String?,
    val paragraphs: List<String>,
    val createdAt: LocalDateTime
) {
}