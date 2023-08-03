package dev.lutergs.lutergsbackend.repository.pushRepositoryImpl

import dev.lutergs.lutergsbackend.service.pushnotification.Subscription
import java.security.KeyFactory
import java.util.Base64

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECPublicKeySpec
import java.security.PublicKey


data class PushSubscriptionEntity(
    val auth: String,
    val key: String,
    val endpoint: String
) {

    private fun getKeyAsBytes(): ByteArray {
        return Base64.getDecoder().decode(this.key)
    }
    fun getUserPublicKey(): PublicKey {
        val ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1")
        val point = ecSpec.curve.decodePoint(this.getKeyAsBytes())
        return ECPublicKeySpec(point, ecSpec)
            .let {
                KeyFactory
                    .getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME)
                    .generatePublic(it)
            }
    }

    fun getAuthAsBytes(): ByteArray {
        return Base64.getDecoder().decode(this.auth)
    }

    companion object {
        fun fromPushSubscription(subscription: Subscription): PushSubscriptionEntity {
            return PushSubscriptionEntity(
                subscription.auth,
                subscription.key,
                subscription.endpoint
            )
        }
    }
}