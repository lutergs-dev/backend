package dev.lutergs.lutergsbackend.utils

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*


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
    return this.toInstant(ZoneOffset.ofHours(9))
        .let { Date.from(it) }
}