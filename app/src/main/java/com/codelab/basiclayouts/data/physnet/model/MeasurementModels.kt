package com.codelab.basiclayouts.data.physnet.model.storage

import kotlinx.serialization.Serializable

@Serializable
data class PhysNetMeasurementData(
    val sessionId: String,
    val timestamp: Long,
    val heartRate: Float,
    val rppgSignal: List<Float>,
    val frameCount: Int,
    val processingTimeMs: Long,
    val confidence: Float,
    val hrvResult: PhysNetHrvData? = null,
    val spo2Result: PhysNetSpO2Data? = null,
    val signalQuality: PhysNetSignalQuality
)

@Serializable
data class PhysNetHrvData(
    val rmssd: Double,
    val pnn50: Double,
    val sdnn: Double,
    val meanRR: Double,
    val triangularIndex: Double,
    val stressIndex: Double,
    val isValid: Boolean
)

@Serializable
data class PhysNetSpO2Data(
    val spo2: Double,
    val redAC: Double,
    val redDC: Double,
    val irAC: Double,
    val irDC: Double,
    val ratioOfRatios: Double,
    val confidence: Double,
    val isValid: Boolean
)

@Serializable
data class PhysNetSignalQuality(
    val snr: Double,
    val motionArtifact: Double,
    val illuminationQuality: Double,
    val overallQuality: Double
)
