package com.codelab.basiclayouts.ui.screen

import android.Manifest
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import java.util.Collections
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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
        0 -> Color(100, 180, 255, 150)
        1 -> Color(120, 200, 255, 150)
        else -> Color(140, 160, 255, 150)
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
enum class CardTab {
    HEART_RATE,
    WAVEFORM
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
    val context = LocalContext.current

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

    // VisionPro-inspired color palette
    val accentBlue = Color(70, 150, 255, 255)
    val accentPurple = Color(180, 120, 255, 255)
    val glassBackground = Color(240, 245, 250, 180)
    val glassBackgroundDarker = Color(230, 235, 240, 200)
    val textColor = Color(20, 30, 40, 220)
    val subtextColor = Color(60, 70, 80, 180)

    MySootheTheme {
        // Full screen background with a subtle gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(240, 245, 255, 255),
                            Color(230, 235, 250, 255)
                        )
                    )
                )
        ) {
            // Subtle particle background
            VisionProParticleBackground(isMeasuring = isMeasuring.value)

            // Main content column with proper spacing
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Back button row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 0.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左上角返回按钮
                    IconButton(
                        onClick = {
                            // 处理返回逻辑，结束 Activity
                            (context as? ComponentActivity)?.finish()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(255, 255, 255, 120),
                                shape = CircleShape
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(20, 30, 40, 180),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // App Title
                Text(
                    text = "rPPG Extraction",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.sp
                    ),
                    modifier = Modifier.padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    color = textColor
                )

                // Face detection section
                FaceDetectionStatusCard(
                    hasFaceDetected = cameraState.value.hasFaceDetected,
                    modifier = Modifier.fillMaxWidth()
                )

                // Camera preview with VisionPro style
                VisionProCameraPreview(
                    cameraState = cameraState,
                    isMeasuring = isMeasuring,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .weight(1f)
                )

                // Combined Health Card (replaces separate HR and Waveform cards)
                CombinedHealthCard(
                    heartRate = heartRateData.value.value,
                    signalBuffer = signalBuffer,
                    isMeasuring = isMeasuring.value,
                    modifier = Modifier.fillMaxWidth()
                )

                // Signal quality indicator
                SignalQualityCard(
                    quality = "High",
                    modifier = Modifier.fillMaxWidth()
                )

                // Action button
                MeasurementButton(
                    isMeasuring = isMeasuring.value,
                    onClick = { isMeasuring.value = !isMeasuring.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Hidden permission requester
                PermissionRequester()
            }
        }
    }

    // Update heart rate and signal data (keeping original logic)
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
 * VisionPro-style particle background animation
 * Creates a subtle floating particle effect
 */
@Composable
fun VisionProParticleBackground(isMeasuring: Boolean) {
    val particles = remember { List(30) { ParticleData() } }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isMeasuring) 0.4f else 0.2f,
        animationSpec = tween(1000),
        label = "backgroundAlpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            // Update particle position
            particle.update()

            // Draw particle with subtle glow
            drawCircle(
                color = particle.color.copy(alpha = animatedAlpha * particle.alpha),
                radius = particle.size * 1.5f,
                center = Offset(particle.x * size.width, particle.y * size.height),
                blendMode = BlendMode.SrcOver
            )

            // Draw particle glow
            drawCircle(
                color = particle.color.copy(alpha = animatedAlpha * particle.alpha * 0.3f),
                radius = particle.size * 5f,
                center = Offset(particle.x * size.width, particle.y * size.height),
                blendMode = BlendMode.SrcOver
            )
        }
    }
}

/**
 * VisionPro-style face detection status card
 */
@Composable
fun FaceDetectionStatusCard(
    hasFaceDetected: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = if (hasFaceDetected) Color(50, 200, 100) else Color(100, 100, 100),
        animationSpec = tween(500),
        label = "statusColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(240, 245, 250, 180))
            .border(
                width = 1.dp,
                color = Color(255, 255, 255, 180),
                shape = RoundedCornerShape(20.dp)
            )
            .blur(0.5.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Status icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(animatedColor, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (hasFaceDetected) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Status text
            Text(
                text = if (hasFaceDetected) "Face detected" else "Position face in frame",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp
                ),
                color = Color(20, 30, 40, 220)
            )
        }
    }
}

/**
 * VisionPro-style camera preview section
 */
@Composable
fun VisionProCameraPreview(
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
            .clip(RoundedCornerShape(32.dp))
            .background(Color(230, 235, 240, 150))
            .border(
                width = 1.dp,
                color = Color(255, 255, 255, 180),
                shape = RoundedCornerShape(32.dp)
            )
            .blur(radiusX = 0.5.dp, radiusY = 0.5.dp)
    ) {
        // Camera preview
        if (hasCameraPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(30.dp))
            ) {
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

                // Analyzing overlay
                if (scannerActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Scanner effect
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .offset(y = ((scannerPosition.value * 2 - 1) * 120).dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(100, 180, 255, 200),
                                            Color(100, 180, 255, 200),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .blur(3.dp)
                        )

                        // "Analyzing" text
                        Text(
                            text = "Analyzing...",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Light,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp)
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(20, 30, 40, 150), RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Camera permission required",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                )
            }
        }

        // Face frame (subtle)
        if (cameraState.value.hasFaceDetected) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp)
            ) {
                val ovalWidth = size.width
                val ovalHeight = size.height * 1.3f

                drawOval(
                    color = Color(255, 255, 255, 60),
                    style = Stroke(width = 2.dp.toPx()),
                    topLeft = Offset(
                        -ovalWidth * 0.15f,
                        -ovalHeight * 0.15f
                    ),
                    size = Size(
                        ovalWidth * 1.3f,
                        ovalHeight
                    )
                )
            }
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
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(
                        Color(255, 255, 255, 120),
                        shape = CircleShape
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = if (currentCameraId == cameraIds.front) {
                        Icons.Default.CameraRear
                    } else {
                        Icons.Default.CameraFront
                    },
                    contentDescription = "Switch camera",
                    tint = Color(20, 30, 40, 180),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * VisionPro-style heart rate display card that expands to full width
 */
@Composable
fun HeartRateCard(
    heartRate: Int,
    modifier: Modifier = Modifier,
    expandedState: MutableState<ExpandedCardState>
) {
    // Animation for the heart icon pulse
    val pulseAnimation = rememberInfiniteTransition(label = "pulseAnimation")
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Check if this card is currently expanded
    val isExpanded = expandedState.value == ExpandedCardState.HEART_RATE

    // Animation for card expansion
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1.05f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "scaleAnimation"
    )

    // Animation for card opacity
    val cardAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0.9f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "alphaAnimation"
    )

    // Card width animation
    val cardWidthFraction by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0.5f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "cardWidthAnimation"
    )

    // Main Card
    Card(
        modifier = modifier
            .fillMaxWidth(cardWidthFraction)
            .graphicsLayer {
                scaleY = scale
                alpha = cardAlpha
            }
            .clickable {
                expandedState.value = if (isExpanded) ExpandedCardState.NONE else ExpandedCardState.HEART_RATE
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(240, 240, 250)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isExpanded) {
            // Expanded layout with full details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top section with heart rate
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Heart icon (pulsing subtly)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = Color(255, 70, 90, 220),
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer {
                                    scaleX = if (heartRate > 0) pulseScale else 1f
                                    scaleY = if (heartRate > 0) pulseScale else 1f
                                }
                        )

                        Text(
                            text = "Heart Rate Monitor",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(60, 70, 100, 230)
                        )
                    }

                    // Heart rate display
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AnimatedContent(
                            targetState = heartRate,
                            transitionSpec = {
                                (slideInVertically { height -> height } + fadeIn()) togetherWith
                                        (slideOutVertically { height -> -height } + fadeOut())
                            },
                            label = "HeartRateAnimation"
                        ) { targetRate ->
                            Text(
                                text = "$targetRate",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = (-1).sp
                                ),
                                color = Color(20, 30, 50, 240)
                            )
                        }

                        Text(
                            text = "bpm",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Normal
                            ),
                            color = Color(80, 90, 110, 200),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                Divider(
                    color = Color(200, 210, 230, 120),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Health metrics in two rows
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // First row of metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricItem(
                            title = "HRV",
                            value = "60",
                            unit = "ms",
                            icon = Icons.Rounded.Timeline
                        )

                        MetricItem(
                            title = "SPO2",
                            value = "98",
                            unit = "%",
                            icon = Icons.Rounded.Opacity
                        )

                        MetricItem(
                            title = "Stress",
                            value = "Low",
                            unit = "",
                            icon = Icons.Rounded.Spa
                        )
                    }

                    // Second row of metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricItem(
                            title = "Min",
                            value = "58",
                            unit = "bpm",
                            icon = null
                        )

                        MetricItem(
                            title = "Avg",
                            value = "72",
                            unit = "bpm",
                            icon = null
                        )

                        MetricItem(
                            title = "Max",
                            value = "124",
                            unit = "bpm",
                            icon = null
                        )
                    }
                }

                // Daily trend chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(230, 235, 250, 180))
                        .padding(8.dp)
                ) {
                    // Placeholder for chart
                    Text(
                        text = "Daily Trend",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(100, 110, 130),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                    )

                    // Simulate a heart rate chart line
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path()
                        val width = size.width
                        val height = size.height

                        // Generate a simulated heart rate line
                        path.moveTo(0f, height * 0.6f)
                        for (i in 1..10) {
                            val x = width * i / 10
                            // Simulate some variance in the heart rate
                            var y = height * (0.6f + (sin(i * 0.8) * 0.15f))
                            y = y.toFloat().toDouble()
                            path.lineTo(x, y.toFloat())
                        }

                        drawPath(
                            path = path,
                            color = Color(255, 100, 120, 180),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
        } else {
            // Compact layout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Heart icon (pulsing subtly)
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = Color(255, 70, 90, 220),
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(32.dp)
                        .graphicsLayer {
                            scaleX = if (heartRate > 0) pulseScale else 1f
                            scaleY = if (heartRate > 0) pulseScale else 1f
                        }
                )

                // HR label
                Text(
                    text = "HR",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(60, 70, 100, 180)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Heart rate display
                AnimatedContent(
                    targetState = heartRate,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> -height } + fadeOut())
                    },
                    label = "HeartRateAnimation"
                ) { targetRate ->
                    Text(
                        text = "$targetRate",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-1).sp,
                            fontSize = 48.sp
                        ),
                        color = Color(20, 30, 50, 240)
                    )
                }

                // BPM label
                Text(
                    text = "bpm",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(80, 90, 110, 200)
                )

                // Additional metrics (in compact form)
                Row(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "HRV: 60ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(60, 70, 100, 180)
                    )
                    Text(
                        text = "SPO2: 98%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(60, 70, 100, 180)
                    )
                }
            }
        }
    }
}

/**
 * VisionPro-style waveform display card that expands to full width
 */
@Composable
fun WaveformCard(
    signalBuffer: MutableState<List<Float>>,
    isMeasuring: Boolean,
    modifier: Modifier = Modifier,
    expandedState: MutableState<ExpandedCardState>
) {
    val BUFFER_SIZE = 100

    // Create a safe copy of the signal data
    val safeSignalData = remember { mutableStateOf(listOf<Float>()) }

    // Update safe copy when signal buffer changes
    LaunchedEffect(signalBuffer.value) {
        val dataCopy = synchronized(signalBuffer.value) {
            signalBuffer.value.takeLast(BUFFER_SIZE).toList()
        }
        safeSignalData.value = dataCopy
    }

    // Check if this card is currently expanded
    val isExpanded = expandedState.value == ExpandedCardState.WAVEFORM

    // Waveform animation
    val waveformAlpha by animateFloatAsState(
        targetValue = if (isMeasuring) 1f else 0.3f,
        animationSpec = tween(500),
        label = "waveformAlpha"
    )

    // Animation for card expansion
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1.05f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "scaleAnimation"
    )

    // Animation for card opacity
    val cardAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0.9f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "alphaAnimation"
    )

    // Card width animation
    val cardWidthFraction by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0.5f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "cardWidthAnimation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth(cardWidthFraction)
            .graphicsLayer {
                scaleY = scale
                alpha = cardAlpha
            }
            .clickable {
                expandedState.value = if (isExpanded) ExpandedCardState.NONE else ExpandedCardState.WAVEFORM
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(240, 240, 250)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isExpanded) {
            // Expanded layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top section with title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timeline,
                            contentDescription = null,
                            tint = Color(100, 130, 255, 220),
                            modifier = Modifier.size(36.dp)
                        )

                        Text(
                            text = "PPG Waveform",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(60, 70, 100, 230)
                        )
                    }

                    // Status indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    if (isMeasuring) Color(100, 230, 100) else Color(230, 100, 100),
                                    shape = CircleShape
                                )
                        )

                        Text(
                            text = if (isMeasuring) "Active" else "Inactive",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isMeasuring) Color(40, 150, 40) else Color(150, 40, 40)
                        )
                    }
                }

                Divider(
                    color = Color(200, 210, 230, 120),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Main waveform display (larger in expanded mode)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(230, 235, 250, 180))
                        .padding(16.dp)
                ) {
                    // Waveform visualization
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

                        // Create gradient for the line (blue to purple)
                        val gradientBrush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(70, 150, 255, (waveformAlpha * 255).toInt()),
                                Color(120, 130, 255, (waveformAlpha * 255).toInt()),
                                Color(170, 110, 255, (waveformAlpha * 255).toInt()),
                                Color(200, 100, 255, (waveformAlpha * 255).toInt())
                            )
                        )

                        // Create the waveform path
                        val path = Path()
                        signalData.forEachIndexed { index, value ->
                            val x = step * index
                            val normalizedValue = (value - minValue) / range
                            val y = height - (normalizedValue * height * 0.8f + height * 0.1f)

                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        // Draw the path with the gradient
                        drawPath(
                            path = path,
                            brush = gradientBrush,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Add glow effect
                        drawPath(
                            path = path,
                            color = Color(100, 150, 255, (waveformAlpha * 60).toInt()),
                            style = Stroke(
                                width = 8.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Draw grid lines (optional)
                        val gridColor = Color(100, 120, 180, 20)
                        val gridStep = height / 4

                        // Horizontal grid lines
                        for (i in 0..4) {
                            val y = i * gridStep
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Vertical grid lines
                        val vGridStep = width / 5
                        for (i in 0..5) {
                            val x = i * vGridStep
                            drawLine(
                                color = gridColor,
                                start = Offset(x, 0f),
                                end = Offset(x, height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                }

                // Additional metrics in a row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        title = "Quality",
                        value = "High",
                        unit = "",
                        icon = Icons.Rounded.SignalCellularAlt
                    )

                    MetricItem(
                        title = "SNR",
                        value = "18",
                        unit = "dB",
                        icon = Icons.Rounded.GraphicEq
                    )

                    MetricItem(
                        title = "Frequency",
                        value = "1.2",
                        unit = "Hz",
                        icon = Icons.Rounded.Speed
                    )
                }

                // History section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(230, 235, 250, 180))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Signal History",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(100, 110, 130),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                    )

                    // Simple signal history visualization
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw a simplified version of the waveform history
                        val width = size.width
                        val height = size.height
                        val path = Path()

                        path.moveTo(0f, height * 0.5f)
                        for (i in 1..20) {
                            val x = width * i / 20
                            val y = height * (0.5f + sin(i * 0.4) * 0.2f *
                                    cos(i * 0.1) * 0.3f)
                            path.lineTo(x, y.toFloat())
                        }

                        drawPath(
                            path = path,
                            color = Color(100, 130, 250, 150),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
        } else {
            // Compact waveform display
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Waveform title
                Text(
                    text = "PPG",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(100, 110, 150, 220),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                )

                // Waveform visualization
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 36.dp, bottom = 8.dp)
                ) {
                    val signalData = safeSignalData.value
                    if (signalData.isEmpty()) return@Canvas

                    // Find min/max values for scaling
                    val minValue = signalData.minOrNull() ?: 0f
                    val maxValue = signalData.maxOrNull() ?: 0f
                    val range = (maxValue - minValue).coerceAtLeast(1f)

                    val width = size.width
                    val height = size.height
                    val step = width / (signalData.size - 1).coerceAtLeast(1)

                    // Create gradient for the line (blue to purple)
                    val gradientBrush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(70, 150, 255, (waveformAlpha * 255).toInt()),
                            Color(120, 130, 255, (waveformAlpha * 255).toInt()),
                            Color(170, 110, 255, (waveformAlpha * 255).toInt()),
                            Color(200, 100, 255, (waveformAlpha * 255).toInt())
                        )
                    )

                    // Create the waveform path
                    val path = Path()
                    signalData.forEachIndexed { index, value ->
                        val x = step * index
                        val normalizedValue = (value - minValue) / range
                        val y = height - (normalizedValue * height * 0.8f + height * 0.1f)

                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    // Draw the path with the gradient
                    drawPath(
                        path = path,
                        brush = gradientBrush,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Add glow effect
                    drawPath(
                        path = path,
                        color = Color(100, 150, 255, (waveformAlpha * 60).toInt()),
                        style = Stroke(
                            width = 8.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isMeasuring) Color(100, 230, 100) else Color(230, 100, 100),
                                shape = CircleShape
                            )
                    )

                    Text(
                        text = if (isMeasuring) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isMeasuring) Color(40, 150, 40) else Color(150, 40, 40)
                    )
                }
            }
        }
    }
}
/**
 * Content for the Waveform tab
 */
@Composable
fun WaveformContent(
    signalData: List<Float>,
    isMeasuring: Boolean,
    isExpanded: Boolean,
    waveformAlpha: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Main waveform display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isExpanded) 150.dp else 100.dp)
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(230, 235, 250, 180))
                .padding(12.dp)
        ) {
            // Waveform visualization
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (signalData.isEmpty()) return@Canvas

                // Find min/max values for scaling
                val minValue = signalData.minOrNull() ?: 0f
                val maxValue = signalData.maxOrNull() ?: 0f
                val range = (maxValue - minValue).coerceAtLeast(1f)

                val width = size.width
                val height = size.height
                val step = width / (signalData.size - 1).coerceAtLeast(1)

                // Create gradient for the line (blue to purple)
                val gradientBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(70, 150, 255, (waveformAlpha * 255).toInt()),
                        Color(120, 130, 255, (waveformAlpha * 255).toInt()),
                        Color(170, 110, 255, (waveformAlpha * 255).toInt()),
                        Color(200, 100, 255, (waveformAlpha * 255).toInt())
                    )
                )

                // Create the waveform path
                val path = Path()
                signalData.forEachIndexed { index, value ->
                    val x = step * index
                    val normalizedValue = (value - minValue) / range
                    val y = height - (normalizedValue * height * 0.8f + height * 0.1f)

                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                // Draw the path with the gradient
                drawPath(
                    path = path,
                    brush = gradientBrush,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Add glow effect
                drawPath(
                    path = path,
                    color = Color(100, 150, 255, (waveformAlpha * 60).toInt()),
                    style = Stroke(
                        width = 8.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw grid lines (optional)
                val gridColor = Color(100, 120, 180, 20)
                val gridStep = height / 4

                // Horizontal grid lines
                for (i in 0..4) {
                    val y = i * gridStep
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Vertical grid lines
                val vGridStep = width / 5
                for (i in 0..5) {
                    val x = i * vGridStep
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }

        // Signal metrics (shown when expanded)
        if (isExpanded) {
            Divider(
                color = Color(200, 210, 230, 120),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Signal metrics row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    title = "Quality",
                    value = "High",
                    unit = "",
                    icon = Icons.Rounded.SignalCellularAlt
                )

                MetricItem(
                    title = "SNR",
                    value = "18",
                    unit = "dB",
                    icon = Icons.Rounded.GraphicEq
                )

                MetricItem(
                    title = "Frequency",
                    value = "1.2",
                    unit = "Hz",
                    icon = Icons.Rounded.Speed
                )
            }

            // Signal history visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(230, 235, 250, 180))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Signal History",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(100, 110, 130),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                )

                // Simple signal history visualization
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw a simplified version of the waveform history
                    val width = size.width
                    val height = size.height
                    val path = Path()

                    path.moveTo(0f, height * 0.5f)
                    for (i in 1..20) {
                        val x = width * i / 20
                        val y = height * (0.5f + sin(i * 0.4) * 0.2f *
                                cos(i * 0.1) * 0.3f)
                        path.lineTo(x, y.toFloat())
                    }

                    drawPath(
                        path = path,
                        color = Color(100, 130, 250, 150),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun HeartRateContent(
    heartRate: Int,
    isExpanded: Boolean,
    pulseScale: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Heart rate display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            // Heart icon (pulsing subtly)
            Icon(
                imageVector = Icons.Rounded.Favorite,
                contentDescription = null,
                tint = Color(255, 70, 90, 220),
                modifier = Modifier
                    .size(38.dp)
                    .graphicsLayer {
                        scaleX = if (heartRate > 0) pulseScale else 1f
                        scaleY = if (heartRate > 0) pulseScale else 1f
                    }
            )

            // Heart rate value
            AnimatedContent(
                targetState = heartRate,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn()) togetherWith
                            (slideOutVertically { height -> -height } + fadeOut())
                },
                label = "heartRateAnimation"
            ) { targetRate ->
                Text(
                    text = "$targetRate",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-1).sp
                    ),
                    color = Color(20, 30, 50, 240)
                )
            }

            // BPM label
            Text(
                text = "bpm",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = Color(80, 90, 110, 200),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Additional heart metrics (shown when expanded)
        if (isExpanded) {
            Divider(
                color = Color(200, 210, 230, 120),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Metrics in two rows
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // First row of metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        title = "HRV",
                        value = "60",
                        unit = "ms",
                        icon = Icons.Rounded.Timeline
                    )

                    MetricItem(
                        title = "SPO2",
                        value = "98",
                        unit = "%",
                        icon = Icons.Rounded.Opacity
                    )

                    MetricItem(
                        title = "Stress",
                        value = "Low",
                        unit = "",
                        icon = Icons.Rounded.Spa
                    )
                }

                // Second row of metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        title = "Min",
                        value = "58",
                        unit = "bpm",
                        icon = null
                    )

                    MetricItem(
                        title = "Avg",
                        value = "72",
                        unit = "bpm",
                        icon = null
                    )

                    MetricItem(
                        title = "Max",
                        value = "124",
                        unit = "bpm",
                        icon = null
                    )
                }
            }

            // Heart rate trend chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(230, 235, 250, 180))
                    .padding(8.dp)
            ) {
                // Chart title
                Text(
                    text = "Heart Rate Trend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(100, 110, 130),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                )

                // Simulate a heart rate chart line
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = Path()
                    val width = size.width
                    val height = size.height

                    // Generate a simulated heart rate line
                    path.moveTo(0f, height * 0.6f)
                    for (i in 1..20) {
                        val x = width * i / 20
                        // Simulate some variance in the heart rate
                        var y = height * (0.6f + (sin(i * 0.8) * 0.15f))
                        y = y.toFloat().toDouble()
                        path.lineTo(x, y.toFloat())
                    }

                    drawPath(
                        path = path,
                        color = Color(255, 100, 120, 180),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CombinedHealthCard(
    heartRate: Int,
    signalBuffer: MutableState<List<Float>>,
    isMeasuring: Boolean,
    modifier: Modifier = Modifier
) {
    // State for expanded sections and selected tab
    val isExpanded = remember { mutableStateOf(false) }
    val selectedTab = remember { mutableStateOf(CardTab.HEART_RATE) }

    // Safe copy of signal data
    val safeSignalData = remember { mutableStateOf(listOf<Float>()) }

    // Update safe copy when signal buffer changes
    LaunchedEffect(signalBuffer.value) {
        val dataCopy = synchronized(signalBuffer.value) {
            signalBuffer.value.takeLast(100).toList()
        }
        safeSignalData.value = dataCopy
    }

    // Animation values
    val waveformAlpha by animateFloatAsState(
        targetValue = if (isMeasuring) 1f else 0.3f,
        animationSpec = tween(500),
        label = "waveformAlpha"
    )

    // Heart rate pulse animation
    val pulseAnimation = rememberInfiniteTransition(label = "pulseAnimation")
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Card expansion animation
    val cardHeight by animateDpAsState(
        targetValue = if (isExpanded.value) 380.dp else 200.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "cardHeightAnimation"
    )

    // Card elevation animation
    val cardElevation by animateDpAsState(
        targetValue = if (isExpanded.value) 8.dp else 4.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "cardElevationAnimation"
    )

    // Card background animation
    val backgroundColor by animateColorAsState(
        targetValue = if (isExpanded.value) Color(245, 245, 252) else Color(240, 240, 250),
        animationSpec = tween(300),
        label = "backgroundAnimation"
    )

    // Main Card
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable {
                isExpanded.value = !isExpanded.value
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = cardElevation
        ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top section - Always visible with tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title
                Text(
                    text = "Health Metrics",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(60, 70, 100, 230)
                )

                // Status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                if (isMeasuring) Color(100, 230, 100) else Color(230, 100, 100),
                                shape = CircleShape
                            )
                    )

                    Text(
                        text = if (isMeasuring) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isMeasuring) Color(40, 150, 40) else Color(150, 40, 40)
                    )
                }
            }

            // Tab selector
            TabRow(
                selectedTabIndex = selectedTab.value.ordinal,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp)),
                containerColor = Color(230, 235, 245, 180),
                contentColor = Color(70, 100, 180),
                indicator = { tabPositions ->
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab.value.ordinal])
                            .fillMaxWidth(0.5f)
                            .height(4.dp)
                            .padding(horizontal = 24.dp)
                            .background(
                                color = Color(70, 150, 255),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                },
                divider = { }
            ) {
                Tab(
                    selected = selectedTab.value == CardTab.HEART_RATE,
                    onClick = { selectedTab.value = CardTab.HEART_RATE },
                    text = {
                        Text(
                            text = "Heart Rate",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedTab.value == CardTab.HEART_RATE)
                                    FontWeight.SemiBold else FontWeight.Normal
                            )
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (selectedTab.value == CardTab.HEART_RATE)
                                Color(255, 70, 90) else Color(100, 110, 140)
                        )
                    }
                )

                Tab(
                    selected = selectedTab.value == CardTab.WAVEFORM,
                    onClick = { selectedTab.value = CardTab.WAVEFORM },
                    text = {
                        Text(
                            text = "Waveform",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedTab.value == CardTab.WAVEFORM)
                                    FontWeight.SemiBold else FontWeight.Normal
                            )
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Timeline,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (selectedTab.value == CardTab.WAVEFORM)
                                Color(70, 150, 255) else Color(100, 110, 140)
                        )
                    }
                )
            }

            // Content area - Changes based on selected tab
            AnimatedContent(
                targetState = selectedTab.value,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "tabContentAnimation"
            ) { targetTab ->
                when (targetTab) {
                    CardTab.HEART_RATE -> HeartRateContent(
                        heartRate = heartRate,
                        isExpanded = isExpanded.value,
                        pulseScale = pulseScale
                    )

                    CardTab.WAVEFORM -> WaveformContent(
                        signalData = safeSignalData.value,
                        isMeasuring = isMeasuring,
                        isExpanded = isExpanded.value,
                        waveformAlpha = waveformAlpha
                    )
                }
            }

            // Expand/collapse indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isExpanded.value)
                        Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded.value) "Collapse" else "Expand",
                    modifier = Modifier.size(24.dp),
                    tint = Color(100, 110, 140)
                )
            }
        }
    }
}
/**
 * Helper component for metric items
 */
@Composable
fun MetricItem(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Optional icon
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(100, 120, 180, 200),
                modifier = Modifier
                    .size(20.dp)
                    .padding(bottom = 4.dp)
            )
        }

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color(100, 110, 140, 180)
        )

        // Value and unit
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(60, 70, 100, 220)
            )

            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(80, 90, 120, 180),
                    modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                )
            }
        }
    }
}

/**
 * Enum to track which card is currently expanded
 */
enum class ExpandedCardState {
    NONE,
    HEART_RATE,
    WAVEFORM
}

/**
 * Updated usage in the parent component:
 */
@Composable
fun ResultsSection(
    heartRateData: MutableState<HeartRateData>, // Assuming this exists
    signalBuffer: MutableState<List<Float>>,
    isMeasuring: MutableState<Boolean>
) {
    // Track which card is expanded
    val expandedCardState = remember { mutableStateOf(ExpandedCardState.NONE) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HeartRateCard and WaveformCard will be positioned based on their fillMaxWidth(cardWidthFraction)
        HeartRateCard(
            heartRate = heartRateData.value.value,
            expandedState = expandedCardState
        )

        WaveformCard(
            signalBuffer = signalBuffer,
            isMeasuring = isMeasuring.value,
            expandedState = expandedCardState
        )
    }
}

/**
 * VisionPro-style signal quality indicator
 */
@Composable
fun SignalQualityCard(
    quality: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(240, 245, 250, 180))
            .border(
                width = 1.dp,
                color = Color(255, 255, 255, 180),
                shape = RoundedCornerShape(24.dp)
            )
            .blur(0.5.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Signal quality label
        Text(
            text = "Signal Quality: $quality",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = Color(20, 30, 40, 220)
        )

        // Mode selector
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mode",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color(60, 70, 80, 180)
            )

            Icon(
                imageVector = Icons.Default.CameraFront,
                contentDescription = "Select mode",
                tint = Color(60, 70, 80, 180),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * VisionPro-style measurement button
 */
@Composable
fun MeasurementButton(
    isMeasuring: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonColor = if (isMeasuring) {
        Color(230, 235, 240, 180)
    } else {
        Color(100, 180, 255, 200)
    }

    val textColor = if (isMeasuring) {
        Color(20, 30, 40, 220)
    } else {
        Color.White
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isMeasuring) Color(20, 30, 40, 40) else Color(255, 255, 255, 180)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = "rPPG",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            ),
            color = textColor
        )
    }
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
            title = { Text("Camera Permission Required") },
            text = { Text("This feature needs camera access to measure heart rate. Please enable camera permission in settings.") },
            confirmButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("OK")
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
                val imageReader = ImageReader.newInstance(320, 240, ImageFormat.YUV_420_888, 2).apply {
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
            val stdDev = window.map { (it - mean) * (it - mean) }.average().let { sqrt(it) }
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
 * Improved preview with better device sizing simulation
 */
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PreviewHeartRateApp() {
    // Simulate data for preview
    CompositionLocalProvider(
        LocalDensity provides LocalDensity.current
    ) {
        MySootheTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(245, 248, 252)
            ) {
                HeartRateApp()
            }
        }
    }
}