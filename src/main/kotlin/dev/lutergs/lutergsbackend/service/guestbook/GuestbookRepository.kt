package dev.lutergs.lutergsbackend.service.guestbook

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

interface GuestbookRepository {

    fun saveComment(comment: Comment): Mono<Comment>

    fun findCommentWithPage(itemsInPage: Int, pageNumber: Int): Flux<Comment>

    fun deleteComment(name: String, createdAt: LocalDateTime, password: String): Mono<Void>
}