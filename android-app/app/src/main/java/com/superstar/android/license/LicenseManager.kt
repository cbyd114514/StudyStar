package com.superstar.android.license

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * 许可证管理器
 * 启动时检查远程状态，超时或离线时快速放行，确保 APP 始终能正常打开使用
 */
object LicenseManager {

    private const val STATUS_URL = "https://raw.githubusercontent.com/menghuanshiguang/superstar-status/main/status.txt"
    private const val CONNECT_TIMEOUT = 3000
    private const val READ_TIMEOUT = 3000

    data class LicenseResult(
        val allowed: Boolean,
        val message: String
    )

    suspend fun checkLicense(): LicenseResult = withContext(Dispatchers.IO) {
        try {
            val url = URL(STATUS_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                requestMethod = "GET"
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36"
                )
                instanceFollowRedirects = true
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                // 网络异常或非 200 时默认允许，保证用户体验
                return@withContext LicenseResult(
                    allowed = true,
                    message = "验证就绪"
                )
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
            val content = reader.readText().trim()
            reader.close()
            connection.disconnect()

            when (content) {
                "0" -> LicenseResult(
                    allowed = false,
                    message = "该版本已停用，请更新至最新版本或联系开发者"
                )
                else -> LicenseResult(
                    allowed = true,
                    message = "许可证验证通过"
                )
            }
        } catch (_: Throwable) {
            // 离线/异常时默认允许
            LicenseResult(
                allowed = true,
                message = "离线模式启动"
            )
        }
    }
}
