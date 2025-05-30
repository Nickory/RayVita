package com.codelab.basiclayouts.ui.theme

import com.codelab.basiclayouts.data.theme.model.ColorSchemeData
import com.codelab.basiclayouts.data.theme.model.ThemeProfile

object ColorSchemes {

    // 原有的暖色系主题（Warm Earth）
    private val warmEarthLight = ColorSchemeData(
        primary = "#6B5C4D",
        onPrimary = "#FFFFFF",
        primaryContainer = "#F4DFCD",
        onPrimaryContainer = "#241A0E",
        secondary = "#635D59",
        onSecondary = "#FFFFFF",
        secondaryContainer = "#EAE1DB",
        onSecondaryContainer = "#1F1B17",
        tertiary = "#5E5F58",
        onTertiary = "#FFFFFF",
        tertiaryContainer = "#E3E3DA",
        onTertiaryContainer = "#1B1C17",
        error = "#BA1A1A",
        errorContainer = "#FFDAD6",
        onError = "#FFFFFF",
        onErrorContainer = "#410002",
        background = "#F5F0EE",
        onBackground = "#1D1B1A",
        surface = "#FFFBFF",
        onSurface = "#1D1B1A",
        surfaceVariant = "#E7E1DE",
        onSurfaceVariant = "#494644",
        outline = "#7A7674",
        inverseOnSurface = "#F5F0EE"
    )

    private val warmEarthDark = ColorSchemeData(
        primary = "#D7C3B1",
        onPrimary = "#3A2E22",
        primaryContainer = "#524437",
        onPrimaryContainer = "#F4DFCD",
        secondary = "#CDC5BF",
        onSecondary = "#34302C",
        secondaryContainer = "#4B4642",
        onSecondaryContainer = "#EAE1DB",
        tertiary = "#C7C7BE",
        onTertiary = "#30312B",
        tertiaryContainer = "#464741",
        onTertiaryContainer = "#E3E3DA",
        error = "#FFB4AB",
        errorContainer = "#93000A",
        onError = "#690005",
        onErrorContainer = "#FFB4AB",
        background = "#32302F",
        onBackground = "#E6E1E0",
        surface = "#1D1B1A",
        onSurface = "#E6E1E0",
        surfaceVariant = "#494644",
        onSurfaceVariant = "#E6E1E0",
        outline = "#94908D",
        inverseOnSurface = "#1D1B1A"
    )

    // iOS Blue Theme
    val iosBlueLight = ColorSchemeData(
        primary = "#007AFF", // Vibrant iOS blue
        onPrimary = "#FFFFFF", // High contrast
        primaryContainer = "#D6E4FF", // Softer blue container
        onPrimaryContainer = "#002966", // Darker for readability
        secondary = "#5856D6", // Complementary purple-blue
        onSecondary = "#FFFFFF",
        secondaryContainer = "#E8E6FF", // Light purple container
        onSecondaryContainer = "#1C1B4A",
        tertiary = "#FF2D55", // Pink accent for vibrancy
        onTertiary = "#FFFFFF",
        tertiaryContainer = "#FFE4E8",
        onTertiaryContainer = "#5C0019",
        error = "#FF3B30",
        errorContainer = "#FFE6E4",
        onError = "#FFFFFF",
        onErrorContainer = "#410002",
        background = "#F7F7F9", // Light gray, iOS-like
        onBackground = "#1C1C1E",
        surface = "#FFFFFF",
        onSurface = "#1C1C1E",
        surfaceVariant = "#E8ECEF", // Subtle gray variant
        onSurfaceVariant = "#44474E",
        outline = "#8E9099",
        inverseOnSurface = "#FFFFFF"
    )

    val iosBlueDark = ColorSchemeData(
        primary = "#0A84FF", // Slightly brighter blue
        onPrimary = "#FFFFFF",
        primaryContainer = "#003087", // Deep blue container
        onPrimaryContainer = "#D6E4FF",
        secondary = "#7B78FF", // Softer purple-blue
        onSecondary = "#FFFFFF",
        secondaryContainer = "#3B3A8A",
        onSecondaryContainer = "#E8E6FF",
        tertiary = "#FF5C77", // Softer pink
        onTertiary = "#FFFFFF",
        tertiaryContainer = "#8A0023",
        onTertiaryContainer = "#FFE4E8",
        error = "#FF453A",
        errorContainer = "#93000A",
        onError = "#FFFFFF",
        onErrorContainer = "#FFE6E4",
        background = "#1A1B1E", // Deep gray-blue, not pure black
        onBackground = "#E3E3E6",
        surface = "#2A2B2F", // Slightly lighter for depth
        onSurface = "#E3E3E6",
        surfaceVariant = "#3B3D43", // Darker variant
        onSurfaceVariant = "#C4C6CC",
        outline = "#8E9099",
        inverseOnSurface = "#2A2B2F"
    )

    // Forest Green Theme
    val forestGreenLight = ColorSchemeData(
        primary = "#2E7D32", // Rich green
        onPrimary = "#FFFFFF",
        primaryContainer = "#C6E8C8", // Soft green container
        onPrimaryContainer = "#0D3D0F",
        secondary = "#4CAF50", // Brighter complementary green
        onSecondary = "#FFFFFF",
        secondaryContainer = "#E0F2E1",
        onSecondaryContainer = "#1B5E20",
        tertiary = "#81C784", // Lighter green accent
        onTertiary = "#1B3D1C",
        tertiaryContainer = "#E8F7E9",
        onTertiaryContainer = "#2E7D32",
        error = "#FF3B30",
        errorContainer = "#FFE6E4",
        onError = "#FFFFFF",
        onErrorContainer = "#410002",
        background = "#F7FBF7", // Very light green-tinted background
        onBackground = "#1B3D1C",
        surface = "#FFFFFF",
        onSurface = "#1B3D1C",
        surfaceVariant = "#E8F7E9",
        onSurfaceVariant = "#3D4E3E",
        outline = "#6B7D6C",
        inverseOnSurface = "#FFFFFF"
    )

    val forestGreenDark = ColorSchemeData(
        primary = "#66BB6A", // Vibrant green
        onPrimary = "#0D3D0F",
        primaryContainer = "#2E7D32",
        onPrimaryContainer = "#C6E8C8",
        secondary = "#81C784",
        onSecondary = "#1B5E20",
        secondaryContainer = "#4CAF50",
        onSecondaryContainer = "#E0F2E1",
        tertiary = "#A5D6A7", // Softer green accent
        onTertiary = "#2E7D32",
        tertiaryContainer = "#3D8C40",
        onTertiaryContainer = "#E8F7E9",
        error = "#FF453A",
        errorContainer = "#93000A",
        onError = "#FFFFFF",
        onErrorContainer = "#FFE6E4",
        background = "#0A120F", // Deep forest green
        onBackground = "#C6E8C8",
        surface = "#1A2B1C", // Slightly lighter for depth
        onSurface = "#C6E8C8",
        surfaceVariant = "#2A3B2C",
        onSurfaceVariant = "#A3B5A4",
        outline = "#6B7D6C",
        inverseOnSurface = "#1A2B1C"
    )

    // Ocean Blue Theme
    val oceanBlueLight = ColorSchemeData(
        primary = "#1976D2", // Deep ocean blue
        onPrimary = "#FFFFFF",
        primaryContainer = "#BBDEFB",
        onPrimaryContainer = "#003087",
        secondary = "#0288D1", // Lighter blue
        onSecondary = "#FFFFFF",
        secondaryContainer = "#E1F5FE",
        onSecondaryContainer = "#004C8C",
        tertiary = "#4FC3F7", // Bright cyan accent
        onTertiary = "#003A66",
        tertiaryContainer = "#E6F7FF",
        onTertiaryContainer = "#006398",
        error = "#FF3B30",
        errorContainer = "#FFE6E4",
        onError = "#FFFFFF",
        onErrorContainer = "#410002",
        background = "#F5FAFF", // Light blue-tinted background
        onBackground = "#003087",
        surface = "#FFFFFF",
        onSurface = "#003087",
        surfaceVariant = "#E6F7FF",
        onSurfaceVariant = "#3A4C66",
        outline = "#6B8299",
        inverseOnSurface = "#FFFFFF"
    )

    val oceanBlueDark = ColorSchemeData(
        primary = "#4FC3F7", // Bright ocean blue
        onPrimary = "#003087",
        primaryContainer = "#1976D2",
        onPrimaryContainer = "#BBDEFB",
        secondary = "#4DD0E1", // Cyan secondary
        onSecondary = "#004C8C",
        secondaryContainer = "#0288D1",
        onSecondaryContainer = "#E1F5FE",
        tertiary = "#81D4FA", // Lighter blue accent
        onTertiary = "#006398",
        tertiaryContainer = "#4FC3F7",
        onTertiaryContainer = "#E6F7FF",
        error = "#FF453A",
        errorContainer = "#93000A",
        onError = "#FFFFFF",
        onErrorContainer = "#FFE6E4",
        background = "#0B1726", // Deep navy blue
        onBackground = "#BBDEFB",
        surface = "#1B2A3A", // Slightly lighter for depth
        onSurface = "#BBDEFB",
        surfaceVariant = "#2B3B4A",
        onSurfaceVariant = "#A3B8CC",
        outline = "#6B8299",
        inverseOnSurface = "#1B2A3A"
    )

    // Violet Dream Theme
    val violetDreamLight = ColorSchemeData(
        primary = "#7E57C2", // Soft purple
        onPrimary = "#FFFFFF",
        primaryContainer = "#D1C4E9", // Light purple container
        onPrimaryContainer = "#311B92",
        secondary = "#AB47BC", // Vibrant magenta
        onSecondary = "#FFFFFF",
        secondaryContainer = "#F3E5F5",
        onSecondaryContainer = "#6A1B9A",
        tertiary = "#F06292", // Pink accent
        onTertiary = "#FFFFFF",
        tertiaryContainer = "#FFE4EC",
        onTertiaryContainer = "#880E4F",
        error = "#FF3B30",
        errorContainer = "#FFE6E4",
        onError = "#FFFFFF",
        onErrorContainer = "#410002",
        background = "#F9F6FD", // Light purple-tinted background
        onBackground = "#311B92",
        surface = "#FFFFFF",
        onSurface = "#311B92",
        surfaceVariant = "#EDE7F6",
        onSurfaceVariant = "#4A3A66",
        outline = "#7E6699",
        inverseOnSurface = "#FFFFFF"
    )

    val violetDreamDark = ColorSchemeData(
        primary = "#B39DDB", // Light purple
        onPrimary = "#311B92",
        primaryContainer = "#7E57C2",
        onPrimaryContainer = "#D1C4E9",
        secondary = "#D81B60", // Deep pink
        onSecondary = "#FFFFFF",
        secondaryContainer = "#AB47BC",
        onSecondaryContainer = "#F3E5F5",
        tertiary = "#FF80AB", // Soft pink accent
        onTertiary = "#880E4F",
        tertiaryContainer = "#F06292",
        onTertiaryContainer = "#FFE4EC",
        error = "#FF453A",
        errorContainer = "#93000A",
        onError = "#FFFFFF",
        onErrorContainer = "#FFE6E4",
        background = "#1A1426", // Deep purple-gray
        onBackground = "#D1C4E9",
        surface = "#2A1F3A", // Slightly lighter for depth
        onSurface = "#D1C4E9",
        surfaceVariant = "#3A2F4A",
        onSurfaceVariant = "#B8A3CC",
        outline = "#7E6699",
        inverseOnSurface = "#2A1F3A"
    )

    // Sunset Orange Theme
    val sunsetOrangeLight = ColorSchemeData(
        primary = "#F57C00", // Warm orange
        onPrimary = "#FFFFFF",
        primaryContainer = "#FFCC80", // Soft orange container
        onPrimaryContainer = "#BF360C",
        secondary = "#FF9800", // Brighter orange
        onSecondary = "#FFFFFF",
        secondaryContainer = "#FFE0B2",
        onSecondaryContainer = "#E65100",
        tertiary = "#FFB300", // Yellow-orange accent
        onTertiary = "#3E2C00",
        tertiaryContainer = "#FFE082",
        onTertiaryContainer = "#5D4037",
        error = "#FF3B30",
        errorContainer = "#FFE6E4",
        onError = "#FFFFFF",
        onErrorContainer = "#410002",
        background = "#FFF8E6", // Warm light background
        onBackground = "#BF360C",
        surface = "#FFFFFF",
        onSurface = "#BF360C",
        surfaceVariant = "#FFF3E0",
        onSurfaceVariant = "#5D4037",
        outline = "#997766",
        inverseOnSurface = "#FFFFFF"
    )

    val sunsetOrangeDark = ColorSchemeData(
        primary = "#FFB300", // Bright orange
        onPrimary = "#BF360C",
        primaryContainer = "#F57C00",
        onPrimaryContainer = "#FFCC80",
        secondary = "#FFCA28", // Yellow-orange secondary
        onSecondary = "#3E2C00",
        secondaryContainer = "#FF9800",
        onSecondaryContainer = "#FFE0B2",
        tertiary = "#FFD54F", // Light yellow accent
        onTertiary = "#5D4037",
        tertiaryContainer = "#FFB300",
        onTertiaryContainer = "#FFE082",
        error = "#FF453A",
        errorContainer = "#93000A",
        onError = "#FFFFFF",
        onErrorContainer = "#FFE6E4",
        background = "#1F140A", // Deep warm brown
        onBackground = "#FFCC80",
        surface = "#3A2A1F", // Slightly lighter for depth
        onSurface = "#FFCC80",
        surfaceVariant = "#4A3A2F",
        onSurfaceVariant = "#CCB3A3",
        outline = "#997766",
        inverseOnSurface = "#3A2A1F"
    )

    val builtInThemes = listOf(
        ThemeProfile(
            id = "warm_earth",
            name = "暖色大地",
            description = "温暖自然的大地色调，营造舒适宁静的氛围",
            lightColors = warmEarthLight,
            darkColors = warmEarthDark,
            isBuiltIn = true
        ),
        ThemeProfile(
            id = "ios_blue",
            name = "iOS 蓝",
            description = "经典的iOS设计风格，清新简洁的蓝色系",
            lightColors = iosBlueLight,
            darkColors = iosBlueDark,
            isBuiltIn = true
        ),
        ThemeProfile(
            id = "forest_green",
            name = "森林绿",
            description = "自然清新的绿色系，带来生机盎然的视觉体验",
            lightColors = forestGreenLight,
            darkColors = forestGreenDark,
            isBuiltIn = true
        ),
        ThemeProfile(
            id = "ocean_blue",
            name = "深海蓝",
            description = "深邃宁静的蓝色系，如深海般的沉静优雅",
            lightColors = oceanBlueLight,
            darkColors = oceanBlueDark,
            isBuiltIn = true
        ),
        ThemeProfile(
            id = "violet_dream",
            name = "紫罗兰梦境",
            description = "神秘浪漫的紫色系，充满创意与想象力",
            lightColors = violetDreamLight,
            darkColors = violetDreamDark,
            isBuiltIn = true
        ),
        ThemeProfile(
            id = "sunset_orange",
            name = "日落橙",
            description = "温暖活力的橙色系，如夕阳般的热情与温暖",
            lightColors = sunsetOrangeLight,
            darkColors = sunsetOrangeDark,
            isBuiltIn = true
        )
    )
}