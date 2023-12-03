package dev.lutergs.lutergsbackend.infra.impl.user

import com.fasterxml.jackson.annotation.JsonProperty
import dev.lutergs.lutergsbackend.domain.user.Email
import dev.lutergs.lutergsbackend.domain.user.NickName
import dev.lutergs.lutergsbackend.domain.user.OAuthRequester
import dev.lutergs.lutergsbackend.domain.user.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

@Component
class OAuthRequesterImpl(
    @Value("\${custom.oauth.client-id}") private val clientId: String,
    @Value("\${custom.oauth.client-secret}") private val clientSecret: String
): OAuthRequester {

    private val oauthInfoRequester: WebClient = WebClient.builder()
        .baseUrl("https://oauth2.googleapis.com/token")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .build()

    private val userInfoRequester: WebClient = WebClient.builder()
        .baseUrl("https://www.googleapis.com/oauth2/v1/userinfo")
        .build()

    private fun getGoogleOauthInfo(code: String, redirectUri: String): Mono<GoogleOAuthInfo> {
        return this.oauthInfoRequester
            .post()
            .body(BodyInserters
                .fromFormData("code", code)
                .with("client_id", this.clientId)
                .with("client_secret", this.clientSecret)
                .with("grant_type", "authorization_code")
                .with("redirect_uri", redirectUri)
            )
            .retrieve()
            .bodyToMono(GoogleOAuthInfo::class.java)
            .onErrorResume(WebClientResponseException::class.java) {
                Mono.error(RuntimeException("${it.localizedMessage}.    body: ${it.responseBodyAsString}"))
            }
    }

    private fun getGoogleUserInfo(accessToken: String): Mono<GoogleUserInfo> {
        return this.userInfoRequester
            .get()
            .uri {it
                .queryParam("access_token", accessToken)
                .queryParam("alt", "json")
                .build() }
            .retrieve()
            .bodyToMono(GoogleUserInfo::class.java)
            .onErrorResume(WebClientResponseException::class.java) {
                Mono.error(RuntimeException("${it.localizedMessage}.    body: ${it.responseBodyAsString}"))
            }
    }

    override fun getUserByCode(code: String, redirectionUrl: String): Mono<User> {
        return this.getGoogleOauthInfo(code, redirectionUrl)
            .flatMap { this.getGoogleUserInfo(it.accessToken) }
            .flatMap { Mono.just(it.toNewUser()) }
    }
}



data class GoogleOAuthInfo(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("expires_in") val expiresIn: Int,
    @JsonProperty("scope") val scope: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("id_token") val idToken: String
)

data class GoogleUserInfo(
    @JsonProperty("id") val id: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("verified_email") val isVerified: Boolean,
    @JsonProperty("picture") val pictureUrl: String
) {
    fun toNewUser(): User {
        return User(
            null,
            Email.fromFullString(this.email),
            LocalDateTime.now(),
            NickName(UUID.randomUUID().toString())
        )
    }
}