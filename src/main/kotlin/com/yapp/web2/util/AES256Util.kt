package com.yapp.web2.util

import org.apache.catalina.util.URLEncoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class AES256Util(
    @Value("\${aes.secretKey}")
    private val key: String
) {

    companion object {
        private const val alg = "AES/CBC/PKCS5Padding"
        // iv 값은 노출시켜도 괜찮다~
        private const val iv = "1234567891234567"

        private const val AES = "AES"
    }

    fun encrypt(text: String): String {
        val cipher = Cipher.getInstance(alg)
        val keySpec = SecretKeySpec(key.substring(0, 16).encodeToByteArray(), AES)
        val ivSpec = IvParameterSpec(iv.encodeToByteArray())

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

        val encrypted = cipher.doFinal(text.toByteArray(StandardCharsets.UTF_8))

        return URLEncoder().encode(Base64.getEncoder().encodeToString(encrypted), StandardCharsets.UTF_8)
    }

    fun decrypt(cipherText: String): String {
        val cipher = Cipher.getInstance(alg)
        val keySpec = SecretKeySpec(key.substring(0, 16).encodeToByteArray(), AES)
        val ivSpec = IvParameterSpec(iv.encodeToByteArray())

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        val aesCipherText = URLDecoder.decode(cipherText, StandardCharsets.UTF_8.toString())
        val decodedBytes = Base64.getDecoder().decode(aesCipherText)
        val decrypted = cipher.doFinal(decodedBytes)

        return String(decrypted, StandardCharsets.UTF_8)
    }
}