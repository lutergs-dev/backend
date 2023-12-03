package dev.lutergs.lutergsbackend.domain.page

import dev.lutergs.lutergsbackend.domain.user.User
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PageRepository {
    fun getPageByEndpoint(endpoint: Endpoint): Mono<Page>
    fun getPageKeyList(pageIndex: Int, pageSize: Int): Flux<PageKey>
    fun getPageOfUser(user: User): Flux<PageKey>
    fun getPageValue(pageKey: PageKey): Mono<PageValue>
    fun savePage(page: Page, user: User): Mono<Page>
}