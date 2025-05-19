package com.codelab.basiclayouts.ui.scan

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelab.basiclayouts.viewmodel.scan.ScanViewModel

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ScanScreen(viewModel: ScanViewModel<Any> = viewModel()) {
    val heartRate by viewModel.heartRate.collectAsState()
    val signalBuffer by viewModel.signalBuffer.collectAsState()
    val isMeasuring by viewModel.isMeasuring.collectAsState()
    val context = LocalContext.current

    // 调试日志
    Log.d("ScanScreen", "HeartRate: ${heartRate.value}, SignalBuffer size: ${signalBuffer.size}, IsMeasuring: $isMeasuring")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部导航栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        (context as? androidx.activity.ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "rPPG Scan",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            // 相机预览
            CameraPreview(
                isMeasuring = isMeasuring,
                onFaceDetected = { viewModel.onFaceDetected(it) },
                onFrameProcessed = { viewModel.processFrame(it) },
                modifier = Modifier
                    .fillMaxWidth(0.8F)
                    .height(200.dp)
            )

            // 健康指标显示
            HealthMetricCard(heartRate = heartRate.value)

            // 信号图表
            SignalGraph(signalBuffer = signalBuffer)

            // 切换按钮
            MeasurementToggleButton(
                isMeasuring = isMeasuring,
                onToggle = { viewModel.toggleMeasurement() }
            )
        }
    }
}

@Composable
fun PreviewScanScreenContent(
    heartRate: Int = 72,
    signalBuffer: List<Float> = List(50) { i -> (Math.sin(i * 0.2) * 10 + 60).toFloat() },
    isMeasuring: Boolean = true,
    onToggle: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部导航栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "rPPG 扫描",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            // 相机预览（预览模式使用占位符）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("相机预览（占位符）")
            }

            // 健康指标显示
            HealthMetricCard(heartRate = heartRate)

            // 信号图表
            SignalGraph(signalBuffer = signalBuffer)

            // 切换按钮
            MeasurementToggleButton(
                isMeasuring = isMeasuring,
                onToggle = onToggle
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun PreviewScanScreen() {
    PreviewScanScreenContent()
}