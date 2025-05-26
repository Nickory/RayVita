package com.codelab.basiclayouts.ui.screen.insight

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.codelab.basiclayouts.viewmodel.insight.InsightViewModel
import com.codelab.basiclayouts.viewmodel.insight.PhysNetMeasurementData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Data class for processed health statistics
 */
data class HealthStatistics(
    val heartRates: List<Float>,
    val minHR: Int,
    val maxHR: Int,
    val avgHR: Int,
    val hrvValues: List<Float>,
    val minHRV: Int,
    val maxHRV: Int,
    val avgHRV: Int,
    val measurementCount: Int,
    val timeRange: Pair<String, String>,
    val timeSpanMs: Long
)

/**
 * Data class for visualization mode
 */
enum class VisualizationMode {
    HEART_RATE, HRV
}

/**
 * Enhanced Heart Rate Color Palette
 */
data class HeartRateColorPalette(
    val lowZone: Color,
    val normalZone: Color,
    val highZone: Color,
    val lowZoneLight: Color,
    val normalZoneLight: Color,
    val highZoneLight: Color
)

/**
 * Enhanced HRV Color Palette
 */
data class HRVColorPalette(
    val lowZone: Color,
    val normalZone: Color,
    val highZone: Color,
    val lowZoneLight: Color,
    val normalZoneLight: Color,
    val highZoneLight: Color
)

/**
 * Health History Data Processing Logic
 */
class HealthHistoryDataProcessor {

    /**
     * Process measurements to extract health statistics
     */
    fun processHealthStatistics(measurements: List<PhysNetMeasurementData>): HealthStatistics {
        val sortedMeasurements = measurements.sortedBy { it.timestamp }
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Heart Rate Statistics
        val heartRates = sortedMeasurements.map { it.heartRate }
        val minHR = heartRates.minOrNull()?.roundToInt() ?: 60
        val maxHR = heartRates.maxOrNull()?.roundToInt() ?: 100
        val avgHR = if (heartRates.isNotEmpty()) heartRates.average().roundToInt() else 75

        // HRV Statistics
        val hrvValues = sortedMeasurements.mapNotNull { it.hrvResult?.rmssd?.toFloat() }
        val minHRV = hrvValues.minOrNull()?.roundToInt() ?: 0
        val maxHRV = hrvValues.maxOrNull()?.roundToInt() ?: 100
        val avgHRV = if (hrvValues.isNotEmpty()) hrvValues.average().roundToInt() else 50

        // Time Range
        val timeRange = if (sortedMeasurements.isNotEmpty()) {
            Pair(
                timeFormat.format(Date(sortedMeasurements.first().timestamp)),
                timeFormat.format(Date(sortedMeasurements.last().timestamp))
            )
        } else {
            Pair("--:--", "--:--")
        }

        val timeSpanMs = if (sortedMeasurements.isNotEmpty()) {
            sortedMeasurements.last().timestamp - sortedMeasurements.first().timestamp
        } else {
            0L
        }

        return HealthStatistics(
            heartRates = heartRates,
            minHR = minHR,
            maxHR = maxHR,
            avgHR = avgHR,
            hrvValues = hrvValues,
            minHRV = minHRV,
            maxHRV = maxHRV,
            avgHRV = avgHRV,
            measurementCount = sortedMeasurements.size,
            timeRange = timeRange,
            timeSpanMs = timeSpanMs
        )
    }

    /**
     * Create Heart Rate Color Palette
     */
    fun createHeartRateColorPalette(
        primaryColor: Color,
        tertiaryColor: Color,
        errorColor: Color,
        primaryContainerColor: Color,
        tertiaryContainerColor: Color,
        errorContainerColor: Color
    ): HeartRateColorPalette {
        return HeartRateColorPalette(
            lowZone = primaryColor,
            normalZone = tertiaryColor,
            highZone = errorColor,
            lowZoneLight = primaryContainerColor,
            normalZoneLight = tertiaryContainerColor,
            highZoneLight = errorContainerColor
        )
    }

    /**
     * Create HRV Color Palette
     */
    fun createHRVColorPalette(
        primaryColor: Color,
        secondaryColor: Color,
        tertiaryColor: Color,
        primaryContainerColor: Color,
        secondaryContainerColor: Color,
        tertiaryContainerColor: Color
    ): HRVColorPalette {
        return HRVColorPalette(
            lowZone = secondaryColor,
            normalZone = tertiaryColor,
            highZone = primaryColor,
            lowZoneLight = secondaryContainerColor,
            normalZoneLight = tertiaryContainerColor,
            highZoneLight = primaryContainerColor
        )
    }

    /**
     * Get heart rate zone colors using Material3 theme
     */
    fun getHeartRateZoneColor(heartRate: Float, palette: HeartRateColorPalette): Color {
        return when {
            heartRate < 60 -> palette.lowZone
            heartRate > 100 -> palette.highZone
            else -> palette.normalZone
        }
    }

    /**
     * Get heart rate zone light colors for gradients
     */
    fun getHeartRateZoneLightColor(heartRate: Float, palette: HeartRateColorPalette): Color {
        return when {
            heartRate < 60 -> palette.lowZoneLight
            heartRate > 100 -> palette.highZoneLight
            else -> palette.normalZoneLight
        }
    }

    /**
     * Get HRV zone colors using Material3 theme
     */
    fun getHRVZoneColor(hrv: Float, palette: HRVColorPalette): Color {
        return when {
            hrv < 30 -> palette.lowZone
            hrv > 80 -> palette.highZone
            else -> palette.normalZone
        }
    }

    /**
     * Get HRV zone light colors for gradients
     */
    fun getHRVZoneLightColor(hrv: Float, palette: HRVColorPalette): Color {
        return when {
            hrv < 30 -> palette.lowZoneLight
            hrv > 80 -> palette.highZoneLight
            else -> palette.normalZoneLight
        }
    }

    /**
     * Sample signal data for visualization
     */
    fun sampleSignalData(signal: FloatArray, maxSamples: Int = 100): FloatArray {
        if (signal.size <= maxSamples) return signal

        val sampleSize = signal.size / maxSamples
        return signal.filterIndexed { index, _ -> index % sampleSize == 0 }.toFloatArray()
    }

    /**
     * Format date for calendar display
     */
    fun formatCalendarDate(date: Date): CalendarDateInfo {
        val dateFormat = SimpleDateFormat("d", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        return CalendarDateInfo(
            day = dateFormat.format(date),
            month = monthFormat.format(date),
            year = yearFormat.format(date),
            dayOfWeek = dayFormat.format(date)
        )
    }
}

/**
 * Data class for calendar date information
 */
data class CalendarDateInfo(
    val day: String,
    val month: String,
    val year: String,
    val dayOfWeek: String
)

/**
 * Composable function to provide health history data
 */
@Composable
fun rememberHealthHistoryData(viewModel: InsightViewModel): HealthHistoryDataState {
    val measurementsByDate by viewModel.measurementsByDate.collectAsState()
    val processor = HealthHistoryDataProcessor()

    return HealthHistoryDataState(
        viewModel = viewModel,
        processor = processor,
        measurementsByDate = measurementsByDate
    )
}

/**
 * Data state holder for health history
 */
data class HealthHistoryDataState(
    val viewModel: InsightViewModel,
    val processor: HealthHistoryDataProcessor,
    val measurementsByDate: Map<String, List<PhysNetMeasurementData>>
) {

    fun getAvailableDates(): List<Date> = viewModel.getAvailableDates()

    fun getMeasurementsForDate(date: Date): List<PhysNetMeasurementData> =
        viewModel.getMeasurementsForDate(date)

    fun refreshFromCloud() {
        // TODO: Implement cloud refresh logic
        // viewModel.refreshDataFromCloud()
    }

    fun processStatistics(measurements: List<PhysNetMeasurementData>): HealthStatistics =
        processor.processHealthStatistics(measurements)
}