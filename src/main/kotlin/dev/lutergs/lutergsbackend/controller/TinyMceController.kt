package dev.lutergs.lutergsbackend.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Component
class TinyMceController(
    @Value("\${custom.tinymce.api-key}") private val apiKey: String
) {

    fun getTinyMceApiKey(request: ServerRequest): Mono<ServerResponse> {
        return TinyMceApiKey(apiKey)
            .let { ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(it))
            }
    }
}

data class TinyMceApiKey(
    val key: String
)