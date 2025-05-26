package com.codelab.basiclayouts.ui.screen.insight

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.codelab.basiclayouts.viewmodel.insight.InsightViewModel
import com.codelab.basiclayouts.viewmodel.insight.PhysNetMeasurementData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 数据类：处理后的健康统计数据
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
 * 可视化模式枚举
 */
enum class VisualizationMode {
    HEART_RATE, HRV
}

/**
 * 增强的心率颜色调色板
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
 * 增强的HRV颜色调色板
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
 * 增强的健康历史数据处理器
 * 新增周视图和月视图支持
 */
class HealthHistoryDataProcessor {

    /**
     * 处理测量数据以提取健康统计信息
     */
    fun processHealthStatistics(measurements: List<PhysNetMeasurementData>): HealthStatistics {
        val sortedMeasurements = measurements.sortedBy { it.timestamp }
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // 心率统计
        val heartRates = sortedMeasurements.map { it.heartRate }
        val minHR = heartRates.minOrNull()?.roundToInt() ?: 60
        val maxHR = heartRates.maxOrNull()?.roundToInt() ?: 100
        val avgHR = if (heartRates.isNotEmpty()) heartRates.average().roundToInt() else 75

        // HRV统计
        val hrvValues = sortedMeasurements.mapNotNull { it.hrvResult?.rmssd?.toFloat() }
        val minHRV = hrvValues.minOrNull()?.roundToInt() ?: 0
        val maxHRV = hrvValues.maxOrNull()?.roundToInt() ?: 100
        val avgHRV = if (hrvValues.isNotEmpty()) hrvValues.average().roundToInt() else 50

        // 时间范围
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
     * 创建心率颜色调色板
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
     * 创建HRV颜色调色板
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
     * 获取心率区域颜色
     */
    fun getHeartRateZoneColor(heartRate: Float, palette: HeartRateColorPalette): Color {
        return when {
            heartRate < 60 -> palette.lowZone
            heartRate > 100 -> palette.highZone
            else -> palette.normalZone
        }
    }

    /**
     * 获取心率区域浅色（用于渐变）
     */
    fun getHeartRateZoneLightColor(heartRate: Float, palette: HeartRateColorPalette): Color {
        return when {
            heartRate < 60 -> palette.lowZoneLight
            heartRate > 100 -> palette.highZoneLight
            else -> palette.normalZoneLight
        }
    }

    /**
     * 获取HRV区域颜色
     */
    fun getHRVZoneColor(hrv: Float, palette: HRVColorPalette): Color {
        return when {
            hrv < 30 -> palette.lowZone
            hrv > 80 -> palette.highZone
            else -> palette.normalZone
        }
    }

    /**
     * 获取HRV区域浅色（用于渐变）
     */
    fun getHRVZoneLightColor(hrv: Float, palette: HRVColorPalette): Color {
        return when {
            hrv < 30 -> palette.lowZoneLight
            hrv > 80 -> palette.highZoneLight
            else -> palette.normalZoneLight
        }
    }

    /**
     * 对信号数据进行采样以用于可视化
     */
    fun sampleSignalData(signal: FloatArray, maxSamples: Int = 100): FloatArray {
        if (signal.size <= maxSamples) return signal

        val sampleSize = signal.size / maxSamples
        return signal.filterIndexed { index, _ -> index % sampleSize == 0 }.toFloatArray()
    }

    /**
     * 格式化日历日期显示
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

    // ========== 新增：多视图支持功能 ==========

    /**
     * 获取周范围显示字符串
     */
    fun getWeekRange(date: Date): Pair<String, String> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val weekStart = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val weekEnd = calendar.time

        val format = SimpleDateFormat("MMM dd", Locale.getDefault())
        return Pair(format.format(weekStart), format.format(weekEnd))
    }

    /**
     * 获取上一天
     */
    fun getPreviousDay(currentDate: Date, availableDates: List<Date>): Date? {
        val sortedDates = availableDates.sortedBy { it.time }
        val currentIndex = sortedDates.indexOf(currentDate)
        return if (currentIndex > 0) sortedDates[currentIndex - 1] else null
    }

    /**
     * 获取下一天
     */
    fun getNextDay(currentDate: Date, availableDates: List<Date>): Date? {
        val sortedDates = availableDates.sortedBy { it.time }
        val currentIndex = sortedDates.indexOf(currentDate)
        return if (currentIndex < sortedDates.size - 1) sortedDates[currentIndex + 1] else null
    }

    /**
     * 获取上一周
     */
    fun getPreviousWeek(currentDate: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        return calendar.time
    }

    /**
     * 获取下一周
     */
    fun getNextWeek(currentDate: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        return calendar.time
    }

    /**
     * 获取上个月
     */
    fun getPreviousMonth(currentDate: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.MONTH, -1)
        return calendar.time
    }

    /**
     * 获取下个月
     */
    fun getNextMonth(currentDate: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.MONTH, 1)
        return calendar.time
    }

    /**
     * 检查两个日期是否为同一天
     */
    fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * 检查两个日期是否在同一周
     */
    fun isSameWeek(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }

    /**
     * 检查两个日期是否在同一月
     */
    fun isSameMonth(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }

    /**
     * 按天分组测量数据（用于周视图）
     */
    fun groupMeasurementsByDay(
        measurements: List<PhysNetMeasurementData>,
        weekDate: Date
    ): List<List<PhysNetMeasurementData>> {
        val calendar = Calendar.getInstance()
        calendar.time = weekDate
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        val weeklyData = mutableListOf<List<PhysNetMeasurementData>>()

        for (i in 0..6) {
            val currentDay = calendar.time
            val dayMeasurements = measurements.filter { measurement ->
                isSameDay(Date(measurement.timestamp), currentDay)
            }
            weeklyData.add(dayMeasurements)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return weeklyData
    }

    /**
     * 按周分组测量数据（用于月视图）
     */
    fun groupMeasurementsByWeek(
        measurements: List<PhysNetMeasurementData>,
        monthDate: Date
    ): List<List<PhysNetMeasurementData>> {
        val calendar = Calendar.getInstance()
        calendar.time = monthDate
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val monthlyData = mutableListOf<List<PhysNetMeasurementData>>()
        val currentMonth = calendar.get(Calendar.MONTH)

        while (calendar.get(Calendar.MONTH) == currentMonth) {
            // 获取当前周的开始日期
            val weekStart = calendar.clone() as Calendar
            weekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

            // 获取当前周的结束日期
            val weekEnd = weekStart.clone() as Calendar
            weekEnd.add(Calendar.DAY_OF_WEEK, 6)

            // 筛选这一周的测量数据
            val weekMeasurements = measurements.filter { measurement ->
                val measurementDate = Date(measurement.timestamp)
                !measurementDate.before(weekStart.time) && !measurementDate.after(weekEnd.time)
            }

            monthlyData.add(weekMeasurements)

            // 移动到下一周
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        return monthlyData.take(4) // 限制为4周显示
    }

    /**
     * 计算每日健康指标统计（用于周/月视图的详细分析）
     */
    fun calculateDailyStats(measurements: List<PhysNetMeasurementData>): DailyHealthStats? {
        if (measurements.isEmpty()) return null

        val heartRates = measurements.map { it.heartRate }
        val hrvValues = measurements.mapNotNull { it.hrvResult?.rmssd?.toFloat() }

        return DailyHealthStats(
            date = Date(measurements.first().timestamp),
            measurementCount = measurements.size,
            avgHeartRate = heartRates.average().toFloat(),
            minHeartRate = heartRates.minOrNull() ?: 0f,
            maxHeartRate = heartRates.maxOrNull() ?: 0f,
            avgHRV = if (hrvValues.isNotEmpty()) hrvValues.average().toFloat() else 0f,
            minHRV = hrvValues.minOrNull() ?: 0f,
            maxHRV = hrvValues.maxOrNull() ?: 0f,
            avgConfidence = measurements.map { it.confidence }.average().toFloat()
        )
    }

    /**
     * 生成周报告摘要
     */
    fun generateWeeklyReport(weeklyData: List<List<PhysNetMeasurementData>>): WeeklyHealthReport {
        val allMeasurements = weeklyData.flatten()
        val dailyStats = weeklyData.mapNotNull { calculateDailyStats(it) }

        val totalMeasurements = allMeasurements.size
        val activeDays = dailyStats.size
        val avgHeartRate = if (dailyStats.isNotEmpty()) {
            dailyStats.map { it.avgHeartRate }.average().toFloat()
        } else 0f
        val avgHRV = if (dailyStats.isNotEmpty()) {
            dailyStats.map { it.avgHRV }.filter { it > 0 }.average().toFloat()
        } else 0f

        return WeeklyHealthReport(
            totalMeasurements = totalMeasurements,
            activeDays = activeDays,
            avgHeartRate = avgHeartRate,
            avgHRV = avgHRV,
            dailyStats = dailyStats,
            trendDirection = calculateTrendDirection(dailyStats)
        )
    }

    /**
     * 计算趋势方向
     */
    private fun calculateTrendDirection(dailyStats: List<DailyHealthStats>): TrendDirection {
        if (dailyStats.size < 2) return TrendDirection.STABLE

        val recentStats = dailyStats.takeLast(3)
        val earlierStats = dailyStats.dropLast(3).takeLast(3)

        if (recentStats.isEmpty() || earlierStats.isEmpty()) return TrendDirection.STABLE

        val recentAvgHR = recentStats.map { it.avgHeartRate }.average()
        val earlierAvgHR = earlierStats.map { it.avgHeartRate }.average()

        val hrDifference = recentAvgHR - earlierAvgHR

        return when {
            hrDifference > 2 -> TrendDirection.INCREASING
            hrDifference < -2 -> TrendDirection.DECREASING
            else -> TrendDirection.STABLE
        }
    }
}

/**
 * 日历日期信息数据类
 */
data class CalendarDateInfo(
    val day: String,
    val month: String,
    val year: String,
    val dayOfWeek: String
)

/**
 * 每日健康统计数据类
 */
data class DailyHealthStats(
    val date: Date,
    val measurementCount: Int,
    val avgHeartRate: Float,
    val minHeartRate: Float,
    val maxHeartRate: Float,
    val avgHRV: Float,
    val minHRV: Float,
    val maxHRV: Float,
    val avgConfidence: Float
)

/**
 * 周报告数据类
 */
data class WeeklyHealthReport(
    val totalMeasurements: Int,
    val activeDays: Int,
    val avgHeartRate: Float,
    val avgHRV: Float,
    val dailyStats: List<DailyHealthStats>,
    val trendDirection: TrendDirection
)

/**
 * 趋势方向枚举
 */
enum class TrendDirection {
    INCREASING, DECREASING, STABLE
}

/**
 * Composable函数：提供增强的健康历史数据
 */
@Composable
fun rememberHealthHistoryData(viewModel: InsightViewModel): EnhancedHealthHistoryDataState {
    val measurementsByDate by viewModel.measurementsByDate.collectAsState()
    val processor = HealthHistoryDataProcessor()

    return EnhancedHealthHistoryDataState(
        viewModel = viewModel,
        processor = processor,
        measurementsByDate = measurementsByDate
    )
}

/**
 * 增强的健康历史数据状态持有者
 */
data class EnhancedHealthHistoryDataState(
    val viewModel: InsightViewModel,
    val processor: HealthHistoryDataProcessor,
    val measurementsByDate: Map<String, List<PhysNetMeasurementData>>
) {

    fun getAvailableDates(): List<Date> = viewModel.getAvailableDates()

    fun getMeasurementsForDate(date: Date): List<PhysNetMeasurementData> =
        viewModel.getMeasurementsForDate(date)

    /**
     * 获取指定周的所有测量数据
     */
    fun getMeasurementsForWeek(weekDate: Date): List<PhysNetMeasurementData> {
        val calendar = Calendar.getInstance()
        calendar.time = weekDate
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val weekStart = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val weekEnd = calendar.time

        return measurementsByDate.values.flatten().filter { measurement ->
            val measurementDate = Date(measurement.timestamp)
            !measurementDate.before(weekStart) && !measurementDate.after(weekEnd)
        }
    }

    /**
     * 获取指定月的所有测量数据
     */
    fun getMeasurementsForMonth(monthDate: Date): List<PhysNetMeasurementData> {
        val calendar = Calendar.getInstance()
        calendar.time = monthDate
        val targetMonth = calendar.get(Calendar.MONTH)
        val targetYear = calendar.get(Calendar.YEAR)

        return measurementsByDate.values.flatten().filter { measurement ->
            val measurementCalendar = Calendar.getInstance()
            measurementCalendar.timeInMillis = measurement.timestamp
            measurementCalendar.get(Calendar.MONTH) == targetMonth &&
                    measurementCalendar.get(Calendar.YEAR) == targetYear
        }
    }

    /**
     * 获取周报告
     */
    fun getWeeklyReport(weekDate: Date): WeeklyHealthReport {
        val weeklyData = processor.groupMeasurementsByDay(getMeasurementsForWeek(weekDate), weekDate)
        return processor.generateWeeklyReport(weeklyData)
    }

    /**
     * 刷新云端数据
     */
    fun refreshFromCloud() {
        // TODO: 实现云端刷新逻辑
        // viewModel.refreshDataFromCloud()
    }

    /**
     * 处理统计数据
     */
    fun processStatistics(measurements: List<PhysNetMeasurementData>): HealthStatistics =
        processor.processHealthStatistics(measurements)
}