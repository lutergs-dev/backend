package dev.lutergs.lutergsbackend.controller

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class RouterConfiguration(
    private val pageDataController: PageDataController,
    private val imageController: ImageController
) {

    @Bean
    fun route() = router {
        accept(MediaType.APPLICATION_JSON).nest {
            // from pageDataController
            GET("/page/list", pageDataController::getAllPageList)
            GET("/page/{endpoint}", pageDataController::getPageData)
            POST("/page", pageDataController::postPageData)

            // from imageController
            GET("/image", imageController::getPresignedImageUrl)
        }
    }
}

data class ErrorResponse(
    val error: String
)