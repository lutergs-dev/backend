package dev.lutergs.lutergsbackend.infra.impl.push

import dev.lutergs.lutergsbackend.domain.push.PushMessageRequest
import dev.lutergs.lutergsbackend.domain.push.subscriber.Subscriber
import dev.lutergs.lutergsbackend.domain.push.topic.Topic
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
        fun fromPushSubscription(subscriber: Subscriber): PushSubscriptionEntity {
            return PushSubscriptionEntity(
                subscriber.auth,
                subscriber.key,
                subscriber.endpoint
            )
        }
    }
}

data class SendPushMessage (
    val topic: String,
    val title: String,
    val body: String,
    val icon: String?       //url
) {
    companion object {
        fun fromTopicAndPushMessage(topic: Topic, pushMessageRequest: PushMessageRequest): SendPushMessage {
            return SendPushMessage(
                topic = topic.name,
                title = pushMessageRequest.title,
                body = pushMessageRequest.body,
                icon = pushMessageRequest.iconUrl
            )
        }

        fun fromTopicNameAndPushMessage(topicName: String, pushMessageRequest: PushMessageRequest): SendPushMessage {
            return SendPushMessage(
                topic = topicName,
                title = pushMessageRequest.title,
                body = pushMessageRequest.body,
                icon = pushMessageRequest.iconUrl
            )
        }
    }
}