package dev.lutergs.lutergsbackend.controller

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class RouterConfiguration(
    private val pageDataController: PageDataController,
    private val tinyMceController: TinyMceController
) {

    @Bean
    fun route() = router {
        accept(MediaType.APPLICATION_JSON).nest {
            // from pageDataController
            GET("/page/list", pageDataController::getAllPageList)
            GET("/page/{name}", pageDataController::getPageData)
            POST("/page", pageDataController::postPageData)

            // from tinyMceController
            GET("/tinymce/apikey", tinyMceController::getTinyMceApiKey)
        }
    }
}