package dev.lutergs.lutergsbackend.service.page

import dev.lutergs.lutergsbackend.repository.PageListEntity
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PageRepository {
    fun getPage(endpoint: String): Mono<Page>

    fun getAllPageNames(): Flux<PageList>

    fun savePage(page: Page): Mono<PageListEntity>
}