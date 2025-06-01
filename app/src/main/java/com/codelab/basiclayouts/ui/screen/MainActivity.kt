package com.codelab.basiclayouts.ui.screen

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.codelab.basiclayouts.data.language.model.LanguagePreferences
import com.codelab.basiclayouts.data.language.model.LanguageRepository
import com.codelab.basiclayouts.data.theme.model.ThemePreferences
import com.codelab.basiclayouts.data.theme.model.ThemeRepository
import com.codelab.basiclayouts.ui.theme.DynamicRayVitaTheme
import com.codelab.basiclayouts.utils.StepCounterPermissionHelper
import com.codelab.basiclayouts.viewModel.language.LanguageViewModel
import com.codelab.basiclayouts.viewModel.language.LanguageViewModelFactory
import com.codelab.basiclayouts.viewModel.theme.DarkModeOption
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModel
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var themeRepository: ThemeRepository
    private lateinit var themePreferences: ThemePreferences
    private lateinit var languageRepository: LanguageRepository
    private lateinit var languagePreferences: LanguagePreferences
    private lateinit var stepCounterPermissionHelper: StepCounterPermissionHelper

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(themeRepository, this@MainActivity)
    }

    private val languageViewModel: LanguageViewModel by viewModels {
        LanguageViewModelFactory(languageRepository)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化语言相关组件（必须在主题之前）
        languagePreferences = LanguagePreferences(this)
        languageRepository = LanguageRepository(this, languagePreferences)

        // 应用保存的语言设置
        val currentLanguage = languagePreferences.getCurrentLanguage()
        LanguageRepository.updateAppLanguage(this, currentLanguage)

        // 初始化主题相关组件
        themePreferences = ThemePreferences(this)
        themeRepository = ThemeRepository(this, themePreferences)

        // 初始化 StepCounterPermissionHelper
        stepCounterPermissionHelper = StepCounterPermissionHelper(this)

        setContent {
            DynamicThemeWrapper(
                themeRepository = themeRepository,
                themeViewModel = themeViewModel
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(
                        stepCounterPermissionHelper = stepCounterPermissionHelper,
                    )
                }
            }
        }
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
}