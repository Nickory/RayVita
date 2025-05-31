package com.codelab.basiclayouts.ui.screen.themeChange

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.codelab.basiclayouts.data.theme.model.ThemePreferences
import com.codelab.basiclayouts.data.theme.model.ThemeRepository
import com.codelab.basiclayouts.ui.theme.DynamicRayVitaTheme
import com.codelab.basiclayouts.viewModel.theme.DarkModeOption
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModel
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModelFactory

class ThemeSelectorActivity : ComponentActivity() {

    private lateinit var themeRepository: ThemeRepository
    private lateinit var themePreferences: ThemePreferences

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(themeRepository, this@ThemeSelectorActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化数据层
        themePreferences = ThemePreferences(this)
        themeRepository = ThemeRepository(this, themePreferences)

        // 设置系统窗口
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // 动态应用主题
            DynamicThemeWrapper(
                themeRepository = themeRepository,
                themeViewModel = themeViewModel
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ThemeSelectorScreen(
                        viewModel = themeViewModel,
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ThemeSelectorActivity::class.java)
            context.startActivity(intent)
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