// ============== HomeViewModel.kt - 使用InsightViewModel真实数据 ==============
package com.codelab.basiclayouts.ui.viewmodel.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.viewmodel.insight.InsightViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 数据模型
data class BannerItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String? = null,
    val actionType: String,
    val actionData: String? = null,
    val isActive: Boolean = true
)

data class HealthData(
    val heartRate: Float = 0f,
    val spO2: Float = 0f,
    val hrv: Float = 0f,
    val heartRateStatus: String = "Unknown",
    val spo2Status: String = "Unknown",
    val hrvStatus: String = "Unknown",
    val lastUpdateTime: String = "Never",
    val measurementCount: Int = 0,
    val confidence: Float = 0f,
    val isConnected: Boolean = false
)

data class HealthTip(
    val id: String,
    val message: String,
    val priority: String = "normal",
    val category: String = "general",
    val timestamp: Long = System.currentTimeMillis()
)

data class BreathingSession(
    val id: String,
    val type: String,
    val duration: Int,
    val isRecommended: Boolean = false,
    val completedToday: Boolean = false
)

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconRes: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: Long? = null,
    val progress: Float = 0f,
    val category: String
)

data class RecentScan(
    val id: String,
    val timestamp: Long,
    val heartRate: Float,
    val spO2: Float,
    val hrv: Float,
    val status: String,
    val confidence: Float,
    val signalQuality: String
)

data class TrendData(
    val label: String,
    val value: String,
    val change: String,
    val isPositive: Boolean,
    val chartData: List<Float> = emptyList()
)

data class HomeUIState(
    val banners: List<BannerItem> = emptyList(),
    val healthData: HealthData = HealthData(),
    val healthTip: HealthTip? = null,
    val breathingSession: BreathingSession? = null,
    val achievements: List<Achievement> = emptyList(),
    val recentScans: List<RecentScan> = emptyList(),
    val trendData: List<TrendData> = emptyList(),
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val error: String? = null,
    val lastRefreshTime: Long = 0L,
    val currentSteps: Int = 0
)

class HomeViewModel(private val context: Context) : ViewModel(), IHomeViewModel {

    // 创建InsightViewModel实例来获取真实数据
    private val insightViewModel = InsightViewModel(context)

    private val _uiState = MutableStateFlow(HomeUIState())
    override val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

    private val dateTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadInitialData()
        observeInsightData()
    }

    private fun observeInsightData() {
        viewModelScope.launch {
            // 分别观察每个数据流，避免复杂的combine类型推断问题
            launch {
                insightViewModel.realTimeHealthStatus.collect { healthStatus ->
                    updateHealthData(healthStatus)
                }
            }

            launch {
                insightViewModel.dailySummary.collect { dailySummary ->
                    updateStepsAndTrends(dailySummary)
                }
            }

            launch {
                insightViewModel.currentSteps.collect { steps ->
                    _uiState.value = _uiState.value.copy(currentSteps = steps)
                }
            }

            launch {
                insightViewModel.measurementsByDate.collect { measurements ->
                    updateRecentScans(measurements)
                }
            }

            launch {
                insightViewModel.aiPrompt.collect { aiPrompt ->
                    updateHealthTip(aiPrompt)
                }
            }
        }
    }

    // 只展示需要修改的关键部分

    private fun loadBanners() {
        val banners = listOf(
            BannerItem(
                id = "banner_1",
                title = context.getString(R.string.home_banner_new_feature_title),
                subtitle = context.getString(R.string.home_banner_new_feature_subtitle),
                actionType = "feature", // 保持不变 - 逻辑标识符
                actionData = "ai_analysis",
                imageUrl = "banner_health.png"
            ),
            BannerItem(
                id = "banner_2",
                title = context.getString(R.string.home_banner_health_challenge_title),
                subtitle = context.getString(R.string.home_banner_health_challenge_subtitle),
                actionType = "activity", // 保持不变 - 逻辑标识符
                actionData = "weekly_challenge",
                imageUrl ="banner_wellness.png"

        ),
            BannerItem(
                id = "banner_3",
                title = context.getString(R.string.home_banner_breathing_training_title),
                subtitle = context.getString(R.string.home_banner_breathing_training_subtitle),
                actionType = "recommendation", // 保持不变 - 逻辑标识符
                actionData = "breathing_training",
                imageUrl = "banner_ai.png"
            )
        )
        _uiState.value = _uiState.value.copy(banners = banners)
    }

    private fun generateAchievements(measurementCount: Int): List<Achievement> {
        return listOf(
            Achievement(
                id = "first_measurement",
                name = context.getString(R.string.home_achievement_first_measurement),
                description = context.getString(R.string.home_achievement_first_measurement_desc),
                iconRes = "ic_first_measurement",
                isUnlocked = measurementCount > 0,
                unlockedDate = if (measurementCount > 0) System.currentTimeMillis() else null,
                category = "measurement" // 保持不变 - 分类标识符
            ),
            Achievement(
                id = "consistent_user",
                name = context.getString(R.string.home_achievement_consistent_user),
                description = context.getString(R.string.home_achievement_consistent_user_desc),
                iconRes = "ic_consistent",
                isUnlocked = measurementCount >= 3,
                progress = (measurementCount / 3f).coerceAtMost(1f),
                category = "consistency" // 保持不变 - 分类标识符
            ),
            Achievement(
                id = "health_expert",
                name = context.getString(R.string.home_achievement_health_expert),
                description = context.getString(R.string.home_achievement_health_expert_desc),
                iconRes = "ic_expert",
                isUnlocked = measurementCount >= 10,
                progress = (measurementCount / 10f).coerceAtMost(1f),
                category = "measurement" // 保持不变 - 分类标识符
            ),
            Achievement(
                id = "data_collector",
                name = context.getString(R.string.home_achievement_data_collector),
                description = context.getString(R.string.home_achievement_data_collector_desc),
                iconRes = "ic_collector",
                isUnlocked = measurementCount >= 20,
                progress = (measurementCount / 20f).coerceAtMost(1f),
                category = "measurement" // 保持不变 - 分类标识符
            )
        )
    }

    // 错误处理中的字符串
    override fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                loadBanners()
                loadBreathingSession()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastRefreshTime = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = context.getString(R.string.home_error_load_data, e.message),
                    isLoading = false
                )
            }
        }
    }

    override fun refreshData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                loadBanners()
                loadBreathingSession()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = context.getString(R.string.home_error_refresh_data, e.message),
                    isLoading = false
                )
            }
        }
    }

    override fun startScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, error = null)

            try {
                kotlinx.coroutines.delay(3000)
                refreshData()
                _uiState.value = _uiState.value.copy(isScanning = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = context.getString(R.string.home_error_scan_failed, e.message),
                    isScanning = false
                )
            }
        }
    }

    // 趋势数据文本本地化
    private fun generateTrendDataFromHealth(healthStatus: com.codelab.basiclayouts.viewmodel.insight.RealTimeHealthStatus): List<TrendData> {
        val trends = mutableListOf<TrendData>()

        if (healthStatus.avgHeartRate > 0) {
            val change = ((healthStatus.currentHeartRate - healthStatus.avgHeartRate) / healthStatus.avgHeartRate * 100).toInt()
            trends.add(
                TrendData(
                    label = context.getString(R.string.home_heart_rate),
                    value = context.getString(R.string.home_bpm_format, healthStatus.currentHeartRate.toInt()),
                    change = "${if (change > 0) "+" else ""}$change%",
                    isPositive = change in -10..10
                )
            )
        }

        if (healthStatus.currentSpO2 > 0) {
            trends.add(
                TrendData(
                    label = context.getString(R.string.home_spo2),
                    value = context.getString(R.string.home_percent_format, healthStatus.currentSpO2.toInt()),
                    change = context.getString(R.string.home_stable),
                    isPositive = healthStatus.currentSpO2 >= 95
                )
            )
        }

        if (healthStatus.currentHRV > 0) {
            val change = if (healthStatus.avgHRV > 0) {
                ((healthStatus.currentHRV - healthStatus.avgHRV) / healthStatus.avgHRV * 100).toInt()
            } else 0
            trends.add(
                TrendData(
                    label = context.getString(R.string.home_hrv),
                    value = context.getString(R.string.home_ms_format, healthStatus.currentHRV.toInt()),
                    change = "${if (change > 0) "+" else ""}$change%",
                    isPositive = change >= 0
                )
            )
        }

        return trends
    }

    private fun updateStepsAndTrends(dailySummary: com.codelab.basiclayouts.viewmodel.insight.DailySummary) {
        val currentTrends = _uiState.value.trendData.toMutableList()

        val stepsIndex = currentTrends.indexOfFirst { it.label == context.getString(R.string.home_steps) }
        val stepsTrend = TrendData(
            label = context.getString(R.string.home_steps),
            value = "${dailySummary.steps}",
            change = if (dailySummary.steps > 8000) {
                context.getString(R.string.home_goal_achieved)
            } else {
                context.getString(R.string.home_keep_going)
            },
            isPositive = dailySummary.steps > 5000
        )

        if (stepsIndex >= 0) {
            currentTrends[stepsIndex] = stepsTrend
        } else {
            currentTrends.add(stepsTrend)
        }

        _uiState.value = _uiState.value.copy(trendData = currentTrends)
    }
    private fun updateHealthData(healthStatus: com.codelab.basiclayouts.viewmodel.insight.RealTimeHealthStatus) {
        val healthData = HealthData(
            heartRate = healthStatus.currentHeartRate,
            spO2 = healthStatus.currentSpO2,
            hrv = healthStatus.currentHRV,
            heartRateStatus = healthStatus.heartRateStatus,
            spo2Status = healthStatus.spo2Status,
            hrvStatus = healthStatus.hrvStatus,
            lastUpdateTime = healthStatus.lastUpdateTime,
            measurementCount = healthStatus.measurementCount,
            confidence = healthStatus.confidence,
            isConnected = healthStatus.signalQuality != "Unknown"
        )

        val trendData = generateTrendDataFromHealth(healthStatus)
        val achievements = generateAchievements(healthStatus.measurementCount)

        _uiState.value = _uiState.value.copy(
            healthData = healthData,
            trendData = trendData,
            achievements = achievements,
            lastRefreshTime = System.currentTimeMillis()
        )
    }

    private fun updateRecentScans(measurements: Map<String, List<com.codelab.basiclayouts.viewmodel.insight.PhysNetMeasurementData>>) {
        val today = dateFormat.format(Date())
        val todayMeasurements = measurements[today] ?: emptyList()
        val recentScans = todayMeasurements.take(5).map { measurement ->
            RecentScan(
                id = measurement.sessionId,
                timestamp = measurement.timestamp,
                heartRate = measurement.heartRate,
                spO2 = measurement.spo2Result?.spo2 ?: 0f,
                hrv = measurement.hrvResult?.rmssd ?: 0f,
                status = when {
                    measurement.confidence > 0.8f -> "success"
                    measurement.confidence > 0.6f -> "warning"
                    else -> "error"
                },
                confidence = measurement.confidence,
                signalQuality = getSignalQualityStatus(measurement.signalQuality)
            )
        }

        _uiState.value = _uiState.value.copy(recentScans = recentScans)
    }

    private fun updateHealthTip(aiPrompt: String) {
        val healthTip = if (aiPrompt.isNotEmpty() && aiPrompt != "Welcome! Start taking measurements to get personalized health insights.") {
            HealthTip(
                id = "ai_tip_${System.currentTimeMillis()}",
                message = aiPrompt,
                priority = determinePriority(aiPrompt),
                category = "ai_generated"
            )
        } else {
            null
        }

        _uiState.value = _uiState.value.copy(healthTip = healthTip)
    }

    private fun getSignalQualityStatus(quality: com.codelab.basiclayouts.viewmodel.insight.SignalQuality?): String {
        if (quality == null) return "Unknown"
        return when {
            quality.overallQuality > 0.8 -> "Excellent"
            quality.overallQuality > 0.6 -> "Good"
            quality.overallQuality > 0.4 -> "Fair"
            else -> "Poor"
        }
    }



    private fun determinePriority(message: String): String {
        return when {
            message.contains("重要", ignoreCase = true) ||
                    message.contains("警告", ignoreCase = true) ||
                    message.contains("注意", ignoreCase = true) ||
                    message.contains("异常", ignoreCase = true) -> "high"
            message.contains("建议", ignoreCase = true) ||
                    message.contains("推荐", ignoreCase = true) -> "normal"
            else -> "low"
        }
    }



    private fun loadBreathingSession() {
        // 基于当前健康状态推荐呼吸训练类型
        val currentHealth = _uiState.value.healthData
        val recommendedType = when {
            currentHealth.heartRate > 90 -> "relax"
            currentHealth.hrvStatus == "Low" -> "focus"
            else -> "energize"
        }

        val session = BreathingSession(
            id = "breathing_${System.currentTimeMillis()}",
            type = recommendedType,
            duration = 5,
            isRecommended = true,
            completedToday = false
        )
        _uiState.value = _uiState.value.copy(breathingSession = session)
    }


    override fun loadHealthData() {
        // 健康数据通过observeInsightData()自动更新，无需手动加载
    }

    override fun loadHealthTips() {
        // 请求AI生成新的健康建议
        insightViewModel.requestInsightPrompt()
    }

    override fun loadRecentScans() {
        // 扫描数据通过observeInsightData()自动更新
    }

    override fun loadTrendData() {
        // 趋势数据通过observeInsightData()自动更新
    }


    override fun checkDeviceConnection() {
        // 设备连接状态通过observeInsightData()自动更新
    }

    override fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // 新增方法
    fun requestAITip() {
        // 直接调用InsightViewModel的AI提示功能
        insightViewModel.requestInsightPrompt()
    }

    fun onBannerClick(banner: BannerItem) {
        when (banner.actionType) {
            "feature" -> {
                // 可以跳转到AI分析页面
            }
            "activity" -> {
                // 可以跳转到挑战页面
            }
            "recommendation" -> {
                // 开始呼吸训练
                _uiState.value.breathingSession?.let { startBreathingSession(it) }
            }
        }
    }

    fun startBreathingSession(session: BreathingSession) {
        viewModelScope.launch {
            // 实现呼吸训练逻辑
            // 可以集成实际的呼吸训练功能
        }
    }

    fun completeBreathingSession() {
        _uiState.value = _uiState.value.copy(
            breathingSession = _uiState.value.breathingSession?.copy(completedToday = true)
        )
        // 完成呼吸训练后建议立即测量
        // 这里可以提示用户进行测量
    }

    override fun onCleared() {
        super.onCleared()
        // 清理InsightViewModel
        insightViewModel.onCleared()
    }
}

// ViewModelFactory
class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

// IHomeViewModel接口
interface IHomeViewModel {
    val uiState: StateFlow<HomeUIState>
    fun loadInitialData()
    fun refreshData()
    fun loadHealthData()
    fun loadHealthTips()
    fun loadRecentScans()
    fun loadTrendData()
    fun startScan()
    fun checkDeviceConnection()
    fun dismissError()
    fun onCleared()
}