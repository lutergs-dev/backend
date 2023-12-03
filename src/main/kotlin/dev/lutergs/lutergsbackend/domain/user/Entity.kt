package dev.lutergs.lutergsbackend.domain.user

import java.lang.IllegalArgumentException
import java.time.LocalDateTime
import java.util.regex.Pattern

data class User(
    val id: Long?,
    val email: Email,               // this is the primary key
    val createdAt: LocalDateTime,
    val nickName: NickName
) {
    fun updateNickName(nickName: NickName): User {
        return User(this.id, this.email, this.createdAt, nickName)
    }

    fun isSaved() = this.id != null
}

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

data class NickName(
    val value: String
)