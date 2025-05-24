package com.codelab.basiclayouts.ui.screen.home

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import com.codelab.basiclayouts.ui.screen.physnet.PhysnetActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BottomNavBar(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit,
    context: Context
) {
    val colorScheme = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()
    val rippleStates = remember { List(5) { mutableStateOf(false) } }

    // 计算屏幕宽度用于指示器定位
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp) // 增加高度以容纳浮动按钮
    ) {
        // 顶部指示器 - 只为前4个标签显示
        TopIndicator(
            selectedTab = selectedTab,
            screenWidth = screenWidth,
            colorScheme = colorScheme
        )

        // 主要导航栏背景
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp
            ),
            color = colorScheme.surface,
            shadowElevation = 12.dp,
            tonalElevation = 3.dp
        ) {
            // 中央凹槽设计
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val centerX = size.width / 2
                val centerY = 0f
                val radius = 45.dp.toPx()

                // 绘制凹槽阴影
                drawCircle(
                    color = Color.Black.copy(alpha = 0.1f),
                    radius = radius + 3.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY + 15.dp.toPx())
                )
            }

            // 导航项目布局
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val regularItems = listOf(
                    NavItem("Home", Icons.Filled.Home, 0),
                    NavItem("Insights", Icons.Outlined.Analytics, 1),
                    NavItem("Social", Icons.Outlined.Group, 2),
                    NavItem("Profile", Icons.Filled.Person, 3)
                )

                // 左侧两个项目
                regularItems.take(2).forEach { item ->
                    NavBarItem(
                        item = item,
                        isSelected = selectedTab == item.tabIndex,
                        onItemClick = {
                            handleNavigation(context, item.tabIndex)
                            onTabSelect(item.tabIndex)
                        },
                        rippleState = rippleStates[item.tabIndex],
                        colorScheme = colorScheme,
                        coroutineScope = coroutineScope
                    )
                }

                // 中央间隔
                Spacer(modifier = Modifier.width(80.dp))

                // 右侧两个项目
                regularItems.drop(2).forEach { item ->
                    NavBarItem(
                        item = item,
                        isSelected = selectedTab == item.tabIndex,
                        onItemClick = {
                            handleNavigation(context, item.tabIndex)
                            onTabSelect(item.tabIndex)
                        },
                        rippleState = rippleStates[item.tabIndex],
                        colorScheme = colorScheme,
                        coroutineScope = coroutineScope
                    )
                }
            }
        }

        // 浮动加号按钮
        FloatingCenterButton(
            modifier = Modifier.align(Alignment.TopCenter),
            onClick = {
                coroutineScope.launch {
                    rippleStates[4].value = true
                    delay(300)
                    rippleStates[4].value = false
                }
                context.startActivity(Intent(context, PhysnetActivity::class.java))
            },
            rippleState = rippleStates[4],
            colorScheme = colorScheme
        )
    }
}

@Composable
private fun TopIndicator(
    selectedTab: Int,
    screenWidth: androidx.compose.ui.unit.Dp,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    // 指示器位置计算 - 只考虑4个标签，忽略中央按钮
    val indicatorPositions = listOf(0.125f, 0.375f, 0.625f, 0.875f) // 更精确的位置
    val targetPosition = if (selectedTab in 0..3) {
        indicatorPositions[selectedTab]
    } else {
        indicatorPositions[0] // 默认位置
    }

    val indicatorPosition by animateFloatAsState(
        targetValue = targetPosition,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicatorPosition"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .padding(horizontal = 24.dp)
    ) {
        // 发光效果背景
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .offset(x = (indicatorPosition * (screenWidth - 88.dp)))
                .clip(RoundedCornerShape(2.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            colorScheme.primary.copy(alpha = 0.3f),
                            colorScheme.primary,
                            colorScheme.primary.copy(alpha = 0.3f)
                        )
                    )
                )
                .blur(2.dp)
        )

        // 主要指示器
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(3.dp)
                .offset(x = (indicatorPosition * (screenWidth - 88.dp) + 4.dp))
                .clip(RoundedCornerShape(2.dp))
                .background(colorScheme.primary)
        )
    }
}

@Composable
private fun NavBarItem(
    item: NavItem,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    rippleState: androidx.compose.runtime.MutableState<Boolean>,
    colorScheme: androidx.compose.material3.ColorScheme,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    // 动画状态
    val verticalOffset by animateDpAsState(
        targetValue = if (isSelected) (-6).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "offsetY"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "iconScale"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "iconTint"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.7f,
        animationSpec = tween(300),
        label = "textAlpha"
    )

    // 涟漪动画
    val rippleScale by animateFloatAsState(
        targetValue = if (rippleState.value) 2f else 0f,
        animationSpec = tween(400),
        label = "rippleScale"
    )
    val rippleAlpha by animateFloatAsState(
        targetValue = if (rippleState.value) 0f else 0.3f,
        animationSpec = tween(400),
        label = "rippleAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .offset(y = verticalOffset)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                coroutineScope.launch {
                    rippleState.value = true
                    delay(400)
                    rippleState.value = false
                }
                onItemClick()
            }
    ) {
        // 涟漪效果
        Canvas(modifier = Modifier.size(40.dp)) {
            drawCircle(
                color = colorScheme.primary.copy(alpha = rippleAlpha),
                radius = 20.dp.toPx() * rippleScale,
                center = center
            )
        }

        // 选中状态背景
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primary.copy(alpha = 0.12f))
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = iconTint,
                modifier = Modifier
                    .size(24.dp)
                    .scale(iconScale)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.title,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = iconTint.copy(alpha = textAlpha),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FloatingCenterButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    rippleState: androidx.compose.runtime.MutableState<Boolean>,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    // 旋转动画
    var rotationState by remember { mutableStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = rotationState,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rotation"
    )

    // 脉冲动画
    var pulseState by remember { mutableStateOf(1f) }
    val pulseAnimation by animateFloatAsState(
        targetValue = pulseState,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        while (true) {
            pulseState = 0.95f
            delay(2000)
            pulseState = 1.05f
            delay(2000)
        }
    }

    // 点击弹跳效果
    var bounceState by remember { mutableStateOf(1f) }
    val bounceScale by animateFloatAsState(
        targetValue = bounceState,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bounce"
    )

    // 涟漪效果
    val rippleScale by animateFloatAsState(
        targetValue = if (rippleState.value) 1.8f else 0f,
        animationSpec = tween(600),
        label = "rippleScale"
    )
    val rippleAlpha by animateFloatAsState(
        targetValue = if (rippleState.value) 0f else 0.4f,
        animationSpec = tween(600),
        label = "rippleAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(64.dp)
            .scale(bounceScale * pulseAnimation)
            .zIndex(10f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                rotationState += 45f
                bounceState = 0.9f
                onClick()

                // 重置弹跳
                bounceState = 1f
            }
    ) {
        // 外层发光圈
        Canvas(
            modifier = Modifier
                .size(80.dp)
                .scale(pulseAnimation)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colorScheme.primary.copy(alpha = 0.2f),
                        colorScheme.primary.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension / 2.5f,
                center = center
            )
        }

        // 涟漪效果
        Canvas(modifier = Modifier.size(64.dp)) {
            drawCircle(
                color = colorScheme.primary.copy(alpha = rippleAlpha),
                radius = 32.dp.toPx() * rippleScale,
                center = center
            )
        }

        // 外层装饰圆环
        Canvas(modifier = Modifier.size(64.dp)) {
            drawCircle(
                color = colorScheme.primary.copy(alpha = 0.15f),
                radius = 32.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // 主按钮
        Surface(
            modifier = Modifier
                .size(56.dp)
                .rotate(rotation),
            shape = CircleShape,
            color = colorScheme.primary,
            shadowElevation = 16.dp,
            tonalElevation = 6.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colorScheme.primary,
                                colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Start Physnet",
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // 内层装饰点
        repeat(8) { index ->
            val angle = (index * 45f) * (PI / 180f).toFloat()
            val radius = 24.dp
            val x = cos(angle) * radius.value
            val y = sin(angle) * radius.value

            Box(
                modifier = Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primary.copy(alpha = 0.3f))
                    .scale(pulseAnimation)
            )
        }
    }
}

// 数据类
data class NavItem(
    val title: String,
    val icon: ImageVector,
    val tabIndex: Int
)

// 导航处理函数
private fun handleNavigation(context: Context, tabIndex: Int) {
    when (tabIndex) {
        0 -> context.startActivity(Intent(context, HomeActivity::class.java))
        1 -> context.startActivity(Intent(context, com.codelab.basiclayouts.ui.insight.InsightActivity::class.java))
        2 -> context.startActivity(Intent(context, com.codelab.basiclayouts.ui.social.SocialActivity::class.java))
        3 -> context.startActivity(Intent(context, com.codelab.basiclayouts.ui.profile.ProfileActivity::class.java))
    }
}