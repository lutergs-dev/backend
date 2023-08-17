package dev.lutergs.lutergsbackend.controller

import dev.lutergs.lutergsbackend.utils.orElse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono

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
            // about subscription
                GET("/push/subscription/valid", pushMessageController::isValidSubscription)
                POST("/push/subscription", pushMessageController::saveSubscription)
                GET("/push/subscription/topic", pushMessageController::getSubscribedTopics)
                POST("/push/subscription/topic", pushMessageController::subscribeToTopic)
                DELETE("/push/subscription/topic", pushMessageController::unsubscribeFromTopic)
            // about topics
                GET("/push/topics", pushMessageController::getTopics)
                GET("/push/topic", pushMessageController::getTopic)
                POST("/push/topic", pushMessageController::createTopic)
                DELETE("/push/topic", pushMessageController::deleteTopic)
                POST("/push/topic/trigger", pushMessageController::triggerTopic)
            // else
                POST("/push/topic/request", pushMessageController::newTopicMakeRequest)
        }
    }
}

data class ErrorResponse(
    val error: String
) {
    companion object {
        fun createFromThrowable(throwable: Throwable, code: Int = 400): Mono<ServerResponse> {
            return ServerResponse.status(code).contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(ErrorResponse(throwable.message.orElse(throwable.stackTraceToString()))))
        }
    }
}