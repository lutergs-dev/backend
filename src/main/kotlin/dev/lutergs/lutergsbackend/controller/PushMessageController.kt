package dev.lutergs.lutergsbackend.controller

import dev.lutergs.lutergsbackend.service.pushnotification.PushMessage
import dev.lutergs.lutergsbackend.service.pushnotification.PushService
import dev.lutergs.lutergsbackend.service.pushnotification.Subscription
import dev.lutergs.lutergsbackend.service.pushnotification.Topic
import dev.lutergs.lutergsbackend.utils.orElse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.zip
import java.util.UUID

@Component
class PushMessageController(
    private val pushService: PushService,
    @Value("\${custom.push.topic-trigger-key}") private val topicTriggerKey: String
) {
    fun saveSubscription(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(SubscriptionRequest::class.java)
            .flatMap {
                this.pushService.saveSubscription(it) }
            .flatMap { ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(it)) }
            .onErrorResume {
                println(it)
                ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
            }
    }

    fun saveTopic(request: ServerRequest): Mono<ServerResponse> {
        return request.headers().firstHeader("Authorization")
            ?.let { it == this.topicTriggerKey }
            ?.let { isAuthorized ->
                if (!isAuthorized) ServerResponse.status(401).contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse("unauthorized")))
                else request.bodyToMono(NewTopicRequest::class.java)
                    .flatMap { this.pushService.saveTopic(it) }
                    .flatMap { ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Mono.just(it)) }
                    .onErrorResume { ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString())))) } }
            ?: ServerResponse.status(401).contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(ErrorResponse("no given auth")))
    }

    fun getTopics(request: ServerRequest): Mono<ServerResponse> {
        return this.pushService.getTopics().collectList()
            .flatMap { ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(it)) }
            .onErrorResume {
                ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
            }
    }

    fun getSubscribedTopics(request: ServerRequest): Mono<ServerResponse> {
        return request.headers().firstHeader("Authorization")
            ?.let { this.pushService.getSubscribedTopics(it) }
            ?.flatMap { ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(it))
            }
            ?.onErrorResume {
                ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
            }
            ?: ServerResponse.status(401).contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(ErrorResponse("no given auth")))
    }

    fun subscribeToTopic(request: ServerRequest): Mono<ServerResponse> {
        return request.headers().firstHeader("Authorization")
            ?.let { Mono.zip(Mono.just(it), request.bodyToMono(TopicRequest::class.java) ) }
            ?.flatMapMany { this.pushService.subscribeToTopic(it.t1, it.t2.topicUUID) }
            ?.let { ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(it)
            }
            ?.onErrorResume { ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString())))) }
            ?: ServerResponse.status(401).contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(ErrorResponse("no given auth")))
    }

    fun unsubscribeToTopic(request: ServerRequest): Mono<ServerResponse> {
        return request.headers().firstHeader("Authorization")
            ?.let { Mono.zip(Mono.just(it), request.bodyToMono(TopicRequest::class.java) ) }
            ?.flatMapMany { this.pushService.unsubscribeToTopic(it.t1, it.t2.topicUUID) }
            ?.let { ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(it)
            }
            ?.onErrorResume { ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString())))) }
            ?: ServerResponse.status(401).contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(ErrorResponse("no given auth")))
    }

    fun triggerTopic(request: ServerRequest): Mono<ServerResponse> {
        return request.headers().firstHeader("Authorization")
            ?.let { it == this.topicTriggerKey }
            ?.let { isAuthorized ->
                if (!isAuthorized) ServerResponse.status(401).contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse("unauthorized")))
                else request.bodyToMono(TriggerTopicRequest::class.java)
                    .flatMapMany { this.pushService.triggerTopic(it) }
                    .let { responses ->
                        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(responses) }
                    .onErrorResume { err ->
                        ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(ErrorResponse(err.message.orElse(err.stackTraceToString()))))
                    } }
            ?: ServerResponse.status(401).contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(ErrorResponse("no given auth")))
    }

    fun newTopicMakeRequest(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(NewTopicMakeRequest::class.java)
            .flatMap { this.pushService.newTopicMakeRequest(it) }
            .flatMap { isSuccess ->
                if (isSuccess) ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just("{\"status\":\"success\"}"))
                else ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just("{\"status\":\"fail\"}")) }
            .onErrorResume {
                ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
            }
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
    val description: String
) {
    fun toTopic(): Topic {
        return Topic(UUID.randomUUID().toString(), this.topicName, this.description, null)
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