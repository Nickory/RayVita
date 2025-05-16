package com.codelab.basiclayouts.ui.viewmodel.home

// 健康数据模型
data class HealthData(
    val heartRate: Int = 76,
    val spO2: Int = 98,
    val hrv: Int = 52,
    val isConnected: Boolean = true
)

// 健康建议模型
data class HealthTip(
    val id: String,
    val message: String,
    val priority: String = "normal", // low, normal, high
    val timestamp: Long = System.currentTimeMillis()
)

// 扫描记录模型
data class RecentScan(
    val id: String,
    val timestamp: Long,
    val heartRate: Int,
    val spO2: Int,
    val hrv: Int = 0,
    val status: String, // success, warning, error
    val duration: Int = 0 // 扫描时长(秒)
)

// 趋势数据模型
data class TrendData(
    val label: String,
    val value: String,
    val change: String, // "+5%", "-2%", etc.
    val isPositive: Boolean,
    val chartData: List<Float> = emptyList() // 可选的图表数据
)

// 主UI状态
data class HomeUIState(
    val healthData: HealthData = HealthData(),
    val healthTips: List<HealthTip> = emptyList(),
    val recentScans: List<RecentScan> = emptyList(),
    val trendData: List<TrendData> = emptyList(),
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val error: String? = null,
    val lastRefreshTime: Long = 0L,
    val isConnected: Boolean = true
)