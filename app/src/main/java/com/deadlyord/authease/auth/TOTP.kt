package com.deadlyord.authease.auth

data class TOTP(
    val issuer: String,
    val accountName: String,
    val secret: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30
) {
    fun generateCurrentCode(): String {
        return OTPGenerator.generateTOTP(secret, period.toLong(), digits, algorithm)
    }

    fun getRemainingTime(): Long {
        return OTPGenerator.getRemainingTime(period.toLong())
    }
}