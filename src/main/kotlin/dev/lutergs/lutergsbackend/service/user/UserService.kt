package dev.lutergs.lutergsbackend.service.user

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class UserService(
    private val oAuthRequester: OAuthRequester,
    private val userRepository: UserRepository,
    private val tokenGenerator: TokenGenerator
) {

    // get user info using JWT token
    fun getUser(token: String): Mono<User> {
        return this.tokenGenerator.getEmailFromToken(token)
            .let { this.userRepository.getUser(it) }
            .switchIfEmpty { Mono.error(NotFoundException()) }
    }

    fun signUp(code: String, redirectionUrl: String): Mono<String> {
        return this.oAuthRequester.getUserByCode(code, redirectionUrl)
            .flatMap { this.userRepository.saveUser(it) }
            .flatMap { Mono.just(this.tokenGenerator.createTokenFromUser(it)) }
    }

    fun login(code: String, redirectionUrl: String): Mono<String> {
        return this.oAuthRequester.getUserByCode(code, redirectionUrl)
            .flatMap { this.userRepository.getUser(it.email) }
            .flatMap { Mono.just(this.tokenGenerator.createTokenFromUser(it)) }
            .switchIfEmpty { Mono.error(NotFoundException()) }
    }
}