package com.wulinpeng.ezreader.crypto

import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


var aesSecretKey = "" // 16 bytes secret key for AES-128

object AESManager {

    fun encrypt(content: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(aesSecretKey.toByteArray(), "AES")
        val ivSpec = IvParameterSpec(aesSecretKey.toByteArray())
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted = cipher.doFinal(content.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun decrypt(content: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(aesSecretKey.toByteArray(), "AES")
        val ivSpec = IvParameterSpec(aesSecretKey.toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        // url 传递参数会把+号变成空格，所以这里要替换回来
        val realContent = content.replace(" ", "+")
        val decrypted = cipher.doFinal(Base64.getDecoder().decode(realContent))
        return String(decrypted)
    }
}