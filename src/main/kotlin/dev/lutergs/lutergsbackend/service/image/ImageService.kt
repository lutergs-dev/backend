package dev.lutergs.lutergsbackend.service.image

import org.springframework.stereotype.Component

@Component
class ImageService(
    private val imageRepository: ImageRepository
) {

    fun getPresignedUrl(fileName: String): String {
        return this.imageRepository.getPresignedUrl(fileName)
    }
}