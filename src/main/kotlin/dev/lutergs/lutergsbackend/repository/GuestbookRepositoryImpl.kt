package dev.lutergs.lutergsbackend.repository

import dev.lutergs.lutergsbackend.service.guestbook.Comment
import dev.lutergs.lutergsbackend.service.guestbook.GuestbookRepository
import dev.lutergs.lutergsbackend.utils.Hasher
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssNNNNNNNNNN")

@Document(collection = "guestbook")
class CommentEntity {
    @Id
    var id: String? = null
    var name: String? = null
    var password: String? = null
    var value: String? = null
    var createdAt: LocalDateTime? = null

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
                this.id = Hasher.hashStringToMd5("${comment.createdAt.format(dateTimeFormatter)}_${comment.name}")
                this.name = comment.name
                this.password = comment.password
                this.value = comment.value
                this.createdAt = comment.createdAt
            }
        }
    }
}

@Repository
interface GuestbookReactiveMongoRepository: ReactiveMongoRepository<CommentEntity, String> {
    fun findByNameAndCreatedAtAndPassword(name: String, createdAt: LocalDateTime, password: String): Mono<CommentEntity>
}


@Component
class GuestbookRepositoryImpl(
    private val guestbookReactiveMongoRepository: GuestbookReactiveMongoRepository
): GuestbookRepository {
    override fun saveComment(comment: Comment): Mono<Comment> {
        return CommentEntity.fromComment(comment)
            .let { guestbookEntity ->
                this.guestbookReactiveMongoRepository.save(guestbookEntity)
                    .flatMap { Mono.just(comment) }
            }
    }

    override fun findCommentWithPage(itemsInPage: Int, pageNumber: Int): Flux<Comment> {
        return this.guestbookReactiveMongoRepository.findAll(Sort.by("createdAt").descending())
            .skip((pageNumber * itemsInPage).toLong())
            .take(10)
            .flatMap { Mono.just(it.toComment()) }
    }

    override fun findCommentByNameAndCreatedAtAndPassword(name: String, createdAt: LocalDateTime, password: String): Mono<Comment> {
        return this.guestbookReactiveMongoRepository.findByNameAndCreatedAtAndPassword(name, createdAt, password)
            .flatMap { Mono.just(it.toComment()) }
    }

    override fun deleteComment(name: String, createdAt: LocalDateTime, password: String): Mono<Void> {
        return this.guestbookReactiveMongoRepository.findByNameAndCreatedAtAndPassword(name, createdAt, password)
            .flatMap { this.guestbookReactiveMongoRepository.delete(it) }
    }
}