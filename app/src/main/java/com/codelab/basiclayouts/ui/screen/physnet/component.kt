package com.codelab.basiclayouts.ui.screen.physnet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.data.physnet.model.EnhancedRppgResult
import com.codelab.basiclayouts.data.physnet.model.HrvData
import com.codelab.basiclayouts.data.physnet.model.QualityLevel
import com.codelab.basiclayouts.data.physnet.model.SignalQuality
import com.codelab.basiclayouts.data.physnet.model.SpO2Data
import com.codelab.basiclayouts.data.physnet.model.SpO2HealthStatus
import com.codelab.basiclayouts.data.physnet.model.StressLevel
import com.codelab.basiclayouts.viewModel.physnet.EnhancedRppgViewModel

// Theme Colors
private val ThemePrimary = Color(0xFF6B5C4D)
private val ThemeOnPrimary = Color(0xFFFFFFFF)
private val ThemePrimaryContainer = Color(0xFFF4DFCD)
private val ThemeOnPrimaryContainer = Color(0xFF241A0E)
private val ThemeSecondary = Color(0xFF635D59)
private val ThemeOnSecondary = Color(0xFFFFFFFF)
private val ThemeSecondaryContainer = Color(0xFFEAE1DB)
private val ThemeOnSecondaryContainer = Color(0xFF1F1B17)
private val ThemeTertiary = Color(0xFF5E5F58)
private val ThemeOnTertiary = Color(0xFFFFFFFF)
private val ThemeTertiaryContainer = Color(0xFFE3E3DA)
private val ThemeOnTertiaryContainer = Color(0xFF1B1C17)
private val ThemeError = Color(0xFFBA1A1A)
private val ThemeErrorContainer = Color(0xFFFFDAD6)
private val ThemeOnError = Color(0xFFFFFFFF)
private val ThemeOnErrorContainer = Color(0xFF410002)
private val ThemeBackground = Color(0xFFF5F0EE)
private val ThemeOnBackground = Color(0xFF1D1B1A)
private val ThemeSurface = Color(0xFFFFFBFF)
private val ThemeOnSurface = Color(0xFF1D1B1A)
private val ThemeSurfaceVariant = Color(0xFFE7E1DE)
private val ThemeOnSurfaceVariant = Color(0xFF494644)
private val ThemeOutline = Color(0xFF7A7674)

/**
 * Compact Health Monitoring Card with Custom Theme
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedHealthMonitoringCard(
    result: EnhancedRppgResult,
    showHrvDetails: Boolean,
    showSpO2Details: Boolean,
    onToggleHrvDetails: () -> Unit,
    onToggleSpO2Details: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Compact Header
            CompactHeader(quality = result.signalQuality.getQualityLevel())

            // Primary Metrics
            CompactMetricsRow(
                heartRate = result.heartRate,
                confidence = result.confidence
            )

            // HRV Section
            result.hrvResult?.let { hrv ->
                CompactHrvSection(
                    hrvData = hrv,
                    showDetails = showHrvDetails,
                    onToggleDetails = onToggleHrvDetails
                )
            }

            // SpO2 Section
            result.spo2Result?.let { spo2 ->
                CompactSpO2Section(
                    spo2Data = spo2,
                    showDetails = showSpO2Details,
                    onToggleDetails = onToggleSpO2Details
                )
            }

            // Technical Details
            CompactTechnicalSection(
                frameCount = result.frameCount,
                processingTime = result.processingTimeMs,
                signalQuality = result.signalQuality
            )
        }
    }
}

/**
 * Compact header with quality indicator
 */
@Composable
private fun CompactHeader(quality: QualityLevel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Column {
                Text(
                    text = "rPPG Result",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ThemeOnSurface
                )
                Text(
                    text = "Real-time Monitoring",
                    style = MaterialTheme.typography.bodySmall,
                    color = ThemeOnSurfaceVariant
                )
            }
        }

        CompactQualityBadge(quality = quality)
    }
}

/**
 * Compact quality badge
 */
@Composable
private fun CompactQualityBadge(quality: QualityLevel) {
    val (color, text) = when (quality) {
        QualityLevel.EXCELLENT -> ThemePrimary to "Excellent"
        QualityLevel.GOOD -> ThemeSecondary to "Good"
        QualityLevel.FAIR -> ThemeTertiary to "Fair"
        QualityLevel.POOR -> ThemeError to "Poor"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Compact metrics display
 */
@Composable
private fun CompactMetricsRow(
    heartRate: Float,
    confidence: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Heart Rate
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ThemeErrorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Heart Rate",
                    tint = ThemeError,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = "${heartRate.toInt()} BPM",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ThemeOnSurface
                )
                Text(
                    text = "Heart Rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = ThemeOnSurfaceVariant
                )
            }
        }

        // Compact Confidence
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "${(confidence * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = getConfidenceColor(confidence)
            )

            LinearProgressIndicator(
                progress = { confidence },
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = getConfidenceColor(confidence),
                trackColor = ThemeSurfaceVariant,
                strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap
            )

            Text(
                text = "Confidence",
                style = MaterialTheme.typography.labelSmall,
                color = ThemeOnSurfaceVariant
            )
        }
    }
}

/**
 * Compact HRV section
 */
@Composable
private fun CompactHrvSection(
    hrvData: HrvData,
    showDetails: Boolean,
    onToggleDetails: () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (showDetails) 180f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "arrow_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleDetails() },
        colors = CardDefaults.cardColors(containerColor = ThemeSecondaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // HRV Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ThemeSecondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "HRV Analysis",
                            tint = ThemeSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "HRV: ${hrvData.rmssd.toInt()} ms",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ThemeOnSecondaryContainer
                        )
                        Text(
                            text = hrvData.getHealthStatus().displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = ThemeOnSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CompactStressBadge(stressLevel = hrvData.getStressLevel())

                    IconButton(
                        onClick = onToggleDetails,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = if (showDetails) "Collapse" else "Expand",
                            tint = ThemeOnSecondaryContainer,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(rotationAngle)
                        )
                    }
                }
            }

            // Expandable Details
            AnimatedVisibility(
                visible = showDetails,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = ThemeOnSecondaryContainer.copy(alpha = 0.2f))
                    CompactHrvGrid(hrvData = hrvData)
                }
            }
        }
    }
}

/**
 * Compact HRV metrics grid
 */
@Composable
private fun CompactHrvGrid(hrvData: HrvData) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CompactMetricCard(
                title = "RMSSD",
                value = "${hrvData.rmssd.toInt()} ms",
                modifier = Modifier.weight(1f)
            )
            CompactMetricCard(
                title = "SDNN",
                value = "${hrvData.sdnn.toInt()} ms",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CompactMetricCard(
                title = "pNN50",
                value = "${hrvData.pnn50.toInt()}%",
                modifier = Modifier.weight(1f)
            )
            CompactMetricCard(
                title = "Stress",
                value = String.format("%.1f", hrvData.stressIndex),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Compact metric card
 */
@Composable
private fun CompactMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ThemeSurface.copy(alpha = 0.8f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = ThemePrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = ThemeOnSurface
            )
        }
    }
}

/**
 * Compact stress badge
 */
@Composable
private fun CompactStressBadge(stressLevel: StressLevel) {
    val color = Color(android.graphics.Color.parseColor(stressLevel.color))

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = stressLevel.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Compact SpO2 section
 */
@Composable
private fun CompactSpO2Section(
    spo2Data: SpO2Data,
    showDetails: Boolean,
    onToggleDetails: () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (showDetails) 180f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "spo2_arrow_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleDetails() },
        colors = CardDefaults.cardColors(containerColor = ThemeTertiaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // SpO2 Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ThemeTertiary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = "Oxygen Saturation",
                            tint = ThemeTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "SpO₂: ${spo2Data.spo2.toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ThemeOnTertiaryContainer
                        )
                        Text(
                            text = spo2Data.getHealthStatus().displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = ThemeOnTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CompactSpO2Badge(status = spo2Data.getHealthStatus())

                    IconButton(
                        onClick = onToggleDetails,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = if (showDetails) "Collapse" else "Expand",
                            tint = ThemeOnTertiaryContainer,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(rotationAngle)
                        )
                    }
                }
            }

            // Expandable Details
            AnimatedVisibility(
                visible = showDetails,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = ThemeOnTertiaryContainer.copy(alpha = 0.2f))

                    // Medical Disclaimer
                    Surface(
                        color = ThemeSurface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Camera-based SpO₂ is for reference only",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = ThemeOnSurfaceVariant
                        )
                    }

                    // SpO2 Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CompactMetricCard(
                            title = "Level",
                            value = "${spo2Data.spo2.toInt()}%",
                            modifier = Modifier.weight(1f)
                        )
                        CompactMetricCard(
                            title = "Confidence",
                            value = "${(spo2Data.confidence * 100).toInt()}%",
                            modifier = Modifier.weight(1f)
                        )
                        CompactMetricCard(
                            title = "R-Value",
                            value = String.format("%.2f", spo2Data.ratioOfRatios),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact SpO2 status badge
 */
@Composable
private fun CompactSpO2Badge(status: SpO2HealthStatus) {
    val (color, icon) = when (status) {
        SpO2HealthStatus.NORMAL -> ThemePrimary to "✓"
        SpO2HealthStatus.MILD_HYPOXEMIA -> ThemeSecondary to "!"
        SpO2HealthStatus.MODERATE_HYPOXEMIA -> ThemeTertiary to "!!"
        SpO2HealthStatus.SEVERE_HYPOXEMIA -> ThemeError to "⚠"
        SpO2HealthStatus.UNKNOWN -> ThemeOutline to "?"
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = CircleShape,
        modifier = Modifier.size(24.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Compact technical details
 */
@Composable
private fun CompactTechnicalSection(
    frameCount: Int,
    processingTime: Long,
    signalQuality: SignalQuality
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Technical Details",
                tint = ThemeOnSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Technical Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = ThemeOnSurface
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CompactTechInfo(
                label = "Frames",
                value = "$frameCount"
            )
            CompactTechInfo(
                label = "Time",
                value = "${processingTime / 1000f}s"
            )
            CompactTechInfo(
                label = "SNR",
                value = "${signalQuality.snr.toInt()}dB"
            )
        }
    }
}

/**
 * Compact technical info display
 */
@Composable
private fun CompactTechInfo(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = ThemePrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ThemeOnSurfaceVariant
        )
    }
}

/**
 * Compact Analysis Mode Selector
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisModeSelector(
    currentMode: EnhancedRppgViewModel.AnalysisMode,
    onModeChanged: (EnhancedRppgViewModel.AnalysisMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ThemePrimaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Analysis Mode",
                    tint = ThemePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Analysis Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ThemeOnPrimaryContainer
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                EnhancedRppgViewModel.AnalysisMode.values().forEach { mode ->
                    val (title, description, icon) = getModeDetails(mode)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeChanged(mode) },
                        color = if (currentMode == mode) {
                            ThemePrimary.copy(alpha = 0.1f)
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = currentMode == mode,
                                onClick = { onModeChanged(mode) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = ThemePrimary,
                                    unselectedColor = ThemeOnSurfaceVariant
                                )
                            )

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (currentMode == mode) ThemePrimary else ThemeOnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (currentMode == mode) ThemePrimary else ThemeOnPrimaryContainer
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ThemeOnPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Mode details helper
 */
private fun getModeDetails(mode: EnhancedRppgViewModel.AnalysisMode): Triple<String, String, ImageVector> {
    return when (mode) {
        EnhancedRppgViewModel.AnalysisMode.HEART_RATE_ONLY -> Triple(
            "Heart Rate Only",
            "High accuracy heart rate measurement",
            Icons.Default.Favorite
        )
        EnhancedRppgViewModel.AnalysisMode.HRV_ONLY -> Triple(
            "HRV Analysis",
            "Heart variability and stress assessment",
            Icons.Default.Timeline
        )
        EnhancedRppgViewModel.AnalysisMode.SPO2_ONLY -> Triple(
            "SpO₂ Measurement",
            "Blood oxygen saturation monitoring",
            Icons.Default.Air
        )
        EnhancedRppgViewModel.AnalysisMode.ALL -> Triple(
            "Complete Analysis",
            "Full health monitoring suite",
            Icons.Default.MonitorHeart
        )
    }
}

/**
 * Confidence color helper
 */
private fun getConfidenceColor(confidence: Float): Color {
    return when {
        confidence >= 0.8f -> ThemePrimary
        confidence >= 0.6f -> ThemeSecondary
        else -> ThemeError
    }
}