package com.codelab.basiclayouts

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.ui.theme.MySootheTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class EasterEggState {
    object Hidden : EasterEggState()
    class Activated(val type: EggType) : EasterEggState()
}

enum class EggType { DEVELOPER, HEART, SECRET }

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySootheTheme {
                HomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var easterEgg by remember { mutableStateOf<EasterEggState>(EasterEggState.Hidden) }
    var tapCount by remember { mutableIntStateOf(0) }

    fun showEgg(eggType: EggType) {
        easterEgg = EasterEggState.Activated(eggType)
        tapCount = 0
    }

    LaunchedEffect(easterEgg) {
        if (easterEgg is EasterEggState.Activated) {
            delay(3000)
            easterEgg = EasterEggState.Hidden
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
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
                        .clickable {
                            tapCount++
                            if (tapCount == 7) showEgg(EggType.DEVELOPER)
                        }
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
                            initialValue = 50f,    // 初始值
                            targetValue = 0f,      // 目标值
                            animationSpec = tween(durationMillis = 1000) // 动画配置
                        ) { value, _ ->
                            imageOffset = value    // 更新状态
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ziheng_wang),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .padding(8.dp)
                                .offset(y = imageOffset.dp)
                                .shadow(8.dp, shape = CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        AnimatedVisibility(visible = imageOffset == 0f) {
                            Text(
                                text = "欢迎使用RayVita",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "通过手机摄像头实现心率监测与健康分析",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "  以智能手机为入口，融合非接触式生物传感与多模态AI分析，构建覆盖生理-心理-社交的全维度健康管理网络，打造个人健康数字孪生体。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp)
                        )

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

                FeatureGroups(context)

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
                FooterSection()
            }

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
private fun FeatureGroups(context: android.content.Context) {
    val coreFeatures = listOf(
        FeatureItem(Icons.Default.MonitorHeart, "心率测量", "实时监测心率"),
        FeatureItem(Icons.Default.Assignment, "心率记录", "查看历史数据"),
        FeatureItem(Icons.Default.Assessment, "健康周报", "本周健康分析"),
        FeatureItem(Icons.Default.Emergency, "紧急预警", "异常即时通知")
    )

    val toolFeatures = listOf(
        FeatureItem(Icons.Default.Settings, "个性化设置", "调整监测偏好"),
        FeatureItem(Icons.Default.Feedback, "用户反馈", "使用建议与问题"),
        FeatureItem(Icons.Default.Share, "分享数据", "与他人共享报告"),
        FeatureItem(Icons.Default.Help, "了解更多", "查看更多信息")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .shadow(16.dp, shape = RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            tonalElevation = 8.dp
        ) {
            FeatureGroup(
                title = "健康监测",
                features = coreFeatures,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                onFeatureClick = { featureTitle ->
                    when (featureTitle) {
                        "心率测量" -> context.startActivity(Intent(context, MainActivity::class.java))
                        "心率记录" -> context.startActivity(Intent(context, HeartRateRecordActivity::class.java))
                        "分享数据" -> shareHealthData(context)
                    }
                }
            )
        }

        Surface(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            FeatureGroup(
                title = "工具与服务",
                features = toolFeatures,
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                onFeatureClick = { featureTitle ->
                    when (featureTitle) {
                        "用户反馈" -> sendEmail(context)
                        "分享数据" -> shareHealthData(context)
                        "了解更多" -> context.startActivity(Intent(context, AboutActivity::class.java))
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
    val columns = if (configuration.screenWidthDp >= 600) 3 else 2

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(if (columns == 3) 150.dp else 210.dp)
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
    val scope = rememberCoroutineScope() // 新增协程作用域

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
                        scope.launch { // 使用协程作用域
                            if (title == "心率测量") {
                                Toast.makeText(context, "💖 专家模式已激活！", Toast.LENGTH_SHORT).show()
                            }
                            if (title == "心率记录") {
                                Toast.makeText(context, "💖 秘书模式已激活！", Toast.LENGTH_SHORT).show()

                            }
                        }
                    },
                    onTap = {
                        // 修复点击动画逻辑
                        scope.launch {
                            isPressed = true
                            delay(80) // 保持按压状态
                            onClick()
                            delay(20) // 确保动画完成
                            isPressed = false
                        }
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer {
                        scaleX = if (isPressed) 0.9f else 1f
                        scaleY = if (isPressed) 0.9f else 1f
                    }
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
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
                text = "版权 © 2025 HeartVia",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }

        // 隐藏版本号触发
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
                        null,
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
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }
}

// region 工具函数
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
            putExtra(Intent.EXTRA_SUBJECT, "[HeartVia] 用户反馈")
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "未找到邮件应用", Toast.LENGTH_SHORT).show()
    }
}
// endregion

private data class FeatureItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)