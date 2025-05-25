package com.codelab.basiclayouts.ui.screen.physnet

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelab.basiclayouts.data.physnet.VideoRecorder
import com.codelab.basiclayouts.viewModel.physnet.AccelerometerViewModel
import com.codelab.basiclayouts.viewModel.physnet.AccelerometerViewModelFactory

/**
 * Enhanced camera preview with accelerometer motion detection
 */
@Composable
fun RppgCameraPreview(
    videoRecorder: VideoRecorder,
    lifecycleOwner: LifecycleOwner,
    onFaceAlignmentChanged: (Boolean) -> Unit,
    accelerometerViewModel: AccelerometerViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    // 使用传入的ViewModel或创建新的
    val actualAccelerometerViewModel: AccelerometerViewModel = accelerometerViewModel ?: viewModel(
        factory = AccelerometerViewModelFactory(context)
    )
    val motionState by actualAccelerometerViewModel.motionState.collectAsState()

    // 管理传感器生命周期
    DisposableEffect(lifecycleOwner) {
        actualAccelerometerViewModel.startDetection()
        onDispose {
            actualAccelerometerViewModel.stopDetection()
        }
    }

    Box(modifier = modifier) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { preview ->
                    previewView = preview
                    setupCamera(preview, lifecycleOwner, videoRecorder)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Face alignment overlay
        FaceAlignmentOverlay(
            onAlignmentChanged = onFaceAlignmentChanged,
            modifier = Modifier.fillMaxSize()
        )

        // Motion status indicator (top-left)
        MotionStatusIndicator(
            isStationary = motionState.isStationary,
            motionStatus = motionState.motionStatus,
            motionLevel = motionState.motionLevel,
            isActive = motionState.isDetectionActive,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        // Motion warning overlay (bottom-center)
        MotionWarningOverlay(
            isStationary = motionState.isStationary,
            motionStatus = motionState.motionStatus,
            isActive = motionState.isDetectionActive,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

/**
 * Motion Status Indicator
 */
@Composable
private fun MotionStatusIndicator(
    isStationary: Boolean,
    motionStatus: String,
    motionLevel: Float,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        Surface(
            color = if (isStationary) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status icon
                Icon(
                    imageVector = if (isStationary) {
                        Icons.Default.Check
                    } else {
                        Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = if (isStationary) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    },
                    modifier = Modifier.size(16.dp)
                )

                // Status text
                Text(
                    text = motionStatus,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isStationary) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )

                // Motion level dot
                if (!isStationary) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    motionLevel > 2.0f -> MaterialTheme.colorScheme.error
                                    motionLevel > 1.0f -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.secondary
                                }
                            )
                    )
                }
            }
        }
    }
}

/**
 * Motion Warning Overlay
 */
@Composable
private fun MotionWarningOverlay(
    isStationary: Boolean,
    motionStatus: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isActive && !isStationary,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp)
                )

                Column {
                    Text(
                        text = "Please keep as still as possible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Current status: $motionStatus",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Face alignment assistance frame
 */
@Composable
private fun FaceAlignmentOverlay(
    onAlignmentChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Animation effects
    val infiniteTransition = rememberInfiniteTransition()
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedStrokeWidth by infiniteTransition.animateFloat(
        initialValue = 2f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Scanning line animation
    val animatedY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2

        // Target frame size (128x128 mapped to screen)
        val targetSize = minOf(canvasWidth, canvasHeight) * 0.5f
        val targetRect = Rect(
            offset = Offset(centerX - targetSize / 2, centerY - targetSize / 2),
            size = Size(targetSize, targetSize)
        )

        // Draw semi-transparent background mask
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            size = size
        )

        // Create circular path for clipping
        val circlePath = Path().apply {
            addOval(
                oval = Rect(
                    center = Offset(centerX, centerY),
                    radius = targetSize / 2
                )
            )
        }

        // Clip out center circular area
        clipPath(circlePath, clipOp = ClipOp.Difference) {
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = size
            )
        }

        // Draw alignment frame
        drawAlignmentFrame(
            center = Offset(centerX, centerY),
            radius = targetSize / 2,
            color = primaryColor,
            strokeWidth = animatedStrokeWidth.dp.toPx(),
            alpha = animatedAlpha
        )

        // Draw corner markers
        drawCornerMarkers(
            rect = targetRect,
            color = primaryColor,
            strokeWidth = 4.dp.toPx()
        )

        // Draw scanning line
        drawScanningLine(
            rect = targetRect,
            color = primaryColor.copy(alpha = 0.5f),
            animatedY = lerp(targetRect.top, targetRect.bottom, animatedY)
        )
    }

    // Simulate face detection (should use real face detection in actual app)
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        onAlignmentChanged(true)
    }
}

/**
 * Draw alignment frame
 */
private fun DrawScope.drawAlignmentFrame(
    center: Offset,
    radius: Float,
    color: Color,
    strokeWidth: Float,
    alpha: Float
) {
    // Main circular frame
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = radius,
        center = center,
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(20f, 10f),
                phase = 0f
            )
        )
    )

    // Inner circle
    drawCircle(
        color = color.copy(alpha = alpha * 0.5f),
        radius = radius * 0.9f,
        center = center,
        style = Stroke(width = strokeWidth * 0.5f)
    )
}

/**
 * Draw corner markers
 */
private fun DrawScope.drawCornerMarkers(
    rect: Rect,
    color: Color,
    strokeWidth: Float
) {
    val markerLength = rect.width * 0.15f
    val corners = listOf(
        rect.topLeft,
        rect.topRight,
        rect.bottomLeft,
        rect.bottomRight
    )

    corners.forEach { corner ->
        // Horizontal line
        val hStart = when (corner) {
            rect.topLeft, rect.bottomLeft -> corner
            else -> corner.copy(x = corner.x - markerLength)
        }
        val hEnd = hStart.copy(x = hStart.x + markerLength)

        // Vertical line
        val vStart = when (corner) {
            rect.topLeft, rect.topRight -> corner
            else -> corner.copy(y = corner.y - markerLength)
        }
        val vEnd = vStart.copy(y = vStart.y + markerLength)

        drawLine(
            color = color,
            start = hStart,
            end = hEnd,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = vStart,
            end = vEnd,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Draw scanning line animation
 */
private fun DrawScope.drawScanningLine(
    rect: Rect,
    color: Color,
    animatedY: Float
) {
    // Draw scanning line
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            color,
            Color.Transparent
        ),
        startY = animatedY - 30f,
        endY = animatedY + 30f
    )

    drawLine(
        brush = gradient,
        start = Offset(rect.left, animatedY),
        end = Offset(rect.right, animatedY),
        strokeWidth = 2.dp.toPx()
    )
}

/**
 * Setup camera
 */
private fun setupCamera(
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    videoRecorder: VideoRecorder
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)

    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // Unbind all use cases
            cameraProvider.unbindAll()

            // Bind camera
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview
            )

            // Bind VideoRecorder
            videoRecorder.bindCamera(cameraProvider, lifecycleOwner, cameraSelector)

        } catch (exc: Exception) {
            Log.e("RppgCameraPreview", "Camera binding failed", exc)
        }
    }, ContextCompat.getMainExecutor(previewView.context))
}