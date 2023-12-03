package dev.lutergs.lutergsbackend.infra.controller

import dev.lutergs.lutergsbackend.service.GuestbookService
import dev.lutergs.lutergsbackend.utils.ServerResponseUtil
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
        return GetCommentsRequest(
            request.queryParamOrNull("index")?.toInt() ?: 0,
            request.queryParamOrNull("size")?.toInt() ?: 10
        ).let { this.guestbookService.getComments(it) }
            .let { ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(it, dev.lutergs.lutergsbackend.domain.guestbook.Comment::class.java) }
            .onErrorResume { ServerResponseUtil.errorResponse(it) }
    }

    fun deleteComments(request: ServerRequest): Mono<ServerResponse> {
        return DeleteCommentRequest(
            request.pathVariable("uuid"),
            request.headers().firstHeader("Authorization") ?: ""
        ).let { this.guestbookService.deleteComments(it) }
            .then(ServerResponseUtil.okResponse(""))
            .onErrorResume { ServerResponseUtil.errorResponse(it) }
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
    val comments: List<dev.lutergs.lutergsbackend.domain.guestbook.Comment>
)

data class DeleteCommentRequest(
    val uuid: String,
    val password: String
)

data class DeleteCommentResponse(
    val isDeleted: Boolean
)