package dev.lutergs.lutergsbackend.infra.controller

import com.fasterxml.jackson.annotation.JsonProperty
import dev.lutergs.lutergsbackend.domain.push.PushMessageRequest
import dev.lutergs.lutergsbackend.infra.impl.push.Response
import dev.lutergs.lutergsbackend.service.PushService
import dev.lutergs.lutergsbackend.utils.ServerResponseUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.queryParamOrNull
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class PushMessageController(
    private val pushService: PushService,
    @Value("\${custom.push.topic-trigger-key}") private val topicTriggerKey: String
) {
// about subscription
    fun isValidSubscriber(request: ServerRequest): Mono<ServerResponse> {
        return request.pathVariable("endpoint")
            .let { this.pushService.isSubscriberExists(it) }
            .flatMap { isExists ->
                if (isExists) ServerResponseUtil.okResponse("")
                else ServerResponseUtil.errorResponse("", 404) }
            .onErrorResume { ServerResponseUtil.errorResponse(it) }
    }

    fun getSubscriber(request: ServerRequest): Mono<ServerResponse> {
        return SubscriberRequest(
                endpoint = request.pathVariable("endpoint"),
                auth = request.headers().firstHeader("Authorization") ?: "",
                getHistory = request.queryParamOrNull("history").toBoolean())
            .let { this.pushService.getSubscriber(it) }
            .flatMap { ServerResponseUtil.okResponse(it) }
            .onErrorResume { ServerResponseUtil.errorResponse(it) }
    }

    fun saveSubscription(request: ServerRequest): Mono<ServerResponse> {
        val endpoint = request.pathVariable("endpoint")
        return request.bodyToMono(NewSubscriberRequest::class.java)
            .flatMap { this.pushService.saveSubscriber(it, endpoint) }
            .flatMap { ServerResponseUtil.okResponse(it) }
            .onErrorResume { ServerResponseUtil.errorResponse(it) }
    }

    fun getSubscribedTopics(request: ServerRequest): Mono<ServerResponse> {
        return SubscriberRequest(
                endpoint = request.pathVariable("endpoint"),
                auth = request.headers().firstHeader("Authorization") ?: "",
                getHistory = request.queryParamOrNull("history").toBoolean())
            .let { this.pushService.getSubscribedTopics(it) }
            .collectList()
            .flatMap { ServerResponseUtil.okResponse(it) }
            .onErrorResume { ServerResponseUtil.errorResponse(it) }
    }

    fun subscribeToTopic(request: ServerRequest): Mono<ServerResponse> {
        return SubscriberRequest(
                endpoint = request.pathVariable("endpoint"),
                auth = request.headers().firstHeader("Authorization") ?: "",
                getHistory = false)
            .let { Mono.zip(Mono.just(it), request.bodyToMono(TopicRequest::class.java)) }
            .flatMap { subscriberAndTopic ->
                Mono.fromCallable { UUID.fromString(subscriberAndTopic.t2.topicUUID) }
                    .flatMap { this.pushService.subscribeToTopic(subscriberAndTopic.t1, it) }
                    .then(ServerResponseUtil.okResponse(""))
                    .onErrorResume { ServerResponseUtil.errorResponse(it) }
            }
    }

    fun unsubscribeFromTopic(request: ServerRequest): Mono<ServerResponse> {
        return SubscriberRequest(
                endpoint = request.pathVariable("endpoint"),
                auth = request.headers().firstHeader("Authorization") ?: "",
                getHistory = false)
            .let { Mono.zip(Mono.just(it), request.bodyToMono(TopicRequest::class.java)) }
            .flatMap { subscriberAndTopic ->
                Mono.fromCallable { UUID.fromString(subscriberAndTopic.t2.topicUUID) }
                    .flatMap { this.pushService.unsubscribeFromTopic(subscriberAndTopic.t1, it) }
                    .then(ServerResponseUtil.okResponse(""))
                    .onErrorResume {
                        when (it) {
                            is IllegalArgumentException -> ServerResponseUtil.errorResponse(it, 400)
                            else -> ServerResponseUtil.errorResponse(it, 500)
                        }
                    }
            }
    }

// about topics
    fun getTopics(request: ServerRequest): Mono<ServerResponse> {
        return this.pushService.getTopics().collectList()
            .flatMap { ServerResponseUtil.okResponse(it) }
            .onErrorResume { ServerResponseUtil.errorResponse(it) }
    }

    fun getTopic(request: ServerRequest): Mono<ServerResponse> {
        return request.headers().firstHeader("Authorization")
            ?.let { it == this.topicTriggerKey }
            ?.let { isAuthorized ->
                if (isAuthorized) request.pathVariable("uuid")
                    .let { UUID.fromString(it) }
                    .let { this.pushService.getTopic(it, request.queryParamOrNull("history").toBoolean()) }
                    .flatMap { ServerResponseUtil.okResponse(it) }
                    .onErrorResume { ServerResponseUtil.errorResponse(it) }
                else ServerResponseUtil.errorResponse("no valid auth", 401) }
            ?: ServerResponseUtil.errorResponse("no auth provided", 401)
    }

    fun createTopic(request: ServerRequest): Mono<ServerResponse> {
        return request.headers().firstHeader("Authorization")
            ?.let { it == this.topicTriggerKey }
            ?.let { isAuthorized ->
                if (!isAuthorized) ServerResponseUtil.errorResponse("unauthorized", 401)
                else request.bodyToMono(NewTopicRequest::class.java)
                    .flatMap { this.pushService.createTopic(it) }
                    .flatMap { ServerResponseUtil.okResponse(it) }
                    .onErrorResume { ServerResponseUtil.errorResponse(it) } }
            ?: ServerResponseUtil.errorResponse("no given auth", 401)
    }

    fun deleteTopic(request: ServerRequest): Mono<ServerResponse> {
        return request.headers().firstHeader("Authorization")
            ?.let { it == this.topicTriggerKey }
            ?.let { isAuthorized ->
                if (isAuthorized) request.pathVariable("uuid")
                    .let { UUID.fromString(it) }
                    .let { this.pushService.deleteTopic(it) }
                    .then(ServerResponseUtil.okResponse(""))
                    .onErrorResume {ServerResponseUtil.errorResponse(it) }
                else ServerResponseUtil.errorResponse("unauthorized", 401)}
            ?: ServerResponseUtil.errorResponse("no given auth", 401)
    }

    fun triggerTopic(request: ServerRequest): Mono<ServerResponse> {
        return request.headers().firstHeader("Authorization")
            ?.let { it == this.topicTriggerKey }
            ?.let { isAuthorized ->
                if (isAuthorized) Mono.zip(
                        request.bodyToMono(TriggerTopicRequest::class.java),
                        Mono.fromCallable { UUID.fromString(request.pathVariable("uuid")) })
                    .flatMapMany { this.pushService.triggerTopic(it.t1, it.t2) }
                    .let { ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(it, Response::class.java) }
                    .onErrorResume { ServerResponseUtil.errorResponse(it) }
                else ServerResponseUtil.errorResponse("unauthorized") }
            ?: ServerResponseUtil.errorResponse("no given auth", 401)
    }

// else
    fun newTopicMakeRequest(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(NewTopicMakeRequest::class.java)
            .flatMap { this.pushService.newTopicMakeRequest(it) }
            .flatMap { isSuccess ->
                if (isSuccess) ServerResponseUtil.okResponse("{\"status\":\"success\"}")
                else ServerResponseUtil.errorResponse("fail to make request") }
            .onErrorResume {ServerResponseUtil.errorResponse(it) }
    }
}

data class IsValidResponse(
    @JsonProperty("isValid") val isValid: Boolean
) {
    override fun toString(): String {
        return "{\"isValid\": ${this.isValid}}"
    }
}

data class NewSubscriberRequest(
    val key: String,
    val auth: String
)

data class SubscriberRequest(
    val endpoint: String,
    val auth: String,
    val getHistory: Boolean
)

data class NewTopicRequest(
    val topicName: String,
    val description: String
)

data class TriggerTopicRequest(
    val title: String,
    val message: String,
    val imageUrl: String?
) {
    fun toPushMessage(): PushMessageRequest {
        return PushMessageRequest(this.title, this.message, this.imageUrl)
    }
}

data class TopicRequest(
    val topicUUID: String
)

data class NewTopicMakeRequest(
    val description: String
)