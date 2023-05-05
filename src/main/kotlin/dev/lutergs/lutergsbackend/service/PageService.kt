package dev.lutergs.lutergsbackend.service

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

    fun addNewPage(newPageRequest: Mono<NewPageRequest>): Mono<PageListEntity> {
        return newPageRequest
            .flatMap { pageRequest ->
                Mono.just(Page(
                    name = pageRequest.title,
                    paragraphs = pageRequest.paragraphs.map { Paragraph.fromString(it) }
                ))
            }.flatMap {
                this.pageDataRepository.savePage(it)
            }
    }
}