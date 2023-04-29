package dev.lutergs.lutergsbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource


@SpringBootApplication
class LutergsBackendApplication

fun main(args: Array<String>) {
    runApplication<LutergsBackendApplication>(*args)
}


@Bean
fun corsFilter(): CorsWebFilter {
    val config = CorsConfiguration()

    // Possibly...
    // config.applyPermitDefaultValues()
    config.allowCredentials = true
    config.addAllowedOrigin("http://localhost:3000")
    config.addAllowedHeader("*")
    config.addAllowedMethod("*")
    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", config)
    return CorsWebFilter(source)
}