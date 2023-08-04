package dev.lutergs.lutergsbackend.controller

import dev.lutergs.lutergsbackend.service.pushnotification.PushService
import dev.lutergs.lutergsbackend.service.pushnotification.Subscription
import dev.lutergs.lutergsbackend.service.pushnotification.Topic
import dev.lutergs.lutergsbackend.utils.orElse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class PushMessageController(
    private val pushService: PushService
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
        return request.bodyToMono(NewTopicRequest::class.java)
            .flatMap {
                this.pushService.saveTopic(it) }
            .flatMap { ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(it)) }
            .onErrorResume {
                ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
            }
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

    fun triggerTopic(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(TriggerTopicRequest::class.java)
            .flatMapMany {
                this.pushService.triggerTopic(it) }
            .let {
                ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(it) }
            .onErrorResume {
                ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(ErrorResponse(it.message.orElse(it.stackTraceToString()))))
            }.log()
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
    val message: String
)