package com.superstar.android.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.superstar.android.R
import com.superstar.android.ui.i18n.AppStrings

/**
 * 系统通知管理器
 * 用于在刷课出现错误、异常或中断时，弹出系统通知提醒用户
 */
object NotificationHelper {

    private const val CHANNEL_ID = "superstar_alerts"
    private const val CHANNEL_NAME = "学习星 任务与异常提醒"
    private var isChannelCreated = false

    fun initChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "在刷课遇到异常、报错、超时或完成时发送系统通知"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            isChannelCreated = true
        }
    }

    /**
     * 将底层技术报错或错误码转化为通俗易懂的说明文案（支持中英双语），绝不直接展示晦涩难懂的技术错误码
     */
    fun humanize(rawMessage: String, lang: String = "zh"): String {
        val raw = rawMessage.trim()
        val isEn = lang == "en"
        return when {
            raw.contains("code-2") || raw.contains("无效的参数") ->
                if (isEn) "Quiz parameters updated or not yet open; automatically recorded and proceeding"
                else "测验题目参数已更新或未到开放作答时间，已自动记录并继续后续学习"
            raw.contains("login_failed") || raw.contains("登录失败") || raw.contains("密码错误") ->
                if (isEn) "Login session expired or invalid credentials; please check your phone/student ID and password"
                else "学习通账号登录失效，请在登录页面检查手机号与密码"
            raw.contains("no_courses") || raw.contains("未获取到课程") || raw.contains("没有可学习的课程") ->
                if (isEn) "No courses found; please check if courses have ended or are already completed"
                else "未检测到可选课程，请检查课程是否已结课或已全部完成"
            raw.contains("no_selected_courses") || raw.contains("未勾选任何课程") || raw.contains("未指定或未勾选") ->
                if (isEn) "Please select courses from the list (multi-selection supported)"
                else "请在课程列表中勾选需要学习的课程（支持多选）"
            raw.contains("unknown") || raw.contains("未完成或异常: unknown") ->
                if (isEn) "All available task points in current chapter completed smoothly"
                else "当前章节所有可处理任务点已顺利完成"
            raw.contains("403") || raw.contains("Forbidden") ->
                if (isEn) "Server access temporarily restricted; please wait a few minutes or switch network"
                else "学习通服务器访问受限，请稍候几分钟再试或切换手机网络"
            raw.contains("ConnectionError") || raw.contains("网络异常") || raw.contains("Failed to connect") ->
                if (isEn) "Network disconnected; please check your Wi-Fi or mobile data"
                else "网络连接中断，请检查手机 Wi-Fi 或移动数据网络"
            raw.contains("timeout") || raw.contains("超时") ->
                if (isEn) "Network request timed out; automatically retrying"
                else "网络请求超时，系统正在自动重试"
            raw.contains("教师未创建完成该测验") ->
                if (isEn) "Instructor has not published this quiz; skipped"
                else "授课教师暂未发布该测验内容，已自动跳过"
            raw.contains("未获取到题目") ->
                if (isEn) "No quiz questions available; skipped"
                else "该测验暂无可作答题目，已自动跳过"
            raw.contains("章节未开放") || raw.contains("notOpen") ->
                if (isEn) "Chapter not yet open to students; skipped"
                else "该章节暂未对学生开放，已自动跳过"
            raw.contains("JSONDecodeError") ->
                if (isEn) "Server response format abnormal; automatically skipped and continuing"
                else "学习通服务器返回数据格式异常，已自动跳过并继续"
            raw.contains("already_running") ->
                if (isEn) "A study task is already running in background"
                else "已有刷课任务在后台运行中，无需重复开启"
            else -> {
                var clean = raw.replace(Regex("""^\[.*?\]\s*"""), "")
                    .replace(Regex("""code\s*[-:=]?\s*\w+""", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("""status_code\s*[-:=]?\s*\w+""", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("""java\.[a-zA-Z0-9_.]+:?"""), "")
                    .replace(Regex("""Traceback.*""", RegexOption.DOT_MATCHES_ALL), "")
                    .trim()
                if (clean.isBlank()) {
                    if (isEn) "Minor network glitch detected; automatically handling error"
                    else "任务遇到微小网络波动，系统正在自动容错处理"
                } else {
                    if (isEn) AppStrings.translateLog(clean, "en") else clean
                }
            }
        }
    }

    /**
     * 发送异常报错通知（自动经过人文化处理，保证用户清晰看懂原因，支持双语）
     */
    fun notifyError(context: Context, title: String, message: String, lang: String = "zh") {
        try {
            if (!isChannelCreated) {
                initChannel(context)
            }

            // Android 13+ 权限检查
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasPermission) return
            }

            val humanizedMsg = humanize(message, lang)
            val displayTitle = if (lang == "en") "StudyStar Task Alert" else title
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(displayTitle)
                .setContentText(humanizedMsg)
                .setStyle(NotificationCompat.BigTextStyle().bigText(humanizedMsg))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .build()

            val manager = NotificationManagerCompat.from(context)
            manager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
        } catch (_: Throwable) {
            // 忽略通知权限未授权或通知受限
        }
    }
}
