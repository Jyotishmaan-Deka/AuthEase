package com.deadlyord.authease.auth

import android.content.Context
import com.deadlyord.authease.db.AccountEntity

class SecureOTPHelper(context: Context) {
    private val cryptoHelper = CryptoHelper(context)

    fun generateSecureOTP(account: AccountEntity): String {
        return try {
            // Try to decrypt the secret key first
            val secretKey = try {
                cryptoHelper.decrypt(account.secretKey)
            } catch (e: Exception) {
                // If decryption fails, assume it's already plain text (for backward compatibility)
                account.secretKey
            }

            // Generate OTP based on account type
            when {
                account.period > 0 -> {
                    // TOTP
                    OTPGenerator.generateTOTP(
                        secret = secretKey,
                        timeStep = account.period.toLong(),
                        digits = account.digits,
                        algorithm = account.algorithm
                    )
                }
                else -> {
                    // HOTP
                    OTPGenerator.generateHOTP(
                        secret = secretKey,
                        counter = account.counter.toLong(),
                        digits = account.digits,
                        algorithm = account.algorithm
                    )
                }
            }
        } catch (e: Exception) {
            "ERROR"
        }
    }

    fun getRemainingTime(account: AccountEntity): Long {
        return if (account.period > 0) {
            OTPGenerator.getRemainingTime(account.period.toLong())
        } else {
            0L // HOTP doesn't have time-based expiration
        }
    }
}