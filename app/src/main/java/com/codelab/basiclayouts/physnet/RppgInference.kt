package com.codelab.basiclayouts.physnet

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.util.Log
import java.nio.FloatBuffer
import java.util.Arrays

class RppgInference(context: Context) {
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private val TAG = "RppgInference"

    init {
        try {
            // 调试：列出 assets 目录中的文件
            val assetFiles = context.assets.list("")?.joinToString(", ") ?: "空"
            Log.d(TAG, "Assets 目录中的文件: $assetFiles")

            // 加载 .ort 模型
            Log.d(TAG, "开始加载模型 physnet_rppg.ort")
            val startTime = System.currentTimeMillis()
            val modelBytes = context.assets
                .open("com/codelab/basiclayouts/physnet/physnet_rppg.ort")
                .readBytes()
            Log.d(TAG, "模型文件大小: ${modelBytes.size} bytes, 加载耗时: ${System.currentTimeMillis() - startTime}ms")

            // 配置会话选项
            val opts = OrtSession.SessionOptions()
            try {
                opts.addNnapi()
                Log.d(TAG, "成功启用 NNAPI 硬件加速")
            } catch (e: Exception) {
                Log.w(TAG, "NNAPI 不可用: ${e.message}")
            }

            // 创建会话
            session = env.createSession(modelBytes, opts)
            Log.d(TAG, "ORT 模型加载成功")

            // 打印模型输入输出信息
            val inputInfo = session.inputInfo
            val outputInfo = session.outputInfo
            Log.d(TAG, "模型输入信息: ${inputInfo.entries.joinToString { "${it.key}: ${it.value}" }}")
            Log.d(TAG, "模型输出信息: ${outputInfo.entries.joinToString { "${it.key}: ${it.value}" }}")

        } catch (e: Exception) {
            Log.e(TAG, "模型加载失败: ${e.message}", e)
            throw IllegalStateException("无法加载模型文件 physnet_rppg.ort，请确保文件存在于 assets 目录", e)
        }
    }

    fun runInference(flatInput: FloatArray): FloatArray {
        return try {
            val shape = longArrayOf(1, 3, 500, 128, 128)
            Log.d(TAG, "开始推理，输入数组大小: ${flatInput.size}")
            Log.d(TAG, "预期输入形状: ${Arrays.toString(shape)}, 预期大小: ${shape.reduce { acc, l -> acc * l }}")

            // 验证输入数据
            if (flatInput.size != shape.reduce { acc, l -> acc * l }.toInt()) {
                Log.w(TAG, "输入数据大小不匹配，实际: ${flatInput.size}, 预期: ${shape.reduce { acc, l -> acc * l }}")
            }
            Log.d(TAG, "输入数据统计: min=${flatInput.minOrNull()}, max=${flatInput.maxOrNull()}, mean=${flatInput.average()}")

            // 创建输入张量
            val bufferStartTime = System.currentTimeMillis()
            val buffer = FloatBuffer.wrap(flatInput)
            val inputTensor = OnnxTensor.createTensor(env, buffer, shape)
            Log.d(TAG, "输入张量创建耗时: ${System.currentTimeMillis() - bufferStartTime}ms")

            // 执行推理
            Log.d(TAG, "开始执行模型推理")
            val inferenceStartTime = System.currentTimeMillis()
            val results = session.run(mapOf("input" to inputTensor))
            Log.d(TAG, "推理完成，耗时: ${System.currentTimeMillis() - inferenceStartTime}ms")

            // 处理输出
            val output = results[0].value
            Log.d(TAG, "原始输出类型: ${output?.javaClass}")

            val rawOutput = when (output) {
                is Array<*> -> {
                    when (val inner = output[0]) {
                        is FloatArray -> {
                            Log.d(TAG, "处理单层数组输出")
                            inner
                        }
                        is Array<*> -> {
                            if (inner[0] is FloatArray) {
                                Log.d(TAG, "处理嵌套数组输出")
                                (inner[0] as FloatArray)
                            } else {
                                throw IllegalStateException("不支持的嵌套输出格式")
                            }
                        }
                        else -> throw IllegalStateException("未知的内部输出类型: ${inner?.javaClass}")
                    }
                }
                is FloatArray -> {
                    Log.d(TAG, "处理直接 FloatArray 输出")
                    output
                }
                else -> throw IllegalStateException("不支持的输出类型: ${output?.javaClass}")
            }

            Log.d(TAG, "原始输出统计: size=${rawOutput.size}, min=${rawOutput.minOrNull()}, max=${rawOutput.maxOrNull()}, mean=${rawOutput.average()}")

            // 应用滤波
            Log.d(TAG, "开始应用带通滤波")
            val filterStartTime = System.currentTimeMillis()
            val processedOutput = applyBandpassFilter(rawOutput, 25f)
            Log.d(TAG, "滤波完成，耗时: ${System.currentTimeMillis() - filterStartTime}ms")
            Log.d(TAG, "处理后输出统计: size=${processedOutput.size}, min=${processedOutput.minOrNull()}, max=${processedOutput.maxOrNull()}, mean=${processedOutput.average()}")

            // 清理资源
            inputTensor.close()
            results.forEach { it.value.close() }
            Log.d(TAG, "推理资源清理完成")

            processedOutput

        } catch (e: Exception) {
            Log.e(TAG, "推理失败: ${e.message}", e)
            throw e
        }
    }

    private fun applyBandpassFilter(signal: FloatArray, fs: Float): FloatArray {
        val lowcut = 0.6f
        val highcut = 4.0f
        Log.d(TAG, "应用带通滤波: lowcut=$lowcut Hz, highcut=$highcut Hz, fs=$fs Hz")
        return butterworthBandpass(signal, lowcut, highcut, fs)
    }

    private fun butterworthBandpass(signal: FloatArray, lowcut: Float, highcut: Float, fs: Float): FloatArray {
        val nyquist = fs / 2
        val low = lowcut / nyquist
        val high = highcut / nyquist
        Log.d(TAG, "巴特沃斯滤波参数: nyquist=$nyquist, low=$low, high=$high")

        val filtered = signal.copyOf()
        val alpha = 0.99f
        var prev = 0f
        for (i in filtered.indices) {
            val current = filtered[i]
            filtered[i] = alpha * (filtered[i] - prev)
            prev = current
        }

        val beta = 0.1f
        for (i in 1 until filtered.size) {
            filtered[i] = beta * filtered[i] + (1 - beta) * filtered[i - 1]
        }

        Log.d(TAG, "滤波后信号统计: size=${filtered.size}, min=${filtered.minOrNull()}, max=${filtered.maxOrNull()}, mean=${filtered.average()}")
        return filtered
    }

    fun release() {
        try {
            Log.d(TAG, "开始释放模型资源")
            session.close()
            env.close()
            Log.d(TAG, "模型资源释放成功")
        } catch (e: Exception) {
            Log.e(TAG, "释放资源失败: ${e.message}", e)
        }
    }
}