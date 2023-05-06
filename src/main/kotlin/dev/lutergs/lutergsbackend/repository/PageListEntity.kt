package dev.lutergs.lutergsbackend.repository

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Document(collection = "pagelist")
class PageListEntity {
    @Id
    var id: String? = null
    var name: String? = null

    companion object {
        fun fromPageEntity(pageEntity: PageEntity): PageListEntity {
            return PageListEntity().apply {
                this.id = pageEntity.id
                this.name = pageEntity.name
            }
        }
    }
}

@Repository
interface PageListReactiveMongoRepository: ReactiveMongoRepository<PageListEntity, String>