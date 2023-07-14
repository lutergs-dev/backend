package dev.lutergs.lutergsbackend.repository

import dev.lutergs.lutergsbackend.service.guestbook.Comment
import dev.lutergs.lutergsbackend.service.guestbook.GuestbookRepository
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Pageable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Table("guestbook")
class CommentEntity {
    @Id var id: Long? = null
    @Column("name") var name: String? = null
    @Column("password") var password: String? = null
    @Column("value") var value: String? = null
    @Column("created_at") var createdAt: LocalDateTime? = null

    fun toComment(): Comment {
        return Comment(
            name = this.name!!,
            password = this.password!!,
            value = this.value!!,
            createdAt = this.createdAt!!
        )
    }

    companion object {
        fun fromComment(comment: Comment): CommentEntity {
            return CommentEntity().apply {
                this.name = comment.name
                this.password = comment.password
                this.value = comment.value
                this.createdAt = comment.createdAt
            }
        }
    }
}

@Repository
interface GuestbookReactiveRepository: ReactiveCrudRepository<CommentEntity, Long> {
    fun findByOrderByCreatedAtDesc(pageable: Pageable): Flux<CommentEntity>
    fun findDistinctFirstByNameAndPasswordAndCreatedAt(
        name: String,
        password: String,
        createdAt: LocalDateTime
    ): Mono<CommentEntity>
}

@Component
class GuestbookRepositoryImpl(
    private val guestbookReactiveRepository: GuestbookReactiveRepository
): GuestbookRepository {
    override fun saveComment(comment: Comment): Mono<Comment> {
        return CommentEntity.fromComment(comment)
            .let { guestbookEntity ->
                this.guestbookReactiveRepository.save(guestbookEntity)
                    .flatMap { Mono.just(comment) }
            }
    }

    override fun findCommentWithPage(itemsInPage: Int, pageNumber: Int): Flux<Comment> {
        return this.guestbookReactiveRepository.findByOrderByCreatedAtDesc(Pageable.ofSize(itemsInPage).withPage(pageNumber))
            .flatMap { Mono.just(it.toComment()) }
    }

    override fun deleteComment(name: String, createdAt: LocalDateTime, password: String): Mono<Void> {
        return this.guestbookReactiveRepository.findDistinctFirstByNameAndPasswordAndCreatedAt(name, password, createdAt)
            .flatMap { this.guestbookReactiveRepository.delete(it) }
    }
}