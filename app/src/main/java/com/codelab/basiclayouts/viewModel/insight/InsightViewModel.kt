package com.codelab.basiclayouts.viewmodel.insight

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.data.UserSessionManager
import com.codelab.basiclayouts.data.model.HealthMeasurementDto
import com.codelab.basiclayouts.network.RetrofitClient
import com.codelab.basiclayouts.network.model.ChatRequest
import com.codelab.basiclayouts.network.model.ChatResponse
import com.codelab.basiclayouts.network.model.Message
import com.codelab.basiclayouts.utils.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt

@Serializable
data class PhysNetMeasurementData(
    val timestamp: Long,
    val sessionId: String,
    val userId: String? = null,
    val rppgSignal: FloatArray,
    val heartRate: Float,
    val frameCount: Int,
    val processingTimeMs: Int,
    val confidence: Float,
    val hrvResult: HRVResult? = null,
    val spo2Result: SPO2Result? = null,
    val signalQuality: SignalQuality?,
    val syncStatus: String = "pending"
)

@Serializable
data class HRVResult(
    val rmssd: Float,
    val pnn50: Float,
    val sdnn: Float,
    val meanRR: Float,
    val triangularIndex: Float,
    val stressIndex: Float,
    val isValid: Boolean
)

@Serializable
data class SPO2Result(
    val spo2: Float,
    val redAC: Float,
    val redDC: Float,
    val irAC: Float,
    val irDC: Float,
    val ratioOfRatios: Float,
    val confidence: Float,
    val isValid: Boolean
)

@Serializable
data class SignalQuality(
    val snr: Float,
    val motionArtifact: Float,
    val illuminationQuality: Float,
    val overallQuality: Float
)

// Real-time health status derived from actual measurements
data class RealTimeHealthStatus(
    val currentHeartRate: Float = 0f,
    val heartRateStatus: String = "Unknown",
    val avgHeartRate: Float = 0f,
    val maxHeartRate: Float = 0f,
    val minHeartRate: Float = 0f,
    val currentHRV: Float = 0f,
    val hrvStatus: String = "Unknown",
    val avgHRV: Float = 0f,
    val currentSpO2: Float = 0f,
    val spo2Status: String = "Unknown",
    val signalQuality: String = "Unknown",
    val measurementCount: Int = 0,
    val lastUpdateTime: String = "Never",
    val confidence: Float = 0f
)

data class DailySummary(
    val avgHeartRate: Float = 0f,
    val maxHeartRate: Float = 0f,
    val minHeartRate: Float = 0f,
    val measurementCount: Int = 0,
    val steps: Int = 0,
    val avgHRV: Float = 0f,
    val avgSpO2: Float = 0f
)

data class ActivityData(
    val day: String,
    val percentage: Float,
    val steps: Int,
    val isToday: Boolean
)

data class SyncResult(
    val uploadedCount: Int = 0,
    val downloadedCount: Int = 0,
    val message: String = ""
)

// AI Tip Mode enumeration
enum class AITipMode(val displayName: String, val promptSuffix: String) {
    DIAGNOSTIC("Diagnostic Advice", "provide diagnostic insights and health recommendations based on the data"),
    COMPREHENSIVE("Comprehensive Advice", "provide comprehensive health guidance covering all aspects"),
    EXERCISE("Exercise Advice", "provide exercise and fitness recommendations"),
    LIFESTYLE("Lifestyle Advice", "provide lifestyle and wellness recommendations"),
    PREVENTION("Prevention Tips", "provide preventive health tips and early warning advice")
}

@HiltViewModel
class InsightViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel(), SensorEventListener {
    private val appContext = context.applicationContext
    private val userSessionManager = UserSessionManager(appContext)
    private val userId: Int? get() = userSessionManager.getUserSession()?.user_id

    // SharedPreferences for step counting persistence
    private val stepPrefs: SharedPreferences = appContext.getSharedPreferences("step_counter", Context.MODE_PRIVATE)

    // Sensor management for step counting
    private val sensorManager: SensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    // Step counting variables
    private var deviceBootSteps = 0
    private var todayBaseSteps = 0
    private var hasInitialStepCount = false
    private var lastSavedDate = ""

    // Real-time health status from actual measurements
    private val _realTimeHealthStatus = MutableStateFlow(RealTimeHealthStatus())
    val realTimeHealthStatus: StateFlow<RealTimeHealthStatus> = _realTimeHealthStatus.asStateFlow()

    private val _aiTipMode = MutableStateFlow(AITipMode.COMPREHENSIVE)
    val aiTipMode: StateFlow<AITipMode> = _aiTipMode.asStateFlow()

    private val _aiPrompt = MutableStateFlow("Welcome! Start taking measurements to get personalized health insights.")
    val aiPrompt: StateFlow<String> = _aiPrompt.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _todayDate = MutableStateFlow(SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()))
    val todayDate: StateFlow<String> = _todayDate.asStateFlow()

    private val _batteryLevel = MutableStateFlow(75)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _dailySummary = MutableStateFlow(DailySummary())
    val dailySummary: StateFlow<DailySummary> = _dailySummary.asStateFlow()

    private val _activityData = MutableStateFlow<List<ActivityData>>(emptyList())
    val activityData: StateFlow<List<ActivityData>> = _activityData.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _measurementsByDate = MutableStateFlow<Map<String, List<PhysNetMeasurementData>>>(emptyMap())
    val measurementsByDate: StateFlow<Map<String, List<PhysNetMeasurementData>>> = _measurementsByDate.asStateFlow()

    private val _syncResult = MutableStateFlow<SyncResult?>(null)
    val syncResult: StateFlow<SyncResult?> = _syncResult.asStateFlow()

    // Step counting with permission check
    private val _currentSteps = MutableStateFlow(0)
    val currentSteps: StateFlow<Int> = _currentSteps.asStateFlow()

    private val _stepPermissionRequired = MutableStateFlow(false)
    val stepPermissionRequired: StateFlow<Boolean> = _stepPermissionRequired.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val json = Json { encodeDefaults = true }

    init {
        loadMeasurements()
        initializeStepCounting()
        generateRealActivityData()

        viewModelScope.launch {
            delay(1000)
            if (userId == null) {
                _errorMessage.value = "Please login to sync data"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
        saveStepCountingState()
    }

    // 改进的步数统计初始化
    private fun initializeStepCounting() {
        val today = dateFormat.format(Date())

        // 检查是否需要权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                appContext,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                _stepPermissionRequired.value = true
                // 使用保存的步数或估算值
                _currentSteps.value = stepPrefs.getInt("steps_$today", (2000..5000).random())
                return
            }
        }

        // 加载保存的步数状态
        loadStepCountingState(today)

        // 启动传感器监听
        startStepCounting()
    }

    private fun loadStepCountingState(today: String) {
        lastSavedDate = stepPrefs.getString("last_date", "") ?: ""

        if (lastSavedDate == today) {
            // 同一天，加载保存的状态
            deviceBootSteps = stepPrefs.getInt("device_boot_steps", 0)
            todayBaseSteps = stepPrefs.getInt("today_base_steps", 0)
            _currentSteps.value = stepPrefs.getInt("current_steps", 0)
            hasInitialStepCount = deviceBootSteps > 0
        } else {
            // 新的一天，重置计数
            resetDailyStepCount(today)
        }
    }

    private fun resetDailyStepCount(today: String) {
        deviceBootSteps = 0
        todayBaseSteps = 0
        hasInitialStepCount = false
        lastSavedDate = today
        _currentSteps.value = 0

        // 保存新的状态
        stepPrefs.edit().apply {
            putString("last_date", today)
            putInt("device_boot_steps", 0)
            putInt("today_base_steps", 0)
            putInt("current_steps", 0)
            apply()
        }
    }

    private fun saveStepCountingState() {
        if (lastSavedDate.isNotEmpty()) {
            stepPrefs.edit().apply {
                putString("last_date", lastSavedDate)
                putInt("device_boot_steps", deviceBootSteps)
                putInt("today_base_steps", todayBaseSteps)
                putInt("current_steps", _currentSteps.value)
                apply()
            }
        }
    }

    // 改进的步数传感器启动
    private fun startStepCounting() {
        when {
            stepCounterSensor != null -> {
                // 优先使用步数计数器，使用合适的传感器延迟
                val success = sensorManager.registerListener(
                    this,
                    stepCounterSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                if (!success) {
                    Log.w("InsightViewModel", "Failed to register step counter sensor")
                    fallbackToEstimatedSteps()
                }
            }
            stepDetectorSensor != null -> {
                // 备用：使用步数检测器
                val success = sensorManager.registerListener(
                    this,
                    stepDetectorSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                if (!success) {
                    Log.w("InsightViewModel", "Failed to register step detector sensor")
                    fallbackToEstimatedSteps()
                }
            }
            else -> {
                Log.w("InsightViewModel", "No step sensors available")
                fallbackToEstimatedSteps()
            }
        }
    }

    private fun fallbackToEstimatedSteps() {
        // 使用基于测量活动的估算步数
        viewModelScope.launch {
            val measurements = _measurementsByDate.value
            val today = dateFormat.format(Date())
            val todayMeasurements = measurements[today] ?: emptyList()

            val estimatedSteps = if (todayMeasurements.isNotEmpty()) {
                // 基于测量次数估算：每次测量约300-800步
                (todayMeasurements.size * (300..800).random()) + (1000..3000).random()
            } else {
                stepPrefs.getInt("steps_$today", (2000..5000).random())
            }

            _currentSteps.value = estimatedSteps.coerceIn(0, 15000)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                handleStepCounterEvent(event)
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                handleStepDetectorEvent()
            }
        }
    }

    private fun handleStepCounterEvent(event: SensorEvent) {
        val currentBootSteps = event.values[0].toInt()

        if (!hasInitialStepCount) {
            deviceBootSteps = currentBootSteps
            todayBaseSteps = _currentSteps.value // 保持已有的步数
            hasInitialStepCount = true
            saveStepCountingState()
            return
        }

        // 计算从设备启动以来的新增步数
        val newStepsSinceBoot = currentBootSteps - deviceBootSteps
        val totalDailySteps = todayBaseSteps + newStepsSinceBoot

        // 合理性检查：一天不超过50000步
        if (totalDailySteps <= 50000) {
            _currentSteps.value = totalDailySteps
            updateDailySummaryWithSteps(totalDailySteps)

            // 定期保存状态（每100步保存一次）
            if (totalDailySteps % 100 == 0) {
                saveStepCountingState()
            }
        }
    }

    private fun handleStepDetectorEvent() {
        // 步数检测器：每检测到一步就触发一次
        val currentSteps = _currentSteps.value + 1
        if (currentSteps <= 50000) { // 合理性检查
            _currentSteps.value = currentSteps
            updateDailySummaryWithSteps(currentSteps)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 监控传感器精度变化
        when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                Log.w("InsightViewModel", "Step sensor accuracy unreliable")
            }
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                Log.w("InsightViewModel", "Step sensor accuracy low")
            }
        }
    }

    // 权限授予后的回调
    fun onStepPermissionGranted() {
        _stepPermissionRequired.value = false
        initializeStepCounting()
    }

    // 手动步数调整（用于测试或校准）
    fun adjustStepCount(adjustment: Int) {
        val newSteps = (_currentSteps.value + adjustment).coerceIn(0, 50000)
        _currentSteps.value = newSteps
        updateDailySummaryWithSteps(newSteps)
        saveStepCountingState()
    }

    private fun loadMeasurements() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dir = File(appContext.filesDir, "measurements")
                if (!dir.exists()) dir.mkdirs()
                val files = dir.listFiles { _, name -> name.endsWith(".json") }?.toList() ?: emptyList()
                val measurements = files.mapNotNull { file ->
                    try {
                        val jsonString = file.readText()
                        val measurement = json.decodeFromString<PhysNetMeasurementData>(jsonString)
                        measurement.copy(
                            userId = (userId ?: measurement.userId).toString(),
                            syncStatus = measurement.syncStatus.takeIf { it.isNotEmpty() } ?: "pending"
                        )
                    } catch (e: Exception) {
                        Log.e("InsightViewModel", "Failed to parse JSON file: ${file.name}", e)
                        null
                    }
                }
                val grouped = measurements.groupBy { dateFormat.format(Date(it.timestamp)) }
                    .mapValues { it.value.sortedByDescending { m -> m.timestamp } }
                _measurementsByDate.value = grouped

                // Update real-time status and daily summary from actual measurements
                updateRealTimeHealthStatus(measurements)
                updateDailySummaryFromMeasurements(measurements)

                if (measurements.size < files.size) {
                    Log.w("InsightViewModel", "${files.size - measurements.size} files failed to parse")
                    _errorMessage.value = "Failed to load ${files.size - measurements.size} measurement files"
                }
            } catch (e: Exception) {
                Log.e("InsightViewModel", "Failed to load measurements directory", e)
                _errorMessage.value = "Error loading measurement data"
            }
        }
    }

    private fun updateRealTimeHealthStatus(measurements: List<PhysNetMeasurementData>) {
        if (measurements.isEmpty()) {
            _realTimeHealthStatus.value = RealTimeHealthStatus()
            return
        }

        // Get today's measurements
        val today = dateFormat.format(Date())
        val todayMeasurements = measurements.filter {
            dateFormat.format(Date(it.timestamp)) == today
        }

        if (todayMeasurements.isEmpty()) {
            // Use latest available measurement
            val latest = measurements.maxByOrNull { it.timestamp }
            if (latest != null) {
                _realTimeHealthStatus.value = createHealthStatusFromSingle(latest)
            }
            return
        }

        val latest = todayMeasurements.maxByOrNull { it.timestamp }!!
        val heartRates = todayMeasurements.map { it.heartRate }
        val hrvValues = todayMeasurements.mapNotNull { it.hrvResult?.rmssd }
        val spo2Values = todayMeasurements.mapNotNull { it.spo2Result?.spo2 }

        _realTimeHealthStatus.value = RealTimeHealthStatus(
            currentHeartRate = latest.heartRate,
            heartRateStatus = getHeartRateStatus(latest.heartRate),
            avgHeartRate = heartRates.average().toFloat(),
            maxHeartRate = heartRates.maxOrNull() ?: 0f,
            minHeartRate = heartRates.minOrNull() ?: 0f,
            currentHRV = latest.hrvResult?.rmssd ?: 0f,
            hrvStatus = getHRVStatus(latest.hrvResult?.rmssd ?: 0f),
            avgHRV = if (hrvValues.isNotEmpty()) hrvValues.average().toFloat() else 0f,
            currentSpO2 = latest.spo2Result?.spo2 ?: 0f,
            spo2Status = getSpO2Status(latest.spo2Result?.spo2 ?: 0f),
            signalQuality = getSignalQualityStatus(latest.signalQuality),
            measurementCount = todayMeasurements.size,
            lastUpdateTime = timeFormat.format(Date(latest.timestamp)),
            confidence = latest.confidence
        )
    }

    private fun createHealthStatusFromSingle(measurement: PhysNetMeasurementData): RealTimeHealthStatus {
        return RealTimeHealthStatus(
            currentHeartRate = measurement.heartRate,
            heartRateStatus = getHeartRateStatus(measurement.heartRate),
            avgHeartRate = measurement.heartRate,
            maxHeartRate = measurement.heartRate,
            minHeartRate = measurement.heartRate,
            currentHRV = measurement.hrvResult?.rmssd ?: 0f,
            hrvStatus = getHRVStatus(measurement.hrvResult?.rmssd ?: 0f),
            avgHRV = measurement.hrvResult?.rmssd ?: 0f,
            currentSpO2 = measurement.spo2Result?.spo2 ?: 0f,
            spo2Status = getSpO2Status(measurement.spo2Result?.spo2 ?: 0f),
            signalQuality = getSignalQualityStatus(measurement.signalQuality),
            measurementCount = 1,
            lastUpdateTime = timeFormat.format(Date(measurement.timestamp)),
            confidence = measurement.confidence
        )
    }

    private fun updateDailySummaryFromMeasurements(measurements: List<PhysNetMeasurementData>) {
        val today = dateFormat.format(Date())
        val todayMeasurements = measurements.filter {
            dateFormat.format(Date(it.timestamp)) == today
        }

        if (todayMeasurements.isEmpty()) {
            _dailySummary.value = DailySummary(steps = _currentSteps.value)
            return
        }

        val heartRates = todayMeasurements.map { it.heartRate }
        val hrvValues = todayMeasurements.mapNotNull { it.hrvResult?.rmssd }
        val spo2Values = todayMeasurements.mapNotNull { it.spo2Result?.spo2 }

        _dailySummary.value = DailySummary(
            avgHeartRate = heartRates.average().toFloat(),
            maxHeartRate = heartRates.maxOrNull() ?: 0f,
            minHeartRate = heartRates.minOrNull() ?: 0f,
            measurementCount = todayMeasurements.size,
            steps = _currentSteps.value,
            avgHRV = if (hrvValues.isNotEmpty()) hrvValues.average().toFloat() else 0f,
            avgSpO2 = if (spo2Values.isNotEmpty()) spo2Values.average().toFloat() else 0f
        )
    }

    private fun updateDailySummaryWithSteps(steps: Int) {
        val current = _dailySummary.value
        _dailySummary.value = current.copy(steps = steps)
    }

    private fun generateRealActivityData() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val activity = mutableListOf<ActivityData>()

            for (i in 6 downTo 0) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -i)
                val date = calendar.time
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

                // Get real measurements for this day
                val dayKey = dateFormat.format(date)
                val dayMeasurements = _measurementsByDate.value[dayKey] ?: emptyList()

                val steps = if (i == 0) {
                    _currentSteps.value // Today's real steps
                } else {
                    // Estimate steps based on measurement activity
                    if (dayMeasurements.isNotEmpty()) {
                        (dayMeasurements.size * 500) + (2000..6000).random()
                    } else {
                        (1000..4000).random()
                    }
                }

                activity.add(
                    ActivityData(
                        day = dayFormat.format(date),
                        percentage = (steps / 10000f).coerceAtMost(1f),
                        steps = steps,
                        isToday = i == 0
                    )
                )
            }

            _activityData.value = activity
        }
    }

    // Status calculation methods
    private fun getHeartRateStatus(hr: Float): String {
        return when {
            hr < 60 -> "Low"
            hr > 100 -> "High"
            else -> "Normal"
        }
    }

    private fun getHRVStatus(hrv: Float): String {
        return when {
            hrv < 20 -> "Low"
            hrv > 50 -> "Good"
            hrv > 80 -> "Excellent"
            else -> "Normal"
        }
    }

    private fun getSpO2Status(spo2: Float): String {
        return when {
            spo2 < 95 -> "Low"
            spo2 >= 98 -> "Excellent"
            else -> "Normal"
        }
    }

    private fun getSignalQualityStatus(quality: SignalQuality?): String {
        if (quality == null) return "Unknown"

        return when {
            quality.overallQuality > 0.8 -> "Excellent"
            quality.overallQuality > 0.6 -> "Good"
            quality.overallQuality > 0.4 -> "Fair"
            else -> "Poor"
        }
    }

    fun setAITipMode(mode: AITipMode) {
        _aiTipMode.value = mode
    }

    fun requestInsightPrompt() {
        val healthStatus = _realTimeHealthStatus.value
        val dailySummary = _dailySummary.value
        val currentMode = _aiTipMode.value

        val summaryData = buildString {
            append("Health Status Summary: ")

            if (healthStatus.measurementCount > 0) {
                append("Current Heart Rate: ${healthStatus.currentHeartRate.roundToInt()} BPM (${healthStatus.heartRateStatus}), ")
                append("Avg HR: ${healthStatus.avgHeartRate.roundToInt()} BPM, ")
                append("HR Range: ${healthStatus.minHeartRate.roundToInt()}-${healthStatus.maxHeartRate.roundToInt()} BPM, ")

                if (healthStatus.currentHRV > 0) {
                    append("Current HRV: ${healthStatus.currentHRV.roundToInt()} ms (${healthStatus.hrvStatus}), ")
                    append("Avg HRV: ${healthStatus.avgHRV.roundToInt()} ms, ")
                }

                if (healthStatus.currentSpO2 > 0) {
                    append("SpO2: ${healthStatus.currentSpO2.roundToInt()}% (${healthStatus.spo2Status}), ")
                }

                append("Signal Quality: ${healthStatus.signalQuality}, ")
                append("Measurements Today: ${healthStatus.measurementCount}, ")
                append("Confidence: ${(healthStatus.confidence * 100).roundToInt()}%, ")
            } else {
                append("No measurements available today, ")
            }

            append("Steps: ${dailySummary.steps}, ")
            append("Last Update: ${healthStatus.lastUpdateTime}")
        }

        val content = "Based on this user's health data: $summaryData. Please ${currentMode.promptSuffix} in exactly 50 words. Focus on actionable insights."
        val messages = listOf(Message("user", content))
        val request = ChatRequest(model = "deepseek-chat", messages = messages)

        _isLoading.value = true
        _errorMessage.value = null

        RetrofitClient.deepseekApi.chatCompletion(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                viewModelScope.launch {
                    delay(800)
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val reply = response.body()?.choices?.firstOrNull()?.message?.content
                        _aiPrompt.value = reply ?: getDefaultTipForMode(currentMode)
                    } else {
                        _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                        _aiPrompt.value = getDefaultTipForMode(currentMode)
                    }
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                viewModelScope.launch {
                    delay(500)
                    _isLoading.value = false
                    _errorMessage.value = "Network error: ${t.localizedMessage}"
                    _aiPrompt.value = getDefaultTipForMode(currentMode)
                }
            }
        })
    }

    private fun getDefaultTipForMode(mode: AITipMode): String {
        return when (mode) {
            AITipMode.DIAGNOSTIC -> "Regular monitoring helps establish baseline values. Track patterns over time for better health insights."
            AITipMode.COMPREHENSIVE -> "Maintain consistent measurement schedule. Balance heart rate monitoring with adequate rest and hydration."
            AITipMode.EXERCISE -> "Monitor heart rate during exercise. Aim for 50-85% of maximum heart rate for effective cardio training."
            AITipMode.LIFESTYLE -> "Quality sleep and stress management significantly impact heart rate variability. Prioritize recovery periods."
            AITipMode.PREVENTION -> "Early detection through regular monitoring. Unusual patterns may warrant healthcare consultation."
        }
    }

    // Keep existing methods for file operations and sync
    fun getAvailableDates(): List<Date> {
        return _measurementsByDate.value.keys.mapNotNull { key ->
            try {
                dateFormat.parse(key)
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.time }
    }

    fun getMeasurementsForDate(date: Date): List<PhysNetMeasurementData> {
        val dateKey = dateFormat.format(date)
        return _measurementsByDate.value[dateKey] ?: emptyList()
    }

    fun saveMeasurement(measurement: PhysNetMeasurementData) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedMeasurement = measurement.copy(
                    userId = measurement.userId ?: userId?.toString() ?: "unknown"
                )
                val dir = File(appContext.filesDir, "measurements")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "${updatedMeasurement.sessionId}.json")
                val jsonString = json.encodeToString(PhysNetMeasurementData.serializer(), updatedMeasurement)
                file.writeText(jsonString)
                loadMeasurements() // This will update all derived states
            } catch (e: Exception) {
                Log.e("InsightViewModel", "Failed to save measurement: ${measurement.sessionId}", e)
                _errorMessage.value = "Error saving measurement"
            }
        }
    }

    fun updateBatteryLevel(level: Int) {
        _batteryLevel.value = level.coerceIn(0, 100)
    }

    // Sync functionality remains the same
    fun syncWithCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!NetworkUtil.isOnline(appContext)) {
                Log.w("Sync", "No network connection")
                _errorMessage.value = "No network connection"
                _syncResult.value = SyncResult(message = "No network connection")
                return@launch
            }
            val currentUserId = userId
            if (currentUserId == null) {
                Log.e("Sync", "User not logged in")
                _errorMessage.value = "Please login to sync data"
                _syncResult.value = SyncResult(message = "Please login to sync data")
                return@launch
            }
            _isLoading.value = true
            try {
                val healthApi = RetrofitClient.getHealthApi(appContext)
                val measurements = _measurementsByDate.value.values.flatten()
                val toSync = measurements.filter { it.syncStatus == "pending" || it.syncStatus == "failed" }
                val syncedSessionIds = mutableListOf<String>()

                for (measurement in toSync) {
                    try {
                        val dto = toHealthMeasurementDto(measurement)
                        val response = healthApi.createMeasurement(dto)
                        if (response.body()?.get("msg") == "measurement created") {
                            updateMeasurementSyncStatus(measurement.sessionId, "synced")
                            syncedSessionIds.add(measurement.sessionId)
                        } else if (response.body()?.get("msg") == "sessionId already exists") {
                            val newMeasurement = measurement.copy(sessionId = UUID.randomUUID().toString())
                            saveMeasurement(newMeasurement)
                            val newDto = toHealthMeasurementDto(newMeasurement)
                            val newResponse = healthApi.createMeasurement(newDto)
                            if (newResponse.body()?.get("msg") == "measurement created") {
                                updateMeasurementSyncStatus(newMeasurement.sessionId, "synced")
                                syncedSessionIds.add(newMeasurement.sessionId)
                            } else {
                                updateMeasurementSyncStatus(newMeasurement.sessionId, "failed")
                                Log.e("Sync", "Retry upload failed ${newMeasurement.sessionId}")
                            }
                        }
                    } catch (e: Exception) {
                        updateMeasurementSyncStatus(measurement.sessionId, "failed")
                        Log.e("Sync", "Upload measurement failed ${measurement.sessionId}", e)
                    }
                }

                if (syncedSessionIds.isNotEmpty()) {
                    try {
                        val syncResponse = healthApi.syncMeasurements(mapOf("sessionIds" to syncedSessionIds))
                        if (!syncResponse.isSuccessful) {
                            Log.e("Sync", "Sync notification failed: ${syncResponse.code()} - ${syncResponse.message()}")
                        }
                    } catch (e: Exception) {
                        Log.e("Sync", "Sync notification failed", e)
                    }
                }

                var downloadedCount = 0
                try {
                    val cloudMeasurements = healthApi.getPendingMeasurements(currentUserId.toString())
                    for (dto in cloudMeasurements) {
                        val measurement = toPhysNetMeasurementData(dto)
                        saveMeasurement(measurement)
                        downloadedCount++
                    }
                } catch (e: Exception) {
                    Log.e("Sync", "Download cloud data failed", e)
                    _errorMessage.value = "Data upload successful, but cloud download failed: ${e.message}"
                    _syncResult.value = SyncResult(
                        uploadedCount = syncedSessionIds.size,
                        downloadedCount = downloadedCount,
                        message = "Data upload successful, but cloud download failed: ${e.message}"
                    )
                    _isLoading.value = false
                    return@launch
                }

                _errorMessage.value = "Sync successful"
                _syncResult.value = SyncResult(
                    uploadedCount = syncedSessionIds.size,
                    downloadedCount = downloadedCount,
                    message = "Sync successful"
                )
            } catch (e: Exception) {
                Log.e("Sync", "Sync failed", e)
                _errorMessage.value = "Sync failed: ${e.message}"
                _syncResult.value = SyncResult(
                    uploadedCount = 0,
                    downloadedCount = 0,
                    message = "Sync failed: ${e.message}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateMeasurementSyncStatus(sessionId: String, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(appContext.filesDir, "measurements/$sessionId.json")
                if (!file.exists()) return@launch
                val jsonString = file.readText()
                val measurement = json.decodeFromString<PhysNetMeasurementData>(jsonString)
                val updated = measurement.copy(syncStatus = newStatus)
                file.writeText(json.encodeToString(PhysNetMeasurementData.serializer(), updated))
                loadMeasurements()
            } catch (e: Exception) {
                Log.e("InsightViewModel", "Update sync status failed: $sessionId", e)
            }
        }
    }

    private fun toHealthMeasurementDto(measurement: PhysNetMeasurementData): HealthMeasurementDto {
        return HealthMeasurementDto(
            sessionId = measurement.sessionId,
            userId = measurement.userId ?: userId?.toString() ?: "unknown",
            timestamp = measurement.timestamp,
            heartRate = measurement.heartRate,
            rppgSignal = measurement.rppgSignal.toList(),
            frameCount = measurement.frameCount,
            processingTimeMs = measurement.processingTimeMs,
            confidence = measurement.confidence,
            hrvResult = measurement.hrvResult,
            spo2Result = measurement.spo2Result,
            signalQuality = measurement.signalQuality,
            createdAt = dateTimeFormat.format(Date(measurement.timestamp)),
            updatedAt = dateTimeFormat.format(Date()),
            syncStatus = measurement.syncStatus
        )
    }

    private fun toPhysNetMeasurementData(dto: HealthMeasurementDto): PhysNetMeasurementData {
        return PhysNetMeasurementData(
            timestamp = dto.timestamp,
            sessionId = dto.sessionId ?: UUID.randomUUID().toString(),
            userId = dto.userId,
            rppgSignal = dto.rppgSignal.toFloatArray(),
            heartRate = dto.heartRate ?: 0f,
            frameCount = dto.frameCount ?: 0,
            processingTimeMs = dto.processingTimeMs ?: 0,
            confidence = dto.confidence ?: 0f,
            hrvResult = dto.hrvResult,
            spo2Result = dto.spo2Result,
            signalQuality = dto.signalQuality,
            syncStatus = dto.syncStatus ?: "synced"
        )
    }
}

class InsightViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InsightViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InsightViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}