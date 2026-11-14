package com.superstar.android.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.superstar.android.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 学习星 (StudyStar) 应用内自动检查更新与静默下载安装管理器
 * 遵循 Material Design 3 Expressive 规范，提供实时下载进度响应与安全 FileProvider 调起安装。
 */
object AppUpdateManager {

    private const val GITHUB_LATEST_RELEASE_API =
        "https://api.github.com/repos/cbyd114514/StudyStar/releases/latest"

    data class UpdateInfo(
        val versionName: String,
        val releaseTitle: String,
        val releaseNotes: String,
        val downloadUrl: String,
        val publishedAt: String,
        val hasUpdate: Boolean
    )

    sealed interface UpdateStatus {
        data object Idle : UpdateStatus
        data object Checking : UpdateStatus
        data class NewVersionAvailable(val info: UpdateInfo) : UpdateStatus
        data class UpToDate(val currentVersion: String) : UpdateStatus
        data class Downloading(val progress: Float, val downloadedBytes: Long, val totalBytes: Long) : UpdateStatus
        data class Downloaded(val apkFile: File) : UpdateStatus
        data class Failed(val errorMsg: String) : UpdateStatus
    }

    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val status: StateFlow<UpdateStatus> = _status.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    /**
     * 检查 GitHub 最新 Release 版本
     */
    suspend fun checkUpdate(context: Context): UpdateStatus = withContext(Dispatchers.IO) {
        _status.value = UpdateStatus.Checking
        try {
            val url = URL(GITHUB_LATEST_RELEASE_API)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "StudyStar-Android-App/${BuildConfig.VERSION_NAME}")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val tagName = json.optString("tag_name", "").trim()
                val latestVer = tagName.removePrefix("v").removePrefix("V").trim()
                val title = json.optString("name", "新版本发布")
                val body = json.optString("body", "包含重要的功能更新与稳定性改进。")
                val publishedAt = json.optString("published_at", "")

                // 从 assets 列表中检索 Release APK 资产直链
                var downloadUrl = ""
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = asset.optString("browser_download_url", "")
                            break
                        }
                    }
                }

                // 如果未在 assets 中解析到直链，使用 release html 链接作为回退
                if (downloadUrl.isBlank()) {
                    downloadUrl = json.optString("html_url", "https://github.com/cbyd114514/StudyStar/releases")
                }

                val currentVer = BuildConfig.VERSION_NAME
                val hasUpdate = isNewerVersion(latestVer, currentVer)

                val updateInfo = UpdateInfo(
                    versionName = latestVer,
                    releaseTitle = title,
                    releaseNotes = body,
                    downloadUrl = downloadUrl,
                    publishedAt = publishedAt,
                    hasUpdate = hasUpdate
                )

                val newStatus = if (hasUpdate) {
                    UpdateStatus.NewVersionAvailable(updateInfo)
                } else {
                    UpdateStatus.UpToDate(currentVer)
                }
                _status.value = newStatus
                return@withContext newStatus
            } else {
                val errorStatus = UpdateStatus.Failed("检查更新失败: HTTP ${connection.responseCode}")
                _status.value = errorStatus
                return@withContext errorStatus
            }
        } catch (e: Exception) {
            val errorStatus = UpdateStatus.Failed("网络连接异常: ${e.message ?: "未知错误"}")
            _status.value = errorStatus
            return@withContext errorStatus
        }
    }

    /**
     * 在应用内下载 APK 并自动调起安装
     */
    suspend fun downloadAndInstallApk(context: Context, downloadUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            val destinationFile = File(downloadDir, "StudyStar_latest.apk")
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            _downloadProgress.value = 0f
            _status.value = UpdateStatus.Downloading(0f, 0L, -1L)

            // 支持 302 重定向跟随（GitHub Releases 下载直链通常重定向到 GitHub Objects 存储服务器）
            var currentUrl = downloadUrl
            var connection: HttpURLConnection
            var redirects = 0
            while (true) {
                val urlObj = URL(currentUrl)
                connection = (urlObj.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 20000
                    readTimeout = 30000
                    setRequestProperty("User-Agent", "StudyStar-Android-App")
                }
                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == 307 || code == 308) {
                    val location = connection.getHeaderField("Location")
                    if (!location.isNullOrBlank() && redirects < 5) {
                        currentUrl = location
                        redirects++
                        continue
                    }
                }
                break
            }

            val totalBytes = connection.contentLength.toLong()
            var downloadedBytes = 0L

            connection.inputStream.use { input: InputStream ->
                FileOutputStream(destinationFile).use { output: FileOutputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        val progress = if (totalBytes > 0) {
                            (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
                        } else {
                            0.5f // 未知总大小时保持中间态
                        }
                        _downloadProgress.value = progress
                        _status.value = UpdateStatus.Downloading(progress, downloadedBytes, totalBytes)
                    }
                    output.flush()
                }
            }

            _downloadProgress.value = 1f
            _status.value = UpdateStatus.Downloaded(destinationFile)

            // 下载完成后自动唤起 Android 系统安装器
            withContext(Dispatchers.Main) {
                installApk(context, destinationFile)
            }
            return@withContext true
        } catch (e: Exception) {
            _status.value = UpdateStatus.Failed("下载更新失败: ${e.message}")
            return@withContext false
        }
    }

    /**
     * 调起系统安装器自动安装 APK
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) return

            val intent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        apkFile
                    )
                } else {
                    Uri.fromFile(apkFile)
                }
                setDataAndType(uri, "application/vnd.android.package-archive")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _status.value = UpdateStatus.Failed("启动安装器失败: ${e.message}")
        }
    }

    fun resetStatus() {
        _status.value = UpdateStatus.Idle
        _downloadProgress.value = 0f
    }

    /**
     * 语义化版本号对比 (Major.Minor.Patch)
     */
    private fun isNewerVersion(latest: String, current: String): Boolean {
        try {
            val latestParts = latest.split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
            val currentParts = current.split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }

            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
        } catch (_: Exception) {
            return latest != current
        }
        return false
    }
}
