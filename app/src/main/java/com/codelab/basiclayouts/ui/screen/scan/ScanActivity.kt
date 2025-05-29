package com.codelab.basiclayouts.ui.scan

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.codelab.basiclayouts.ui.theme.RayVitaTheme

class ScanActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RayVitaTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ScanScreen() // 调用之前定义的 ScanScreen Composable
                }
            }
        }
    }
}