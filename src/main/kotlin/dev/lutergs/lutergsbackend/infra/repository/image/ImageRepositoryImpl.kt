package dev.lutergs.lutergsbackend.infra.repository.image

import dev.lutergs.lutergsbackend.domain.image.ImageRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Component
class ImageRepositoryImpl(
    @Value("\${custom.aws.credentials.access-key}") private val accessKey: String,
    @Value("\${custom.aws.credentials.secret-key}") private val secretKey: String,
    @Value("\${custom.aws.region}") private val region: String,
    @Value("\${custom.aws.s3.bucket-name}") private val bucketName: String,
    @Value("\${custom.aws.s3.bucket-directory-prefix}") private val bucketPrefix: String
): ImageRepository {

    private val s3Presigner = S3Presigner.builder()
        .region(Region.of(this.region))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(this.accessKey, this.secretKey)
        ))
        .build()

    override fun getPresignedUrl(fileName: String): String {
        return PutObjectRequest.builder()
            .bucket(this.bucketName)
            .key("${this.bucketPrefix}/$fileName")
            .acl(ObjectCannedACL.PUBLIC_READ)
            .contentType("application/octet-stream")
            .build()
            .let { PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(2))
                .putObjectRequest(it)
                .build() }
            .let { this.s3Presigner.presignPutObject(it) }
            .url()
            .toString()
    }
}