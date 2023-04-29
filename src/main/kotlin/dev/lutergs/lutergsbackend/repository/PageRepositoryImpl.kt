package dev.lutergs.lutergsbackend.repository

import dev.lutergs.lutergsbackend.service.Page
import dev.lutergs.lutergsbackend.service.PageRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono



@Repository
class PageRepositoryRepositoryImpl(
    private val pageInfoReactiveMongoRepository: PageInfoReactiveMongoRepository,
    private val pageListReactiveMongoRepository: PageListReactiveMongoRepository
): PageRepository {

    override fun getPage(name: String): Mono<Page> {
        return this.pageInfoReactiveMongoRepository.getPageDataByName(name)
            .flatMap { Mono.just(it.toPage()) }
    }

    override fun getAllPageNames(): Flux<String> {
        return this.pageListReactiveMongoRepository.findAll()
            .flatMap { Mono.justOrEmpty(it.name) }
    }
}