package dev.lutergs.lutergsbackend.infra.controller

import com.nimbusds.jose.JOSEException
import dev.lutergs.lutergsbackend.domain.page.PageKey
import dev.lutergs.lutergsbackend.service.PageService
import dev.lutergs.lutergsbackend.utils.orElse
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono


@Component
class PageController(
    private val pageService: PageService
) {
    private val cookieName = "lutergs.dev"

    fun getPageList(request: ServerRequest): Mono<ServerResponse> {
        return GetPagesRequest(
                index = request.queryParamOrNull("index")?.toInt() ?: 0,
                size = request.queryParamOrNull("size")?.toInt() ?: 10)
            .let { this.pageService.getPageList(it) }
            .let { ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(it, PageKey::class.java) }
            .onErrorResume {
                when(it) {      // TODO : error 메시지에 따른 detail handling 구현 필요
                    else -> ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
                }
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
    
    fun createPage(request: ServerRequest): Mono<ServerResponse> {
        return this.getCookie(request)
            ?.let { token ->
                request.bodyToMono(AddPageRequest::class.java)
                    .flatMap { this.pageService.addPage(it, token) } }
            ?.flatMap { ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(it))}
            ?.onErrorResume {
                when (it) {
                    is SecurityException, is JOSEException -> ServerResponse.status(401).contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
                    is NotFoundException -> ServerResponse.status(403).contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(ErrorResponse("user not found!")))
                    else -> ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
                }
            }
            ?: ServerResponse.notFound().build()
    }

    fun getCookie(request: ServerRequest): String? {
        return request.cookies()[this.cookieName]
            ?.find { it.name == this.cookieName }
            ?.value
    }
}

data class GetPagesRequest(
    val index: Int,
    val size: Int
)

data class AddPageRequest(
    val title: String,
    val paragraphs: List<String>
)