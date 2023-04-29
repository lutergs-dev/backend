package dev.lutergs.lutergsbackend.controller

import dev.lutergs.lutergsbackend.service.Job
import reactor.core.publisher.Flux
import java.lang.IllegalArgumentException


data class ParagraphAddRequest(
    val beforeParagraphHash: String?,
    val afterParagraphHash: String?,
    val paragraph: String
) {
    fun getJobType(): Job {
        return when {
            beforeParagraphHash == null && afterParagraphHash == null -> throw IllegalArgumentException("문단을 추가할 페이지가 존재하지 않습니다.")
            beforeParagraphHash == null && afterParagraphHash != null -> Job.ADD_FIRST
            beforeParagraphHash != null && afterParagraphHash == null -> Job.ADD_LAST
            else -> Job.ADD_BETWEEN
        }
    }
}

data class AllPageNames(
    val names: List<String>
) {
    companion object {
        fun fromFluxStrings(data: Flux<String>): AllPageNames {
            return AllPageNames(data.collectList().block()!!)
        }
    }
}