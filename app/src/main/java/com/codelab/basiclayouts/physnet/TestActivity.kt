//package com.codelab.basiclayouts.physnet
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.core.content.ContextCompat
//import com.codelab.basiclayouts.ui.theme.MySootheTheme
//
//class TestActivity : ComponentActivity() {
//
//    private val requestPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
//        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
//
//        if (cameraGranted && audioGranted) {
//            // 权限已获取，可以开始录制
//        } else {
//            // 处理权限被拒绝的情况
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // 检查并请求权限
//        checkAndRequestPermissions()
//
//        setContent {
//            MySootheTheme {
//                RppgScreen()
//            }
//        }
//    }
//
//    private fun checkAndRequestPermissions() {
//        val permissionsToRequest = mutableListOf<String>()
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//            != PackageManager.PERMISSION_GRANTED) {
//            permissionsToRequest.add(Manifest.permission.CAMERA)
//        }
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//            != PackageManager.PERMISSION_GRANTED) {
//            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
//        }
//
//        if (permissionsToRequest.isNotEmpty()) {
//            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
//        }
//    }
//}