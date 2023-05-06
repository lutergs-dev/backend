package dev.lutergs.lutergsbackend.repository

import dev.lutergs.lutergsbackend.service.page.Page
import dev.lutergs.lutergsbackend.service.page.Paragraph
import dev.lutergs.lutergsbackend.utils.Hasher
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
    var endpoint: String? = null
    var paragraphs: List<ParagraphEntity>? = null

    fun toPage(): Page {
        return Page(
            id = this.endpoint!!,
            name = this.name!!,
            paragraphs = this.paragraphs!!.map(ParagraphEntity::toParagraph)
        )
    }

    companion object {
        fun fromPage(page: Page): PageEntity {
            return PageEntity().apply {
                this.id = Hasher.hashStringToMd5(page.name)
                this.name = page.name
                this.endpoint = page.id
                this.paragraphs = page.paragraphs.map { ParagraphEntity.fromParagraph(it) }
            }
        }
    }
}

class ParagraphEntity {
    var data: String? = null
    var hash: String? = null

    fun toParagraph(): Paragraph {
        return Paragraph(this.data!!, this.hash!!)
    }

    companion object {
        fun fromParagraph(paragraph: Paragraph): ParagraphEntity {
            return ParagraphEntity().apply {
                this.data = paragraph.data
                this.hash = paragraph.hash
            }
        }
    }
}

@Repository
interface PageInfoReactiveMongoRepository: ReactiveMongoRepository<PageEntity, String> {
    fun getPageDataByEndpoint(name: String): Mono<PageEntity>
}
