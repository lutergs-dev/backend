package dev.lutergs.lutergsbackend.repository

import dev.lutergs.lutergsbackend.service.Page
import dev.lutergs.lutergsbackend.service.PageRepository
import dev.lutergs.lutergsbackend.service.Paragraph
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono


@Document(collection = "pagedata")
class PageEntity {
    @Id
    var id: String? = null
    var name: String? = null
    var paragraphs: List<ParagraphEntity>? = null

    fun toPage(): Page {
        return Page(this.name!!, this.paragraphs!!.map(ParagraphEntity::toParagraph))
    }
}

class ParagraphEntity {
    var data: String? = null
    var hash: String? = null

    fun toParagraph(): Paragraph {
        return Paragraph(this.data!!, this.hash!!)
    }
}

@Repository
interface PageInfoReactiveMongoRepository: ReactiveMongoRepository<PageEntity, String> {
    fun getPageDataByName(name: String): Mono<PageEntity>
}

@Repository
class PageRepositoryRepositoryImpl(
    private val pageInfoReactiveMongoRepository: PageInfoReactiveMongoRepository
): PageRepository {

    override fun getPage(name: String): Mono<Page> {
        return this.pageInfoReactiveMongoRepository.getPageDataByName(name)
            .flatMap { Mono.just(it.toPage()) }
    }
}