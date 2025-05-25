package com.codelab.basiclayouts.data.physnet


import android.util.Log
import kotlin.math.*

/**
 * SpO2计算器 - 从RGB信号估算血氧饱和度
 * 注意：手机摄像头计算SpO2准确性有限，仅供参考
 */
class SpO2Calculator {

    companion object {
        private const val TAG = "SpO2Calculator"
        // 经验校准系数（需要根据实际测试调整）
        private const val CALIBRATION_A = -45.06
        private const val CALIBRATION_B = 30.354
        private const val CALIBRATION_C = 94.845
    }

    data class SpO2Result(
        val spo2: Double,         // 血氧饱和度百分比
        val redAC: Double,        // 红光AC分量
        val redDC: Double,        // 红光DC分量
        val irAC: Double,         // 红外光AC分量 (合成)
        val irDC: Double,         // 红外光DC分量 (合成)
        val ratioOfRatios: Double, // R值
        val confidence: Double,   // 置信度 (0-1)
        val isValid: Boolean      // 结果是否有效
    )

    /**
     * 从RGB信号计算SpO2
     * @param redChannel 红色通道信号
     * @param greenChannel 绿色通道信号
     * @param blueChannel 蓝色通道信号
     * @param samplingRate 采样率
     * @return SpO2计算结果
     */
    fun calculateSpO2(
        redChannel: FloatArray,
        greenChannel: FloatArray,
        blueChannel: FloatArray,
        samplingRate: Float = 25f
    ): SpO2Result {

        return try {
            Log.d(TAG, "开始SpO2计算，信号长度: ${redChannel.size}")

            // 1. 验证输入
            if (!validateInput(redChannel, greenChannel, blueChannel)) {
                return SpO2Result(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false)
            }

            // 2. 预处理信号
            val filteredRed = bandpassFilter(redChannel, samplingRate, 0.5f, 4.0f)
            val filteredGreen = bandpassFilter(greenChannel, samplingRate, 0.5f, 4.0f)
            val filteredBlue = bandpassFilter(blueChannel, samplingRate, 0.5f, 4.0f)

            // 3. 计算AC和DC分量
            val redAC = calculateAC(filteredRed)
            val redDC = calculateDC(redChannel)

            // 4. 使用绿色+蓝色通道合成红外信号
            val syntheticIR = combineChannelsForIR(filteredGreen, filteredBlue)
            val irAC = calculateAC(syntheticIR)
            val irDC = calculateDC(syntheticIR)

            // 5. 检查信号质量
            val redSNR = calculateSNR(filteredRed)
            val irSNR = calculateSNR(syntheticIR)
            val confidence = calculateConfidence(redSNR, irSNR, redAC, irAC)

            Log.d(TAG, "信号质量: Red SNR=$redSNR, IR SNR=$irSNR, Confidence=$confidence")

            if (confidence < 0.3 || redDC == 0.0 || irDC == 0.0) {
                Log.w(TAG, "信号质量不足，无法计算SpO2")
                return SpO2Result(0.0, redAC, redDC, irAC, irDC, 0.0, confidence, false)
            }

            // 6. 计算比值的比值 (R)
            val ratioRed = redAC / redDC
            val ratioIR = irAC / irDC
            val ratioOfRatios = if (ratioIR != 0.0) abs(ratioRed / ratioIR) else 0.0

            // 7. 使用经验公式计算SpO2
            val spo2 = calculateSpO2FromRatio(ratioOfRatios)

            // 8. 验证结果合理性
            val isValid = spo2 in 70.0..100.0 && confidence > 0.3

            Log.d(TAG, "SpO2计算完成: $spo2%, R=$ratioOfRatios, Valid=$isValid")

            SpO2Result(
                spo2 = if (isValid) spo2 else 0.0,
                redAC = redAC,
                redDC = redDC,
                irAC = irAC,
                irDC = irDC,
                ratioOfRatios = ratioOfRatios,
                confidence = confidence,
                isValid = isValid
            )

        } catch (e: Exception) {
            Log.e(TAG, "SpO2计算失败", e)
            SpO2Result(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false)
        }
    }

    /**
     * 验证输入数据
     */
    private fun validateInput(
        redChannel: FloatArray,
        greenChannel: FloatArray,
        blueChannel: FloatArray
    ): Boolean {
        if (redChannel.isEmpty() || greenChannel.isEmpty() || blueChannel.isEmpty()) {
            Log.e(TAG, "输入通道为空")
            return false
        }

        if (redChannel.size != greenChannel.size || redChannel.size != blueChannel.size) {
            Log.e(TAG, "RGB通道长度不一致")
            return false
        }

        // 检查是否包含NaN或无穷大
        val hasInvalidRed = redChannel.any { it.isNaN() || it.isInfinite() }
        val hasInvalidGreen = greenChannel.any { it.isNaN() || it.isInfinite() }
        val hasInvalidBlue = blueChannel.any { it.isNaN() || it.isInfinite() }

        if (hasInvalidRed || hasInvalidGreen || hasInvalidBlue) {
            Log.e(TAG, "输入数据包含无效值")
            return false
        }

        return true
    }

    /**
     * 带通滤波器 - 使用与RppgProcessor类似的实现
     */
    private fun bandpassFilter(
        signal: FloatArray,
        samplingRate: Float,
        lowCutoff: Float,
        highCutoff: Float
    ): FloatArray {
        val filteredSignal = signal.copyOf()

        // 1. 高通滤波 (去除低频噪声)
        val alphaHigh = calculateAlpha(samplingRate, lowCutoff)
        var prevInput = filteredSignal[0]
        var prevOutput = 0f

        for (i in 1 until filteredSignal.size) {
            val output = alphaHigh * (prevOutput + filteredSignal[i] - prevInput)
            filteredSignal[i] = output
            prevInput = filteredSignal[i]
            prevOutput = output
        }

        // 2. 低通滤波 (去除高频噪声)
        val alphaLow = calculateAlpha(samplingRate, highCutoff)
        prevOutput = filteredSignal[0]

        for (i in 1 until filteredSignal.size) {
            val output = alphaLow * filteredSignal[i] + (1 - alphaLow) * prevOutput
            filteredSignal[i] = output
            prevOutput = output
        }

        return filteredSignal
    }

    private fun calculateAlpha(samplingRate: Float, cutoffFreq: Float): Float {
        val rc = 1.0f / (2.0f * PI.toFloat() * cutoffFreq)
        val dt = 1.0f / samplingRate
        return dt / (rc + dt)
    }

    /**
     * 计算AC分量 (变化部分的标准差)
     */
    private fun calculateAC(signal: FloatArray): Double {
        if (signal.isEmpty()) return 0.0

        val mean = signal.average()
        val variance = signal.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance)
    }

    /**
     * 计算DC分量 (平均值)
     */
    private fun calculateDC(signal: FloatArray): Double {
        return if (signal.isNotEmpty()) signal.average() else 0.0
    }

    /**
     * 合并绿色和蓝色通道模拟红外光
     */
    private fun combineChannelsForIR(green: FloatArray, blue: FloatArray): FloatArray {
        require(green.size == blue.size) { "Green and blue channels must have same size" }

        return FloatArray(green.size) { i ->
            // 经验加权组合 - 绿色通道更接近红外特性
            0.7f * green[i] + 0.3f * blue[i]
        }
    }

    /**
     * 计算信噪比
     */
    private fun calculateSNR(signal: FloatArray): Double {
        if (signal.isEmpty()) return 0.0

        val mean = signal.average()
        val signalPower = mean * mean
        val noisePower = signal.map { (it - mean) * (it - mean) }.average()

        return if (noisePower > 0) 10 * log10(signalPower / noisePower) else 0.0
    }

    /**
     * 计算置信度
     */
    private fun calculateConfidence(
        redSNR: Double,
        irSNR: Double,
        redAC: Double,
        irAC: Double
    ): Double {
        // 基于信噪比和AC分量强度的置信度计算
        val snrFactor = ((redSNR + irSNR) / 40.0).coerceIn(0.0, 1.0)
        val acFactor = ((redAC + irAC) / 20.0).coerceIn(0.0, 1.0)

        return (snrFactor * 0.6 + acFactor * 0.4).coerceIn(0.0, 1.0)
    }

    /**
     * 从比值计算SpO2
     */
    private fun calculateSpO2FromRatio(ratio: Double): Double {
        // 使用经验校准公式 (需要根据实际设备调整)
        return when {
            ratio <= 0.4 -> 100.0
            ratio >= 3.4 -> 70.0
            else -> {
                // 二次多项式拟合
                CALIBRATION_A * ratio * ratio + CALIBRATION_B * ratio + CALIBRATION_C
            }
        }.coerceIn(70.0, 100.0)
    }
}