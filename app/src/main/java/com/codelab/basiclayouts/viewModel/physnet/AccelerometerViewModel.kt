package com.codelab.basiclayouts.viewModel.physnet


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * 独立的加速度计检测ViewModel
 */
class AccelerometerViewModel(
    private val context: Context
) : ViewModel(), SensorEventListener {

    companion object {
        private const val TAG = "AccelerometerViewModel"

        // 运动检测参数
        private const val MOTION_THRESHOLD = 0.5f // 运动阈值
        private const val STATIONARY_THRESHOLD = 0.2f // 静止阈值
        private const val SAMPLE_COUNT = 10 // 样本数量用于平滑
    }

    data class MotionState(
        val isStationary: Boolean = true,
        val motionLevel: Float = 0f,
        val motionStatus: String = "Stationary",
        val isDetectionActive: Boolean = false
    )

    // 传感器相关
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val accelerationSamples = mutableListOf<Float>()

    private val _motionState = MutableStateFlow(MotionState())
    val motionState: StateFlow<MotionState> = _motionState.asStateFlow()

    init {
        checkSensorAvailability()
    }

    private fun checkSensorAvailability() {
        if (accelerometer == null) {
            Log.w(TAG, "Accelerometer not available on this device")
            _motionState.value = _motionState.value.copy(
                motionStatus = "Sensor Unavailable"
            )
        } else {
            Log.d(TAG, "Accelerometer available")
        }
    }

    fun startDetection() {
        accelerometer?.let { sensor ->
            val result = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )

            _motionState.value = _motionState.value.copy(
                isDetectionActive = result
            )

            if (result) {
                Log.d(TAG, "Motion detection started")
            } else {
                Log.e(TAG, "Failed to start motion detection")
            }
        } ?: run {
            Log.w(TAG, "Cannot start detection - accelerometer unavailable")
        }
    }

    fun stopDetection() {
        sensorManager.unregisterListener(this)
        accelerationSamples.clear()

        _motionState.value = _motionState.value.copy(
            isDetectionActive = false,
            isStationary = true,
            motionLevel = 0f,
            motionStatus = "Detection Stopped"
        )

        Log.d(TAG, "Motion detection stopped")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            if (sensorEvent.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = sensorEvent.values[0]
                val y = sensorEvent.values[1]
                val z = sensorEvent.values[2]

                // 计算总加速度
                val totalAcceleration = sqrt(x * x + y * y + z * z)

                // 计算加速度变化量（去除重力影响）
                val deltaAcceleration = kotlin.math.abs(totalAcceleration - SensorManager.GRAVITY_EARTH)

                // 添加到样本列表
                accelerationSamples.add(deltaAcceleration)
                if (accelerationSamples.size > SAMPLE_COUNT) {
                    accelerationSamples.removeAt(0)
                }

                // 计算平均加速度变化
                val avgDelta = accelerationSamples.average().toFloat()

                // 判断是否静止（使用滞后比较避免频繁切换）
                val currentState = _motionState.value
                val isCurrentlyStationary = if (currentState.isStationary) {
                    avgDelta < MOTION_THRESHOLD
                } else {
                    avgDelta < STATIONARY_THRESHOLD
                }

                val motionStatus = if (isCurrentlyStationary) {
                    "Stationary"
                } else {
                    when {
                        avgDelta > 2.0f -> "High Motion"
                        avgDelta > 1.0f -> "Moderate Motion"
                        else -> "Light Motion"
                    }
                }

                // 更新状态
                _motionState.value = currentState.copy(
                    isStationary = isCurrentlyStationary,
                    motionLevel = avgDelta,
                    motionStatus = motionStatus
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }

    /**
     * 获取运动状态的描述性文本
     */
    fun getMotionDescription(): String {
        val state = _motionState.value
        return when {
            !state.isDetectionActive -> "Motion detection inactive"
            state.isStationary -> "Device is stationary - ready for measurement"
            else -> "Device is moving - please keep still for accurate measurement"
        }
    }

    /**
     * 检查是否适合开始测量
     */
    fun isReadyForMeasurement(): Boolean {
        val state = _motionState.value
        return state.isDetectionActive && state.isStationary
    }

    override fun onCleared() {
        super.onCleared()
        stopDetection()
        Log.d(TAG, "AccelerometerViewModel cleared")
    }
}

/**
 * AccelerometerViewModel工厂类
 */
class AccelerometerViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccelerometerViewModel::class.java)) {
            return AccelerometerViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}