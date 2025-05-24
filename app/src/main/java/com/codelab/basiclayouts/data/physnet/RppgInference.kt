package com.codelab.basiclayouts.data.physnet

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer

/**
 * rPPG推理引擎 - 升级版
 * 支持更好的错误处理、性能监控和内存管理
 */
class RppgInference(context: Context) {

    companion object {
        private const val TAG = "RppgInference"
        private const val MODEL_NAME = "physnet_rppg.ort"
        private const val INPUT_NAME = "input"
        private val EXPECTED_INPUT_SHAPE = longArrayOf(1, 3, 500, 128, 128)
    }

    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null
    private var isInitialized = false

    init {
        initializeModel(context)
    }

    /**
     * 初始化模型
     */
    private fun initializeModel(context: Context) {
        try {
            Log.d(TAG, "开始初始化rPPG模型...")
            val startTime = System.currentTimeMillis()

            // 检查模型文件是否存在
            val modelPath = "com/codelab/basiclayouts/physnet/$MODEL_NAME"
            val assetFiles = context.assets.list("")?.toList() ?: emptyList()
            Log.d(TAG, "Assets目录文件: ${assetFiles.joinToString()}")

            // 加载模型文件
            val modelBytes = try {
                context.assets.open(modelPath).use { inputStream ->
                    inputStream.readBytes()
                }
            } catch (e: Exception) {
                Log.e(TAG, "无法找到模型文件: $modelPath", e)
                throw IllegalStateException("模型文件不存在: $modelPath", e)
            }

            Log.d(TAG, "模型文件大小: ${modelBytes.size} bytes")

            // 创建会话选项
            val sessionOptions = OrtSession.SessionOptions().apply {
                try {
                    // 尝试启用硬件加速
                    addNnapi()
                    Log.d(TAG, "NNAPI硬件加速已启用")
                } catch (e: Exception) {
                    Log.w(TAG, "NNAPI不可用，使用CPU: ${e.message}")
                }

                // 设置其他优化选项
                setIntraOpNumThreads(4)
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            }

            // 创建推理会话
            session = env.createSession(modelBytes, sessionOptions)

            // 验证模型输入输出
            validateModelSchema()

            val initTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "模型初始化完成，耗时: ${initTime}ms")

            isInitialized = true

        } catch (e: Exception) {
            Log.e(TAG, "模型初始化失败", e)
            isInitialized = false
            throw IllegalStateException("无法初始化rPPG模型", e)
        }
    }

    /**
     * 验证模型架构
     */
    private fun validateModelSchema() {
        val currentSession = session ?: throw IllegalStateException("会话未初始化")

        try {
            val inputInfo = currentSession.inputInfo
            val outputInfo = currentSession.outputInfo

            Log.d(TAG, "模型输入信息:")
            inputInfo.forEach { (name, info) ->
                Log.d(TAG, "  $name: $info")
            }

            Log.d(TAG, "模型输出信息:")
            outputInfo.forEach { (name, info) ->
                Log.d(TAG, "  $name: $info")
            }

            // 检查输入名称
            if (!inputInfo.containsKey(INPUT_NAME)) {
                throw IllegalStateException("模型输入名称不匹配，期望: $INPUT_NAME")
            }

        } catch (e: Exception) {
            Log.e(TAG, "模型架构验证失败", e)
            throw e
        }
    }

    /**
     * 运行推理
     */
    suspend fun runInference(inputArray: FloatArray): FloatArray = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            throw IllegalStateException("推理引擎未初始化")
        }

        val currentSession = session ?: throw IllegalStateException("会话已释放")

        try {
            Log.d(TAG, "开始推理...")
            val inferenceStart = System.currentTimeMillis()

            // 验证输入数据
            validateInput(inputArray)

            // 创建输入张量
            val inputTensor = createInputTensor(inputArray)

            try {
                // 执行推理
                val results = currentSession.run(mapOf(INPUT_NAME to inputTensor))

                try {
                    // 处理输出
                    val output = processOutput(results)

                    val inferenceTime = System.currentTimeMillis() - inferenceStart
                    Log.d(TAG, "推理完成，耗时: ${inferenceTime}ms")

                    return@withContext output

                } finally {
                    // 清理结果
                    results.forEach { it.value.close() }
                }

            } finally {
                // 清理输入张量
                inputTensor.close()
            }

        } catch (e: Exception) {
            Log.e(TAG, "推理失败", e)
            throw RuntimeException("rPPG推理失败", e)
        }
    }

    /**
     * 验证输入数据
     */
    private fun validateInput(inputArray: FloatArray) {
        val expectedSize = EXPECTED_INPUT_SHAPE.reduce { acc, dim -> acc * dim }.toInt()

        if (inputArray.size != expectedSize) {
            throw IllegalArgumentException(
                "输入数据大小不匹配。期望: $expectedSize，实际: ${inputArray.size}"
            )
        }

        // 检查数据范围（标准化后的数据应该在合理范围内）
        val min = inputArray.minOrNull() ?: 0f
        val max = inputArray.maxOrNull() ?: 0f

        if (min < -10f || max > 10f) {
            Log.w(TAG, "输入数据范围异常: min=$min, max=$max")
        }

        // 检查是否包含NaN或无穷大
        val hasInvalidValues = inputArray.any { it.isNaN() || it.isInfinite() }
        if (hasInvalidValues) {
            throw IllegalArgumentException("输入数据包含NaN或无穷大值")
        }

        Log.d(TAG, "输入验证通过: size=${inputArray.size}, range=[$min, $max]")
    }

    /**
     * 创建输入张量
     */
    private fun createInputTensor(inputArray: FloatArray): OnnxTensor {
        return try {
            val buffer = FloatBuffer.wrap(inputArray)
            OnnxTensor.createTensor(env, buffer, EXPECTED_INPUT_SHAPE)
        } catch (e: Exception) {
            Log.e(TAG, "创建输入张量失败", e)
            throw RuntimeException("无法创建输入张量", e)
        }
    }

    /**
     * 处理模型输出
     */
    private fun processOutput(results: OrtSession.Result): FloatArray {
        try {
            if (results.size() == 0) {
                throw IllegalStateException("模型无输出结果")
            }

            val output = results[0].value
            Log.d(TAG, "原始输出类型: ${output?.javaClass}")

            // 处理不同的输出格式
            val rawOutput = when (output) {
                is Array<*> -> {
                    // 处理 [1, 5, 500] 格式的输出
                    when {
                        output.isArrayOf<Array<FloatArray>>() -> {
                            @Suppress("UNCHECKED_CAST")
                            val outputArray = output as Array<Array<FloatArray>>

                            if (outputArray.isEmpty() || outputArray[0].isEmpty()) {
                                throw IllegalStateException("输出数组为空")
                            }

                            // 分析所有通道
                            val channels = outputArray[0]
                            Log.d(TAG, "输出通道数: ${channels.size}")

                            channels.forEachIndexed { i, channel ->
                                val min = channel.minOrNull() ?: 0f
                                val max = channel.maxOrNull() ?: 0f
                                val mean = channel.average().toFloat()
                                Log.d(TAG, "通道 $i: min=$min, max=$max, mean=$mean, size=${channel.size}")
                            }

                            // 选择最佳通道（通常是第0个通道，或者可以基于信号质量选择）
                            selectBestChannel(channels)
                        }
                        else -> {
                            throw IllegalStateException("不支持的输出格式: ${output.javaClass}")
                        }
                    }
                }
                is FloatArray -> {
                    output
                }
                else -> {
                    throw IllegalStateException("未知的输出类型: ${output?.javaClass}")
                }
            }

            // 验证输出
            if (rawOutput.isEmpty()) {
                throw IllegalStateException("输出信号为空")
            }

            // 应用后处理
            val processedOutput = postProcessSignal(rawOutput)

            Log.d(TAG, "输出处理完成: size=${processedOutput.size}")
            return processedOutput

        } catch (e: Exception) {
            Log.e(TAG, "处理输出失败", e)
            throw RuntimeException("无法处理模型输出", e)
        }
    }

    /**
     * 选择最佳输出通道
     */
    private fun selectBestChannel(channels: Array<FloatArray>): FloatArray {
        if (channels.isEmpty()) {
            throw IllegalStateException("没有可用的输出通道")
        }

        // 计算每个通道的信号质量指标
        val channelScores = channels.mapIndexed { index, channel ->
            val variance = channel.map { it - channel.average() }
                .map { it * it }
                .average()

            val snr = if (variance > 0) {
                (channel.maxOrNull() ?: 0f) / kotlin.math.sqrt(variance.toFloat())
            } else 0f

            Log.d(TAG, "通道 $index SNR: $snr")
            index to snr
        }

        // 选择SNR最高的通道
        val bestChannel = channelScores.maxByOrNull { it.second }?.first ?: 0
        Log.d(TAG, "选择通道: $bestChannel")

        return channels[bestChannel]
    }

    /**
     * 信号后处理
     */
    private fun postProcessSignal(signal: FloatArray): FloatArray {
        try {
            // 1. 去除异常值
            val cleanedSignal = removeOutliers(signal)

            // 2. 应用带通滤波
            val filteredSignal = applyBandpassFilter(cleanedSignal, 25f)

            // 3. 标准化
            val normalizedSignal = normalizeSignal(filteredSignal)

            return normalizedSignal

        } catch (e: Exception) {
            Log.e(TAG, "信号后处理失败", e)
            return signal // 返回原始信号
        }
    }

    /**
     * 去除异常值
     */
    private fun removeOutliers(signal: FloatArray): FloatArray {
        if (signal.size < 10) return signal

        val sorted = signal.sorted()
        val q1 = sorted[sorted.size / 4]
        val q3 = sorted[sorted.size * 3 / 4]
        val iqr = q3 - q1

        val lowerBound = q1 - 1.5f * iqr
        val upperBound = q3 + 1.5f * iqr

        return signal.map { value ->
            when {
                value < lowerBound -> lowerBound
                value > upperBound -> upperBound
                else -> value
            }
        }.toFloatArray()
    }

    /**
     * 带通滤波
     */
    private fun applyBandpassFilter(signal: FloatArray, fs: Float): FloatArray {
        val lowcut = 0.6f  // 0.6 Hz (36 BPM)
        val highcut = 4.0f // 4.0 Hz (240 BPM)

        return butterworthBandpass(signal, lowcut, highcut, fs)
    }

    /**
     * 简化的巴特沃斯带通滤波器
     */
    private fun butterworthBandpass(signal: FloatArray, lowcut: Float, highcut: Float, fs: Float): FloatArray {
        val nyquist = fs / 2
        val low = lowcut / nyquist
        val high = highcut / nyquist

        // 简化实现：高通 + 低通
        var filtered = signal.copyOf()

        // 高通滤波（去除低频成分）
        val alphaHigh = 0.95f
        var prevHigh = 0f
        for (i in filtered.indices) {
            val current = filtered[i]
            filtered[i] = alphaHigh * (filtered[i] - prevHigh)
            prevHigh = current
        }

        // 低通滤波（去除高频噪声）
        val alphaLow = 0.1f
        for (i in 1 until filtered.size) {
            filtered[i] = alphaLow * filtered[i] + (1 - alphaLow) * filtered[i - 1]
        }

        return filtered
    }

    /**
     * 信号标准化
     */
    private fun normalizeSignal(signal: FloatArray): FloatArray {
        val mean = signal.average().toFloat()
        val std = kotlin.math.sqrt(
            signal.map { (it - mean) * (it - mean) }.average()
        ).toFloat()

        return if (std > 0) {
            signal.map { (it - mean) / std }.toFloatArray()
        } else {
            signal
        }
    }

    /**
     * 获取模型信息
     */
    fun getModelInfo(): Map<String, Any> {
        val currentSession = session
        return if (currentSession != null && isInitialized) {
            mapOf(
                "initialized" to true,
                "inputShape" to EXPECTED_INPUT_SHAPE.contentToString(),
                "inputNames" to currentSession.inputInfo.keys.toList(),
                "outputNames" to currentSession.outputInfo.keys.toList()
            )
        } else {
            mapOf("initialized" to false)
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        try {
            Log.d(TAG, "开始释放推理引擎资源...")

            session?.close()
            session = null

            // 注意：不要关闭全局的 OrtEnvironment，因为可能被其他地方使用

            isInitialized = false

            Log.d(TAG, "推理引擎资源释放完成")

        } catch (e: Exception) {
            Log.e(TAG, "释放资源失败", e)
        }
    }

    /**
     * 检查是否已初始化
     */
    fun isReady(): Boolean = isInitialized && session != null
}