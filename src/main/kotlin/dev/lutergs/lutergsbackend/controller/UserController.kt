package dev.lutergs.lutergsbackend.controller

import com.nimbusds.jose.JOSEException
import dev.lutergs.lutergsbackend.service.user.UserService
import dev.lutergs.lutergsbackend.utils.orElse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component

import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.queryParamOrNull
import reactor.core.publisher.Mono
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import java.net.URI
import java.text.ParseException
import java.time.Duration

@Component
class UserController(
    @Value("\${custom.server.url.backend}") private val backendServerUrl: String,
    @Value("\${custom.server.url.frontend}") private val frontServerUrl: String,
    @Value("\${custom.server.domain.backend}") private val backendDomain: String,
    @Value("\${custom.token.expire-hour}") private val tokenExpireHour: Int,
    private val userService: UserService
) {
    private val cookieName = "lutergs.dev"

    fun getUser(request: ServerRequest): Mono<ServerResponse> {
        return request.cookies()[cookieName]
            ?.find { it.name == cookieName }
            ?.let { this.userService.getUser(it.value) }
            ?.flatMap { ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(it)) }
            ?.onErrorResume {
                when (it) {
                    is SecurityException, is JOSEException -> ServerResponse.status(401).contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
                    else -> ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
                }
            }
            ?: ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(ErrorResponse("token is empty")))
    }

    fun login(request: ServerRequest): Mono<ServerResponse> {
        return request.queryParamOrNull("code")
            ?.let { code -> this.userService.login(code, "${backendServerUrl}/user/login") }
            ?.flatMap { ServerResponse
                .permanentRedirect(URI.create("${frontServerUrl}/user"))
                .cookie(this.createCookie(it))
                .build() }
            ?.onErrorResume {
                ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
            } ?: ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(ErrorResponse("Google OAuth request malformed!")))
    }

    fun signUp(request: ServerRequest): Mono<ServerResponse> {
        return request.queryParamOrNull("code")
            ?.let { code -> this.userService.signUp(code, "${backendServerUrl}/user/signup") }
            ?.flatMap { ServerResponse
                .permanentRedirect(URI.create("http://localhost:3000/user"))
                .cookie(this.createCookie(it))
                .build()
                .log()}
            ?.onErrorResume {
                ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
            } ?: ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(ErrorResponse("Google OAuth request malformed!")))
    }

    fun createCookie(token: String): ResponseCookie {
        return ResponseCookie
            .from(cookieName, token)
            .httpOnly(true)
            .domain(backendDomain)
            .path("/user")
            .maxAge(Duration.ofHours(this.tokenExpireHour.toLong()))
            .secure(false)
            .build()
    }

}
