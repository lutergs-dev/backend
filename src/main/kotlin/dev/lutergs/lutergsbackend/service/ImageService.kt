package dev.lutergs.lutergsbackend.service

import dev.lutergs.lutergsbackend.domain.image.ImageRepository

class ImageService(
    private val imageRepository: ImageRepository
) {

    fun getPresignedUrl(fileName: String): String {
        return this.imageRepository.getPresignedUrl(fileName)
    }
}