package com.codelab.basiclayouts.data.physnet

import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * HRV计算器 - 从rPPG信号计算心率变异性指标
 * 与现有RppgProcessor兼容
 */
class HrvCalculator {

    companion object {
        private const val TAG = "HrvCalculator"
        private const val MIN_PEAKS_FOR_HRV = 5 // 最少需要5个峰值来计算HRV
    }

    data class HrvResult(
        val rmssd: Double,        // 相邻R-R间期差值的均方根 (ms)
        val pnn50: Double,        // 相邻R-R间期差值>50ms的百分比
        val sdnn: Double,         // R-R间期标准差 (ms)
        val meanRR: Double,       // 平均R-R间期 (ms)
        val triangularIndex: Double, // 三角指数
        val stressIndex: Double,  // 压力指数
        val isValid: Boolean      // 结果是否有效
    )

    /**
     * 从rPPG信号计算HRV指标
     * @param rppgSignal rPPG信号数组
     * @param samplingRate 采样率 (Hz)
     * @return HRV计算结果
     */
    fun calculateHRV(rppgSignal: FloatArray, samplingRate: Float = 25f): HrvResult {
        return try {
            Log.d(TAG, "开始HRV计算，信号长度: ${rppgSignal.size}")

            // 1. 预处理信号
            val preprocessedSignal = preprocessSignal(rppgSignal)

            // 2. 检测峰值点
            val peaks = detectPeaks(preprocessedSignal, samplingRate)
            Log.d(TAG, "检测到 ${peaks.size} 个峰值")

            if (peaks.size < MIN_PEAKS_FOR_HRV) {
                Log.w(TAG, "峰值数量不足，无法计算HRV")
                return HrvResult(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false)
            }

            // 3. 计算R-R间期 (以毫秒为单位)
            val rrIntervals = calculateRRIntervals(peaks, samplingRate)

            // 4. 过滤异常值
            val filteredRR = filterOutliers(rrIntervals)
            Log.d(TAG, "过滤后R-R间期数量: ${filteredRR.size}")

            if (filteredRR.size < MIN_PEAKS_FOR_HRV - 1) {
                Log.w(TAG, "有效R-R间期不足")
                return HrvResult(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false)
            }

            // 5. 计算HRV指标
            val rmssd = calculateRMSSD(filteredRR)
            val pnn50 = calculatePNN50(filteredRR)
            val sdnn = calculateSDNN(filteredRR)
            val meanRR = filteredRR.average()
            val triangularIndex = calculateTriangularIndex(filteredRR)
            val stressIndex = calculateStressIndex(rmssd)

            Log.d(TAG, "HRV计算完成: RMSSD=$rmssd, SDNN=$sdnn, pNN50=$pnn50")

            HrvResult(rmssd, pnn50, sdnn, meanRR, triangularIndex, stressIndex, true)

        } catch (e: Exception) {
            Log.e(TAG, "HRV计算失败", e)
            HrvResult(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false)
        }
    }

    /**
     * 预处理rPPG信号
     */
    private fun preprocessSignal(signal: FloatArray): FloatArray {
        // 1. 去除直流分量
        val mean = signal.average().toFloat()
        val centered = signal.map { it - mean }.toFloatArray()

        // 2. 应用移动平均平滑
        return movingAverage(centered, 3)
    }

    /**
     * 移动平均平滑
     */
    private fun movingAverage(signal: FloatArray, windowSize: Int): FloatArray {
        val smoothed = FloatArray(signal.size)
        val halfWindow = windowSize / 2

        for (i in signal.indices) {
            val start = (i - halfWindow).coerceAtLeast(0)
            val end = (i + halfWindow).coerceAtMost(signal.size - 1)

            var sum = 0f
            for (j in start..end) {
                sum += signal[j]
            }
            smoothed[i] = sum / (end - start + 1)
        }

        return smoothed
    }

    /**
     * 检测rPPG信号中的峰值点 - 使用与RppgProcessor相同的逻辑
     */
    private fun detectPeaks(signal: FloatArray, samplingRate: Float): List<Int> {
        val peaks = mutableListOf<Int>()
        val minDistance = (samplingRate * 0.4).toInt() // 最小峰值间距 (400ms = 150 BPM)

        // 自适应阈值 - 使用75%分位数
        val sortedSignal = signal.sorted()
        val threshold = sortedSignal[sortedSignal.size * 3 / 4]

        for (i in 1 until signal.size - 1) {
            if (signal[i] > signal[i - 1] &&
                signal[i] > signal[i + 1] &&
                signal[i] > threshold) {

                // 检查与上一个峰值的距离
                if (peaks.isEmpty() || i - peaks.last() >= minDistance) {
                    peaks.add(i)
                }
            }
        }

        return peaks
    }

    /**
     * 计算R-R间期
     */
    private fun calculateRRIntervals(peaks: List<Int>, samplingRate: Float): DoubleArray {
        if (peaks.size < 2) return doubleArrayOf()

        return DoubleArray(peaks.size - 1) { i ->
            (peaks[i + 1] - peaks[i]) * 1000.0 / samplingRate // 转换为毫秒
        }
    }

    /**
     * 过滤R-R间期异常值 - 使用与RppgProcessor相同的逻辑
     */
    private fun filterOutliers(rrIntervals: DoubleArray): DoubleArray {
        if (rrIntervals.size < 3) return rrIntervals

        val sorted = rrIntervals.sorted()
        val q1 = sorted[sorted.size / 4]
        val q3 = sorted[sorted.size * 3 / 4]
        val iqr = q3 - q1

        val lowerBound = q1 - 1.5 * iqr
        val upperBound = q3 + 1.5 * iqr

        return rrIntervals.filter { it in lowerBound..upperBound }.toDoubleArray()
    }

    /**
     * 计算RMSSD (相邻R-R间期差值的均方根)
     */
    private fun calculateRMSSD(rrIntervals: DoubleArray): Double {
        if (rrIntervals.size < 2) return 0.0

        val differences = DoubleArray(rrIntervals.size - 1) { i ->
            rrIntervals[i + 1] - rrIntervals[i]
        }

        val sumSquares = differences.map { it * it }.sum()
        return sqrt(sumSquares / differences.size)
    }

    /**
     * 计算pNN50 (相邻R-R间期差值>50ms的百分比)
     */
    private fun calculatePNN50(rrIntervals: DoubleArray): Double {
        if (rrIntervals.size < 2) return 0.0

        val differences = DoubleArray(rrIntervals.size - 1) { i ->
            abs(rrIntervals[i + 1] - rrIntervals[i])
        }

        val count50 = differences.count { it > 50.0 }
        return count50.toDouble() / differences.size * 100.0
    }

    /**
     * 计算SDNN (R-R间期标准差)
     */
    private fun calculateSDNN(rrIntervals: DoubleArray): Double {
        if (rrIntervals.isEmpty()) return 0.0

        val mean = rrIntervals.average()
        val variance = rrIntervals.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance)
    }

    /**
     * 计算三角指数
     */
    private fun calculateTriangularIndex(rrIntervals: DoubleArray): Double {
        if (rrIntervals.size < 3) return 0.0

        // 创建直方图 (bin width = 7.8125ms)
        val binWidth = 7.8125
        val minRR = rrIntervals.minOrNull() ?: 0.0
        val maxRR = rrIntervals.maxOrNull() ?: 0.0
        val binCount = ((maxRR - minRR) / binWidth).toInt() + 1

        val histogram = IntArray(binCount)
        for (rr in rrIntervals) {
            val binIndex = ((rr - minRR) / binWidth).toInt().coerceIn(0, binCount - 1)
            histogram[binIndex]++
        }

        val maxFreq = histogram.maxOrNull() ?: 0
        return if (maxFreq > 0) rrIntervals.size.toDouble() / maxFreq else 0.0
    }

    /**
     * 计算压力指数
     */
    private fun calculateStressIndex(rmssd: Double): Double {
        // 基于RMSSD的压力指数计算
        return if (rmssd > 0) (50.0 / rmssd).coerceIn(0.0, 10.0) else 10.0
    }
}