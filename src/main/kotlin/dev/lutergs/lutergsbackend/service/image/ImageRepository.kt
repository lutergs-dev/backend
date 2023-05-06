package dev.lutergs.lutergsbackend.service.image

interface ImageRepository {
    fun getPresignedUrl(fileName: String): String
}