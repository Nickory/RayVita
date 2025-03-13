package com.codelab.basiclayouts.rppg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect

import android.graphics.YuvImage
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap


class RppgProcessor(
    private val faceDetector: FaceDetector,
    private val context: Context ,// 添加 Context 参数
    private val viewModel: HeartRateViewModel // 通过构造函数注入 ViewModel
) {
    private val featureExtractor = SpatioTemporalFeatureExtractor("model.tflite", context)
    private val contrastiveLoss = ContrastiveLoss()
    private val heartRateCalc = HeartRateCalculator()

    private val signalBuffer = mutableListOf<FloatArray>()
    private val regionBuffers = ConcurrentHashMap<Int, MutableList<Float>>()

    @OptIn(ExperimentalGetImage::class)
    suspend fun processFrame(image: ImageProxy) {
        val startTime = System.currentTimeMillis()

        // 1. 人脸检测
        val faces = faceDetector.detectFaces(image)
        if (faces.isEmpty()) return

        // 2. 区域划分
        val regions = faces.mapIndexed { index, rect ->
            Rect(
                (rect.left + rect.width() / 4).toInt(),
                (rect.top + rect.height() / 4).toInt(),
                (rect.right - rect.width() / 4).toInt(),
                (rect.bottom - rect.height() / 4).toInt()
            )
        }

        // 3. 特征提取
        val frames = regions.map { region ->
            cropRegion(image, region)
        }

        val features = featureExtractor.extractFeatures(frames)
        signalBuffer.add(features)

        // 4. 对比学习
        if (signalBuffer.size >= 2) {
            val anchor = signalBuffer[signalBuffer.size - 2]
            val positive = signalBuffer.last()
            val negatives = signalBuffer.shuffled().take(5).dropLast(1)

            val loss = contrastiveLoss.compute(anchor, positive, negatives)
            Log.d("ContrastiveLoss", "Current Loss: $loss")
        }

        // 5. 心率计算
        if (features.isNotEmpty()) {
            heartRateCalc.update(features)
            val bpm = heartRateCalc.calculateBpm()

            // 更新UI
            withContext(Dispatchers.Main) {
//                viewModel.bpm.value = bpm
            }
        }

        val endTime = System.currentTimeMillis()
        Log.d("ProcessingTime", "Frame processed in ${endTime - startTime}ms")
    }

    private fun cropRegion(image: ImageProxy, region: Rect): Bitmap {
        // 1. 确保区域不越界
        val safeRegion = Rect(
            region.left.coerceAtLeast(0),
            region.top.coerceAtLeast(0),
            region.right.coerceAtMost(image.width),
            region.bottom.coerceAtMost(image.height)
        )

        // 2. 正确转换 YUV 数据为 Bitmap
        val yuvBuffer = image.planes[0].buffer
        val bitmap =
            Bitmap.createBitmap(safeRegion.width(), safeRegion.height(), Bitmap.Config.ARGB_8888)

        // 3. 使用 YuvImage 转换（需要 Android API 支持）
        val yuvImage = YuvImage(
            yuvBuffer.array(),
            ImageFormat.NV21,
            image.width,
            image.height,
            null
        )

        val outputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(region, 80, outputStream)
        val jpegData = outputStream.toByteArray()

        // 4. 安全解码 Bitmap
        return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size) ?: run {
            Log.e("CropRegion", "解码失败，数据大小: ${jpegData.size}")
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // 返回占位图避免崩溃
        }
        }
}