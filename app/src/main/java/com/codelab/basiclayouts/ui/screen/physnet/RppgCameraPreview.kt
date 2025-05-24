package com.codelab.basiclayouts.ui.screen.physnet

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.codelab.basiclayouts.data.physnet.VideoRecorder

/**
 * 相机预览组件，包含人脸对齐辅助框
 */
@Composable
fun RppgCameraPreview(
    videoRecorder: VideoRecorder,
    lifecycleOwner: LifecycleOwner,
    onFaceAlignmentChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    Box(modifier = modifier) {
        // 相机预览
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { preview ->
                    previewView = preview
                    setupCamera(preview, lifecycleOwner, videoRecorder)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 对齐辅助框覆盖层
        FaceAlignmentOverlay(
            onAlignmentChanged = onFaceAlignmentChanged,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * 人脸对齐辅助框
 */
@Composable
private fun FaceAlignmentOverlay(
    onAlignmentChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    // 动画效果
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

    // 扫描线动画
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

        // 目标框大小 (128x128 映射到屏幕)
        val targetSize = minOf(canvasWidth, canvasHeight) * 0.5f
        val targetRect = Rect(
            offset = Offset(centerX - targetSize / 2, centerY - targetSize / 2),
            size = Size(targetSize, targetSize)
        )

        // 绘制半透明背景遮罩
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            size = size
        )

        // 创建圆形路径用于裁剪
        val circlePath = Path().apply {
            addOval(
                oval = Rect(
                    center = Offset(centerX, centerY),
                    radius = targetSize / 2
                )
            )
        }

        // 裁剪出中心圆形区域
        clipPath(circlePath, clipOp = ClipOp.Difference) {
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = size
            )
        }

        // 绘制对齐框
        drawAlignmentFrame(
            center = Offset(centerX, centerY),
            radius = targetSize / 2,
            color = primaryColor,
            strokeWidth = animatedStrokeWidth.dp.toPx(),
            alpha = animatedAlpha
        )

        // 绘制角标
        drawCornerMarkers(
            rect = targetRect,
            color = primaryColor,
            strokeWidth = 4.dp.toPx()
        )

        // 绘制扫描线
        drawScanningLine(
            rect = targetRect,
            color = primaryColor.copy(alpha = 0.5f),
            animatedY = lerp(targetRect.top, targetRect.bottom, animatedY)
        )
    }

    // 模拟人脸检测（实际应用中应使用真实的人脸检测）
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        onAlignmentChanged(true)
    }
}

/**
 * 绘制对齐框
 */
private fun DrawScope.drawAlignmentFrame(
    center: Offset,
    radius: Float,
    color: Color,
    strokeWidth: Float,
    alpha: Float
) {
    // 主圆框
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

    // 内圆
    drawCircle(
        color = color.copy(alpha = alpha * 0.5f),
        radius = radius * 0.9f,
        center = center,
        style = Stroke(width = strokeWidth * 0.5f)
    )
}

/**
 * 绘制角标
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
        // 水平线
        val hStart = when (corner) {
            rect.topLeft, rect.bottomLeft -> corner
            else -> corner.copy(x = corner.x - markerLength)
        }
        val hEnd = hStart.copy(x = hStart.x + markerLength)

        // 垂直线
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
 * 绘制扫描线动画
 */
private fun DrawScope.drawScanningLine(
    rect: Rect,
    color: Color,
    animatedY: Float
) {
    // 绘制扫描线
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
 * 设置相机
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

            // 解绑所有用例
            cameraProvider.unbindAll()

            // 绑定相机
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview
            )

            // 绑定VideoRecorder
            videoRecorder.bindCamera(cameraProvider, lifecycleOwner, cameraSelector)

        } catch (exc: Exception) {
            Log.e("RppgCameraPreview", "相机绑定失败", exc)
        }
    }, ContextCompat.getMainExecutor(previewView.context))
}