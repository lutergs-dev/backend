package dev.lutergs.lutergsbackend

import jakarta.xml.bind.DatatypeConverter
import org.junit.jupiter.api.Test
import java.security.MessageDigest
import java.util.*

class HashingTest {

    @Test
    fun `Lorem Ipsum MD5 해싱 테스트`() {
        val first: String = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."

        val md: MessageDigest = MessageDigest.getInstance("MD5")
        md.update(first.toByteArray())
        val digest: ByteArray = md.digest()
        val hashed: String = DatatypeConverter.printHexBinary(digest).uppercase(Locale.getDefault())

        println(hashed)

    }
}