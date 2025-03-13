package com.codelab.basiclayouts.rppg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SpatioTemporalFeatureExtractor(
    modelPath: String,
    private val context: Context // 添加Context参数
) {
    private val interpreter = Interpreter(loadModelFile(modelPath))

    fun extractFeatures(frames: List<Bitmap>): FloatArray {
        // 输入缓冲区配置（形状：[1, 30, 128, 128, 3]）
        val inputBuffer = ByteBuffer.allocateDirect(1 * 30 * 128 * 128 * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())

        frames.forEachIndexed { index, bitmap ->
            // 图像预处理
            val resized = Bitmap.createScaledBitmap(bitmap, 128, 128, true)
            val pixels = IntArray(128 * 128)
            resized.getPixels(pixels, 0, 128, 0, 0, 128, 128)

            pixels.forEachIndexed { index, pixel ->
                val r = Color.red(pixel).toFloat() / 255f
                val g = Color.green(pixel).toFloat() / 255f
                val b = Color.blue(pixel).toFloat() / 255f

                // 将RGB值按顺序写入缓冲区
                inputBuffer.putFloat(r)
                inputBuffer.putFloat(g)
                inputBuffer.putFloat(b)
            }
        }

        // 输出缓冲区配置（形状：[30, 128, 128]）
        val outputBuffer = Array(30) { FloatArray(128 * 128) }
        interpreter.run(inputBuffer, outputBuffer)

        // 手动展平输出缓冲区为1D数组
        val flattened = FloatArray(30 * 128 * 128)
        var offset = 0
        outputBuffer.forEach { floatArray ->
            System.arraycopy(floatArray, 0, flattened, offset, floatArray.size)
            offset += floatArray.size
        }

        // 展平输出缓冲区为1D数组
//        outputBuffer.flatten()
        return flattened
    }

    private fun loadModelFile(assetName: String): ByteBuffer {
        val fileDescriptor = context.assets.openFd(assetName) // 使用注入的context
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val bytes = inputStream.readBytes()
        return ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder())
    }
}