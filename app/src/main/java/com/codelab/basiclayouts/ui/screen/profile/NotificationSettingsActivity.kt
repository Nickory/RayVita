package com.codelab.basiclayouts.ui.screen.profile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.codelab.basiclayouts.data.language.model.LanguagePreferences
import com.codelab.basiclayouts.data.language.model.LanguageRepository
import com.codelab.basiclayouts.data.theme.model.ThemePreferences
import com.codelab.basiclayouts.data.theme.model.ThemeRepository
import com.codelab.basiclayouts.ui.theme.DynamicRayVitaTheme
import com.codelab.basiclayouts.viewModel.theme.DarkModeOption
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModel
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModelFactory
import com.codelab.basiclayouts.viewmodel.profile.NotificationSettingsViewModel
import com.codelab.basiclayouts.viewmodel.profile.NotificationSettingsViewModelFactory

class NotificationSettingsActivity : ComponentActivity() {

    private lateinit var themeRepository: ThemeRepository
    private lateinit var themePreferences: ThemePreferences
    private lateinit var languageRepository: LanguageRepository
    private lateinit var languagePreferences: LanguagePreferences

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(themeRepository, this@NotificationSettingsActivity)
    }

    private val notificationViewModel: NotificationSettingsViewModel by viewModels {
        NotificationSettingsViewModelFactory(this@NotificationSettingsActivity)
    }

    // 权限请求启动器
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // 权限结果处理
        if (isGranted) {
            // 权限被授予，刷新权限状态
            notificationViewModel.refreshPermissionStatus()
        } else {
            // 权限被拒绝，可以显示解释或引导用户到设置页面
            // 这里可以添加一个对话框解释为什么需要这个权限
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, NotificationSettingsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val languagePrefs = LanguagePreferences(newBase)
            val currentLanguage = languagePrefs.getCurrentLanguage()
            LanguageRepository.updateAppLanguage(newBase, currentLanguage)
            super.attachBaseContext(newBase)
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化语言设置
        languagePreferences = LanguagePreferences(this)
        languageRepository = LanguageRepository(this, languagePreferences)

        val currentLanguage = languagePreferences.getCurrentLanguage()
        LanguageRepository.updateAppLanguage(this, currentLanguage)

        // 初始化主题设置
        themePreferences = ThemePreferences(this)
        themeRepository = ThemeRepository(this, themePreferences)

        setContent {
            DynamicThemeWrapper(
                themeRepository = themeRepository,
                themeViewModel = themeViewModel
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotificationSettingsScreen(
                        viewModel = notificationViewModel,
                        onBackClick = { finish() },
                        onRequestPermission = { requestNotificationPermission() },
                        onOpenSystemSettings = { openSystemNotificationSettings() }
                    )
                }
            }
        }
    }

    /**
     * 请求通知权限
     */
    private fun requestNotificationPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ 需要请求 POST_NOTIFICATIONS 权限
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                        // 权限已经被授予
                        notificationViewModel.refreshPermissionStatus()
                    }

                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) -> {
                        // 显示权限说明，然后请求权限
                        // 这里可以显示一个对话框解释为什么需要这个权限
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    else -> {
                        // 直接请求权限
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
            else -> {
                // Android 12 及以下版本，通知权限在安装时自动授予
                // 但用户可能在系统设置中关闭了通知
                openSystemNotificationSettings()
            }
        }
    }

    /**
     * 打开系统通知设置页面
     * 增强版本，支持更多的设置选项和错误处理
     */
    private fun openSystemNotificationSettings() {
        try {
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        // Android 8.0+ 优先打开应用的通知设置页面
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        // 备用方案：如果无法打开，尝试打开通知渠道设置
                        putExtra(Settings.EXTRA_CHANNEL_ID, "rayvita_health_reminders")
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                        // Android 5.0+ 打开应用详情页面
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", packageName, null)
                    }
                    else -> {
                        // Android 4.4 及以下版本打开应用管理页面
                        action = Settings.ACTION_APPLICATION_SETTINGS
                    }
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            // 如果无法打开应用特定的设置页面，尝试打开系统通知设置
            try {
                val notificationIntent = Intent().apply {
                    action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                    } else {
                        Settings.ACTION_SOUND_SETTINGS
                    }
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(notificationIntent)
            } catch (e2: Exception) {
                // 最后的备用方案：打开主设置页面
                try {
                    val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                    settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(settingsIntent)
                } catch (e3: Exception) {
                    // 如果连主设置都无法打开，记录错误
                    e3.printStackTrace()
                }
            }
        }
    }

    /**
     * 打开通知渠道设置（Android 8.0+）
     * 这是一个额外的方法，可以用于精确控制通知渠道设置
     */
    private fun openNotificationChannelSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    putExtra(Settings.EXTRA_CHANNEL_ID, "rayvita_health_reminders")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            } catch (e: Exception) {
                // 如果无法打开渠道设置，回退到应用通知设置
                openSystemNotificationSettings()
            }
        } else {
            // 在较旧版本的Android上，回退到应用通知设置
            openSystemNotificationSettings()
        }
    }

    override fun onResume() {
        super.onResume()
        // 当用户从设置页面返回时，刷新权限状态
        notificationViewModel.refreshPermissionStatus()
    }

    @Composable
    private fun DynamicThemeWrapper(
        themeRepository: ThemeRepository,
        themeViewModel: ThemeViewModel,
        content: @Composable () -> Unit
    ) {
        val currentThemeFlow = themeRepository.getCurrentTheme()
        val currentTheme by currentThemeFlow.collectAsState(initial = null)
        val uiState by themeViewModel.uiState.collectAsState()

        val isSystemInDarkTheme = isSystemInDarkTheme()
        val shouldUseDarkTheme = when (uiState.darkModeOption) {
            DarkModeOption.FOLLOW_SYSTEM -> isSystemInDarkTheme
            DarkModeOption.LIGHT -> false
            DarkModeOption.DARK -> true
        }

        currentTheme?.let { theme ->
            DynamicRayVitaTheme(
                themeProfile = theme,
                darkTheme = shouldUseDarkTheme,
                content = content
            )
        } ?: run {
            content()
        }
    }
}