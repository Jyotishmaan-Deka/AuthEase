package com.deadlyord.authease.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CryptoHelper(private val context: Context) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "authenticator_app_key"

    private fun getOrCreateKey(): SecretKey {
        if(!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC) // There are different types, check once
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationValidityDurationSeconds(30)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }

        return keyStore.getKey(keyAlias, null) as SecretKey
    }

    fun encrypt(data: String): String{
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        return Base64.encodeToString(iv, Base64.DEFAULT) + ":" +
                Base64.encodeToString(encryptedBytes,Base64.DEFAULT)
    }

    fun decrypt(encryptedData: String): String{
        val parts = encryptedData.split(":")
        val iv = Base64.decode(parts[0], Base64.DEFAULT)
        val encryptedBytes = Base64.decode(parts[1], Base64.DEFAULT)

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), IvParameterSpec(iv))
        return String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
    }
}