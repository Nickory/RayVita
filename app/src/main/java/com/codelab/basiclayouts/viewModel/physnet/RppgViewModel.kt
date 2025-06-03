package com.codelab.basiclayouts.viewModel.physnet

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.data.physnet.EnhancedRppgProcessor
import com.codelab.basiclayouts.data.physnet.EnhancedRppgRepository
import com.codelab.basiclayouts.data.physnet.HrvCalculator
import com.codelab.basiclayouts.data.physnet.SpO2Calculator
import com.codelab.basiclayouts.data.physnet.VideoRecorder
import com.codelab.basiclayouts.data.physnet.model.CaptureSession
import com.codelab.basiclayouts.data.physnet.model.EnhancedRppgResult
import com.codelab.basiclayouts.data.physnet.model.HrvData
import com.codelab.basiclayouts.data.physnet.model.SignalQuality
import com.codelab.basiclayouts.data.physnet.model.SpO2Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.abs
import kotlin.math.log10

class EnhancedRppgViewModel(
    private val context: Context,
    private val videoRecorder: VideoRecorder,
    private val rppgProcessor: EnhancedRppgProcessor,
    private val repository: EnhancedRppgRepository,
    private val storageViewModel: MeasurementStorageViewModel // Inject new ViewModel
) : ViewModel() {

    companion object {
        private const val TAG = "EnhancedRppgViewModel"
        private const val RECORDING_DURATION_SECONDS = 20
        private const val TARGET_FRAME_COUNT = 500
        private const val SAMPLING_RATE = 25f
    }

    data class EnhancedRppgUiState(
        val isRecording: Boolean = false,
        val isProcessing: Boolean = false,
        val recordingProgress: Float = 0f,
        val recordingTimeSeconds: Int = 0,
        val currentResult: EnhancedRppgResult? = null,
        val errorMessage: String? = null,
        val isLoading: Boolean = false,
        val loadingMessage: String = "",
        val sessionHistory: List<EnhancedRppgResult> = emptyList(),
        val isFaceAligned: Boolean = false,
        val showHrvDetails: Boolean = false,
        val showSpO2Details: Boolean = false,
        val analysisMode: AnalysisMode = AnalysisMode.ALL
    )

    enum class AnalysisMode {
        HEART_RATE_ONLY, HRV_ONLY, SPO2_ONLY, ALL
    }

    private val _uiState = MutableStateFlow(EnhancedRppgUiState())
    val uiState: StateFlow<EnhancedRppgUiState> = _uiState.asStateFlow()

    private var currentSession: CaptureSession? = null
    private val hrvCalculator = HrvCalculator()
    private val spo2Calculator = SpO2Calculator()

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
                delay(1000)
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
            loadingMessage = context.getString(R.string.physstate_extracting_signal_data)
        )

        viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()

                // 1. Process rPPG
                _uiState.value = _uiState.value.copy(
                    loadingMessage = context.getString(R.string.physstate_analyzing_heart_rate)
                )
                val processingResult = withContext(Dispatchers.Default) {
                    rppgProcessor.processFramesWithRGB(frames)
                }

                // 2. Calculate signal quality
                _uiState.value = _uiState.value.copy(
                    loadingMessage = context.getString(R.string.physstate_evaluating_signal_quality)
                )
                val signalQuality = calculateSignalQuality(processingResult.rppgSignal, processingResult.rgbData)

                var hrvResult: HrvData? = null
                var spo2Result: SpO2Data? = null

                // 3. HRV analysis
                if (_uiState.value.analysisMode in listOf(AnalysisMode.HRV_ONLY, AnalysisMode.ALL)) {
                    _uiState.value = _uiState.value.copy(
                        loadingMessage = context.getString(R.string.physstate_analyzing_hrv)
                    )
                    val hrvCalculationResult = withContext(Dispatchers.Default) {
                        hrvCalculator.calculateHRV(processingResult.rppgSignal, SAMPLING_RATE)
                    }
                    if (hrvCalculationResult.isValid) {
                        hrvResult = HrvData(
                            rmssd = hrvCalculationResult.rmssd,
                            pnn50 = hrvCalculationResult.pnn50,
                            sdnn = hrvCalculationResult.sdnn,
                            meanRR = hrvCalculationResult.meanRR,
                            triangularIndex = hrvCalculationResult.triangularIndex,
                            stressIndex = hrvCalculationResult.stressIndex,
                            isValid = true
                        )
                        Log.d(TAG, "HRV计算成功: RMSSD=${hrvResult.rmssd}, 压力指数=${hrvResult.stressIndex}")
                    }
                }

                // 4. SpO2 analysis
                if (_uiState.value.analysisMode in listOf(AnalysisMode.SPO2_ONLY, AnalysisMode.ALL)) {
                    _uiState.value = _uiState.value.copy(
                        loadingMessage = context.getString(R.string.physstate_calculating_spo2)
                    )
                    val spo2CalculationResult = withContext(Dispatchers.Default) {
                        spo2Calculator.calculateSpO2(
                            processingResult.rgbData.redChannel,
                            processingResult.rgbData.greenChannel,
                            processingResult.rgbData.blueChannel,
                            SAMPLING_RATE
                        )
                    }
                    if (spo2CalculationResult.isValid && spo2CalculationResult.confidence > 0.3) {
                        spo2Result = SpO2Data(
                            spo2 = spo2CalculationResult.spo2,
                            redAC = spo2CalculationResult.redAC,
                            redDC = spo2CalculationResult.redDC,
                            irAC = spo2CalculationResult.irAC,
                            irDC = spo2CalculationResult.irDC,
                            ratioOfRatios = spo2CalculationResult.ratioOfRatios,
                            confidence = spo2CalculationResult.confidence,
                            isValid = true
                        )
                        Log.d(TAG, "SpO2计算成功: ${spo2Result.spo2}%, 置信度=${spo2Result.confidence}")
                    } else {
                        Log.w(TAG, "SpO2计算质量不足，置信度=${spo2CalculationResult.confidence}")
                    }
                }

                val processingTime = System.currentTimeMillis() - startTime

                // 5. Create result
                val result = EnhancedRppgResult(
                    sessionId = currentSession?.sessionId ?: UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    heartRate = processingResult.heartRate,
                    rppgSignal = processingResult.rppgSignal.toList(),
                    frameCount = frames.size,
                    processingTimeMs = processingTime,
                    confidence = calculateOverallConfidence(signalQuality, hrvResult, spo2Result),
                    hrvResult = hrvResult,
                    spo2Result = spo2Result,
                    signalQuality = signalQuality
                )

                // 6. Save to repository
                _uiState.value = _uiState.value.copy(
                    loadingMessage = context.getString(R.string.physstate_saving_results)
                )
                saveEnhancedResult(result)

                // 7. Save to local JSON file
                storageViewModel.saveMeasurement(result)

                // 8. Upload to server
                _uiState.value = _uiState.value.copy(
                    loadingMessage = context.getString(R.string.physstate_syncing_to_cloud)
                )
                uploadResultToServer(result)

                // 9. Update UI state
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    isLoading = false,
                    loadingMessage = "",
                    currentResult = result,
                    sessionHistory = _uiState.value.sessionHistory + result
                )

                Log.d(TAG, "增强处理完成: HR=${result.heartRate} BPM, " +
                        "HRV=${hrvResult?.isValid}, SpO2=${spo2Result?.isValid}")

            } catch (e: Exception) {
                Log.e(TAG, "增强处理失败", e)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    isLoading = false,
                    errorMessage = context.getString(R.string.physstate_processing_failed, e.message ?: "")
                )
            }
        }
    }

    private fun calculateSignalQuality(
        rppgSignal: FloatArray,
        rgbData: EnhancedRppgProcessor.RgbChannelData
    ): SignalQuality {
        val mean = rppgSignal.average()
        val signalPower = mean * mean
        val noisePower = rppgSignal.map { (it - mean) * (it - mean) }.average()
        val snr = if (noisePower > 0) 10 * log10(signalPower / noisePower) else 0.0
        val derivatives = rppgSignal.toList().zipWithNext { a, b -> abs(b - a) }
        val avgDerivative = derivatives.average()
        val maxSignal = rppgSignal.maxOrNull() ?: 1f
        val motionArtifact = 1.0 - (avgDerivative / maxSignal).coerceIn(0.0, 1.0)
        val avgIntensity = (rgbData.redChannel.average() +
                rgbData.greenChannel.average() +
                rgbData.blueChannel.average()) / 3.0
        val illuminationQuality = (avgIntensity / 255.0).coerceIn(0.0, 1.0)
        val overallQuality = (snr / 30.0 * 0.4 + motionArtifact * 0.3 + illuminationQuality * 0.3)
            .coerceIn(0.0, 1.0)
        return SignalQuality(snr, 1.0 - motionArtifact, illuminationQuality, overallQuality)
    }

    private fun calculateOverallConfidence(
        signalQuality: SignalQuality,
        hrvResult: HrvData?,
        spo2Result: SpO2Data?
    ): Float {
        var totalConfidence = signalQuality.overallQuality.toFloat()
        var count = 1
        hrvResult?.let {
            if (it.isValid) {
                val hrvConfidence = (it.rmssd / 100.0).coerceIn(0.0, 1.0).toFloat()
                totalConfidence += hrvConfidence
                count++
            }
        }
        spo2Result?.let {
            if (it.isValid) {
                totalConfidence += it.confidence.toFloat()
                count++
            }
        }
        return (totalConfidence / count).coerceIn(0f, 1f)
    }

    private suspend fun saveEnhancedResult(result: EnhancedRppgResult) {
        try {
            repository.saveEnhancedResult(result)
            Log.d(TAG, "增强结果保存成功")
        } catch (e: Exception) {
            Log.e(TAG, "保存增强结果失败，尝试保存基础结果", e)
            try {
                repository.saveResult(result.toRppgResult())
                Log.d(TAG, "基础结果保存成功")
            } catch (e2: Exception) {
                Log.e(TAG, "保存基础结果也失败", e2)
            }
        }
    }

    private suspend fun uploadResultToServer(result: EnhancedRppgResult) {
        try {
            val success = repository.uploadEnhancedResult(result)
            if (success) {
                Log.d(TAG, "增强结果上传成功")
            } else {
                Log.w(TAG, "增强结果上传失败，尝试上传基础结果")
                repository.uploadResult(result.toRppgResult())
            }
        } catch (e: Exception) {
            Log.e(TAG, "上传错误", e)
        }
    }

    private fun loadSessionHistory() {
        viewModelScope.launch {
            try {
                val enhancedHistory = repository.getRecentEnhancedResults(10)
                if (enhancedHistory.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        sessionHistory = enhancedHistory
                    )
                    Log.d(TAG, "加载了${enhancedHistory.size}条增强历史记录")
                } else {
                    val history = repository.getRecentResults(10)
                    val convertedHistory = history.map { originalResult ->
                        EnhancedRppgResult(
                            sessionId = originalResult.sessionId,
                            timestamp = originalResult.timestamp,
                            heartRate = originalResult.heartRate,
                            rppgSignal = originalResult.rppgSignal,
                            frameCount = originalResult.frameCount,
                            processingTimeMs = originalResult.processingTimeMs,
                            confidence = originalResult.confidence,
                            hrvResult = null,
                            spo2Result = null,
                            signalQuality = SignalQuality(
                                0.0,
                                0.0,
                                0.0,
                                originalResult.confidence.toDouble()
                            )
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        sessionHistory = convertedHistory
                    )
                    Log.d(TAG, "转换了${convertedHistory.size}条历史记录")
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载历史记录失败", e)
                _uiState.value = _uiState.value.copy(
                    sessionHistory = emptyList()
                )
            }
        }
    }

    fun toggleHrvDetails() {
        _uiState.value = _uiState.value.copy(
            showHrvDetails = !_uiState.value.showHrvDetails
        )
    }

    fun toggleSpO2Details() {
        _uiState.value = _uiState.value.copy(
            showSpO2Details = !_uiState.value.showSpO2Details
        )
    }

    fun setAnalysisMode(mode: AnalysisMode) {
        _uiState.value = _uiState.value.copy(analysisMode = mode)
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