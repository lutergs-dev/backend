package dev.lutergs.lutergsbackend.controller

import dev.lutergs.lutergsbackend.service.guestbook.Comment
import dev.lutergs.lutergsbackend.service.guestbook.GuestbookService
import dev.lutergs.lutergsbackend.utils.orElse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono

@Component
class GuestbookController(
    private val guestbookService: GuestbookService
) {

    fun postComment(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(NewCommentRequest::class.java)
            .flatMap {
                if (it.name.isEmpty() || it.name.isBlank()) {
                    Mono.error(IllegalArgumentException("이름이 비어있습니다."))
                } else if (it.value.isEmpty() || it.value.isBlank()) {
                    Mono.error(IllegalArgumentException("내용이 비어있습니다."))
                } else {
                    this.guestbookService.saveComment(it)
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

    fun getComments(request: ServerRequest): Mono<ServerResponse> {
        return runCatching {
            GetCommentsRequest(
                request.queryParamOrNull("index")!!.toInt(),
                request.queryParamOrNull("size")!!.toInt()
            )
        }.map { getGuestbookRequest ->
            this.guestbookService.getComments(getGuestbookRequest)
                .collectList()
                .flatMap { Mono.just(GetCommentsResponse(it)) }
                .let {
                    ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(it)
                }
        }.getOrElse {
            ServerResponse.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(it.toString(), String::class.java)
        }
    }

    fun deleteComments(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(DeleteCommentRequest::class.java)
            .flatMap { this.guestbookService.deleteComments(it) }
            .then(Mono.defer {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(DeleteCommentResponse(true)))
            })
            .onErrorResume {
                ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
            }
    }
}


data class NewCommentRequest(
    val name: String,
    val password: String,
    val value: String
)

data class GetCommentsRequest(
    val index: Int,
    val size: Int
)

data class GetCommentsResponse(
    val comments: List<Comment>
)

data class DeleteCommentRequest(
    val name: String,
    val createdAt: String,
    val password: String
)

data class DeleteCommentResponse(
    val isDeleted: Boolean
)