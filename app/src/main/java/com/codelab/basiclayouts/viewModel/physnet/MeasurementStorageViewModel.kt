package com.codelab.basiclayouts.viewModel.physnet

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.data.physnet.model.EnhancedRppgResult
import com.codelab.basiclayouts.data.physnet.model.HrvData
import com.codelab.basiclayouts.data.physnet.model.SignalQuality
import com.codelab.basiclayouts.data.physnet.model.SpO2Data
import com.codelab.basiclayouts.data.physnet.model.storage.PhysNetHrvData
import com.codelab.basiclayouts.data.physnet.model.storage.PhysNetMeasurementData
import com.codelab.basiclayouts.data.physnet.model.storage.PhysNetSignalQuality
import com.codelab.basiclayouts.data.physnet.model.storage.PhysNetSpO2Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MeasurementStorageViewModel(private val context: Context) : ViewModel() {
    companion object {
        private const val TAG = "MeasurementStorageViewModel"
        private const val MEASUREMENTS_DIR = "measurements"
    }

    private val json = Json { prettyPrint = true }

    /**
     * Save measurement data as JSON to local storage
     */
    fun saveMeasurement(result: EnhancedRppgResult) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dir = File(context.filesDir, MEASUREMENTS_DIR)
                if (!dir.exists()) {
                    dir.mkdirs()
                }

                val measurementData = PhysNetMeasurementData(
                    sessionId = result.sessionId,
                    timestamp = result.timestamp,
                    heartRate = result.heartRate,
                    rppgSignal = result.rppgSignal,
                    frameCount = result.frameCount,
                    processingTimeMs = result.processingTimeMs,
                    confidence = result.confidence,
                    hrvResult = result.hrvResult?.let { mapHrvData(it) },
                    spo2Result = result.spo2Result?.let { mapSpO2Data(it) },
                    signalQuality = mapSignalQuality(result.signalQuality)
                )

                val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                val formattedTimestamp = timestampFormat.format(Date(result.timestamp))
                val filename = "session_${result.sessionId}_$formattedTimestamp.json"
                val file = File(dir, filename)

                val jsonString = json.encodeToString(measurementData)
                file.writeText(jsonString)

                Log.d(TAG, "Measurement saved successfully: $filename")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save measurement", e)
            }
        }
    }

    /**
     * Load a single measurement from a JSON file
     */
    private fun loadMeasurement(file: File): PhysNetMeasurementData? {
        return try {
            val jsonString = file.readText()
            json.decodeFromString<PhysNetMeasurementData>(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load measurement from ${file.name}", e)
            null
        }
    }

    /**
     * Data class to represent categorized measurements
     */
    data class CategorizedMeasurements(
        val today: List<PhysNetMeasurementData> = emptyList(),
        val yesterday: List<PhysNetMeasurementData> = emptyList(),
        val thisWeek: List<PhysNetMeasurementData> = emptyList(),
        val older: List<PhysNetMeasurementData> = emptyList()
    )

    /**
     * Get all saved measurements, categorized by date
     */
    fun getCategorizedMeasurements(): CategorizedMeasurements {
        val files = getSavedMeasurements()
        val today = mutableListOf<PhysNetMeasurementData>()
        val yesterday = mutableListOf<PhysNetMeasurementData>()
        val thisWeek = mutableListOf<PhysNetMeasurementData>()
        val older = mutableListOf<PhysNetMeasurementData>()

        // Get current date boundaries
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Reset time to midnight for today
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis

        // Yesterday
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStart = calendar.timeInMillis

        // Start of the week (Monday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val weekStart = calendar.timeInMillis

        files.forEach { file ->
            loadMeasurement(file)?.let { measurement ->
                val timestamp = measurement.timestamp
                when {
                    timestamp >= todayStart -> today.add(measurement)
                    timestamp >= yesterdayStart -> yesterday.add(measurement)
                    timestamp >= weekStart -> thisWeek.add(measurement)
                    else -> older.add(measurement)
                }
            }
        }

        return CategorizedMeasurements(
            today = today.sortedByDescending { it.timestamp },
            yesterday = yesterday.sortedByDescending { it.timestamp },
            thisWeek = thisWeek.sortedByDescending { it.timestamp },
            older = older.sortedByDescending { it.timestamp }
        )
    }

    /**
     * Retrieve all saved measurement files
     */
    fun getSavedMeasurements(): List<File> {
        val dir = File(context.filesDir, MEASUREMENTS_DIR)
        return dir.listFiles { _, name -> name.endsWith(".json") }?.toList() ?: emptyList()
    }

    /**
     * Map HrvData to PhysNetHrvData
     */
    private fun mapHrvData(hrv: HrvData): PhysNetHrvData {
        return PhysNetHrvData(
            rmssd = hrv.rmssd,
            pnn50 = hrv.pnn50,
            sdnn = hrv.sdnn,
            meanRR = hrv.meanRR,
            triangularIndex = hrv.triangularIndex,
            stressIndex = hrv.stressIndex,
            isValid = hrv.isValid
        )
    }

    /**
     * Map SpO2Data to PhysNetSpO2Data
     */
    private fun mapSpO2Data(spo2: SpO2Data): PhysNetSpO2Data {
        return PhysNetSpO2Data(
            spo2 = spo2.spo2,
            redAC = spo2.redAC,
            redDC = spo2.redDC,
            irAC = spo2.irAC,
            irDC = spo2.irDC,
            ratioOfRatios = spo2.ratioOfRatios,
            confidence = spo2.confidence,
            isValid = spo2.isValid
        )
    }

    /**
     * Map SignalQuality to PhysNetSignalQuality
     */
    private fun mapSignalQuality(signalQuality: SignalQuality): PhysNetSignalQuality {
        return PhysNetSignalQuality(
            snr = signalQuality.snr,
            motionArtifact = signalQuality.motionArtifact,
            illuminationQuality = signalQuality.illuminationQuality,
            overallQuality = signalQuality.overallQuality
        )
    }
}
