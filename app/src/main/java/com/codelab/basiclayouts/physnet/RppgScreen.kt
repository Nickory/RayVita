package com.codelab.basiclayouts.physnet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RppgScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isRecording by remember { mutableStateOf(false) }
    var recordingProgress by remember { mutableStateOf(0f) }
    var heartRate by remember { mutableStateOf(0f) }
    var rppgData by remember { mutableStateOf<FloatArray?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val videoRecorder = remember { VideoRecorder(context) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题
        Text(
            text = "rPPG Heart Rate Monitor",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 相机预览
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = androidx.compose.ui.graphics.Color.DarkGray
            )
        ) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        setupCamera(this, lifecycleOwner, videoRecorder)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 录制进度
        if (isRecording) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    progress = recordingProgress,
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${recordingTime}s / 20s",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 录制按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    if (!isRecording && !isProcessing) {
                        errorMessage = null
                        startRecording(
                            videoRecorder = videoRecorder,
                            onProgressUpdate = { progress, time ->
                                recordingProgress = progress
                                recordingTime = time
                            },
                            onRecordingComplete = { frames ->
                                isRecording = false
                                recordingProgress = 0f
                                recordingTime = 0
                                isProcessing = true

                                coroutineScope.launch {
                                    try {
                                        val result = processRppgInference(context, frames)
                                        heartRate = result.first
                                        rppgData = result.second
                                        Log.d(
                                            "RppgScreen",
                                            "波形数据统计: size=${result.second.size}, " +
                                                    "min=${result.second.minOrNull()}, " +
                                                    "max=${result.second.maxOrNull()}, " +
                                                    "mean=${result.second.average()}"
                                        )
                                    } catch (e: Exception) {
                                        Log.e("RppgScreen", "推理失败", e)
                                        errorMessage = "录制失败：${e.message}"
                                    } finally {
                                        isProcessing = false
                                    }
                                }
                            },
                            coroutineScope = coroutineScope
                        )
                        isRecording = true
                    }
                },
                enabled = !isRecording && !isProcessing,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording)
                        androidx.compose.ui.graphics.Color.Red
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isRecording) "录制中"
                    else if (isProcessing) "处理中"
                    else "开始",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 错误提示
        errorMessage?.let {
            Text(
                text = it,
                color = androidx.compose.ui.graphics.Color.Red,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 心率显示
        if (heartRate > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "心率检测结果",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${heartRate.toInt()} BPM",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 波形显示
        rppgData?.let { data ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "rPPG 波形",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(androidx.compose.ui.graphics.Color.Black)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        ) {
                            Log.d("WaveformDebug", "Canvas 尺寸: width=${size.width}, height=${size.height}")
                            // Inline waveform drawing to avoid composable error
                            if (data.isEmpty()) {
                                Log.w("WaveformDebug", "波形数据为空")
                                return@Canvas
                            }

                            if (size.height <= 0f) {
                                Log.e("WaveformDebug", "Canvas 高度为 0，无法绘制波形")
                                return@Canvas
                            }

                            // 绘制网格背景
                            val gridColor = androidx.compose.ui.graphics.Color(0xFF2A2A2A)
                            val gridStrokeWidth = with(LocalDensity) { 1.dp.toPx() }
                            val gridSpacingX = size.width / 10 // 10 条垂直线
                            val gridSpacingY = size.height / 5  // 5 条水平线

                            // 垂直网格线
                            for (i in 0..10) {
                                val x = i * gridSpacingX
                                drawLine(
                                    color = gridColor,
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = gridStrokeWidth
                                )
                            }

                            // 水平网格线
                            for (i in 0..5) {
                                val y = i * gridSpacingY
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = gridStrokeWidth
                                )
                            }

                            // 绘制基线（零线）
                            val minValue = data.minOrNull() ?: 0f
                            val maxValue = data.maxOrNull() ?: 1f
                            val range = maxValue - minValue
                            if (range == 0f) {
                                Log.w("WaveformDebug", "波形数据范围为 0，无法绘制")
                                return@Canvas
                            }
                            val zeroNormalized = (-minValue) / range
                            val zeroY = size.height * (0.85f - zeroNormalized * 0.7f)
                            drawLine(
                                color = androidx.compose.ui.graphics.Color(0xFF555555),
                                start = Offset(0f, zeroY),
                                end = Offset(size.width, zeroY),
                                strokeWidth = with(LocalDensity) { 1.dp.toPx() }
                            )

                            // 绘制波形
                            val path = Path()
                            val stepX = if (data.size > 1) size.width / (data.size - 1) else size.width

                            data.forEachIndexed { index, value ->
                                val normalized = (value - minValue) / range // 归一化到 [0, 1]
                                // 映射到 [0.15h, 0.85h]，留出边距
                                val y = size.height * (0.85f - normalized * 0.7f)
                                val x = index * stepX

                                Log.d("WaveformDebug", "第 $index 项 value=$value, normalized=$normalized, y=$y, x=$x")

                                if (index == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                            }

                            // 渐变颜色
                            val gradientBrush = Brush.linearGradient(
                                colors = listOf(
                                    androidx.compose.ui.graphics.Color.Green,
                                    androidx.compose.ui.graphics.Color.Cyan
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height)
                            )

                            drawPath(
                                path = path,
                                brush = gradientBrush,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun setupCamera(
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    videoRecorder: VideoRecorder
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview
            )
            videoRecorder.bindCamera(cameraProvider, lifecycleOwner, cameraSelector)
        } catch (exc: Exception) {
            Log.e("RppgScreen", "摄像头绑定失败", exc)
        }
    }, ContextCompat.getMainExecutor(previewView.context))
}

private fun startRecording(
    videoRecorder: VideoRecorder,
    onProgressUpdate: (Float, Int) -> Unit,
    onRecordingComplete: (List<Bitmap>) -> Unit,
    coroutineScope: CoroutineScope
) {
    videoRecorder.startRecording { frames ->
        onRecordingComplete(frames)
    }

    coroutineScope.launch {
        repeat(20) { second ->
            delay(1000)
            val progress = (second + 1) / 20f
            onProgressUpdate(progress, second + 1)
        }
    }
}

private suspend fun processRppgInference(
    context: Context,
    frames: List<Bitmap>
): Pair<Float, FloatArray> = withContext(Dispatchers.Default) {
    if (frames.isEmpty()) {
        throw IllegalArgumentException("没有捕获到任何帧")
    }

    val processedFrames = if (frames.size != 500) {
        interpolateFrames(frames, 500)
    } else {
        frames
    }

    val inputArray = framesToModelInput(processedFrames)
    val inference = RppgInference(context)
    val rppgSignal = inference.runInference(inputArray)
    val heartRate = calculateHeartRate(rppgSignal, 25f)

    Pair(heartRate, rppgSignal)
}

private fun interpolateFrames(frames: List<Bitmap>, targetCount: Int): List<Bitmap> {
    if (frames.isEmpty()) {
        throw IllegalArgumentException("帧列表为空，无法插值")
    }
    if (frames.size >= targetCount) {
        return frames.take(targetCount)
    }

    val interpolated = mutableListOf<Bitmap>()
    val ratio = (frames.size - 1).toFloat() / (targetCount - 1)

    for (i in 0 until targetCount) {
        val index = (i * ratio).toInt()
        interpolated.add(frames[index.coerceAtMost(frames.size - 1)])
    }

    return interpolated
}

private fun framesToModelInput(frames: List<Bitmap>): FloatArray {
    val batchSize = 1
    val channels = 3
    val frameCount = 500
    val height = 128
    val width = 128

    val inputSize = batchSize * channels * frameCount * height * width
    val inputArray = FloatArray(inputSize)

    for (frameIdx in 0 until frameCount) {
        val frame = if (frameIdx < frames.size) frames[frameIdx] else frames.last()
        val bitmap = Bitmap.createScaledBitmap(frame, width, height, true)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val r = (Color.red(pixel) / 255f - 0.485f) / 0.229f
                val g = (Color.green(pixel) / 255f - 0.456f) / 0.224f
                val b = (Color.blue(pixel) / 255f - 0.406f) / 0.225f

                val pixelIdx = y * width + x
                val frameOffset = frameIdx * height * width
                val channelSize = frameCount * height * width

                inputArray[0 * channelSize + frameOffset + pixelIdx] = r
                inputArray[1 * channelSize + frameOffset + pixelIdx] = g
                inputArray[2 * channelSize + frameOffset + pixelIdx] = b
            }
        }
    }

    return inputArray
}

private fun calculateHeartRate(rppgSignal: FloatArray, fps: Float): Float {
    val windowSize = (fps * 4).toInt()
    if (rppgSignal.size < windowSize) return 0f

    val peaks = findPeaks(rppgSignal)
    val avgInterval = if (peaks.size > 1) {
        (peaks.last() - peaks.first()).toFloat() / (peaks.size - 1)
    } else {
        return 0f
    }

    val heartRate = (fps * 60f) / avgInterval
    return heartRate.coerceIn(40f, 200f)
}

private fun findPeaks(signal: FloatArray): List<Int> {
    val peaks = mutableListOf<Int>()
    val threshold = signal.average().toFloat()

    for (i in 1 until signal.size - 1) {
        if (signal[i] > signal[i - 1] && signal[i] > signal[i + 1] && signal[i] > threshold) {
            peaks.add(i)
        }
    }

    return peaks
}