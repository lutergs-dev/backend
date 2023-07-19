package dev.lutergs.lutergsbackend.repository

import dev.lutergs.lutergsbackend.service.image.ImageRepository
import io.awspring.cloud.s3.ObjectMetadata
import io.awspring.cloud.s3.S3Template
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.model.*
import java.io.InputStream
import java.time.Duration

@Component
class ImageRepositoryImpl(
    private val s3Template: S3Template,
    @Value("\${custom.s3.bucket-name}") private val bucketName: String,
    @Value("\${custom.s3.bucket-directory-prefix}") private val bucketPrefix: String
): ImageRepository {
    override fun getPresignedUrl(fileName: String): String {
        return this.s3Template.createSignedPutURL(
            this.bucketName,
            "${this.bucketPrefix}/$fileName",
            Duration.ofMinutes(2),
            ObjectMetadata.builder()
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build(),
            "binary/octet-stream"
        ).toString()
    }
}