package dev.lutergs.lutergsbackend.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatusCode
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.BodyInserters
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
    @Value("\${custom.server.url.frontend}") frontendServerUrl: String,
    @Value("\${custom.server.url.frontend-app}") frontendAppUrl: String
): WebFilter {

    private val allowList = listOf(frontendServerUrl, frontendAppUrl)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return exchange.request.headers.origin
            ?.let {
                if (allowList.contains(it)) {
                    exchange.response.headers
                        .apply {
                            this.add("Access-Control-Allow-Origin", it)
                            this.add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                            this.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE")
                            this.add("Access-Control-Allow-Credentials", "true")
                        }
                }
                exchange
            }
            ?.let { chain.filter(it) }
            ?: chain.filter(exchange)
    }
}