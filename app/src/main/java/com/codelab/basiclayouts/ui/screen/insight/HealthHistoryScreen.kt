package com.codelab.basiclayouts.ui.insight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelab.basiclayouts.viewmodel.insight.HealthRecord
import com.codelab.basiclayouts.viewmodel.insight.InsightViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthHistoryScreen(viewModel: InsightViewModel, onBackPressed: () -> Unit) {
    val healthRecords by viewModel.healthRecords.collectAsState()
    var selectedDate by remember { mutableStateOf(healthRecords.lastOrNull()?.date ?: Calendar.getInstance().time) }
    val selectedRecord by viewModel.getSelectedRecord(selectedDate).collectAsState(initial = null)

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dayFormat = SimpleDateFormat("d", Locale.getDefault())
    val weekdayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val fullDateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    val calendar = remember { Calendar.getInstance() }
    var currentMonthYear by remember { mutableStateOf(monthYearFormat.format(selectedDate)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health History") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    calendar.time = selectedDate
                    calendar.add(Calendar.MONTH, -1)
                    selectedDate = calendar.time
                    currentMonthYear = monthYearFormat.format(selectedDate)
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentMonthYear,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = {
                    calendar.time = selectedDate
                    calendar.add(Calendar.MONTH, 1)
                    selectedDate = calendar.time
                    currentMonthYear = monthYearFormat.format(selectedDate)
                }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(healthRecords) { record ->
                    val isSelected = record.date.time == selectedDate.time
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                            .clickable { selectedDate = record.date }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = weekdayFormat.format(record.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayFormat.format(record.date),
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${record.heartRate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            selectedRecord?.let { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = fullDateFormat.format(record.date),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HealthMetric(
                                value = "${record.heartRate}",
                                unit = "bpm",
                                label = "Heart Rate",
                                color = MaterialTheme.colorScheme.error
                            )
                            HealthMetric(
                                value = "${record.steps}",
                                unit = "steps",
                                label = "Steps",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            HealthMetric(
                                value = "${record.sleepHours}",
                                unit = "hrs",
                                label = "Sleep",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        HeartRateChart(selectedDate = record.date, healthRecords = healthRecords)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Weekly Trends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val lastWeekRecords = healthRecords.takeLast(7)
                    val avgHeartRate = lastWeekRecords.map { it.heartRate }.average().toInt()
                    val avgSteps = lastWeekRecords.map { it.steps }.average().toInt()
                    val avgSleep = lastWeekRecords.map { it.sleepHours }.average()
                    TrendMetricRow(
                        label = "Heart Rate",
                        value = "$avgHeartRate bpm",
                        trend = if (avgHeartRate > 80) "↑" else "↓",
                        trendLabel = if (avgHeartRate > 80) "3% higher than usual" else "5% lower than usual",
                        trendPositive = avgHeartRate in 60..80
                    )
                    Divider()
                    TrendMetricRow(
                        label = "Steps",
                        value = "$avgSteps",
                        trend = if (avgSteps > 8000) "↑" else "↓",
                        trendLabel = if (avgSteps > 8000) "8% more active" else "12% less active",
                        trendPositive = avgSteps > 8000
                    )
                    Divider()
                    TrendMetricRow(
                        label = "Sleep",
                        value = "${String.format("%.1f", avgSleep)} hrs",
                        trend = if (avgSleep > 7) "↑" else "↓",
                        trendLabel = if (avgSleep > 7) "Better sleep quality" else "Lower sleep quality",
                        trendPositive = avgSleep > 7
                    )
                }
            }
        }
    }
}

@Composable
fun HealthMetric(value: String, unit: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TrendMetricRow(label: String, value: String, trend: String, trendLabel: String, trendPositive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (trendPositive) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = trend,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (trendPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trendLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (trendPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun HeartRateChart(selectedDate: java.util.Date, healthRecords: List<HealthRecord>, modifier: Modifier = Modifier) {
    val calendar = Calendar.getInstance().apply { time = selectedDate }
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    val targetRecords = healthRecords.filter { record ->
        val recordCalendar = Calendar.getInstance().apply { time = record.date }
        val recordDayOfYear = recordCalendar.get(Calendar.DAY_OF_YEAR)
        val diff = abs(recordDayOfYear - dayOfYear)
        diff <= 3
    }.sortedBy { it.date }

    if (targetRecords.size < 3) return

    val maxHeartRate = targetRecords.maxOf { it.heartRate }.toFloat()
    val minHeartRate = targetRecords.minOf { it.heartRate }.toFloat()
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    val errorColor = MaterialTheme.colorScheme.error
    val backgroundColor = MaterialTheme.colorScheme.background
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Heart Rate Trend",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val padding = 30f
                val availableHeight = height - 2 * padding
                val midY = height / 2

                drawLine(
                    color = outlineVariantColor,
                    start = Offset(padding, midY),
                    end = Offset(width - padding, midY),
                    strokeWidth = 1f
                )
                drawLine(
                    color = outlineVariantColor,
                    start = Offset(padding, padding),
                    end = Offset(width - padding, padding),
                    strokeWidth = 1f,
                    alpha = 0.5f
                )
                drawLine(
                    color = outlineVariantColor,
                    start = Offset(padding, height - padding),
                    end = Offset(width - padding, height - padding),
                    strokeWidth = 1f,
                    alpha = 0.5f
                )

                if (targetRecords.size >= 2) {
                    val availableWidth = width - 2 * padding
                    val xStep = availableWidth / (targetRecords.size - 1)
                    val range = maxHeartRate - minHeartRate
                    val normalizeFactor = if (range > 0) availableHeight / range else 1f

                    for (i in 0 until targetRecords.size - 1) {
                        val startX = padding + i * xStep
                        val startY = height - padding - ((targetRecords[i].heartRate - minHeartRate) * normalizeFactor)
                        val endX = padding + (i + 1) * xStep
                        val endY = height - padding - ((targetRecords[i + 1].heartRate - minHeartRate) * normalizeFactor)
                        drawLine(
                            color = errorColor,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }

                    for (i in targetRecords.indices) {
                        val x = padding + i * xStep
                        val y = height - padding - ((targetRecords[i].heartRate - minHeartRate) * normalizeFactor)
                        drawCircle(color = backgroundColor, radius = 6f, center = Offset(x, y))
                        drawCircle(color = errorColor, radius = 4f, center = Offset(x, y))
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 30.dp, end = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                targetRecords.forEach { record ->
                    Text(
                        text = dayFormat.format(record.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariantColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(24.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${maxHeartRate.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariantColor
                )
                Spacer(modifier = Modifier.weight(1f))
                val midHeartRate = ((maxHeartRate + minHeartRate) / 2).toInt()
                Text(
                    text = "$midHeartRate",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariantColor
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${minHeartRate.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariantColor
                )
            }
        }
    }
}