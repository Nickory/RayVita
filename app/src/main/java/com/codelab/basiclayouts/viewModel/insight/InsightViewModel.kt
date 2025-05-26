package com.codelab.basiclayouts.viewmodel.insight

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.network.RetrofitClient
import com.codelab.basiclayouts.network.model.ChatRequest
import com.codelab.basiclayouts.network.model.ChatResponse
import com.codelab.basiclayouts.network.model.Message
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

@Serializable
data class PhysNetMeasurementData(
    val timestamp: Long,
    val sessionId: String,
    val rppgSignal: FloatArray,
    val heartRate: Float,
    val frameCount: Int,
    val processingTimeMs: Int,
    val confidence: Float,
    val hrvResult: HRVResult? = null,
    val spo2Result: SPO2Result? = null,
    val signalQuality: SignalQuality? = null
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

class InsightViewModel(
    context: Context
) : ViewModel() {
    private val appContext = context.applicationContext
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

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val json = Json // 使用默认 Json 配置，无需 ignoreUnknownKeys

    init {
        generateSampleData()
        startSimulation()
        loadMeasurements()
    }

    // Load measurements from local JSON files
    private fun loadMeasurements() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dir = File(appContext.filesDir, "measurements")
                val files = dir.listFiles { _, name -> name.endsWith(".json") }?.toList() ?: emptyList()
                val measurements = files.mapNotNull { file ->
                    try {
                        val jsonString = file.readText()
                        json.decodeFromString<PhysNetMeasurementData>(jsonString)
                    } catch (e: Exception) {
                        Log.e("InsightViewModel", "Failed to parse JSON file: ${file.name}, error: ${e.message}", e)
                        null
                    }
                }
                val grouped = measurements.groupBy { dateFormat.format(Date(it.timestamp)) }
                    .mapValues { it.value.sortedByDescending { m -> m.timestamp } }
                _measurementsByDate.value = grouped
                if (measurements.size < files.size) {
                    Log.w("InsightViewModel", "Some JSON files failed to parse: ${files.size - measurements.size} files skipped")
                    _errorMessage.value = "Failed to load ${files.size - measurements.size} measurement files"
                }
            } catch (e: Exception) {
                Log.e("InsightViewModel", "Failed to load measurements directory", e)
                _errorMessage.value = "Error loading measurement data"
            }
        }
    }

    // Get available dates for navigation
    fun getAvailableDates(): List<Date> {
        return _measurementsByDate.value.keys.mapNotNull { key ->
            try {
                dateFormat.parse(key)
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.time }
    }

    // Get measurements for a specific date
    fun getMeasurementsForDate(date: Date): List<PhysNetMeasurementData> {
        val dateKey = dateFormat.format(date)
        return _measurementsByDate.value[dateKey] ?: emptyList()
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
        val content = "Based on user today ${summaryData}, provide a 50-word useful health tip."
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
                        _aiPrompt.value = reply ?: "Maintain a consistent exercise routine and monitor your heart rate to optimize cardiovascular health."
                    } else {
                        _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                        _aiPrompt.value = "Maintain a consistent exercise routine and monitor your heart rate to optimize cardiovascular health."
                    }
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                viewModelScope.launch {
                    delay(500)
                    _isLoading.value = false
                    _errorMessage.value = "Network error: ${t.localizedMessage}"
                    _aiPrompt.value = "Maintain a consistent exercise routine and monitor your heart rate to optimize cardiovascular health."
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