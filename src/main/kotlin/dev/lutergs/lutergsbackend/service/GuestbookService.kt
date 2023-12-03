package dev.lutergs.lutergsbackend.service

import dev.lutergs.lutergsbackend.domain.guestbook.Comment
import dev.lutergs.lutergsbackend.domain.guestbook.GuestbookRepository
import dev.lutergs.lutergsbackend.infra.controller.DeleteCommentRequest
import dev.lutergs.lutergsbackend.infra.controller.GetCommentsRequest
import dev.lutergs.lutergsbackend.infra.controller.NewCommentRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

class GuestbookService(
    private val guestbookRepository: GuestbookRepository
) {

    fun saveComment(newCommentRequest: NewCommentRequest): Mono<Comment> {
        return Comment(
            uuid = UUID.randomUUID(),
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
            UUID.fromString(deleteCommentRequest.uuid),
            deleteCommentRequest.password
        )
    }
}