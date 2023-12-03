package dev.lutergs.lutergsbackend.graalVM

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
//import dev.lutergs.lutergsbackend.controller.*
//import dev.lutergs.lutergsbackend.infra.controller.*
//import dev.lutergs.lutergsbackend.infra.impl.user.TokenGeneratorImpl
//import dev.lutergs.lutergsbackend.service.page.Page
//import dev.lutergs.lutergsbackend.service.page.PageKey
//import dev.lutergs.lutergsbackend.service.push.subscriber.Subscriber
//import dev.lutergs.lutergsbackend.service.push.topic.Topic
//import dev.lutergs.lutergsbackend.service.user.Email
//import dev.lutergs.lutergsbackend.service.user.NickName
//import dev.lutergs.lutergsbackend.service.user.TokenGenerator
//import dev.lutergs.lutergsbackend.service.user.User
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.time.LocalDateTime

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@ActiveProfiles("local")
//class SpringBootReflectionTest @Autowired constructor(
//    @Value("\${server.port}") private val port: Int,
//
//    // for tokenGenerator
//    @Value("\${custom.token.expire-hour}") private val tokenExpireHour: Int,
//    @Value("\${custom.token.rsa-key-location}") private val keyLocation: String
//)  {
//
//    private val objectMapper: ObjectMapper = ObjectMapper()
//        .registerKotlinModule()
//        .registerModule(JavaTimeModule())
//    private lateinit var webClient: WebClient
//
//    init {
//        val tokenGenerator: TokenGenerator = TokenGeneratorImpl(this.tokenExpireHour, this.keyLocation)
//        val testCookie = User(
//            id = 1,
//            email = Email.fromFullString("koo04034@gmail.com"),
//            createdAt = LocalDateTime.now(),
//            nickName = NickName("LuterGS")
//        )
//            .let { tokenGenerator.createTokenFromUser(it) }
//        webClient = WebClient.builder()
//            .baseUrl("http://localhost:${this.port}")
//            .defaultCookie("lutergs.dev", testCookie)
//            .build()
//    }
//
//
//
//    @Test
//    fun guestbookControllerReflectionTest() {
//        // get guestbook
//        this.webClient
//            .get()
//            .uri { it
//                .path("/guestbook")
//                .queryParam("index", 0)
//                .queryParam("size", 5)
//                .build() }
//            .retrieve()
//            .bodyToMono(GetCommentsResponse::class.java)
//            .block()!!.let { println("GET guestbook : $it") }
//
//        // post guestbook
//        val newCommentRequest = NewCommentRequest("test", "test", "test value")
//        this.webClient
//            .post()
//            .uri { it.path("/guestbook").build() }
//            .headers { it.contentType = MediaType.APPLICATION_JSON }
//            .body(BodyInserters.fromValue(this.objectMapper.writeValueAsString(newCommentRequest)))
//            .retrieve()
//            .bodyToMono(dev.lutergs.lutergsbackend.domain.guestbook.Comment::class.java)
//            .onErrorResume {
//                when (it) {
//                    is WebClientResponseException -> {
//                        println("POST error! status code : ${it.statusCode}, body : ${it.responseBodyAsString}")
//                    }
//                }
//                Mono.error(it)
//            }
//            .doOnError { it.printStackTrace() }
//            .flatMap { comment ->
//                println("POST guestbook: $comment")
//
//                // delete guestbook
//                // TODO : delete 에 body 가 없도록 해야 함
//                val deleteCommentRequest = DeleteCommentRequest(comment.name, comment.createdAt.toString(), comment.password)
//                webClient
//                    .method(HttpMethod.DELETE)
//                    .uri { it.path("/guestbook").build() }
//                    .headers { it.contentType = MediaType.APPLICATION_JSON }
//                    .body(BodyInserters.fromValue(this.objectMapper.writeValueAsString(deleteCommentRequest)))
//                    .retrieve()
//                    .bodyToMono(DeleteCommentResponse::class.java)
//                    .onErrorResume {
//                        when (it) {
//                            is WebClientResponseException -> {
//                                println("DELETE error! status code : ${it.statusCode}, body : ${it.responseBodyAsString}")
//                            }
//                        }
//                        Mono.error(it)
//                    }
//            }
//            .doOnError { it.printStackTrace() }
//            .block()!!.let { println("DELETE guestbook: $it") }
//    }
//
//    @Test
//    fun pageControllerReflectionTest() {
//        // get page list
//        this.webClient
//            .get()
//            .uri { it
//                .queryParam("index", 0)
//                .queryParam("size", 5)
//                .path("/page/list")
//                .build() }
//            .retrieve()
//            .bodyToFlux(PageKey::class.java)
//            .collectList()
//            .flatMap { pageKeys ->
//                println("GET pageKeys : $pageKeys")
//
//                // get page value
//                this.webClient
//                    .get()
//                    .uri { it
//                        .path("/page/${pageKeys.first().endpoint.value}")
//                        .build() }
//                    .retrieve()
//                    .bodyToMono(Page::class.java)
//            }
//            .block()!!.let { println("GET page : $it") }
//
//        // post page value
//        val addPageRequest = AddPageRequest("test title", listOf("paragraph1", "paragraph2"))
//        this.webClient
//            .post()
//            .uri { it.path("/page").build() }
//            .headers {
//                it.contentType = MediaType.APPLICATION_JSON }
//            .body(BodyInserters.fromValue(this.objectMapper.writeValueAsString(addPageRequest)))
//            .retrieve()
//            .bodyToMono(Page::class.java)
//            .block()!!.let { println("POST page : $it") }
//    }
//
//    @Test
//    fun imageControllerReflectionTest() {
//        // no need because AWS SDK works well with graalVM
//        // https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/setup-project-graalvm.html
//    }
//
//    @Test
//    fun oauthControllerReflectionTest() {
//        this.webClient
//            .get()
//            .uri { it.path("/user").build() }
//            .retrieve()
//            .bodyToMono(User::class.java)
//            .block()!!.let { println("GET user : $it") }
//        this.webClient
//            .post()
//            .uri { it.path("/user").build() }
//            .headers { it.contentType = MediaType.APPLICATION_JSON }
//            .body(BodyInserters.fromValue(this.objectMapper.writeValueAsString(ChangeNickNameDto("LuterGS2"))))
//            .retrieve()
//            .bodyToMono(User::class.java)
//            .block()!!.let { println("POST changeUserName: $it") }
//        this.webClient
//            .post()
//            .uri { it.path("/user").build() }
//            .headers { it.contentType = MediaType.APPLICATION_JSON }
//            .body(BodyInserters.fromValue(this.objectMapper.writeValueAsString(ChangeNickNameDto("LuterGS"))))
//            .retrieve()
//            .bodyToMono(User::class.java)
//            .block()!!.let { println("POST changeUserName: $it") }
//
//        // destruct 시에 Logout 해야 함
//        this.webClient
//            .get()
//            .uri { it.path("/user/logout").build() }
//            .retrieve()
//            .toBodilessEntity()
//            .block()!!.let { println("GET userLogout") }
//    }
//
//    @Test
//    fun pwaSubscriptionControllerReflectionTest() {
//        this.webClient
//            .get()
//            .uri { it.path("/push/topics").build() }
//            .retrieve()
//            .bodyToFlux(Topic::class.java)
//            .collectList()
//            .block()!!.let { println("GET pushTopics : $it") }
//
//
//        val newSubscriberRequest = NewSubscriberRequest(
//            endpoint = "https://web.push.apple.com/QMgU3wLVHR89ksVght6AlZeIRpMR56Z6Y0yyXPv8lElwpqXwYG3phhvwnU9UTIsCXuq46OpOIcNI4NKFj2J9Gm5nHl6pXu4UrtcXNSxGXN6r6BAGdN9i-NDYgMnNNKaaNGA7csgCi-Y4eJQqVQfrZ89vXqT0TUGTtYZuHTtAJvk",
//            key = "BOyEi/OWn+0B6zSrGBSfOb0NBU9PIuYf0rBfvet5YtMDnNMjX2NJjYGAhJjiPuDETy9qgeWUH+GjVZ3zzmwP+po=",
//            auth = "oZejykwmbaL8RQJnCyEQ5Q=="
//        )
//        this.webClient
//            .post()
//            .uri { it
//                .path("/push/subscription")
//                .build() }
//            .body(BodyInserters.fromValue(this.objectMapper.writeValueAsString(newSubscriberRequest)))
//            .retrieve()
//            .bodyToMono(Subscriber::class.java)
//            .block()!!.let { println("POST pushSubscriptions : $it") }
//
//        this.webClient
//            .get()
//            .uri { it
//                .path("/push/subscription/valid")
//                .queryParam("auth", newSubscriberRequest.auth)
//                .build() }
//            .retrieve()
//            .bodyToMono(IsValidResponse::class.java)
//            .block()!!.let { println("GET pushSubscriptionIsValid : $it") }
//    }
//}