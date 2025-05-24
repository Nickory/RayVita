package com.codelab.basiclayouts.data.physnet.model

import kotlinx.serialization.Serializable
import java.util.Date

/**
 * rPPG测量结果数据模型
 */
@Serializable
data class RppgResult(
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val heartRate: Float,
    val hrv: Float? = null,  // 心率变异性
    val rppgSignal: List<Float>,  // 波形数据
    val confidence: Float = 0f,  // 测量置信度
    val frameCount: Int = 500,
    val processingTimeMs: Long = 0L,
    val isSynced: Boolean = false  // 是否已同步到服务器
) {
    val date: Date
        get() = Date(timestamp)

    val isValid: Boolean
        get() = heartRate in 40f..200f && rppgSignal.isNotEmpty()

    fun toApiRequest(): Map<String, Any?> = mapOf(
        "session_id" to sessionId,
        "ts" to timestamp,
        "heart_rate" to heartRate,
        "hrv" to hrv,
        "raw_signal_url" to null  // 可以后续上传原始信号到云存储
    )
}

/**
 * API响应模型
 */
@Serializable
data class RppgApiResponse(
    val msg: String,
    val sample_id: Int? = null
)

/**
 * 捕获会话
 */
@Serializable
data class CaptureSession(
    val sessionId: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val results: List<RppgResult> = emptyList()
)