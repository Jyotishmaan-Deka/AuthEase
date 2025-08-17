package com.deadlyord.authease.auth

import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object OTPGenerator {
    private const val HMAC_SHA1 = "HmacSHA1"
    private const val HMAC_SHA256 = "HmacSHA256"
    private const val HMAC_SHA512 = "HmacSHA512"

    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    private fun generateOTP(
        key: ByteArray,
        input:ByteArray,
        digits: Int,
        algorithm: String
    ): String {
        val hmacAlgorithm = when(algorithm.uppercase()) {
            "SHA1" -> HMAC_SHA1
            "SHA256" -> HMAC_SHA256
            "SHA512" -> HMAC_SHA512
            else -> HMAC_SHA1
        }

        val mac = Mac.getInstance(hmacAlgorithm)
        mac.init(SecretKeySpec(key, algorithm))
        val hash = mac.doFinal(input)

        val offset = hash[hash.size - 1].toInt() and 0xf
        val binary = ()
    }
}