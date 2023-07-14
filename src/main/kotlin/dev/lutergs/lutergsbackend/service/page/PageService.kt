package dev.lutergs.lutergsbackend.service.page

import dev.lutergs.lutergsbackend.controller.AddPageRequest
import dev.lutergs.lutergsbackend.controller.GetPagesRequest
import dev.lutergs.lutergsbackend.service.user.UserService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class PageService(
    private val pageRepository: PageRepository,
    private val userService: UserService
) {
    fun getPageByEndpoint(endpoint: String): Mono<Page> {
        return this.pageRepository.getPageByEndpoint(Endpoint(endpoint))
    }

    fun getPageList(getPagesRequest: GetPagesRequest): Flux<PageKey> {
        return this.pageRepository.getPageKeyList(getPagesRequest.index, getPagesRequest.size)
    }

    fun addPage(addPageRequest: AddPageRequest, token: String): Mono<Page> {
        return this.userService.getUser(token)
            .flatMap { user ->
                val page = Page(
                    PageKey(null, addPageRequest.title, Endpoint.create(), user.nickName, LocalDateTime.now()),
                    PageValue(null, addPageRequest.paragraphs)
                )
                this.pageRepository.savePage(page, user)
            }
    }
}