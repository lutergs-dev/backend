package dev.lutergs.lutergsbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication


@SpringBootApplication
class LutergsBackendApplication

fun main(args: Array<String>) {
    SpringApplicationBuilder(LutergsBackendApplication::class.java)
        .listeners(ApplicationContextInjector())
        .run()
}