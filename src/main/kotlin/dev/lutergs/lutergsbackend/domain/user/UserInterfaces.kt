package dev.lutergs.lutergsbackend.domain.user

import reactor.core.publisher.Mono

interface OAuthRequester {
    fun getUserByCode(code: String, redirectionUrl: String): Mono<User>
}

interface UserRepository {
    fun getUserByEmail(email: Email): Mono<User>
    fun getUserByNickname(nickname: NickName): Mono<User>
    fun checkUserExistsByNickName(nickName: NickName): Mono<Boolean>
    fun saveUser(user: User): Mono<User>
    fun changeNickName(user: User): Mono<User>
}

interface TokenGenerator {
    fun createTokenFromUser(user: User): String
    fun getEmailFromToken(token: String): Email
}