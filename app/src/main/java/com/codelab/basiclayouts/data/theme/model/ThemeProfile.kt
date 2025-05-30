package com.codelab.basiclayouts.data.theme.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class ThemeProfile(
    val id: String,
    val name: String,
    val description: String,
    val lightColors: ColorSchemeData,
    val darkColors: ColorSchemeData,
    val isBuiltIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class ColorSchemeData(
    val primary: String,
    val onPrimary: String,
    val primaryContainer: String,
    val onPrimaryContainer: String,
    val secondary: String,
    val onSecondary: String,
    val secondaryContainer: String,
    val onSecondaryContainer: String,
    val tertiary: String,
    val onTertiary: String,
    val tertiaryContainer: String,
    val onTertiaryContainer: String,
    val error: String,
    val errorContainer: String,
    val onError: String,
    val onErrorContainer: String,
    val background: String,
    val onBackground: String,
    val surface: String,
    val onSurface: String,
    val surfaceVariant: String,
    val onSurfaceVariant: String,
    val outline: String,
    val inverseOnSurface: String = "#FFFFFF",
    val inverseSurface: String = "#000000",
    val inversePrimary: String = primary,
    val shadow: String = "#000000",
    val surfaceTint: String = primary,
    val outlineVariant: String = outline,
    val scrim: String = "#000000"
)

@Serializable
data class DeepSeekColorResponse(
    val themeName: String,
    val themeDescription: String,
    val themeIntention: String,
    val lightColors: ColorSchemeData,
    val darkColors: ColorSchemeData
)