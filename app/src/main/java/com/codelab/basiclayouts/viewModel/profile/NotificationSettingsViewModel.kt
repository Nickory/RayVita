package com.codelab.basiclayouts.viewmodel.profile

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.notification.RayVitaNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationSettingsUiState(
    val isNotificationEnabled: Boolean = true,
    val isTimedReminderEnabled: Boolean = false,
    val reminderFrequency: ReminderFrequency = ReminderFrequency.EVERY_6_HOURS,
    val customReminderHour: Int = 9,
    val customReminderMinute: Int = 0,
    val reminderType: ReminderType = ReminderType.NOTIFICATION_BAR,
    val includeAiHealthTips: Boolean = true,
    val previewText: String = "",
    val isLoading: Boolean = false,
    val showSaveSuccess: Boolean = false,
    val showTestSuccess: Boolean = false,
    val showPermissionError: Boolean = false,
    val hasNotificationPermission: Boolean = true
)

enum class ReminderFrequency(val displayKeyId: Int, val hours: Int) {
    EVERY_3_HOURS(R.string.notification_frequency_3_hours, 3),
    EVERY_6_HOURS(R.string.notification_frequency_6_hours, 6),
    ONCE_DAILY(R.string.notification_frequency_once_daily, 24),
    TWICE_DAILY(R.string.notification_frequency_twice_daily, 12)
}

enum class ReminderType(val displayKeyId: Int) {
    NOTIFICATION_BAR(R.string.notification_type_notification_bar),
    POPUP(R.string.notification_type_popup),
    SILENT(R.string.notification_type_silent)
}

class NotificationSettingsViewModel(
    private val context: Context,
    private val notificationManager: RayVitaNotificationManager
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "notification_settings",
        Context.MODE_PRIVATE
    )

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        updatePreviewText()
        checkNotificationPermission()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val isNotificationEnabled = prefs.getBoolean(PREF_NOTIFICATION_ENABLED, true)
            val isTimedReminderEnabled = prefs.getBoolean(PREF_TIMED_REMINDER_ENABLED, false)
            val reminderFrequencyOrdinal = prefs.getInt(PREF_REMINDER_FREQUENCY, ReminderFrequency.EVERY_6_HOURS.ordinal)
            val reminderFrequency = ReminderFrequency.values().getOrElse(reminderFrequencyOrdinal) { ReminderFrequency.EVERY_6_HOURS }
            val customReminderHour = prefs.getInt(PREF_CUSTOM_REMINDER_HOUR, 9)
            val customReminderMinute = prefs.getInt(PREF_CUSTOM_REMINDER_MINUTE, 0)
            val reminderTypeOrdinal = prefs.getInt(PREF_REMINDER_TYPE, ReminderType.NOTIFICATION_BAR.ordinal)
            val reminderType = ReminderType.values().getOrElse(reminderTypeOrdinal) { ReminderType.NOTIFICATION_BAR }
            val includeAiHealthTips = prefs.getBoolean(PREF_INCLUDE_AI_TIPS, true)

            _uiState.value = _uiState.value.copy(
                isNotificationEnabled = isNotificationEnabled,
                isTimedReminderEnabled = isTimedReminderEnabled,
                reminderFrequency = reminderFrequency,
                customReminderHour = customReminderHour,
                customReminderMinute = customReminderMinute,
                reminderType = reminderType,
                includeAiHealthTips = includeAiHealthTips
            )
            updatePreviewText()
        }
    }

    fun toggleNotificationEnabled() {
        _uiState.value = _uiState.value.copy(
            isNotificationEnabled = !_uiState.value.isNotificationEnabled
        )
    }

    fun toggleTimedReminderEnabled() {
        _uiState.value = _uiState.value.copy(
            isTimedReminderEnabled = !_uiState.value.isTimedReminderEnabled
        )
    }

    fun updateReminderFrequency(frequency: ReminderFrequency) {
        _uiState.value = _uiState.value.copy(
            reminderFrequency = frequency
        )
        updatePreviewText()
    }

    fun updateCustomReminderTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(
            customReminderHour = hour,
            customReminderMinute = minute
        )
        updatePreviewText()
    }

    fun updateReminderType(type: ReminderType) {
        _uiState.value = _uiState.value.copy(
            reminderType = type
        )
        updatePreviewText()
    }

    fun toggleAiHealthTips() {
        _uiState.value = _uiState.value.copy(
            includeAiHealthTips = !_uiState.value.includeAiHealthTips
        )
        updatePreviewText()
    }

    private fun updatePreviewText() {
        val currentState = _uiState.value
        val baseText = context.getString(R.string.notification_preview_message)
        val aiTipText = if (currentState.includeAiHealthTips) {
            "\n💡 " + context.getString(R.string.notification_ai_tip_example)
        } else ""

        // 根据提醒类型添加说明
        val typeHint = when (currentState.reminderType) {
            ReminderType.POPUP -> "\n🔔 " + context.getString(R.string.notification_popup_hint, "顶部弹出提醒")
            ReminderType.SILENT -> "\n🔕 " + context.getString(R.string.notification_silent_hint, "静默通知")
            ReminderType.NOTIFICATION_BAR -> ""
        }

        _uiState.value = _uiState.value.copy(
            previewText = baseText + aiTipText + typeHint
        )
    }

    private fun checkNotificationPermission() {
        val hasPermission = notificationManager.hasNotificationPermission()
        _uiState.value = _uiState.value.copy(hasNotificationPermission = hasPermission)
    }

    /**
     * 测试通知方法
     * 修改为传递当前选择的 reminderType
     */
    fun testNotification() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 检查权限
            if (!notificationManager.hasNotificationPermission()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showPermissionError = true
                )

                // 3秒后隐藏错误消息
                kotlinx.coroutines.delay(3000)
                _uiState.value = _uiState.value.copy(showPermissionError = false)
                return@launch
            }

            // 发送真实的测试通知，传递当前的 reminderType
            val success = notificationManager.sendTestNotification(
                includeAiTips = _uiState.value.includeAiHealthTips,
                reminderType = _uiState.value.reminderType // 传递当前选择的提醒类型
            )

            kotlinx.coroutines.delay(500)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                showTestSuccess = success
            )

            if (success) {
                // 3秒后隐藏成功消息
                kotlinx.coroutines.delay(3000)
                _uiState.value = _uiState.value.copy(showTestSuccess = false)
            }
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val currentState = _uiState.value
            with(prefs.edit()) {
                putBoolean(PREF_NOTIFICATION_ENABLED, currentState.isNotificationEnabled)
                putBoolean(PREF_TIMED_REMINDER_ENABLED, currentState.isTimedReminderEnabled)
                putInt(PREF_REMINDER_FREQUENCY, currentState.reminderFrequency.ordinal)
                putInt(PREF_CUSTOM_REMINDER_HOUR, currentState.customReminderHour)
                putInt(PREF_CUSTOM_REMINDER_MINUTE, currentState.customReminderMinute)
                putInt(PREF_REMINDER_TYPE, currentState.reminderType.ordinal)
                putBoolean(PREF_INCLUDE_AI_TIPS, currentState.includeAiHealthTips)
                apply()
            }

            // 模拟保存延迟
            kotlinx.coroutines.delay(500)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                showSaveSuccess = true
            )

            // 3秒后隐藏成功消息
            kotlinx.coroutines.delay(3000)
            _uiState.value = _uiState.value.copy(showSaveSuccess = false)
        }
    }

    fun restoreDefaults() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 清除所有设置
            prefs.edit().clear().apply()

            // 重置为默认值
            _uiState.value = NotificationSettingsUiState()
            updatePreviewText()

            kotlinx.coroutines.delay(300)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun hideSaveSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSaveSuccess = false)
    }

    fun hideTestSuccessMessage() {
        _uiState.value = _uiState.value.copy(showTestSuccess = false)
    }

    fun hidePermissionErrorMessage() {
        _uiState.value = _uiState.value.copy(showPermissionError = false)
    }

    fun refreshPermissionStatus() {
        checkNotificationPermission()
    }

    fun openSystemNotificationSettings() {
        // 这个方法可以用来打开系统通知设置
        // 在Activity中实现具体逻辑
    }

    companion object {
        private const val PREF_NOTIFICATION_ENABLED = "notification_enabled"
        private const val PREF_TIMED_REMINDER_ENABLED = "timed_reminder_enabled"
        private const val PREF_REMINDER_FREQUENCY = "reminder_frequency"
        private const val PREF_CUSTOM_REMINDER_HOUR = "custom_reminder_hour"
        private const val PREF_CUSTOM_REMINDER_MINUTE = "custom_reminder_minute"
        private const val PREF_REMINDER_TYPE = "reminder_type"
        private const val PREF_INCLUDE_AI_TIPS = "include_ai_tips"
    }
}

class NotificationSettingsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationSettingsViewModel::class.java)) {
            val notificationManager = RayVitaNotificationManager(context)
            @Suppress("UNCHECKED_CAST")
            return NotificationSettingsViewModel(context, notificationManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}