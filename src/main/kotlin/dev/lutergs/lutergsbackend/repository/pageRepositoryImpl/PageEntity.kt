package dev.lutergs.lutergsbackend.repository.pageRepositoryImpl

import dev.lutergs.lutergsbackend.service.page.PageKey
import dev.lutergs.lutergsbackend.service.page.PageValue
import dev.lutergs.lutergsbackend.utils.toDefaultZoneOffsetDateTime
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

@Table("page_key")
class PageKeyEntity {
    @Id var id: Long? = null        // must be same id as PageValueEntity id
    @Column("endpoint") var endpoint: String? = null
    @Column("user_account_id") var userId: Long? = null
    @Column("title") var title: String? = null
    @Column("created_at") var createdAt: OffsetDateTime? = null

    companion object {
        fun fromPageKey(pageKey: PageKey, userId: Long): PageKeyEntity {
            return PageKeyEntity().apply {
                this.id = pageKey.id
                this.endpoint = pageKey.endpoint.value
                this.userId = userId
                this.title = pageKey.title
                this.createdAt = pageKey.createdAt.toDefaultZoneOffsetDateTime()
            }
        }
    }
}

@Document("page_value")
class PageValueEntity {
    @Id var id: String? = null        // must be same id as PageKeyEntity id
    var pageKeyId: Long? = null
    var paragraphs: List<String>? = null

    fun toPageValue(): PageValue {
        return PageValue(this.id, this.paragraphs!!)
    }

    companion object {
        fun fromPageValue(pageValue: PageValue, pageKeyEntity: PageKeyEntity): PageValueEntity {
            return PageValueEntity().apply {
                this.id = pageValue.id
                this.pageKeyId = pageKeyEntity.id
                this.paragraphs = pageValue.paragraphs
            }
        }
    }
}


@Repository
interface PageKeyReactiveRepository: ReactiveCrudRepository<PageKeyEntity, Long> {
    fun findByOrderByIdDesc(pageable: Pageable): Flux<PageKeyEntity>
    fun findByUserId(userId: Long): Flux<PageKeyEntity>
    fun findByEndpoint(endpoint: String): Mono<PageKeyEntity>
}

@Repository
interface PageValueReactiveRepository: ReactiveMongoRepository<PageValueEntity, String> {
    fun findByPageKeyId(pageKeyId: Long): Mono<PageValueEntity>
}


