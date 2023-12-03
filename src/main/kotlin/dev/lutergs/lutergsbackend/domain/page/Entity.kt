package dev.lutergs.lutergsbackend.domain.page

import dev.lutergs.lutergsbackend.domain.user.NickName
import java.time.LocalDateTime
import java.util.UUID


data class Page(
    val pageKey: PageKey,
    val pageValue: PageValue
)

data class PageKey(
    val id: Long?,
    val title: String,
    val endpoint: Endpoint,
    val nickName: NickName,
    val createdAt: LocalDateTime
)

data class PageValue(
    val id: String?,
    val paragraphs: List<String>
)

data class Endpoint(
    val value: String
) {
    companion object {
        fun create(): Endpoint {
            return Endpoint(UUID.randomUUID().toString())
        }
    }
}
