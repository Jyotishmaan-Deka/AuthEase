package com.deadlyord.authease.utils

import android.net.Uri
import com.deadlyord.authease.db.AccountEntity

object QRCodeParser {

    fun parseOtpAuthUri(uri: String): AccountEntity? {
        return try {
            val parsedUri = Uri.parse(uri)

            if (parsedUri.scheme != "otpauth") return null

            val type = parsedUri.host // "totp" or "hotp"
            val path = parsedUri.path?.removePrefix("/")

            if (path.isNullOrBlank()) return null

            val parts = path.split(":")
            val issuer = if (parts.size > 1) parts[0] else ""
            val accountName = if (parts.size > 1) parts[1] else parts[0]

            val secret = parsedUri.getQueryParameter("secret") ?: return null
            val algorithm = parsedUri.getQueryParameter("algorithm") ?: "SHA1"
            val digits = parsedUri.getQueryParameter("digits")?.toIntOrNull() ?: 6
            val period = parsedUri.getQueryParameter("period")?.toIntOrNull() ?: 30
            val counter = parsedUri.getQueryParameter("counter")?.toIntOrNull() ?: 0

            AccountEntity(
                issuer = issuer,
                accountName = accountName,
                secretKey = secret,
                algorithm = algorithm,
                digits = digits,
                period = period,
                counter = counter
            )
        } catch (e: Exception) {
            null
        }
    }
}
