package dev.lutergs.lutergsbackend.service.user

import dev.lutergs.lutergsbackend.controller.RawUser
import dev.lutergs.lutergsbackend.repository.UserEntity
import java.time.LocalDateTime
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

data class User(
    val id: String,
    val password: String        // need to implement basic auth (salt and encode)
    val createdAt: LocalDateTime
) {
    companion object {
        fun fromRawUser(rawUser: RawUser, salt: String): User {
            return Cipher.getInstance("AES/ECB/PKCS5Padding")
                .apply { this.init(
                    Cipher.ENCRYPT_MODE,
                    SecretKeySpec(salt.toByteArray(), "AES")) }
                .doFinal(rawUser.password.toByteArray())
                .let { Base64.getEncoder().encodeToString(it) }
                .let { User(id = rawUser.id, password = it, createdAt = LocalDateTime.now()) }
        }
    }
}