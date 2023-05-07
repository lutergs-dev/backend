package dev.lutergs.lutergsbackend.utils

import java.time.LocalDateTime


fun String?.orElse(alternativeValue: String): String {
    return when (this) {
        null -> alternativeValue
        else -> this
    }
}

fun String.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.parse(this)
}