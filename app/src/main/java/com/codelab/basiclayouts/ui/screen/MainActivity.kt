package com.codelab.basiclayouts.ui.screen

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.codelab.basiclayouts.data.theme.model.ThemePreferences
import com.codelab.basiclayouts.data.theme.model.ThemeRepository
import com.codelab.basiclayouts.ui.theme.DynamicRayVitaTheme
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModel
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var themeRepository: ThemeRepository
    private lateinit var themePreferences: ThemePreferences

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(themeRepository, this@MainActivity)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化数据层
        themePreferences = ThemePreferences(this)
        themeRepository = ThemeRepository(this, themePreferences)

        setContent {
            // 获取当前主题
            val currentTheme by themeRepository.getCurrentTheme().collectAsState(initial = null)

            // 使用 DynamicRayVitaTheme
            currentTheme?.let { themeProfile ->
                DynamicRayVitaTheme(
                    themeProfile = themeProfile,
                    content = {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MainApp()
                        }
                    }
                )
            } ?: run {
                // 主题加载中时的回退内容
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}