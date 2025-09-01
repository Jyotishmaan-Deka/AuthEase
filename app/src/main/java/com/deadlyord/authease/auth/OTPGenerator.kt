package com.deadlyord.authease.auth

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

    // Base32 decoding
    private fun decodeBase32(encoded: String): ByteArray {
        val cleanInput = encoded.uppercase().replace("=", "")
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

        val outputLength = (cleanInput.length * 5) / 8
        val output = ByteArray(outputLength)
        var buffer = 0
        var bitsLeft = 0
        var count = 0

        for (char in cleanInput) {
            val value = alphabet.indexOf(char)
            if (value < 0) throw IllegalArgumentException("Invalid Base32 character: $char")

            buffer = (buffer shl 5) or value
            bitsLeft += 5

            if (bitsLeft >= 8) {
                output[count++] = (buffer shr (bitsLeft - 8)).toByte()
                bitsLeft -= 8
            }
        }

        return output.sliceArray(0 until count)
    }

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
        return try {
            val key = decodeBase32(secret)
            val time = System.currentTimeMillis() / 1000L
            val timeSteps = time / timeStep

            val timeBytes = ByteBuffer.allocate(8).putLong(timeSteps).array()
            generateOTP(key, timeBytes, digits, algorithm)
        } catch (e: Exception) {
            "000000" // Return default on error
        }
    }

    fun generateHOTP(
        secret: String,
        counter: Long,
        digits: Int = 6,
        algorithm: String = "SHA1"
    ): String {
        return try {
            val key = decodeBase32(secret)
            val counterBytes = ByteBuffer.allocate(8).putLong(counter).array()
            generateOTP(key, counterBytes, digits, algorithm)
        } catch (e: Exception) {
            "000000" // Return default on error
        }
    }

    fun getRemainingTime(timeStep: Long = 30): Long {
        val time = System.currentTimeMillis() / 1000L
        return timeStep - (time % timeStep)
    }
}