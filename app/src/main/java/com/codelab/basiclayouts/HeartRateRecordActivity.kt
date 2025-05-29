package com.codelab.basiclayouts

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.ui.theme.RayVitaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class HeartRateRecordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RayVitaTheme {
                HeartRateRecordScreen()
            }
        }
    }
}

data class HeartRateRecord(
    val value: Int,
    val timestamp: Long,
    val note: String = ""
)

data class HeartRateStatistics(
    val min: Int,
    val max: Int,
    val average: Int,
    val restingRate: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateRecordScreen() {
    val context = LocalContext.current
    val activity = (context as? ComponentActivity)
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Animation states
    var animateContent by remember { mutableStateOf(false) }
    val heartPulseScale by animateFloatAsState(
        targetValue = if (animateContent) 1.1f else 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartbeat"
    )

    // Heart rate data from shared preferences
    val sharedPreferences = remember { context.getSharedPreferences("heart_rate_data", MODE_PRIVATE) }
    val savedDataString = remember { sharedPreferences.getString("data", "") ?: "" }

    // Sample heart rate data - in a real app, you'd load this from a database or shared preferences
    val heartRateData = remember {
        val baseData = savedDataString.split(",").filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }

        // If no data, generate some sample data
        if (baseData.isEmpty()) {
            val cal = Calendar.getInstance()
            List(14) { index ->
                cal.add(Calendar.HOUR, -2)
                val timeMs = cal.timeInMillis
                val randomValue = (65..85).random()
                HeartRateRecord(randomValue, timeMs)
            }.reversed()
        } else {
            val cal = Calendar.getInstance()
            baseData.mapIndexed { index, value ->
                cal.add(Calendar.HOUR, -2 * index)
                HeartRateRecord(value, cal.timeInMillis)
            }.reversed()
        }
    }

    // Statistics
    val statistics = remember(heartRateData) {
        if (heartRateData.isEmpty()) {
            HeartRateStatistics(0, 0, 0, 0)
        } else {
            val values = heartRateData.map { it.value }
            HeartRateStatistics(
                min = values.minOrNull() ?: 0,
                max = values.maxOrNull() ?: 0,
                average = values.average().roundToInt(),
                restingRate = (values.sorted().take(3).average().roundToInt())
            )
        }
    }

    // Time period selection
    val timePeriods = listOf("今日", "本周", "本月", "全部")
    var selectedTimePeriod by remember { mutableStateOf(timePeriods[0]) }

    // Tab selection
    val tabs = listOf("概览", "图表", "数据", "分析")
    var selectedTab by remember { mutableIntStateOf(0) }

    // Settings and controls
    var showExportDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf<String?>(null) }

    // Status dashboard statistics expanded
    var statsExpanded by remember { mutableStateOf(true) }

    // Trigger animations after a delay
    LaunchedEffect(Unit) {
        delay(100)
        animateContent = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("心率数据记录") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "筛选"
                        )
                    }
                    IconButton(onClick = { showExportDialog = true }) {  // 改为使用IconButton
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "导出"
                        )
                    }
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer, // 可根据需要调整
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navItems = listOf(
                    Triple("首页", Icons.Default.Home, "首页"),
                    Triple("数据", Icons.Default.BarChart, "数据中心"),
                    Triple("记录", Icons.Default.Add, "记录心率"),
                    Triple("分析", Icons.Default.Analytics, "健康分析"),
                    Triple("我的", Icons.Default.Person, "个人中心")
                )

                navItems.forEachIndexed { index, (title, icon, description) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = description) },
                        label = { Text(title) },
                        selected = index == 1,
                        onClick = {
                            Toast.makeText(context, "导航到: $title", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("开始记录新的心率数据")
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "记录心率"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Time period selector
            AnimatedVisibility(
                visible = animateContent,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(timePeriods) { index, period ->
                        FilterChip(
                            selected = selectedTimePeriod == period,
                            onClick = {
                                selectedTimePeriod = period
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("查看${period}心率数据")
                                }
                            },
                            label = { Text(period) },
                            leadingIcon = if (selectedTimePeriod == period) {
                                {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else {
                                null
                            }
                        )
                    }
                }
            }

            // Heart rate summary card with animation
            AnimatedVisibility(
                visible = animateContent,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .scale(heartPulseScale),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(60.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "${statistics.average}",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "BPM 平均心率",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Health status based on average heart rate
                        val (statusText, statusColor) = when {
                            statistics.average < 60 -> "心率偏低" to MaterialTheme.colorScheme.tertiary
                            statistics.average > 100 -> "心率偏高" to MaterialTheme.colorScheme.error
                            else -> "心率正常" to MaterialTheme.colorScheme.primary
                        }

                        Surface(
                            color = statusColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Statistics dashboard
            AnimatedVisibility(
                visible = animateContent,
                enter = fadeIn() + slideInVertically { it / 3 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { statsExpanded = !statsExpanded },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "心率统计",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = { statsExpanded = !statsExpanded }
                            ) {
                                Icon(
                                    imageVector = if (statsExpanded)
                                        Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (statsExpanded) "收起" else "展开"
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = statsExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatisticItem(
                                    value = "${statistics.min}",
                                    label = "最低值",
                                    icon = Icons.Default.ArrowDownward,
                                    color = MaterialTheme.colorScheme.tertiary
                                )

                                StatisticItem(
                                    value = "${statistics.max}",
                                    label = "最高值",
                                    icon = Icons.Default.ArrowUpward,
                                    color = MaterialTheme.colorScheme.error
                                )

                                StatisticItem(
                                    value = "${statistics.restingRate}",
                                    label = "静息心率",
                                    icon = Icons.Default.Bedtime,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                StatisticItem(
                                    value = "${kotlin.math.abs(statistics.average - 75)}%",
                                    label = "与正常差",
                                    icon = Icons.Default.ShowChart,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Heart rate chart
            AnimatedVisibility(
                visible = animateContent,
                enter = fadeIn() + slideInVertically { it / 4 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "心率趋势",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "点击查看详情",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    coroutineScope.launch {
                                        selectedTab = 1 // Switch to chart tab
                                        snackbarHostState.showSnackbar("查看详细心率趋势图表")
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Heart rate chart
                        HeartRateChart(
                            heartRateData = heartRateData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clickable {
                                    coroutineScope.launch {
                                        selectedTab = 1 // Switch to chart tab
                                        snackbarHostState.showSnackbar("查看详细心率趋势图表")
                                    }
                                }
                        )
                    }
                }
            }

            // Heart rate zones
            AnimatedVisibility(
                visible = animateContent,
                enter = fadeIn() + slideInVertically { it / 5 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "心率区间分析",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        HeartRateZoneItem(
                            zoneName = "休息区间",
                            zoneRange = "50-60 BPM",
                            zoneColor = MaterialTheme.colorScheme.tertiary,
                            percentage = 15f,
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("休息区间：心率较低，通常处于深度放松或睡眠状态")
                                }
                            }
                        )

                        HeartRateZoneItem(
                            zoneName = "健康区间",
                            zoneRange = "60-80 BPM",
                            zoneColor = MaterialTheme.colorScheme.primary,
                            percentage = 65f,
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("健康区间：正常静息心率，表示良好的心脏健康状态")
                                }
                            }
                        )

                        HeartRateZoneItem(
                            zoneName = "有氧区间",
                            zoneRange = "80-100 BPM",
                            zoneColor = MaterialTheme.colorScheme.secondary,
                            percentage = 15f,
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("有氧区间：轻度运动或日常活动心率范围")
                                }
                            }
                        )

                        HeartRateZoneItem(
                            zoneName = "高强度区间",
                            zoneRange = "100+ BPM",
                            zoneColor = MaterialTheme.colorScheme.error,
                            percentage = 5f,
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("高强度区间：剧烈运动、压力或异常状态下的心率")
                                }
                            }
                        )
                    }
                }
            }

            // Health tips based on heart rate data
            AnimatedVisibility(
                visible = animateContent,
                enter = fadeIn() + slideInVertically { it / 6 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "健康建议",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val tips = when {
                            statistics.average < 60 -> listOf(
                                "心率偏低可能与休息状态、运动员的良好心肺功能或某些药物有关",
                                "如感到头晕、疲劳或无力，建议咨询医生",
                                "适当增加有氧运动可能有助于调节心率"
                            )
                            statistics.average > 100 -> listOf(
                                "静息心率持续偏高可能与压力、咖啡因摄入过多、脱水或疾病有关",
                                "建议保持良好的休息、规律作息，减少刺激性饮料摄入",
                                "如有不适感，请及时就医咨询专业建议"
                            )
                            else -> listOf(
                                "您的平均心率在正常范围内，继续保持健康的生活习惯",
                                "规律的有氧运动对维持心脏健康非常有益",
                                "保持良好的睡眠质量和饮食习惯有助于心率稳定"
                            )
                        }

                        tips.forEach { tip ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(end = 8.dp, top = 2.dp)
                                        .size(16.dp)
                                )

                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("查看更多健康建议")
                                }
                            },
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("查看更多")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(70.dp)) // Space for the FAB
        }
    }
}

@Composable
fun StatisticItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HeartRateZoneItem(
    zoneName: String,
    zoneRange: String,
    zoneColor: Color,
    percentage: Float,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = zoneName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = zoneRange,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(zoneColor)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${percentage.toInt()}% 的时间",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun HeartRateChart(
    heartRateData: List<HeartRateRecord>,
    modifier: Modifier = Modifier
) {
    if (heartRateData.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无心率数据",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        return
    }

    val dateFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxHeartRate = heartRateData.maxOfOrNull { it.value }?.toFloat() ?: 100f
            val minHeartRate = heartRateData.minOfOrNull { it.value }?.toFloat() ?: 50f
            val range = (maxHeartRate - minHeartRate).coerceAtLeast(30f)

            val normalizedData = heartRateData.map {
                it.value.toFloat()
            }

            val path = Path()
            val points = mutableListOf<Offset>()
            val width = size.width
            val height = size.height
            val stepX = width / (normalizedData.size - 1)

            // Draw grid lines
            val paint = Paint()
            paint.color = Color.Gray.copy(alpha = 0.2f)
            paint.strokeWidth = 1f

            // Horizontal grid lines
            val gridStep = 20
            for (i in 0..((maxHeartRate - minHeartRate) / gridStep).toInt()) {
                val y = height - (i * gridStep / range * height)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            // Create points and path
            normalizedData.forEachIndexed { index, value ->
                val x = index * stepX
                val normalizedValue = (value - minHeartRate) / range
                val y = height - (normalizedValue * height)

                points.add(Offset(x, y))

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            // Draw the path
            drawPath(
                path = path,
                color = Color.Gray,
            )

            // Draw points
            points.forEach { point ->
                drawCircle(
                    color = Color.Blue,
                    radius = 4f,
                    center = point
                )

                drawCircle(
                    color = Color.White,
                    radius = 2f,
                    center = point
                )
            }

            // Draw area under the curve
            val fillPath = Path()
            fillPath.addPath(path)
            fillPath.lineTo(width, height)
            fillPath.lineTo(0f, height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Red.copy(alpha = 0.3f),
                        Color.Red.copy(alpha = 0.0f)
                    )
                )
            )
        }

        // Draw time labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            heartRateData.indices.filter {
                it == 0 || it == heartRateData.lastIndex || it == heartRateData.size / 2
            }.forEach { index ->
                Text(
                    text = dateFormatter.format(Date(heartRateData[index].timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}