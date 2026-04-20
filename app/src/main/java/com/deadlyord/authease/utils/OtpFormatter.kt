package com.deadlyord.authease.utils

/**
 * Fix 5: Centralised OTP formatting — single source of truth.
 * Previously the same formatting logic was duplicated across AccountAdapter
 * and any other place that displayed OTP codes.
 */
object OtpFormatter {

    /**
     * Formats a raw OTP string into a human-readable grouped string.
     *
     * Examples:
     *   "123456" → "123 456"
     *   "12345678" → "1234 5678"
     *   any other length → returned unchanged
     */
    fun format(otp: String): String = when (otp.length) {
        6 -> "${otp.substring(0, 3)} ${otp.substring(3)}"
        8 -> "${otp.substring(0, 4)} ${otp.substring(4)}"
        else -> otp
    }

    /**
     * Strips any spaces from a formatted OTP before copying to clipboard.
     */
    fun stripSpaces(otp: String): String = otp.replace(" ", "")
}
