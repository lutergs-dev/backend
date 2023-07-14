package dev.lutergs.lutergsbackend.utils

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.random.Random


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
    return this.atOffset(ZoneOffset.ofHours(9)).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun generateRandomString(length: Int): String {
    return (1..length)
        .map { Random.nextInt(0, charPool.size).let { charPool[it] } }
        .joinToString("")
}