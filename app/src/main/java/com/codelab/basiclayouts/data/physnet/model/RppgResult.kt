package com.codelab.basiclayouts.data.physnet.model

import kotlinx.serialization.Serializable

/**
 * 增强的rPPG结果，包含HRV和SpO2数据
 * 保持与原有RppgResult的兼容性
 */
@Serializable
data class EnhancedRppgResult(
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),

    // 基础心率数据（保持与原版本兼容）
    val heartRate: Float,
    val rppgSignal: List<Float>,
    val frameCount: Int,
    val processingTimeMs: Long,
    val confidence: Float,

    // 新增的HRV数据
    val hrvResult: HrvData? = null,

    // 新增的SpO2数据
    val spo2Result: SpO2Data? = null,

    // 新增的信号质量评估
    val signalQuality: SignalQuality
) {
    /**
     * 转换为原始的RppgResult格式（向后兼容）
     */
    fun toRppgResult(): RppgResult {
        return RppgResult(
            sessionId = sessionId,
            heartRate = heartRate,
            rppgSignal = rppgSignal,
            frameCount = frameCount,
            processingTimeMs = processingTimeMs,
            confidence = confidence,
            timestamp = timestamp,
            isSynced = false  // 默认未同步
        )
    }
}

/**
 * 原始的RppgResult类 - 保持兼容性
 */
@Serializable
data class RppgResult(
    val sessionId: String,
    val heartRate: Float,
    val rppgSignal: List<Float>,
    val frameCount: Int,
    val processingTimeMs: Long,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    /**
     * 转换为API请求格式
     */
    fun toApiRequest(): Map<String, Any> {
        return mapOf(
            "sessionId" to sessionId,
            "heartRate" to heartRate,
            "rppgSignal" to rppgSignal,
            "frameCount" to frameCount,
            "processingTimeMs" to processingTimeMs,
            "confidence" to confidence,
            "timestamp" to timestamp
        )
    }
}

/**
 * HRV数据
 */
@Serializable
data class HrvData(
    val rmssd: Double,           // 相邻R-R间期差值的均方根 (ms)
    val pnn50: Double,           // 相邻R-R间期差值>50ms的百分比
    val sdnn: Double,            // R-R间期标准差 (ms)
    val meanRR: Double,          // 平均R-R间期 (ms)
    val triangularIndex: Double, // 三角指数
    val stressIndex: Double,     // 压力指数
    val isValid: Boolean
) {
    /**
     * 获取HRV健康状态
     */
    fun getHealthStatus(): HrvHealthStatus {
        return when {
            !isValid -> HrvHealthStatus.UNKNOWN
            rmssd >= 50 && sdnn >= 50 -> HrvHealthStatus.EXCELLENT
            rmssd >= 30 && sdnn >= 30 -> HrvHealthStatus.GOOD
            rmssd >= 20 && sdnn >= 20 -> HrvHealthStatus.FAIR
            else -> HrvHealthStatus.POOR
        }
    }

    /**
     * 计算压力水平
     */
    fun getStressLevel(): StressLevel {
        return when {
            stressIndex < 1.0 -> StressLevel.LOW
            stressIndex < 2.0 -> StressLevel.MODERATE
            stressIndex < 4.0 -> StressLevel.HIGH
            else -> StressLevel.VERY_HIGH
        }
    }
}

/**
 * SpO2数据
 */
@Serializable
data class SpO2Data(
    val spo2: Double,            // 血氧饱和度百分比
    val redAC: Double,           // 红光AC分量
    val redDC: Double,           // 红光DC分量
    val irAC: Double,            // 红外光AC分量（合成）
    val irDC: Double,            // 红外光DC分量（合成）
    val ratioOfRatios: Double,   // R值
    val confidence: Double,      // 置信度
    val isValid: Boolean
) {
    /**
     * 获取SpO2健康状态
     */
    fun getHealthStatus(): SpO2HealthStatus {
        return when {
            !isValid -> SpO2HealthStatus.UNKNOWN
            spo2 >= 95 -> SpO2HealthStatus.NORMAL
            spo2 >= 90 -> SpO2HealthStatus.MILD_HYPOXEMIA
            spo2 >= 85 -> SpO2HealthStatus.MODERATE_HYPOXEMIA
            else -> SpO2HealthStatus.SEVERE_HYPOXEMIA
        }
    }
}

/**
 * 信号质量评估
 */
@Serializable
data class SignalQuality(
    val snr: Double,                    // 信噪比
    val motionArtifact: Double,         // 运动伪影水平 (0-1)
    val illuminationQuality: Double,    // 光照质量 (0-1)
    val overallQuality: Double          // 整体质量 (0-1)
) {
    fun getQualityLevel(): QualityLevel {
        return when {
            overallQuality >= 0.8 -> QualityLevel.EXCELLENT
            overallQuality >= 0.6 -> QualityLevel.GOOD
            overallQuality >= 0.4 -> QualityLevel.FAIR
            else -> QualityLevel.POOR
        }
    }
}

/**
 * API响应模型
 */
@Serializable
data class RppgApiResponse(
    val msg: String,
    val sample_id: String? = null,
    val success: Boolean = true
)

/**
 * HRV健康状态枚举
 */
enum class HrvHealthStatus(val displayName: String, val description: String) {
    EXCELLENT("极佳", "心率变异性很高，身体恢复状态良好"),
    GOOD("良好", "心率变异性正常，身体状态健康"),
    FAIR("一般", "心率变异性略低，注意休息和减压"),
    POOR("较差", "心率变异性较低，建议咨询医生"),
    UNKNOWN("未知", "数据不足，无法评估")
}

/**
 * 压力水平枚举
 */
enum class StressLevel(val displayName: String, val color: String) {
    LOW("低压力", "#4CAF50"),
    MODERATE("中等压力", "#FF9800"),
    HIGH("高压力", "#FF5722"),
    VERY_HIGH("极高压力", "#F44336")
}

/**
 * SpO2健康状态枚举
 */
enum class SpO2HealthStatus(val displayName: String, val description: String) {
    NORMAL("正常", "血氧饱和度正常"),
    MILD_HYPOXEMIA("轻度缺氧", "血氧饱和度略低"),
    MODERATE_HYPOXEMIA("中度缺氧", "血氧饱和度偏低，建议咨询医生"),
    SEVERE_HYPOXEMIA("重度缺氧", "血氧饱和度严重偏低，请立即就医"),
    UNKNOWN("未知", "数据不准确，建议重新测量")
}

/**
 * 信号质量等级
 */
enum class QualityLevel(val displayName: String) {
    EXCELLENT("优秀"),
    GOOD("良好"),
    FAIR("一般"),
    POOR("较差")
}

/**
 * 捕获会话
 */
@Serializable
data class CaptureSession(
    val sessionId: String,
    val startTime: Long = System.currentTimeMillis()
)