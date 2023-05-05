package dev.lutergs.lutergsbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class LutergsBackendApplication

fun main(args: Array<String>) {
    runApplication<LutergsBackendApplication>(*args)
}