package com.codelab.basiclayouts.ui.screen.physnet

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.data.language.model.LanguagePreferences
import com.codelab.basiclayouts.data.language.model.LanguageRepository
import com.codelab.basiclayouts.data.physnet.EnhancedRppgProcessor
import com.codelab.basiclayouts.data.physnet.EnhancedRppgRepository
import com.codelab.basiclayouts.data.physnet.VideoRecorder
import com.codelab.basiclayouts.data.theme.model.ThemePreferences
import com.codelab.basiclayouts.data.theme.model.ThemeRepository
import com.codelab.basiclayouts.ui.theme.DynamicRayVitaTheme
import com.codelab.basiclayouts.viewModel.physnet.EnhancedRppgViewModel
import com.codelab.basiclayouts.viewModel.physnet.MeasurementStorageViewModel
import com.codelab.basiclayouts.viewModel.theme.DarkModeOption
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModel
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModelFactory

class PhysnetActivity : ComponentActivity() {

    companion object {
        private const val TAG = "PhysnetActivity"
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    private var hasAllPermissions by mutableStateOf(false)
    private var permissionDenied by mutableStateOf(false)

    private lateinit var themeRepository: ThemeRepository
    private lateinit var themePreferences: ThemePreferences
    private lateinit var languageRepository: LanguageRepository
    private lateinit var languagePreferences: LanguagePreferences

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(themeRepository, this@PhysnetActivity)
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            // 在Activity启动时应用语言设置
            val languagePrefs = LanguagePreferences(newBase)
            val currentLanguage = languagePrefs.getCurrentLanguage()
            LanguageRepository.updateAppLanguage(newBase, currentLanguage)
            super.attachBaseContext(newBase)
        } else {
            super.attachBaseContext(newBase)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        hasAllPermissions = cameraGranted && audioGranted
        permissionDenied = !hasAllPermissions

        if (hasAllPermissions) {
            Log.d(TAG, "所有权限已获取")
        } else {
            Log.w(TAG, "权限被拒绝: camera=$cameraGranted, audio=$audioGranted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 初始化语言相关组件
        languagePreferences = LanguagePreferences(this)
        languageRepository = LanguageRepository(this, languagePreferences)

        // 应用保存的语言设置
        val currentLanguage = languagePreferences.getCurrentLanguage()
        LanguageRepository.updateAppLanguage(this, currentLanguage)

        // 初始化主题相关组件
        themePreferences = ThemePreferences(this)
        themeRepository = ThemeRepository(this, themePreferences)

        splashScreen.setKeepOnScreenCondition {
            !hasAllPermissions && !permissionDenied
        }

        checkAndRequestPermissions()

        setContent {
            DynamicThemeWrapper(
                themeRepository = themeRepository,
                themeViewModel = themeViewModel
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
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

        // 根据用户的深色模式选择确定是否使用深色主题
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
            // 如果主题还在加载中，使用默认主题
            content()
        }
    }

    @Composable
    private fun AppContent() {
        val context = LocalContext.current
        val viewModel: EnhancedRppgViewModel = viewModel(
            factory = RppgViewModelFactory(
                context = context,
                videoRecorder = VideoRecorder(context),
                rppgProcessor = EnhancedRppgProcessor(context),
                repository = EnhancedRppgRepository(context)
            )
        )

        when {
            hasAllPermissions -> {
                RppgScreen(
                    viewModel = viewModel,
                    onBackClick = {
                        // 处理返回逻辑
                        Log.d(TAG, "返回按钮被点击")
                        finish()
                    },
                    onHistoryClick = {
                        // 跳转到 Insight History
                        Log.d(TAG, "历史记录按钮被点击")
                        navigateToInsightHistory()
                    }
                )
            }
            permissionDenied -> {
                PermissionDeniedScreen(
                    onRetryClick = {
                        permissionDenied = false
                        checkAndRequestPermissions()
                    }
                )
            }
            else -> {
                LoadingScreen()
            }
        }
    }

    /**
     * 导航到 Insight History 页面
     */
    private fun navigateToInsightHistory() {
        try {
            // 专门的 InsightActivity
            // val intent = Intent(this, InsightActivity::class.java)
            // intent.putExtra("selected_tab", 1) // 直接跳转到 History tab
            // intent.putExtra("show_history", true)
            // startActivity(intent)

            // MainActivity 中的 Tab 切换
            val intent = Intent()
            intent.setClassName(this, "com.codelab.basiclayouts.ui.screen.MainActivity")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.putExtra("navigate_to_insight", true)
            intent.putExtra("insight_tab", "insight") //
            startActivity(intent)

            //添加过渡动画
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)

        } catch (e: Exception) {
            Log.e(TAG, "导航到 Insight History 失败", e)
            // 如果导航失败，可以显示一个 Toast 或者回退方案
            // Toast.makeText(this, "无法打开历史记录页面", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    private fun PermissionDeniedScreen(onRetryClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = stringResource(R.string.physnet_camera_icon_desc),
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.physnet_permission_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.physnet_permission_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PermissionItem(
                        icon = Icons.Default.CameraAlt,
                        title = stringResource(R.string.physnet_camera_permission_title),
                        description = stringResource(R.string.physnet_camera_permission_desc)
                    )
                    PermissionItem(
                        icon = Icons.Default.Mic,
                        title = stringResource(R.string.physnet_mic_permission_title),
                        description = stringResource(R.string.physnet_mic_permission_desc)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            FilledTonalButton(
                onClick = onRetryClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.physnet_retry_icon_desc),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.physnet_retry_permission))
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    // TODO: 打开应用设置页面
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = android.net.Uri.fromParts("package", packageName, null)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "无法打开应用设置", e)
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.physnet_open_settings),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    @Composable
    private fun PermissionItem(
        icon: ImageVector,
        title: String,
        description: String
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }

    @Composable
    private fun LoadingScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.physnet_initializing),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        REQUIRED_PERMISSIONS.forEach { permission ->
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            Log.d(TAG, "请求权限: ${permissionsToRequest.joinToString()}")
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            hasAllPermissions = true
            Log.d(TAG, "所有权限已获取")
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMissingPermissions(): List<String> {
        return REQUIRED_PERMISSIONS.filter { !hasPermission(it) }
    }

    override fun onResume() {
        super.onResume()
        if (permissionDenied && getMissingPermissions().isEmpty()) {
            hasAllPermissions = true
            permissionDenied = false
            Log.d(TAG, "权限状态已更新")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Activity 销毁")
    }

    override fun onBackPressed() {
        // 处理系统返回键
        Log.d(TAG, "系统返回键被按下")
        super.onBackPressed()
    }
}

class RppgViewModelFactory(
    private val context: Context,
    private val videoRecorder: VideoRecorder,
    private val rppgProcessor: EnhancedRppgProcessor,
    private val repository: EnhancedRppgRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EnhancedRppgViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EnhancedRppgViewModel(
                context = context,
                videoRecorder = videoRecorder,
                rppgProcessor = rppgProcessor,
                repository = repository,
                storageViewModel = MeasurementStorageViewModel(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

object PermissionManager {
    fun hasAllPermissions(activity: ComponentActivity): Boolean {
        return PhysnetActivity.REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getPermissionStatus(activity: ComponentActivity): Map<String, Boolean> {
        return PhysnetActivity.REQUIRED_PERMISSIONS.associateWith { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isPermissionPermanentlyDenied(activity: ComponentActivity, permission: String): Boolean {
        return !activity.shouldShowRequestPermissionRationale(permission) &&
                ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
    }
}