package dev.lutergs.lutergsbackend.service

import dev.lutergs.lutergsbackend.repository.PageDataEntity
import reactor.core.publisher.Mono

interface PageInfoRepository {
    fun getPageData(name: String): Mono<PageDataEntity>
}

data class PageData(
    val name: String,
    val index: Int,
    val data: String
)