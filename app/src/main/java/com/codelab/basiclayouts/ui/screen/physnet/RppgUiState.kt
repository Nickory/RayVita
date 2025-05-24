package com.codelab.basiclayouts.ui.screen.physnet

import androidx.compose.runtime.Immutable

/**
 * rPPG UI 状态类，存储屏幕的所有状态信息
 */
@Immutable
data class RppgUiState(
    val isFaceAligned: Boolean = false, // 面部是否对准
    val isRecording: Boolean = false, // 是否正在录制
    val recordingProgress: Float = 0f, // 录制进度 (0f 到 1f)
    val recordingTimeSeconds: Int = 0, // 录制时间（秒）
    val isProcessing: Boolean = false, // 是否正在处理数据
    val isLoading: Boolean = false, // 是否显示加载覆盖层
    val loadingMessage: String = "正在处理…", // 加载时的消息
    val currentResult: RppgResult? = null, // 当前测量结果
    val errorMessage: String? = null, // 错误消息（如果有）
    val sessionHistory: List<RppgResult> = emptyList() // 历史测量记录
)

/**
 * rPPG 测量结果类，存储一次测量的数据
 */
@Immutable
data class RppgResult(
    val sessionId: String, // 会话 ID
    val heartRate: Float, // 心率 (bpm)
    val rppgSignal: List<Float>, // rPPG 信号数据
    val frameCount: Int, // 帧数
    val processingTimeMs: Long, // 处理时间（毫秒）
    val confidence: Float // 置信度 (0f 到 1f)
)