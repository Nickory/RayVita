package com.codelab.basiclayouts.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.codelab.basiclayouts.data.theme.model.ColorSchemeData
import com.codelab.basiclayouts.data.theme.model.ThemeProfile


/**
 * 当前主题的CompositionLocal
 */
val LocalThemeProfile = staticCompositionLocalOf<ThemeProfile?> { null }

/**
 * 动态主题包装器
 */
@Composable
fun DynamicRayVitaTheme(
    themeProfile: ThemeProfile,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = createColorScheme(
        colorData = if (darkTheme) themeProfile.darkColors else themeProfile.lightColors
    )

    CompositionLocalProvider(LocalThemeProfile provides themeProfile) {
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}

/**
 * 更新的RayVitaTheme，支持动态主题
 */
@Composable
fun RayVitaTheme(
    themeProfile: ThemeProfile? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    if (themeProfile != null) {
        DynamicRayVitaTheme(
            themeProfile = themeProfile,
            darkTheme = darkTheme,
            content = content
        )
    } else {
        // 使用默认主题（向后兼容）
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
}

/**
 * 从ColorSchemeData创建MaterialTheme的ColorScheme
 */
private fun createColorScheme(colorData: ColorSchemeData): ColorScheme {
    return lightColorScheme(
        primary = parseColor(colorData.primary),
        onPrimary = parseColor(colorData.onPrimary),
        primaryContainer = parseColor(colorData.primaryContainer),
        onPrimaryContainer = parseColor(colorData.onPrimaryContainer),
        secondary = parseColor(colorData.secondary),
        onSecondary = parseColor(colorData.onSecondary),
        secondaryContainer = parseColor(colorData.secondaryContainer),
        onSecondaryContainer = parseColor(colorData.onSecondaryContainer),
        tertiary = parseColor(colorData.tertiary),
        onTertiary = parseColor(colorData.onTertiary),
        tertiaryContainer = parseColor(colorData.tertiaryContainer),
        onTertiaryContainer = parseColor(colorData.onTertiaryContainer),
        error = parseColor(colorData.error),
        errorContainer = parseColor(colorData.errorContainer),
        onError = parseColor(colorData.onError),
        onErrorContainer = parseColor(colorData.onErrorContainer),
        background = parseColor(colorData.background),
        onBackground = parseColor(colorData.onBackground),
        surface = parseColor(colorData.surface),
        onSurface = parseColor(colorData.onSurface),
        surfaceVariant = parseColor(colorData.surfaceVariant),
        onSurfaceVariant = parseColor(colorData.onSurfaceVariant),
        outline = parseColor(colorData.outline),
        inverseOnSurface = parseColor(colorData.inverseOnSurface),
        inverseSurface = parseColor(colorData.inverseSurface),
        inversePrimary = parseColor(colorData.inversePrimary),
        surfaceTint = parseColor(colorData.surfaceTint),
        outlineVariant = parseColor(colorData.outlineVariant),
        scrim = parseColor(colorData.scrim)
    )
}

/**
 * 解析十六进制颜色字符串为Color对象
 */
private fun parseColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        Color.Magenta // 如果解析失败，返回明显的错误颜色
    }
}

/**
 * 获取当前主题配置
 */
@Composable
fun getCurrentThemeProfile(): ThemeProfile? {
    return LocalThemeProfile.current
}

/**
 * 全局主题状态管理器
 */
object ThemeManager {

    /**
     * 验证颜色字符串是否有效
     */
    fun isValidColor(colorString: String): Boolean {
        return try {
            android.graphics.Color.parseColor(colorString)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 生成颜色的浅色版本
     */
    fun lightenColor(color: String, factor: Float = 0.3f): String {
        return try {
            val androidColor = android.graphics.Color.parseColor(color)
            val red = ((android.graphics.Color.red(androidColor) * (1 - factor)) + (255 * factor)).toInt()
            val green = ((android.graphics.Color.green(androidColor) * (1 - factor)) + (255 * factor)).toInt()
            val blue = ((android.graphics.Color.blue(androidColor) * (1 - factor)) + (255 * factor)).toInt()

            String.format("#%02X%02X%02X", red, green, blue)
        } catch (e: Exception) {
            color
        }
    }

    /**
     * 生成颜色的深色版本
     */
    fun darkenColor(color: String, factor: Float = 0.3f): String {
        return try {
            val androidColor = android.graphics.Color.parseColor(color)
            val red = (android.graphics.Color.red(androidColor) * (1 - factor)).toInt()
            val green = (android.graphics.Color.green(androidColor) * (1 - factor)).toInt()
            val blue = (android.graphics.Color.blue(androidColor) * (1 - factor)).toInt()

            String.format("#%02X%02X%02X", red, green, blue)
        } catch (e: Exception) {
            color
        }
    }

    /**
     * 计算颜色的对比色（用于文字等）
     */
    fun getContrastColor(backgroundColor: String): String {
        return try {
            val color = android.graphics.Color.parseColor(backgroundColor)
            val luminance = (0.299 * android.graphics.Color.red(color) +
                    0.587 * android.graphics.Color.green(color) +
                    0.114 * android.graphics.Color.blue(color)) / 255

            if (luminance > 0.5) "#000000" else "#FFFFFF"
        } catch (e: Exception) {
            "#000000"
        }
    }
}