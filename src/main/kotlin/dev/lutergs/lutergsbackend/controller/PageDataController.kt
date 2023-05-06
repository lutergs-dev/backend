package dev.lutergs.lutergsbackend.controller

import dev.lutergs.lutergsbackend.service.page.PageList
import dev.lutergs.lutergsbackend.service.page.PageService
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono


@Component
class PageDataController(
    private val pageService: PageService
) {

    fun getAllPageList(request: ServerRequest): Mono<ServerResponse> {
        return this.pageService.getAllPageName()
            .let { fluxNames -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fluxNames.collectList().flatMap { Mono.just(AllPageNames(it)) })
            }
    }

    fun getPageData(request: ServerRequest): Mono<ServerResponse> {
        return request.pathVariable("endpoint")
            .let { this.pageService.getPageByEndpoint(it) }
            .let { ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(it)
            }
    }
    
    fun postPageData(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(NewPageRequest::class.java)
            .flatMap { this.pageService.addNewPage(it) }
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(NewPageResponse("/page/${it.name!!}")))
            }.onErrorResume {
                ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.localizedMessage)))
            }
    }
}


data class NewPageRequest(
    val title: Title,
    val paragraphs: List<String>
)

data class Title(
    val id: String,
    val name: String
)

data class NewPageResponse(
    val data: String
)

data class AllPageNames(
    val pages: List<PageList>
)