package dev.lutergs.lutergsbackend.controller

import dev.lutergs.lutergsbackend.service.PageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono

@Configuration
class PageDataController(
    private val apiHandler: ApiHandler
) {

    @Bean
    fun route() = router {
        accept(MediaType.APPLICATION_JSON).nest {
            GET("/page/list", apiHandler::getAllPageList)
            GET("/page/{name}", apiHandler::getPageData)
            POST("/page/{name}", apiHandler::postPageData)
        }
    }
}

@Component
class ApiHandler(
    private val pageService: PageService,
    @Value("\${custom.frontend-server}") private val frontendServerUrl: String,
) {
    // TODO : ServerResponse 를 마지막에 intercept 하는 interceptor 설정 필요

    fun getAllPageList(request: ServerRequest): Mono<ServerResponse> {
        return this.pageService.getAllPageName()
            .let { fluxNames -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", this.frontendServerUrl)
                .body(fluxNames.collectList().flatMap { Mono.just(AllPageNames(it)) })
            }
    }

    fun getPageData(request: ServerRequest): Mono<ServerResponse> {
        return request.pathVariable("name")
            .let { this.pageService.getAllPageDataByName(it) }
            .let { ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", this.frontendServerUrl)
                .body(it)
            }
    }
    
    fun postPageData(request: ServerRequest): Mono<ServerResponse> {
        val pageName = request.pathVariable("name")
        return request.bodyToMono(ParagraphAddRequest::class.java)
            .let { ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", this.frontendServerUrl)
                .body(it)
            }
    }
}