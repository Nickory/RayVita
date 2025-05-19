package com.codelab.basiclayouts.ui.scan

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MeasurementToggleButton(isMeasuring: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onToggle,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = if (isMeasuring) "Stop Measurement" else "Start Measurement",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
