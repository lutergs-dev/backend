package dev.lutergs.lutergsbackend.controller

import dev.lutergs.lutergsbackend.service.pushnotification.*
import dev.lutergs.lutergsbackend.utils.ServerResponseUtil
import org.springframework.beans.factory.annotation.Value
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
    fun isValidSubscription(request: ServerRequest): Mono<ServerResponse> {
        return request.queryParamOrNull("auth")
            ?.let { this.pushService.isValidSubscription(it) }
            ?.flatMap {
                if (it == true) ServerResponseUtil.okResponse("{\"isValid\":\"${it}\"}}")
                else ServerResponseUtil.errorResponse("{\"isValid\":\"${it}\"}}", 404) }
            ?: ServerResponseUtil.errorResponse("no provided auth")
    }

    fun saveSubscription(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(SubscriptionRequest::class.java)
            .flatMap { this.pushService.saveSubscription(it) }
            .flatMap { ServerResponseUtil.okResponse(it) }
            .onErrorResume { ServerResponseUtil.errorResponse(it) }
    }

    fun getSubscribedTopics(request: ServerRequest): Mono<ServerResponse> {
        return request.headers().firstHeader("Authorization")
            ?.let { this.pushService.getSubscribedTopics(it) }
            ?.flatMap { ServerResponseUtil.okResponse(it) }
            ?.onErrorResume { ServerResponseUtil.errorResponse(it) }
            ?: ServerResponseUtil.errorResponse("no given auth", 401)
    }

    fun subscribeToTopic(request: ServerRequest): Mono<ServerResponse> {
        return request.headers().firstHeader("Authorization")
            ?.let { Mono.zip(Mono.just(it), request.bodyToMono(TopicRequest::class.java) ) }
            ?.flatMap { authAndTopicRequest ->
                this.pushService.subscribeToTopic(authAndTopicRequest.t1, authAndTopicRequest.t2.topicUUID)
                    .flatMap { ServerResponseUtil.okResponse(it) }
                    .onErrorResume { ServerResponseUtil.errorResponse(it) } }
            ?: ServerResponseUtil.errorResponse("no given auth", 401)
    }

    fun unsubscribeFromTopic(request: ServerRequest): Mono<ServerResponse> {
        return Pair(
            request.headers().firstHeader("Authorization"),
            request.queryParamOrNull("uuid")
        ).let { pair ->
            when {
                pair.first == null && pair.second == null -> ServerResponseUtil.errorResponse("no auth and topic UUID provided", 401)
                pair.first == null && pair.second != null -> ServerResponseUtil.errorResponse("no auth provided", 401)
                pair.first != null && pair.second == null -> ServerResponseUtil.errorResponse("no topic UUID provided")
                else -> this.pushService.unsubscribeFromTopic(pair.first!!, pair.second!!)
                    .then(ServerResponseUtil.okResponse("{\"result\":\"success\"}"))
                    .onErrorResume { ServerResponseUtil.errorResponse(it) }
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
                if (isAuthorized) request.queryParamOrNull("uuid")
                    ?.let { this.pushService.getTopic(it) }
                    ?.flatMap { ServerResponseUtil.okResponse(it) }
                    ?.onErrorResume { ServerResponseUtil.errorResponse(it) }
                    ?: ServerResponseUtil.errorResponse("no UUID provided")
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
                if (isAuthorized) request.queryParamOrNull("uuid")
                    ?.let { this.pushService.deleteTopic(it) }
                    ?.then(ServerResponseUtil.okResponse("{\"result\":\"success\"}"))
                    ?.onErrorResume {ServerResponseUtil.errorResponse(it) }
                    ?: ServerResponseUtil.errorResponse("no uuid provided")
                else ServerResponseUtil.errorResponse("unauthorized", 401)}
            ?: ServerResponseUtil.errorResponse("no given auth", 401)
    }

    fun triggerTopic(request: ServerRequest): Mono<ServerResponse> {
        return request.headers().firstHeader("Authorization")
            ?.let { it == this.topicTriggerKey }
            ?.let { isAuthorized ->
                if (!isAuthorized) ServerResponseUtil.errorResponse("unauthorized", 401)
                else request.bodyToMono(TriggerTopicRequest::class.java)
                    .flatMapMany { this.pushService.triggerTopic(it) }
                    .collectList()
                    .flatMap { ServerResponseUtil.okResponse(it) }
                    .onErrorResume { ServerResponseUtil.errorResponse(it) } }
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

data class SubscriptionRequest(
    val endpoint: String,
    val key: String,
    val auth: String
) {
    fun toSubscription(): Subscription {
        return Subscription(auth, key, endpoint, null)
    }
}

data class NewTopicRequest(
    val topicName: String,
    val description: String,
    val type: String
) {
    fun toTopic(): Topic {
        return Topic(UUID.randomUUID().toString(), this.topicName, this.description,  TopicType.valueOf(this.type), null)
    }
}

data class TriggerTopicRequest(
    val topicUUID: String,
    val title: String,
    val message: String,
    val imageUrl: String?
) {
    fun toPushMessage(): PushMessage {
        return PushMessage(this.title, this.message, this.imageUrl)
    }
}

data class TopicRequest(
    val topicUUID: String
)

data class NewTopicMakeRequest(
    val description: String
)