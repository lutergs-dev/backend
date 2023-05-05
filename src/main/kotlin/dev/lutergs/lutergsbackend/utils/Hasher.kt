package dev.lutergs.lutergsbackend.utils

import jakarta.xml.bind.DatatypeConverter
import java.security.MessageDigest

object Hasher {

    private val messageDigest: MessageDigest = MessageDigest.getInstance("MD5")

    fun hashStringToMd5(target: String): String {
        messageDigest.reset()
        messageDigest.update(target.toByteArray())
        return messageDigest.digest()
            .let { DatatypeConverter.printHexBinary(it).uppercase() }
    }
}