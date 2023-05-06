package dev.lutergs.lutergsbackend.controller

import dev.lutergs.lutergsbackend.service.image.ImageService
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono

@Component
class ImageController(
    private val imageService: ImageService
) {

    fun getPresignedImageUrl(request: ServerRequest): Mono<ServerResponse> {
        return runCatching {
            request.queryParam("name")
                .map { filename -> this.imageService.getPresignedUrl(filename) }
                .orElseThrow()
        }.mapCatching {
            ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(PresignedUrlResponse(it)))
        }.recoverCatching {
            ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(ErrorResponse(it.localizedMessage)))
        }.getOrThrow()
    }
}

data class PresignedUrlRequest(
    val imageName: String
)

data class PresignedUrlResponse(
    val url: String
)