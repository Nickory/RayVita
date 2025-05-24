package com.codelab.basiclayouts.data.physnet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * rPPG处理器 - 封装帧处理到心率计算的完整流程
 */
class RppgProcessor(private val context: Context) {

    companion object {
        private const val TAG = "RppgProcessor"
        private const val TARGET_FRAME_COUNT = 500
        private const val FRAME_WIDTH = 128
        private const val FRAME_HEIGHT = 128
        private const val FPS = 25f

        // 图像标准化参数 (ImageNet)
        private const val MEAN_R = 0.485f
        private const val MEAN_G = 0.456f
        private const val MEAN_B = 0.406f
        private const val STD_R = 0.229f
        private const val STD_G = 0.224f
        private const val STD_B = 0.225f
    }

    private val inference = RppgInference(context)

    /**
     * 处理视频帧并返回心率和rPPG信号
     */
    suspend fun processFrames(frames: List<Bitmap>): Pair<Float, FloatArray> =
        withContext(Dispatchers.Default) {
            try {
                Log.d(TAG, "开始处理 ${frames.size} 帧")

                // 1. 预处理帧
                val processedFrames = preprocessFrames(frames)

                // 2. 转换为模型输入
                val inputArray = framesToModelInput(processedFrames)

                // 3. 运行推理
                val rppgSignal = inference.runInference(inputArray)

                // 4. 计算心率
                val heartRate = calculateHeartRate(rppgSignal, FPS)

                Log.d(TAG, "处理完成: 心率=${heartRate} BPM")

                Pair(heartRate, rppgSignal)

            } catch (e: Exception) {
                Log.e(TAG, "处理失败", e)
                throw e
            }
        }

    /**
     * 预处理帧序列
     */
    private fun preprocessFrames(frames: List<Bitmap>): List<Bitmap> {
        if (frames.isEmpty()) {
            throw IllegalArgumentException("没有可处理的帧")
        }

        // 确保正好有TARGET_FRAME_COUNT帧
        val processedFrames = when {
            frames.size == TARGET_FRAME_COUNT -> frames
            frames.size > TARGET_FRAME_COUNT -> {
                // 均匀采样
                val step = frames.size.toFloat() / TARGET_FRAME_COUNT
                List(TARGET_FRAME_COUNT) { i ->
                    frames[(i * step).toInt()]
                }
            }
            else -> {
                // 插值扩充
                interpolateFrames(frames, TARGET_FRAME_COUNT)
            }
        }

        return processedFrames
    }

    /**
     * 帧插值
     */
    private fun interpolateFrames(frames: List<Bitmap>, targetCount: Int): List<Bitmap> {
        if (frames.isEmpty()) {
            throw IllegalArgumentException("帧列表为空")
        }

        val interpolated = mutableListOf<Bitmap>()
        val sourceCount = frames.size

        for (i in 0 until targetCount) {
            val sourceIndex = (i.toFloat() / targetCount * sourceCount).toInt()
                .coerceIn(0, sourceCount - 1)
            interpolated.add(frames[sourceIndex])
        }

        return interpolated
    }

    /**
     * 将帧序列转换为模型输入格式
     * 输出格式: [batch, channels, frames, height, width]
     */
    private fun framesToModelInput(frames: List<Bitmap>): FloatArray {
        val batchSize = 1
        val channels = 3
        val frameCount = TARGET_FRAME_COUNT
        val height = FRAME_HEIGHT
        val width = FRAME_WIDTH

        val inputSize = batchSize * channels * frameCount * height * width
        val inputArray = FloatArray(inputSize)

        // 处理每一帧
        frames.forEachIndexed { frameIdx, frame ->
            // 缩放到目标尺寸
            val scaledBitmap = Bitmap.createScaledBitmap(frame, width, height, true)

            // 提取像素并标准化
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = scaledBitmap.getPixel(x, y)

                    // 提取RGB值并标准化
                    val r = (Color.red(pixel) / 255f - MEAN_R) / STD_R
                    val g = (Color.green(pixel) / 255f - MEAN_G) / STD_G
                    val b = (Color.blue(pixel) / 255f - MEAN_B) / STD_B

                    // 计算在输入数组中的位置
                    val pixelIdx = y * width + x
                    val frameOffset = frameIdx * height * width
                    val channelSize = frameCount * height * width

                    // 按照 [batch, channel, frame, height, width] 格式存储
                    inputArray[0 * channelSize + frameOffset + pixelIdx] = r
                    inputArray[1 * channelSize + frameOffset + pixelIdx] = g
                    inputArray[2 * channelSize + frameOffset + pixelIdx] = b
                }
            }

            // 释放缩放后的位图
            if (scaledBitmap != frame) {
                scaledBitmap.recycle()
            }
        }

        return inputArray
    }

    /**
     * 从rPPG信号计算心率
     */
    private fun calculateHeartRate(rppgSignal: FloatArray, fps: Float): Float {
        if (rppgSignal.isEmpty()) return 0f

        // 使用多种方法计算心率并取平均
        val peakBasedHR = calculatePeakBasedHeartRate(rppgSignal, fps)
        val autocorrelationHR = calculateAutocorrelationHeartRate(rppgSignal, fps)

        Log.d(TAG, "峰值法心率: $peakBasedHR, 自相关法心率: $autocorrelationHR")

        // 如果两种方法结果相近，取平均；否则使用峰值法
        return if (kotlin.math.abs(peakBasedHR - autocorrelationHR) < 10) {
            (peakBasedHR + autocorrelationHR) / 2
        } else {
            peakBasedHR
        }
    }

    /**
     * 基于峰值检测的心率计算
     */
    private fun calculatePeakBasedHeartRate(signal: FloatArray, fps: Float): Float {
        // 移动平均平滑
        val smoothedSignal = movingAverage(signal, windowSize = 5)

        // 查找峰值
        val peaks = findPeaks(smoothedSignal)

        if (peaks.size < 2) return 0f

        // 计算峰值间隔
        val intervals = mutableListOf<Float>()
        for (i in 1 until peaks.size) {
            intervals.add((peaks[i] - peaks[i - 1]).toFloat())
        }

        // 去除异常值
        val filteredIntervals = removeOutliers(intervals)

        if (filteredIntervals.isEmpty()) return 0f

        // 计算平均间隔
        val avgInterval = filteredIntervals.average().toFloat()

        // 转换为BPM
        val heartRate = (fps * 60f) / avgInterval

        return heartRate.coerceIn(40f, 200f)
    }

    /**
     * 基于自相关的心率计算
     */
    private fun calculateAutocorrelationHeartRate(signal: FloatArray, fps: Float): Float {
        // 去除直流分量
        val mean = signal.average().toFloat()
        val centered = signal.map { it - mean }.toFloatArray()

        // 计算自相关
        val minLag = (fps * 60 / 200).toInt() // 200 BPM
        val maxLag = (fps * 60 / 40).toInt()  // 40 BPM

        var maxCorr = 0f
        var bestLag = 0

        for (lag in minLag..maxLag) {
            var corr = 0f
            for (i in 0 until centered.size - lag) {
                corr += centered[i] * centered[i + lag]
            }
            if (corr > maxCorr) {
                maxCorr = corr
                bestLag = lag
            }
        }

        if (bestLag == 0) return 0f

        val heartRate = (fps * 60f) / bestLag
        return heartRate.coerceIn(40f, 200f)
    }

    /**
     * 移动平均
     */
    private fun movingAverage(signal: FloatArray, windowSize: Int): FloatArray {
        val smoothed = FloatArray(signal.size)
        val halfWindow = windowSize / 2

        for (i in signal.indices) {
            val start = (i - halfWindow).coerceAtLeast(0)
            val end = (i + halfWindow).coerceAtMost(signal.size - 1)

            var sum = 0f
            for (j in start..end) {
                sum += signal[j]
            }
            smoothed[i] = sum / (end - start + 1)
        }

        return smoothed
    }

    /**
     * 查找峰值
     */
    private fun findPeaks(signal: FloatArray): List<Int> {
        val peaks = mutableListOf<Int>()
        val threshold = signal.average().toFloat()

        // 自适应阈值
        val dynamicThreshold = signal.sortedArray()[signal.size * 3 / 4]

        for (i in 1 until signal.size - 1) {
            if (signal[i] > signal[i - 1] &&
                signal[i] > signal[i + 1] &&
                signal[i] > dynamicThreshold) {

                // 确保峰值间隔合理（避免检测到噪声）
                if (peaks.isEmpty() || i - peaks.last() > 10) {
                    peaks.add(i)
                }
            }
        }

        return peaks
    }

    /**
     * 去除异常值
     */
    private fun removeOutliers(values: List<Float>): List<Float> {
        if (values.size < 3) return values

        val sorted = values.sorted()
        val q1 = sorted[sorted.size / 4]
        val q3 = sorted[sorted.size * 3 / 4]
        val iqr = q3 - q1

        val lowerBound = q1 - 1.5f * iqr
        val upperBound = q3 + 1.5f * iqr

        return values.filter { it in lowerBound..upperBound }
    }

    /**
     * 释放资源
     */
    fun release() {
        try {
            inference.release()
            Log.d(TAG, "处理器资源已释放")
        } catch (e: Exception) {
            Log.e(TAG, "释放资源失败", e)
        }
    }
}