package com.codelab.basiclayouts.ui.screen.home

import android.os.Build
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelab.basiclayouts.ui.viewmodel.home.HealthData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DashboardSection(
    healthData: HealthData,
    isLoading: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(Color.Transparent)
            .border(
                width = 1.5.dp,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    radius = 800f
                ),
                shape = RoundedCornerShape(36.dp)
            )
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(36.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
    ) {
        // Animation setup
        val infiniteTransition = rememberInfiniteTransition(label = "fusion_transition")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 18000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "gradient_angle"
        )
        val mistOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 600f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 22000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "mist_offset"
        )
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 12000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "pulse_scale"
        )

        // Dynamic color transitions
        val color1 by infiniteTransition.animateColor(
            initialValue = Color(0xFFCCE7FF).copy(alpha = 0.65f), // Soft cyan
            targetValue = Color(0xFFFFB6C1).copy(alpha = 0.6f), // Light coral
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "color1"
        )
        val color2 by infiniteTransition.animateColor(
            initialValue = Color(0xFFE6E6FA).copy(alpha = 0.55f), // Lavender
            targetValue = Color(0xFFFFD700).copy(alpha = 0.5f), // Gold
            animationSpec = infiniteRepeatable(
                animation = tween(24000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "color2"
        )
        val color3 by infiniteTransition.animateColor(
            initialValue = Color(0xFFB0E0E6).copy(alpha = 0.5f), // Powder blue
            targetValue = Color(0xFFFFA07A).copy(alpha = 0.45f), // Light salmon
            animationSpec = infiniteRepeatable(
                animation = tween(26000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "color3"
        )

        // Main gradient layer with rich colors
        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(pulseScale)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(color1, color2, color3, color1.copy(alpha = 0.3f)),
                        start = Offset(
                            x = 300f * cos(angle * PI / 180).toFloat(),
                            y = 300f * sin(angle * PI / 180).toFloat()
                        ),
                        end = Offset(
                            x = 300f * cos((angle + 180) * PI / 180).toFloat(),
                            y = 300f * sin((angle + 180) * PI / 180).toFloat()
                        )
                    )
                )
                .run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        this.blur(45.dp)
                    } else {
                        this.background(Color.White.copy(alpha = 0.15f))
                    }
                }
        )

        // Misty fusion layer with swirling effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            color2.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = Offset(
                            x = mistOffset,
                            y = 400f - mistOffset / 2
                        ),
                        radius = 900f
                    )
                )
                .run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        this.blur(30.dp)
                    } else {
                        this
                    }
                }
        )

        // Content layer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${healthData.heartRate}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 72.sp,
                    letterSpacing = (-2).sp,
                    color = Color.Black.copy(alpha = 0.95f)
                )
                Text(
                    text = "bpm",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = Color.Black.copy(alpha = 0.75f)
                )
                Text(
                    text = "${healthData.spO2}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 72.sp,
                    letterSpacing = (-2).sp,
                    color = Color.Black.copy(alpha = 0.95f)
                )
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "% SpO2",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black.copy(alpha = 0.75f)
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, end = 8.dp)
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    // HeartRateWaveform()
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BodyVisualization()
            }
        }
    }
}

@Composable
fun HeartRateWaveform() {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val animatedX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform"
    )

    val orange = Color(0xFFFF7D45)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val midY = height / 2

        // Starting point
        var currentX = 0f
        var currentY = midY

        // Draw the ECG-like pattern
        val segmentWidth = width / 10

        // Simple ECG pattern
        val points = listOf(
            Offset(currentX, midY),                 // Start
            Offset(currentX + segmentWidth * 0.2f, midY),          // Baseline
            Offset(currentX + segmentWidth * 0.3f, midY - 5),      // P wave
            Offset(currentX + segmentWidth * 0.4f, midY),          // Return to baseline
            Offset(currentX + segmentWidth * 0.5f, midY),          // Brief flat
            Offset(currentX + segmentWidth * 0.6f, midY + 15),     // Q wave
            Offset(currentX + segmentWidth * 0.7f, midY - 40),     // R wave (peak)
            Offset(currentX + segmentWidth * 0.8f, midY + 20),     // S wave
            Offset(currentX + segmentWidth * 0.9f, midY),          // Return to baseline
            Offset(currentX + segmentWidth * 1.1f, midY + 10),     // T wave
            Offset(currentX + segmentWidth * 1.3f, midY),          // Return to baseline
        )

        // Offset for animation
        val offset = (animatedX % (width * 0.5f))

        for (i in 0 until 3) { // Repeat pattern
            val patternOffset = i * segmentWidth * 3f - offset

            for (j in 0 until points.size - 1) {
                val start = Offset(points[j].x + patternOffset, points[j].y)
                val end = Offset(points[j + 1].x + patternOffset, points[j + 1].y)

                if (start.x >= 0 && start.x <= width && end.x >= 0 && end.x <= width) {
                    drawLine(
                        color = orange,
                        start = start,
                        end = end,
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun BodyVisualization() {
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseFactor by pulseTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {

//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .alpha(0.7f)
//                .blur(200.dp)
//                .background(
//                    brush = Brush.radialGradient(
//                        colors = listOf(
//                            Color(0xFFF5F5F5),
//                            Color(0xFFFAFAFA)
//                        )
//                    )
//                )
//        )

//        Image(
//            painter = painterResource(id = R.drawable.man), // 替换成你实际的图片资源
//            contentDescription = "Virtual Human",
//            contentScale = ContentScale.Fit,
//            modifier = Modifier
//                .fillMaxHeight(0.9f) // 根据需要调整比例
//        )

//        Box(
//            modifier = Modifier
//                .size(80.dp)
//                .offset(x = (-20).dp, y = (-30).dp) // Position near the chest
//                .graphicsLayer {
//                    var scaleX = pulseFactor
//                    var scaleY = pulseFactor
//                }
//                .background(
//                    brush = Brush.radialGradient(
//                        colors = listOf(
//                            Color(0xFFFF7D45).copy(alpha = 0.8f),
//                            Color(0xFFFF7D45).copy(alpha = 0.1f)
//                        )
//                    ),
//                    shape = CircleShape
//                )
//        )
    }
}