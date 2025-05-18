package com.codelab.basiclayouts.viewmodel.insight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.network.RetrofitClient
import com.codelab.basiclayouts.network.model.ChatRequest
import com.codelab.basiclayouts.network.model.ChatResponse
import com.codelab.basiclayouts.network.model.Message
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random

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

class InsightViewModel : ViewModel() {
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

    init {
        generateSampleData()
        startSimulation()
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

    fun requestInsightPrompt() {
        val content = "Based on heart rate ${_heartRate.value} bpm and signal quality ${_signalQuality.value}, provide a 50-word useful health tip."
        val messages = listOf(Message("user", content))
        val request = ChatRequest(model = "deepseek-chat", messages = messages)

        _isLoading.value = true
        _errorMessage.value = null

        RetrofitClient.api.chatCompletion(request).enqueue(object : Callback<ChatResponse> {
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