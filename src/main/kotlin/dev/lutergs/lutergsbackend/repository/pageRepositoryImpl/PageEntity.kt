package dev.lutergs.lutergsbackend.repository.pageRepositoryImpl

import dev.lutergs.lutergsbackend.service.page.PageKey
import dev.lutergs.lutergsbackend.service.page.PageValue
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Pageable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Table("page_key")
class PageKeyEntity {
    @Id var id: Long? = null        // must be same id as PageValueEntity id
    @Column("endpoint") var endpoint: String? = null
    @Column("user_account_id") var userId: Long? = null
    @Column("title") var title: String? = null
    @Column("created_at") var createdAt: LocalDateTime? = null

    companion object {
        fun fromPageKey(pageKey: PageKey, userId: Long): PageKeyEntity {
            return PageKeyEntity().apply {
                this.id = pageKey.id
                this.endpoint = pageKey.endpoint.value
                this.userId = userId
                this.title = pageKey.title
                this.createdAt = pageKey.createdAt
            }
        }
    }
}

@Table("page_value")
class PageValueEntity {
    @Id var id: Long? = null        // must be same id as PageKeyEntity id
    @Column("page_key_id") var pageKeyId: Long? = null
    @Column("paragraphs") var paragraphs: List<String>? = null

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
interface PageValueReactiveRepository: ReactiveCrudRepository<PageValueEntity, Long> {
    fun findByPageKeyId(pageKeyId: Long): Mono<PageValueEntity>
}


