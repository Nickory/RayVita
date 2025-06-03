package com.codelab.basiclayouts.ui.theme

import android.content.Context
import com.codelab.basiclayouts.R
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
    private val iosBlueLight = ColorSchemeData(
        primary = "#007AFF",
        onPrimary = "#FFFFFF",
        primaryContainer = "#D6E4FF",
        onPrimaryContainer = "#002966",
        secondary = "#5856D6",
        onSecondary = "#FFFFFF",
        secondaryContainer = "#E8E6FF",
        onSecondaryContainer = "#1C1B4A",
        tertiary = "#FF2D55",
        onTertiary = "#FFFFFF",
        tertiaryContainer = "#FFE4E8",
        onTertiaryContainer = "#5C0019",
        error = "#FF3B30",
        errorContainer = "#FFE6E4",
        onError = "#FFFFFF",
        onErrorContainer = "#410002",
        background = "#F7F7F9",
        onBackground = "#1C1C1E",
        surface = "#FFFFFF",
        onSurface = "#1C1C1E",
        surfaceVariant = "#E8ECEF",
        onSurfaceVariant = "#44474E",
        outline = "#8E9099",
        inverseOnSurface = "#FFFFFF"
    )

    private val iosBlueDark = ColorSchemeData(
        primary = "#0A84FF",
        onPrimary = "#FFFFFF",
        primaryContainer = "#003087",
        onPrimaryContainer = "#D6E4FF",
        secondary = "#7B78FF",
        onSecondary = "#FFFFFF",
        secondaryContainer = "#3B3A8A",
        onSecondaryContainer = "#E8E6FF",
        tertiary = "#FF5C77",
        onTertiary = "#FFFFFF",
        tertiaryContainer = "#8A0023",
        onTertiaryContainer = "#FFE4E8",
        error = "#FF453A",
        errorContainer = "#93000A",
        onError = "#FFFFFF",
        onErrorContainer = "#FFE6E4",
        background = "#1A1B1E",
        onBackground = "#E3E3E6",
        surface = "#2A2B2F",
        onSurface = "#E3E3E6",
        surfaceVariant = "#3B3D43",
        onSurfaceVariant = "#C4C6CC",
        outline = "#8E9099",
        inverseOnSurface = "#2A2B2F"
    )

    // Forest Green Theme
    private val forestGreenLight = ColorSchemeData(
        primary = "#2E7D32",
        onPrimary = "#FFFFFF",
        primaryContainer = "#C6E8C8",
        onPrimaryContainer = "#0D3D0F",
        secondary = "#4CAF50",
        onSecondary = "#FFFFFF",
        secondaryContainer = "#E0F2E1",
        onSecondaryContainer = "#1B5E20",
        tertiary = "#81C784",
        onTertiary = "#1B3D1C",
        tertiaryContainer = "#E8F7E9",
        onTertiaryContainer = "#2E7D32",
        error = "#FF3B30",
        errorContainer = "#FFE6E4",
        onError = "#FFFFFF",
        onErrorContainer = "#410002",
        background = "#F7FBF7",
        onBackground = "#1B3D1C",
        surface = "#FFFFFF",
        onSurface = "#1B3D1C",
        surfaceVariant = "#E8F7E9",
        onSurfaceVariant = "#3D4E3E",
        outline = "#6B7D6C",
        inverseOnSurface = "#FFFFFF"
    )

    private val forestGreenDark = ColorSchemeData(
        primary = "#66BB6A",
        onPrimary = "#0D3D0F",
        primaryContainer = "#2E7D32",
        onPrimaryContainer = "#C6E8C8",
        secondary = "#81C784",
        onSecondary = "#1B5E20",
        secondaryContainer = "#4CAF50",
        onSecondaryContainer = "#E0F2E1",
        tertiary = "#A5D6A7",
        onTertiary = "#2E7D32",
        tertiaryContainer = "#3D8C40",
        onTertiaryContainer = "#E8F7E9",
        error = "#FF453A",
        errorContainer = "#93000A",
        onError = "#FFFFFF",
        onErrorContainer = "#FFE6E4",
        background = "#0A120F",
        onBackground = "#C6E8C8",
        surface = "#1A2B1C",
        onSurface = "#C6E8C8",
        surfaceVariant = "#2A3B2C",
        onSurfaceVariant = "#A3B5A4",
        outline = "#6B7D6C",
        inverseOnSurface = "#1A2B1C"
    )

    // ==================== rPPG 生物医学主题系列 ====================

    // 1. NeuroPulse | 神经脉冲
    private val neuroPulseLight = ColorSchemeData(
        primary = "#6B46C1", // 深紫 - 神经网络核心
        onPrimary = "#FFFFFF",
        primaryContainer = "#DDD6FE", // 柔和紫色容器
        onPrimaryContainer = "#2D1B69",
        secondary = "#00D9FF", // 电光蓝 - 神经信号
        onSecondary = "#003544",
        secondaryContainer = "#B8F2FF", // 浅电光蓝
        onSecondaryContainer = "#001F2A",
        tertiary = "#8B5CF6", // 中性紫 - 脉冲强调
        onTertiary = "#FFFFFF",
        tertiaryContainer = "#E0E7FF",
        onTertiaryContainer = "#3730A3",
        error = "#DC2626",
        errorContainer = "#FEE2E2",
        onError = "#FFFFFF",
        onErrorContainer = "#7F1D1D",
        background = "#FAFBFF", // 极浅紫调背景
        onBackground = "#1E1B3A",
        surface = "#FFFFFF",
        onSurface = "#1E1B3A",
        surfaceVariant = "#F1F0FF",
        onSurfaceVariant = "#4C4669",
        outline = "#7C7A92",
        inverseOnSurface = "#F8FAFC"
    )

    private val neuroPulseDark = ColorSchemeData(
        primary = "#A78BFA", // 明亮神经紫
        onPrimary = "#2D1B69",
        primaryContainer = "#5B21B6",
        onPrimaryContainer = "#DDD6FE",
        secondary = "#22D3EE", // 亮电光蓝
        onSecondary = "#001F2A",
        secondaryContainer = "#0891B2",
        onSecondaryContainer = "#B8F2FF",
        tertiary = "#C4B5FD", // 柔和紫光
        onTertiary = "#3730A3",
        tertiaryContainer = "#6366F1",
        onTertiaryContainer = "#E0E7FF",
        error = "#F87171",
        errorContainer = "#7F1D1D",
        onError = "#7F1D1D",
        onErrorContainer = "#FEE2E2",
        background = "#0F0A1A", // 深空紫背景
        onBackground = "#E8E4F3",
        surface = "#1A1629", // 神经暗紫表面
        onSurface = "#E8E4F3",
        surfaceVariant = "#2A1F3D",
        onSurfaceVariant = "#C4B5FD",
        outline = "#7C7A92",
        inverseOnSurface = "#1A1629"
    )

    // 2. BioSerenity | 生物静息态
    private val bioSerenityLight = ColorSchemeData(
        primary = "#10B981", // 生命绿 - 健康活力
        onPrimary = "#FFFFFF",
        primaryContainer = "#D1FAE5", // 柔和生命绿
        onPrimaryContainer = "#064E3B",
        secondary = "#F5F5DC", // 象牙白 - 有机质感
        onSecondary = "#374151",
        secondaryContainer = "#FEFDFB", // 纯净象牙
        onSecondaryContainer = "#1F2937",
        tertiary = "#34D399", // 清新薄荷绿
        onTertiary = "#064E3B",
        tertiaryContainer = "#ECFDF5",
        onTertiaryContainer = "#047857",
        error = "#EF4444",
        errorContainer = "#FEE2E2",
        onError = "#FFFFFF",
        onErrorContainer = "#7F1D1D",
        background = "#FEFFFE", // 纯净生物背景
        onBackground = "#111827",
        surface = "#FFFFFF",
        onSurface = "#111827",
        surfaceVariant = "#F0FDF4",
        onSurfaceVariant = "#365A3D",
        outline = "#6B7C70",
        inverseOnSurface = "#F9FAFB"
    )

    private val bioSerenityDark = ColorSchemeData(
        primary = "#34D399", // 明亮生命绿
        onPrimary = "#064E3B",
        primaryContainer = "#059669",
        onPrimaryContainer = "#D1FAE5",
        secondary = "#F3F4F6", // 月光银 - 静息态
        onSecondary = "#1F2937",
        secondaryContainer = "#4B5563",
        onSecondaryContainer = "#FEFDFB",
        tertiary = "#6EE7B7", // 柔和薄荷
        onTertiary = "#047857",
        tertiaryContainer = "#10B981",
        onTertiaryContainer = "#ECFDF5",
        error = "#F87171",
        errorContainer = "#7F1D1D",
        onError = "#7F1D1D",
        onErrorContainer = "#FEE2E2",
        background = "#0A1F12", // 深森林背景
        onBackground = "#E0F2E0",
        surface = "#1A2E23", // 生物暗绿表面
        onSurface = "#E0F2E0",
        surfaceVariant = "#2A3F32",
        onSurfaceVariant = "#A7C4A7",
        outline = "#6B7C70",
        inverseOnSurface = "#1A2E23"
    )

    // 3. OptiWave | 光波干涉
    private val optiWaveLight = ColorSchemeData(
        primary = "#0F766E", // 深青 - 精密科技
        onPrimary = "#FFFFFF",
        primaryContainer = "#CCFBF1", // 浅科技青
        onPrimaryContainer = "#134E4A",
        secondary = "#E5E7EB", // 科技银白
        onSecondary = "#374151",
        secondaryContainer = "#F9FAFB", // 纯银白
        onSecondaryContainer = "#111827",
        tertiary = "#06B6D4", // 微光蓝 - 干涉波
        onTertiary = "#FFFFFF",
        tertiaryContainer = "#E0F7FA",
        onTertiaryContainer = "#164E63",
        error = "#DC2626",
        errorContainer = "#FEE2E2",
        onError = "#FFFFFF",
        onErrorContainer = "#7F1D1D",
        background = "#FAFBFC", // 实验室白
        onBackground = "#111827",
        surface = "#FFFFFF",
        onSurface = "#111827",
        surfaceVariant = "#F0F9FF",
        onSurfaceVariant = "#475569",
        outline = "#64748B",
        inverseOnSurface = "#F8FAFC"
    )

    private val optiWaveDark = ColorSchemeData(
        primary = "#14B8A6", // 明亮科技青
        onPrimary = "#134E4A",
        primaryContainer = "#0F766E",
        onPrimaryContainer = "#CCFBF1",
        secondary = "#F3F4F6", // 冷光银
        onSecondary = "#111827",
        secondaryContainer = "#374151",
        onSecondaryContainer = "#F9FAFB",
        tertiary = "#22D3EE", // 亮微光蓝
        onTertiary = "#164E63",
        tertiaryContainer = "#0891B2",
        onTertiaryContainer = "#E0F7FA",
        error = "#F87171",
        errorContainer = "#7F1D1D",
        onError = "#7F1D1D",
        onErrorContainer = "#FEE2E2",
        background = "#0A0E0F", // 深空实验室
        onBackground = "#E5E7EB",
        surface = "#1A1F23", // 科技暗面
        onSurface = "#E5E7EB",
        surfaceVariant = "#2A333A",
        onSurfaceVariant = "#B8C5C5",
        outline = "#64748B",
        inverseOnSurface = "#1A1F23"
    )

    // 4. CardioSync | 心律同步
    private val cardioSyncLight = ColorSchemeData(
        primary = "#DC2626", // 血红 - 心跳脉搏
        onPrimary = "#FFFFFF",
        primaryContainer = "#FEE2E2", // 柔和血红
        onPrimaryContainer = "#7F1D1D",
        secondary = "#EA580C", // 动脉橙 - 血流活力
        onSecondary = "#FFFFFF",
        secondaryContainer = "#FED7AA", // 温暖橙容器
        onSecondaryContainer = "#9A3412",
        tertiary = "#F472B6", // 生命粉红 - 活力律动
        onTertiary = "#FFFFFF",
        tertiaryContainer = "#FCE7F3",
        onTertiaryContainer = "#BE185D",
        error = "#991B1B",
        errorContainer = "#FCA5A5",
        onError = "#FFFFFF",
        onErrorContainer = "#450A0A",
        background = "#FFFBFB", // 温暖生命背景
        onBackground = "#1F1717",
        surface = "#FFFFFF",
        onSurface = "#1F1717",
        surfaceVariant = "#FFEBEB",
        onSurfaceVariant = "#8B2635",
        outline = "#B91C1C",
        inverseOnSurface = "#FEF2F2"
    )

    private val cardioSyncDark = ColorSchemeData(
        primary = "#F87171", // 明亮心律红
        onPrimary = "#7F1D1D",
        primaryContainer = "#B91C1C",
        onPrimaryContainer = "#FEE2E2",
        secondary = "#FB923C", // 亮动脉橙
        onSecondary = "#9A3412",
        secondaryContainer = "#C2410C",
        onSecondaryContainer = "#FED7AA",
        tertiary = "#F9A8D4", // 柔和生命粉
        onTertiary = "#BE185D",
        tertiaryContainer = "#EC4899",
        onTertiaryContainer = "#FCE7F3",
        error = "#FCA5A5",
        errorContainer = "#450A0A",
        onError = "#450A0A",
        onErrorContainer = "#FCA5A5",
        background = "#1A0B0B", // 深血色背景
        onBackground = "#F5E5E5",
        surface = "#2D1818", // 心律暗红表面
        onSurface = "#F5E5E5",
        surfaceVariant = "#3D2323",
        onSurfaceVariant = "#E5A3A3",
        outline = "#B91C1C",
        inverseOnSurface = "#2D1818"
    )

    // 5. AuroraFlow | 极光流动
    private val auroraFlowLight = ColorSchemeData(
        primary = "#8B5CF6", // 梦幻紫 - 极光主色
        onPrimary = "#FFFFFF",
        primaryContainer = "#EDE9FE", // 浅梦幻紫
        onPrimaryContainer = "#5B21B6",
        secondary = "#F472B6", // 柔粉 - 极光渐变
        onSecondary = "#FFFFFF",
        secondaryContainer = "#FCE7F3", // 浅粉容器
        onSecondaryContainer = "#BE185D",
        tertiary = "#06B6D4", // 极光青 - 流动色彩
        onTertiary = "#FFFFFF",
        tertiaryContainer = "#E0F7FA",
        onTertiaryContainer = "#164E63",
        error = "#EF4444",
        errorContainer = "#FEE2E2",
        onError = "#FFFFFF",
        onErrorContainer = "#7F1D1D",
        background = "#FEFCFF", // 极光天空背景
        onBackground = "#1E1B3A",
        surface = "#FFFFFF",
        onSurface = "#1E1B3A",
        surfaceVariant = "#F5F0FF",
        onSurfaceVariant = "#5B4E75",
        outline = "#8B7FA8",
        inverseOnSurface = "#F8FAFC"
    )

    private val auroraFlowDark = ColorSchemeData(
        primary = "#A78BFA", // 亮梦幻紫
        onPrimary = "#5B21B6",
        primaryContainer = "#7C3AED",
        onPrimaryContainer = "#EDE9FE",
        secondary = "#F9A8D4", // 亮柔粉
        onSecondary = "#BE185D",
        secondaryContainer = "#EC4899",
        onSecondaryContainer = "#FCE7F3",
        tertiary = "#22D3EE", // 亮极光青
        onTertiary = "#164E63",
        tertiaryContainer = "#0891B2",
        onTertiaryContainer = "#E0F7FA",
        error = "#F87171",
        errorContainer = "#7F1D1D",
        onError = "#7F1D1D",
        onErrorContainer = "#FEE2E2",
        background = "#0F0A1E", // 深夜极光背景
        onBackground = "#E8E4F8",
        surface = "#1A1034", // 极光暗面
        onSurface = "#E8E4F8",
        surfaceVariant = "#2A1F4A",
        onSurfaceVariant = "#C4B5FD",
        outline = "#8B7FA8",
        inverseOnSurface = "#1A1034"
    )

    // ==================== 其他经典主题 ====================

    // Ocean Blue Theme
    private val oceanBlueLight = ColorSchemeData(
        primary = "#1976D2",
        onPrimary = "#FFFFFF",
        primaryContainer = "#BBDEFB",
        onPrimaryContainer = "#003087",
        secondary = "#0288D1",
        onSecondary = "#FFFFFF",
        secondaryContainer = "#E1F5FE",
        onSecondaryContainer = "#004C8C",
        tertiary = "#4FC3F7",
        onTertiary = "#003A66",
        tertiaryContainer = "#E6F7FF",
        onTertiaryContainer = "#006398",
        error = "#FF3B30",
        errorContainer = "#FFE6E4",
        onError = "#FFFFFF",
        onErrorContainer = "#410002",
        background = "#F5FAFF",
        onBackground = "#003087",
        surface = "#FFFFFF",
        onSurface = "#003087",
        surfaceVariant = "#E6F7FF",
        onSurfaceVariant = "#3A4C66",
        outline = "#6B8299",
        inverseOnSurface = "#FFFFFF"
    )

    private val oceanBlueDark = ColorSchemeData(
        primary = "#4FC3F7",
        onPrimary = "#003087",
        primaryContainer = "#1976D2",
        onPrimaryContainer = "#BBDEFB",
        secondary = "#4DD0E1",
        onSecondary = "#004C8C",
        secondaryContainer = "#0288D1",
        onSecondaryContainer = "#E1F5FE",
        tertiary = "#81D4FA",
        onTertiary = "#006398",
        tertiaryContainer = "#4FC3F7",
        onTertiaryContainer = "#E6F7FF",
        error = "#FF453A",
        errorContainer = "#93000A",
        onError = "#FFFFFF",
        onErrorContainer = "#FFE6E4",
        background = "#0B1726",
        onBackground = "#BBDEFB",
        surface = "#1B2A3A",
        onSurface = "#BBDEFB",
        surfaceVariant = "#2B3B4A",
        onSurfaceVariant = "#A3B8CC",
        outline = "#6B8299",
        inverseOnSurface = "#1B2A3A"
    )

    // Violet Dream Theme
    private val violetDreamLight = ColorSchemeData(
        primary = "#7E57C2",
        onPrimary = "#FFFFFF",
        primaryContainer = "#D1C4E9",
        onPrimaryContainer = "#311B92",
        secondary = "#AB47BC",
        onSecondary = "#FFFFFF",
        secondaryContainer = "#F3E5F5",
        onSecondaryContainer = "#6A1B9A",
        tertiary = "#F06292",
        onTertiary = "#FFFFFF",
        tertiaryContainer = "#FFE4EC",
        onTertiaryContainer = "#880E4F",
        error = "#FF3B30",
        errorContainer = "#FFE6E4",
        onError = "#FFFFFF",
        onErrorContainer = "#410002",
        background = "#F9F6FD",
        onBackground = "#311B92",
        surface = "#FFFFFF",
        onSurface = "#311B92",
        surfaceVariant = "#EDE7F6",
        onSurfaceVariant = "#4A3A66",
        outline = "#7E6699",
        inverseOnSurface = "#FFFFFF"
    )

    private val violetDreamDark = ColorSchemeData(
        primary = "#B39DDB",
        onPrimary = "#311B92",
        primaryContainer = "#7E57C2",
        onPrimaryContainer = "#D1C4E9",
        secondary = "#D81B60",
        onSecondary = "#FFFFFF",
        secondaryContainer = "#AB47BC",
        onSecondaryContainer = "#F3E5F5",
        tertiary = "#FF80AB",
        onTertiary = "#880E4F",
        tertiaryContainer = "#F06292",
        onTertiaryContainer = "#FFE4EC",
        error = "#FF453A",
        errorContainer = "#93000A",
        onError = "#FFFFFF",
        onErrorContainer = "#FFE6E4",
        background = "#1A1426",
        onBackground = "#D1C4E9",
        surface = "#2A1F3A",
        onSurface = "#D1C4E9",
        surfaceVariant = "#3A2F4A",
        onSurfaceVariant = "#B8A3CC",
        outline = "#7E6699",
        inverseOnSurface = "#2A1F3A"
    )

    // Sunset Orange Theme
    private val sunsetOrangeLight = ColorSchemeData(
        primary = "#F57C00",
        onPrimary = "#FFFFFF",
        primaryContainer = "#FFCC80",
        onPrimaryContainer = "#BF360C",
        secondary = "#FF9800",
        onSecondary = "#FFFFFF",
        secondaryContainer = "#FFE0B2",
        onSecondaryContainer = "#E65100",
        tertiary = "#FFB300",
        onTertiary = "#3E2C00",
        tertiaryContainer = "#FFE082",
        onTertiaryContainer = "#5D4037",
        error = "#FF3B30",
        errorContainer = "#FFE6E4",
        onError = "#FFFFFF",
        onErrorContainer = "#410002",
        background = "#FFF8E6",
        onBackground = "#BF360C",
        surface = "#FFFFFF",
        onSurface = "#BF360C",
        surfaceVariant = "#FFF3E0",
        onSurfaceVariant = "#5D4037",
        outline = "#997766",
        inverseOnSurface = "#FFFFFF"
    )

    private val sunsetOrangeDark = ColorSchemeData(
        primary = "#FFB300",
        onPrimary = "#BF360C",
        primaryContainer = "#F57C00",
        onPrimaryContainer = "#FFCC80",
        secondary = "#FFCA28",
        onSecondary = "#3E2C00",
        secondaryContainer = "#FF9800",
        onSecondaryContainer = "#FFE0B2",
        tertiary = "#FFD54F",
        onTertiary = "#5D4037",
        tertiaryContainer = "#FFB300",
        onTertiaryContainer = "#FFE082",
        error = "#FF453A",
        errorContainer = "#93000A",
        onError = "#FFFFFF",
        onErrorContainer = "#FFE6E4",
        background = "#1F140A",
        onBackground = "#FFCC80",
        surface = "#3A2A1F",
        onSurface = "#FFCC80",
        surfaceVariant = "#4A3A2F",
        onSurfaceVariant = "#CCB3A3",
        outline = "#997766",
        inverseOnSurface = "#3A2A1F"
    )

    // ==================== 主题数据类，存储资源ID ====================

    /**
     * 内置主题数据类，存储资源ID而非字符串
     */
    data class BuiltInThemeData(
        val id: String,
        val nameResId: Int,
        val descriptionResId: Int,
        val lightColors: ColorSchemeData,
        val darkColors: ColorSchemeData,
        val isBuiltIn: Boolean = true
    )

    /**
     * 内置主题资源ID列表（不需要Context）
     */
    val builtInThemeResources = listOf(
        BuiltInThemeData(
            id = "warm_earth",
            nameResId = R.string.builtin_theme_warm_earth_name,
            descriptionResId = R.string.builtin_theme_warm_earth_description,
            lightColors = warmEarthLight,
            darkColors = warmEarthDark
        ),
        BuiltInThemeData(
            id = "ios_blue",
            nameResId = R.string.builtin_theme_ios_blue_name,
            descriptionResId = R.string.builtin_theme_ios_blue_description,
            lightColors = iosBlueLight,
            darkColors = iosBlueDark
        ),
        BuiltInThemeData(
            id = "forest_green",
            nameResId = R.string.builtin_theme_forest_green_name,
            descriptionResId = R.string.builtin_theme_forest_green_description,
            lightColors = forestGreenLight,
            darkColors = forestGreenDark
        ),
        BuiltInThemeData(
            id = "ocean_blue",
            nameResId = R.string.builtin_theme_ocean_blue_name,
            descriptionResId = R.string.builtin_theme_ocean_blue_description,
            lightColors = oceanBlueLight,
            darkColors = oceanBlueDark
        ),
        BuiltInThemeData(
            id = "violet_dream",
            nameResId = R.string.builtin_theme_violet_dream_name,
            descriptionResId = R.string.builtin_theme_violet_dream_description,
            lightColors = violetDreamLight,
            darkColors = violetDreamDark
        ),
        BuiltInThemeData(
            id = "sunset_orange",
            nameResId = R.string.builtin_theme_sunset_orange_name,
            descriptionResId = R.string.builtin_theme_sunset_orange_description,
            lightColors = sunsetOrangeLight,
            darkColors = sunsetOrangeDark
        ),
        BuiltInThemeData(
            id = "bio_serenity",
            nameResId = R.string.builtin_theme_bio_serenity_name,
            descriptionResId = R.string.builtin_theme_bio_serenity_description,
            lightColors = bioSerenityLight,
            darkColors = bioSerenityDark
        ),
        BuiltInThemeData(
            id = "cardio_sync",
            nameResId = R.string.builtin_theme_cardio_sync_name,
            descriptionResId = R.string.builtin_theme_cardio_sync_description,
            lightColors = cardioSyncLight,
            darkColors = cardioSyncDark
        ),
        BuiltInThemeData(
            id = "aurora_flow",
            nameResId = R.string.builtin_theme_aurora_flow_name,
            descriptionResId = R.string.builtin_theme_aurora_flow_description,
            lightColors = auroraFlowLight,
            darkColors = auroraFlowDark
        )
    )

    // ==================== 转换函数 ====================

    /**
     * 将资源ID数据转换为ThemeProfile（需要Context）
     */
    fun BuiltInThemeData.toThemeProfile(context: Context): ThemeProfile {
        return ThemeProfile(
            id = this.id,
            name = context.getString(this.nameResId),
            description = context.getString(this.descriptionResId),
            lightColors = this.lightColors,
            darkColors = this.darkColors,
            isBuiltIn = this.isBuiltIn
        )
    }

    /**
     * 将资源ID列表转换为ThemeProfile列表
     */
    fun getBuiltInThemes(context: Context): List<ThemeProfile> {
        return builtInThemeResources.map { it.toThemeProfile(context) }
    }

    /**
     * 通过ID查找内置主题资源数据
     */
    fun findBuiltInThemeDataById(id: String): BuiltInThemeData? {
        return builtInThemeResources.find { it.id == id }
    }

    /**
     * 通过ID查找并转换为ThemeProfile
     */
    fun findBuiltInThemeById(context: Context, id: String): ThemeProfile? {
        return findBuiltInThemeDataById(id)?.toThemeProfile(context)
    }
}