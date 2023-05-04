package dev.lutergs.lutergsbackend.controller

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class RouterConfiguration(
    private val pageDataController: PageDataController
) {

    @Bean
    fun route() = router {
        accept(MediaType.APPLICATION_JSON).nest {
            // from
            GET("/page/list", pageDataController::getAllPageList)
            GET("/page/{name}", pageDataController::getPageData)
            POST("/page/{name}", pageDataController::postPageData)
        }
    }
}