package dev.lutergs.lutergsbackend.controller

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class RouterConfiguration(
    private val pageDataController: PageDataController,
    private val imageController: ImageController,
    private val guestbookController: GuestbookController,
    private val userController: UserController,
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

            // from guestbookController
            GET("/guestbook", guestbookController::getComments)
            POST("/guestbook", guestbookController::postComment)
            DELETE("/guestbook", guestbookController::deleteComments)

            // from oauthController
            GET("/user", userController::getUser)
            GET("/user/login", userController::login)
            GET("/user/signup", userController::signUp)
        }
    }
}

data class ErrorResponse(
    val error: String
)