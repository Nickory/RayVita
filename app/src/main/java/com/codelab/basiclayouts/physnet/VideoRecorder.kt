package com.codelab.basiclayouts.physnet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
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
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

class VideoRecorder(private val context: Context) {
    private val executor = Executors.newSingleThreadExecutor()
    private val frames = mutableListOf<Bitmap>()
    private val isRecording = AtomicBoolean(false)
    private var imageAnalysis: ImageAnalysis? = null
    private var onRecordingComplete: ((List<Bitmap>) -> Unit)? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun bindCamera(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector
    ) {
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(android.util.Size(640, 480))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(executor) { imageProxy ->
                    processFrame(imageProxy)
                }
            }

        try {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                imageAnalysis
            )
        } catch (exc: Exception) {
            Log.e("VideoRecorder", "相机绑定失败", exc)
        }
    }

    fun startRecording(onComplete: (List<Bitmap>) -> Unit) {
        if (isRecording.get()) return

        onRecordingComplete = onComplete
        frames.clear()
        isRecording.set(true)

        Log.d("VideoRecorder", "开始录制，目标500帧")

        coroutineScope.launch {
            delay(20000) // 20秒
            stopRecording()
        }
    }

    private fun processFrame(imageProxy: ImageProxy) {
        if (!isRecording.get()) {
            imageProxy.close()
            return
        }

        try {
            val bitmap = imageProxyToBitmap(imageProxy)
            if (bitmap != null) {
                val faceBitmap = detectAndCropFace(bitmap) ?: bitmap // 若人脸检测失败，使用完整帧
                synchronized(frames) {
                    frames.add(faceBitmap)
                    Log.d("VideoRecorder", "添加第 ${frames.size} 帧")
                    if (frames.size >= 500) {
                        stopRecording()
                    }
                }
            } else {
                Log.w("VideoRecorder", "无法将图像转换为 Bitmap")
            }
        } catch (e: Exception) {
            Log.e("VideoRecorder", "处理帧失败", e)
        } finally {
            imageProxy.close()
        }
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        if (imageProxy.format != ImageFormat.YUV_420_888) {
            Log.e("VideoRecorder", "不支持的图像格式: ${imageProxy.format}")
            return null
        }

        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val jpegBytes = out.toByteArray()

        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }

    private fun detectAndCropFace(bitmap: Bitmap): Bitmap? {
        val centerX = bitmap.width / 2
        val centerY = bitmap.height / 2
        val faceSize = min(bitmap.width, bitmap.height) / 2

        val left = (centerX - faceSize / 2).coerceAtLeast(0)
        val top = (centerY - faceSize / 2).coerceAtLeast(0)
        val right = (centerX + faceSize / 2).coerceAtMost(bitmap.width)
        val bottom = (centerY + faceSize / 2).coerceAtMost(bitmap.height)

        return try {
            val croppedBitmap = Bitmap.createBitmap(
                bitmap,
                left,
                top,
                right - left,
                bottom - top
            )
            Bitmap.createScaledBitmap(croppedBitmap, 128, 128, true)
        } catch (e: Exception) {
            Log.e("VideoRecorder", "裁剪人脸失败", e)
            null
        }
    }

    private fun stopRecording() {
        if (!isRecording.compareAndSet(true, false)) return

        Log.d("VideoRecorder", "停止录制，共获得${frames.size}帧")

        val recordedFrames = synchronized(frames) {
            val finalFrames = mutableListOf<Bitmap>()
            finalFrames.addAll(frames)

            while (finalFrames.size < 500 && finalFrames.isNotEmpty()) {
                val lastFrame = finalFrames.last()
                lastFrame.config?.let { lastFrame.copy(it, false) }?.let { finalFrames.add(it) }
            }

            finalFrames.take(500)
        }

        onRecordingComplete?.invoke(recordedFrames)
    }

    fun release() {
        isRecording.set(false)
        executor.shutdown()
        coroutineScope.cancel()

        frames.forEach { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        frames.clear()
    }
}