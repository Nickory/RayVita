package com.codelab.basiclayouts.viewmodel.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.data.scan.HeartRateMeasurement
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sin

class ScanViewModel<T> : ViewModel() {

    private val _heartRate = MutableStateFlow<HeartRateMeasurement>(
        value = HeartRateMeasurement(
            timestamp = 0L,
            value = 0,
            confidence = 0f
        )
    )
    val heartRate: StateFlow<HeartRateMeasurement> = _heartRate.asStateFlow()

    private val _signalBuffer = MutableStateFlow<List<Float>>(emptyList())
    val signalBuffer: StateFlow<List<Float>> = _signalBuffer.asStateFlow()

    private val _isMeasuring = MutableStateFlow(false)
    val isMeasuring: StateFlow<Boolean> = _isMeasuring.asStateFlow()

    private val signalInternal = mutableListOf<Float>()

    fun toggleMeasurement() {
        _isMeasuring.value = !_isMeasuring.value
        if (_isMeasuring.value) {
            startSimulatedMeasurement()
        } else {
            stopMeasurement()
        }
    }

    fun onFaceDetected(detected: Boolean) {
        // Handle UI logic or visual cues if needed
        // Example: if (!detected) stopMeasurement()
    }

    fun processFrame(frame: Float) {
        // TODO: Implement real image analysis for rPPG extraction
        // For now, use mock data
        val mockValue = (50..100).random().toFloat().coerceIn(0f, 100f)
        signalInternal.add(mockValue)
        if (signalInternal.size > 150) {
            signalInternal.removeAt(0)
        }
        _signalBuffer.value = signalInternal.toList()
    }

    private fun startSimulatedMeasurement() {
        viewModelScope.launch {
            while (_isMeasuring.value) {
                val t = System.currentTimeMillis() / 1000.0
                val simulatedBpm = (70 + 10 * sin(t)).toInt().coerceIn(30, 200)
                val confidence = 0.85f.coerceIn(0f, 1f)
                val timestamp = System.currentTimeMillis()

                // Update signal buffer
                val fakeSignal = 80 + 10 * sin(t).toFloat()
                signalInternal.add(fakeSignal)
                if (signalInternal.size > 150) {
                    signalInternal.removeAt(0)
                }
                _signalBuffer.value = signalInternal.toList()

                // Update heart rate
                _heartRate.value = HeartRateMeasurement(
                    timestamp = timestamp,
                    value = simulatedBpm,
                    confidence = confidence
                )

                delay(1000L)
            }
        }
    }

    private fun stopMeasurement() {
        signalInternal.clear()
        _signalBuffer.value = emptyList()
        _heartRate.value = HeartRateMeasurement(
            timestamp = 0L,
            value = 0,
            confidence = 0f
        )
    }
}