package dev.lutergs.lutergsbackend.domain.image

interface ImageRepository {
    fun getPresignedUrl(fileName: String): String
}