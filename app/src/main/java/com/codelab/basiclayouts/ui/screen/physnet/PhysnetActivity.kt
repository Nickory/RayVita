package com.codelab.basiclayouts.ui.screen.physnet

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelab.basiclayouts.data.physnet.EnhancedRppgProcessor
import com.codelab.basiclayouts.data.physnet.EnhancedRppgRepository
import com.codelab.basiclayouts.data.physnet.VideoRecorder
import com.codelab.basiclayouts.ui.theme.MySootheTheme
import com.codelab.basiclayouts.viewModel.physnet.EnhancedRppgViewModel

/**
 * 主 Activity - 升级版
 * 支持 Material 3 设计、权限管理和应用生命周期
 */
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

        splashScreen.setKeepOnScreenCondition {
            !hasAllPermissions && !permissionDenied
        }

        checkAndRequestPermissions()

        setContent {
            MySootheTheme {
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
                RppgScreen(viewModel = viewModel)
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
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "需要权限",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "rPPG 心率监测需要以下权限才能正常工作：",
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
                        title = "相机权限",
                        description = "用于拍摄面部视频进行心率检测"
                    )
                    PermissionItem(
                        icon = Icons.Default.Mic,
                        title = "麦克风权限",
                        description = "用于音频同步和增强检测精度"
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
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("重新请求权限")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    // TODO: 打开应用设置页面
                }
            ) {
                Text(
                    text = "在设置中手动开启权限",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    @Composable
    private fun PermissionItem(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                    text = "初始化中...",
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
}

/**
 * ViewModel 工厂，用于手动创建 RppgViewModel
 */
class RppgViewModelFactory(
    private val context: Context,
    private val videoRecorder: VideoRecorder,
    private val rppgProcessor: EnhancedRppgProcessor,
    private val repository: EnhancedRppgRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EnhancedRppgViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EnhancedRppgViewModel(context, videoRecorder, rppgProcessor, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * 权限管理器 - 可选的独立组件
 */
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