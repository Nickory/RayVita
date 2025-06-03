package com.codelab.basiclayouts.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.ui.screen.MainActivity
import com.codelab.basiclayouts.viewmodel.profile.ReminderType

class RayVitaNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "rayvita_health_reminders"
        const val CHANNEL_NAME = "Health Reminders"
        const val CHANNEL_DESCRIPTION = "Heart rate measurement reminders and health tips"
        const val NOTIFICATION_ID = 1001

        // 权限请求码
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    /**
     * 创建通知渠道（Android 8.0+）
     * 修改为支持高优先级通知
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // 修改为 IMPORTANCE_HIGH 以支持 Heads-Up 通知
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                // 允许在锁屏上显示
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            systemNotificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 检查通知权限
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            notificationManager.areNotificationsEnabled()
        }
    }

    /**
     * 发送测试通知
     * 添加 reminderType 参数以支持不同类型的通知
     */
    fun sendTestNotification(
        includeAiTips: Boolean = true,
        reminderType: ReminderType = ReminderType.NOTIFICATION_BAR
    ): Boolean {
        if (!hasNotificationPermission()) {
            return false
        }

        val baseMessage = context.getString(R.string.notification_preview_message)
        val fullMessage = if (includeAiTips) {
            baseMessage + "\n💡 " + context.getString(R.string.notification_ai_tip_example)
        } else {
            baseMessage
        }

        sendNotification(
            title = "RayVita - " + context.getString(R.string.notification_test_notification),
            message = fullMessage,
            reminderType = reminderType,
            isTest = true
        )

        return true
    }

    /**
     * 发送心率提醒通知
     * 添加 reminderType 参数以支持不同类型的通知
     */
    fun sendHeartRateReminder(
        includeAiTips: Boolean = true,
        reminderType: ReminderType = ReminderType.NOTIFICATION_BAR
    ): Boolean {
        if (!hasNotificationPermission()) {
            return false
        }

        val baseMessage = context.getString(R.string.notification_preview_message)
        val fullMessage = if (includeAiTips) {
            baseMessage + "\n💡 " + context.getString(R.string.notification_ai_tip_example)
        } else {
            baseMessage
        }

        sendNotification(
            title = "RayVita - Heart Rate Reminder",
            message = fullMessage,
            reminderType = reminderType,
            isTest = false
        )

        return true
    }

    /**
     * 发送通知的核心方法
     * 修改为根据 reminderType 设置不同的优先级和行为
     */
    private fun sendNotification(
        title: String,
        message: String,
        reminderType: ReminderType,
        isTest: Boolean = false
    ) {
        // 创建点击通知后的Intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "heart_rate_measurement")
            putExtra("from_notification", true)
            putExtra("is_test", isTest)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 根据 reminderType 设置优先级和行为
        val (priority, shouldVibrate, shouldShowLights) = when (reminderType) {
            ReminderType.POPUP -> Triple(
                NotificationCompat.PRIORITY_HIGH, // 高优先级，触发 Heads-Up 通知
                true,
                true
            )
            ReminderType.SILENT -> Triple(
                NotificationCompat.PRIORITY_LOW, // 低优先级，静默通知
                false,
                false
            )
            ReminderType.NOTIFICATION_BAR -> Triple(
                NotificationCompat.PRIORITY_DEFAULT, // 默认优先级，标准通知栏显示
                true,
                true
            )
        }

        // 构建通知
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // 需要添加这个图标
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority) // 根据类型设置优先级
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 根据类型决定是否启用振动和灯光
        if (shouldVibrate) {
            notificationBuilder.setVibrate(longArrayOf(0, 1000, 500, 1000))
        }

        if (shouldShowLights) {
            notificationBuilder.setLights(android.graphics.Color.BLUE, 3000, 3000)
        }

        // 对于 POPUP 类型，添加额外设置以确保 Heads-Up 显示
        if (reminderType == ReminderType.POPUP) {
            notificationBuilder
                .setCategory(NotificationCompat.CATEGORY_REMINDER) // 设置为提醒类别
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 在锁屏上显示
                .setDefaults(NotificationCompat.DEFAULT_ALL) // 使用所有默认设置
        }

        // 添加操作按钮（除了 SILENT 类型）
        if (reminderType != ReminderType.SILENT) {
            notificationBuilder.addAction(
                R.drawable.ic_heart, // 需要添加这个图标
                context.getString(R.string.notification_measure_now),
                pendingIntent
            )
        }

        val notification = notificationBuilder.build()

        // 发送通知
        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // 处理权限异常
            e.printStackTrace()
        }
    }

    /**
     * 取消所有通知
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    /**
     * 取消特定通知
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}