//package com.codelab.basiclayouts.data.physnet
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.util.Log
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
///**
// * 增强的rPPG处理器 - 支持HRV和SpO2计算
// * 在原有功能基础上添加RGB通道数据提取
// */
//class EnhancedRppgProcessor(private val context: Context) {
//
//    companion object {
//        private const val TAG = "EnhancedRppgProcessor"
//        private const val TARGET_FRAME_COUNT = 500
//        private const val FRAME_WIDTH = 128
//        private const val FRAME_HEIGHT = 128
//        private const val FPS = 25f
//
//        // 图像标准化参数 (ImageNet)
//        private const val MEAN_R = 0.485f
//        private const val MEAN_G = 0.456f
//        private const val MEAN_B = 0.406f
//        private const val STD_R = 0.229f
//        private const val STD_G = 0.224f
//        private const val STD_B = 0.225f
//    }
//
//    private val inference = RppgInference(context)
//
//    /**
//     * RGB通道数据
//     */
//    data class RgbChannelData(
//        val redChannel: FloatArray,
//        val greenChannel: FloatArray,
//        val blueChannel: FloatArray,
//        val timestamps: LongArray
//    ) {
//        override fun equals(other: Any?): Boolean {
//            if (this === other) return true
//            if (javaClass != other?.javaClass) return false
//
//            other as RgbChannelData
//
//            if (!redChannel.contentEquals(other.redChannel)) return false
//            if (!greenChannel.contentEquals(other.greenChannel)) return false
//            if (!blueChannel.contentEquals(other.blueChannel)) return false
//            if (!timestamps.contentEquals(other.timestamps)) return false
//
//            return true
//        }
//
//        override fun hashCode(): Int {
//            var result = redChannel.contentHashCode()
//            result = 31 * result + greenChannel.contentHashCode()
//            result = 31 * result + blueChannel.contentHashCode()
//            result = 31 * result + timestamps.contentHashCode()
//            return result
//        }
//    }
//
//    /**
//     * 处理结果
//     */
//    data class ProcessingResult(
//        val heartRate: Float,
//        val rppgSignal: FloatArray,
//        val rgbData: RgbChannelData
//    )
//
//    /**
//     * 处理视频帧并返回心率、rPPG信号和RGB数据
//     * 保持与原版本的兼容性
//     */
//    suspend fun processFrames(frames: List<Bitmap>): Pair<Float, FloatArray> =
//        withContext(Dispatchers.Default) {
//            val result = processFramesWithRGB(frames)
//            Pair(result.heartRate, result.rppgSignal)
//        }
//
//    /**
//     * 处理视频帧并返回完整的结果（包含RGB数据）
//     */
//    suspend fun processFramesWithRGB(frames: List<Bitmap>): ProcessingResult =
//        withContext(Dispatchers.Default) {
//            try {
//                Log.d(TAG, "开始处理 ${frames.size} 帧（包含RGB提取）")
//
//                // 1. 预处理帧
//                val processedFrames = preprocessFrames(frames)
//
//                // 2. 同时提取模型输入和RGB通道数据
//                val (inputArray, rgbData) = framesToModelInputWithRGB(processedFrames)
//
//                // 3. 运行推理
//                val rppgSignal = inference.runInference(inputArray)
//
//                // 4. 计算心率
//                val heartRate = calculateHeartRate(rppgSignal, FPS)
//
//                Log.d(TAG, "处理完成: 心率=${heartRate} BPM, RGB通道长度=${rgbData.redChannel.size}")
//
//                ProcessingResult(heartRate, rppgSignal, rgbData)
//
//            } catch (e: Exception) {
//                Log.e(TAG, "增强处理失败", e)
//                throw e
//            }
//        }
//
//    /**
//     * 预处理帧序列 - 与原版本保持一致
//     */
//    private fun preprocessFrames(frames: List<Bitmap>): List<Bitmap>