package dev.lutergs.lutergsbackend.service

import dev.lutergs.lutergsbackend.utils.Hasher
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
        fun fromString(data: String): Paragraph {
            return Paragraph(data, Hasher.hashStringToMd5(data))
        }
    }
}