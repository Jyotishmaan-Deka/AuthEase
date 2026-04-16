package com.deadlyord.authease.auth

import android.content.Context
import com.deadlyord.authease.db.AccountEntity

class SecureOTPHelper(context: Context) {
    private val cryptoHelper = CryptoHelper(context)

    /**
     * Returns the plaintext secret for an account, decrypting it if necessary.
     * Exposed so callers (e.g. AccountAdapter) can build a TOTP instance with the real secret.
     */
    fun getDecryptedSecret(account: AccountEntity): String {
        return try {
            cryptoHelper.decrypt(account.secretKey)
        } catch (e: Exception) {
            // Backward-compatibility: secret was stored as plain text before encryption was added
            account.secretKey
        }
    }

    fun generateSecureOTP(account: AccountEntity): String {
        return try {
            val totp = TOTP(
                issuer = account.issuer,
                accountName = account.accountName,
                secret = getDecryptedSecret(account),
                algorithm = account.algorithm,
                digits = account.digits,
                period = account.period
            )
            if (account.period > 0) {
                totp.generateCurrentCode()
            } else {
                OTPGenerator.generateHOTP(
                    secret = getDecryptedSecret(account),
                    counter = account.counter.toLong(),
                    digits = account.digits,
                    algorithm = account.algorithm
                )
            }
        } catch (e: Exception) {
            "ERROR"
        }
    }

    fun getRemainingTime(account: AccountEntity): Long {
        return if (account.period > 0) {
            OTPGenerator.getRemainingTime(account.period.toLong())
        } else {
            0L
        }
    }
}
