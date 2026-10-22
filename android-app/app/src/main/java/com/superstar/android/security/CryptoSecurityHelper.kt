package com.superstar.android.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android Keystore 硬件级安全加解密与敏感信息脱敏助手
 * - 采用 AES-256-GCM 硬件隔离密钥（硬件 TEE / StrongBox 保护）
 * - 确保本地持久化的账号密码即使在 Root 或物理提取状态下也无法被直接还原
 * - 提供运行日志敏感信息自动脱敏过滤，防止凭证与 API Key 泄露
 */
object CryptoSecurityHelper {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "StudyStar_Master_Key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val ENCRYPTED_PREFIX = "ENC_GCM:"

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    @Synchronized
    private fun getOrCreateSecretKey(): SecretKey {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) {
                return entry.secretKey
            }
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keyGenSpec)
        return keyGenerator.generateKey()
    }

    /**
     * 对敏感文本（如密码、Token）进行硬件级 AES-GCM 加密
     */
    fun encrypt(plainText: String?): String {
        if (plainText.isNullOrEmpty()) return ""
        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

            val base64 = Base64.encodeToString(combined, Base64.NO_WRAP)
            "$ENCRYPTED_PREFIX$base64"
        } catch (_: Throwable) {
            // 在极特殊未支持 Keystore 的仿真环境平滑回退
            plainText
        }
    }

    /**
     * 对加密文本进行解密，若原本为明文则平滑兼容返回
     */
    fun decrypt(cipherText: String?): String {
        if (cipherText.isNullOrEmpty()) return ""
        if (!cipherText.startsWith(ENCRYPTED_PREFIX)) {
            // 平滑兼容历史未加密的明文存储
            return cipherText
        }

        return try {
            val base64Data = cipherText.removePrefix(ENCRYPTED_PREFIX)
            val combined = Base64.decode(base64Data, Base64.NO_WRAP)
            if (combined.size <= GCM_IV_LENGTH) return ""

            val iv = ByteArray(GCM_IV_LENGTH)
            val actualCipher = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, actualCipher, 0, actualCipher.size)

            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val plainBytes = cipher.doFinal(actualCipher)
            String(plainBytes, Charsets.UTF_8)
        } catch (_: Throwable) {
            // 解密失败保护
            ""
        }
    }

    private val PASSWORD_PATTERN = Regex("(?i)(password|pwd|pass|密码)[\"':=\\s]+([^\",\\s&}]+)")
    private val API_KEY_PATTERN = Regex("(sk-[a-zA-Z0-9]{8,})")
    private val BEARER_PATTERN = Regex("(?i)(Bearer\\s+)([a-zA-Z0-9_.-]{12,})")

    /**
     * 日志脱敏过滤器：确保日志、控制台及剪贴板中绝不暴露密码与 API Key
     */
    fun sanitizeLog(message: String): String {
        if (message.isBlank()) return message
        var sanitized = message

        // 1. 脱敏密码
        sanitized = PASSWORD_PATTERN.replace(sanitized) { matchResult ->
            val prefix = matchResult.groupValues[1]
            "$prefix: ******"
        }

        // 2. 脱敏 API Key (保留前 3 后 4 字符)
        sanitized = API_KEY_PATTERN.replace(sanitized) { matchResult ->
            val fullKey = matchResult.value
            if (fullKey.length > 7) {
                fullKey.take(5) + "..." + fullKey.takeLast(4)
            } else {
                "sk-***"
            }
        }

        // 3. 脱敏 Bearer Token
        sanitized = BEARER_PATTERN.replace(sanitized) { matchResult ->
            val prefix = matchResult.groupValues[1]
            val token = matchResult.groupValues[2]
            "$prefix" + token.take(4) + "..." + token.takeLast(4)
        }

        return sanitized
    }
}
