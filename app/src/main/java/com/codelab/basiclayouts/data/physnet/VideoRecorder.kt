package com.codelab.basiclayouts.data.physnet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

/**
 * 升级版视频录制器
 * 支持实时人脸检测、质量控制和性能监控
 */
class VideoRecorder(private val context: Context) {

    companion object {
        private const val TAG = "VideoRecorder"
        private const val TARGET_FRAME_COUNT = 500
        private const val RECORDING_DURATION_MS = 20000L // 20秒
        private const val TARGET_RESOLUTION_WIDTH = 640
        private const val TARGET_RESOLUTION_HEIGHT = 480
        private const val FACE_CROP_SIZE = 128
        private const val MIN_FACE_SIZE = 80
        private const val QUALITY_THRESHOLD = 0.6f
    }

    // 执行器
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val processingScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // 状态管理
    private val isRecording = AtomicBoolean(false)
    private val frameCount = AtomicInteger(0)
    private val qualityFrameCount = AtomicInteger(0)

    // 数据存储
    private val capturedFrames = mutableListOf<Bitmap>()
    private val frameQualityScores = mutableListOf<Float>()

    // 状态流
    private val _recordingState = MutableStateFlow(RecordingState())
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    // 相机组件
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null

    // 回调
    private var onRecordingComplete: ((List<Bitmap>) -> Unit)? = null
    private var onFaceDetected: ((Boolean) -> Unit)? = null

    /**
     * 录制状态数据类
     */
    data class RecordingState(
        val isRecording: Boolean = false,
        val frameCount: Int = 0,
        val qualityFrameCount: Int = 0,
        val progress: Float = 0f,
        val currentQuality: Float = 0f,
        val averageQuality: Float = 0f,
        val faceDetected: Boolean = false,
        val error: String? = null
    )

    /**
     * 绑定相机
     */
    fun bindCamera(
        provider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector
    ) {
        try {
            cameraProvider = provider

            // 配置图像分析
            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(TARGET_RESOLUTION_WIDTH, TARGET_RESOLUTION_HEIGHT))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            // 绑定到生命周期
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                imageAnalysis
            )

            Log.d(TAG, "相机绑定成功")

        } catch (e: Exception) {
            Log.e(TAG, "相机绑定失败", e)
            updateRecordingState { it.copy(error = "相机绑定失败: ${e.message}") }
        }
    }

    /**
     * 开始录制
     */
    fun startRecording(onComplete: (List<Bitmap>) -> Unit) {
        if (!isRecording.compareAndSet(false, true)) {
            Log.w(TAG, "录制已在进行中")
            return
        }

        onRecordingComplete = onComplete

        // 重置状态
        frameCount.set(0)
        qualityFrameCount.set(0)
        capturedFrames.clear()
        frameQualityScores.clear()

        updateRecordingState {
            RecordingState(
                isRecording = true,
                frameCount = 0,
                qualityFrameCount = 0,
                progress = 0f,
                currentQuality = 0f,
                averageQuality = 0f,
                faceDetected = false,
                error = null
            )
        }

        Log.d(TAG, "开始录制，目标帧数: $TARGET_FRAME_COUNT")

        // 启动录制超时
        processingScope.launch {
            delay(RECORDING_DURATION_MS)
            if (isRecording.get()) {
                Log.d(TAG, "录制超时，自动停止")
                stopRecording()
            }
        }
    }

    /**
     * 停止录制
     */
    fun stopRecording() {
        if (!isRecording.compareAndSet(true, false)) {
            return
        }

        Log.d(TAG, "停止录制，共捕获 ${capturedFrames.size} 帧")

        processingScope.launch {
            try {
                // 处理并优化帧序列
                val finalFrames = processCapturedFrames()

                // 更新最终状态
                updateRecordingState { state ->
                    state.copy(
                        isRecording = false,
                        progress = 1f,
                        frameCount = finalFrames.size
                    )
                }

                // 回调结果
                withContext(Dispatchers.Main) {
                    onRecordingComplete?.invoke(finalFrames)
                }

            } catch (e: Exception) {
                Log.e(TAG, "处理录制结果失败", e)
                updateRecordingState { it.copy(error = "处理失败: ${e.message}") }
            }
        }
    }

    /**
     * 设置人脸检测回调
     */
    fun setFaceDetectionCallback(callback: (Boolean) -> Unit) {
        onFaceDetected = callback
    }

    /**
     * 处理相机帧
     */
    private fun processImageProxy(imageProxy: ImageProxy) {
        if (!isRecording.get()) {
            imageProxy.close()
            return
        }

        try {
            val currentFrame = frameCount.get()
            if (currentFrame >= TARGET_FRAME_COUNT) {
                imageProxy.close()
                stopRecording()
                return
            }

            // 转换为Bitmap
            val bitmap = imageProxyToBitmap(imageProxy) ?: run {
                Log.w(TAG, "无法转换图像")
                imageProxy.close()
                return
            }

            // 人脸检测和裁剪
            val faceResult = detectAndCropFace(bitmap)
            val faceBitmap = faceResult.first
            val faceDetected = faceResult.second
            val quality = faceResult.third

            // 更新人脸检测状态
            onFaceDetected?.invoke(faceDetected)

            // 保存高质量帧
            if (quality >= QUALITY_THRESHOLD && faceBitmap != null) {
                synchronized(capturedFrames) {
                    capturedFrames.add(faceBitmap)
                    frameQualityScores.add(quality)

                    val currentCount = frameCount.incrementAndGet()
                    val qualityCount = qualityFrameCount.incrementAndGet()

                    // 更新状态
                    updateRecordingState { state ->
                        state.copy(
                            frameCount = currentCount,
                            qualityFrameCount = qualityCount,
                            progress = currentCount.toFloat() / TARGET_FRAME_COUNT,
                            currentQuality = quality,
                            averageQuality = frameQualityScores.average().toFloat(),
                            faceDetected = faceDetected
                        )
                    }

                    Log.v(TAG, "捕获帧 $currentCount/$TARGET_FRAME_COUNT, 质量: $quality")

                    // 检查是否完成
                    if (currentCount >= TARGET_FRAME_COUNT) {
                        stopRecording()
                    }
                }
            } else {
                // 即使质量不够，也要更新状态
                updateRecordingState { state ->
                    state.copy(
                        currentQuality = quality,
                        faceDetected = faceDetected
                    )
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "处理帧失败", e)
        } finally {
            imageProxy.close()
        }
    }

    /**
     * 转换ImageProxy为Bitmap
     */
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            when (imageProxy.format) {
                ImageFormat.YUV_420_888 -> yuv420ToBitmap(imageProxy)
                else -> {
                    Log.e(TAG, "不支持的图像格式: ${imageProxy.format}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "图像转换失败", e)
            null
        }
    }

    /**
     * YUV420转Bitmap
     */
    private fun yuv420ToBitmap(imageProxy: ImageProxy): Bitmap? {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Y
        yBuffer.get(nv21, 0, ySize)
        // V
        vBuffer.get(nv21, ySize, vSize)
        // U
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            imageProxy.width,
            imageProxy.height,
            null
        )

        val outputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            Rect(0, 0, imageProxy.width, imageProxy.height),
            95, // 高质量
            outputStream
        )

        val jpegBytes = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }

    /**
     * 人脸检测和裁剪
     * 返回: (裁剪后的人脸图像, 是否检测到人脸, 质量分数)
     */
    private fun detectAndCropFace(bitmap: Bitmap): Triple<Bitmap?, Boolean, Float> {
        return try {
            // 简化的人脸检测（基于中心区域）
            // 在实际应用中，应该使用ML Kit或其他人脸检测API
            val faceRect = detectFaceRegion(bitmap)

            if (faceRect != null) {
                val croppedFace = cropFaceRegion(bitmap, faceRect)
                val quality = calculateFrameQuality(croppedFace, bitmap)
                Triple(croppedFace, true, quality)
            } else {
                // 如果没有检测到人脸，使用中心区域
                val centerCrop = cropCenterRegion(bitmap)
                val quality = calculateFrameQuality(centerCrop, bitmap)
                Triple(centerCrop, false, quality * 0.5f) // 降低质量分数
            }

        } catch (e: Exception) {
            Log.e(TAG, "人脸检测失败", e)
            Triple(null, false, 0f)
        }
    }

    /**
     * 检测人脸区域（简化实现）
     */
    private fun detectFaceRegion(bitmap: Bitmap): Rect? {
        // 这是一个简化的实现，实际应用中应该使用ML Kit Face Detection
        val width = bitmap.width
        val height = bitmap.height

        // 假设人脸在图像中心区域
        val faceWidth = min(width, height) / 2
        val faceHeight = faceWidth

        val left = (width - faceWidth) / 2
        val top = (height - faceHeight) / 2

        return Rect(left, top, left + faceWidth, top + faceHeight)
    }

    /**
     * 裁剪人脸区域
     */
    private fun cropFaceRegion(bitmap: Bitmap, faceRect: Rect): Bitmap {
        val croppedBitmap = Bitmap.createBitmap(
            bitmap,
            faceRect.left.coerceAtLeast(0),
            faceRect.top.coerceAtLeast(0),
            faceRect.width().coerceAtMost(bitmap.width - faceRect.left),
            faceRect.height().coerceAtMost(bitmap.height - faceRect.top)
        )

        // 缩放到目标尺寸
        return Bitmap.createScaledBitmap(croppedBitmap, FACE_CROP_SIZE, FACE_CROP_SIZE, true)
    }

    /**
     * 裁剪中心区域
     */
    private fun cropCenterRegion(bitmap: Bitmap): Bitmap {
        val size = min(bitmap.width, bitmap.height)
        val left = (bitmap.width - size) / 2
        val top = (bitmap.height - size) / 2

        val croppedBitmap = Bitmap.createBitmap(bitmap, left, top, size, size)
        return Bitmap.createScaledBitmap(croppedBitmap, FACE_CROP_SIZE, FACE_CROP_SIZE, true)
    }

    /**
     * 计算帧质量分数
     */
    private fun calculateFrameQuality(faceBitmap: Bitmap, originalBitmap: Bitmap): Float {
        try {
            // 1. 亮度检查
            val brightness = calculateBrightness(faceBitmap)
            val brightnessScore = when {
                brightness < 50 -> 0.3f // 太暗
                brightness > 200 -> 0.5f // 太亮
                else -> 1.0f // 合适
            }

            // 2. 对比度检查
            val contrast = calculateContrast(faceBitmap)
            val contrastScore = (contrast / 50f).coerceIn(0f, 1f)

            // 3. 清晰度检查（简化的拉普拉斯算子）
            val sharpness = calculateSharpness(faceBitmap)
            val sharpnessScore = (sharpness / 100f).coerceIn(0f, 1f)

            // 4. 综合评分
            val totalScore = (brightnessScore * 0.3f + contrastScore * 0.3f + sharpnessScore * 0.4f)

            Log.v(TAG, "质量评分: 亮度=$brightnessScore, 对比度=$contrastScore, 清晰度=$sharpnessScore, 总分=$totalScore")

            return totalScore.coerceIn(0f, 1f)

        } catch (e: Exception) {
            Log.e(TAG, "质量计算失败", e)
            return 0.5f
        }
    }

    /**
     * 计算平均亮度
     */
    private fun calculateBrightness(bitmap: Bitmap): Float {
        var totalBrightness = 0f
        var pixelCount = 0

        for (x in 0 until bitmap.width step 4) {
            for (y in 0 until bitmap.height step 4) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                totalBrightness += (r + g + b) / 3f
                pixelCount++
            }
        }

        return if (pixelCount > 0) totalBrightness / pixelCount else 0f
    }

    /**
     * 计算对比度
     */
    private fun calculateContrast(bitmap: Bitmap): Float {
        val pixels = mutableListOf<Float>()

        for (x in 0 until bitmap.width step 4) {
            for (y in 0 until bitmap.height step 4) {
                val pixel = bitmap.getPixel(x, y)
                val gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3f
                pixels.add(gray)
            }
        }

        if (pixels.size < 2) return 0f

        val mean = pixels.average().toFloat()
        val variance = pixels.map { (it - mean) * (it - mean) }.average().toFloat()

        return kotlin.math.sqrt(variance)
    }

    /**
     * 计算清晰度（拉普拉斯算子）
     */
    private fun calculateSharpness(bitmap: Bitmap): Float {
        val grayBitmap = toGrayscale(bitmap)
        var sharpness = 0f

        for (x in 1 until grayBitmap.width - 1) {
            for (y in 1 until grayBitmap.height - 1) {
                val center = Color.red(grayBitmap.getPixel(x, y))
                val top = Color.red(grayBitmap.getPixel(x, y - 1))
                val bottom = Color.red(grayBitmap.getPixel(x, y + 1))
                val left = Color.red(grayBitmap.getPixel(x - 1, y))
                val right = Color.red(grayBitmap.getPixel(x + 1, y))

                val laplacian = kotlin.math.abs(4 * center - top - bottom - left - right)
                sharpness += laplacian
            }
        }

        return sharpness / ((grayBitmap.width - 2) * (grayBitmap.height - 2))
    }

    /**
     * 转换为灰度图
     */
    private fun toGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val gray = (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114).toInt()
                grayBitmap.setPixel(x, y, Color.rgb(gray, gray, gray))
            }
        }

        return grayBitmap
    }

    /**
     * 处理捕获的帧序列
     */
    private fun processCapturedFrames(): List<Bitmap> {
        synchronized(capturedFrames) {
            Log.d(TAG, "处理 ${capturedFrames.size} 帧，质量分数: ${frameQualityScores.average()}")

            // 如果帧数不足，进行插值
            return when {
                capturedFrames.size >= TARGET_FRAME_COUNT -> {
                    // 选择质量最好的帧
                    selectBestFrames(capturedFrames, frameQualityScores, TARGET_FRAME_COUNT)
                }
                capturedFrames.size > TARGET_FRAME_COUNT / 2 -> {
                    // 帧数足够，进行插值
                    interpolateFrames(capturedFrames, TARGET_FRAME_COUNT)
                }
                else -> {
                    // 帧数太少，重复最后几帧
                    expandFrameSequence(capturedFrames, TARGET_FRAME_COUNT)
                }
            }
        }
    }

    /**
     * 选择质量最好的帧
     */
    private fun selectBestFrames(frames: List<Bitmap>, qualities: List<Float>, targetCount: Int): List<Bitmap> {
        // 确保 frames 和 qualities 长度匹配
        require(frames.size == qualities.size) { "Frames and qualities must have the same size" }

        // 创建带索引的帧和质量对
        val indexedFrames = frames.zip(qualities).withIndex()
        // 按质量降序排序
        val sortedByQuality = indexedFrames.sortedByDescending { it.value.second }
        // 取目标数量的帧，按原始索引升序排序
        val selectedFrames = sortedByQuality.take(targetCount).sortedBy { it.index }
        // 提取原始 Bitmap
        return selectedFrames.map { it.value.first }
    }

    /**
     * 帧插值
     */
    private fun interpolateFrames(frames: List<Bitmap>, targetCount: Int): List<Bitmap> {
        if (frames.isEmpty()) return emptyList()

        val interpolated = mutableListOf<Bitmap>()
        val step = frames.size.toFloat() / targetCount

        for (i in 0 until targetCount) {
            val index = (i * step).toInt().coerceIn(0, frames.size - 1)
            interpolated.add(frames[index])
        }

        return interpolated
    }

    /**
     * 扩展帧序列
     */
    private fun expandFrameSequence(frames: List<Bitmap>, targetCount: Int): List<Bitmap> {
        if (frames.isEmpty()) return emptyList()

        val expanded = mutableListOf<Bitmap>()
        expanded.addAll(frames)

        // 重复最后几帧来填充
        val lastFrames = frames.takeLast(kotlin.math.min(frames.size, 10))
        while (expanded.size < targetCount) {
            lastFrames.forEach { frame ->
                if (expanded.size < targetCount) {
                    expanded.add(frame)
                }
            }
        }

        return expanded.take(targetCount)
    }

    /**
     * 更新录制状态
     */
    private fun updateRecordingState(update: (RecordingState) -> RecordingState) {
        _recordingState.value = update(_recordingState.value)
    }

    /**
     * 获取录制统计信息
     */
    fun getRecordingStats(): Map<String, Any> {
        val state = _recordingState.value
        return mapOf(
            "totalFrames" to state.frameCount,
            "qualityFrames" to state.qualityFrameCount,
            "averageQuality" to state.averageQuality,
            "progress" to state.progress,
            "isRecording" to state.isRecording
        )
    }

    /**
     * 释放资源
     */
    fun release() {
        try {
            Log.d(TAG, "开始释放VideoRecorder资源...")

            // 停止录制
            if (isRecording.get()) {
                stopRecording()
            }

            // 释放相机资源
            imageAnalysis = null
            camera = null
            cameraProvider = null

            // 释放帧数据
            synchronized(capturedFrames) {
                capturedFrames.forEach { bitmap ->
                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                }
                capturedFrames.clear()
                frameQualityScores.clear()
            }

            // 关闭协程
            processingScope.cancel()

            // 关闭执行器
            cameraExecutor.shutdown()

            Log.d(TAG, "VideoRecorder资源释放完成")

        } catch (e: Exception) {
            Log.e(TAG, "释放资源失败", e)
        }
    }
}