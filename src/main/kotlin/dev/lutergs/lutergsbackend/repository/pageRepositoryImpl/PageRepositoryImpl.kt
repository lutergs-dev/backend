package dev.lutergs.lutergsbackend.repository.pageRepositoryImpl

import dev.lutergs.lutergsbackend.repository.userInterfaceImpl.UserEntityReactiveRepository
import dev.lutergs.lutergsbackend.service.page.*
import dev.lutergs.lutergsbackend.service.user.NickName
import dev.lutergs.lutergsbackend.service.user.User
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class PageRepositoryImpl(
    private val pageKeyReactiveRepository: PageKeyReactiveRepository,
    private val pageValueReactiveRepository: PageValueReactiveRepository,
    private val userEntityReactiveRepository: UserEntityReactiveRepository
): PageRepository {
    override fun getPageByEndpoint(endpoint: Endpoint): Mono<Page> {
        return this.pageKeyReactiveRepository.findByEndpoint(endpoint.value)
            .flatMap { pageKeyEntity -> Mono.zip(
                this.toPageKey(pageKeyEntity),
                this.pageValueReactiveRepository.findByPageKeyId(pageKeyEntity.id!!)
                    .flatMap { Mono.just(it.toPageValue()) }
            )}.flatMap { Mono.just(Page(it.t1, it.t2)) }
    }

    override fun getPageKeyList(pageIndex: Int, pageSize: Int): Flux<PageKey> {
        return this.pageKeyReactiveRepository.findByOrderByIdDesc(Pageable.ofSize(pageSize).withPage(pageIndex))
            .flatMap { this.toPageKey(it) }
    }

    override fun getPageOfUser(user: User): Flux<PageKey> {
        return this.userEntityReactiveRepository.findDistinctFirstByEmail(user.email.toString())
            .flatMapMany { this.pageKeyReactiveRepository.findByUserId(it.id!!) }
            .flatMap { Mono.just(this.toPageKey(it, user)) }
    }

    override fun getPageValue(pageKey: PageKey): Mono<PageValue> {
        return pageKey.id
            ?.let { this.pageValueReactiveRepository.findByPageKeyId(it) }
            ?.flatMap { Mono.just(it.toPageValue()) }
            ?: Mono.error(IllegalArgumentException("page ID 가 존재하지 않습니다."))
    }

    @Transactional
    override fun savePage(page: Page, user: User): Mono<Page> {
        return PageKeyEntity.fromPageKey(page.pageKey, user.id!!)
            .let { this.pageKeyReactiveRepository.save(it) }
            .flatMap { pageKeyEntity ->
                PageValueEntity.fromPageValue(page.pageValue, pageKeyEntity)
                    .let { this.pageValueReactiveRepository.save(it) }
                    .flatMap { Mono.just(Page(
                        this.toPageKey(pageKeyEntity, user),
                        it.toPageValue()
                    )) }
            }
    }

    private fun toPageKey(from: PageKeyEntity): Mono<PageKey> {
        return this.userEntityReactiveRepository.findById(from.userId!!)
            .flatMap { Mono.just(PageKey(from.id, from.title!!, Endpoint(from.endpoint!!), NickName(it.nickName!!), from.createdAt!!)) }
    }

    private fun toPageKey(from: PageKeyEntity, user: User): PageKey {
        return PageKey(from.id!!, from.title!!, Endpoint(from.endpoint!!), user.nickName, from.createdAt!!)
    }
}