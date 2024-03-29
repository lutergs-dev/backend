package dev.lutergs.lutergsbackend.utils

import org.reactivestreams.Publisher
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.TimeZone


fun String?.orElse(alternativeValue: String): String {
    return when (this) {
        null -> alternativeValue
        else -> this
    }
}

fun String.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.parse(this)
}

fun LocalDateTime.toDate(offsetHour: Int): Date {
    return this.toInstant(ZoneOffset.ofHours(offsetHour))
        .let { Date.from(it) }
}

fun LocalDateTime.toIsoOffsetString(offsetHour: Int): String {
    return this.atOffset(ZoneOffset.ofHours(offsetHour)).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

fun OffsetDateTime.toDefaultZoneLocalDateTime(): LocalDateTime {
    return this.atZoneSameInstant(TimeZone.getDefault().toZoneId()).toLocalDateTime()
}

fun LocalDateTime.toDefaultZoneOffsetDateTime(): OffsetDateTime {
    return this.atZone(TimeZone.getDefault().toZoneId()).toOffsetDateTime()
}

fun OffsetDateTime.toLong(): Long {
    return this.toInstant().toEpochMilli()
}

fun Long.toOffsetDateTime(zoneOffset: ZoneOffset = ZoneOffset.ofHours(9)): OffsetDateTime {
    return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this), zoneOffset)
}

object ServerResponseUtil {
    data class ErrorResponse(
        val error: String
    )

    inline fun <reified T : Any> okResponse(body: T): Mono<ServerResponse> {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Mono.just(body))
    }

    inline fun <reified T : Any> okResponse(publisher: Publisher<T>): Mono<ServerResponse> {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(publisher)
    }
    fun errorResponse(throwable: Throwable, code: Int = 400): Mono<ServerResponse> {
        return ServerResponse.status(code).contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(ErrorResponse(throwable.message.orElse(throwable.stackTraceToString()))))
    }

    fun errorResponse(message: String, code: Int = 400): Mono<ServerResponse> {
        return ServerResponse.status(code).contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(ErrorResponse(message)))
    }
}
