package dev.lutergs.lutergsbackend.utils


fun String?.orElse(alternativeValue: String): String {
    return when (this) {
        null -> alternativeValue
        else -> this
    }
}