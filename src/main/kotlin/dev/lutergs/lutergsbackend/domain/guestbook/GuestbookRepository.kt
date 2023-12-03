package dev.lutergs.lutergsbackend.domain.guestbook

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface GuestbookRepository {

    fun saveComment(comment: Comment): Mono<Comment>

    fun findCommentWithPage(itemsInPage: Int, pageNumber: Int): Flux<Comment>

    fun deleteComment(uuid: UUID, password: String): Mono<Void>
}