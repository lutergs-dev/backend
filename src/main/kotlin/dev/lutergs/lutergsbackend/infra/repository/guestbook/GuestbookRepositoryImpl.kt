package dev.lutergs.lutergsbackend.infra.repository.guestbook

import dev.lutergs.lutergsbackend.domain.guestbook.Comment
import dev.lutergs.lutergsbackend.domain.guestbook.GuestbookRepository
import dev.lutergs.lutergsbackend.utils.toDefaultZoneLocalDateTime
import dev.lutergs.lutergsbackend.utils.toDefaultZoneOffsetDateTime
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Pageable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.util.*

@Table("guestbook")
class CommentEntity {
    @Id var id: Long? = null
    @Column("uuid") var uuid: String? = ""
    @Column("name") var name: String = ""
    @Column("password") var password: String? = null        // oracle db treats empty string as null
    @Column("value") var value: String = ""
    @Column("created_at") var createdAt: OffsetDateTime = OffsetDateTime.now()

    fun toComment(): Comment {
        return Comment(
            uuid = UUID.fromString(this.uuid),
            name = this.name,
            password = this.password ?: "",
            value = this.value,
            createdAt = this.createdAt.toDefaultZoneLocalDateTime()
        )
    }

    companion object {
        fun fromComment(comment: Comment): CommentEntity {
            return CommentEntity().apply {
                this.uuid = comment.uuid.toString()
                this.name = comment.name
                this.password = comment.password
                this.value = comment.value
                this.createdAt = comment.createdAt.toDefaultZoneOffsetDateTime()
            }
        }
    }
}

@Repository
interface GuestbookReactiveRepository: ReactiveCrudRepository<CommentEntity, Long> {
    fun findByOrderByCreatedAtDesc(pageable: Pageable): Flux<CommentEntity>

    fun findDistinctFirstByUuid(
        uuid: String
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

    override fun deleteComment(uuid: UUID, password: String): Mono<Void> {
        return this.guestbookReactiveRepository.findDistinctFirstByUuid(uuid.toString())
            .flatMap {
                // oracle db's empty string is null
                if (it.password == password || (it.password == null && password == "")) this.guestbookReactiveRepository.delete(it)
                else Mono.error(IllegalArgumentException("wrong password"))
            }
    }
}