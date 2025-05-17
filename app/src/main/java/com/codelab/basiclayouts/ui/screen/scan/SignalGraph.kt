package com.codelab.basiclayouts.ui.scan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SignalGraph(
    signalBuffer: List<Float>, // <-- 改名以匹配调用方式
    modifier: Modifier = Modifier,
    title: String = "Signal Graph"
) {
    val gradient = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF4D9DE0),
                Color(0xFF9BC1BC),
                Color(0xFFED6A5A)
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (signalBuffer.isEmpty()) return@Canvas

                val path = Path()
                val minVal = signalBuffer.minOrNull() ?: 0f
                val maxVal = signalBuffer.maxOrNull() ?: 1f
                val range = (maxVal - minVal).coerceAtLeast(1f)

                val width = size.width
                val height = size.height
                val step = width / (signalBuffer.size - 1).coerceAtLeast(1)

                signalBuffer.forEachIndexed { index, value ->
                    val x = step * index
                    val normalized = (value - minVal) / range
                    val y = height - (normalized * height * 0.8f + height * 0.1f)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    brush = gradient,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
