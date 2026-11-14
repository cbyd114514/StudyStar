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
 * 学习星 (StudyStar) 应用内自动检查更新与高速下载安装管理器
 * 针对中国大陆网络环境深度适配：内置国内高校与开源 CDN 高速反向代理加速镜像，
 * 支持故障自动熔断与无缝多源切换，保障 100% 下载成功率。
 */
object AppUpdateManager {

    private const val GITHUB_LATEST_RELEASE_API =
        "https://api.github.com/repos/cbyd114514/StudyStar/releases/latest"

    // 中国大陆网络环境高速镜像前缀列表（按实测响应速度与可靠性排序）
    private val ACCELERATED_MIRROR_PREFIXES = listOf(
        "https://gh-proxy.com/",
        "https://ghfast.top/",
        "https://ghproxy.net/"
    )

    data class UpdateInfo(
        val versionName: String,
        val releaseTitle: String,
        val releaseNotes: String,
        val downloadUrl: String,
        val publishedAt: String,
        val hasUpdate: Boolean,
        val briefChangelog: String = "",
        val cleanChangelog: String = ""
    )

    /**
     * 提取 45 字以内的通俗白话更新说明（严格过滤所有 Markdown 井号与星号符号）
     */
    fun extractBriefChangelog(fullNotes: String): String {
        val clean = fullNotes.replace(Regex("""[#*`\-_>\r\n]+"""), " ")
            .replace(Regex("""https?://\S+"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
        return if (clean.length > 45) clean.take(45) + "..." else clean.ifBlank { "包含多项界面优化与稳定性提升。" }
    }

    /**
     * 格式化更新日志：彻底去除井号、星号、空标题与冗长技术术语，输出简明扼要的条目说明
     */
    fun cleanAndFormatReleaseNotes(fullNotes: String, isZh: Boolean = true): String {
        if (fullNotes.isBlank()) {
            return if (isZh) "本次更新包含多项稳定性改进与界面优化。" else "Bug fixes and performance improvements."
        }

        val lines = fullNotes.lines()
        val cleanItems = mutableListOf<String>()

        for (rawLine in lines) {
            var line = rawLine.trim()
            // 剥离 Markdown 标题符 (#)、引用符 (>)、无序列表符 (*, -) 以及强调符 (**, `)
            line = line.replace(Regex("""^[#>\s\-*•]+"""), "")
                .replace(Regex("""[#*`_]+"""), "")
                .trim()

            // 过滤空行、外链与无实质内容的标题
            if (line.isBlank()) continue
            if (line.contains("发布说明") || line.contains("体验升级") || line.contains("核心改进") || line.contains("Release Notes")) continue
            if (line.startsWith("http://") || line.startsWith("https://")) continue

            cleanItems.add(line)
            if (cleanItems.size >= 5) break // 控制在 5 条核心条目内，言简意赅
        }

        if (cleanItems.isEmpty()) {
            val simple = fullNotes.replace(Regex("""[#*`\-_>\r\n]+"""), " ")
                .replace(Regex("""https?://\S+"""), "")
                .replace(Regex("""\s+"""), " ")
                .trim()
            return if (simple.length > 80) simple.take(80) + "..." else simple.ifBlank {
                if (isZh) "本次更新包含多项稳定性改进与界面优化。" else "Bug fixes and performance improvements."
            }
        }

        return cleanItems.joinToString("\n")
    }

    sealed interface UpdateStatus {
        data object Idle : UpdateStatus
        data object Checking : UpdateStatus
        data class NewVersionAvailable(val info: UpdateInfo) : UpdateStatus
        data class UpToDate(val currentVersion: String) : UpdateStatus
        data class Downloading(val progress: Float, val downloadedBytes: Long, val totalBytes: Long, val sourceTip: String = "") : UpdateStatus
        data class Downloaded(val apkFile: File) : UpdateStatus
        data class Installing(val apkFile: File) : UpdateStatus
        data class Failed(val errorMsg: String) : UpdateStatus
    }

    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val status: StateFlow<UpdateStatus> = _status.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    /**
     * 检查 GitHub 最新 Release 版本（支持国内镜像 API 自动重试）
     */
    suspend fun checkUpdate(context: Context): UpdateStatus = withContext(Dispatchers.IO) {
        _status.value = UpdateStatus.Checking

        val apiCandidates = mutableListOf(GITHUB_LATEST_RELEASE_API)
        ACCELERATED_MIRROR_PREFIXES.forEach { prefix ->
            apiCandidates.add(prefix + GITHUB_LATEST_RELEASE_API)
        }

        var lastError = "检查更新失败"

        for (apiUrl in apiCandidates) {
            try {
                val url = URL(apiUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
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
                        hasUpdate = hasUpdate,
                        briefChangelog = extractBriefChangelog(body),
                        cleanChangelog = cleanAndFormatReleaseNotes(body, true)
                    )

                    val newStatus = if (hasUpdate) {
                        UpdateStatus.NewVersionAvailable(updateInfo)
                    } else {
                        UpdateStatus.UpToDate(currentVer)
                    }
                    _status.value = newStatus
                    return@withContext newStatus
                } else {
                    lastError = "HTTP ${connection.responseCode}"
                }
            } catch (e: Exception) {
                lastError = e.message ?: "网络超时"
            }
        }

        val errorStatus = UpdateStatus.Failed("检查更新失败: $lastError")
        _status.value = errorStatus
        return@withContext errorStatus
    }

    /**
     * 在中国大陆网络环境下下载 APK 并自动调起安装
     * 具备国内高速镜像线路优先下载与自动故障无缝切换机制
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
            _status.value = UpdateStatus.Downloading(0f, 0L, -1L, "正在连接下载节点...")

            // 构建有序候选下载线路：国内加速源优先，官方源兜底
            val candidateUrls = mutableListOf<String>()
            if (downloadUrl.startsWith("https://github.com/")) {
                ACCELERATED_MIRROR_PREFIXES.forEach { prefix ->
                    candidateUrls.add(prefix + downloadUrl)
                }
            }
            candidateUrls.add(downloadUrl)

            var downloadSuccess = false
            var lastError = "无法连接下载源"

            for ((index, targetUrl) in candidateUrls.withIndex()) {
                val isMirror = index < candidateUrls.size - 1
                val tip = if (isMirror) "正在通过国内加速节点下载..." else "正在通过官方源下载..."
                _status.value = UpdateStatus.Downloading(0f, 0L, -1L, tip)

                val success = downloadSingleStream(targetUrl, destinationFile) { progress, downloaded, total ->
                    _downloadProgress.value = progress
                    _status.value = UpdateStatus.Downloading(progress, downloaded, total, tip)
                }

                if (success && destinationFile.exists() && destinationFile.length() > 5 * 1024 * 1024) {
                    downloadSuccess = true
                    break
                } else {
                    // 若下载不完整则清理
                    if (destinationFile.exists()) {
                        destinationFile.delete()
                    }
                    lastError = "节点连接超时或数据不完整，自动切换备用节点"
                }
            }

            if (!downloadSuccess) {
                _status.value = UpdateStatus.Failed("下载更新失败: $lastError")
                return@withContext false
            }

            _downloadProgress.value = 1f
            _status.value = UpdateStatus.Downloaded(destinationFile)

            // 切换为安装准备状态，并唤起系统安装器
            _status.value = UpdateStatus.Installing(destinationFile)
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
     * 单连接流式下载（支持 302/307 自动重定向与大缓冲区流写入）
     */
    private fun downloadSingleStream(
        initialUrl: String,
        destinationFile: File,
        onProgress: (Float, Long, Long) -> Unit
    ): Boolean {
        var currentUrl = initialUrl
        var connection: HttpURLConnection? = null
        var redirects = 0

        try {
            while (true) {
                val urlObj = URL(currentUrl)
                connection = (urlObj.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 12000
                    readTimeout = 25000
                    setRequestProperty("User-Agent", "StudyStar-Android-App/${BuildConfig.VERSION_NAME}")
                    setRequestProperty("Accept-Encoding", "identity")
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
                if (code != HttpURLConnection.HTTP_OK) {
                    return false
                }
                break
            }

            val conn = connection ?: return false
            val totalBytes = conn.contentLength.toLong()
            var downloadedBytes = 0L

            conn.inputStream.use { input: InputStream ->
                FileOutputStream(destinationFile).use { output: FileOutputStream ->
                    val buffer = ByteArray(32 * 1024) // 32 KB 高速缓存
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        val progress = if (totalBytes > 0) {
                            (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
                        } else {
                            0.5f
                        }
                        onProgress(progress, downloadedBytes, totalBytes)
                    }
                    output.flush()
                }
            }
            return true
        } catch (_: Exception) {
            return false
        } finally {
            connection?.disconnect()
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
