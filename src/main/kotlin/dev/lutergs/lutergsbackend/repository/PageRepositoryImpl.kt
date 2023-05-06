package dev.lutergs.lutergsbackend.repository

import dev.lutergs.lutergsbackend.service.page.Page
import dev.lutergs.lutergsbackend.service.page.PageList
import dev.lutergs.lutergsbackend.service.page.PageRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@Repository
class PageRepositoryRepositoryImpl(
    private val pageInfoReactiveMongoRepository: PageInfoReactiveMongoRepository,
    private val pageListReactiveMongoRepository: PageListReactiveMongoRepository
): PageRepository {

    override fun getPage(endpoint: String): Mono<Page> {
        return this.pageInfoReactiveMongoRepository.getPageDataByEndpoint(endpoint)
            .flatMap { Mono.just(it.toPage()) }
    }

    override fun getAllPageNames(): Flux<PageList> {
        return this.pageListReactiveMongoRepository.findAll()
            .flatMap { Mono.justOrEmpty(PageList(endpoint = it.endpoint!!, name = it.name!!)) }
    }

    override fun savePage(page: Page): Mono<PageListEntity> {
        return PageEntity.fromPage(page)
            .let { pageEntity ->
                this.pageInfoReactiveMongoRepository.findById(pageEntity.id!!)
                    .hasElement()
                    .flatMap { alreadyExists ->
                        if (alreadyExists) Mono.error(Exception("같은 제목의 글이 존재합니다"))
                        else {
                            this.pageInfoReactiveMongoRepository.save(pageEntity)
                        }
                    }.flatMap {
                        this.pageListReactiveMongoRepository.save(PageListEntity.fromPageEntity(it))
                    }
            }
    }
}