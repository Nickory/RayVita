package com.codelab.basiclayouts.ui.screen.physnet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.data.physnet.model.EnhancedRppgResult
import com.codelab.basiclayouts.data.physnet.model.HrvData
import com.codelab.basiclayouts.data.physnet.model.QualityLevel
import com.codelab.basiclayouts.data.physnet.model.SignalQuality
import com.codelab.basiclayouts.data.physnet.model.SpO2Data
import com.codelab.basiclayouts.data.physnet.model.SpO2HealthStatus
import com.codelab.basiclayouts.data.physnet.model.StressLevel
import com.codelab.basiclayouts.viewModel.physnet.EnhancedRppgViewModel

/**
 * Compact Health Monitoring Card with Material Theme
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    text = stringResource(id = R.string.rppg_result_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(id = R.string.realtime_monitoring),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val (color, textResId) = when (quality) {
        QualityLevel.EXCELLENT -> MaterialTheme.colorScheme.primary to R.string.quality_excellent
        QualityLevel.GOOD -> MaterialTheme.colorScheme.secondary to R.string.quality_good
        QualityLevel.FAIR -> MaterialTheme.colorScheme.tertiary to R.string.quality_fair
        QualityLevel.POOR -> MaterialTheme.colorScheme.error to R.string.quality_poor
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = stringResource(id = textResId),
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
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = stringResource(id = R.string.heart_rate),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = stringResource(id = R.string.heart_rate_bpm, heartRate.toInt()),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(id = R.string.heart_rate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Compact Confidence
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(id = R.string.percentage_format, (confidence * 100).toInt()),
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
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap
            )

            Text(
                text = stringResource(id = R.string.confidence),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
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
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = stringResource(id = R.string.hrv_analysis),
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(id = R.string.hrv_value_ms, hrvData.rmssd.toInt()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = hrvData.getHealthStatus().displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
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
                            contentDescription = if (showDetails)
                                stringResource(id = R.string.collapse)
                            else
                                stringResource(id = R.string.expand),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
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
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
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
                title = stringResource(id = R.string.rmssd),
                value = stringResource(id = R.string.value_ms, hrvData.rmssd.toInt()),
                modifier = Modifier.weight(1f)
            )
            CompactMetricCard(
                title = stringResource(id = R.string.sdnn),
                value = stringResource(id = R.string.value_ms, hrvData.sdnn.toInt()),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CompactMetricCard(
                title = stringResource(id = R.string.pnn50),
                value = stringResource(id = R.string.percentage_format, hrvData.pnn50.toInt()),
                modifier = Modifier.weight(1f)
            )
            CompactMetricCard(
                title = stringResource(id = R.string.stress),
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
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
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
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
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
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = stringResource(id = R.string.oxygen_saturation),
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(id = R.string.spo2_value_percent, spo2Data.spo2.toInt()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = spo2Data.getHealthStatus().displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
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
                            contentDescription = if (showDetails)
                                stringResource(id = R.string.collapse)
                            else
                                stringResource(id = R.string.expand),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
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
                    HorizontalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f))

                    // Medical Disclaimer
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.spo2_disclaimer),
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // SpO2 Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CompactMetricCard(
                            title = stringResource(id = R.string.level),
                            value = stringResource(id = R.string.percentage_format, spo2Data.spo2.toInt()),
                            modifier = Modifier.weight(1f)
                        )
                        CompactMetricCard(
                            title = stringResource(id = R.string.confidence),
                            value = stringResource(id = R.string.percentage_format, (spo2Data.confidence * 100).toInt()),
                            modifier = Modifier.weight(1f)
                        )
                        CompactMetricCard(
                            title = stringResource(id = R.string.r_value),
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
        SpO2HealthStatus.NORMAL -> MaterialTheme.colorScheme.primary to "✓"
        SpO2HealthStatus.MILD_HYPOXEMIA -> MaterialTheme.colorScheme.secondary to "!"
        SpO2HealthStatus.MODERATE_HYPOXEMIA -> MaterialTheme.colorScheme.tertiary to "!!"
        SpO2HealthStatus.SEVERE_HYPOXEMIA -> MaterialTheme.colorScheme.error to "⚠"
        SpO2HealthStatus.UNKNOWN -> MaterialTheme.colorScheme.outline to "?"
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
                contentDescription = stringResource(id = R.string.technical_details),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = stringResource(id = R.string.technical_details),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CompactTechInfo(
                label = stringResource(id = R.string.frames),
                value = "$frameCount"
            )
            CompactTechInfo(
                label = stringResource(id = R.string.time),
                value = stringResource(id = R.string.time_seconds, processingTime / 1000f)
            )
            CompactTechInfo(
                label = stringResource(id = R.string.snr),
                value = stringResource(id = R.string.snr_db, signalQuality.snr.toInt())
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
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
                    contentDescription = stringResource(id = R.string.analysis_mode),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(id = R.string.analysis_mode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                EnhancedRppgViewModel.AnalysisMode.values().forEach { mode ->
                    val (titleResId, descriptionResId, icon) = getModeDetails(mode)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeChanged(mode) },
                        color = if (currentMode == mode) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
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
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (currentMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(id = titleResId),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (currentMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = stringResource(id = descriptionResId),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MotionStatusIndicator(
    isStationary: Boolean,
    motionStatus: String,
    motionLevel: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = if (isStationary) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 运动状态图标
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

            // 状态文本
            Text(
                text = motionStatus,
                style = MaterialTheme.typography.labelMedium,
                color = if (isStationary) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )

            // 运动级别指示器（可选）
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

/**
 * 在相机预览中使用的运动状态提示
 */
@Composable
fun MotionStatusOverlay(
    isStationary: Boolean,
    motionStatus: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isStationary,
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
                        text = stringResource(id = R.string.motion_status_warning),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = stringResource(id = R.string.current_status, motionStatus),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Mode details helper
 */
private fun getModeDetails(mode: EnhancedRppgViewModel.AnalysisMode): Triple<Int, Int, ImageVector> {
    return when (mode) {
        EnhancedRppgViewModel.AnalysisMode.HEART_RATE_ONLY -> Triple(
            R.string.mode_heart_rate_only,
            R.string.mode_heart_rate_only_desc,
            Icons.Default.Favorite
        )
        EnhancedRppgViewModel.AnalysisMode.HRV_ONLY -> Triple(
            R.string.mode_hrv_analysis,
            R.string.mode_hrv_analysis_desc,
            Icons.Default.Timeline
        )
        EnhancedRppgViewModel.AnalysisMode.SPO2_ONLY -> Triple(
            R.string.mode_spo2_measurement,
            R.string.mode_spo2_measurement_desc,
            Icons.Default.Air
        )
        EnhancedRppgViewModel.AnalysisMode.ALL -> Triple(
            R.string.mode_complete_analysis,
            R.string.mode_complete_analysis_desc,
            Icons.Default.MonitorHeart
        )
    }
}

/**
 * Confidence color helper
 */
@Composable
private fun getConfidenceColor(confidence: Float): Color {
    return when {
        confidence >= 0.8f -> MaterialTheme.colorScheme.primary
        confidence >= 0.6f -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }
}