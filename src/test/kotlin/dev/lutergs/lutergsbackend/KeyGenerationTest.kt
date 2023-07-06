package dev.lutergs.lutergsbackend

import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.util.Base64URL
import org.junit.jupiter.api.Test
import java.security.PrivateKey

class KeyGenerationTest {

    @Test
    fun `랜덤 키 생성 테스트`() {

        for(i: Int in 1..10) {
            JWK.parseFromPEMEncodedObjects("-----BEGIN RSA PRIVATE KEY-----\n" +
                    "MIIEowIBAAKCAQEAy1MuEc6J5auRt7G7Uj5cYgWgyyILKfcZBwVSKQswWVJNZNqV\n" +
                    "DIQbtxSgK+V4Gy3xQKfdG9ZoloV9X5bVKYndJXqe/yTNh2U6sWHKbmJA2ipC+PVI\n" +
                    "QZlsvatLYuNrhJF9udWuJSh4f75C+H0tVr20/E80iovanr+ts1mvmJCgHfKNwazJ\n" +
                    "mgI8e4yKHEHV5+sc8VjGPopD1eET66gUj6zRXq0tLzfHOEWeLDANYN0Lea8HAWuD\n" +
                    "K4dDdLF8cPqHpsbBV1m0lVy0kyFEX7fmrtdlKe9ZtoPeZU7WDGo9d/LJGFwQpY/q\n" +
                    "lQNdXkaP5ii62jvCg0q3h4E5mED8Oz+9gR4oWwIDAQABAoIBAFk5idx/le0HIt2J\n" +
                    "LapUOmc7kAnESUDjsgRuNdVUFyYDY3owH7tL1/u6HeXVf6Txvs6AO5wFNYVkVBXY\n" +
                    "E3f0i2rwighuRpE1f5Gq19Ij5NC4PgaRYOGEqf77xgvBwAN/czxTRGYDAgoQ3MO9\n" +
                    "7uEDRAJ64ZQw9kG23Mt5PoEFanZpG0yL6ik3EQ+d9Ne0JBUrp8j3UjkBbFkjQ2/l\n" +
                    "JiuWyrfKYNawnCsS8V5hWalwDvQTXe6qACt6tdkxRgnysJmm7Qqiyg9YTPAEjQOT\n" +
                    "LedSKtsiaBwTMu+Vw7pWAemnVuNmoJKjir3o8JNTe23S/hhe4eKBTfSDX6reGoaj\n" +
                    "xQ1j0AUCgYEA9Gk//qCgxtnb4ivwSKqIQ4SbRzx3K96ShZCAMI8f7BrETLskVasj\n" +
                    "48Q2NS1p9CdDubItb9H8TpuUsEHlkRPSViCQDB4f/NXg7Hxs37NJb7nvKMbBRpW3\n" +
                    "fIDyrVLOGz6KTinDnKwqidHFa8WH5+tneIF66zdT4k2Yh+K8a9zdUl8CgYEA1Pc1\n" +
                    "9540vaa4HaG4I0wDcupDMGooTXH1VixmgcpyrZ4NqSKQ9GQ04CZSWdD1P5mAv/a9\n" +
                    "qA6N0Lj8eXqd//mvh3LfrIhTHZhgmy5uvWq9yfOR8e/gpyiqcelX+EJLCzYL9ROp\n" +
                    "991SjsPrbjhUM4zNfrsp/Mz/pN2AiqDVkhYcw4UCgYEAqJyjhWoTAJvzUYi2b6sV\n" +
                    "7l5hMTfJjv4xXutdRCK5Bw1ANx8z5dX3IH6QVwdJfSJJWqZNKkNXKHO0lBQh54Ah\n" +
                    "L0Gvnmx57NcwIHWGNp1dIDLJhvv3AlbvCnl68Y+amLkAeQPe6wrrHcEVzU+sfpar\n" +
                    "/zp0LikxOYIGpFgLXO410UkCgYATMfEAT5crXOKNcS0aqVhKPEkwDPFzAYl+aeQX\n" +
                    "YLrzYSKUExSDoqCNb4bOp6HjSJ+tLb0sb5tiq7m97Upebw/eIoF8mJD9QuW6NvYu\n" +
                    "RneywyxtZRSXnHe4arLOIpOHACkVvt78A0vUqF4JAjS8rL085fvD2GpWXsyeMh7t\n" +
                    "2uZezQKBgARwwqGenMTeFZAv0XHLcn0w02YNnj2ANtorvPyTPjjJ/98TVMOcycVL\n" +
                    "baxFbjw9qPK03SRLSB4RjdIdkOXMCVkrxEClcvxCW1a+erjVNcFScLixXK61clwC\n" +
                    "VCuPidygG+lRijKS6R751LwIBC82hwz67Ydrf8Sge9DP/NcaNABW\n" +
                    "-----END RSA PRIVATE KEY-----")
                .toRSAKey()
                .also { println(it) }
        }


//        for(i: Int in 1..10) {
//            RSAKey.Builder(
//                Base64URL.from("123455667778"),
//                Base64URL.from("123455667778"),
//            ).keyUse(KeyUse.SIGNATURE)
//                .keyID("123")
//                .build()
//                .also { println(it) }
//        }


//
//        val kp = KeyPairGenerator.getInstance("RSA")
//            .genKeyPair()
//        KeyPair
//
//        Provider
//
////        val generator = RSAKeyGenerator(2048)
////            .keyID("12345")
////            .secureRandom(SecureRandom("1234".toByteArray()))
////
////        for(i: Int in 1..10) {
////            println(generator.generate().toJSONString())
////        }
//
//
//        {"p":"Acesv9gF9aMgEHPszI8VPBJnNEs8jEaoS-XQqQLMGv6IEuLQKqQ3D6IcMvHmo3jeFJfXgkH5nCCMDMUHIxDom6kJHobTNBrq2ajEchFeX58lPk9JuIgkdi4LdawZKRXT-iGRcjP8FbZnT1IHQrMxSNYUt73w5iT-WU_i8KFY921T","kty":"RSA","q":"trs-xise62Oi5ovxffemWSYDvSuyOeuYFZoJfaoy4tLWppfzuCy_nrndT1gZGdK-JRAqXZWB5pY1KE2fuSQx86SCRRgRyAYQxtDYiUEd5MuicIjRjWxuLIWA8cKTHAI0oCQOiKTse1LfLVPPs9rC2ueWh--C6qTQ3-y0D-kUP_k","d":"F4lxnY1CxnUSYpOb294z5rJObVOsg7a48yqyTwiw1-fHoqqJSinyWNkJsv_E9KbDKi7pe88e1wbp1NCkB7ZqE1S_7Z62lq5mM0b8tocKMWFobw9a3E76WSoBnsbJtXvbJFf5iIXnvpb93_7tIjOdS0_BvUxDJvL5KYOpIdz1yzEG4DkCdNIZJUaTleD7ApZiGfbtkvhg2FDbMWZN_p3OGdro9Wm_hj1d-IGPiyI-groPUnTFG_2TJ5VRFCATHZ7b26fP9Di4nDywSYR4h8i2kk9LUImfW9MKmTnj2-MtBemr5_BbTxTIDasoadi-MfJfpx2lHIvC2990mATPkLqjWQ","e":"AQAB","qi":"ASvZxwBxZ4aN5dhAqGzNgaRTr1E_qD1wFtyJGrO97wD-RPeZxRBsDe1FrcNfldAMDX7rLGtBk-L6RuKIcp0Fjho5wd6QX6y1OIXPYxpVdaBZBdDVi9PhL4MfhYc_SyMuZvCh2AJeKDIV9SKcPVmlOtMp69TFXdOtPuwOjAn6nLcN","dp":"eOeYO4xRQcgnNWsWV7ytpMBnerMFEVZkVQUJYyme2_xk7nOHbO0DI-NRjSfCJjbexe8zq6e8SlJ-rOSY5EqjyrUVuXZes-mueP4uufcNbh-mqckIwm7SQKNfxJskcJs_GhsKb0qqxCwjOsOcaglgpxT2L9q0ZgpqziyL5nPBqus","dq":"hqjmUdNlLcJ3Qb7PcC0YgYnqJ3YUGWrs4L5rUBgk99K4LHy-NnODos0FsnsRxF3xkg6fqeeLmwoGknThx-ZMiWgqOBDMycLuihVN_ZfnF7yZMKUjCD1n05dutrrU_kcB0SHBNCGVxCwGG7kvzzLfdcJExFhU-eYLp3kgGLmDGsE","n":"AUVCG1BJ3Lxhv8yAtjVIE-nFfgzoGvT5ULiXp8g9GUSPq33PQPLeXv5f2A6LwG8u-sm6L5hkHSg6j4SglQD3-PgHTSFeBHKZS_WXjFlE_5BZDpKm9KxLm385Enn2eMSSUcpZp3IcPvelVrfYVOrabgHWi8-iUKhQ-dh6IzkkjYsv-byn746vPAa40ysfbMKu6AxX4hNwZZI6YKSeHmBS1MlUMOyK6Ublv03tfpOpokMqrIXeGXW6yyTEG8cZRzaOKExWok1kGy-aRuS1utX4H4Mu8i0463VwE6appVKS7BKECQXCHrUKPplK0HzmFlSLYucpY9Gc7kILeGmrhoIMwrs"}

    }
}