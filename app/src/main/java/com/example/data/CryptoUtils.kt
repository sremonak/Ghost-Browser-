package com.example.data

import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12

    // Master seed key derived for Ghost Browser built-in vault encryption
    private val defaultMasterKey: SecretKey by lazy {
        val keyBytes = "GhostBrowserVaultSecretKey2026!#".substring(0, 32).toByteArray(StandardCharsets.UTF_8)
        SecretKeySpec(keyBytes, "AES")
    }

    data class EncryptedResult(val ivBase64: String, val dataBase64: String)

    fun encrypt(plainText: String): EncryptedResult {
        try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, defaultMasterKey)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            return EncryptedResult(
                ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP),
                dataBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            )
        } catch (e: Exception) {
            // Fallback plain base64 encoding if device cipher fails
            val dummyIv = Base64.encodeToString(ByteArray(IV_LENGTH_BYTE), Base64.NO_WRAP)
            val dummyData = Base64.encodeToString(plainText.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
            return EncryptedResult(dummyIv, dummyData)
        }
    }

    fun decrypt(ivBase64: String, dataBase64: String): String {
        try {
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val data = Base64.decode(dataBase64, Base64.NO_WRAP)
            val cipher = Cipher.getInstance(ALGORITHM)
            val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.DECRYPT_MODE, defaultMasterKey, spec)
            val decryptedBytes = cipher.doFinal(data)
            return String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            return try {
                val data = Base64.decode(dataBase64, Base64.NO_WRAP)
                String(data, StandardCharsets.UTF_8)
            } catch (ex: Exception) {
                "••• Encrypted •••"
            }
        }
    }
}
