package com.codelab.basiclayouts

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssistantPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BroadcastOnPersonal
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Light
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.codelab.basiclayouts.ui.theme.MySootheTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

sealed class EasterEggState {
    object Hidden : EasterEggState()
    class Activated(val type: EggType) : EasterEggState()
}

enum class EggType { DEVELOPER, HEART, SECRET }

// 智能体消息数据类
data class AIMessage(
    val content: String,
    val isUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

// 健康提示数据类
data class HealthTip(
    val title: String,
    val content: String,
    val icon: ImageVector,
    val importance: Int = 1
)

class HomeActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySootheTheme {
                HomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.N)
@Preview(showBackground = true)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var easterEgg by remember { mutableStateOf<EasterEggState>(EasterEggState.Hidden) }
    var tapCount by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedBottomTab by remember { mutableIntStateOf(0) }
    var aiAssistantExpanded by remember { mutableStateOf(false) }
    var showHealthTips by remember { mutableStateOf(false) }

    // AI智能体相关状态
    var aiInput by remember { mutableStateOf(TextFieldValue()) }
    val aiMessages = remember { mutableStateListOf<AIMessage>(
        AIMessage("您好！我是您的健康助手。我可以帮您分析心率数据，提供健康建议，或者回答健康相关问题。", false),
        AIMessage("今天我能为您做些什么？", false)
    )}

    // 模拟健康提示数据
    val healthTips = remember {
        listOf(
            HealthTip(
                "保持良好睡眠",
                "研究表明，每晚7-8小时的优质睡眠可显著降低心血管疾病风险。",
                Icons.Default.Light
            ),
            HealthTip(
                "定期测量心率",
                "建议您每日固定时间检测静息心率，有助于及早发现潜在问题。",
                Icons.Default.MonitorHeart,
                3
            ),
            HealthTip(
                "适量运动",
                "每周进行150分钟中等强度有氧运动，可提高心肺功能。",
                Icons.Default.AssistantPhoto,
                2
            )
        )
    }

    fun showEgg(eggType: EggType) {
        easterEgg = EasterEggState.Activated(eggType)
        tapCount = 0
    }

    // 处理AI消息发送
    fun sendAiMessage() {
        if (aiInput.text.isNotBlank()) {
            aiMessages.add(AIMessage(aiInput.text, true))

            // 模拟AI响应
            scope.launch {
                delay(800)
                when {
                    aiInput.text.contains("心率", ignoreCase = true) ->
                        aiMessages.add(AIMessage("您的静息心率在过去一周内平均为68BPM，处于健康范围内。但在周三晚上检测到心率异常升高至92BPM，可能与您当天的高强度运动相关。"))
                    aiInput.text.contains("睡眠", ignoreCase = true) ->
                        aiMessages.add(AIMessage("根据您的睡眠监测数据，您的平均睡眠时长为6.5小时，略低于建议的7-8小时。您的深度睡眠占比约22%，处于正常范围。建议您尝试在睡前1小时避免使用电子设备，可能有助于提高睡眠质量。"))
                    aiInput.text.contains("建议", ignoreCase = true) || aiInput.text.contains("提示", ignoreCase = true) -> {
                        aiMessages.add(AIMessage("基于您的健康数据，我有以下几点建议：\n1. 增加每周运动频率，尤其是有氧运动\n2. 保持规律的睡眠时间\n3. 注意监测心率变化，特别是运动后的恢复情况"))
                        showHealthTips = true
                    }
                    else -> aiMessages.add(AIMessage("感谢您的问题。我已记录并会为您提供相关的健康建议。请问您还有其他问题吗？"))
                }
            }

            aiInput = TextFieldValue("")
        }
    }

    LaunchedEffect(easterEgg) {
        if (easterEgg is EasterEggState.Activated) {
            delay(3000)
            easterEgg = EasterEggState.Hidden
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                // 用户信息区域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 用户头像
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "健康伙伴",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "点击登录账号",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // 导航菜单项
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.MonitorHeart, contentDescription = null) },
                    label = { Text("心率测量") },
                    selected = selectedBottomTab == 0,
                    onClick = {
                        selectedBottomTab = 0
                        scope.launch { drawerState.close() }
                        context.startActivity(Intent(context, MainActivity::class.java))
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                    label = { Text("健康记录") },
                    selected = selectedBottomTab == 1,
                    onClick = {
                        selectedBottomTab = 1
                        scope.launch { drawerState.close() }
                        context.startActivity(Intent(context, HeartRateRecordActivity::class.java))
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                    label = { Text("健康分析") },
                    selected = selectedBottomTab == 2,
                    onClick = {
                        selectedBottomTab = 2
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = null) },
                    label = { Text("健康助手") },
                    selected = selectedBottomTab == 3,
                    onClick = {
                        selectedBottomTab = 3
                        scope.launch {
                            drawerState.close()
                            aiAssistantExpanded = true
                        }
                    },
                    badge = {
                        Badge { Text("新") }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Share, contentDescription = null) },
                    label = { Text("分享数据") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        shareHealthData(context)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("设置") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = {
                        // ✅ 使用 Info 图标（Outlined 风格更符合现代设计）
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null
                        )
                    },
                    label = { Text("关于与未来") },
                    selected = selectedBottomTab == 0,
                    onClick = {
                        selectedBottomTab = 0
                        scope.launch { drawerState.close() }
                        context.startActivity(Intent(context, AboutActivity::class.java))
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )


                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Help, contentDescription = null) },
                    label = { Text("帮助与反馈") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            sendEmail(context)
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "RayVita v3.1.4",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonitorHeart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RayVita",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    actions = {
                        BadgedBox(
                            badge = {
                                Badge { Text("2") }
                            }
                        ) {
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "通知"
                                )
                            }
                        }
                        IconButton(onClick = { aiAssistantExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "AI助手"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    val items = listOf(
                        Triple("首页", Icons.Default.Favorite, 0),
                        Triple("记录", Icons.Default.Assignment, 1),
                        Triple("分析", Icons.Default.Assessment, 2),
                        Triple("我的", Icons.Default.PersonOutline, 3)
                    )

                    items.forEach { (title, icon, index) ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title
                                )
                            },
                            label = { Text(title) },
                            selected = selectedBottomTab == index,
                            onClick = {
                                selectedBottomTab = index
                                if (index == 1) {
                                    context.startActivity(Intent(context, HeartRateRecordActivity::class.java))
                                }
                            }
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        aiAssistantExpanded = true
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "打开AI助手",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Hero Section
                        HeroSection(tapCount) { tapCount++
                            if (tapCount == 7) showEgg(EggType.DEVELOPER)
                        }

                        // 健康状态概览
                        HealthStatusOverview()

                        Spacer(modifier = Modifier.height(16.dp))

                        // 健康提示卡片
                        HealthTipsSection(healthTips)

                        Spacer(modifier = Modifier.height(24.dp))

                        // AI助手预览
                        AiAssistantPreview(
                            onClick = { aiAssistantExpanded = true }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 功能分组
                        FeatureGroups(context)

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )

                        // 页脚
                        FooterSection()

                        // 底部间距
                        Spacer(modifier = Modifier.height(70.dp))
                    }

                    // AI助手对话框
                    if (aiAssistantExpanded) {
                        AIAssistantDialog(
                            messages = aiMessages,
                            inputValue = aiInput,
                            onInputChange = { aiInput = it },
                            onSendClick = { sendAiMessage() },
                            onDismiss = { aiAssistantExpanded = false }
                        )
                    }

                    // 健康提示详情对话框
                    if (showHealthTips) {
                        HealthTipsDialog(
                            tips = healthTips,
                            onDismiss = { showHealthTips = false }
                        )
                    }

                    // 彩蛋
                    when (val egg = easterEgg) {
                        is EasterEggState.Activated -> {
                            when (egg.type) {
                                EggType.DEVELOPER -> DeveloperEgg { easterEgg = EasterEggState.Hidden }
                                else -> Unit
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
fun HeroSection(tapCount: Int, onTap: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.background
                    ),
                    startY = 0f,
                    endY = 500f
                )
            )
            .clickable(onClick = onTap)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 500f
                    )
                )
        )

        var imageOffset by remember { mutableFloatStateOf(50f) }

        LaunchedEffect(Unit) {
            animate(
                initialValue = 50f,
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1000)
            ) { value, _ ->
                imageOffset = value
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
                    .offset(y = imageOffset.dp)
                    .graphicsLayer {
                        rotationZ = 0f
                        cameraDistance = 12f
                    }
            ) {
                HeartParticleEffect(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer {
                            scaleX = 0.8f
                            scaleY = 0.8f
                        }
                )
            }

            AnimatedVisibility(visible = imageOffset == 0f) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "欢迎使用RayVita",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "通过手机摄像头实现心率监测与健康分析",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = if (tapCount > 3) MaterialTheme.colorScheme.error.copy(
                alpha = 0.2f + tapCount * 0.1f
            ) else Color.Transparent,
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
    }
}

@Composable
fun HealthStatusOverview() {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    val today = dateFormat.format(Date())

    val heartRate = 72
    val steps = 8764
    val sleepHours = 7.5f

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
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
                    text = "今日健康概览",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = today,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HealthMetricItem(
                    icon = Icons.Default.MonitorHeart,
                    value = "$heartRate",
                    unit = "BPM",
                    label = "平均心率",
                    tint = MaterialTheme.colorScheme.primary
                )

                HealthMetricItem(
                    icon = Icons.Default.AccessTime,
                    value = "$sleepHours",
                    unit = "小时",
                    label = "睡眠时长",
                    tint = MaterialTheme.colorScheme.tertiary
                )

                HealthMetricItem(
                    icon = Icons.Default.FormatListBulleted,
                    value = "$steps",
                    unit = "步",
                    label = "今日步数",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun HealthMetricItem(
    icon: ImageVector,
    value: String,
    unit: String,
    label: String,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = tint.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .padding(12.dp)
                    .size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HealthTipsSection(tips: List<HealthTip>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(24.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TipsAndUpdates,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "健康小贴士",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "查看全部",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tips) { tip ->
                    HealthTipCard(tip)
                }
            }
        }
    }
}

@Composable
fun HealthTipCard(tip: HealthTip) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (tip.importance) {
                3 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                2 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = tip.icon,
                    contentDescription = null,
                    tint = when (tip.importance) {
                        3 -> MaterialTheme.colorScheme.error
                        2 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = tip.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AiAssistantPreview(onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "健康智能助手",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "您的专属健康管理顾问，随时提供个性化建议",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun FeatureGroups(context: android.content.Context) {
    val coreFeatures = listOf(
        FeatureItem(Icons.Default.MonitorHeart, "心率测量", "实时监测心率"),
        FeatureItem(Icons.Default.Assignment, "心率记录", "查看历史数据"),
        FeatureItem(Icons.Default.Assessment, "健康周报", "本周健康分析"),
        FeatureItem(Icons.Default.Emergency, "紧急预警", "异常即时通知")
    )

    val toolFeatures = listOf(
        FeatureItem(Icons.Default.DataUsage, "数据趋势", "健康数据分析"),
        FeatureItem(Icons.Default.Psychology, "心理健康", "情绪与压力监测"),
        FeatureItem(Icons.Default.BroadcastOnPersonal, "健康社区", "与朋友分享"),
        FeatureItem(Icons.Default.Settings, "个性化设置", "调整监测偏好")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "功能中心",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            FeatureGroup(
                title = "健康监测",
                features = coreFeatures,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                onFeatureClick = { featureTitle ->
                    when (featureTitle) {
                        "心率测量" -> context.startActivity(Intent(context, MainActivity::class.java))
                        "心率记录" -> context.startActivity(Intent(context, HeartRateRecordActivity::class.java))
                        "健康周报" -> Toast.makeText(context, "健康周报功能即将上线", Toast.LENGTH_SHORT).show()
                        "紧急预警" -> Toast.makeText(context, "紧急预警功能已开启", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            FeatureGroup(
                title = "工具与服务",
                features = toolFeatures,
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                onFeatureClick = { featureTitle ->
                    when (featureTitle) {
                        "健康社区" -> context.startActivity(Intent(context, SocialActivity::class.java))
                        "个性化设置" -> Toast.makeText(context, "设置已保存", Toast.LENGTH_SHORT).show()
                        "数据趋势" -> Toast.makeText(context, "数据分析功能即将上线", Toast.LENGTH_SHORT).show()
                        "心理健康" -> Toast.makeText(context, "心理健康分析即将上线", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun FeatureGroup(
    title: String,
    features: List<FeatureItem>,
    containerColor: Color,
    onFeatureClick: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val columns = if (configuration.screenWidthDp >= 600) 4 else 2

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { }
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(if (columns == 4) 120.dp else 200.dp),
            userScrollEnabled = false
        ) {
            items(features) { feature ->
                FeatureCard(
                    icon = feature.icon,
                    title = feature.title,
                    description = feature.description,
                    onClick = { onFeatureClick(feature.title) }
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isPressed by remember { mutableStateOf(false) }
    var isLongPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 动画参数
    val cardElevation by animateDpAsState(
        targetValue = when {
            isPressed -> 8.dp
            isHovered -> 12.dp
            else -> 4.dp
        },
        animationSpec = tween(150),
        label = "elevation"
    )

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = 0.4f,
            stiffness = 300f
        ),
        label = "scale"
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            isHovered -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(150),
        label = "iconColor"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(150),
        label = "textColor"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(cardElevation)
        ),
        elevation = CardDefaults.cardElevation(cardElevation),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
                rotationZ = if (isLongPressed) 5f else 0f
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onLongPress = {
                        isLongPressed = true
                        scope.launch {
                            when (title) {
                                "心率测量" -> Toast.makeText(context, "💖 专家模式已激活！", Toast.LENGTH_SHORT).show()
                                "心率记录" -> Toast.makeText(context, "💖 历史数据分析模式已激活！", Toast.LENGTH_SHORT).show()
                                "健康周报" -> Toast.makeText(context, "💖 详细报告模式已激活！", Toast.LENGTH_SHORT).show()
                                "紧急预警" -> Toast.makeText(context, "💖 高级预警已开启！", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onTap = {
                        scope.launch {
                            isPressed = true
                            delay(80)
                            onClick()
                            delay(20)
                            isPressed = false
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(32.dp)
                    .padding(4.dp)
                    .graphicsLayer {
                        scaleX = if (isPressed) 0.9f else 1f
                        scaleY = if (isPressed) 0.9f else 1f
                    }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isLongPressed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(16.dp)
                )
            }
        }
    }

    LaunchedEffect(isLongPressed) {
        if (isLongPressed) {
            delay(2000)
            isLongPressed = false
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AIAssistantDialog(
    messages: List<AIMessage>,
    inputValue: TextFieldValue,
    onInputChange: (TextFieldValue) -> Unit,
    onSendClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val dateFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val scrollState = rememberScrollState()

    LaunchedEffect(messages.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Dialog Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "健康智能助手",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Messages Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    messages.forEach { message ->
                        AnimatedContent(
                            targetState = message,
                            transitionSpec = {
                                if (message.isUser) {
                                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> -width } + fadeOut())
                                } else {
                                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> width } + fadeOut())
                                }
                            }
                        ) { msg ->
                            MessageBubble(
                                message = msg,
                                dateFormatter = dateFormatter
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Input Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { /* 语音输入功能 */ },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardVoice,
                                contentDescription = "语音输入",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = inputValue,
                            onValueChange = onInputChange,
                            placeholder = { Text("输入您的问题...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onSendClick,
                            enabled = inputValue.text.isNotBlank(),
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (inputValue.text.isNotBlank())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "发送",
                                tint = if (inputValue.text.isNotBlank())
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: AIMessage,
    dateFormatter: SimpleDateFormat
) {
    val formattedTime = dateFormatter.format(Date(message.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }

        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                ),
                color = if (message.isUser)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        if (message.isUser) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

@Composable
fun HealthTipsDialog(
    tips: List<HealthTip>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "健康建议",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    tips.forEach { tip ->
                        DetailedTipCard(tip)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "这些建议基于您的健康数据生成，仅供参考。如有疑问，请咨询医疗专业人士。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("我知道了")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedTipCard(tip: HealthTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (tip.importance) {
                3 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                2 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = when (tip.importance) {
                    3 -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    2 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = tip.icon,
                    contentDescription = null,
                    tint = when (tip.importance) {
                        3 -> MaterialTheme.colorScheme.error
                        2 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = tip.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun HeartParticleEffect(modifier: Modifier = Modifier) {
    val particles = remember { generateHeartParticles(1200) }

    // 动画状态管理
    val (heartbeat, lightWave) = remember {
        mutableStateOf(1f) to mutableStateOf(0f)
    }

    // 主心跳动画
    LaunchedEffect(Unit) {
        while (isActive) {
            animate(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ) { value, _ ->
                heartbeat.value = value
            }
            animate(
                initialValue = 1.15f,
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 900,
                    easing = LinearEasing
                )
            ) { value, _ ->
                heartbeat.value = value
            }
            delay(1200)
        }
    }

    // 光晕波动动画
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        ) { value, _ ->
            lightWave.value = value
        }
    }

    Canvas(modifier = modifier) {
        // 背景光晕
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x66FF3D00), Color(0x10FF1744), Color.Transparent),
                center = center,
                radius = size.minDimension * 0.8f * (0.9f + lightWave.value * 0.2f)
            ),
            blendMode = BlendMode.Plus
        )

        // 绘制粒子
        particles.forEach { p ->
            val scale = heartbeat.value
            val baseSize = 4f * scale
            // 核心缩放控制参数
            val heartScale = mutableStateOf(0.3f)

            // 坐标映射
            val x = (p.x * heartScale.value * 30).toFloat() + center.x
            val y = (p.y * heartScale.value * 30).toFloat() + center.y
            // 粒子大小控制
            val particleSize = 3f * heartScale.value * 0.15f

            // 科技感颜色（蓝紫色系）
            val hue = 240f + (p.z.coerceIn(-1.0, 1.0) * 40f).toFloat()
            val color = Color.hsl(
                hue = hue,
                saturation = 0.85f,
                lightness = 0.6f + (lightWave.value * 0.2f)
            )

            // 主粒子
            drawCircle(
                color = color,
                radius = particleSize,
                center = Offset(x, y),
                blendMode = BlendMode.Screen
            )

            // 光晕效果
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = baseSize * 1.5f,
                center = Offset(x, y),
                blendMode = BlendMode.Overlay
            )
        }
    }
}

private fun generateHeartParticles(count: Int): List<Point3D> {
    return List(count) {
        val theta = Random.nextDouble(0.0, 2 * PI)
        val r = Random.nextDouble(0.8, 1.2)

        // 立体化改进的方程
        val x = 16 * sin(theta).pow(3) * r
        val baseY = 13 * cos(theta) - 5 * cos(2*theta) - 2 * cos(3*theta) - cos(4*theta)
        val y = -baseY * r

        // 增强Z轴计算（增加心形厚度）
        val z = when {
            baseY > 0 -> (sin(theta * 3) * 8 * r).coerceIn(-4.0, 4.0)
            else -> (cos(theta * 2) * 6 * r).coerceIn(-3.0, 3.0)
        }

        Point3D(
            x * 1.05,
            y * 0.92,
            z * 1.8
        )
    }
}

data class Point3D(
    val x: Double,
    val y: Double,
    val z: Double
) {
    fun distanceTo(other: Point3D): Double {
        return sqrt(
            (x - other.x).pow(2) +
                    (y - other.y).pow(2) +
                    (z - other.z).pow(2)
        )
    }
}

@Composable
private fun DeveloperEgg(onDismiss: () -> Unit) {
    var scale by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        scale = 1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .size(300.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "🌟 开发团队 🌟",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listOf("👑 首席摸鱼官", "🎮 电竞夏总", "☕ 咖啡因依赖", "🐛 Bug制造机")) {
                        Chip(text = it, color = MaterialTheme.colorScheme.errorContainer)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "「我们写的不是代码，是艺术」",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun Chip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun FooterSection() {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "联系我们：zhwang@nuist.edu.cn",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier
                    .clickable { sendEmail(context) }
                    .padding(4.dp)
            )
            Text(
                text = "开发团队：王子恒 夏东旭 吴迪",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
            Text(
                text = "版权 © 2025 RayVita",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }

        Text(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .alpha(0.03f)
                .clickable { expanded = true },
            text = "v3.14.15",
            style = MaterialTheme.typography.labelSmall
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            DropdownMenuItem(
                text = {
                    Text("内部测试菜单", color = MaterialTheme.colorScheme.onSurface)
                },
                onClick = { /* 测试功能实现 */ },
                leadingIcon = {
                    Icon(
                        Icons.Default.Science,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            DropdownMenuItem(
                text = {
                    Text("查看彩虹模式", color = MaterialTheme.colorScheme.onSurface)
                },
                onClick = { /* 颜色动画实现 */ },
                leadingIcon = {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }
}

// 工具函数
private fun shareHealthData(context: android.content.Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "查看我的健康报告：https://rayvita.com/report")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, null))
}

private fun sendEmail(context: android.content.Context) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:zhwang@nuist.edu.cn")
            putExtra(Intent.EXTRA_SUBJECT, "[RayVita] 用户反馈")
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "未找到邮件应用", Toast.LENGTH_SHORT).show()
    }
}

private data class FeatureItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)