package com.codelab.basiclayouts.ui.screen.physnet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelab.basiclayouts.viewModel.physnet.AccelerometerViewModel
import com.codelab.basiclayouts.viewModel.physnet.AccelerometerViewModelFactory
import com.codelab.basiclayouts.viewModel.physnet.EnhancedRppgViewModel

/**
 * Enhanced rPPG Screen with HRV, SpO2 support and Motion Detection
 * 修改：移除重复的依赖项创建，使用传入的ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun RppgScreen(
    modifier: Modifier = Modifier,
    viewModel: EnhancedRppgViewModel, // 强制要求传入 ViewModel，避免重复创建
    onBackClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 移除重复的依赖项创建！
    // 不再创建 videoRecorder, rppgProcessor, repository
    // 直接使用ViewModel中的依赖项

    // Create Accelerometer ViewModel (这个是独立的，不会导致重复初始化问题)
    val accelerometerViewModel: AccelerometerViewModel = viewModel(
        factory = AccelerometerViewModelFactory(context.applicationContext)
    )

    // Collect states
    val uiState by viewModel.uiState.collectAsState()
    val motionState by accelerometerViewModel.motionState.collectAsState()

    // State for settings dialog
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Gradient background
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "rPPG Monitor",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            enabled = true
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            enabled = true
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = onHistoryClick,
                            enabled = true
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = "History",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Camera preview card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Box {
                        // 使用ViewModel中的videoRecorder，而不是重新创建
                        // 这是关键修改：直接使用viewModel.getVideoRecorder()
                        RppgCameraPreview(
                            videoRecorder = viewModel.getVideoRecorder(),
                            lifecycleOwner = lifecycleOwner,
                            onFaceAlignmentChanged = viewModel::updateFaceAlignment,
                            accelerometerViewModel = accelerometerViewModel
                        )

                        // Face alignment indicator
                        if (!uiState.isFaceAligned && !uiState.isRecording) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(16.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Please align face within frame",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }

                        // Analysis mode indicator
                        if (uiState.analysisMode != EnhancedRppgViewModel.AnalysisMode.HEART_RATE_ONLY) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = when (uiState.analysisMode) {
                                        EnhancedRppgViewModel.AnalysisMode.HRV_ONLY -> "HRV"
                                        EnhancedRppgViewModel.AnalysisMode.SPO2_ONLY -> "SpO₂"
                                        EnhancedRppgViewModel.AnalysisMode.ALL -> "Full Analysis"
                                        else -> ""
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recording control area
                AnimatedContent(
                    targetState = uiState.isRecording,
                    transitionSpec = {
                        fadeIn() + scaleIn() with fadeOut() + scaleOut()
                    }
                ) { isRecording ->
                    if (isRecording) {
                        RecordingIndicator(
                            progress = uiState.recordingProgress,
                            timeSeconds = uiState.recordingTimeSeconds,
                            onStop = viewModel::stopRecording,
                            analysisMode = uiState.analysisMode
                        )
                    } else {
                        RecordButton(
                            enabled = !uiState.isProcessing &&
                                    uiState.isFaceAligned &&
                                    motionState.isStationary &&
                                    motionState.isDetectionActive,
                            onClick = {
                                if (accelerometerViewModel.isReadyForMeasurement()) {
                                    viewModel.startRecording()
                                }
                            },
                            motionReady = accelerometerViewModel.isReadyForMeasurement()
                        )
                    }
                }

                // Motion status info card
                AnimatedVisibility(
                    visible = motionState.isDetectionActive,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    MotionStatusCard(
                        motionState = motionState,
                        motionDescription = accelerometerViewModel.getMotionDescription(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Enhanced result display
                AnimatedVisibility(
                    visible = uiState.currentResult != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    uiState.currentResult?.let { result ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            EnhancedHealthMonitoringCard(
                                result = result,
                                showHrvDetails = uiState.showHrvDetails,
                                showSpO2Details = uiState.showSpO2Details,
                                onToggleHrvDetails = viewModel::toggleHrvDetails,
                                onToggleSpO2Details = viewModel::toggleSpO2Details,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )

                            RppgWaveform(
                                signal = result.rppgSignal.toFloatArray(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(horizontal = 16.dp)
                            )

                            if (result.hrvResult?.isValid == true || result.spo2Result?.isValid == true) {
                                HealthRecommendationCard(
                                    result = result,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }

                // Error message
                uiState.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Settings dialog for analysis mode
        if (showSettingsDialog) {
            AnalysisModeDialog(
                currentMode = uiState.analysisMode,
                onModeSelected = { mode ->
                    viewModel.setAnalysisMode(mode)
                    showSettingsDialog = false
                },
                onDismiss = { showSettingsDialog = false }
            )
        }

        // Loading overlay
        if (uiState.isLoading) {
            RppgLoadingOverlay(
                message = uiState.loadingMessage
            )
        }
    }
}

/**
 * Motion Status Information Card
 */
@Composable
private fun MotionStatusCard(
    motionState: AccelerometerViewModel.MotionState,
    motionDescription: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (motionState.isStationary) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Motion Detection",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = motionDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!motionState.isStationary) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Motion Level: ${String.format("%.2f", motionState.motionLevel)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Enhanced Record Button with motion awareness
 */
@Composable
private fun RecordButton(
    enabled: Boolean,
    onClick: () -> Unit,
    motionReady: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val buttonColor = if (motionReady) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .size(80.dp)
                .scale(animatedScale),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = buttonColor
            )
        ) {
            Icon(
                imageVector = Icons.Default.FiberManualRecord,
                contentDescription = "Start Recording",
                modifier = Modifier.size(40.dp)
            )
        }

        Text(
            text = if (motionReady) {
                "Ready to Record"
            } else {
                "Waiting for Stillness"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Recording Indicator
 */
@Composable
private fun RecordingIndicator(
    progress: Float,
    timeSeconds: Int,
    onStop: () -> Unit,
    analysisMode: EnhancedRppgViewModel.AnalysisMode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(100.dp),
                strokeWidth = 8.dp,
                color = MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.errorContainer
            )

            FilledIconButton(
                onClick = onStop,
                modifier = Modifier.size(60.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop Recording",
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Text(
            text = "$timeSeconds s / 20 s",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = when (analysisMode) {
                EnhancedRppgViewModel.AnalysisMode.HEART_RATE_ONLY -> "Measuring Heart Rate"
                EnhancedRppgViewModel.AnalysisMode.HRV_ONLY -> "Analyzing Heart Rate Variability"
                EnhancedRppgViewModel.AnalysisMode.SPO2_ONLY -> "Measuring Blood Oxygen Saturation"
                EnhancedRppgViewModel.AnalysisMode.ALL -> "Performing Full Analysis"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PulsingDot()
    }
}

/**
 * Pulsing Indicator Dot
 */
@Composable
private fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(16.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error)
    )
}

/**
 * Analysis Mode Selection Dialog
 */
@Composable
private fun AnalysisModeDialog(
    currentMode: EnhancedRppgViewModel.AnalysisMode,
    onModeSelected: (EnhancedRppgViewModel.AnalysisMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Analysis Mode") },
        text = {
            Column {
                EnhancedRppgViewModel.AnalysisMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentMode == mode,
                            onClick = { onModeSelected(mode) }
                        )
                        Text(
                            text = when (mode) {
                                EnhancedRppgViewModel.AnalysisMode.HEART_RATE_ONLY -> "Heart Rate Only"
                                EnhancedRppgViewModel.AnalysisMode.HRV_ONLY -> "Heart Rate Variability Only"
                                EnhancedRppgViewModel.AnalysisMode.SPO2_ONLY -> "Blood Oxygen Saturation Only"
                                EnhancedRppgViewModel.AnalysisMode.ALL -> "Full Analysis"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Health Recommendation Card
 */
@Composable
private fun HealthRecommendationCard(
    result: com.codelab.basiclayouts.data.physnet.model.EnhancedRppgResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Health Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // HRV recommendations
            result.hrvResult?.let { hrv ->
                if (hrv.isValid) {
                    Text(
                        text = "• ${hrv.getHealthStatus().description}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    when (hrv.getStressLevel()) {
                        com.codelab.basiclayouts.data.physnet.model.StressLevel.HIGH,
                        com.codelab.basiclayouts.data.physnet.model.StressLevel.VERY_HIGH -> {
                            Text(
                                text = "• Consider resting, deep breathing, or meditation",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        else -> {
                            Text(
                                text = "• Maintain healthy lifestyle habits",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // SpO2 recommendations
            result.spo2Result?.let { spo2 ->
                if (spo2.isValid) {
                    when (spo2.getHealthStatus()) {
                        com.codelab.basiclayouts.data.physnet.model.SpO2HealthStatus.MILD_HYPOXEMIA,
                        com.codelab.basiclayouts.data.physnet.model.SpO2HealthStatus.MODERATE_HYPOXEMIA,
                        com.codelab.basiclayouts.data.physnet.model.SpO2HealthStatus.SEVERE_HYPOXEMIA -> {
                            Text(
                                text = "• ${spo2.getHealthStatus().description}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        else -> {
                            Text(
                                text = "• Blood oxygen level is normal",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Disclaimer
            if (result.spo2Result?.isValid == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: These measurements are for reference only. Consult a doctor for health concerns.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

///**
// * Enhanced Loading Overlay
// */
//@Composable
//private fun RppgLoadingOverlay(
//    message: String,
//    modifier: Modifier = Modifier
//) {
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//            .background(Color.Black.copy(alpha = 0.7f)),
//        contentAlignment = Alignment.Center
//    ) {
//        Card(
//            modifier = Modifier.padding(32.dp),
//            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//        ) {
//            Column(
//                modifier = Modifier.padding(24.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                CircularProgressIndicator()
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Text(
//                    text = message,
//                    style = MaterialTheme.typography.bodyLarge,
//                    textAlign = TextAlign.Center
//                )
//            }
//        }
//    }
//}