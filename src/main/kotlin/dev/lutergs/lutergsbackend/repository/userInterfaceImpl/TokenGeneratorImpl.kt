package dev.lutergs.lutergsbackend.repository.userInterfaceImpl

import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.RSADecrypter
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jose.util.Base64URL
import dev.lutergs.lutergsbackend.service.user.Email
import dev.lutergs.lutergsbackend.service.user.TokenGenerator
import dev.lutergs.lutergsbackend.service.user.User
import org.springframework.beans.TypeMismatchException
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Component
class TokenGeneratorImpl: TokenGenerator {

    private val rsaKey = RSAKeyGenerator(2048, false).generate()
    private val jweEncryptor = RSAEncrypter(rsaKey)
    private val jweDecrypter = RSADecrypter(rsaKey)
    private val jwsSigner = MACSigner(rsaKey.toJSONString())


    override fun createTokenFromUser(user: User): String {
        return this.createJWSfromUser(user)
            .let { this.encryptJWStoJWE(it) }
            .serialize()
    }

    override fun getEmailFromToken(token: String): Email {
        return this.decryptJWEToJWS(token)
            .let { this.decryptJWStoUser(it) }
    }

    private fun userToMap(user: User): Map<String, String> {
//        val now = LocalDateTime.now()
        return mapOf(
            Pair("email", user.email.toString())
//            Pair("exp", now.plusHours(1).format(DateTimeFormatter.ISO_DATE)),
//            Pair("nbf", now.format(DateTimeFormatter.ISO_DATE))
        )
    }

    private fun mapToEmail(map: MutableMap<String, Any>): Email {
        return Email.fromFullString(map["email"] as String)
    }

    private fun checkValidToken(map: MutableMap<String, Any>) {
        val now = LocalDateTime.now()

        val expire = map["exp"] ?: throw NotFoundException()
        if (expire !is String) throw TypeMismatchException(expire, String::class.java)
        if (LocalDateTime.parse(expire, DateTimeFormatter.ISO_DATE) < now) throw SecurityException("token is expired")

        val notBefore = map["nbf"] ?: throw NotFoundException()
        if (notBefore !is String) throw TypeMismatchException(notBefore, String::class.java)
        if (LocalDateTime.parse(notBefore, DateTimeFormatter.ISO_DATE) > now) throw SecurityException("token is not activated")
    }

    private fun createJWSfromUser(user: User): JWSObject {
        return JWSObject(
            JWSHeader(JWSAlgorithm.HS256),
            Payload(this.userToMap(user))
        ).apply {
            this.sign(jwsSigner) }
    }

    private fun decryptJWStoUser(jwsObject: JWSObject): Email {
        return jwsObject.payload
            .toJSONObject()
//            .also { this.checkValidToken(it) }
            .let { this.mapToEmail(it) }
    }

    private fun encryptJWStoJWE(jwsObject: JWSObject): JWEObject {
        return JWEObject(
            JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128CBC_HS256),
            Payload(jwsObject)
        ).apply {
            this.encrypt(jweEncryptor)
        }
    }

    private fun decryptJWEToJWS(value: String): JWSObject {
        val (first, second, third, forth, fifth) = value.split(".")
        return JWEObject(
            Base64URL.from(first),
            Base64URL.from(second),
            Base64URL.from(third),
            Base64URL.from(forth),
            Base64URL.from(fifth)
        ).apply {
            this.decrypt(jweDecrypter)
        }.payload.toJWSObject()
    }
}