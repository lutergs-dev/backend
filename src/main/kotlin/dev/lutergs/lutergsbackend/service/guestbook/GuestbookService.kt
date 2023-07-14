package dev.lutergs.lutergsbackend.service.guestbook

import dev.lutergs.lutergsbackend.controller.DeleteCommentRequest
import dev.lutergs.lutergsbackend.controller.GetCommentsRequest
import dev.lutergs.lutergsbackend.controller.NewCommentRequest
import dev.lutergs.lutergsbackend.utils.toLocalDateTime
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class GuestbookService(
    private val guestbookRepository: GuestbookRepository
) {

    fun saveComment(newCommentRequest: NewCommentRequest): Mono<Comment> {
        return Comment(
            name = newCommentRequest.name,
            password = newCommentRequest.password,
            value = newCommentRequest.value,
            createdAt = LocalDateTime.now()
        ).let {
            this.guestbookRepository.saveComment(it)
        }
    }

    fun getComments(getCommentsRequest: GetCommentsRequest): Flux<Comment> {
        return this.guestbookRepository.findCommentWithPage(getCommentsRequest.size, getCommentsRequest.index)
            .flatMap { Mono.just(it.maskingPassword()) }
    }

    fun deleteComments(deleteCommentRequest: DeleteCommentRequest): Mono<Void> {

        return this.guestbookRepository.deleteComment(
            name = deleteCommentRequest.name,
            createdAt = deleteCommentRequest.createdAt.toLocalDateTime(),
            password = deleteCommentRequest.password
        )
    }
}