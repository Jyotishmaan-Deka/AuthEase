package com.deadlyord.authease.auth

import android.util.Base64
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object OTPGenerator {
    private const val HMAC_SHA1 = "HmacSHA1"
    private const val HMAC_SHA256 = "HmacSHA256"
    private const val HMAC_SHA512 = "HmacSHA512"

    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    private fun generateOTP(
        key: ByteArray,
        input: ByteArray,
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
        mac.init(SecretKeySpec(key, hmacAlgorithm))
        val hash = mac.doFinal(input)

        val offset = hash[hash.size - 1].toInt() and 0xf
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                ((hash[offset + 1].toInt() and 0xff) shl 16) or
                ((hash[offset + 2].toInt() and 0xff) shl 8) or
                (hash[offset + 3].toInt() and 0xff)

        val otp = binary % 10.0.pow(digits.toDouble()).toInt()
        return String.format("%0${digits}d", otp)
    }

    fun generateTOTP(
        secret: String,
        timeStep: Long = 30,
        digits: Int = 6,
        algorithm: String = "SHA1"
    ): String {
        val key = Base64.decode(secret, Base64.DEFAULT)
        val time = System.currentTimeMillis() / 1000L
        val timeSteps = time / timeStep

        val timeBytes = ByteBuffer.allocate(8).putLong(timeSteps).array()
        return generateOTP(key, timeBytes, digits, algorithm)
    }

    fun generateHOTP(
        secret: String,
        counter: Long,
        digits: Int = 6,
        algorithm: String = "SHA1"
    ): String {
        val key = Base64.decode(secret, Base64.DEFAULT)
        val counterBytes = ByteBuffer.allocate(8).putLong(counter).array()
        return generateOTP(key, counterBytes, digits, algorithm)
    }

    fun getRemainingTime(timeStep: Long = 30): Long {
        val time = System.currentTimeMillis() / 1000L
        return timeStep - (time % timeStep)
    }
}
