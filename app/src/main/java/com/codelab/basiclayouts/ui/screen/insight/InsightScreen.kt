package com.codelab.basiclayouts.ui.insight

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.NetworkWifi1Bar
import androidx.compose.material.icons.filled.NetworkWifi2Bar
import androidx.compose.material.icons.filled.NetworkWifi3Bar
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.utils.StepCounterPermissionHelper
import com.codelab.basiclayouts.viewmodel.insight.AIConversationViewModel
import com.codelab.basiclayouts.viewmodel.insight.AIConversationViewModelFactory
import com.codelab.basiclayouts.viewmodel.insight.AITipMode
import com.codelab.basiclayouts.viewmodel.insight.InsightViewModel
import com.codelab.basiclayouts.viewmodel.insight.InsightViewModelFactory
import kotlin.math.roundToInt

@Composable
fun InsightScreen(
    viewModel: InsightViewModel = viewModel(factory = InsightViewModelFactory(LocalContext.current)),
    selectedTab: Int = 0,
    onTabSelect: (Int) -> Unit = {},
    stepCounterPermissionHelper: StepCounterPermissionHelper
) {
    val context = LocalContext.current
    val aiViewModel: AIConversationViewModel = viewModel(factory = AIConversationViewModelFactory(context))

    // 权限相关状态
    val stepPermissionRequired by viewModel.stepPermissionRequired.collectAsState()

    // Real data states
    val realTimeHealthStatus by viewModel.realTimeHealthStatus.collectAsState()
    val aiTip by viewModel.aiPrompt.collectAsState()
    val aiTipMode by viewModel.aiTipMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val todayDate by viewModel.todayDate.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val activityData by viewModel.activityData.collectAsState()
    val currentSteps by viewModel.currentSteps.collectAsState()

    var showHistoryScreen by remember { mutableStateOf(false) }
    var showChatScreen by remember { mutableStateOf(false) }
    var showPermissionPrompt by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 权限检查效果
    LaunchedEffect(stepPermissionRequired) {
        showPermissionPrompt = stepPermissionRequired
    }

    if (showHistoryScreen) {
        HealthHistoryScreen(viewModel = viewModel, onBackPressed = { showHistoryScreen = false })
        return
    }

    if (showChatScreen) {
        AIConversationScreen(viewModel = aiViewModel, onBackPressed = { showChatScreen = false })
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 权限请求提示（如果需要）
            if (showPermissionPrompt) {
                StepCounterPermissionPrompt(
                    onRequestPermission = {
                        stepCounterPermissionHelper.requestPermission(
                            onGranted = {
                                viewModel.onStepPermissionGranted()
                                showPermissionPrompt = false
                            },
                            onDenied = {
                                showPermissionPrompt = false
                            }
                        )
                    },
                    onDismiss = {
                        showPermissionPrompt = false
                    }
                )
            }

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.health_insights),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = todayDate,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            // Real-time Health Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Monitor,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.current_health_status),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = { showHistoryScreen = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = stringResource(R.string.view_history),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.history_records))
                            }
                        }

                        if (realTimeHealthStatus.measurementCount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                RealTimeMetricDisplay(
                                    value = realTimeHealthStatus.currentHeartRate.roundToInt().toString(),
                                    unit = stringResource(R.string.unit_bpm),
                                    label = stringResource(R.string.heart_rate),
                                    status = realTimeHealthStatus.heartRateStatus,
                                    statusColor = getHeartRateColor(realTimeHealthStatus.heartRateStatus),
                                    icon = Icons.Default.Favorite,
                                    backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    iconColor = MaterialTheme.colorScheme.error
                                )

                                if (realTimeHealthStatus.currentHRV > 0) {
                                    RealTimeMetricDisplay(
                                        value = realTimeHealthStatus.currentHRV.roundToInt().toString(),
                                        unit = stringResource(R.string.unit_ms),
                                        label = stringResource(R.string.heart_rate_variability),
                                        status = realTimeHealthStatus.hrvStatus,
                                        statusColor = getHRVColor(realTimeHealthStatus.hrvStatus),
                                        icon = Icons.Default.HealthAndSafety,
                                        backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                        iconColor = MaterialTheme.colorScheme.secondary
                                    )
                                } else {
                                    RealTimeMetricDisplay(
                                        value = "",
                                        unit = "",
                                        label = stringResource(R.string.signal),
                                        status = realTimeHealthStatus.signalQuality,
                                        statusColor = getSignalQualityColor(realTimeHealthStatus.signalQuality),
                                        icon = getSignalQualityIcon(realTimeHealthStatus.signalQuality),
                                        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        iconColor = MaterialTheme.colorScheme.primary,
                                        showIconOnly = true
                                    )
                                }

                                if (realTimeHealthStatus.currentSpO2 > 0) {
                                    RealTimeMetricDisplay(
                                        value = realTimeHealthStatus.currentSpO2.roundToInt().toString(),
                                        unit = "%",
                                        label = stringResource(R.string.blood_oxygen),
                                        status = realTimeHealthStatus.spo2Status,
                                        statusColor = getSpO2Color(realTimeHealthStatus.spo2Status),
                                        icon = Icons.Default.Monitor,
                                        backgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                        iconColor = MaterialTheme.colorScheme.tertiary
                                    )
                                } else {
                                    RealTimeMetricDisplay(
                                        value = "$batteryLevel%",
                                        unit = "",
                                        label = stringResource(R.string.battery),
                                        status = stringResource(R.string.hours_remaining, batteryLevel / 5),
                                        statusColor = MaterialTheme.colorScheme.tertiary,
                                        icon = Icons.Default.Monitor,
                                        backgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                        iconColor = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.last_update, realTimeHealthStatus.lastUpdateTime),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(R.string.today_measurements, realTimeHealthStatus.measurementCount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(R.string.no_measurement_data),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(R.string.start_measurement_prompt),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Enhanced AI Tip Card with Mode Selection
            EnhancedTipCard(
                tip = aiTip,
                isLoading = isLoading,
                currentMode = aiTipMode,
                onModeChange = { mode -> viewModel.setAITipMode(mode) },
                onRefresh = { viewModel.requestInsightPrompt() },
                onChatClick = { showChatScreen = true }
            )

            // Real Steps & Activity Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.activity_tracking),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (stepPermissionRequired) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = stringResource(R.string.permission_required),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.today_steps),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentSteps.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (stepPermissionRequired) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.estimated),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.target_steps),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.completion_percentage, ((currentSteps / 10000f) * 100).roundToInt()),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        activityData.forEach { activity ->
                            ActivityBar(
                                day = activity.day,
                                percentage = activity.percentage,
                                color = MaterialTheme.colorScheme.primary,
                                isToday = activity.isToday,
                                steps = activity.steps
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.week_total_steps, activityData.sumOf { it.steps }),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RealTimeMetricDisplay(
    value: String,
    unit: String,
    label: String,
    status: String,
    statusColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    showIconOnly: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            if (showIconOnly) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
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
                    if (unit.isNotEmpty()) {
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodySmall,
                            color = iconColor
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = getLocalizedStatus(status),
            style = MaterialTheme.typography.bodySmall,
            color = statusColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActivityBar(day: String, percentage: Float, color: Color, isToday: Boolean = false, steps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = getLocalizedDayName(day),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(40.dp),
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage.coerceAtMost(1f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isToday) MaterialTheme.colorScheme.primary else color.copy(alpha = 0.6f))
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when {
                steps >= 1000 -> stringResource(R.string.steps_k_format, steps / 1000)
                else -> steps.toString()
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(40.dp),
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

// 修复后的 AI 模式图标函数
fun getModeIcon(mode: AITipMode): ImageVector {
    return when (mode) {
        AITipMode.DIAGNOSTIC -> Icons.Default.LocalHospital       // 诊断模式 - 医院图标
        AITipMode.COMPREHENSIVE -> Icons.Default.Psychology       // 综合模式 - 心理学/大脑图标
        AITipMode.EXERCISE -> Icons.Default.FitnessCenter        // 运动模式 - 健身图标
        AITipMode.LIFESTYLE -> Icons.Default.Lightbulb           // 生活方式 - 灯泡图标
        AITipMode.PREVENTION -> Icons.Default.Security           // 预防模式 - 安全/盾牌图标
    }
}

@Composable
fun EnhancedTipCard(
    tip: String,
    isLoading: Boolean,
    currentMode: AITipMode,
    onModeChange: (AITipMode) -> Unit,
    onRefresh: () -> Unit,
    onChatClick: () -> Unit
) {
    var showModeMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TipsAndUpdates,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ai_health_assistant),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))

                Box {
                    Card(
                        modifier = Modifier
                            .clickable { showModeMenu = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = getModeIcon(currentMode),  // 直接传递枚举值
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = getAITipModeText(currentMode),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Icon(
                                imageVector = if (showModeMenu)
                                    Icons.Default.ExpandLess
                                else
                                    Icons.Default.ExpandMore,
                                contentDescription = stringResource(R.string.select_mode),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showModeMenu,
                        onDismissRequest = { showModeMenu = false },
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        AITipMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = getModeIcon(mode),  // 直接传递枚举值
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (mode == currentMode)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Column {
                                            Text(
                                                text = getAITipModeText(mode),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (mode == currentMode)
                                                    FontWeight.Bold
                                                else
                                                    FontWeight.Normal,
                                                color = if (mode == currentMode)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = getAITipModeDescription(mode),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (mode == currentMode) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = stringResource(R.string.selected),
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onModeChange(mode)
                                    showModeMenu = false
                                },
                                modifier = Modifier
                                    .background(
                                        if (mode == currentMode)
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                                        else
                                            Color.Transparent
                                    )
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.width(40.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                        Text(
                            text = stringResource(R.string.ai_thinking),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRefresh,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.new_suggestion), fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = onChatClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = stringResource(R.string.chat),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.chat_with_ai), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun StepCounterPermissionPrompt(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsRun,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = stringResource(R.string.step_counter_permission),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = stringResource(R.string.step_counter_permission_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.later))
                }

                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.grant_permission))
                }
            }
        }
    }
}

// Helper functions for localization
@Composable
private fun getLocalizedStatus(status: String): String {
    return when (status) {
        "Low" -> stringResource(R.string.status_low)
        "High" -> stringResource(R.string.status_high)
        "Normal" -> stringResource(R.string.status_normal)
        "Excellent" -> stringResource(R.string.status_excellent)
        "Good" -> stringResource(R.string.status_good)
        "Fair" -> stringResource(R.string.status_fair)
        "Poor" -> stringResource(R.string.status_poor)
        else -> status
    }
}

@Composable
private fun getLocalizedDayName(day: String): String {
    return when (day) {
        "Mon" -> stringResource(R.string.day_monday)
        "Tue" -> stringResource(R.string.day_tuesday)
        "Wed" -> stringResource(R.string.day_wednesday)
        "Thu" -> stringResource(R.string.day_thursday)
        "Fri" -> stringResource(R.string.day_friday)
        "Sat" -> stringResource(R.string.day_saturday)
        "Sun" -> stringResource(R.string.day_sunday)
        else -> day
    }
}

@Composable
private fun getAITipModeText(mode: AITipMode): String {
    return when (mode) {
        AITipMode.DIAGNOSTIC -> stringResource(R.string.mode_diagnostic)
        AITipMode.COMPREHENSIVE -> stringResource(R.string.mode_comprehensive)
        AITipMode.EXERCISE -> stringResource(R.string.mode_exercise)
        AITipMode.LIFESTYLE -> stringResource(R.string.mode_lifestyle)
        AITipMode.PREVENTION -> stringResource(R.string.mode_prevention)
    }
}

@Composable
private fun getAITipModeDescription(mode: AITipMode): String {
    return when (mode) {
        AITipMode.DIAGNOSTIC -> stringResource(R.string.mode_diagnostic_desc)
        AITipMode.COMPREHENSIVE -> stringResource(R.string.mode_comprehensive_desc)
        AITipMode.EXERCISE -> stringResource(R.string.mode_exercise_desc)
        AITipMode.LIFESTYLE -> stringResource(R.string.mode_lifestyle_desc)
        AITipMode.PREVENTION -> stringResource(R.string.mode_prevention_desc)
    }
}

// Color functions - now check for English status values
private fun getHeartRateColor(status: String): Color {
    return when (status) {
        "Low" -> Color(0xFF2196F3)
        "High" -> Color(0xFFE53E3E)
        "Normal" -> Color(0xFF38A169)
        else -> Color.Gray
    }
}

private fun getHRVColor(status: String): Color {
    return when (status) {
        "Poor" -> Color(0xFFE53E3E)
        "Fair" -> Color(0xFFD69E2E)
        "Good" -> Color(0xFF38A169)
        "Excellent" -> Color(0xFF00B5D6)
        else -> Color.Gray
    }
}

private fun getSpO2Color(status: String): Color {
    return when (status) {
        "Low" -> Color(0xFFE53E3E)
        "Normal" -> Color(0xFF38A169)
        "Excellent" -> Color(0xFF00B5D6)
        else -> Color.Gray
    }
}

private fun getSignalQualityColor(status: String): Color {
    return when (status) {
        "Excellent" -> Color(0xFF00B5D6)
        "Good" -> Color(0xFF38A169)
        "Fair" -> Color(0xFFD69E2E)
        "Poor" -> Color(0xFFE53E3E)
        else -> Color.Gray
    }
}

private fun getSignalQualityIcon(status: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (status) {
        "Excellent" -> Icons.Default.NetworkWifi
        "Good" -> Icons.Default.NetworkWifi3Bar
        "Fair" -> Icons.Default.NetworkWifi2Bar
        "Poor" -> Icons.Default.NetworkWifi1Bar
        else -> Icons.Default.NetworkWifi1Bar
    }
}