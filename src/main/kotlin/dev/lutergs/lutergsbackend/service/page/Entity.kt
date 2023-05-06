package dev.lutergs.lutergsbackend.service.page

import dev.lutergs.lutergsbackend.utils.Hasher


enum class Job {
    CREATE, ADD_FIRST, ADD_BETWEEN, ADD_LAST, MODIFY, DELETE
}

data class Page(
    val id: String,
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

data class PageList(
    val endpoint: String,
    val name: String
)