package com.codelab.basiclayouts.ui.screen.physnet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.data.physnet.model.EnhancedRppgResult
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Enhanced Material3 Heart Rate Result Display Card
 * Redesigned with modern UI principles and improved layout
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun EnhancedRppgResultCard(
    result: EnhancedRppgResult,
    modifier: Modifier = Modifier
) {
    // Synchronized heartbeat animation based on actual heart rate
    val heartbeatAnimation = rememberInfiniteTransition(label = "heartbeat")
    val heartScale by heartbeatAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (60000 / result.heartRate).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_scale"
    )

    // Dynamic gradient based on heart rate zones with enhanced Material3 colors
    val (gradientColors, statusInfo) = getHeartRateTheme(result.heartRate)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = gradientColors,
                        radius = 500f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Enhanced Header Section
                CardHeader(
                    heartScale = heartScale,
//                    isSynced = result.isSynced
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Premium Heart Rate Display Circle
                HeartRateDisplay(
                    heartRate = result.heartRate,
                    statusInfo = statusInfo
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Status Badge
                HeartRateStatusBadge(
                    status = statusInfo.status,
                    color = statusInfo.color,
                    icon = statusInfo.icon
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Enhanced Information Grid
                EnhancedInfoGrid(result = result)
            }
        }
    }
}

/**
 * Modern card header with improved typography and layout
 */
@Composable
private fun CardHeader(
    heartScale: Float,
//    isSynced: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MonitorHeart,
                contentDescription = "Heart Rate Monitor",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .scale(heartScale)
                    .size(28.dp)
            )
            Column {
                Text(
                    text = "Heart Rate",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Monitoring Result",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

//        // Enhanced sync status
//        SyncStatusIndicator(isSynced = isSynced)
    }
}

/**
 * Premium heart rate display with enhanced visual design
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HeartRateDisplay(
    heartRate: Float,
    statusInfo: HeartRateStatusInfo
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            statusInfo.color.copy(alpha = 0.15f),
                            statusInfo.color.copy(alpha = 0.08f),
                            statusInfo.color.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Middle ring
        Box(
            modifier = Modifier
                .size(176.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        )

        // Inner content area
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated heart rate value
                AnimatedContent(
                    targetState = heartRate.toInt(),
                    transitionSpec = {
                        slideInVertically { height -> height / 2 } + fadeIn() with
                                slideOutVertically { height -> -height / 2 } + fadeOut()
                    },
                    label = "heart_rate_animation"
                ) { rate ->
                    Text(
                        text = "$rate",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.2f
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = statusInfo.color
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "BPM",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = MaterialTheme.typography.titleLarge.letterSpacing * 1.5f
                )
            }
        }
    }
}

/**
 * Enhanced status badge with modern Material3 styling
 */
@Composable
private fun HeartRateStatusBadge(
    status: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = status,
                color = color,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Modern information grid with improved spacing and typography
 */
@Composable
private fun EnhancedInfoGrid(result: EnhancedRppgResult) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoCard(
                icon = Icons.Default.AccessTime,
                label = "Measured",
                value = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(result.timestamp),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            InfoCard(
                icon = Icons.Default.Speed,
                label = "Confidence",
                value = "${(result.confidence * 100).toInt()}%",
                modifier = Modifier.weight(1f)
            )
        }

        // Bottom centered card
        InfoCard(
            icon = Icons.Default.Timer,
            label = "Processing Time",
            value = "${result.processingTimeMs}ms",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Enhanced info card with modern Material3 design
 */
@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Modern sync status indicator
 */
@Composable
private fun SyncStatusIndicator(isSynced: Boolean) {
    val containerColor = if (isSynced) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = if (isSynced) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = contentColor
            )
            Text(
                text = if (isSynced) "Synced" else "Offline",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

/**
 * Heart rate status information data class
 */
private data class HeartRateStatusInfo(
    val status: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * Enhanced heart rate theme provider with Material3 colors
 */
@Composable
private fun getHeartRateTheme(heartRate: Float): Pair<List<Color>, HeartRateStatusInfo> {
    return when {
        heartRate < 60 -> {
            val color = MaterialTheme.colorScheme.tertiary
            Pair(
                listOf(
                    color.copy(alpha = 0.08f),
                    color.copy(alpha = 0.03f)
                ),
                HeartRateStatusInfo(
                    status = "Below Normal",
                    color = color,
                    icon = Icons.Default.TrendingDown
                )
            )
        }
        heartRate > 100 -> {
            val color = MaterialTheme.colorScheme.error
            Pair(
                listOf(
                    color.copy(alpha = 0.08f),
                    color.copy(alpha = 0.03f)
                ),
                HeartRateStatusInfo(
                    status = "Above Normal",
                    color = color,
                    icon = Icons.Default.TrendingUp
                )
            )
        }
        else -> {
            val color = MaterialTheme.colorScheme.primary
            Pair(
                listOf(
                    color.copy(alpha = 0.08f),
                    color.copy(alpha = 0.03f)
                ),
                HeartRateStatusInfo(
                    status = "Normal Range",
                    color = color,
                    icon = Icons.Default.CheckCircle
                )
            )
        }
    }
}