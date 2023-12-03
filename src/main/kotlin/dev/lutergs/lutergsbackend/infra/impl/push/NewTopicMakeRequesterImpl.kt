package dev.lutergs.lutergsbackend.infra.impl.push

import com.fasterxml.jackson.databind.ObjectMapper
import dev.lutergs.lutergsbackend.domain.push.NewTopicMakeRequester
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

// ntfy requester
@Component
class NewTopicMakeRequesterImpl(
    @Value("\${custom.push.new-topic.request-url}") private val requestUrl: String,
    @Value("\${custom.push.new-topic.username}") username: String,
    @Value("\${custom.push.new-topic.password}") password: String,
    private val objectMapper: ObjectMapper
): NewTopicMakeRequester {

    private val newTopicMakeRequester: WebClient = WebClient.builder()
        .baseUrl(this.requestUrl)
        .defaultHeaders {
            it.contentType = MediaType.APPLICATION_JSON
            it.setBasicAuth(username, password)
        }
        .build()

    override fun request(description: String): Mono<Boolean> {
        return this.newTopicMakeRequester
            .post()
            .body(
                NewTopicMakeRequestDto.fromDescription(description)
                .let { this.objectMapper.writeValueAsString(it) }
                .let { BodyInserters.fromValue(it) })
            .retrieve()
            .bodyToMono(NewTopicMakeResponseDto::class.java)
            .flatMap { Mono.just(true) }
            .onErrorResume { Mono.just(false) }
    }
}

data class NewTopicMakeRequestDto(
    val topic: String,
    val title: String,
    val message: String
) {
    companion object {
        fun fromDescription(description: String): NewTopicMakeRequestDto {
            return NewTopicMakeRequestDto("lutergs-pwa", "new topic make request!", description)
        }
    }
}

data class NewTopicMakeResponseDto(
    val id: String,
    val time: Long,
    val expires: Long,
    val event: String,
    val topic: String,
    val title: String,
    val message: String
)