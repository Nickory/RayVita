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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * 重新设计的RPPG加载界面 - 简洁优雅
 *
 * @param message 显示的加载消息
 * @param progress 进度值：
 *                -1 = 不确定进度（20秒内从0%增长到95%，停在"即将完成"）
 *                0-1 = 确定进度（直接显示传入的进度值）
 * @param modifier 修饰符
 */
@Composable
fun RppgLoadingOverlay(
    message: String = "正在分析生理信号...",
    progress: Float = -1f, // -1 为不确定进度，0-1 为确定进度
    modifier: Modifier = Modifier
) {
    // 主题色定义
    val primaryColor = Color(0xFF6B5C4D)
    val primaryContainer = Color(0xFFF4DFCD)
    val onPrimaryContainer = Color(0xFF241A0E)
    val surface = Color(0xFFFFFBFF)
    val onSurface = Color(0xFF1D1B1A)
    val surfaceVariant = Color(0xFFE7E1DE)
    val outline = Color(0xFF7A7674)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        // 主内容卡片
        Card(
            modifier = Modifier
                .wrapContentSize()
                .padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = surface.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .widthIn(min = 280.dp, max = 320.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 主动画 - 心率监测器
                HeartRateMonitor(
                    primaryColor = primaryColor,
                    surfaceColor = surfaceVariant
                )

                // 状态文字
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.2.sp
                        ),
                        color = onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "请保持静止，确保检测准确性",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                // 进度指示
                ProgressSection(
                    progress = progress,
                    primaryColor = primaryColor,
                    primaryContainer = primaryContainer,
                    onPrimaryContainer = onPrimaryContainer,
                    outline = outline
                )

                // 实时状态指标
                if (progress > 0.2f || progress < 0) {
                    StatusIndicators(
                        primaryColor = primaryColor,
                        surfaceVariant = surfaceVariant,
                        onSurface = onSurface
                    )
                }
            }
        }
    }
}

/**
 * 心率监测器 - 主要动画组件
 */
@Composable
private fun HeartRateMonitor(
    primaryColor: Color,
    surfaceColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition()

    // 心跳脉冲 - 模拟真实心跳频率
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(860, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)),
            repeatMode = RepeatMode.Reverse
        )
    )

    // 扫描旋转 - 每5秒一圈，20秒内完成4圈
    val scanRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing)
        )
    )

    // 波纹效果 - 每4秒一个周期，20秒内5个周期
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))
        )
    )

    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))
        )
    )

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // 背景圆环
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = surfaceColor,
                radius = size.minDimension / 2.5f,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // 扫描环
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(0.9f)
        ) {
            rotate(scanRotation) {
                drawScanArc(primaryColor)
            }
        }

        // 波纹效果 - 每个波纹间隔调整为适配20秒
        repeat(3) { index ->
            val delay = index * 1200 // 1.2秒间隔，更适合长时间观看
            val waveScale by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 4000, // 4秒一个周期
                        delayMillis = delay,
                        easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                    )
                )
            )

            val waveAlpha by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 4000, // 4秒一个周期
                        delayMillis = delay,
                        easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                    )
                )
            )

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(waveScale)
            ) {
                drawCircle(
                    color = primaryColor.copy(alpha = waveAlpha * 0.3f),
                    radius = size.minDimension / 8,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        // 中心脉冲圆点
        Canvas(
            modifier = Modifier
                .size(32.dp)
                .scale(heartScale)
        ) {
            // 渐变填充
            val gradient = Brush.radialGradient(
                colors = listOf(
                    primaryColor,
                    primaryColor.copy(alpha = 0.8f)
                ),
                radius = size.minDimension / 2
            )

            drawCircle(
                brush = gradient,
                radius = size.minDimension / 2
            )

            // 边框
            drawCircle(
                color = primaryColor,
                radius = size.minDimension / 2,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // ECG波形效果
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawECGWave(
                color = primaryColor.copy(alpha = 0.4f),
                offset = rippleScale
            )
        }
    }
}

/**
 * 进度区域
 */
@Composable
private fun ProgressSection(
    progress: Float,
    primaryColor: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    outline: Color
) {
    val infiniteTransition = rememberInfiniteTransition()

    val animatedProgress by if (progress < 0) {
        // 不确定进度：20秒内从0%到95%，模拟真实的分析进度
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0.95f, // 最终停在95%，表示"即将完成"
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 40000, // 20秒完成
                    easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f) // 先快后慢的自然曲线
                ),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        // 确定进度：如果是从外部传入的实际进度值
        remember(progress) { mutableStateOf(progress) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 进度条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(outline.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        // 统一使用简洁的渐变，不再区分确定/不确定模式
                        Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor,
                                primaryColor.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .clip(RoundedCornerShape(3.dp))
            )
        }

        // 进度信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (progress >= 0) "${(animatedProgress * 100).toInt()}%" else "分析中",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = primaryColor
            )

            Surface(
                color = primaryContainer.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = getProcessingStage(animatedProgress),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = onPrimaryContainer
                )
            }
        }
    }
}

/**
 * 状态指标
 */
@Composable
private fun StatusIndicators(
    primaryColor: Color,
    surfaceVariant: Color,
    onSurface: Color
) {
    val infiniteTransition = rememberInfiniteTransition()

    // 心率变化 - 模拟真实心率波动，3秒一个周期
    val heartRate by infiniteTransition.animateFloat(
        initialValue = 68f,
        targetValue = 75f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // 信号质量变化 - 4秒一个周期，模拟检测质量波动
    val signalQuality by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        color = surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 心率指标
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${heartRate.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFE53E3E)
                )
                Text(
                    text = "BPM",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurface.copy(alpha = 0.6f)
                )
            }

            // 分隔线
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(onSurface.copy(alpha = 0.2f))
            )

            // 信号质量
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${(signalQuality * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = primaryColor
                )
                Text(
                    text = "质量",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * 绘制扫描弧线
 */
private fun DrawScope.drawScanArc(color: Color) {
    val radius = size.minDimension / 2.5f

    drawArc(
        color = color.copy(alpha = 0.8f),
        startAngle = -90f,
        sweepAngle = 90f,
        useCenter = false,
        style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round
        ),
        topLeft = Offset(
            (size.width - radius * 2) / 2,
            (size.height - radius * 2) / 2
        ),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
    )
}

/**
 * 绘制ECG波形
 */
private fun DrawScope.drawECGWave(
    color: Color,
    offset: Float
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = size.minDimension / 3.5f

    val path = Path()
    var isFirst = true

    for (i in 0..360 step 8) {
        val angle = Math.toRadians(i.toDouble() + offset * 50)
        val amplitude = when {
            i % 60 in 20..25 -> 8f * sin((i - 20) * Math.PI / 5).toFloat()
            i % 60 in 30..35 -> 12f * sin((i - 30) * Math.PI / 5).toFloat()
            else -> 0f
        }

        val x = centerX + (radius + amplitude) * cos(angle).toFloat()
        val y = centerY + (radius + amplitude) * sin(angle).toFloat()

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
private fun getProcessingStage(progress: Float): String {
    return when {
        progress < 0.05f -> "初始化"
        progress < 0.20f -> "信号采集"
        progress < 0.40f -> "预处理"
        progress < 0.60f -> "噪声过滤"
        progress < 0.80f -> "特征提取"
        progress < 0.95f -> "数据分析"
        else -> "即将完成"
    }
}