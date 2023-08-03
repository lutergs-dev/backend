package dev.lutergs.lutergsbackend.controller

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class RouterConfiguration(
    private val pageController: PageController,
    private val imageController: ImageController,
    private val guestbookController: GuestbookController,
    private val userController: UserController,
    private val pushMessageController: PushMessageController
) {

    @Bean
    fun route() = router {
        accept(MediaType.APPLICATION_JSON).nest {
            // from pageDataController
            GET("/page/list", pageController::getPageList)
            GET("/page/{endpoint}", pageController::getPageData)
            POST("/page", pageController::postPageData)

            // from imageController
            GET("/image", imageController::getPresignedImageUrl)
            GET("/image2", imageController::test)

            // from guestbookController
            GET("/guestbook", guestbookController::getComments)
            POST("/guestbook", guestbookController::postComment)
            DELETE("/guestbook", guestbookController::deleteComments)

            // from oauthController
            GET("/user", userController::getUser)
            POST("/user", userController::changeUserName)
            GET("/user/signup", userController::signUp)
            GET("/user/logout", userController::logout)

            // from pushMessageController
            POST("/push/subscription", pushMessageController::saveSubscription)
            POST("/push/topic", pushMessageController::saveTopic)
            GET("/push/topic", pushMessageController::getTopics)
            POST("/push/topic/trigger", pushMessageController::triggerTopic)
        }
    }
}

data class ErrorResponse(
    val error: String
)