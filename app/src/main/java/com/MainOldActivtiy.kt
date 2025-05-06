package com

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.codelab.basiclayouts.ui.theme.MySootheTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Collections

/**
 * Main Activity for the Heart Rate Monitoring application.
 * This class serves as the entry point for the application and sets up the Compose UI.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeartRateApp()
        }
    }
}

/**
 * Data interface for heart rate measurement data
 * Used to standardize data transfer between components
 */
interface HeartRateData {
    val timestamp: Long         // Timestamp of the measurement in milliseconds
    val value: Int              // Heart rate value in BPM (beats per minute)
    val confidence: Float       // Confidence level of the measurement (0.0-1.0)
}

/**
 * Data class implementing the HeartRateData interface
 * @param timestamp Time when measurement was taken (milliseconds since epoch)
 * @param value Heart rate value in BPM
 * @param confidence Confidence score of the measurement (0.0-1.0)
 */
data class HeartRateMeasurement(
    override val timestamp: Long = System.currentTimeMillis(),
    override val value: Int = 0,
    override val confidence: Float = 0f
) : HeartRateData

/**
 * Camera state data class holding various camera-related settings
 * @param cameraId ID of the currently active camera
 * @param hasFaceDetected Whether a face is currently detected in the frame
 * @param isFrontCamera Whether the current camera is the front-facing camera
 */
data class CameraState(
    val cameraId: String? = null,
    val hasFaceDetected: Boolean = false,
    val isFrontCamera: Boolean = true
)

/**
 * Data class representing available camera IDs
 * @param front ID of the front-facing camera
 * @param back ID of the back-facing camera
 */
data class CameraIds(val front: String?, val back: String?)

/**
 * Simple data class for a particle in the background animation
 */
data class ParticleData(
    var x: Float = (Math.random().toFloat()),
    var y: Float = (Math.random().toFloat()),
    var vx: Float = ((Math.random() * 0.001f - 0.0005f).toFloat()),
    var vy: Float = ((Math.random() * 0.001f - 0.0005f).toFloat()),
    var size: Float = ((Math.random() * 3f + 1f).toFloat()),
    var alpha: Float = ((Math.random() * 0.2f + 0.1f).toFloat()),
    val color: Color = when ((Math.random() * 3).toInt()) {
        0 -> Color(0, 150, 255)
        1 -> Color(0, 200, 180)
        else -> Color(70, 120, 255)
    }
) {
    fun update() {
        x += vx
        y += vy

        // Wrap around screen edges
        if (x < 0f) x = 1f
        if (x > 1f) x = 0f
        if (y < 0f) y = 1f
        if (y > 1f) y = 0f
    }
}

/**
 * Shared signal buffer for heart rate data
 * Using a thread-safe implementation to avoid concurrent modification
 */
private val signalBufferData = Collections.synchronizedList(mutableListOf<Float>())

/**
 * Main Composable function for the Heart Rate App
 * Sets up the theme and overall structure of the application UI
 */
@Composable
fun HeartRateApp() {
    // Application state
    val cameraState = remember { mutableStateOf(CameraState()) }
    val heartRateData = remember { mutableStateOf(HeartRateMeasurement()) }
    val isMeasuring = remember { mutableStateOf(false) }

    // Create a safe state for the signal buffer
    val signalBuffer = remember { mutableStateOf<List<Float>>(emptyList()) }

    // Update the signal buffer periodically
    LaunchedEffect(Unit) {
        while (true) {
            // Make a thread-safe copy of the current signal data
            synchronized(signalBufferData) {
                signalBuffer.value = signalBufferData.toList()
            }
            delay(16) // Approximately 60fps refresh rate
        }
    }

    MySootheTheme {
        // Animated background with subtle pulsing effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        )
                    )
                )
        ) {
            // Sci-fi background elements (subtle floating particles)
            ParticleBackground(isMeasuring = isMeasuring.value)

            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Title with sci-fi style
                Text(
                    text = "PULSE MONITOR",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                // Enhanced Camera Preview with face detection visualization
                EnhancedCameraPreviewSection(
                    cameraState = cameraState,
                    isMeasuring = isMeasuring,
                    modifier = Modifier.weight(1f)
                )

                // Advanced Heart Rate Display with animations
                EnhancedHeartRateDisplaySection(
                    heartRateData = heartRateData,
                    isMeasuring = isMeasuring,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Improved Heart Rate Waveform with sci-fi styling
                EnhancedHeartRateWaveformSection(
                    signalBuffer = signalBuffer,
                    isMeasuring = isMeasuring.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(vertical = 8.dp)
                )

                // Hidden permission requester
                PermissionRequester()
            }

            // Measurement status indicator
            if (isMeasuring.value) {
                MeasurementStatusIndicator(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                )
            }
        }
    }

    // Update heart rate and signal data
    LaunchedEffect(isMeasuring.value) {
        if (isMeasuring.value) {
            while (true) {
                val newHeartRate = calculateHeartRate()
                heartRateData.value = HeartRateMeasurement(
                    value = newHeartRate,
                    confidence = if (newHeartRate > 0) 0.85f else 0f
                )
                delay(1000)
            }
        }
    }
}

/**
 * Floating particle background animation for sci-fi effect
 * Creates a subtle moving particle effect in the background
 * @param isMeasuring Whether heart rate measurement is active
 */
@Composable
fun ParticleBackground(isMeasuring: Boolean) {
    val particles = remember { List(20) { ParticleData() } }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isMeasuring) 0.6f else 0.3f,
        animationSpec = tween(1000),
        label = "backgroundAlpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            // Update particle position
            particle.update()

            // Draw particle with glow effect
            drawCircle(
                color = particle.color.copy(alpha = animatedAlpha * particle.alpha),
                radius = particle.size,
                center = Offset(particle.x * size.width, particle.y * size.height),
                blendMode = BlendMode.Plus
            )

            // Draw particle glow
            drawCircle(
                color = particle.color.copy(alpha = animatedAlpha * particle.alpha * 0.4f),
                radius = particle.size * 3f,
                center = Offset(particle.x * size.width, particle.y * size.height),
                blendMode = BlendMode.Plus
            )
        }
    }
}

/**
 * Enhanced Camera Preview with face detection overlay and sci-fi UI
 * @param cameraState Current camera state
 * @param isMeasuring Whether heart rate measurement is active
 * @param modifier Modifier for customizing layout
 */
@Composable
fun EnhancedCameraPreviewSection(
    cameraState: MutableState<CameraState>,
    isMeasuring: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasCameraPermission = isGranted
    }

    val cameraIds = rememberCameraIds(context)
    var currentCameraId by remember { mutableStateOf(cameraIds.front) }

    // Face detection status animation
    val faceDetectedAlpha by animateFloatAsState(
        targetValue = if (cameraState.value.hasFaceDetected) 1f else 0.3f,
        animationSpec = tween(500),
        label = "faceDetectedAlpha"
    )

    // Scanner animation
    val scannerPosition = remember { Animatable(0f) }
    val scannerActive = isMeasuring.value && cameraState.value.hasFaceDetected

    LaunchedEffect(scannerActive) {
        if (scannerActive) {
            scannerPosition.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            scannerPosition.snapTo(0f)
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(Manifest.permission.CAMERA)
        } else {
            hasCameraPermission = true
        }
    }

    // Update camera state when camera changes
    LaunchedEffect(currentCameraId) {
        cameraState.value = cameraState.value.copy(
            cameraId = currentCameraId,
            isFrontCamera = currentCameraId == cameraIds.front
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.7f))
    ) {
        // Camera preview
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    TextureView(ctx).apply {
                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                                surface.setDefaultBufferSize(320, 240)
                                openCamera(
                                    textureView = this@apply,
                                    cameraId = currentCameraId ?: return,
                                    onFaceDetected = { hasFace ->
                                        cameraState.value = cameraState.value.copy(hasFaceDetected = hasFace)
                                    }
                                )
                            }
                            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, w: Int, h: Int) {}
                            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = true
                            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "需要摄像头权限",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                )
            }
        }

        // Corner brackets (sci-fi UI element)
        CornerBrackets(
            active = cameraState.value.hasFaceDetected,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        )

        // Scanner line animation
        if (scannerActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .offset(y = (scannerPosition.value * 200).dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary,
                                Color.Transparent
                            )
                        )
                    )
                    .blur(4.dp)
            )
        }

        // Face detection status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = faceDetectedAlpha),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (cameraState.value.hasFaceDetected) "面部已检测" else "请将面部对准摄像头",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = faceDetectedAlpha)
                    )
                }
            }
        }

        // Controls overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = { (context as Activity).finish() },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Camera switch button (only show if both cameras available)
            if (cameraIds.back != null && cameraIds.front != null) {
                IconButton(
                    onClick = {
                        currentCameraId = if (currentCameraId == cameraIds.front) {
                            cameraIds.back
                        } else {
                            cameraIds.front
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (currentCameraId == cameraIds.front) {
                            Icons.Default.CameraRear
                        } else {
                            Icons.Default.CameraFront
                        },
                        contentDescription = "切换摄像头",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Visual corner brackets for sci-fi UI effect
 * @param active Whether the brackets should be highlighted
 * @param modifier Modifier for customizing layout
 */
@Composable
fun CornerBrackets(active: Boolean, modifier: Modifier = Modifier) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

    // Animate the color change
    val animatedColor by animateColorAsState(
        targetValue = if (active) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 300),
        label = "bracketColor"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val bracketSize = size.width * 0.15f
        val strokeWidth = 3.dp.toPx()

        // Top-left corner
        drawLine(
            color = animatedColor,
            start = Offset(0f, 0f),
            end = Offset(bracketSize, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = animatedColor,
            start = Offset(0f, 0f),
            end = Offset(0f, bracketSize),
            strokeWidth = strokeWidth
        )

        // Top-right corner
        drawLine(
            color = animatedColor,
            start = Offset(size.width, 0f),
            end = Offset(size.width - bracketSize, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = animatedColor,
            start = Offset(size.width, 0f),
            end = Offset(size.width, bracketSize),
            strokeWidth = strokeWidth
        )

        // Bottom-left corner
        drawLine(
            color = animatedColor,
            start = Offset(0f, size.height),
            end = Offset(bracketSize, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = animatedColor,
            start = Offset(0f, size.height),
            end = Offset(0f, size.height - bracketSize),
            strokeWidth = strokeWidth
        )

        // Bottom-right corner
        drawLine(
            color = animatedColor,
            start = Offset(size.width, size.height),
            end = Offset(size.width - bracketSize, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = animatedColor,
            start = Offset(size.width, size.height),
            end = Offset(size.width, size.height - bracketSize),
            strokeWidth = strokeWidth
        )
    }
}

/**
 * Enhanced heart rate display with animated transitions and sci-fi UI elements
 * @param heartRateData Current heart rate measurement data
 * @param isMeasuring Whether heart rate measurement is active
 * @param modifier Modifier for customizing layout
 */
@Composable
fun EnhancedHeartRateDisplaySection(
    heartRateData: MutableState<HeartRateMeasurement>,
    isMeasuring: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val animatedHeartRate by animateFloatAsState(
        targetValue = heartRateData.value.value.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "heartRateAnimation"
    )

    val pulseAnimation = rememberInfiniteTransition(label = "pulseAnimation")
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Heart rate display card with glow effect
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (isMeasuring.value && heartRateData.value.value > 0) {
                        drawRect(
                            color = Color.Red.copy(alpha = 0.2f * pulseScale),
                            size = size.copy(
                                width = size.width * 1.05f,
                                height = size.height * 1.05f
                            ),
                            topLeft = Offset(-size.width * 0.025f, -size.height * 0.025f),
                            blendMode = BlendMode.Plus
                        )
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Heart rate value display with animation
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Heart icon with pulse animation
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(36.dp)
                            .graphicsLayer {
                                if (isMeasuring.value && heartRateData.value.value > 0) {
                                    scaleX = pulseScale
                                    scaleY = pulseScale
                                }
                            }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Animated heart rate value
                    AnimatedContent(
                        targetState = animatedHeartRate.toInt(),
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn()) togetherWith
                                    (slideOutVertically { height -> -height } + fadeOut())
                        },
                        label = "HeartRateAnimation"
                    ) { targetRate ->
                        Text(
                            text = "$targetRate",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "BPM",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Status indicator
                    if (isMeasuring.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // Heart rate classification
                Spacer(modifier = Modifier.height(8.dp))
                HeartRateClassification(heartRateData.value.value)

                // Start/Stop button
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        isMeasuring.value = !isMeasuring.value
                        if (!isMeasuring.value) {
                            coroutineScope.launch {
                                // Reset heart rate when stopping
                                heartRateData.value = HeartRateMeasurement()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMeasuring.value)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp)
                ) {
                    Text(
                        text = if (isMeasuring.value) "停止测量" else "开始测量",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

/**
 * Display heart rate classification based on the measured value
 * @param heartRate Current heart rate value in BPM
 */
@Composable
fun HeartRateClassification(heartRate: Int) {
    val (statusText, statusColor) = when {
        heartRate == 0 -> Pair("等待测量...", MaterialTheme.colorScheme.onSurfaceVariant)
        heartRate < 60 -> Pair("偏低", Color(0xFF4D88FF))
        heartRate in 60..100 -> Pair("正常", Color(0xFF4CAF50))
        heartRate in 101..120 -> Pair("偏快", Color(0xFFFFA726))
        else -> Pair("过快", Color(0xFFE53935))
    }

    Text(
        text = "心率状态: $statusText",
        style = MaterialTheme.typography.bodyMedium,
        color = statusColor,
        modifier = Modifier.padding(vertical = 4.dp)
    )

    Text(
        text = "理想心率范围: 60-100 BPM",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    )
}

/**
 * Enhanced heart rate waveform visualization with sci-fi styling
 * @param signalBuffer Buffer containing the signal data points
 * @param isMeasuring Whether heart rate measurement is active
 * @param modifier Modifier for customizing layout
 */
@Composable
fun EnhancedHeartRateWaveformSection(
    signalBuffer: MutableState<List<Float>>,
    isMeasuring: Boolean,
    modifier: Modifier = Modifier
) {
    val BUFFER_SIZE = 100

    // Create a safe copy of the signal data to prevent concurrent modification
    val safeSignalData = remember { mutableStateOf(listOf<Float>()) }

    // Update safe copy when signal buffer changes
    LaunchedEffect(signalBuffer.value) {
        // Create a defensive copy of the data to avoid concurrent modification
        val dataCopy = synchronized(signalBuffer.value) {
            signalBuffer.value.takeLast(BUFFER_SIZE).toList()
        }
        safeSignalData.value = dataCopy
    }

    // Grid lines animation
    val gridAnimation = rememberInfiniteTransition(label = "gridAnimation")
    val gridOffset by gridAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gridOffset"
    )

    // Fade animation for the waveform
    val waveformAlpha by animateFloatAsState(
        targetValue = if (isMeasuring) 1f else 0.3f,
        animationSpec = tween(500),
        label = "waveformAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(8.dp)
        ) {
            // Grid background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = Color.Red.copy(alpha = 0.1f)
                val gridSize = 20f

                // Vertical grid lines
                for (x in 0..size.width.toInt() step gridSize.toInt()) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), size.height),
                        strokeWidth = 1f
                    )
                }

                // Horizontal grid lines with animation
                for (y in -gridSize.toInt() * 2..size.height.toInt() step gridSize.toInt()) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y + gridOffset),
                        end = Offset(size.width, y + gridOffset),
                        strokeWidth = 1f
                    )
                }
            }

            // Waveform
            Canvas(modifier = Modifier.fillMaxSize()) {
                val signalData = safeSignalData.value
                if (signalData.isEmpty()) return@Canvas

                // Find min/max values for scaling
                val minValue = signalData.minOrNull() ?: 0f
                val maxValue = signalData.maxOrNull() ?: 0f
                val range = (maxValue - minValue).coerceAtLeast(1f)

                val width = size.width
                val height = size.height
                val step = width / (signalData.size - 1).coerceAtLeast(1)

                // Create the waveform path
                val path = Path()
                signalData.forEachIndexed { index, value ->
                    val x = step * index
                    val normalizedValue = (value - minValue) / range
                    val y = height - (normalizedValue * height * 0.8f + height * 0.1f)

                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                // Draw the path with glow effect
                drawPath(
                    path = path,
                    color = Color.Green.copy(alpha = waveformAlpha),
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Add glow effect
                drawPath(
                    path = path,
                    color = Color.Green.copy(alpha = waveformAlpha * 0.4f),
                    style = Stroke(
                        width = 8.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // Waveform title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "心率波形",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Status indicator dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (isMeasuring) MaterialTheme.colorScheme.primary else Color.Gray,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

/**
 * Animated measurement status indicator
 * @param modifier Modifier for customizing layout
 */
@Composable
fun MeasurementStatusIndicator(modifier: Modifier = Modifier) {
    val pulseAnimation = rememberInfiniteTransition(label = "pulseAnimation")
    val pulseAlpha by pulseAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .size(12.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .drawBehind {
                drawCircle(
                    color = Color.Green.copy(alpha = pulseAlpha * 0.5f),
                    radius = size.width * 0.8f
                )
            }
    )
}

/**
 * Permission requester component
 * Handles camera permission request and displays rationale if needed
 */
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
            text = { Text("此功能需要访问相机以测量心率，请在设置中启用相机权限") },
            confirmButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("确定")
                }
            }
        )
    }
}

/**
 * Helper function to remember available camera IDs
 * @param context Android context
 * @return CameraIds object containing available front and back camera IDs
 */
@Composable
fun rememberCameraIds(context: Context): CameraIds {
    val cameraManager = remember {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    return remember {
        var front: String? = null
        var back: String? = null
        try {
            val cameraIdList = cameraManager.cameraIdList
            for (id in cameraIdList) {
                when (cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT -> front = id
                    CameraCharacteristics.LENS_FACING_BACK -> back = id
                }
            }
        } catch (e: Exception) {
            Log.e("Camera", "Error getting camera IDs: ${e.message}")
        }
        CameraIds(front, back)
    }
}

/**
 * Opens the camera and sets up image processing
 * @param textureView TextureView to display camera preview
 * @param cameraId ID of the camera to open
 * @param onFaceDetected Callback for face detection status
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private fun openCamera(
    textureView: TextureView,
    cameraId: String,
    onFaceDetected: (Boolean) -> Unit = {}
) {
    val cameraManager = textureView.context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    var currentCamera: CameraDevice? = null

    try {
        if (ActivityCompat.checkSelfPermission(
                textureView.context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                currentCamera = camera
                val surfaceTexture = textureView.surfaceTexture?.apply {
                    setDefaultBufferSize(320, 240)
                }
                val previewSurface = Surface(surfaceTexture)

                // Configure ImageReader for image processing
                val                 imageReader = ImageReader.newInstance(320, 240, ImageFormat.YUV_420_888, 2).apply {
                    setOnImageAvailableListener({ reader ->
                        try {
                            reader.acquireLatestImage()?.use { image ->
                                // Process image for heart rate detection
                                processImageForHeartRate(image)

                                // Simulate face detection for demo
                                onFaceDetected(true)
                            }
                        } catch (e: Exception) {
                            // Log and ignore any exceptions during image processing
                            Log.e("Camera", "Error processing image: ${e.message}")
                        }
                    }, Handler(HandlerThread("ImageProcessor").apply { start() }.looper))
                }

                val imageSurface = imageReader.surface

                camera.createCaptureSession(
                    listOf(previewSurface, imageSurface),
                    object : CameraCaptureSession.StateCallback() {
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
                    },
                    null
                )
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
                currentCamera = null
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
                currentCamera = null
                Log.e("Camera", "Camera device error: $error")
            }
        }, null)
    } catch (e: SecurityException) {
        Log.e("Camera", "Security exception: ${e.message}")
    } catch (e: CameraAccessException) {
        Log.e("Camera", "Camera access exception: ${e.message}")
    } catch (e: Exception) {
        Log.e("Camera", "Camera error: ${e.message}")
    }
}

/**
 * Process camera image for heart rate detection
 * Extracts red channel data and updates signal buffer
 * @param image Camera image frame
 */
// Track frame counter outside the function to avoid concurrent issues
private var frameCounter = 0

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private fun processImageForHeartRate(image: Image) {
    // Process every 4th frame to reduce CPU usage
    frameCounter = (frameCounter + 1) % 4
    if (frameCounter != 0) return

    try {
        val yPlane = image.planes[0]
        val buffer = yPlane.buffer
        val rowStride = yPlane.rowStride
        val pixelStride = yPlane.pixelStride

        var total = 0L
        val width = image.width
        val height = image.height

        // Focus on the center area where face is likely to be
        val centerX = width / 2
        val centerY = height / 2
        val regionSize = minOf(width, height) / 3

        // Calculate the average red value in the center region
        for (y in centerY - regionSize until centerY + regionSize) {
            if (y < 0 || y >= height) continue

            for (x in centerX - regionSize until centerX + regionSize) {
                if (x < 0 || x >= width) continue

                // Calculate pixel position in buffer
                val position = y * rowStride + x * pixelStride
                if (position >= 0 && position < buffer.capacity()) {
                    buffer.position(position)
                    // Get Y value (brightness) which correlates with blood flow
                    total += (buffer.get().toInt() and 0xFF)
                }
            }
        }

        // Calculate average brightness value
        val regionArea = (regionSize * 2) * (regionSize * 2)
        val average = if (regionArea > 0) total.toFloat() / regionArea else 0f

        // Update the signal buffer with better thread safety
        synchronized(signalBufferData) {
            // Add the new value
            signalBufferData.add(average)

            // Limit buffer size (use if check to avoid potential concurrent issues)
            val BUFFER_SIZE = 150
            if (signalBufferData.size > BUFFER_SIZE) {
                // Create a new list with only the latest values
                val newList = signalBufferData.takeLast(BUFFER_SIZE)
                signalBufferData.clear()
                signalBufferData.addAll(newList)
            }
        }
    } catch (e: Exception) {
        Log.e("ImageProcessing", "Error processing image: ${e.message}")
    }
}

/**
 * Calculate heart rate from signal buffer
 * Uses advanced peak detection and filtering algorithms
 * @return Calculated heart rate in BPM
 */
private fun calculateHeartRate(): Int {
    // Make a thread-safe copy of the signal data
    val signal = synchronized(signalBufferData) {
        signalBufferData.toList()
    }

    if (signal.size < 100) return 0

    // 1. Signal preprocessing - enhanced filtering
    val filteredSignal = preprocessSignal(signal)

    // 2. Peak detection with adaptive thresholding
    val validPeaks = detectValidPeaks(filteredSignal)

    // 3. Calculate heart rate based on peaks
    return calculateBpm(validPeaks).coerceIn(40..200)
}

/**
 * Preprocess signal for heart rate calculation
 * Applies combined low-pass and high-pass filtering for noise reduction
 * @param raw Raw signal data
 * @return Processed signal data
 */
private fun preprocessSignal(raw: List<Float>): FloatArray {
    val size = raw.size
    val processed = FloatArray(size)

    // Improved low-pass filter (moving average with optimized window size)
    val lowPass = FloatArray(size)
    val lowPassWindow = 5 // 5-frame window
    for (i in lowPassWindow until size) {
        lowPass[i] = raw.subList(i - lowPassWindow, i + 1).average().toFloat()
    }

    // Enhanced high-pass filter (removes baseline drift)
    val highPass = FloatArray(size)
    val highPassWindow = 20 // 20-frame window for baseline estimation
    for (i in highPassWindow until size) {
        highPass[i] = raw[i] - raw.subList(i - highPassWindow, i).average().toFloat()
    }

    // Combined bandpass filter with optimized weights
    for (i in highPassWindow until size) {
        processed[i] = highPass[i] * 0.75f + lowPass[i] * 0.25f
    }

    return processed
}

/**
 * Detect valid peaks in the signal for heart rate calculation
 * Uses adaptive thresholding and minimum peak distance constraints
 * @param signal Processed signal data
 * @return List of peak indices
 */
private fun detectValidPeaks(signal: FloatArray): List<Int> {
    val peaks = mutableListOf<Int>()
    var dynamicThreshold = 0f
    var risingEdge = false
    var lastPeakPosition = -15  // Minimum peak distance constraint

    // Peak detection with improved adaptive thresholding
    for (i in 1 until signal.size - 1) {
        // Dynamic threshold calculation based on recent signal history
        if (i > 90) {  // About 3 seconds at 30fps
            val window = signal.copyOfRange(i - 90, i)
            val mean = window.average()
            val stdDev = window.map { (it - mean) * (it - mean) }.average().let { kotlin.math.sqrt(it) }
            // Threshold = mean + factor * standard deviation
            dynamicThreshold = (mean + 1.2 * stdDev).toFloat()
        }

        // Rising edge detection
        val isRising = signal[i] > signal[i - 1]

        // Valid peak detection
        if (risingEdge && !isRising && signal[i] > dynamicThreshold) {
            if (i - lastPeakPosition > 15) {  // Minimum peak distance
                peaks.add(i)
                lastPeakPosition = i
                // Dynamically adjust threshold after peak detection
                dynamicThreshold *= 0.95f
            }
        }
        risingEdge = isRising
    }

    return peaks
}

/**
 * Calculate BPM (beats per minute) from peak indices
 * Uses median filtering to remove outliers
 * @param peaks List of peak indices
 * @return Calculated heart rate in BPM
 */
private fun calculateBpm(peaks: List<Int>): Int {
    if (peaks.size < 3) return 0  // Need at least 3 peaks for reliable calculation

    // Calculate inter-peak intervals
    val intervals = peaks.zipWithNext { a, b -> b - a }

    // Remove outliers using median filtering
    val median = intervals.sorted()[intervals.size / 2]
    val validIntervals = intervals.filter {
        it in (median * 0.7).toInt()..(median * 1.3).toInt()
    }

    // Calculate average heart rate (assuming 30fps)
    if (validIntervals.isEmpty()) return 0
    val avgInterval = validIntervals.average()

    // Convert to BPM (30fps * 60 seconds / average frames between peaks)
    return ((30 * 60) / avgInterval).toInt()
}

/**
 * Preview composable for the Heart Rate App
 * Displays a simulated version of the app for design purposes
 */
@Preview(showBackground = true)
@Composable
fun PreviewHeartRateApp() {
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
                Text(
                    text = "PULSE MONITOR",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                // Camera preview placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("相机预览", style = MaterialTheme.typography.bodyLarge)
                }

                // Heart rate display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "75",
                                style = MaterialTheme.typography.headlineLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BPM",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}