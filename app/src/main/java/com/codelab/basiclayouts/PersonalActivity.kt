//package com.codelab.basiclayouts
//
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.Spring
//import androidx.compose.animation.core.animateDpAsState
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.spring
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.slideInVertically
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.pager.HorizontalPager
//import androidx.compose.foundation.pager.rememberPagerState
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AccountCircle
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.ArrowForward
//import androidx.compose.material.icons.filled.Badge
//import androidx.compose.material.icons.filled.Bolt
//import androidx.compose.material.icons.filled.Cloud
//import androidx.compose.material.icons.filled.CloudSync
//import androidx.compose.material.icons.filled.ColorLens
//import androidx.compose.material.icons.filled.Favorite
//import androidx.compose.material.icons.filled.FitnessCenter
//import androidx.compose.material.icons.filled.History
//import androidx.compose.material.icons.filled.Info
//import androidx.compose.material.icons.filled.Language
//import androidx.compose.material.icons.filled.Lock
//import androidx.compose.material.icons.filled.MedicalServices
//import androidx.compose.material.icons.filled.MoreVert
//import androidx.compose.material.icons.filled.Notifications
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material.icons.filled.Share
//import androidx.compose.material.icons.filled.Star
//import androidx.compose.material.icons.filled.Timelapse
//import androidx.compose.material.icons.filled.WbSunny
//import androidx.compose.material.icons.rounded.Edit
//import androidx.compose.material3.Badge
//import androidx.compose.material3.BadgedBox
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Divider
//import androidx.compose.material3.DropdownMenu
//import androidx.compose.material3.DropdownMenuItem
//import androidx.compose.material3.ElevatedCard
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.ExtendedFloatingActionButton
//import androidx.compose.material3.FabPosition
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.OutlinedCard
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.SnackbarHost
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Switch
//import androidx.compose.material3.SwitchDefaults
//import androidx.compose.material3.Tab
//import androidx.compose.material3.TabRow
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.rotate
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.window.Dialog
//import com.codelab.basiclayouts.ui.theme.MySootheTheme
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Date
//import java.util.Locale
//import kotlin.random.Random
//
//class PersonalActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MySootheTheme {
//                PersonalScreen()
//            }
//        }
//    }
//}
//
//data class HealthTrend(
//    val date: String,
//    val heartRate: Int,
//    val sleepQuality: Int,
//    val steps: Int
//)
//
//data class DailyGoal(
//    val title: String,
//    val progress: Float,
//    val target: String,
//    val achieved: String,
//    val icon: ImageVector
//)
//
//data class Achievement(
//    val icon: ImageVector,
//    val title: String,
//    val description: String,
//    val achieved: Boolean
//)
//
//data class Notification(
//    val title: String,
//    val message: String,
//    val time: String,
//    val read: Boolean,
//    val type: NotificationType
//)
//
//enum class NotificationType {
//    REMINDER, ALERT, ACHIEVEMENT, SYSTEM
//}
//
//data class HealthTipP(
//    val title: String,
//    val content: String,
//    val source: String,
//    val icon: ImageVector
//)
//
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
//@Composable
//fun PersonalScreen() {
//    val scrollState = rememberScrollState()
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    // Application state
//    var selectedTab by remember { mutableIntStateOf(0) }
//    var showMenu by remember { mutableStateOf(false) }
//    var darkMode by remember { mutableStateOf(false) }
//    var selectedLanguage by remember { mutableStateOf("简体中文") }
//    var showNotificationsDialog by remember { mutableStateOf(false) }
//    var notifications = remember {
//        mutableStateOf(
//            listOf(
//                Notification(
//                    "健康提醒",
//                    "你今天还有500步才能达成目标，加油！",
//                    "10分钟前",
//                    false,
//                    NotificationType.REMINDER
//                ),
//                Notification(
//                    "健康成就",
//                    "恭喜你连续7天记录健康数据，获得坚持达人勋章！",
//                    "2小时前",
//                    true,
//                    NotificationType.ACHIEVEMENT
//                ),
//                Notification(
//                    "系统通知",
//                    "RayVita 3.2版本已发布，点击查看新功能详情。",
//                    "昨天",
//                    true,
//                    NotificationType.SYSTEM
//                )
//            )
//        )
//    }
//    var unreadNotifications = notifications.value.count { !it.read }
//
//    // Health data
//    val healthTrends = remember {
//        List(7) { index ->
//            val date = Calendar.getInstance().apply {
//                add(Calendar.DAY_OF_YEAR, -6 + index)
//            }.time
//            val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
//            HealthTrend(
//                date = dateFormat.format(date),
//                heartRate = Random.nextInt(65, 75),
//                sleepQuality = Random.nextInt(75, 95),
//                steps = Random.nextInt(6000, 12000)
//            )
//        }
//    }
//
//    val dailyGoals = remember {
//        listOf(
//            DailyGoal(
//                title = "步数",
//                progress = 0.75f,
//                target = "10,000",
//                achieved = "7,500",
//                icon = Icons.Default.FitnessCenter
//            ),
//            DailyGoal(
//                title = "饮水量",
//                progress = 0.6f,
//                target = "2000ml",
//                achieved = "1200ml",
//                icon = Icons.Default.WbSunny
//            ),
//            DailyGoal(
//                title = "活动时间",
//                progress = 0.45f,
//                target = "60分钟",
//                achieved = "27分钟",
//                icon = Icons.Default.Timelapse
//            )
//        )
//    }
//
//    val achievements = remember {
//        listOf(
//            Achievement(
//                icon = Icons.Default.Star,
//                title = "连续登录7天",
//                description = "连续7天使用应用记录健康数据",
//                achieved = true
//            ),
//            Achievement(
//                icon = Icons.Default.Favorite,
//                title = "心率监测达人",
//                description = "连续30天进行心率监测",
//                achieved = true
//            ),
//            Achievement(
//                icon = Icons.Default.FitnessCenter,
//                title = "运动达人",
//                description = "单日步数超过15000步",
//                achieved = true
//            ),
//            Achievement(
//                icon = Icons.Default.History,
//                title = "早睡达人",
//                description = "连续10天在23:00前入睡",
//                achieved = false
//            ),
//            Achievement(
//                icon = Icons.Default.Bolt,
//                title = "极速者",
//                description = "单次运动心率达到最大心率的85%",
//                achieved = false
//            )
//        )
//    }
//
//    val healthTips = remember {
//        listOf(
//            HealthTipP(
//                title = "充足的睡眠对心脏健康至关重要",
//                content = "研究表明，每晚7-8小时的优质睡眠可以降低患心血管疾病的风险。",
//                source = "健康时报",
//                icon = Icons.Default.Favorite
//            ),
//            HealthTipP(
//                title = "多喝水有助于新陈代谢",
//                content = "保持充分水分摄入可以帮助身体正常运作，成年人应每天饮水约2000毫升。",
//                source = "健康生活杂志",
//                icon = Icons.Default.WbSunny
//            ),
//            HealthTipP(
//                title = "适度运动可改善睡眠质量",
//                content = "每天30分钟的中等强度运动可以帮助你更快入睡，并提高睡眠质量。但避免睡前剧烈运动。",
//                source = "睡眠研究中心",
//                icon = Icons.Default.FitnessCenter
//            )
//        )
//    }
//
//    // Animation state for content visibility
//    var animateContent by remember { mutableStateOf(false) }
//    val profileElevation by animateDpAsState(
//        targetValue = if (animateContent) 4.dp else 0.dp,
//        animationSpec = spring(
//            dampingRatio = Spring.DampingRatioMediumBouncy,
//            stiffness = Spring.StiffnessLow
//        ),
//        label = "elevation"
//    )
//
//    LaunchedEffect(Unit) {
//        delay(100)
//        animateContent = true
//    }
//
//    val tabTitles = listOf("健康概览", "每日目标", "健康趋势", "个人设置")
//    val statsPagerState = rememberPagerState(pageCount = { tabTitles.size })
//
//    // Notifications Dialog
//    if (showNotificationsDialog) {
//        Dialog(onDismissRequest = { showNotificationsDialog = false }) {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                shape = RoundedCornerShape(20.dp)
//            ) {
//                NotificationContent(
//                    notifications = notifications.value,
//                    onDismiss = { showNotificationsDialog = false },
//                    onReadAll = {
//                        notifications.value = notifications.value.map { it.copy(read = true) }
//                        unreadNotifications = 0
//                        scope.launch {
//                            snackbarHostState.showSnackbar("已将所有通知标记为已读")
//                        }
//                        showNotificationsDialog = false
//                    },
//                    onNotificationClick = { index ->
//                        if (!notifications.value[index].read) {
//                            notifications.value = notifications.value.toMutableList().apply {
//                                this[index] = this[index].copy(read = true)
//                            }
//                            unreadNotifications -= 1
//                        }
//                        scope.launch {
//                            showNotificationsDialog = false
//                            delay(300)
//                            snackbarHostState.showSnackbar("已查看通知: ${notifications.value[index].title}")
//                        }
//                    }
//                )
//            }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "个人中心",
//                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = {
//                        Toast.makeText(context, "返回上一级", Toast.LENGTH_SHORT).show()
//                        (context as? ComponentActivity)?.finish()
//                    }) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "返回"
//                        )
//                    }
//                },
//                actions = {
//                    BadgedBox(
//                        badge = {
//                            if (unreadNotifications > 0) {
//                                Badge { Text(text = unreadNotifications.toString()) }
//                            }
//                        }
//                    ) {
//                        IconButton(
//                            onClick = { showNotificationsDialog = true }
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Notifications,
//                                contentDescription = "通知",
//                                tint = MaterialTheme.colorScheme.primary
//                            )
//                        }
//                    }
//
//                    IconButton(onClick = { showMenu = !showMenu }) {
//                        Icon(
//                            imageVector = Icons.Default.MoreVert,
//                            contentDescription = "更多选项"
//                        )
//                    }
//
//                    DropdownMenu(
//                        expanded = showMenu,
//                        onDismissRequest = { showMenu = false }
//                    ) {
//                        DropdownMenuItem(
//                            text = { Text(text = "个人资料") },
//                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
//                            onClick = {
//                                showMenu = false
//                                Toast.makeText(context, "查看个人资料", Toast.LENGTH_SHORT).show()
//                            }
//                        )
//                        DropdownMenuItem(
//                            text = { Text(text = if (darkMode) "切换到浅色模式" else "切换到深色模式") },
//                            leadingIcon = { Icon(Icons.Default.ColorLens, contentDescription = null) },
//                            onClick = {
//                                darkMode = !darkMode
//                                showMenu = false
//                                Toast.makeText(
//                                    context,
//                                    if (darkMode) "已切换到深色模式" else "已切换到浅色模式",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        )
//                        DropdownMenuItem(
//                            text = { Text(text = "语言设置") },
//                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
//                            onClick = {
//                                showMenu = false
//                                val languages = listOf("简体中文", "English", "日本語", "한국어")
//                                val currentIndex = languages.indexOf(selectedLanguage)
//                                selectedLanguage = languages[(currentIndex + 1) % languages.size]
//                                Toast.makeText(context, "已切换语言：$selectedLanguage", Toast.LENGTH_SHORT).show()
//                            }
//                        )
//                        DropdownMenuItem(
//                            text = { Text(text = "关于我们") },
//                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
//                            onClick = {
//                                showMenu = false
//                                Toast.makeText(context, "关于我们页面", Toast.LENGTH_SHORT).show()
//                            }
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.surface,
//                    titleContentColor = MaterialTheme.colorScheme.onSurface
//                )
//            )
//        },
//        bottomBar = {
//            NavigationBar {
//                val navItems = listOf(
//                    Triple("首页", Icons.Default.FitnessCenter, "回到首页"),
//                    Triple("数据", Icons.Default.History, "健康数据"),
//                    Triple("社区", Icons.Default.Share, "社区互动"),
//                    Triple("我的", Icons.Default.Person, "个人中心")
//                )
//
//                navItems.forEachIndexed { index, (title, icon, description) ->
//                    NavigationBarItem(
//                        icon = { Icon(icon, contentDescription = description) },
//                        label = { Text(title) },
//                        selected = index == 3,
//                        onClick = {
//                            if (index != 3) {
//                                Toast.makeText(context, "导航到：$title", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    )
//                }
//            }
//        },
//        floatingActionButton = {
//            ExtendedFloatingActionButton(
//                onClick = {
//                    scope.launch {
//                        snackbarHostState.showSnackbar("开始记录新的健康数据")
//                    }
//                },
//                icon = { Icon(Icons.Default.Add, contentDescription = "添加") },
//                text = { Text("记录健康") },
//                containerColor = MaterialTheme.colorScheme.primary,
//                contentColor = MaterialTheme.colorScheme.onPrimary
//            )
//        },
//        floatingActionButtonPosition = FabPosition.End,
//        snackbarHost = { SnackbarHost(snackbarHostState) }
//    ) { innerPadding ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .verticalScroll(scrollState)
//                    .padding(16.dp)
//            ) {
//                AnimatedVisibility(
//                    visible = animateContent,
//                    enter = fadeIn() + slideInVertically { it / 3 }
//                ) {
//                    ProfileHeader(
//                        elevation = profileElevation,
//                        onEditClick = {
//                            Toast.makeText(context, "编辑个人资料", Toast.LENGTH_SHORT).show()
//                        },
//                        onAvatarClick = {
//                            Toast.makeText(context, "更换头像", Toast.LENGTH_SHORT).show()
//                        }
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                AnimatedVisibility(
//                    visible = animateContent,
//                    enter = fadeIn() + slideInVertically { it / 3 }
//                ) {
//                    Column {
//                        TabRow(selectedTabIndex = selectedTab) {
//                            tabTitles.forEachIndexed { index, title ->
//                                Tab(
//                                    selected = selectedTab == index,
//                                    onClick = {
//                                        selectedTab = index
//                                        scope.launch {
//                                            statsPagerState.animateScrollToPage(index)
//                                        }
//                                    },
//                                    text = { Text(title) }
//                                )
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        HorizontalPager(
//                            state = statsPagerState,
//                            modifier = Modifier.fillMaxWidth()
//                        ) { page ->
//                            when (page) {
//                                0 -> HealthOverview()
//                                1 -> DailyGoalsSection(dailyGoals)
//                                2 -> HealthTrendsSection(healthTrends)
//                                3 -> SettingsSection()
//                            }
//                        }
//
//                        LaunchedEffect(statsPagerState.currentPage) {
//                            selectedTab = statsPagerState.currentPage
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                AnimatedVisibility(
//                    visible = animateContent,
//                    enter = fadeIn() + slideInVertically { it / 3 }
//                ) {
//                    MembershipCard(
//                        onCardClick = {
//                            Toast.makeText(context, "查看会员详情", Toast.LENGTH_SHORT).show()
//                        }
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                AnimatedVisibility(
//                    visible = animateContent,
//                    enter = fadeIn() + slideInVertically { it / 3 }
//                ) {
//                    HealthAchievementsSection(
//                        achievements = achievements,
//                        onViewAllClick = {
//                            Toast.makeText(context, "查看全部成就", Toast.LENGTH_SHORT).show()
//                        },
//                        onAchievementClick = { achievement ->
//                            Toast.makeText(
//                                context,
//                                "成就详情: ${achievement.title}\n${achievement.description}",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                AnimatedVisibility(
//                    visible = animateContent,
//                    enter = fadeIn() + slideInVertically { it / 3 }
//                ) {
//                    HealthTipsSection(
//                        tips = healthTips,
//                        onTipClick = { tip ->
//                            Toast.makeText(
//                                context,
//                                "${tip.title}\n${tip.content}\n来源：${tip.source}",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                AnimatedVisibility(
//                    visible = animateContent,
//                    enter = fadeIn() + slideInVertically { it / 3 }
//                ) {
//                    Column {
//                        Text(
//                            text = "社交分享",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold,
//                            modifier = Modifier.padding(vertical = 8.dp)
//                        )
//                        SocialShareCard()
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                AnimatedVisibility(
//                    visible = animateContent,
//                    enter = fadeIn() + slideInVertically { it / 3 }
//                ) {
//                    Text(
//                        text = "版本 wzhv 0.1.3t \n© 2025 RayVita 保留所有权利",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 16.dp)
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(40.dp))
//            }
//
//            AnimatedVisibility(
//                visible = animateContent,
//                enter = fadeIn() + slideInVertically { it / 2 },
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .padding(bottom = 16.dp)
//            ) {
//                Button(
//                    onClick = {
//                        Toast.makeText(context, "正在退出登录...", Toast.LENGTH_SHORT).show()
//                        scope.launch {
//                            delay(1000)
//                            (context as? ComponentActivity)?.finish()
//                        }
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 32.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.errorContainer,
//                        contentColor = MaterialTheme.colorScheme.onErrorContainer
//                    ),
//                    shape = RoundedCornerShape(16.dp)
//                ) {
//                    Text(
//                        text = "退出登录",
//                        modifier = Modifier.padding(vertical = 4.dp)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun NotificationContent(
//    notifications: List<Notification>,
//    onDismiss: () -> Unit,
//    onReadAll: () -> Unit,
//    onNotificationClick: (Int) -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "通知中心",
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold
//            )
//
//            Row {
//                TextButton(onClick = onReadAll) {
//                    Text("全部已读")
//                }
//
//                TextButton(onClick = onDismiss) {
//                    Text("关闭")
//                }
//            }
//        }
//
//        Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//        if (notifications.isEmpty()) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "暂无通知",
//                    style = MaterialTheme.typography.bodyLarge,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                )
//            }
//        } else {
//            Column(
//                modifier = Modifier.weight(1f, fill = false)
//            ) {
//                notifications.forEachIndexed { index, notification ->
//                    NotificationItem(
//                        notification = notification,
//                        onClick = { onNotificationClick(index) }
//                    )
//
//                    if (index < notifications.size - 1) {
//                        Divider(
//                            modifier = Modifier.padding(vertical = 8.dp),
//                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun NotificationItem(
//    notification: Notification,
//    onClick: () -> Unit
//) {
//    val backgroundColor = if (!notification.read)
//        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
//    else
//        Color.Transparent
//
//    val icon = when (notification.type) {
//        NotificationType.REMINDER -> Icons.Default.Timelapse
//        NotificationType.ALERT -> Icons.Default.Info
//        NotificationType.ACHIEVEMENT -> Icons.Default.Star
//        NotificationType.SYSTEM -> Icons.Default.Settings
//    }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick)
//            .background(backgroundColor)
//            .padding(vertical = 12.dp, horizontal = 8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            imageVector = icon,
//            contentDescription = null,
//            tint = MaterialTheme.colorScheme.primary,
//            modifier = Modifier.size(24.dp)
//        )
//
//        Spacer(modifier = Modifier.width(12.dp))
//
//        Column(modifier = Modifier.weight(1f)) {
//            Text(
//                text = notification.title,
//                style = MaterialTheme.typography.titleSmall,
//                fontWeight = FontWeight.Bold
//            )
//
//            Text(
//                text = notification.message,
//                style = MaterialTheme.typography.bodyMedium,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis
//            )
//
//            Text(
//                text = notification.time,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//            )
//        }
//
//        if (!notification.read) {
//            Box(
//                modifier = Modifier
//                    .size(8.dp)
//                    .background(MaterialTheme.colorScheme.primary, CircleShape)
//            )
//        }
//    }
//}
//
//@Composable
//fun ProfileHeader(
//    elevation: androidx.compose.ui.unit.Dp,
//    onEditClick: () -> Unit,
//    onAvatarClick: () -> Unit
//) {
//    OutlinedCard(
//        modifier = Modifier
//            .fillMaxWidth()
//            .shadow(elevation = elevation, shape = RoundedCornerShape(24.dp)),
//        shape = RoundedCornerShape(24.dp),
//        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(160.dp)
//                .background(
//                    brush = Brush.verticalGradient(
//                        colors = listOf(
//                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
//                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
//                        )
//                    )
//                )
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Surface(
//                    modifier = Modifier
//                        .size(95.dp)
//                        .shadow(8.dp, CircleShape)
//                        .clickable(onClick = onAvatarClick),
//                    shape = CircleShape,
//                    color = MaterialTheme.colorScheme.surface
//                ) {
//                    Box(contentAlignment = Alignment.Center) {
//                        Icon(
//                            imageVector = Icons.Default.AccountCircle,
//                            contentDescription = null,
//                            modifier = Modifier.size(95.dp),
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                        Surface(
//                            modifier = Modifier
//                                .align(Alignment.BottomEnd)
//                                .padding(4.dp)
//                                .size(28.dp),
//                            shape = CircleShape,
//                            color = MaterialTheme.colorScheme.primaryContainer
//                        ) {
//                            Icon(
//                                imageVector = Icons.Rounded.Edit,
//                                contentDescription = "编辑头像",
//                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
//                                modifier = Modifier
//                                    .padding(6.dp)
//                                    .clickable(
//                                        interactionSource = remember { MutableInteractionSource() },
//                                        indication = null,
//                                        onClick = onAvatarClick
//                                    )
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.width(16.dp))
//
//                Column(modifier = Modifier.weight(1f)) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text(
//                            text = "小Ray",
//                            style = MaterialTheme.typography.titleLarge,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Surface(
//                            modifier = Modifier.shadow(4.dp, RoundedCornerShape(4.dp)),
//                            shape = RoundedCornerShape(4.dp),
//                            color = MaterialTheme.colorScheme.tertiaryContainer
//                        ) {
//                            Text(
//                                text = "专业版",
//                                style = MaterialTheme.typography.labelSmall,
//                                fontWeight = FontWeight.Medium,
//                                color = MaterialTheme.colorScheme.onTertiaryContainer,
//                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    Text(
//                        text = "ID: health20250318",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                    )
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text(
//                            text = "综合健康评分",
//                            style = MaterialTheme.typography.bodyMedium,
//                            fontWeight = FontWeight.Medium,
//                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
//                        )
//
//                        Spacer(modifier = Modifier.width(8.dp))
//
//                        Text(
//                            text = "92",
//                            style = MaterialTheme.typography.titleMedium,
//                            color = MaterialTheme.colorScheme.primary,
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 18.sp
//                        )
//
//                        Text(
//                            text = "/100",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                        )
//
//                        Spacer(modifier = Modifier.width(4.dp))
//
//                        Icon(
//                            imageVector = Icons.Default.Star,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.tertiary,
//                            modifier = Modifier.size(18.dp)
//                        )
//                    }
//                }
//
//                IconButton(onClick = onEditClick) {
//                    Icon(
//                        imageVector = Icons.Default.ArrowForward,
//                        contentDescription = "编辑个人资料",
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun HealthOverview() {
//    var animationPlayed by remember { mutableStateOf(false) }
//    LaunchedEffect(Unit) {
//        delay(300)
//        animationPlayed = true
//    }
//
//    Column(modifier = Modifier.fillMaxWidth()) {
//        Text(
//            text = "健康数据概览",
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(vertical = 8.dp)
//        )
//
//        ElevatedCard(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(20.dp),
//            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    HealthMetric(
//                        title = "静息心率",
//                        value = "68",
//                        unit = "BPM",
//                        icon = Icons.Default.Favorite,
//                        progress = if (animationPlayed) 0.68f else 0f,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//
//                    HealthMetric(
//                        title = "睡眠质量",
//                        value = "83",
//                        unit = "%",
//                        icon = Icons.Default.History,
//                        progress = if (animationPlayed) 0.83f else 0f,
//                        color = MaterialTheme.colorScheme.tertiary
//                    )
//
//                    HealthMetric(
//                        title = "活跃度",
//                        value = "76",
//                        unit = "%",
//                        icon = Icons.Default.FitnessCenter,
//                        progress = if (animationPlayed) 0.76f else 0f,
//                        color = MaterialTheme.colorScheme.secondary
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Text(
//                            text = "连续监测",
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                        )
//
//                        Row(verticalAlignment = Alignment.Bottom) {
//                            Text(
//                                text = "27",
//                                style = MaterialTheme.typography.titleLarge,
//                                fontWeight = FontWeight.Bold
//                            )
//
//                            Text(
//                                text = "天",
//                                style = MaterialTheme.typography.bodyMedium,
//                                modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
//                            )
//                        }
//                    }
//
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Text(
//                            text = "健康预警",
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                        )
//
//                        Row(verticalAlignment = Alignment.Bottom) {
//                            Text(
//                                text = "0",
//                                style = MaterialTheme.typography.titleLarge,
//                                fontWeight = FontWeight.Bold,
//                                color = MaterialTheme.colorScheme.tertiary
//                            )
//
//                            Text(
//                                text = "条",
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.tertiary,
//                                modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
//                            )
//                        }
//                    }
//
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Text(
//                            text = "健康趋势",
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                        )
//
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.Center
//                        ) {
//                            Text(
//                                text = "上升",
//                                style = MaterialTheme.typography.titleMedium,
//                                fontWeight = FontWeight.Bold,
//                                color = MaterialTheme.colorScheme.primary
//                            )
//
//                            Icon(
//                                imageVector = Icons.Default.ArrowForward,
//                                contentDescription = null,
//                                tint = MaterialTheme.colorScheme.primary,
//                                modifier = Modifier
//                                    .size(16.dp)
//                                    .rotate(270f)
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "今日步数",
//                        style = MaterialTheme.typography.bodyLarge,
//                        fontWeight = FontWeight.Medium
//                    )
//
//                    Text(
//                        text = "7,532 / 10,000",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                LinearProgressIndicator(
//                    progress = { 0.75f },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(8.dp)
//                        .clip(RoundedCornerShape(4.dp)),
//                    color = MaterialTheme.colorScheme.primary,
//                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun HealthMetric(
//    title: String,
//    value: String,
//    unit: String,
//    icon: ImageVector,
//    progress: Float,
//    color: Color
//) {
//    val animatedProgress = animateFloatAsState(
//        targetValue = progress,
//        animationSpec = tween(1000),
//        label = "progress"
//    )
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.padding(horizontal = 8.dp)
//    ) {
//        Box(
//            contentAlignment = Alignment.Center,
//            modifier = Modifier
//                .size(72.dp)
//                .padding(4.dp)
//                .clickable {
//                    // Handle metric click
//                }
//        ) {
//            CircularProgressIndicator(
//                progress = { animatedProgress.value },
//                modifier = Modifier.fillMaxSize(),
//                color = color,
//                trackColor = color.copy(alpha = 0.1f),
//                strokeWidth = 4.dp,
//                strokeCap = StrokeCap.Round
//            )
//
//            Icon(
//                imageVector = icon,
//                contentDescription = null,
//                tint = color,
//                modifier = Modifier.size(28.dp)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = title,
//            style = MaterialTheme.typography.bodySmall,
//            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
//            textAlign = TextAlign.Center
//        )
//
//        Row(verticalAlignment = Alignment.Bottom) {
//            Text(
//                text = value,
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold
//            )
//
//            Text(
//                text = unit,
//                style = MaterialTheme.typography.labelSmall,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
//                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
//            )
//        }
//    }
//}
//
//@Composable
//fun DailyGoalsSection(goals: List<DailyGoal>) {
//    Column(modifier = Modifier.fillMaxWidth()) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "今日目标",
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold
//            )
//
//            Text(
//                text = "${SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(Date())}",
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Column(
//            modifier = Modifier.fillMaxWidth(),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            goals.forEach { goal ->
//                DailyGoalItem(goal)
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center
//        ) {
//            Button(
//                onClick = { /* Add new goal */ },
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = null,
//                    modifier = Modifier.size(18.dp)
//                )
//
//                Spacer(modifier = Modifier.width(4.dp))
//
//                Text(text = "添加新目标")
//            }
//        }
//    }
//}
//
//@Composable
//fun DailyGoalItem(goal: DailyGoal) {
//    val context = LocalContext.current
//    val animatedProgress = animateFloatAsState(
//        targetValue = goal.progress,
//        animationSpec = tween(1000),
//        label = "progress"
//    )
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable {
//                Toast
//                    .makeText(
//                        context,
//                        "${goal.title}目标详情: 已完成 ${goal.achieved}/${goal.target}",
//                        Toast.LENGTH_SHORT
//                    )
//                    .show()
//            },
//        shape = RoundedCornerShape(16.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Surface(
//                shape = CircleShape,
//                color = MaterialTheme.colorScheme.primaryContainer,
//                modifier = Modifier.size(48.dp)
//            ) {
//                Box(contentAlignment = Alignment.Center) {
//                    Icon(
//                        imageVector = goal.icon,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.width(16.dp))
//
//            Column(
//                modifier = Modifier.weight(1f)
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        text = goal.title,
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.SemiBold
//                    )
//
//                    Text(
//                        text = "${(goal.progress * 100).toInt()}%",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.primary,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                LinearProgressIndicator(
//                    progress = { animatedProgress.value },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(8.dp)
//                        .clip(RoundedCornerShape(4.dp)),
//                    color = MaterialTheme.colorScheme.primary,
//                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
//                )
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        text = "已完成: ${goal.achieved}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                    )
//
//                    Text(
//                        text = "目标: ${goal.target}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun HealthTrendsSection(trends: List<HealthTrend>) {
//    val context = LocalContext.current
//    val selectedMetric = remember { mutableIntStateOf(0) }
//    val metrics = listOf("心率", "睡眠", "步数")
//
//    Column(modifier = Modifier.fillMaxWidth()) {
//        Text(
//            text = "健康趋势",
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Use a TabRow instead of SingleChoiceSegmentedButtonRow which has compatibility issues
//        TabRow(
//            selectedTabIndex = selectedMetric.value,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            metrics.forEachIndexed { index, metric ->
//                Tab(
//                    selected = selectedMetric.value == index,
//                    onClick = { selectedMetric.value = index },
//                    text = { Text(metric) }
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(220.dp),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp)
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "过去7天 ${metrics[selectedMetric.value]}趋势",
//                        style = MaterialTheme.typography.titleSmall,
//                        fontWeight = FontWeight.SemiBold
//                    )
//
//                    IconButton(
//                        onClick = {
//                            Toast
//                                .makeText(
//                                    context,
//                                    "查看${metrics[selectedMetric.value]}详细数据",
//                                    Toast.LENGTH_SHORT
//                                )
//                                .show()
//                        }
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowForward,
//                            contentDescription = "查看详情",
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Simple visualization of trend data
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(1f),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.Bottom
//                ) {
//                    trends.forEachIndexed { index, trend ->
//                        val value = when (selectedMetric.value) {
//                            0 -> trend.heartRate
//                            1 -> trend.sleepQuality
//                            2 -> trend.steps / 100 // Scale down for display
//                            else -> 0
//                        }
//
//                        val maxValue = when (selectedMetric.value) {
//                            0 -> 100
//                            1 -> 100
//                            2 -> 120 // 12000 steps / 100
//                            else -> 100
//                        }
//
//                        val normalizedHeight = (value.toFloat() / maxValue)
//                        val animatedHeight by animateFloatAsState(
//                            targetValue = normalizedHeight,
//                            animationSpec = tween(1000),
//                            label = "barHeight"
//                        )
//
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Box(
//                                modifier = Modifier
//                                    .width(24.dp)
//                                    .fillMaxHeight(animatedHeight)
//                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
//                                    .background(
//                                        MaterialTheme.colorScheme.primary.copy(
//                                            alpha = 0.3f + 0.7f * normalizedHeight
//                                        )
//                                    )
//                                    .clickable {
//                                        val metricText = when (selectedMetric.value) {
//                                            0 -> "${trend.heartRate} BPM"
//                                            1 -> "${trend.sleepQuality}%"
//                                            2 -> "${trend.steps} 步"
//                                            else -> ""
//                                        }
//
//                                        Toast
//                                            .makeText(
//                                                context,
//                                                "${trend.date}: $metricText",
//                                                Toast.LENGTH_SHORT
//                                            )
//                                            .show()
//                                    }
//                            )
//
//                            Spacer(modifier = Modifier.height(4.dp))
//
//                            Text(
//                                text = trend.date,
//                                style = MaterialTheme.typography.labelSmall,
//                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    val average = when (selectedMetric.value) {
//                        0 -> trends.map { it.heartRate }.average().toInt()
//                        1 -> trends.map { it.sleepQuality }.average().toInt()
//                        2 -> trends.map { it.steps }.average().toInt()
//                        else -> 0
//                    }
//
//                    val unit = when (selectedMetric.value) {
//                        0 -> "BPM"
//                        1 -> "%"
//                        2 -> "步"
//                        else -> ""
//                    }
//
//                    Text(
//                        text = "平均: $average $unit",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.primary,
//                        fontWeight = FontWeight.Bold
//                    )
//
//                    Text(
//                        text = "点击查看详情",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.primary,
//                        fontStyle = FontStyle.Italic
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SettingsSection() {
//    val context = LocalContext.current
//    Column(modifier = Modifier.fillMaxWidth()) {
//        Text(
//            text = "设置与隐私",
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(vertical = 8.dp)
//        )
//
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(20.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                SettingsItem(
//                    title = "通知设置",
//                    subtitle = "接收健康提醒和活动通知",
//                    icon = Icons.Default.Notifications,
//                    hasSwitch = true,
//                    initialSwitchState = true
//                )
//
//                Divider(
//                    modifier = Modifier.padding(vertical = 8.dp),
//                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
//                )
//
//                SettingsItem(
//                    title = "数据同步",
//                    subtitle = "自动同步健康数据到云端",
//                    icon = Icons.Default.CloudSync,
//                    hasSwitch = true,
//                    initialSwitchState = true
//                )
//
//                Divider(
//                    modifier = Modifier.padding(vertical = 8.dp),
//                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
//                )
//
//                SettingsItem(
//                    title = "暗黑模式",
//                    subtitle = "切换应用显示主题",
//                    icon = Icons.Default.ColorLens,
//                    hasSwitch = true,
//                    initialSwitchState = false,
//                    onClick = {
//                        Toast.makeText(context, "切换暗黑模式", Toast.LENGTH_SHORT).show()
//                    }
//                )
//
//                Divider(
//                    modifier = Modifier.padding(vertical = 8.dp),
//                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
//                )
//
//                SettingsItem(
//                    title = "隐私与安全",
//                    subtitle = "管理个人数据和隐私设置",
//                    icon = Icons.Default.Lock,
//                    hasSwitch = false,
//                    onClick = {
//                        Toast.makeText(context, "隐私与安全设置", Toast.LENGTH_SHORT).show()
//                    }
//                )
//
//                Divider(
//                    modifier = Modifier.padding(vertical = 8.dp),
//                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
//                )
//
//                SettingsItem(
//                    title = "健康数据授权",
//                    subtitle = "管理第三方应用数据访问权限",
//                    icon = Icons.Default.MedicalServices,
//                    hasSwitch = false,
//                    onClick = {
//                        Toast.makeText(context, "健康数据授权设置", Toast.LENGTH_SHORT).show()
//                    }
//                )
//
//                Divider(
//                    modifier = Modifier.padding(vertical = 8.dp),
//                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
//                )
//
//                SettingsItem(
//                    title = "语言设置",
//                    subtitle = "选择应用显示语言",
//                    icon = Icons.Default.Language,
//                    hasSwitch = false,
//                    onClick = {
//                        Toast.makeText(context, "语言设置", Toast.LENGTH_SHORT).show()
//                    }
//                )
//
//                Divider(
//                    modifier = Modifier.padding(vertical = 8.dp),
//                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
//                )
//
//                SettingsItem(
//                    title = "清空缓存",
//                    subtitle = "清除应用本地缓存数据",
//                    icon = Icons.Default.Cloud,
//                    hasSwitch = false,
//                    onClick = {
//                        Toast.makeText(context, "缓存已清空", Toast.LENGTH_SHORT).show()
//                    }
//                )
//
//                Divider(
//                    modifier = Modifier.padding(vertical = 8.dp),
//                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
//                )
//
//                SettingsItem(
//                    title = "关于我们",
//                    subtitle = "版本信息和开发团队",
//                    icon = Icons.Default.Info,
//                    hasSwitch = false,
//                    onClick = {
//                        Toast.makeText(context, "关于我们", Toast.LENGTH_SHORT).show()
//                    }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun SettingsItem(
//    title: String,
//    subtitle: String,
//    icon: ImageVector,
//    hasSwitch: Boolean,
//    initialSwitchState: Boolean = false,
//    onClick: () -> Unit = {}
//) {
//    val context = LocalContext.current
//    var switchState by remember { mutableStateOf(initialSwitchState) }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable {
//                if (!hasSwitch) onClick()
//                else {
//                    switchState = !switchState
//                    Toast.makeText(
//                        context,
//                        if (switchState) "$title 已开启" else "$title 已关闭",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//            .padding(vertical = 12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            imageVector = icon,
//            contentDescription = null,
//            tint = MaterialTheme.colorScheme.primary,
//            modifier = Modifier.size(24.dp)
//        )
//
//        Spacer(modifier = Modifier.width(16.dp))
//
//        Column(modifier = Modifier.weight(1f)) {
//            Text(
//                text = title,
//                style = MaterialTheme.typography.bodyLarge
//            )
//
//            Text(
//                text = subtitle,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//            )
//        }
//
//        if (hasSwitch) {
//            Switch(
//                checked = switchState,
//                onCheckedChange = {
//                    switchState = it
//                    Toast.makeText(
//                        context,
//                        if (it) "$title 已开启" else "$title 已关闭",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                },
//                colors = SwitchDefaults.colors(
//                    checkedThumbColor = MaterialTheme.colorScheme.primary,
//                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
//                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
//                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
//                )
//            )
//        } else {
//            Icon(
//                imageVector = Icons.Default.ArrowForward,
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
//                modifier = Modifier.size(20.dp)
//            )
//        }
//    }
//}
//
//@Composable
//fun MembershipCard(
//    onCardClick: () -> Unit
//) {
//    val formatter = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
//    val expiryDate = formatter.format(Date(System.currentTimeMillis() + 180 * 24 * 60 * 60 * 1000L))
//
//    ElevatedCard(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onCardClick),
//        shape = RoundedCornerShape(20.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(170.dp)
//                .background(
//                    brush = Brush.linearGradient(
//                        colors = listOf(
//                            MaterialTheme.colorScheme.primary,
//                            MaterialTheme.colorScheme.tertiary
//                        )
//                    )
//                )
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(20.dp)
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "RayVita·Premium",
//                        style = MaterialTheme.typography.titleLarge,
//                        color = Color.White,
//                        fontWeight = FontWeight.Bold
//                    )
//
//                    Icon(
//                        imageVector = Icons.Default.Badge,
//                        contentDescription = null,
//                        tint = Color.White.copy(alpha = 0.8f),
//                        modifier = Modifier.size(32.dp)
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Text(
//                    text = "专业版会员",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = Color.White.copy(alpha = 0.9f)
//                )
//
//                Spacer(modifier = Modifier.weight(1f))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.Bottom
//                ) {
//                    Column(modifier = Modifier.weight(1f)) {
//                        Text(
//                            text = "会员特权",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = Color.White.copy(alpha = 0.8f)
//                        )
//
//                        Text(
//                            text = "专业数据分析 · 个性化健康方案 · VIP客服",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = Color.White.copy(alpha = 0.9f),
//                            fontWeight = FontWeight.Medium
//                        )
//                    }
//
//                    Column(horizontalAlignment = Alignment.End) {
//                        Text(
//                            text = "有效期至",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = Color.White.copy(alpha = 0.8f)
//                        )
//
//                        Text(
//                            text = expiryDate,
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = Color.White,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun HealthAchievementsSection(
//    achievements: List<Achievement>,
//    onViewAllClick: () -> Unit,
//    onAchievementClick: (Achievement) -> Unit
//) {
//    Column(modifier = Modifier.fillMaxWidth()) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "健康成就",
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold
//            )
//
//            Text(
//                text = "查看全部",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.clickable(onClick = onViewAllClick)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        LazyRow(
//            horizontalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            items(achievements) { achievement ->
//                AchievementCard(
//                    achievement = achievement,
//                    onClick = { onAchievementClick(achievement) }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun AchievementCard(
//    achievement: Achievement,
//    onClick: () -> Unit
//) {
//    ElevatedCard(
//        modifier = Modifier
//            .width(120.dp)
//            .clickable(onClick = onClick),
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = if (achievement.achieved) 4.dp else 1.dp
//        ),
//        colors = CardDefaults.cardColors(
//            containerColor = if (achievement.achieved)
//                MaterialTheme.colorScheme.secondaryContainer
//            else
//                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
//        )
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp)
//        ) {
//            Icon(
//                imageVector = achievement.icon,
//                contentDescription = null,
//                tint = if (achievement.achieved)
//                    MaterialTheme.colorScheme.primary
//                else
//                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
//                modifier = Modifier.size(36.dp)
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = achievement.title,
//                style = MaterialTheme.typography.bodyMedium,
//                fontWeight = if (achievement.achieved) FontWeight.Bold else FontWeight.Normal,
//                textAlign = TextAlign.Center,
//                color = if (achievement.achieved)
//                    MaterialTheme.colorScheme.onSecondaryContainer
//                else
//                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Text(
//                text = if (achievement.achieved) "已获得" else "未完成",
//                style = MaterialTheme.typography.bodySmall,
//                color = if (achievement.achieved)
//                    MaterialTheme.colorScheme.primary
//                else
//                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
//                textAlign = TextAlign.Center
//            )
//        }
//    }
//}
//
//@Composable
//fun HealthTipsSection(
//    tips: List<HealthTipP>,
//    onTipClick: (HealthTipP) -> Unit
//) {
//    Column(modifier = Modifier.fillMaxWidth()) {
//        Text(
//            text = "健康小贴士",
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(vertical = 8.dp)
//        )
//
//        tips.forEach { tip ->
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 6.dp)
//                    .clickable { onTipClick(tip) },
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        imageVector = tip.icon,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.size(24.dp)
//                    )
//
//                    Spacer(modifier = Modifier.width(16.dp))
//
//                    Column(modifier = Modifier.weight(1f)) {
//                        Text(
//                            text = tip.title,
//                            style = MaterialTheme.typography.bodyLarge,
//                            fontWeight = FontWeight.SemiBold
//                        )
//
//                        Text(
//                            text = tip.content,
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
//                            maxLines = 2,
//                            overflow = TextOverflow.Ellipsis
//                        )
//
//                        Text(
//                            text = "来源: ${tip.source}",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.padding(top = 4.dp)
//                        )
//                    }
//
//                    Icon(
//                        imageVector = Icons.Default.ArrowForward,
//                        contentDescription = "查看详情",
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SocialShareCard() {
//    val context = LocalContext.current
//    val platforms = listOf("微信", "微博", "QQ", "朋友圈")
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(20.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Text(
//                text = "分享您的健康成就",
//                style = MaterialTheme.typography.bodyLarge,
//                fontWeight = FontWeight.Medium
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = "让朋友们了解您的健康生活方式",
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                platforms.forEach { platform ->
//                    ShareButton(
//                        icon = Icons.Default.Share,
//                        platform = platform,
//                        onClick = {
//                            Toast
//                                .makeText(context, "分享到 $platform", Toast.LENGTH_SHORT)
//                                .show()
//                        }
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Button(
//                onClick = {
//                    Toast
//                        .makeText(context, "生成健康报告卡片", Toast.LENGTH_SHORT)
//                        .show()
//                },
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text("生成健康报告卡片")
//            }
//        }
//    }
//}
//
//@Composable
//fun ShareButton(
//    icon: ImageVector,
//    platform: String,
//    onClick: () -> Unit
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.clickable(onClick = onClick)
//    ) {
//        Surface(
//            modifier = Modifier.size(48.dp),
//            shape = CircleShape,
//            color = MaterialTheme.colorScheme.primaryContainer
//        ) {
//            Box(contentAlignment = Alignment.Center) {
//                Icon(
//                    imageVector = icon,
//                    contentDescription = platform,
//                    tint = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(4.dp))
//
//        Text(
//            text = platform,
//            style = MaterialTheme.typography.bodySmall
//        )
//    }
//}