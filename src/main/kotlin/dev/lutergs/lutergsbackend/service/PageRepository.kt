package dev.lutergs.lutergsbackend.service

import dev.lutergs.lutergsbackend.repository.PageListEntity
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PageRepository {
    fun getPage(name: String): Mono<Page>

    fun getAllPageNames(): Flux<String>

    fun savePage(page: Page): Mono<PageListEntity>
}