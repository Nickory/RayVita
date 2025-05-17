package com.codelab.basiclayouts.data.scan

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Object responsible for processing the raw signal to compute heart rate and clean waveform.
 */
object SignalProcessor {

    private const val FRAME_RATE = 30  // assumed frames per second
    private const val BUFFER_SIZE = 150

    /**
     * Preprocesses a raw signal using low-pass and high-pass filters.
     * @param raw List of raw brightness values (typically Y channel from YUV frame).
     * @return Filtered and normalized signal ready for peak detection.
     */
    fun preprocessSignal(raw: List<Float>): FloatArray {
        val size = raw.size
        val filtered = FloatArray(size)

        // Low-pass filter: simple moving average
        val lowPass = FloatArray(size)
        val lpWindow = 5
        for (i in lpWindow until size) {
            lowPass[i] = raw.subList(i - lpWindow, i + 1).average().toFloat()
        }

        // High-pass filter: subtract local baseline
        val highPass = FloatArray(size)
        val hpWindow = 20
        for (i in hpWindow until size) {
            highPass[i] = raw[i] - raw.subList(i - hpWindow, i).average().toFloat()
        }

        // Combine filters
        for (i in hpWindow until size) {
            filtered[i] = 0.75f * highPass[i] + 0.25f * lowPass[i]
        }

        return filtered
    }

    /**
     * Detects peak indices using adaptive thresholding and rising edge detection.
     * @param signal Filtered signal array.
     * @return List of peak indices.
     */
    fun detectPeaks(signal: FloatArray): List<Int> {
        val peaks = mutableListOf<Int>()
        var threshold = 0f
        var isRising = false
        var lastPeak = -15

        for (i in 1 until signal.size - 1) {
            if (i > 90) {
                val window = signal.copyOfRange(i - 90, i)
                val mean = window.average()
                val std = sqrt(window.map { (it - mean).pow(2) }.average())
                threshold = (mean + 1.2 * std).toFloat()
            }

            val rising = signal[i] > signal[i - 1]

            if (isRising && !rising && signal[i] > threshold) {
                if (i - lastPeak > 15) {
                    peaks.add(i)
                    lastPeak = i
                    threshold *= 0.95f
                }
            }

            isRising = rising
        }

        return peaks
    }

    /**
     * Calculates heart rate in BPM from peak indices.
     * @param peaks List of detected peak indices.
     * @return Heart rate in beats per minute.
     */
    fun calculateBpm(peaks: List<Int>): Int {
        if (peaks.size < 3) return 0

        val intervals = peaks.zipWithNext { a, b -> b - a }
        val median = intervals.sorted()[intervals.size / 2]
        val filtered = intervals.filter {
            it in (median * 0.7).toInt()..(median * 1.3).toInt()
        }

        val avgInterval = filtered.average()
        return ((FRAME_RATE * 60) / avgInterval).toInt().coerceIn(40, 200)
    }
}
