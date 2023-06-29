package dev.lutergs.lutergsbackend.service.user

import reactor.core.publisher.Mono

interface OAuthRequester {
    fun getUserByCode(code: String, redirectionUrl: String): Mono<User>
}

interface UserRepository {
    fun getUser(email: Email): Mono<User>
    fun saveUser(user: User): Mono<User>
}

interface TokenGenerator {
    fun createTokenFromUser(user: User): String
    fun getEmailFromToken(token: String): Email
}