package dev.lutergs.lutergsbackend.infra.controller

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
            GET("/pages", pageController::getPageList)
            GET("/pages/{endpoint}", pageController::getPageData)
            POST("/pages", pageController::createPage)
            // 게시물 수정 PATCH("/pages/{endpoint}", pageController::postPageData)

            // from imageController
            GET("/image", imageController::getPresignedImageUrl)
            GET("/image2", imageController::test)

            // from guestbookController
            // DELETE 도 고유 id 로 삭제할 수 있게 리팩터링 (uuid 부여?)
            GET("/guestbook", guestbookController::getComments)
            POST("/guestbook", guestbookController::postComment)
            DELETE("/guestbook/{uuid}", guestbookController::deleteComments)

            // from oauthController
            GET("/user", userController::getUser)
            POST("/user", userController::changeUserName)
            GET("/user/signup", userController::signUp)
            GET("/user/logout", userController::logout)

            // from pushMessageController
            // about subscription
                HEAD("/push/subscribers/{endpoint}", pushMessageController::isValidSubscriber)
                GET("/push/subscribers/{endpoint}", pushMessageController::getSubscriber)
                POST("/push/subscribers/{endpoint}", pushMessageController::saveSubscription)
                GET("/push/subscribers/{endpoint}/topic", pushMessageController::getSubscribedTopics)
                PUT("/push/subscribers/{endpoint}/topic", pushMessageController::subscribeToTopic)
                DELETE("/push/subscribers/{endpoint}/topic", pushMessageController::unsubscribeFromTopic)
            // about topics
                GET("/push/topics", pushMessageController::getTopics)
                GET("/push/topics/{uuid}", pushMessageController::getTopic)
                POST("/push/topics", pushMessageController::createTopic)
                DELETE("/push/topics/{uuid}", pushMessageController::deleteTopic)
                POST("/push/topics/{uuid}", pushMessageController::triggerTopic)
            // else
                POST("/push/suggestion", pushMessageController::newTopicMakeRequest)
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