package com.codelab.basiclayouts.ui.screen.physnet

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * 优雅的RPPG加载界面 - 现代化设计
 */
@Composable
fun RppgLoadingOverlay(
    message: String = "正在分析生理信号...",
    progress: Float = -1f, // -1 为不确定进度，0-1 为确定进度
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A).copy(alpha = 0.90f),
                        Color(0xFF000000).copy(alpha = 0.96f)
                    ),
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 360.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(40.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // 主要动画 - 心率脉冲分析器
                HeartRateAnalyzer()

                // 状态信息区域
                StatusSection(message = message)

                // 进度指示器
                ProgressIndicator(progress = progress)

                // 实时数据指标
                if (progress > 0.3f || progress < 0) {
                    LiveMetrics()
                }
            }
        }
    }
}

/**
 * 心率分析器动画 - 主要视觉焦点
 */
@Composable
private fun HeartRateAnalyzer() {
    val infiniteTransition = rememberInfiniteTransition()

    // 心跳脉冲动画
    val heartBeat by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)),
            repeatMode = RepeatMode.Reverse
        )
    )

    // 外围扫描环
    val scanRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        )
    )

    // 数据波纹效果
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f))
        )
    )

    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f))
        )
    )

    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        // 背景波纹效果
        repeat(2) { index ->
            val delay = index * 1500
            val waveScale by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 2.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 3000,
                        delayMillis = delay,
                        easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
                    )
                )
            )

            val waveAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 3000,
                        delayMillis = delay,
                        easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
                    )
                )
            )

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(waveScale)
            ) {
                drawCircle(
                    color = Color(0xFF4CAF50).copy(alpha = waveAlpha * 0.15f),
                    radius = size.minDimension / 6,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // 扫描环系统
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(scanRotation)
        ) {
            drawScanningRings(
                primaryColor = Color(0xFF4CAF50),
                secondaryColor = Color(0xFF66BB6A)
            )
        }

        // 中心心率图标
        Canvas(
            modifier = Modifier
                .size(60.dp)
                .scale(heartBeat)
        ) {
            drawHeartIcon(
                color = Color(0xFF4CAF50),
                size = size
            )
        }

        // ECG波形叠加
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawECGWave(
                color = Color(0xFF81C784).copy(alpha = 0.6f),
                progress = rippleScale
            )
        }
    }
}

/**
 * 状态信息区域
 */
@Composable
private fun StatusSection(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Text(
            text = "请保持静止，确保最佳检测效果",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 进度指示器
 */
@Composable
private fun ProgressIndicator(progress: Float) {
    val infiniteTransition = rememberInfiniteTransition()

    val animatedProgress by if (progress < 0) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        remember(progress) { mutableStateOf(progress) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 现代进度条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFF66BB6A),
                                Color(0xFF81C784)
                            )
                        )
                    )
                    .clip(RoundedCornerShape(2.dp))
            )
        }

        // 进度百分比和阶段信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (progress >= 0) {
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF4CAF50)
                )
            } else {
                Text(
                    text = "分析中...",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            Text(
                text = getCurrentStage(animatedProgress),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 实时指标显示
 */
@Composable
private fun LiveMetrics() {
    val infiniteTransition = rememberInfiniteTransition()

    val heartRate by infiniteTransition.animateFloat(
        initialValue = 72f,
        targetValue = 78f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val confidence by infiniteTransition.animateFloat(
        initialValue = 0.87f,
        targetValue = 0.94f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricDisplay(
                label = "心率",
                value = "${heartRate.toInt()}",
                unit = "BPM",
                color = Color(0xFFE53E3E)
            )

            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )

            MetricDisplay(
                label = "信号质量",
                value = "${(confidence * 100).toInt()}",
                unit = "%",
                color = Color(0xFF4CAF50)
            )
        }
    }
}

/**
 * 单个指标显示组件
 */
@Composable
private fun MetricDisplay(
    label: String,
    value: String,
    unit: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * 绘制扫描环系统
 */
private fun DrawScope.drawScanningRings(
    primaryColor: Color,
    secondaryColor: Color
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = size.minDimension / 3

    // 主扫描环
    drawArc(
        color = primaryColor.copy(alpha = 0.6f),
        startAngle = -90f,
        sweepAngle = 60f,
        useCenter = false,
        style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round
        ),
        topLeft = Offset(centerX - radius, centerY - radius),
        size = Size(radius * 2, radius * 2)
    )

    // 副扫描环
    drawArc(
        color = secondaryColor.copy(alpha = 0.4f),
        startAngle = 90f,
        sweepAngle = 45f,
        useCenter = false,
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round
        ),
        topLeft = Offset(centerX - radius * 0.8f, centerY - radius * 0.8f),
        size = Size(radius * 1.6f, radius * 1.6f)
    )
}

/**
 * 绘制心脏图标
 */
private fun DrawScope.drawHeartIcon(
    color: Color,
    size: Size
) {
    val heartPath = Path().apply {
        val width = size.width * 0.8f
        val height = size.height * 0.8f
        val offsetX = (size.width - width) / 2
        val offsetY = (size.height - height) / 2

        // 创建更精确的心形路径
        moveTo(offsetX + width / 2, offsetY + height * 0.25f)

        // 左半部分
        cubicTo(
            offsetX + width / 2, offsetY,
            offsetX, offsetY,
            offsetX, offsetY + height * 0.375f
        )
        cubicTo(
            offsetX, offsetY + height * 0.75f,
            offsetX + width / 2, offsetY + height,
            offsetX + width / 2, offsetY + height
        )

        // 右半部分
        cubicTo(
            offsetX + width / 2, offsetY + height,
            offsetX + width, offsetY + height * 0.75f,
            offsetX + width, offsetY + height * 0.375f
        )
        cubicTo(
            offsetX + width, offsetY,
            offsetX + width / 2, offsetY,
            offsetX + width / 2, offsetY + height * 0.25f
        )
        close()
    }

    // 心脏渐变填充
    drawPath(
        path = heartPath,
        brush = Brush.linearGradient(
            colors = listOf(
                color,
                color.copy(alpha = 0.8f)
            ),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )
    )

    // 心脏边框
    drawPath(
        path = heartPath,
        color = color.copy(alpha = 0.9f),
        style = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * 绘制ECG波形
 */
private fun DrawScope.drawECGWave(
    color: Color,
    progress: Float
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = size.minDimension / 3

    val path = Path()
    var isFirst = true

    for (i in 0..360 step 5) {
        val angle = Math.toRadians(i.toDouble())
        val baseRadius = radius + when (i % 60) {
            in 10..15 -> 8f * sin((i - 10) * Math.PI / 5).toFloat()
            in 20..25 -> -12f * sin((i - 20) * Math.PI / 5).toFloat()
            in 30..35 -> 15f * sin((i - 30) * Math.PI / 5).toFloat()
            else -> 0f
        }

        val x = centerX + baseRadius * cos(angle).toFloat()
        val y = centerY + baseRadius * sin(angle).toFloat()

        if (isFirst) {
            path.moveTo(x, y)
            isFirst = false
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    )
}

/**
 * 获取当前处理阶段
 */
private fun getCurrentStage(progress: Float): String {
    return when {
        progress < 0.2f -> "信号采集"
        progress < 0.4f -> "噪声过滤"
        progress < 0.6f -> "特征提取"
        progress < 0.8f -> "数据分析"
        progress < 1.0f -> "结果验证"
        else -> "分析完成"
    }
}