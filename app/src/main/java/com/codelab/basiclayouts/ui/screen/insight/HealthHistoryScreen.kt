package com.codelab.basiclayouts.ui.insight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.viewmodel.insight.InsightViewModel
import com.codelab.basiclayouts.viewmodel.insight.PhysNetMeasurementData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthHistoryScreen(viewModel: InsightViewModel, onBackPressed: () -> Unit) {
    val measurementsByDate by viewModel.measurementsByDate.collectAsState()
    val availableDates = viewModel.getAvailableDates()
    var selectedDate by remember { mutableStateOf(availableDates.firstOrNull() ?: Calendar.getInstance().time) }
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val measurements = viewModel.getMeasurementsForDate(selectedDate)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("rPPG History") },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { selectedDate = availableDates.getOrNull(availableDates.indexOf(selectedDate) + 1) ?: selectedDate },
                    enabled = availableDates.indexOf(selectedDate) < availableDates.size - 1
                ) {
                    Icon(Icons.Default.ArrowBack, "Previous Day")
                }
                Text(
                    text = dateFormat.format(selectedDate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { selectedDate = availableDates.getOrNull(availableDates.indexOf(selectedDate) - 1) ?: selectedDate },
                    enabled = availableDates.indexOf(selectedDate) > 0
                ) {
                    Icon(Icons.Default.ArrowForward, "Next Day")
                }
            }

            // Measurement records list
            if (measurements.isEmpty()) {
                Text(
                    text = "No measurements for this date",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(measurements) { measurement ->
                        MeasurementCard(measurement)
                    }
                }
            }
        }
    }
}

@Composable
fun MeasurementCard(measurement: PhysNetMeasurementData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Time: ${timeFormat.format(Date(measurement.timestamp))}", style = MaterialTheme.typography.bodyMedium)
                Text("ID: ${measurement.sessionId.take(8)}...", style = MaterialTheme.typography.bodySmall)
            }

            RppgWaveformThumbnail(
                signal = measurement.rppgSignal,
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Heart Rate: ${measurement.heartRate.toInt()} BPM", color = MaterialTheme.colorScheme.error)
                measurement.hrvResult?.let { Text("HRV: ${it.rmssd.toInt()} ms", color = MaterialTheme.colorScheme.secondary) }
                measurement.spo2Result?.let { Text("SpO2: ${it.spo2.toInt()}%", color = MaterialTheme.colorScheme.tertiary) }
                Text("Confidence: ${(measurement.confidence * 100).toInt()}%", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun RppgWaveformThumbnail(
    signal: FloatArray,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            if (signal.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val padding = 10f

            // Draw simplified grid
            for (i in 0..2) {
                val y = padding + (height - 2 * padding) * i / 2
                drawLine(gridColor, Offset(padding, y), Offset(width - padding, y), strokeWidth = 1.dp.toPx())
            }
            for (i in 0..4) {
                val x = padding + (width - 2 * padding) * i / 4
                drawLine(gridColor, Offset(x, padding), Offset(x, height - padding), strokeWidth = 1.dp.toPx())
            }

            // Draw waveform
            val minValue = signal.minOrNull() ?: 0f
            val maxValue = signal.maxOrNull() ?: 1f
            val range = maxValue - minValue.takeIf { it != 0f }!! ?: 1f
            val path = Path()
            val stepX = (width - 2 * padding) / (signal.size - 1).coerceAtLeast(1)

            signal.forEachIndexed { index, value ->
                val normalized = (value - minValue) / range
                val x = padding + index * stepX
                val y = height - padding - (normalized * (height - 2 * padding))
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = primaryColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 1.5.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
    }
}