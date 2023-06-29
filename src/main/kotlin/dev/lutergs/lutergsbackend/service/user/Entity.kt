package dev.lutergs.lutergsbackend.service.user

import java.lang.IllegalArgumentException
import java.time.LocalDateTime
import java.util.regex.Pattern

data class User(
    val email: Email,
    val createdAt: LocalDateTime?
)

data class Email private constructor(
    val username: String,
    val provider: String
) {
    companion object {

        private val emailPattern = Pattern.compile("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        fun fromFullString(value: String): Email {
            if (isValidEmail(value)) {
                val (username, provider) = value.split("@")
                return Email(username, provider)
            }
            throw IllegalArgumentException("Not valid email!")
        }

        private fun isValidEmail(value: String): Boolean {
            return this.emailPattern
                .matcher(value)
                .matches()
        }
    }

    override fun toString(): String {
        return "${this.username}@${this.provider}"
    }
}