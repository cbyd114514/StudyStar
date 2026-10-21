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
     * 发送异常报错通知
     */
    fun notifyError(context: Context, title: String, message: String) {
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

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
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
