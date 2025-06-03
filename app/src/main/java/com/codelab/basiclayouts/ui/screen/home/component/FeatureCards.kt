// ============== component/FeatureCards.kt ==============
package com.codelab.basiclayouts.ui.screen.home.component

import android.os.Build
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.ui.viewmodel.home.Achievement
import com.codelab.basiclayouts.ui.viewmodel.home.BreathingSession
import com.codelab.basiclayouts.ui.viewmodel.home.HealthTip
import com.codelab.basiclayouts.ui.viewmodel.home.RecentScan
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Enhanced AI Assistant Card with advanced gradient effects
@Composable
fun EnhancedAIAssistantCard(
    tip: HealthTip,
    onGetNewTip: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var isGenerating by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "aiGradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientOffset"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colorScheme.primaryContainer.copy(alpha = 0.1f),
                            colorScheme.background.copy(alpha = 0.7f)
                        ),
                        start = Offset(gradientOffset % 500f, 0f),
                        end = Offset((gradientOffset % 500f) + 300f, 300f)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.home_ai_health_insights),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.home_personalized_recommendations),
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            isGenerating = true
                            onGetNewTip()
                        },
                        enabled = !isGenerating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.home_generate),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Stop loading after tip changes
                LaunchedEffect(tip.message) {
                    delay(2000)
                    isGenerating = false
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AI Tip Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorScheme.primaryContainer.copy(alpha = 0.1f),
                                    colorScheme.background.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            colorScheme.outline.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = tip.message,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = colorScheme.onBackground
                    )
                }
            }
        }
    }
}
@Composable
fun PremiumBreathingCard(
    session: BreathingSession?,
    onStartRPPG: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var isActive by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(30) }
    var phase by remember { mutableStateOf("inhale") }
    var cycleCount by remember { mutableStateOf(0) }
    var isCompleted by remember { mutableStateOf(false) }

    // Advanced gradient animations
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")

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

    // Breathing circle animation with smooth transitions
    val breathingScale by animateFloatAsState(
        targetValue = when (phase) {
            "inhale" -> 1.3f
            "hold" -> 1.25f
            else -> 1.0f
        },
        animationSpec = tween(
            durationMillis = when (phase) {
                "inhale" -> 4000
                "hold" -> 2000
                else -> 4000
            },
            easing = LinearOutSlowInEasing
        ),
        label = "breathing_animation"
    )

    // Outer ripple animations
    val ripple1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "ripple1"
    )

    val ripple2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing, delayMillis = 1000),
            repeatMode = RepeatMode.Restart
        ), label = "ripple2"
    )

    val ripple3 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing, delayMillis = 2000),
            repeatMode = RepeatMode.Restart
        ), label = "ripple3"
    )

    // Particle rotation
    val particleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "particle_rotation"
    )

    // Dynamic breathing colors
    val primaryColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF4FC3F7),
        targetValue = Color(0xFF81C784),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "primary_color"
    )

    val secondaryColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF9C27B0),
        targetValue = Color(0xFFFF7043),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "secondary_color"
    )

    val accentColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFFFB74D),
        targetValue = Color(0xFFE57373),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "accent_color"
    )

    // Dynamic background colors
    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFFCCE7FF).copy(alpha = 0.65f),
        targetValue = Color(0xFFFFB6C1).copy(alpha = 0.6f),
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "color1"
    )
    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFFE6E6FA).copy(alpha = 0.55f),
        targetValue = Color(0xFFFFD700).copy(alpha = 0.5f),
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "color2"
    )
    val color3 by infiniteTransition.animateColor(
        initialValue = Color(0xFFB0E0E6).copy(alpha = 0.5f),
        targetValue = Color(0xFFFFA07A).copy(alpha = 0.45f),
        animationSpec = infiniteRepeatable(
            animation = tween(26000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "color3"
    )

    LaunchedEffect(isActive) {
        if (isActive && seconds > 0) {
            var cycleStartTime = 30 - seconds
            while (seconds > 0 && isActive) {
                delay(1000L) // Update every second
                seconds-- // Decrement by 1

                val cycleTime = (30 - seconds) % 10
                when {
                    cycleTime < 4 -> phase = "inhale"
                    cycleTime < 6 -> phase = "hold"
                    else -> phase = "exhale"
                }

                if (cycleTime >= 9.9 && cycleStartTime < cycleTime) {
                    cycleCount++
                    cycleStartTime += 10
                }
            }

            if (seconds == 0) {
                isActive = false
                isCompleted = true
            }
        }
    }

    val resetTimer = {
        seconds = 30
        isActive = false
        phase = "inhale"
        cycleCount = 0
        isCompleted = false
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
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
                shape = RoundedCornerShape(24.dp)
            )
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
    ) {
        // Main gradient layer
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

        // Misty fusion layer
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

        // Content
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = null,
                        tint = colorScheme.tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.home_breathing_exercise),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.9f)
                        )
                        Text(
                            text = stringResource(R.string.home_thirty_second_session),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }
                }

                if (!isCompleted) {
                    IconButton(onClick = resetTimer) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.home_reset),
                            tint = Color.Black.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isCompleted) {
                // Enhanced breathing visualization
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Premium breathing circle with multiple layers
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Canvas for custom animations
                        Canvas(
                            modifier = Modifier.size(200.dp)
                        ) {
                            val center = size.center
                            val radius = size.minDimension / 2f

                            // Draw ripple effects
                            if (isActive) {
                                drawRippleEffect(
                                    center = center,
                                    baseRadius = radius * 0.6f,
                                    scale = ripple1,
                                    color = primaryColor.copy(alpha = 0.3f * (1.6f - ripple1))
                                )
                                drawRippleEffect(
                                    center = center,
                                    baseRadius = radius * 0.6f,
                                    scale = ripple2,
                                    color = secondaryColor.copy(alpha = 0.25f * (1.6f - ripple2))
                                )
                                drawRippleEffect(
                                    center = center,
                                    baseRadius = radius * 0.6f,
                                    scale = ripple3,
                                    color = accentColor.copy(alpha = 0.2f * (1.6f - ripple3))
                                )
                            }

                            // Draw outer glow ring
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.1f),
                                        Color.Transparent
                                    ),
                                    radius = radius * 0.9f
                                ),
                                radius = radius * 0.9f,
                                center = center
                            )

                            // Draw animated particles around the circle
                            drawBreathingParticles(
                                center = center,
                                radius = radius * 0.75f,
                                rotation = particleRotation,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                isActive = isActive
                            )

                            // Draw main breathing ring with gradient stroke
                            val strokeWidth = 8.dp.toPx()
                            val mainRadius = radius * 0.6f * breathingScale

                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = when (phase) {
                                        "inhale" -> listOf(
                                            primaryColor,
                                            primaryColor.copy(alpha = 0.7f),
                                            secondaryColor,
                                            primaryColor
                                        )
                                        "hold" -> listOf(
                                            accentColor,
                                            accentColor.copy(alpha = 0.7f),
                                            primaryColor,
                                            accentColor
                                        )
                                        else -> listOf(
                                            secondaryColor,
                                            secondaryColor.copy(alpha = 0.7f),
                                            accentColor,
                                            secondaryColor
                                        )
                                    }
                                ),
                                radius = mainRadius,
                                center = center,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            )

                            // Draw inner glow
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = when (phase) {
                                        "inhale" -> listOf(
                                            primaryColor.copy(alpha = 0.4f),
                                            primaryColor.copy(alpha = 0.1f),
                                            Color.Transparent
                                        )
                                        "hold" -> listOf(
                                            accentColor.copy(alpha = 0.4f),
                                            accentColor.copy(alpha = 0.1f),
                                            Color.Transparent
                                        )
                                        else -> listOf(
                                            secondaryColor.copy(alpha = 0.4f),
                                            secondaryColor.copy(alpha = 0.1f),
                                            Color.Transparent
                                        )
                                    }
                                ),
                                radius = mainRadius * 0.8f,
                                center = center
                            )
                        }

                        // Central timer display with enhanced styling
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(breathingScale * 0.7f + 0.3f)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.9f),
                                            Color.White.copy(alpha = 0.7f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .border(
                                    2.dp,
                                    brush = Brush.sweepGradient(
                                        colors = when (phase) {
                                            "inhale" -> listOf(
                                                primaryColor.copy(alpha = 0.8f),
                                                primaryColor.copy(alpha = 0.4f)
                                            )
                                            "hold" -> listOf(
                                                accentColor.copy(alpha = 0.8f),
                                                accentColor.copy(alpha = 0.4f)
                                            )
                                            else -> listOf(
                                                secondaryColor.copy(alpha = 0.8f),
                                                secondaryColor.copy(alpha = 0.4f)
                                            )
                                        }
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = seconds.toString(),
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                                fontWeight = FontWeight.Bold,
                                color = when (phase) {
                                    "inhale" -> primaryColor
                                    "hold" -> accentColor
                                    else -> secondaryColor
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phase instruction with enhanced styling
                    Text(
                        text = when (phase) {
                            "inhale" -> stringResource(R.string.home_breathe_in)
                            "hold" -> stringResource(R.string.home_hold)
                            else -> stringResource(R.string.home_breathe_out)
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                        fontWeight = FontWeight.Medium,
                        color = when (phase) {
                            "inhale" -> primaryColor
                            "hold" -> accentColor
                            else -> secondaryColor
                        }
                    )

                    Text(
                        text = stringResource(R.string.home_cycle_format, cycleCount + 1, 3),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Control button with enhanced styling
                    Button(
                        onClick = { isActive = !isActive },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) colorScheme.error else primaryColor
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isActive) stringResource(R.string.home_pause) else stringResource(R.string.home_start_exercise),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Completion state with enhanced effects
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Celebration particles
                        Canvas(modifier = Modifier.size(120.dp)) {
                            val center = size.center
                            val radius = size.minDimension / 2f

                            // Draw celebration sparkles
                            repeat(8) { i ->
                                val angle = (i * 45f) + (particleRotation * 0.5f)
                                val x = center.x + cos(angle * PI / 180) * radius * 0.8f
                                val y = center.y + sin(angle * PI / 180) * radius * 0.8f

                                drawCircle(
                                    color = primaryColor.copy(alpha = 0.8f),
                                    radius = 3.dp.toPx(),
                                    center = Offset(x.toFloat(), y.toFloat())
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.3f),
                                            primaryColor.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                                .border(
                                    3.dp,
                                    primaryColor.copy(alpha = 0.6f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = primaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.home_well_done),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )

                    Text(
                        text = stringResource(R.string.home_breathing_completion_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onStartRPPG()
                            resetTimer()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.home_start_health_scan),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Helper function to draw ripple effects
private fun DrawScope.drawRippleEffect(
    center: Offset,
    baseRadius: Float,
    scale: Float,
    color: Color
) {
    val radius = baseRadius * scale
    drawCircle(
        color = color,
        radius = radius,
        center = center,
        style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    )
}

// Helper function to draw breathing particles
private fun DrawScope.drawBreathingParticles(
    center: Offset,
    radius: Float,
    rotation: Float,
    primaryColor: Color,
    secondaryColor: Color,
    isActive: Boolean
) {
    val particleCount = 12
    val colors = listOf(primaryColor, secondaryColor)

    repeat(particleCount) { i ->
        val angle = (i * (360f / particleCount)) + rotation
        val particleRadius = if (isActive) radius * (0.9f + 0.1f * sin((rotation + i * 30) * PI / 180).toFloat()) else radius
        val x = center.x + cos(angle * PI / 180) * particleRadius
        val y = center.y + sin(angle * PI / 180) * particleRadius

        val color = colors[i % colors.size].copy(
            alpha = if (isActive) 0.6f + 0.4f * sin((rotation * 2 + i * 60) * PI / 180).toFloat() else 0.3f
        )

        drawCircle(
            color = color,
            radius = if (isActive) 3.dp.toPx() + sin((rotation + i * 45) * PI / 180).toFloat() * 2.dp.toPx() else 2.dp.toPx(),
            center = Offset(x.toFloat(), y.toFloat())
        )
    }
}

// Enhanced Measurement History Card
@Composable
fun EnhancedMeasurementHistoryCard(
    scans: List<RecentScan>
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.home_recent_measurements),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }

                Text(
                    text = stringResource(R.string.home_today),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            scans.take(3).forEachIndexed { index, scan ->
                EnhancedScanItem(scan = scan)
                if (index < minOf(scans.size - 1, 2)) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (scans.size > 3) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { /* Navigate to full history */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.home_view_all_records))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedScanItem(scan: RecentScan) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.surfaceContainerHighest
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(scan.timestamp)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.home_quality_format, scan.signalQuality),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniMetric(
                    value = "${scan.heartRate.toInt()}",
                    unit = stringResource(R.string.home_unit_bpm),
                    color = colorScheme.error
                )
                MiniMetric(
                    value = "${scan.spO2.toInt()}",
                    unit = stringResource(R.string.home_unit_percent),
                    color = colorScheme.tertiary
                )
                MiniMetric(
                    value = "${scan.hrv.toInt()}",
                    unit = stringResource(R.string.home_unit_ms),
                    color = colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun MiniMetric(
    value: String,
    unit: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.7f)
        )
    }
}

// Premium Achievements Card
@Composable
fun PremiumAchievementsCard(
    achievements: List<Achievement>,
    onViewAll: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val unlockedCount = achievements.count { it.isUnlocked }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.home_achievement_badges),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.home_earned_format, unlockedCount, achievements.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onViewAll,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.home_view_all))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(achievements.take(4)) { achievement ->
                    PremiumAchievementBadge(achievement = achievement)
                }
            }
        }
    }
}

@Composable
fun PremiumAchievementBadge(
    achievement: Achievement
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (achievement.isUnlocked) colorScheme.primary else colorScheme.outline.copy(alpha = 0.2f),
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (achievement.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (achievement.isUnlocked) colorScheme.onPrimary else colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = achievement.name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = if (achievement.isUnlocked) colorScheme.onSurface else colorScheme.onSurfaceVariant
        )

        if (!achievement.isUnlocked && achievement.progress > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { achievement.progress },
                modifier = Modifier
                    .width(50.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = colorScheme.primary,
                trackColor = colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    }
}