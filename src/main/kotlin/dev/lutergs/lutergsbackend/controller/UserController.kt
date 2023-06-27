package dev.lutergs.lutergsbackend.controller

import dev.lutergs.lutergsbackend.service.user.UserService
import dev.lutergs.lutergsbackend.utils.orElse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Component
class UserController(
    private val userService: UserService
) {

    fun verifyUser(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(RawUser::class.java)
            .flatMap {
                if (it.id.isNotBlank() && it.password.isNotBlank()) {
                    this.userService.verifyUser(it)
                } else {
                    Mono.error(IllegalArgumentException("잘못된 아이디 혹은 비밀번호 형식입니다."))
                }
            }
            .flatMap {
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(it))
            }.onErrorResume {
                ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
            }
    }

    fun createUser(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(RawUser::class.java)
            .flatMap {
                if (it.id.isNotBlank() && it.password.isNotBlank()) {
                    this.userService.createUser(it)
                } else {
                    Mono.error(IllegalArgumentException("잘못된 아이디 혹은 비밀번호 형식입니다."))
                }
            }
            .flatMap {
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(it))
            }.onErrorResume {
                ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
            }
    }

    fun deleteUser(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(RawUser::class.java)
            .flatMap {
                if (it.id.isNotBlank() && it.password.isNotBlank()) {
                    this.userService.deleteUser(it)
                } else {
                    Mono.error(IllegalArgumentException("잘못된 아이디 혹은 비밀번호 형식입니다."))
                }
            }
            .flatMap {
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(it))
            }.onErrorResume {
                ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
            }
    }

    fun changePassword(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(ChangeUserPasswordDto::class.java)
            .flatMap {
                if (it.id.isNotBlank() && it.oldPassword.isNotBlank() && it.newPassword.isNotBlank()) {
                    this.userService.changePassword(it)
                } else {
                    Mono.error(IllegalArgumentException("비밀번호가 잘못되었습니다."))
                }
            }
            .flatMap {
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(it))
            }.onErrorResume {
                ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
            }
    }
}

data class RawUser(
    val id: String,
    val password: String
)

data class ChangeUserPasswordDto(
    val id: String,
    val oldPassword: String,
    val newPassword: String
)