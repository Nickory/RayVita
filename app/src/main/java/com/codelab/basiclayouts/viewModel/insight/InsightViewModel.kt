package com.codelab.basiclayouts.viewmodel.insight

import android.content.Context
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
import java.util.Random
import java.util.UUID
import javax.inject.Inject

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

data class HealthRecord(
    val date: Date,
    val heartRate: Int,
    val steps: Int,
    val calories: Int,
    val sleepHours: Int,
    val signalQuality: String
)

data class DailySummary(
    val avgHeartRate: Int = 75,
    val maxHeartRate: Int = 120,
    val steps: Int = 8500,
    val recovery: Int = 73
)

data class ActivityData(
    val day: String,
    val percentage: Float,
    val steps: Int,
    val isToday: Boolean
)

// 新增：同步结果数据类
data class SyncResult(
    val uploadedCount: Int = 0,
    val downloadedCount: Int = 0,
    val message: String = ""
)

@HiltViewModel
class InsightViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val appContext = context.applicationContext

    private val userSessionManager = UserSessionManager(appContext)
    private val userId: Int? get() = userSessionManager.getUserSession()?.user_id

    private val _heartRate = MutableStateFlow(72)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    private val _signalQuality = MutableStateFlow("Good")
    val signalQuality: StateFlow<String> = _signalQuality.asStateFlow()

    private val _aiPrompt = MutableStateFlow("Try to keep your heart rate within a healthy range (60-100 bpm) for your age during rest periods.")
    val aiPrompt: StateFlow<String> = _aiPrompt.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _todayDate = MutableStateFlow(SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()))
    val todayDate: StateFlow<String> = _todayDate.asStateFlow()

    private val _batteryLevel = MutableStateFlow(75)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _healthRecords = MutableStateFlow<List<HealthRecord>>(emptyList())
    val healthRecords: StateFlow<List<HealthRecord>> = _healthRecords.asStateFlow()

    private val _dailySummary = MutableStateFlow(DailySummary())
    val dailySummary: StateFlow<DailySummary> = _dailySummary.asStateFlow()

    private val _activityData = MutableStateFlow<List<ActivityData>>(emptyList())
    val activityData: StateFlow<List<ActivityData>> = _activityData.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _measurementsByDate = MutableStateFlow<Map<String, List<PhysNetMeasurementData>>>(emptyMap())
    val measurementsByDate: StateFlow<Map<String, List<PhysNetMeasurementData>>> = _measurementsByDate.asStateFlow()

    // 新增：同步结果状态
    private val _syncResult = MutableStateFlow<SyncResult?>(null)
    val syncResult: StateFlow<SyncResult?> = _syncResult.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private val json = Json { encodeDefaults = true }

    init {
        generateSampleData()
//        startSimulation()
        loadMeasurements()
        viewModelScope.launch {
            delay(1000)
            if (userId == null) {
                _errorMessage.value = "请登录以同步数据"
            }
        }
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
                        Log.e("InsightViewModel", "解析 JSON 文件失败: ${file.name}", e)
                        null
                    }
                }
                val grouped = measurements.groupBy { dateFormat.format(Date(it.timestamp)) }
                    .mapValues { it.value.sortedByDescending { m -> m.timestamp } }
                _measurementsByDate.value = grouped
                if (measurements.size < files.size) {
                    Log.w("InsightViewModel", "${files.size - measurements.size} 个文件解析失败")
                    _errorMessage.value = "无法加载 ${files.size - measurements.size} 个测量文件"
                }
            } catch (e: Exception) {
                Log.e("InsightViewModel", "加载测量目录失败", e)
                _errorMessage.value = "加载测量数据错误"
            }
        }
    }

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
                loadMeasurements()
            } catch (e: Exception) {
                Log.e("InsightViewModel", "Failed to save measurement: ${measurement.sessionId}", e)
                _errorMessage.value = "Error saving measurement"
            }
        }
    }
    fun fixExistingMeasurementFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dir = File(appContext.filesDir, "measurements")
                val files = dir.listFiles { _, name -> name.endsWith(".json") }?.toList() ?: return@launch
                files.forEach { file ->
                    try {
                        val jsonString = file.readText()
                        val measurement = json.decodeFromString<PhysNetMeasurementData>(jsonString)
                        if (measurement.userId == null) {
                            val updated = measurement.copy(
                                userId = userId?.toString() ?: "unknown"
                            )
                            file.writeText(json.encodeToString(PhysNetMeasurementData.serializer(), updated))
                        }
                    } catch (e: Exception) {
                        Log.e("InsightViewModel", "Failed to fix file: ${file.name}", e)
                    }
                }
                loadMeasurements()
            } catch (e: Exception) {
                Log.e("InsightViewModel", "Failed to fix JSON files", e)
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
                Log.e("InsightViewModel", "更新同步状态失败: $sessionId", e)
            }
        }
    }


    fun syncWithCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!NetworkUtil.isOnline(appContext)) {
                Log.w("Sync", "无网络连接")
                _errorMessage.value = "无网络连接"
                _syncResult.value = SyncResult(message = "无网络连接")
                return@launch
            }
            val currentUserId = userId
            if (currentUserId == null) {
                Log.e("Sync", "用户未登录")
                _errorMessage.value = "请登录以同步数据"
                _syncResult.value = SyncResult(message = "请登录以同步数据")
                return@launch
            }
            _isLoading.value = true
            try {
                val healthApi = RetrofitClient.getHealthApi(appContext)
                // 步骤 1：上传本地 pending/failed 数据
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
                                Log.e("Sync", "重试上传失败 ${newMeasurement.sessionId}")
                            }
                        }
                    } catch (e: Exception) {
                        updateMeasurementSyncStatus(measurement.sessionId, "failed")
                        Log.e("Sync", "上传测量失败 ${measurement.sessionId}", e)
                    }
                }

                // 步骤 2：通知云端已同步的 sessionIds
                if (syncedSessionIds.isNotEmpty()) {
                    try {
                        val syncResponse = healthApi.syncMeasurements(mapOf("sessionIds" to syncedSessionIds))
                        if (!syncResponse.isSuccessful) {
                            Log.e("Sync", "通知同步失败: ${syncResponse.code()} - ${syncResponse.message()}")
                        }
                    } catch (e: Exception) {
                        Log.e("Sync", "通知同步失败", e)
                    }
                }

                // 步骤 3：下载云端数据
                var downloadedCount = 0
                try {
                    val cloudMeasurements = healthApi.getPendingMeasurements(currentUserId.toString())
                    for (dto in cloudMeasurements) {
                        val measurement = toPhysNetMeasurementData(dto)
                        saveMeasurement(measurement)
                        downloadedCount++
                    }
                } catch (e: Exception) {
                    Log.e("Sync", "下载云数据失败", e)
                    _errorMessage.value = "数据上传成功，但下载云数据失败: ${e.message}"
                    _syncResult.value = SyncResult(
                        uploadedCount = syncedSessionIds.size,
                        downloadedCount = downloadedCount,
                        message = "数据上传成功，但下载云数据失败: ${e.message}"
                    )
                    _isLoading.value = false
                    return@launch
                }

                _errorMessage.value = "同步成功"
                _syncResult.value = SyncResult(
                    uploadedCount = syncedSessionIds.size,
                    downloadedCount = downloadedCount,
                    message = "同步成功"
                )
            } catch (e: Exception) {
                Log.e("Sync", "同步失败", e)
                _errorMessage.value = "同步失败: ${e.message}"
                _syncResult.value = SyncResult(
                    uploadedCount = 0,
                    downloadedCount = 0,
                    message = "同步失败: ${e.message}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun toHealthMeasurementDto(measurement: PhysNetMeasurementData): HealthMeasurementDto {
        return HealthMeasurementDto(
            sessionId = measurement.sessionId,
            userId = measurement.userId ?: userId?.toString() ?: "unknown", // 修复类型不匹配
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

    fun updateHeartRate(hr: Int) {
        _heartRate.value = hr
    }

    fun updateSignalQuality(quality: String) {
        _signalQuality.value = quality
    }

    fun updateBatteryLevel(level: Int) {
        _batteryLevel.value = level.coerceIn(0, 100)
    }

    fun updateHealthRecord(record: HealthRecord) {
        _healthRecords.value = (_healthRecords.value + record).sortedBy { it.date }
    }

    fun updateDailySummary(summary: DailySummary) {
        _dailySummary.value = summary
    }

    fun updateActivityData(data: List<ActivityData>) {
        _activityData.value = data
    }

    fun getSelectedRecord(date: Date): StateFlow<HealthRecord?> {
        val flow = MutableStateFlow<HealthRecord?>(null)
        viewModelScope.launch {
            flow.value = _healthRecords.value.find { it.date.time == date.time }
        }
        return flow.asStateFlow()
    }

    fun requestInsightPrompt(summaryData: String) {
        val content = "Based on user today $summaryData, provide a 50-word useful health tip."
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
                        _aiPrompt.value = reply ?: "保持规律运动，监测心率以优化心血管健康。"
                    } else {
                        _errorMessage.value = "错误: ${response.code()} - ${response.message()}"
                        _aiPrompt.value = "保持规律运动，监测心率以优化心血管健康。"
                    }
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                viewModelScope.launch {
                    delay(500)
                    _isLoading.value = false
                    _errorMessage.value = "网络错误: ${t.localizedMessage}"
                    _aiPrompt.value = "保持规律运动，监测心率以优化心血管健康。"
                }
            }
        })
    }

    private fun generateSampleData() {
        val calendar = Calendar.getInstance()
        val random = Random()
        val records = mutableListOf<HealthRecord>()
        val activity = mutableListOf<ActivityData>()

        for (i in 13 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = calendar.time
            val heartRate = 65 + random.nextInt(20)
            val steps = 3000 + random.nextInt(9000)
            val calories = 1800 + random.nextInt(700)
            val sleepHours = 3 + random.nextInt(7)
            val quality = listOf("Good", "Excellent", "Fair").random()
            records.add(HealthRecord(date, heartRate, steps, calories, sleepHours, quality))

            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
            activity.add(
                ActivityData(
                    day = dayFormat.format(date),
                    percentage = steps / 10000f,
                    steps = steps,
                    isToday = i == 0
                )
            )
        }

        _healthRecords.value = records
        _activityData.value = activity
    }

    private fun startSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                val currentHR = _heartRate.value
                val delta = (-2..2).random()
                val newHR = (currentHR + delta).coerceIn(60, 100)
                val newQuality = if (Random().nextInt(20) == 0) listOf("Excellent", "Good", "Fair").random() else _signalQuality.value
                val newBattery = (_batteryLevel.value - 1).coerceAtLeast(10)
                updateHeartRate(newHR)
                updateSignalQuality(newQuality)
                updateBatteryLevel(newBattery)
                updateHealthRecord(
                    HealthRecord(
                        date = Date(),
                        heartRate = newHR,
                        steps = _dailySummary.value.steps + (0..100).random(),
                        calories = 2000,
                        sleepHours = 7,
                        signalQuality = newQuality
                    )
                )
                userId?.let { uid ->
                    saveMeasurement(
                        PhysNetMeasurementData(
                            timestamp = System.currentTimeMillis(),
                            sessionId = UUID.randomUUID().toString(),
                            userId = uid.toString(),
                            rppgSignal = FloatArray(10) { Random().nextFloat() },
                            heartRate = newHR.toFloat(),
                            frameCount = 100,
                            processingTimeMs = 500,
                            confidence = 0.95f,
                            signalQuality = SignalQuality(20f, 0.1f, 0.8f, 0.9f)
                        )
                    )
                }
            }
        }
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