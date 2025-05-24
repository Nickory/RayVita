package com.codelab.basiclayouts.viewModel.physnet

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.data.physnet.RppgProcessor
import com.codelab.basiclayouts.data.physnet.RppgRepository
import com.codelab.basiclayouts.data.physnet.VideoRecorder
import com.codelab.basiclayouts.data.physnet.model.CaptureSession
import com.codelab.basiclayouts.data.physnet.model.RppgResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * rPPG ViewModel - 管理录制、推理和结果展示
 */
class RppgViewModel(
    private val context: Context,
    private val videoRecorder: VideoRecorder,
    private val rppgProcessor: RppgProcessor,
    private val repository: RppgRepository
) : ViewModel() {

    companion object {
        private const val TAG = "RppgViewModel"
        private const val RECORDING_DURATION_SECONDS = 20
        private const val TARGET_FRAME_COUNT = 500
    }

    data class RppgUiState(
        val isRecording: Boolean = false,
        val isProcessing: Boolean = false,
        val recordingProgress: Float = 0f,
        val recordingTimeSeconds: Int = 0,
        val currentResult: RppgResult? = null,
        val errorMessage: String? = null,
        val isLoading: Boolean = false,
        val loadingMessage: String = "",
        val sessionHistory: List<RppgResult> = emptyList(),
        val isFaceAligned: Boolean = false
    )

    private val _uiState = MutableStateFlow(RppgUiState())
    val uiState: StateFlow<RppgUiState> = _uiState.asStateFlow()

    private var currentSession: CaptureSession? = null

    init {
        loadSessionHistory()
    }

    fun startRecording() {
        if (_uiState.value.isRecording || _uiState.value.isProcessing) {
            Log.w(TAG, "Already recording or processing")
            return
        }

        currentSession = CaptureSession(
            sessionId = UUID.randomUUID().toString()
        )

        _uiState.value = _uiState.value.copy(
            isRecording = true,
            recordingProgress = 0f,
            recordingTimeSeconds = 0,
            errorMessage = null
        )

        videoRecorder.startRecording { frames ->
            onRecordingComplete(frames)
        }

        viewModelScope.launch {
            repeat(RECORDING_DURATION_SECONDS) { second ->
                kotlinx.coroutines.delay(1000)
                if (_uiState.value.isRecording) {
                    _uiState.value = _uiState.value.copy(
                        recordingProgress = (second + 1) / RECORDING_DURATION_SECONDS.toFloat(),
                        recordingTimeSeconds = second + 1
                    )
                }
            }
        }
    }

    fun stopRecording() {
        videoRecorder.stopRecording()
        _uiState.value = _uiState.value.copy(isRecording = false)
    }

    private fun onRecordingComplete(frames: List<Bitmap>) {
        _uiState.value = _uiState.value.copy(
            isRecording = false,
            isProcessing = true,
            isLoading = true,
            loadingMessage = "正在分析心率数据..."
        )

        viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                val (heartRate, rppgSignal) = withContext(Dispatchers.Default) {
                    rppgProcessor.processFrames(frames)
                }
                val processingTime = System.currentTimeMillis() - startTime

                val result = RppgResult(
                    sessionId = currentSession?.sessionId ?: UUID.randomUUID().toString(),
                    heartRate = heartRate,
                    rppgSignal = rppgSignal.toList(),
                    frameCount = frames.size,
                    processingTimeMs = processingTime,
                    confidence = calculateConfidence(rppgSignal)
                )

                _uiState.value = _uiState.value.copy(
                    loadingMessage = "保存测量结果..."
                )
                repository.saveResult(result)

                _uiState.value = _uiState.value.copy(
                    loadingMessage = "同步到云端..."
                )
                uploadResultToServer(result)

                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    isLoading = false,
                    loadingMessage = "",
                    currentResult = result,
                    sessionHistory = _uiState.value.sessionHistory + result
                )

                Log.d(TAG, "Processing complete: HR=${result.heartRate} BPM")
            } catch (e: Exception) {
                Log.e(TAG, "Processing failed", e)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    isLoading = false,
                    errorMessage = "处理失败: ${e.message}"
                )
            }
        }
    }

    private fun calculateConfidence(signal: FloatArray): Float {
        if (signal.isEmpty()) return 0f
        val variance = signal.map { it - signal.average() }
            .map { it * it }
            .average()
            .toFloat()
        val snr = if (variance > 0) {
            (signal.maxOrNull() ?: 0f) / kotlin.math.sqrt(variance)
        } else 0f
        return (snr / 10f).coerceIn(0f, 1f)
    }

    private suspend fun uploadResultToServer(result: RppgResult) {
        try {
            val success = repository.uploadResult(result)
            if (success) {
                Log.d(TAG, "Result uploaded successfully")
            } else {
                Log.w(TAG, "Failed to upload result")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload error", e)
        }
    }

    private fun loadSessionHistory() {
        viewModelScope.launch {
            try {
                val history = repository.getRecentResults(10)
                _uiState.value = _uiState.value.copy(
                    sessionHistory = history
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load history", e)
            }
        }
    }

    fun updateFaceAlignment(isAligned: Boolean) {
        _uiState.value = _uiState.value.copy(isFaceAligned = isAligned)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getVideoRecorder(): VideoRecorder = videoRecorder

    override fun onCleared() {
        super.onCleared()
        videoRecorder.release()
        rppgProcessor.release()
    }
}