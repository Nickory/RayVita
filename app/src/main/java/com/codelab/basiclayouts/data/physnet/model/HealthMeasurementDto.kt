package com.codelab.basiclayouts.data.physnet.model


import com.google.gson.annotations.SerializedName
import com.codelab.basiclayouts.viewmodel.insight.HRVResult
import com.codelab.basiclayouts.viewmodel.insight.SPO2Result
import com.codelab.basiclayouts.viewmodel.insight.SignalQuality

data class HealthMeasurementDto(
    @SerializedName("sessionId") val sessionId: String?,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("heartRate") val heartRate: Float?,
    @SerializedName("rppgSignal") val rppgSignal: List<Float>,
    @SerializedName("frameCount") val frameCount: Int?,
    @SerializedName("processingTimeMs") val processingTimeMs: Int?,
    @SerializedName("confidence") val confidence: Float?,
    @SerializedName("hrvResult") val hrvResult: HRVResult?,
    @SerializedName("spo2Result") val spo2Result: SPO2Result?,
    @SerializedName("signalQuality") val signalQuality: SignalQuality?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("syncStatus") val syncStatus: String?
)