package dev.lutergs.lutergsbackend.service.page

import dev.lutergs.lutergsbackend.controller.NewPageRequest
import dev.lutergs.lutergsbackend.repository.pageRepository.PageListEntity
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PageService(
    private val pageDataRepository: PageRepository
) {

    fun getPageByEndpoint(endpoint: String): Mono<Page> {
        return this.pageDataRepository.getPage(endpoint)
    }

    fun getAllPageName(): Flux<PageList> {
        return this.pageDataRepository.getAllPageNames()
    }

    fun addNewPage(newPageRequest: NewPageRequest): Mono<PageListEntity> {
        return Page(
            id = newPageRequest.title.id,
            name = newPageRequest.title.name,
            paragraphs = newPageRequest.paragraphs.map { Paragraph.fromString(it) }
        ).let {
            this.pageDataRepository.savePage(it)
        }
    }
}