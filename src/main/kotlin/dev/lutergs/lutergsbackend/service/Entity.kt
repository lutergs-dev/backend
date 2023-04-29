package dev.lutergs.lutergsbackend.service

import jakarta.xml.bind.DatatypeConverter
import java.security.MessageDigest


enum class Job {
    CREATE, ADD_FIRST, ADD_BETWEEN, ADD_LAST, MODIFY, DELETE
}

data class Page(
    val name: String,
    val paragraphs: List<Paragraph>
)

data class Paragraph(
    val data: String,
    val hash: String
) {
    companion object {

        private val messageDigest: MessageDigest = MessageDigest.getInstance("MD5")

        private fun hashStringToMd5(target: String): String {
            messageDigest.reset()
            messageDigest.update(target.toByteArray())
            return messageDigest.digest()
                .let { DatatypeConverter.printHexBinary(it).uppercase() }
        }

        fun fromString(data: String): Paragraph {
            return Paragraph(data, hashStringToMd5(data))
        }
    }
}