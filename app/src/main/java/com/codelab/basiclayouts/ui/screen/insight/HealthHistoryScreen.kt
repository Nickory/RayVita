package com.codelab.basiclayouts.ui.insight

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelab.basiclayouts.ui.screen.insight.HRVColorPalette
import com.codelab.basiclayouts.ui.screen.insight.HealthHistoryDataProcessor
import com.codelab.basiclayouts.ui.screen.insight.HealthStatistics
import com.codelab.basiclayouts.ui.screen.insight.HeartRateColorPalette
import com.codelab.basiclayouts.ui.screen.insight.VisualizationMode
import com.codelab.basiclayouts.ui.screen.insight.rememberHealthHistoryData
import com.codelab.basiclayouts.viewmodel.insight.InsightViewModel
import com.codelab.basiclayouts.viewmodel.insight.PhysNetMeasurementData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// 时间视图模式枚举
enum class TimeViewMode {
    DAY, WEEK, MONTH
}
@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthHistoryScreen(viewModel: InsightViewModel, onBackPressed: () -> Unit) {
    val dataState = rememberHealthHistoryData(viewModel)
    val availableDates = dataState.getAvailableDates()
    var selectedDate by remember {
        mutableStateOf(availableDates.firstOrNull() ?: Calendar.getInstance().time)
    }
    var timeViewMode by remember { mutableStateOf(TimeViewMode.DAY) }
    var visualizationMode by remember { mutableStateOf(VisualizationMode.HEART_RATE) }

    // 根据时间视图模式获取数据
    val displayData = when (timeViewMode) {
        TimeViewMode.DAY -> dataState.getMeasurementsForDate(selectedDate)
        TimeViewMode.WEEK -> dataState.getMeasurementsForWeek(selectedDate)
        TimeViewMode.MONTH -> dataState.getMeasurementsForMonth(selectedDate)
    }

    // 新增：SnackBar 相关状态
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val syncResult by viewModel.syncResult.collectAsState()

    // 监听同步结果并显示 SnackBar
    syncResult?.let { result ->
        LaunchedEffect(result) {
            coroutineScope.launch {
                val totalUpdated = result.uploadedCount + result.downloadedCount
                val message = when {
                    result.message.contains("同步成功") -> {
                        "Synced successfully, updated $totalUpdated measurements"
                    }
                    result.message.contains("下载云数据失败") -> {
                        "Uploaded ${result.uploadedCount} measurements, download failed: ${result.message.substringAfter(":")}"
                    }
                    else -> result.message
                }
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = androidx.compose.material3.SnackbarDuration.Long // 4 秒
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Health History",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { dataState.refreshFromCloud() }
                    ) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = "Refresh from Cloud",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = data.visuals.message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 时间视图模式切换器
            TimeViewModeSelector(
                selectedMode = timeViewMode,
                onModeChanged = { timeViewMode = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // 日期选择器（适配不同视图模式）
            EnhancedDateSelector(
                selectedDate = selectedDate,
                availableDates = availableDates,
                timeViewMode = timeViewMode,
                onDateChanged = { selectedDate = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // 增强的可视化图表
            if (displayData.isNotEmpty()) {
                EnhancedVisualizationChart(
                    measurements = displayData,
                    mode = visualizationMode,
                    timeViewMode = timeViewMode,
                    onModeChanged = { visualizationMode = it },
                    dataProcessor = dataState.processor,
                    selectedDate = selectedDate,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 测量记录列表（只在日视图显示详细卡片）
            when (timeViewMode) {
                TimeViewMode.DAY -> {
                    if (displayData.isEmpty()) {
                        EmptyStateView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp)
                        ) {
                            items(displayData) { measurement ->
                                EnhancedMeasurementCard(measurement)
                            }
                        }
                    }
                }
                TimeViewMode.WEEK, TimeViewMode.MONTH -> {
                    WeekMonthSummaryView(
                        measurements = displayData,
                        timeViewMode = timeViewMode,
                        selectedDate = selectedDate,
                        dataProcessor = dataState.processor,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TimeViewModeSelector(
    selectedMode: TimeViewMode,
    onModeChanged: (TimeViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimeViewModeChip(
                icon = Icons.Default.ViewDay,
                label = "Day",
                isSelected = selectedMode == TimeViewMode.DAY,
                onClick = { onModeChanged(TimeViewMode.DAY) }
            )

            TimeViewModeChip(
                icon = Icons.Default.ViewWeek,
                label = "Week",
                isSelected = selectedMode == TimeViewMode.WEEK,
                onClick = { onModeChanged(TimeViewMode.WEEK) }
            )

            TimeViewModeChip(
                icon = Icons.Default.CalendarMonth,
                label = "Month",
                isSelected = selectedMode == TimeViewMode.MONTH,
                onClick = { onModeChanged(TimeViewMode.MONTH) }
            )
        }
    }
}

@Composable
fun TimeViewModeChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.height(36.dp)
    )
}

@Composable
fun EnhancedDateSelector(
    selectedDate: Date,
    availableDates: List<Date>,
    timeViewMode: TimeViewMode,
    onDateChanged: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
    val processor = HealthHistoryDataProcessor()
    val sortedDates = availableDates.sortedBy { it.time }
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 标题显示根据模式调整
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        when (timeViewMode) {
                            TimeViewMode.DAY -> {
                                val dateInfo = processor.formatCalendarDate(selectedDate)
                                "${dateInfo.month} ${dateInfo.day}, ${dateInfo.year}"
                            }
                            TimeViewMode.WEEK -> {
                                val weekRange = processor.getWeekRange(selectedDate)
                                "Week: ${weekRange.first} - ${weekRange.second}"
                            }
                            TimeViewMode.MONTH -> {
                                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate)
                            }
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!isExpanded) {
                        IconButton(
                            onClick = {
                                val prevDate = when (timeViewMode) {
                                    TimeViewMode.DAY -> processor.getPreviousDay(selectedDate, sortedDates)
                                    TimeViewMode.WEEK -> processor.getPreviousWeek(selectedDate)
                                    TimeViewMode.MONTH -> processor.getPreviousMonth(selectedDate)
                                }
                                prevDate?.let { onDateChanged(it) }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Previous",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val nextDate = when (timeViewMode) {
                                    TimeViewMode.DAY -> processor.getNextDay(selectedDate, sortedDates)
                                    TimeViewMode.WEEK -> processor.getNextWeek(selectedDate)
                                    TimeViewMode.MONTH -> processor.getNextMonth(selectedDate)
                                }
                                nextDate?.let { onDateChanged(it) }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Next",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Calendar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 可展开的日期选择
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(sortedDates.take(15)) { date ->
                            val isSelected = when (timeViewMode) {
                                TimeViewMode.DAY -> processor.isSameDay(date, selectedDate)
                                TimeViewMode.WEEK -> processor.isSameWeek(date, selectedDate)
                                TimeViewMode.MONTH -> processor.isSameMonth(date, selectedDate)
                            }
                            val dateInfo = processor.formatCalendarDate(date)

                            Box(
                                modifier = Modifier
                                    .clickable { onDateChanged(date) }
                                    .size(width = 44.dp, height = 52.dp)
                                    .background(
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        dateInfo.dayOfWeek,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 8.sp,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        dateInfo.day,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedVisualizationChart(
    measurements: List<PhysNetMeasurementData>,
    mode: VisualizationMode,
    timeViewMode: TimeViewMode,
    onModeChanged: (VisualizationMode) -> Unit,
    dataProcessor: HealthHistoryDataProcessor,
    selectedDate: Date,
    modifier: Modifier = Modifier
) {
    val statistics = dataProcessor.processHealthStatistics(measurements)
    val sortedMeasurements = measurements.sortedBy { it.timestamp }

    // 创建颜色调色板
    val hrPalette = dataProcessor.createHeartRateColorPalette(
        primaryColor = MaterialTheme.colorScheme.primary,
        tertiaryColor = MaterialTheme.colorScheme.tertiary,
        errorColor = MaterialTheme.colorScheme.error,
        primaryContainerColor = MaterialTheme.colorScheme.primaryContainer,
        tertiaryContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
        errorContainerColor = MaterialTheme.colorScheme.errorContainer
    )

    val hrvPalette = dataProcessor.createHRVColorPalette(
        primaryColor = MaterialTheme.colorScheme.primary,
        secondaryColor = MaterialTheme.colorScheme.secondary,
        tertiaryColor = MaterialTheme.colorScheme.tertiary,
        primaryContainerColor = MaterialTheme.colorScheme.primaryContainer,
        secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        tertiaryContainerColor = MaterialTheme.colorScheme.tertiaryContainer
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题和模式切换
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        when (mode) {
                            VisualizationMode.HEART_RATE -> "Heart Rate"
                            VisualizationMode.HRV -> "Heart Rate Variability"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                    Text(
                        "${statistics.measurementCount} measurements",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = mode == VisualizationMode.HEART_RATE,
                        onClick = { onModeChanged(VisualizationMode.HEART_RATE) },
                        label = {
                            Text(
                                "HR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.height(28.dp)
                    )

                    FilterChip(
                        selected = mode == VisualizationMode.HRV,
                        onClick = { onModeChanged(VisualizationMode.HRV) },
                        label = {
                            Text(
                                "HRV",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 根据时间视图模式选择不同高度
            val chartHeight = when (timeViewMode) {
                TimeViewMode.DAY -> 70.dp
                TimeViewMode.WEEK -> 120.dp
                TimeViewMode.MONTH -> 150.dp
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
            ) {
                if (sortedMeasurements.isEmpty()) return@Canvas

                when (timeViewMode) {
                    TimeViewMode.DAY -> {
                        when (mode) {
                            VisualizationMode.HEART_RATE -> {
                                drawPremiumHeartRateVisualization(
                                    measurements = sortedMeasurements,
                                    statistics = statistics,
                                    dataProcessor = dataProcessor,
                                    hrPalette = hrPalette
                                )
                            }
                            VisualizationMode.HRV -> {
                                drawPremiumHRVVisualization(
                                    measurements = sortedMeasurements,
                                    statistics = statistics,
                                    dataProcessor = dataProcessor,
                                    hrvPalette = hrvPalette
                                )
                            }
                        }
                    }
                    TimeViewMode.WEEK -> {
                        drawWeeklyBandVisualization(
                            measurements = sortedMeasurements,
                            mode = mode,
                            dataProcessor = dataProcessor,
                            hrPalette = hrPalette,
                            hrvPalette = hrvPalette,
                            selectedDate = selectedDate
                        )
                    }
                    TimeViewMode.MONTH -> {
                        drawMonthlyBandVisualization(
                            measurements = sortedMeasurements,
                            mode = mode,
                            dataProcessor = dataProcessor,
                            hrPalette = hrPalette,
                            hrvPalette = hrvPalette,
                            selectedDate = selectedDate
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 统计信息显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                when (mode) {
                    VisualizationMode.HEART_RATE -> {
                        CompactStatItem("Range", "${statistics.minHR}-${statistics.maxHR}", "BPM")
                        CompactStatItem("Avg", "${statistics.avgHR}", "BPM")
                        CompactStatItem("Peak", "${statistics.maxHR}", "BPM")
                    }
                    VisualizationMode.HRV -> {
                        CompactStatItem("Range", "${statistics.minHRV}-${statistics.maxHRV}", "ms")
                        CompactStatItem("Avg", "${statistics.avgHRV}", "ms")
                        CompactStatItem("Peak", "${statistics.maxHRV}", "ms")
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "${statistics.timeRange.first} - ${statistics.timeRange.second}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 9.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun WeekMonthSummaryView(
    measurements: List<PhysNetMeasurementData>,
    timeViewMode: TimeViewMode,
    selectedDate: Date,
    dataProcessor: HealthHistoryDataProcessor,
    modifier: Modifier = Modifier
) {
    val statistics = dataProcessor.processHealthStatistics(measurements)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                when (timeViewMode) {
                    TimeViewMode.WEEK -> "Weekly Summary"
                    TimeViewMode.MONTH -> "Monthly Summary"
                    else -> "Summary"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatCard("Total", "${statistics.measurementCount}", "Sessions")
                SummaryStatCard("Avg HR", "${statistics.avgHR}", "BPM")
                SummaryStatCard("Avg HRV", "${statistics.avgHRV}", "ms")
            }

            if (statistics.timeRange.first != "--:--") {
                Text(
                    "Period: ${statistics.timeRange.first} - ${statistics.timeRange.second}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun SummaryStatCard(label: String, value: String, unit: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            unit,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CompactStatItem(label: String, value: String, unit: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            Text(
                unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 9.sp
            )
        }
    }
}

// Canvas绘制函数 - 解决阴影裁剪问题的关键是使用正确的容器padding

fun DrawScope.drawPremiumHeartRateVisualization(
    measurements: List<PhysNetMeasurementData>,
    statistics: HealthStatistics,
    dataProcessor: HealthHistoryDataProcessor,
    hrPalette: HeartRateColorPalette
) {
    val sortedMeasurements = measurements.sortedBy { it.timestamp }
    if (sortedMeasurements.isEmpty()) return

    drawPremiumGrid()

    val maxBarHeight = size.height * 0.85f
    val range = (statistics.maxHR - statistics.minHR).coerceAtLeast(1).toFloat()
    val baselineY = size.height - 8.dp.toPx()

    val totalWidth = size.width - 24.dp.toPx()
    val barSpacing = 3.dp.toPx()
    val availableWidth = totalWidth - (sortedMeasurements.size - 1) * barSpacing
    val barWidth = (availableWidth / sortedMeasurements.size).coerceAtLeast(4.dp.toPx())

    // 绘制趋势线
    val trendPath = Path()
    var firstPoint = true

    sortedMeasurements.forEachIndexed { index, measurement ->
        val x = 12.dp.toPx() + index * (barWidth + barSpacing) + barWidth / 2
        val normalizedHeight = ((measurement.heartRate - statistics.minHR) / range) * maxBarHeight
        val y = baselineY - normalizedHeight

        if (firstPoint) {
            trendPath.moveTo(x, y)
            firstPoint = false
        } else {
            trendPath.lineTo(x, y)
        }
    }

    drawPath(
        path = trendPath,
        color = hrPalette.normalZone.copy(alpha = 0.6f),
        style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.cornerPathEffect(8.dp.toPx())
        )
    )

    // 绘制柱状图
    sortedMeasurements.forEachIndexed { index, measurement ->
        val x = 12.dp.toPx() + index * (barWidth + barSpacing)
        val normalizedHeight = ((measurement.heartRate - statistics.minHR) / range) * maxBarHeight
        val y = baselineY - normalizedHeight

        val baseColor = dataProcessor.getHeartRateZoneColor(measurement.heartRate, hrPalette)
        val lightColor = dataProcessor.getHeartRateZoneLightColor(measurement.heartRate, hrPalette)

        // 阴影
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.1f),
            topLeft = Offset(x + 1.dp.toPx(), y + 2.dp.toPx()),
            size = Size(barWidth, normalizedHeight),
            cornerRadius = CornerRadius(3.dp.toPx())
        )

        // 主要柱体
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    lightColor.copy(alpha = 0.9f),
                    baseColor.copy(alpha = 0.8f),
                    baseColor
                ),
                startY = y,
                endY = baselineY
            ),
            topLeft = Offset(x, y),
            size = Size(barWidth, normalizedHeight),
            cornerRadius = CornerRadius(3.dp.toPx())
        )

        // 高光效果
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.4f),
                    Color.Transparent
                ),
                startY = y,
                endY = y + normalizedHeight * 0.3f
            ),
            topLeft = Offset(x, y),
            size = Size(barWidth, normalizedHeight * 0.3f),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
    }
}

fun DrawScope.drawPremiumHRVVisualization(
    measurements: List<PhysNetMeasurementData>,
    statistics: HealthStatistics,
    dataProcessor: HealthHistoryDataProcessor,
    hrvPalette: HRVColorPalette
) {
    val hrvMeasurements = measurements.filter { it.hrvResult != null }.sortedBy { it.timestamp }
    if (hrvMeasurements.isEmpty()) return

    drawPremiumGrid()

    val maxBarHeight = size.height * 0.85f
    val range = (statistics.maxHRV - statistics.minHRV).coerceAtLeast(1).toFloat()
    val baselineY = size.height - 8.dp.toPx()

    val totalWidth = size.width - 24.dp.toPx()
    val barSpacing = 3.dp.toPx()
    val availableWidth = totalWidth - (hrvMeasurements.size - 1) * barSpacing
    val barWidth = (availableWidth / hrvMeasurements.size).coerceAtLeast(4.dp.toPx())

    // 趋势线
    val trendPath = Path()
    var firstPoint = true

    hrvMeasurements.forEachIndexed { index, measurement ->
        val hrvValue = measurement.hrvResult?.rmssd?.toFloat() ?: return@forEachIndexed
        val x = 12.dp.toPx() + index * (barWidth + barSpacing) + barWidth / 2
        val normalizedHeight = ((hrvValue - statistics.minHRV) / range) * maxBarHeight
        val y = baselineY - normalizedHeight

        if (firstPoint) {
            trendPath.moveTo(x, y)
            firstPoint = false
        } else {
            trendPath.lineTo(x, y)
        }
    }

    drawPath(
        path = trendPath,
        color = hrvPalette.normalZone.copy(alpha = 0.6f),
        style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.cornerPathEffect(8.dp.toPx())
        )
    )

    // HRV柱状图
    hrvMeasurements.forEachIndexed { index, measurement ->
        val hrvValue = measurement.hrvResult?.rmssd?.toFloat() ?: return@forEachIndexed
        val x = 12.dp.toPx() + index * (barWidth + barSpacing)
        val normalizedHeight = ((hrvValue - statistics.minHRV) / range) * maxBarHeight
        val y = baselineY - normalizedHeight

        val baseColor = dataProcessor.getHRVZoneColor(hrvValue, hrvPalette)
        val lightColor = dataProcessor.getHRVZoneLightColor(hrvValue, hrvPalette)

        drawRoundRect(
            color = Color.Black.copy(alpha = 0.1f),
            topLeft = Offset(x + 1.dp.toPx(), y + 2.dp.toPx()),
            size = Size(barWidth, normalizedHeight),
            cornerRadius = CornerRadius(3.dp.toPx())
        )

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    lightColor.copy(alpha = 0.9f),
                    baseColor.copy(alpha = 0.8f),
                    baseColor
                ),
                startY = y,
                endY = baselineY
            ),
            topLeft = Offset(x, y),
            size = Size(barWidth, normalizedHeight),
            cornerRadius = CornerRadius(3.dp.toPx())
        )

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.4f),
                    Color.Transparent
                ),
                startY = y,
                endY = y + normalizedHeight * 0.3f
            ),
            topLeft = Offset(x, y),
            size = Size(barWidth, normalizedHeight * 0.3f),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
    }
}

// 新增：周视图带状图绘制
fun DrawScope.drawWeeklyBandVisualization(
    measurements: List<PhysNetMeasurementData>,
    mode: VisualizationMode,
    dataProcessor: HealthHistoryDataProcessor,
    hrPalette: HeartRateColorPalette,
    hrvPalette: HRVColorPalette,
    selectedDate: Date
) {
    if (measurements.isEmpty()) return

    drawPremiumGrid()

    // 按天分组数据
    val weeklyData = dataProcessor.groupMeasurementsByDay(measurements, selectedDate)
    val dayWidth = size.width / 7f
    val maxBandHeight = size.height * 0.8f

    weeklyData.forEachIndexed { dayIndex, dayMeasurements ->
        if (dayMeasurements.isEmpty()) return@forEachIndexed

        val x = dayIndex * dayWidth
        val values = when (mode) {
            VisualizationMode.HEART_RATE -> dayMeasurements.map { it.heartRate }
            VisualizationMode.HRV -> dayMeasurements.mapNotNull { it.hrvResult?.rmssd?.toFloat() }
        }

        if (values.isNotEmpty()) {
            val minValue = values.minOrNull() ?: 0f
            val maxValue = values.maxOrNull() ?: 100f
            val avgValue = values.average().toFloat()

            // 计算全局范围用于归一化
            val globalMin = when (mode) {
                VisualizationMode.HEART_RATE -> 50f
                VisualizationMode.HRV -> 20f
            }
            val globalMax = when (mode) {
                VisualizationMode.HEART_RATE -> 120f
                VisualizationMode.HRV -> 100f
            }
            val globalRange = globalMax - globalMin

            // 归一化值
            val normalizedMin = ((minValue - globalMin) / globalRange).coerceIn(0f, 1f)
            val normalizedMax = ((maxValue - globalMin) / globalRange).coerceIn(0f, 1f)
            val normalizedAvg = ((avgValue - globalMin) / globalRange).coerceIn(0f, 1f)

            // 计算Y坐标
            val baseY = size.height - 10.dp.toPx()
            val minY = baseY - normalizedMin * maxBandHeight
            val maxY = baseY - normalizedMax * maxBandHeight
            val avgY = baseY - normalizedAvg * maxBandHeight

            val bandColor = when (mode) {
                VisualizationMode.HEART_RATE -> dataProcessor.getHeartRateZoneColor(avgValue, hrPalette)
                VisualizationMode.HRV -> dataProcessor.getHRVZoneColor(avgValue, hrvPalette)
            }

            // 绘制范围带
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        bandColor.copy(alpha = 0.3f),
                        bandColor.copy(alpha = 0.1f)
                    ),
                    startY = maxY,
                    endY = minY
                ),
                topLeft = Offset(x + 4.dp.toPx(), maxY),
                size = Size(dayWidth - 8.dp.toPx(), minY - maxY),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // 绘制平均值线
            drawRoundRect(
                color = bandColor,
                topLeft = Offset(x + 4.dp.toPx(), avgY - 1.dp.toPx()),
                size = Size(dayWidth - 8.dp.toPx(), 2.dp.toPx()),
                cornerRadius = CornerRadius(1.dp.toPx())
            )

            // 绘制测量点数指示器
            drawCircle(
                color = bandColor,
                radius = 3.dp.toPx(),
                center = Offset(x + dayWidth / 2, baseY + 6.dp.toPx())
            )
        }
    }
}

// 新增：月视图带状图绘制
fun DrawScope.drawMonthlyBandVisualization(
    measurements: List<PhysNetMeasurementData>,
    mode: VisualizationMode,
    dataProcessor: HealthHistoryDataProcessor,
    hrPalette: HeartRateColorPalette,
    hrvPalette: HRVColorPalette,
    selectedDate: Date
) {
    if (measurements.isEmpty()) return

    drawPremiumGrid()

    // 按周分组数据
    val monthlyData = dataProcessor.groupMeasurementsByWeek(measurements, selectedDate)
    val weekWidth = size.width / 4f // 假设每月4周
    val maxBandHeight = size.height * 0.8f

    monthlyData.forEachIndexed { weekIndex, weekMeasurements ->
        if (weekMeasurements.isEmpty()) return@forEachIndexed

        val x = weekIndex * weekWidth
        val values = when (mode) {
            VisualizationMode.HEART_RATE -> weekMeasurements.map { it.heartRate }
            VisualizationMode.HRV -> weekMeasurements.mapNotNull { it.hrvResult?.rmssd?.toFloat() }
        }

        if (values.isNotEmpty()) {
            val minValue = values.minOrNull() ?: 0f
            val maxValue = values.maxOrNull() ?: 100f
            val avgValue = values.average().toFloat()

            val globalMin = when (mode) {
                VisualizationMode.HEART_RATE -> 50f
                VisualizationMode.HRV -> 20f
            }
            val globalMax = when (mode) {
                VisualizationMode.HEART_RATE -> 120f
                VisualizationMode.HRV -> 100f
            }
            val globalRange = globalMax - globalMin

            val normalizedMin = ((minValue - globalMin) / globalRange).coerceIn(0f, 1f)
            val normalizedMax = ((maxValue - globalMin) / globalRange).coerceIn(0f, 1f)
            val normalizedAvg = ((avgValue - globalMin) / globalRange).coerceIn(0f, 1f)

            val baseY = size.height - 10.dp.toPx()
            val minY = baseY - normalizedMin * maxBandHeight
            val maxY = baseY - normalizedMax * maxBandHeight
            val avgY = baseY - normalizedAvg * maxBandHeight

            val bandColor = when (mode) {
                VisualizationMode.HEART_RATE -> dataProcessor.getHeartRateZoneColor(avgValue, hrPalette)
                VisualizationMode.HRV -> dataProcessor.getHRVZoneColor(avgValue, hrvPalette)
            }

            // 绘制更宽的范围带
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        bandColor.copy(alpha = 0.4f),
                        bandColor.copy(alpha = 0.2f),
                        bandColor.copy(alpha = 0.1f)
                    ),
                    startY = maxY,
                    endY = minY
                ),
                topLeft = Offset(x + 6.dp.toPx(), maxY),
                size = Size(weekWidth - 12.dp.toPx(), minY - maxY),
                cornerRadius = CornerRadius(6.dp.toPx())
            )

            // 平均值线
            drawRoundRect(
                color = bandColor,
                topLeft = Offset(x + 6.dp.toPx(), avgY - 2.dp.toPx()),
                size = Size(weekWidth - 12.dp.toPx(), 4.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )

            // 测量次数指示器
            drawCircle(
                color = bandColor,
                radius = 4.dp.toPx(),
                center = Offset(x + weekWidth / 2, baseY + 8.dp.toPx())
            )
        }
    }
}

fun DrawScope.drawPremiumGrid() {
    val gridColor = Color.Gray.copy(alpha = 0.08f)

    for (i in 1..2) {
        val y = size.height * i / 3
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 0.8.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
        )
    }

    for (i in 1..3) {
        val x = size.width * i / 4
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 0.8.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
        )
    }
}

@Composable
fun EnhancedMeasurementCard(measurement: PhysNetMeasurementData) {
    var isExpanded by remember { mutableStateOf(false) }
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // 关键修复：使用clip和正确的容器padding来避免阴影被裁剪
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)) // 确保容器形状一致
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp) // 稍微增加阴影
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 紧凑标题行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            timeFormat.format(Date(measurement.timestamp)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                measurement.sessionId.take(4),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "${measurement.heartRate.roundToInt()} BPM",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.error
                        )

                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 紧凑指标行
                if (!isExpanded) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        measurement.hrvResult?.let { hrv ->
                            CompactMetricItem(
                                label = "HRV",
                                value = "${hrv.rmssd.roundToInt()}ms",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        measurement.spo2Result?.let { spo2 ->
                            CompactMetricItem(
                                label = "SpO2",
                                value = "${spo2.spo2.roundToInt()}%",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        CompactMetricItem(
                            label = "Confidence",
                            value = "${(measurement.confidence * 100).roundToInt()}%",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 信号可视化
                CompactSignalVisualization(
                    signal = measurement.rppgSignal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isExpanded) 80.dp else 50.dp)
                )

                // 可展开详细信息
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        CompactDetailedMetricsSection(measurement)
                    }
                }
            }
        }
    }
}

@Composable
fun CompactMetricItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(
            "$label: $value",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun CompactSignalVisualization(
    signal: FloatArray,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val processor = HealthHistoryDataProcessor()

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            if (signal.isEmpty()) return@Canvas

            val sampledSignal = processor.sampleSignalData(signal, 80)
            val minValue = sampledSignal.minOrNull() ?: 0f
            val maxValue = sampledSignal.maxOrNull() ?: 1f
            val range = (maxValue - minValue).takeIf { it != 0f } ?: 1f

            val path = Path()
            val gradientPath = Path()
            val stepX = size.width / (sampledSignal.size - 1).coerceAtLeast(1)
            val centerY = size.height / 2f

            sampledSignal.forEachIndexed { index, value ->
                val normalized = (value - minValue) / range
                val x = index * stepX
                val y = centerY + (normalized - 0.5f) * size.height * 0.8f

                if (index == 0) {
                    path.moveTo(x, y)
                    gradientPath.moveTo(x, centerY)
                    gradientPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    gradientPath.lineTo(x, y)
                }

                if (index == sampledSignal.size - 1) {
                    gradientPath.lineTo(x, centerY)
                    gradientPath.close()
                }
            }

            drawPath(
                path = gradientPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.3f),
                        primaryColor.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                )
            )

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Composable
fun CompactDetailedMetricsSection(measurement: PhysNetMeasurementData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Detailed Analysis",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        measurement.hrvResult?.let { hrv ->
            CompactDetailSection(
                title = "Heart Rate Variability",
                color = MaterialTheme.colorScheme.secondary
            ) {
                CompactDetailRow("RMSSD", "${hrv.rmssd.roundToInt()} ms")
                CompactDetailRow("pNN50", "${hrv.pnn50.roundToInt()}%")
                CompactDetailRow("SDNN", "${hrv.sdnn.roundToInt()} ms")
                CompactDetailRow("Stress Index", String.format("%.2f", hrv.stressIndex))
            }
        }

        measurement.spo2Result?.let { spo2 ->
            CompactDetailSection(
                title = "Blood Oxygen",
                color = MaterialTheme.colorScheme.tertiary
            ) {
                CompactDetailRow("SpO2 Level", "${spo2.spo2.roundToInt()}%")
                CompactDetailRow("Confidence", "${(spo2.confidence * 100).roundToInt()}%")
            }
        }

        CompactDetailSection(
            title = "Processing Info",
            color = MaterialTheme.colorScheme.primary
        ) {
            CompactDetailRow("Frames", "${measurement.frameCount}")
            CompactDetailRow("Time", "${measurement.processingTimeMs}ms")
            CompactDetailRow("Confidence", "${(measurement.confidence * 100).roundToInt()}%")
        }
    }
}

@Composable
fun CompactDetailSection(
    title: String,
    color: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = color.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = color
        )

        Spacer(modifier = Modifier.height(6.dp))

        content()
    }
}

@Composable
fun CompactDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyStateView(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Timeline,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "No measurements found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Start taking measurements to view your health insights",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}