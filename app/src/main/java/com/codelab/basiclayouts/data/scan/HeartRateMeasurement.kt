package com.codelab.basiclayouts.data.scan

/**
 * Interface representing a standardized heart rate data point.
 */
interface HeartRateData {
    val timestamp: Long      // Time of the measurement (in milliseconds)
    val value: Int           // Heart rate value in BPM (beats per minute)
    val confidence: Float    // Confidence level (0.0 to 1.0)
}

/**
 * Data class representing a single heart rate measurement.
 *
 * @param timestamp Epoch time when the heart rate was recorded
 * @param value Heart rate in beats per minute (BPM)
 * @param confidence Confidence level (0.0 to 1.0)
 */
data class HeartRateMeasurement(
    override val timestamp: Long = System.currentTimeMillis(),
    override val value: Int = 0,
    override val confidence: Float = 0f
) : HeartRateData
