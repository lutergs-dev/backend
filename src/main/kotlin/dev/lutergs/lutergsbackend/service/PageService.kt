package dev.lutergs.lutergsbackend.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PageService(
    private val pageDataRepository: PageRepository
) {

    fun getAllPageDataByName(name: String): Mono<Page> {
        return this.pageDataRepository.getPage(name)
    }

    fun getAllPageName(): Flux<String> {
        return this.pageDataRepository.getAllPageNames()
    }

//    fun addParagraphToPage(pageName: String, paragraphAddRequest: ParagraphAddRequest) {
//
//        this.pageDataRepository.getPage(pageName)
//            .flatMap {
//                when (paragraphAddRequest.getJobType()) {
//
//                }
//                paragraphAddRequest.getJobType()
//            }
//
//    }

}