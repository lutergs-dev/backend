package dev.lutergs.lutergsbackend.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono


@Bean
fun corsWebFilter(): CorsWebFilter {

    return run {
        CorsConfiguration()
            .apply {
                this.setAllowedOriginPatterns(listOf("http://localhost:3000"))
                this.allowedHeaders
                this.maxAge = 8000L;
                this.addAllowedMethod("*")
            }
    }.let {
        UrlBasedCorsConfigurationSource()
            .apply {
                this.registerCorsConfiguration("/**", it)
            }
    }.let {
        CorsWebFilter(it)
    }
}

@Component
class CustomWebFilter(
    @Value("\${custom.frontend-server}") private val frontendServerUrl: String
): WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return exchange.response.headers
            .apply {
                this.add("Access-Control-Allow-Origin", frontendServerUrl)
                this.add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
            }.let { chain.filter(exchange) }
    }
}