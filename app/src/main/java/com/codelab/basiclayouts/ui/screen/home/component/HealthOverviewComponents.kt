// ============== component/HealthOverviewComponents.kt ==============
package com.codelab.basiclayouts.ui.screen.home.component

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.ui.viewmodel.home.HealthData
import com.codelab.basiclayouts.ui.viewmodel.home.TrendData

// Enhanced Health Overview Card with insight-style colors
@Composable
fun EnhancedHealthOverviewCard(
    healthData: HealthData,
    trendData: List<TrendData>,
    isLoading: Boolean,
    onRefresh: () -> Unit
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
            modifier = Modifier.padding(16.dp)
        ) {
            // Header - 更紧凑的头部布局
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.home_health_overview),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        if (healthData.lastUpdateTime != "Never") {
                            Text(
                                text = stringResource(R.string.home_updated_format, healthData.lastUpdateTime),
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .background(
                            colorScheme.primaryContainer,
                            CircleShape
                        )
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.home_refresh),
                        tint = colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                LoadingHealthData()
            } else if (healthData.measurementCount == 0) {
                EmptyHealthDataCard()
            } else {
                // Health metrics grid with insight styling
                EnhancedHealthMetricsGrid(healthData = healthData)

                // Compact trends section
                if (trendData.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        color = colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CompactTrendSection(trendData = trendData)
                }
            }
        }
    }
}

// Enhanced Health Metrics Grid with insight-style colors
@Composable
fun EnhancedHealthMetricsGrid(
    healthData: HealthData
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Column {
        // 主要健康指标 - 三列紧密排列
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EnhancedMetricDisplay(
                value = if (healthData.heartRate > 0) "${healthData.heartRate.toInt()}" else "--",
                unit = stringResource(R.string.home_unit_bpm),
                label = stringResource(R.string.home_heart_rate),
                status = healthData.heartRateStatus,
                icon = Icons.Default.Favorite,
                backgroundColor = colorScheme.error.copy(alpha = 0.1f),
                iconColor = colorScheme.error,
                modifier = Modifier.weight(1f)
            )

            EnhancedMetricDisplay(
                value = if (healthData.spO2 > 0) "${healthData.spO2.toInt()}" else "--",
                unit = stringResource(R.string.home_unit_percent),
                label = stringResource(R.string.home_spo2),
                status = healthData.spo2Status,
                icon = Icons.Default.Air,
                backgroundColor = colorScheme.tertiary.copy(alpha = 0.1f),
                iconColor = colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )

            EnhancedMetricDisplay(
                value = if (healthData.hrv > 0) "${healthData.hrv.toInt()}" else "--",
                unit = stringResource(R.string.home_unit_ms),
                label = stringResource(R.string.home_hrv),
                status = healthData.hrvStatus,
                icon = Icons.Default.Timeline,
                backgroundColor = colorScheme.secondary.copy(alpha = 0.1f),
                iconColor = colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Measurement statistics - 紧凑的统计信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricInfoChip(
                label = stringResource(R.string.home_today),
                value = stringResource(R.string.home_times_format, healthData.measurementCount)
            )

            MetricInfoChip(
                label = stringResource(R.string.home_accuracy),
                value = stringResource(R.string.home_percent_format, (healthData.confidence * 100).toInt())
            )
        }
    }
}

// Enhanced Metric Display with insight styling
@Composable
fun EnhancedMetricDisplay(
    value: String,
    unit: String,
    label: String,
    status: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            if (value == "--") {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = iconColor
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = iconColor
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant
        )
        Text(
            text = getStatusDisplayText(status),
            style = MaterialTheme.typography.labelSmall,
            color = getStatusColor(status),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MetricInfoChip(
    label: String,
    value: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun CompactTrendSection(
    trendData: List<TrendData>
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Column {
        Text(
            text = stringResource(R.string.home_seven_day_trends),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            trendData.take(3).forEach { trend ->
                CompactTrendItem(
                    trend = trend,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CompactTrendItem(
    trend: TrendData,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val trendColor = if (trend.isPositive) colorScheme.primary else colorScheme.error

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (trend.isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = trendColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = trend.change,
                style = MaterialTheme.typography.labelMedium,
                color = trendColor,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = trend.label,
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant
        )
    }
}

// Helper functions
@Composable
fun getStatusDisplayText(status: String): String {
    val context = LocalContext.current
    return when (status) {
        "Low" -> stringResource(R.string.home_status_low)
        "High" -> stringResource(R.string.home_status_high)
        "Normal" -> stringResource(R.string.home_status_normal)
        "Good" -> stringResource(R.string.home_status_good)
        "Excellent" -> stringResource(R.string.home_status_excellent)
        else -> status
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "Low" -> Color(0xFF2196F3)
        "High" -> Color(0xFFE53E3E)
        "Normal" -> Color(0xFF38A169)
        "Good" -> Color(0xFF38A169)
        "Excellent" -> Color(0xFF00B5D6)
        else -> Color.Gray
    }
}