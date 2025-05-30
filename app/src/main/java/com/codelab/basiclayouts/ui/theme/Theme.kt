/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codelab.basiclayouts.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface
)

val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline
)
//
///**
// * 主要的应用主题组件
// * 支持动态主题切换和向后兼容
// */
//@Composable
//fun RayVitaTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    dynamicTheme: Boolean = false,
//    content: @Composable () -> Unit
//) {
//    if (dynamicTheme) {
//        // 使用动态主题
//        val context = LocalContext.current
//        val themePreferences = ThemePreferences(context)
//        val themeRepository = ThemeRepository(context, themePreferences)
//
//        val currentTheme by themeRepository.getCurrentTheme().collectAsState(initial = null)
//
//        currentTheme?.let { theme ->
//            DynamicRayVitaTheme(
//                themeProfile = theme,
//                darkTheme = darkTheme,
//                content = content
//            )
//        } ?: run {
//            // 加载中状态，使用默认主题
//            DefaultRayVitaTheme(darkTheme = darkTheme, content = content)
//        }
//    } else {
//        // 使用默认主题（向后兼容）
//        DefaultRayVitaTheme(darkTheme = darkTheme, content = content)
//    }
//}

/**
 * 默认主题（原有逻辑，保持向后兼容）
 */
@Composable
private fun DefaultRayVitaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}

///**
// * 应用级别的主题包装器
// * 在Application级别使用，自动应用用户选择的主题
// */
//@Composable
//fun AppThemeWrapper(
//    content: @Composable () -> Unit
//) {
//    RayVitaTheme(
//        dynamicTheme = true,
//        content = content
//    )
//}
//

/**
 * 主题工具函数
 */
object ThemeUtils {
    /**
     * 启动主题选择器
     */
    fun openThemeSelector(context: android.content.Context) {
        com.codelab.basiclayouts.ui.screen.themeChange.ThemeSelectorActivity.start(context)
    }
}