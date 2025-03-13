package com.codelab.basiclayouts.rppg

class HeartRateCalculator {
    private val peaks = mutableListOf<Int>()
    private val intervals = mutableListOf<Int>()

    fun update(signal: FloatArray) {
        val validPeaks = detectValidPeaks(signal)
        peaks.addAll(validPeaks)

        if (peaks.size >= 2) {
            val newInterval = peaks.last() - peaks[peaks.size - 2]
            intervals.add(newInterval)

            // 动态窗口过滤（保留最近5秒数据）
            while (intervals.size > 150) intervals.removeAt(0)
        }
    }

    fun calculateBpm(): Any {
        if (intervals.isEmpty()) return 0

        val medianInterval = intervals.median()
        return ((60000 / medianInterval.toFloat()) * 10 + 5) / 10 // 保留一位小数
    }

    private fun detectValidPeaks(signal: FloatArray): List<Int> {
        val peaks = mutableListOf<Int>()
        var lastPeak = -1

        for (i in 1 until signal.size - 1) {
            if (signal[i] > signal[i - 1] && signal[i] > signal[i + 1]) {
                if (i - lastPeak > 30) { // 最小峰间距（约1秒）
                    peaks.add(i)
                    lastPeak = i
                }
            }
        }
        return peaks
    }
}

// 扩展函数：计算中位数
fun List<Int>.median(): Int {
    val sorted = sorted()
    return when {
        size % 2 == 0 -> (sorted[size / 2 - 1] + sorted[size / 2]) / 2
        else -> sorted[size / 2]
    }
}