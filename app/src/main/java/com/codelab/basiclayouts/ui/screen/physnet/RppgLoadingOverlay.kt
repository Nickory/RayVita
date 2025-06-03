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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelab.basiclayouts.R
import kotlin.math.cos
import kotlin.math.sin

/**
 * RPPG加载界面 - 简洁优雅
 *
 * @param message 显示的加载消息
 * @param progress 进度值：
 *                -1 = 不确定进度（20秒内从0%增长到95%，停在"即将完成"）
 *                0-1 = 确定进度（直接显示传入的进度值）
 * @param modifier 修饰符
 */
@Composable
fun RppgLoadingOverlay(
    message: String = stringResource(R.string.analyzing_physiological_signals),
    progress: Float = -1f, // -1 为不确定进度，0-1 为确定进度
    modifier: Modifier = Modifier
) {
    // 主题色定义
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline

    // 创建深度渐变背景
    val backgroundGradient = Brush.radialGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.6f),
            Color.Black.copy(alpha = 0.8f),
            Color.Black.copy(alpha = 0.85f)
        ),
        radius = 800f
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        // 主内容卡片
        Card(
            modifier = Modifier
                .wrapContentSize()
                .padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = surface.copy(alpha = 0.97f)
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(36.dp)
                    .widthIn(min = 280.dp, max = 320.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
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
                        text = stringResource(R.string.keep_still_for_accuracy),
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
        targetValue = 1.18f,
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
        initialValue = 0.9f,
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

    // 光晕效果动画
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        // 外层光晕环
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = primaryColor.copy(alpha = glowIntensity * 0.15f),
                radius = size.minDimension / 2.2f,
                style = Stroke(width = 8.dp.toPx())
            )
        }

        // 背景圆环 - 增强渐变效果
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gradient = Brush.sweepGradient(
                colors = listOf(
                    surfaceColor.copy(alpha = 0.4f),
                    surfaceColor.copy(alpha = 0.8f),
                    surfaceColor.copy(alpha = 0.4f),
                    surfaceColor.copy(alpha = 0.8f)
                )
            )
            drawCircle(
                brush = gradient,
                radius = size.minDimension / 2.5f,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // 扫描环 - 精美渐变效果
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(0.9f)
        ) {
            rotate(scanRotation) {
                drawEnhancedScanArc(primaryColor, glowIntensity)
            }
        }

        // 波纹效果 - 每个波纹间隔调整为适配20秒
        repeat(4) { index ->
            val delay = index * 1000 // 1秒间隔，更密集的波纹
            val waveScale by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 2.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 4000, // 4秒一个周期
                        delayMillis = delay,
                        easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                    )
                )
            )

            val waveAlpha by infiniteTransition.animateFloat(
                initialValue = 0.8f,
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
                // 渐变波纹
                val waveGradient = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = waveAlpha * 0.4f),
                        primaryColor.copy(alpha = waveAlpha * 0.2f),
                        Color.Transparent
                    ),
                    radius = size.minDimension / 6
                )

                drawCircle(
                    brush = waveGradient,
                    radius = size.minDimension / 8,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // 中心脉冲圆点 - 增强效果
        Canvas(
            modifier = Modifier
                .size(36.dp)
                .scale(heartScale)
        ) {
            // 外层光晕
            val outerGradient = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.6f),
                    primaryColor.copy(alpha = 0.3f),
                    Color.Transparent
                ),
                radius = size.minDimension / 1.5f
            )

            drawCircle(
                brush = outerGradient,
                radius = size.minDimension / 1.5f
            )

            // 主渐变填充
            val gradient = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.9f),
                    primaryColor.copy(alpha = 0.95f),
                    primaryColor.copy(alpha = 0.8f)
                ),
                radius = size.minDimension / 2
            )

            drawCircle(
                brush = gradient,
                radius = size.minDimension / 2
            )

            // 内层高光
            val highlightGradient = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.6f),
                    Color.Transparent
                ),
                radius = size.minDimension / 4,
                center = Offset(size.width * 0.3f, size.height * 0.3f)
            )

            drawCircle(
                brush = highlightGradient,
                radius = size.minDimension / 4,
                center = Offset(size.width * 0.3f, size.height * 0.3f)
            )

            // 边框
            drawCircle(
                color = primaryColor.copy(alpha = 0.9f),
                radius = size.minDimension / 2,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // ECG波形效果 - 增强版
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawEnhancedECGWave(
                color = primaryColor.copy(alpha = 0.5f),
                offset = rippleScale,
                glowIntensity = glowIntensity
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

    // 进度条光效动画
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 进度条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(outline.copy(alpha = 0.15f))
        ) {
            // 进度条背景渐变
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.8f),
                                primaryColor,
                                primaryColor.copy(alpha = 0.9f),
                                primaryColor
                            )
                        )
                    )
                    .clip(RoundedCornerShape(4.dp))
            )

            // 光效动画
            if (animatedProgress > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                startX = shimmerOffset * 300f,
                                endX = (shimmerOffset + 0.3f) * 300f
                            )
                        )
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }

        // 进度信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (progress >= 0) "${(animatedProgress * 100).toInt()}%" else stringResource(R.string.analyzing),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = primaryColor
            )

            Surface(
                color = primaryContainer.copy(alpha = 0.8f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = getProcessingStage(animatedProgress),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
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
        color = surfaceVariant.copy(alpha = 0.25f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 心率指标
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${heartRate.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFE53E3E)
                )
                Text(
                    text = stringResource(R.string.bpm_unit),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurface.copy(alpha = 0.7f)
                )
            }

            // 分隔线
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                onSurface.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // 信号质量
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${(signalQuality * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = primaryColor
                )
                Text(
                    text = stringResource(R.string.quality_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 绘制增强版扫描弧线
 */
private fun DrawScope.drawEnhancedScanArc(color: Color, glowIntensity: Float) {
    val radius = size.minDimension / 2.5f

    // 外层光晕
    drawArc(
        color = color.copy(alpha = glowIntensity * 0.3f),
        startAngle = -95f,
        sweepAngle = 100f,
        useCenter = false,
        style = Stroke(
            width = 8.dp.toPx(),
            cap = StrokeCap.Round
        ),
        topLeft = Offset(
            (size.width - radius * 2) / 2,
            (size.height - radius * 2) / 2
        ),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
    )

    // 主扫描弧
    drawArc(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color.Transparent,
                color.copy(alpha = 0.4f),
                color.copy(alpha = 0.9f),
                color,
                Color.Transparent
            ),
            center = Offset(size.width / 2, size.height / 2)
        ),
        startAngle = -90f,
        sweepAngle = 90f,
        useCenter = false,
        style = Stroke(
            width = 4.dp.toPx(),
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
 * 绘制增强版ECG波形
 */
private fun DrawScope.drawEnhancedECGWave(
    color: Color,
    offset: Float,
    glowIntensity: Float
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = size.minDimension / 3.5f

    val path = Path()
    var isFirst = true

    for (i in 0..360 step 6) {
        val angle = Math.toRadians(i.toDouble() + offset * 50)
        val amplitude = when {
            i % 60 in 18..28 -> 10f * sin((i - 18) * Math.PI / 10).toFloat() * glowIntensity
            i % 60 in 32..38 -> 15f * sin((i - 32) * Math.PI / 6).toFloat() * glowIntensity
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

    // 外层光晕
    drawPath(
        path = path,
        color = color.copy(alpha = 0.3f * glowIntensity),
        style = Stroke(
            width = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
    )

    // 主波形
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    )
}

/**
 * 获取当前处理阶段
 */
@Composable
private fun getProcessingStage(progress: Float): String {
    return when {
        progress < 0.05f -> stringResource(R.string.stage_initializing)
        progress < 0.20f -> stringResource(R.string.stage_signal_collection)
        progress < 0.40f -> stringResource(R.string.stage_preprocessing)
        progress < 0.60f -> stringResource(R.string.stage_noise_filtering)
        progress < 0.80f -> stringResource(R.string.stage_feature_extraction)
        progress < 0.95f -> stringResource(R.string.stage_data_analysis)
        else -> stringResource(R.string.stage_almost_complete)
    }
}