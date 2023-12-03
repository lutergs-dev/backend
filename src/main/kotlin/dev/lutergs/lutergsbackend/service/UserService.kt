package dev.lutergs.lutergsbackend.service

import dev.lutergs.lutergsbackend.domain.user.*
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

class UserService(
    private val oAuthRequester: OAuthRequester,
    private val userRepository: UserRepository,
    private val tokenGenerator: TokenGenerator
) {
    private val validNicknameRegex = Regex("^[A-Za-z0-9\\-_'\\.]+$")

    // get user info using JWT token
    fun getUser(token: String): Mono<User> {
        return Mono.fromCallable { this.tokenGenerator.getEmailFromToken(token) }
            .flatMap { this.userRepository.getUserByEmail(it) }
            .switchIfEmpty { Mono.error(NotFoundException()) }
    }

    fun changeUserName(token: String, rawNewNickname: String): Mono<User> {
        return if (validNicknameRegex.matches(rawNewNickname)) {
            this.getUser(token)
                .flatMap { oldUser ->
                    val newNickname = NickName(rawNewNickname)
                    this.userRepository.checkUserExistsByNickName(newNickname)
                        .flatMap {
                            if (!it) oldUser
                                .updateNickName(newNickname)
                                .let { newUser -> this.userRepository.changeNickName(newUser) }
                            else Mono.error(IllegalStateException("이미 사용중인 닉네임입니다."))
                        }
                }
        } else {
            Mono.error(IllegalArgumentException("잘못된 닉네임 형식입니다."))
        }
    }

    fun signUp(code: String, redirectionUrl: String): Mono<String> {
        return this.oAuthRequester.getUserByCode(code, redirectionUrl)
            .flatMap {
                this.userRepository.getUserByEmail(it.email)
                    .switchIfEmpty { this.userRepository.saveUser(it) } }
            .flatMap { Mono.fromCallable { this.tokenGenerator.createTokenFromUser(it) } }
    }
}