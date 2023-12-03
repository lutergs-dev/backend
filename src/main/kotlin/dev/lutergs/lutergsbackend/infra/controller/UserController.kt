package dev.lutergs.lutergsbackend.infra.controller

import com.nimbusds.jose.JOSEException
import dev.lutergs.lutergsbackend.service.UserService
import dev.lutergs.lutergsbackend.utils.orElse
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Component

import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.queryParamOrNull
import reactor.core.publisher.Mono
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import java.net.URI
import java.time.Duration

@Component
class UserController(
    @Value("\${custom.server.url.backend}") private val backendServerUrl: String,
    @Value("\${custom.server.url.frontend}") private val frontServerUrl: String,
    @Value("\${custom.server.root-domain}") private val rootDomain: String,
    @Value("\${custom.token.expire-hour}") private val tokenExpireHour: Int,
    @Value("\${custom.token.is-secure}") private val secureCookie: Boolean,
    private val userService: UserService
) {
    private val cookieName = "lutergs.dev"


    fun getUser(request: ServerRequest): Mono<ServerResponse> {
        return this.getCookie(request)
            ?.let { this.userService.getUser(it) }
            ?.flatMap { ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(it)) }
            ?.onErrorResume {
                when (it) {
                    is SecurityException, is JOSEException -> ServerResponse.status(401).contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
                    is NotFoundException -> ServerResponse.status(403).contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(ErrorResponse("user not found!")))
                    else -> ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
                }
            }
            ?: ServerResponse.notFound().build()
    }

    fun changeUserName(request: ServerRequest): Mono<ServerResponse> {
        return this.getCookie(request)
            ?.let { token -> request
                .bodyToMono(ChangeNickNameDto::class.java)
                .flatMap { this.userService.changeUserName(token, it.nickname) }
            }?.flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(it))
            }?.onErrorResume {
                when (it) {
                    is IllegalStateException, is IllegalArgumentException -> ServerResponse.status(406).contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
                    is SecurityException, is JOSEException -> ServerResponse.status(401).contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
                    is NotFoundException -> ServerResponse.status(403).contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(ErrorResponse("user not found!")))
                    else -> ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
                }
            }
            ?: ServerResponse.notFound().build()
    }


    fun signUp(request: ServerRequest): Mono<ServerResponse> {
        return request.queryParamOrNull("code")
            ?.let { code -> this.userService.signUp(code, "${this.backendServerUrl}/user/signup") }
            ?.flatMap { ServerResponse
                .permanentRedirect(URI.create("${this.frontServerUrl}/user"))
                .cookie(this.createCookie(it, false))
                .build() }
            ?.onErrorResume {
                ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString())))) }
            ?: ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(ErrorResponse("Google OAuth request malformed!")))
    }

    fun logout(request: ServerRequest): Mono<ServerResponse> {
        return this.getCookie(request)
            ?.let {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .cookie(this.createCookie(it, true))
                    .build()
            }
            ?: ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(ErrorResponse("유저 정보 없음")))
    }

    fun getCookie(request: ServerRequest): String? {
        return request.cookies()[this.cookieName]
            ?.find { it.name == this.cookieName }
            ?.value
    }

    fun createCookie(token: String, isLogout: Boolean): ResponseCookie {
        return ResponseCookie
            .from(cookieName, token)
            .httpOnly(true)
            .domain(rootDomain)
            .path("/")
            .maxAge(
                if (isLogout) Duration.ZERO
                else Duration.ofHours(this.tokenExpireHour.toLong())
            )
            .secure(this.secureCookie)
            .build()
    }
}

data class ChangeNickNameDto(
    val nickname: String
)
