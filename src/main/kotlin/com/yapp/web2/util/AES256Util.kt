package com.yapp.web2.util

import org.springframework.beans.factory.annotation.Value
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES256Util {
    companion object {
        private const val alg = "AES/CBC/PKCS5Padding"
        @Value("\${aes.secretKey}")
        private const val key = "비미리에여비미리에여비미리에여비미리에여"
        private val iv = key.substring(0, 16)

        fun encrypt(text: String): String {
            val cipher = Cipher.getInstance(alg)
            val keySpec = SecretKeySpec(key.encodeToByteArray(), "AES")
            val ivSpec = IvParameterSpec(iv.encodeToByteArray())
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

            val encrypted = cipher.doFinal(text.toByteArray(StandardCharsets.UTF_8))
            return Base64.getEncoder().encodeToString(encrypted)
        }

        fun decrypt(cipherText: String): String {
            val cipher = Cipher.getInstance(alg)
            val keySpec = SecretKeySpec(key.encodeToByteArray(), "AES")
            val ivSpec = IvParameterSpec(iv.encodeToByteArray())
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

            val decodedBytes = Base64.getDecoder().decode(cipherText)
            val decrypted = cipher.doFinal(decodedBytes)

            return String(decrypted, StandardCharsets.UTF_8)
        }
    }
}