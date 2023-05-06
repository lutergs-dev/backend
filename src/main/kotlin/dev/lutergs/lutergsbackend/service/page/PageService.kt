package dev.lutergs.lutergsbackend.service.page

import dev.lutergs.lutergsbackend.controller.NewPageRequest
import dev.lutergs.lutergsbackend.repository.PageListEntity
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PageService(
    private val pageDataRepository: PageRepository
) {

    fun getAllPageDataByName(name: String): Mono<Page> {
        return this.pageDataRepository.getPage(name)
    }

    fun getAllPageName(): Flux<String> {
        return this.pageDataRepository.getAllPageNames()
    }

    fun addNewPage(newPageRequest: NewPageRequest): Mono<PageListEntity> {
        return Page(
            name = newPageRequest.title,
            paragraphs = newPageRequest.paragraphs.map { Paragraph.fromString(it) }
        ).let {
            this.pageDataRepository.savePage(it)
        }
    }
}