package com.codelab.basiclayouts.ui.screen.physnet

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * rPPG波形图组件 - Material 3风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RppgWaveform(
    signal: FloatArray,
    modifier: Modifier = Modifier,
    showGrid: Boolean = true,
    showLabels: Boolean = true,
    animateDrawing: Boolean = true
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // 动画进度
    val animationProgress = if (animateDrawing) {
        val transition = rememberInfiniteTransition()
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        progress
    } else {
        1f
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "rPPG Signal Waveform",
                    style = MaterialTheme.typography.titleMedium
                )

                // 信号质量指示器
                SignalQualityIndicator(signal = signal)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 波形图
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                WaveformCanvas(
                    signal = signal,
                    animationProgress = animationProgress,
                    showGrid = showGrid,
                    showLabels = showLabels,
                    primaryColor = primaryColor,
                    gridColor = onSurfaceVariant.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 统计信息
            if (showLabels) {
                Spacer(modifier = Modifier.height(8.dp))
                WaveformStats(signal = signal)
            }
        }
    }
}

/**
 * 波形绘制画布
 */
@Composable
private fun WaveformCanvas(
    signal: FloatArray,
    animationProgress: Float,
    showGrid: Boolean,
    showLabels: Boolean,
    primaryColor: Color,
    gridColor: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    Canvas(modifier = modifier.padding(8.dp)) {
        if (signal.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 20f

        // 绘制网格
        if (showGrid) {
            drawGrid(
                width = width,
                height = height,
                gridColor = gridColor,
                padding = padding
            )
        }

        // 计算数据范围
        val minValue = signal.minOrNull() ?: 0f
        val maxValue = signal.maxOrNull() ?: 1f
        val range = maxValue - minValue

        if (range == 0f) return@Canvas

        // 绘制波形
        val path = Path()
        val pointsToShow = (signal.size * animationProgress).toInt().coerceAtLeast(1)
        val stepX = (width - 2 * padding) / (signal.size - 1).coerceAtLeast(1)

        signal.take(pointsToShow).forEachIndexed { index, value ->
            val normalized = (value - minValue) / range
            val x = padding + index * stepX
            val y = height - padding - (normalized * (height - 2 * padding))

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                // 使用贝塞尔曲线平滑
                val prevX = padding + (index - 1) * stepX
                val prevY = height - padding -
                        ((signal[index - 1] - minValue) / range * (height - 2 * padding))

                val controlX1 = prevX + (x - prevX) / 3
                val controlY1 = prevY
                val controlX2 = prevX + 2 * (x - prevX) / 3
                val controlY2 = y

                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            }
        }

        // 绘制渐变填充
        val fillPath = Path().apply {
            addPath(path)
            if (pointsToShow > 0) {
                val lastX = padding + (pointsToShow - 1) * stepX
                lineTo(lastX, height - padding)
                lineTo(padding, height - padding)
                close()
            }
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.3f),
                    primaryColor.copy(alpha = 0.05f)
                ),
                startY = 0f,
                endY = height
            )
        )

        // 绘制波形线
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(
                width = with(density) { 2.dp.toPx() },
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 绘制当前点
        if (pointsToShow > 0 && animationProgress < 1f) {
            val currentIndex = (pointsToShow - 1).coerceIn(signal.indices)
            val currentValue = signal[currentIndex]
            val normalized = (currentValue - minValue) / range
            val x = padding + currentIndex * stepX
            val y = height - padding - (normalized * (height - 2 * padding))

            // 外圈动画
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = with(density) { 8.dp.toPx() },
                center = Offset(x, y)
            )

            // 内圈
            drawCircle(
                color = primaryColor,
                radius = with(density) { 4.dp.toPx() },
                center = Offset(x, y)
            )
        }

        // 绘制标签
        if (showLabels) {
            drawLabels(
                minValue = minValue,
                maxValue = maxValue,
                width = width,
                height = height,
                padding = padding,
                textMeasurer = textMeasurer,
                textColor = gridColor
            )
        }
    }
}

/**
 * 绘制网格
 */
private fun DrawScope.drawGrid(
    width: Float,
    height: Float,
    gridColor: Color,
    padding: Float
) {
    val horizontalLines = 5
    val verticalLines = 10

    // 水平线
    for (i in 0..horizontalLines) {
        val y = padding + (height - 2 * padding) * i / horizontalLines
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = if (i % 2 == 0) null else PathEffect.dashPathEffect(
                floatArrayOf(5f, 5f)
            )
        )
    }

    // 垂直线
    for (i in 0..verticalLines) {
        val x = padding + (width - 2 * padding) * i / verticalLines
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, height - padding),
            strokeWidth = 1.dp.toPx(),
            pathEffect = if (i % 2 == 0) null else PathEffect.dashPathEffect(
                floatArrayOf(5f, 5f)
            )
        )
    }
}

/**
 * 绘制标签
 */
private fun DrawScope.drawLabels(
    minValue: Float,
    maxValue: Float,
    width: Float,
    height: Float,
    padding: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    textColor: Color
) {
    val textStyle = TextStyle(
        fontSize = 10.sp,
        color = textColor
    )

    // Y轴标签
    val yLabels = listOf(maxValue, (maxValue + minValue) / 2, minValue)
    yLabels.forEachIndexed { index, value ->
        val y = padding + (height - 2 * padding) * index / (yLabels.size - 1)
        val text = "%.2f".format(value)
        val textResult = textMeasurer.measure(text, textStyle)

        drawText(
            textMeasurer = textMeasurer,
            text = text,
            topLeft = Offset(0f, y - textResult.size.height / 2),
            style = textStyle
        )
    }
}
/**
 * Signal Quality Indicator
 */
@Composable
private fun SignalQualityIndicator(signal: FloatArray) {
    val quality = calculateSignalQuality(signal)
    val (text, color) = when {
        quality > 0.8f -> "Excellent" to MaterialTheme.colorScheme.primary
        quality > 0.6f -> "Good" to MaterialTheme.colorScheme.secondary
        quality > 0.4f -> "Fair" to MaterialTheme.colorScheme.tertiary
        else -> "Poor" to MaterialTheme.colorScheme.error
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

/**
 * Waveform Statistics
 */
@Composable
private fun WaveformStats(signal: FloatArray) {
    if (signal.isEmpty()) return

    val stats = remember(signal) {
        mapOf(
            "Sample Points" to signal.size.toString(),
            "Maximum" to "%.3f".format(signal.maxOrNull() ?: 0f),
            "Minimum" to "%.3f".format(signal.minOrNull() ?: 0f),
            "Average" to "%.3f".format(signal.average())
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        stats.forEach { (label, value) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * 计算信号质量
 */
private fun calculateSignalQuality(signal: FloatArray): Float {
    if (signal.isEmpty()) return 0f

    val variance = signal.map { it - signal.average() }
        .map { it * it }
        .average()
        .toFloat()

    val snr = if (variance > 0) {
        (signal.maxOrNull() ?: 0f) / kotlin.math.sqrt(variance)
    } else 0f

    return (snr / 10f).coerceIn(0f, 1f)
}