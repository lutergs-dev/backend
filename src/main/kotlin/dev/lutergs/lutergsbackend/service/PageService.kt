package dev.lutergs.lutergsbackend.service

import dev.lutergs.lutergsbackend.repository.PageDataEntity
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class PageDataService(
    private val pageDataRepository: PageInfoRepository
) {

    fun getAllPageDataByName(name: String): Mono<PageDataEntity> {
        return this.pageDataRepository.getPageData(name)
    }

}