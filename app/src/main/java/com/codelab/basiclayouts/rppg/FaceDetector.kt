package com.codelab.basiclayouts.rppg

import android.content.Context
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await  // 需要导入
import kotlinx.coroutines.withContext

class FaceDetector(private val context: Context) {
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )

    //加油

    @androidx.camera.core.ExperimentalGetImage
    suspend fun detectFaces(image: ImageProxy): List<Rect> = withContext(Dispatchers.IO) {
        val mediaImage = image.image ?: return@withContext emptyList() // 避免空指针异常
        val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
        val results: List<Face> = faceDetector.process(inputImage).await() // 确保 await() 可用
        results.map { it.boundingBox }
    }
}
