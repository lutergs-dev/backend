package dev.lutergs.lutergsbackend.infra.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class CustomWebFilter(
    @Value("\${custom.server.url.frontend}") frontendServerUrl: String,
    @Value("\${custom.server.url.frontend-app}") frontendAppUrl: String
): WebFilter {

    private val allowList = listOf(frontendServerUrl, frontendAppUrl)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val serverUrl = exchange.request.headers.origin
            ?.takeIf { this.allowList.contains(it) }
            ?: this.allowList[0]
        return exchange.response.headers
            .apply {
                this.add("Access-Control-Allow-Origin", serverUrl)
                this.add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization")
                this.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, HEAD")
                this.add("Access-Control-Allow-Credentials", "true")
            }.let { chain.filter(exchange) }
    }
}