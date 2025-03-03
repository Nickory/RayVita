package com.codelab.basiclayouts

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.media.ImageReader
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.codelab.basiclayouts.ui.theme.MySootheTheme
import kotlinx.coroutines.delay



//import android.content.Context
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HeartRateApp() }
    }
}

@Composable
fun HeartRateApp() {
    MySootheTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                CameraPreviewSection()
                HeartRateDisplaySection()
                HeartRateWaveformSection(signalBuffer = signalBuffer)
                PermissionRequester()
            }
        }
    }
}

//

@Composable
fun HeartRateWaveformSection(signalBuffer: MutableState<MutableList<Float>>) {
    val BUFFER_SIZE = 100

    // 基础版 remember 使用
    val visibleSignal = remember {
        mutableStateOf<List<Float>>(emptyList())
    }

    // 在副作用中更新信号
    LaunchedEffect(signalBuffer.value) {
        visibleSignal.value = signalBuffer.value.takeLast(BUFFER_SIZE)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            if (visibleSignal.value.isEmpty()) return@Canvas

            // 直接计算极值（不缓存）
            val minValue = visibleSignal.value.minOrNull() ?: 0f
            val maxValue = visibleSignal.value.maxOrNull() ?: 0f

            val width = size.width
            val height = size.height
            val step = width / (visibleSignal.value.size - 1).coerceAtLeast(1)

            // 构建路径
            val path = Path().apply {
                visibleSignal.value.forEachIndexed { index, value ->
                    val x = step * index
                    val y = if (maxValue != minValue) {
                        height - ((value - minValue) * height / (maxValue - minValue))
                    } else {
                        height / 2f
                    }

                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }

            // 绘制路径
            drawPath(
                path = path,
                color = Color.Red, // 简化颜色配置
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        Text(
            text = "心率波形",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


// 修改CameraPreviewSection组件
@Composable
fun CameraPreviewSection() {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasCameraPermission = isGranted
    }
    val cameraIds = rememberCameraIds(context)
    var currentCameraId by remember { mutableStateOf(cameraIds.front) }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(Manifest.permission.CAMERA)
        } else {
            hasCameraPermission = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .padding(16.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    TextureView(ctx).apply {
                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
                            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                                surface.setDefaultBufferSize(320, 240)
                                openCamera(this@apply, currentCameraId ?: return)
                            }
                            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, w: Int, h: Int) {}
                            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = true
                            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
            )
        } else {
            Text(
                "需要摄像头权限",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // 返回按钮
        IconButton(
            onClick = { (context as Activity).finish() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.surfaceTint,
            )
        }

        // 摄像头切换按钮
        if (cameraIds.back != null && cameraIds.front != null) {
            IconButton(
                onClick = {
                    currentCameraId = if (currentCameraId == cameraIds.front) cameraIds.back else cameraIds.front
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = if (currentCameraId == cameraIds.front) Icons.Default.CameraRear else Icons.Default.CameraFront,
                    contentDescription = "切换摄像头",
                    tint = MaterialTheme.colorScheme.surfaceTint
                )
            }
        }

        Text(
            "请将面部对准放摄像头以测量心率",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(8.dp),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// 新增摄像头ID获取函数
@Composable
fun rememberCameraIds(context: Context): CameraIds {
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    return remember {
        var front: String? = null
        var back: String? = null
        cameraManager.cameraIdList.forEach { id ->
            when (cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.LENS_FACING)) {
                CameraCharacteristics.LENS_FACING_FRONT -> front = id
                CameraCharacteristics.LENS_FACING_BACK -> back = id
            }
        }
        CameraIds(front, back)
    }
}

data class CameraIds(val front: String?, val back: String?)

private fun saveHeartRate(heartRate: Int) {
//    val sharedPreferences = context.getSharedPreferences("heart_rate_data", MODE_PRIVATE)
//    val editor = sharedPreferences.edit()
//    val currentData = sharedPreferences.getString("data", "") ?: ""
//    editor.putString("data", "$currentData#$heartRate")
//    editor.apply()
}

@Composable
fun HeartRateDisplaySection() {
    var heartRate by remember { mutableStateOf(0) }
    var isMeasuring by remember { mutableStateOf(false) }
    val animatedHeartRate by animateFloatAsState(
        targetValue = heartRate.toFloat(),
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
    )
    val context = LocalContext.current

    LaunchedEffect(isMeasuring) {
        if (isMeasuring) {
            while (true) {
                heartRate = calculateHeartRate()
                saveHeartRate(
                    heartRate,
                ) // 保存心率数据
                delay(1000)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.wrapContentSize(),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "${animatedHeartRate.toInt()}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "BPM",
                    style = MaterialTheme.typography.titleMedium
                )
                if (isMeasuring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
        Text(
            "正常心率范围：60-100 BPM",
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Button(
            onClick = { isMeasuring = !isMeasuring },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(if (isMeasuring) "停止测量" else "开始测量")
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedCameraPreview() {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        CameraPreview(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}
// 修改后的CameraPreview组件
@Composable
fun CameraPreview(modifier: Modifier) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(Manifest.permission.CAMERA)
        } else {
            hasCameraPermission = true
        }
    }

    if (hasCameraPermission) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
                        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                            // 设置默认缓冲区尺寸
                            surface.setDefaultBufferSize(320, 240)
                            val currentCameraId = null
                            openCamera(this@apply, currentCameraId ?: return)
                        }
                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, w: Int, h: Int) {}
                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = true
                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize(0.5f)
                .clip(RoundedCornerShape(16.dp))
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("需要摄像头权限")
        }
    }
}


@Composable
private fun PermissionRequester() {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted && !ActivityCompat.shouldShowRequestPermissionRationale(
                context as ComponentActivity,
                Manifest.permission.CAMERA
            )
        ) {
            showRationale = true
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("需要相机权限") },
            text = { Text("此功能需要访问相机以测量心率") },
            confirmButton = {
                TextButton({ launcher.launch(Manifest.permission.CAMERA) }) {
                    Text("确定")
                }
            }
        )
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HeartRateDisplay() {
    var heartRate by remember { mutableStateOf(0) }
    val animatedHeartRate by animateFloatAsState(
        targetValue = heartRate.toFloat(),
        animationSpec = tween(500)
    )

    LaunchedEffect(Unit) {
        while (true) {
            heartRate = calculateHeartRate()
            delay(1000)
        }
    }

    Card(
        modifier = Modifier
            .padding(32.dp),

        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            AnimatedContent(
                targetState = animatedHeartRate,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height } + fadeOut())
                }
            ) { targetRate ->
                Text(
                    text = "${targetRate.toInt()}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.graphicsLayer {
                        scaleX = 1 + (targetRate - heartRate) * 0.1f
                        var scaleY = 1 + (targetRate - heartRate) * 0.1f
                    }
                )
            }
            Text(
                text = "BPM",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// 以下是相机处理相关实现
private val signalBuffer = mutableStateOf(mutableListOf<Float>())
private const val BUFFER_SIZE = 150
private lateinit var imageReader: ImageReader


// 修改后的openCamera函数
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
private fun openCamera(textureView: TextureView, cameraId: String) {
    val cameraManager = textureView.context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    var currentCamera: CameraDevice? = null

    try {
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                currentCamera = camera
                val surfaceTexture = textureView.surfaceTexture?.apply {
                    setDefaultBufferSize(320, 240)
                }
                val previewSurface = Surface(surfaceTexture)

                // 配置ImageReader
                imageReader = ImageReader.newInstance(320, 240, ImageFormat.YUV_420_888, 2).apply {
                    setOnImageAvailableListener({ reader ->
                        reader.acquireLatestImage()?.use { image ->
                            processImage(image)
                        }
                    }, Handler(HandlerThread("ImageProcessor").apply { start() }.looper))
                }

                val imageSurface = imageReader.surface

                camera.createCaptureSession(listOf(previewSurface, imageSurface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                            addTarget(previewSurface)
                            addTarget(imageSurface)
                        }
                        session.setRepeatingRequest(request.build(), null, null)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e("Camera", "Session configuration failed")
                    }
                }, null)
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
                currentCamera = null
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
                currentCamera = null
            }
        }, null)
    } catch (e: SecurityException) {
        Log.e("Camera", "Security exception: ${e.message}")
    } catch (e: CameraAccessException) {
        Log.e("Camera", "Access exception: ${e.message}")
    }
}


private var frameCounter = 0
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
private fun processImage(image: Image) {
    frameCounter++
    if (frameCounter % 2 != 0) return // 每 5 帧处理一次
    val yPlane = image.planes[0]
    val buffer = yPlane.buffer
    val rowStride = yPlane.rowStride
    val pixelStride = yPlane.pixelStride

    var total = 0L
    val width = image.width
    val height = image.height

    for (row in 0 until height) {
        buffer.position(row * rowStride)
        var rowSum = 0L
        for (col in 0 until width) {
            buffer.position(row * rowStride + col * pixelStride)
            rowSum += buffer.get().toInt() and 0xFF
        }
        total += rowSum
    }

    val average = total.toFloat() / (width * height)

    synchronized(signalBuffer) {
        val currentList = signalBuffer.value.toMutableList()
        currentList.add(average)
        while (currentList.size > BUFFER_SIZE) {
            currentList.removeAt(0)
        }
        signalBuffer.value = currentList // 更新 MutableState 的值
    }
}

//private fun calculateHeartRate(): Int {
//    if (signalBuffer.size < BUFFER_SIZE) return 0
//    val peaks = (1 until signalBuffer.lastIndex).count {
//        signalBuffer[it] > signalBuffer[it - 1] && signalBuffer[it] > signalBuffer[it + 1]
//    }
//    return (peaks * 12).coerceIn(50..640) // 简化计算并限制合理范围
//}

private fun calculateHeartRate(): Int {
    // 访问 signalBuffer 的值
    val signal = signalBuffer.value
    if (signal.size < BUFFER_SIZE) return 0

    // 1. 信号预处理
    val filteredSignal = preprocessSignal(signal)

    // 2. 动态阈值峰值检测
    val validPeaks = detectValidPeaks(filteredSignal)

    // 3. 动态心率计算
    return calculateBpm(validPeaks).coerceIn(40..200)
}

// 信号预处理（带通滤波）
private fun preprocessSignal(raw: List<Float>): FloatArray {
    val size = raw.size
    val processed = FloatArray(size)

    // 滑动平均滤波（窗口5帧，低通）
    val lowPass = FloatArray(size)
    for (i in 4 until size) {
        lowPass[i] = raw.subList(i-4, i+1).average().toFloat()
    }

    // 高通滤波（去除基线漂移）
    val highPass = FloatArray(size)
    for (i in 20 until size) {
        highPass[i] = raw[i] - raw.subList(i-20, i).average().toFloat()
    }

    // 组合滤波（带通）
    for (i in 20 until size) {
        processed[i] = highPass[i] * 0.8f + lowPass[i] * 0.2f
    }
    return processed
}

// 改进的峰值检测算法
private fun detectValidPeaks(signal: FloatArray): List<Int> {
    val peaks = mutableListOf<Int>()
    var dynamicThreshold = 0f
    var risingEdge = false
    var lastPeakPosition = -15  // 最小峰间距（约0.5秒）

    for (i in 1 until signal.size - 1) {
        // 动态阈值计算（基于前3秒的平均值）
        if (i > 90) {  // 3秒数据（假设30fps）
            val window = signal.copyOfRange(i-90, i)
            dynamicThreshold = (window.average() * 1.3f).toFloat()
        }

        // 上升沿检测
        val isRising = signal[i] > signal[i-1]

        // 发现有效峰值
        if (risingEdge && !isRising && signal[i] > dynamicThreshold) {
            if (i - lastPeakPosition > 15) {  // 最小峰间距限制
                peaks.add(i)
                lastPeakPosition = i
                risingEdge = false
                dynamicThreshold *= 0.9f  // 动态调整阈值
            }
        }
        risingEdge = isRising
    }
    return peaks
}

// 基于时间间隔的心率计算
private fun calculateBpm(peaks: List<Int>): Int {
    if (peaks.size < 2) return 0

    // 计算峰间间隔（帧数）
    val intervals = peaks.zipWithNext { a, b -> b - a }

    // 去除异常间隔（使用中位数滤波）
    val median = intervals.sorted()[intervals.size / 2]
    val validIntervals = intervals.filter {
        it in (median * 0.7).toInt()..(median * 1.3).toInt()
    }

    // 计算平均心率（假设30fps）
    if (validIntervals.isEmpty()) return 0
    val avgInterval = validIntervals.average()
    return ((30 * 60) / avgInterval).toInt()
}


// 主题定义（需要添加到你的主题文件中）
// 在 ui/theme/Theme.kt 中添加：
// private val DarkColorScheme = darkColorScheme(...)
// private val LightColorScheme = lightColorScheme(...)

@Preview(showBackground = true)
@Composable
fun PreviewHeartRateApp() {
    MySootheTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            HeartRateDisplay()
            Text(
                "相机预览",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clip(RoundedCornerShape(36.dp))
            )
        }
    }
}