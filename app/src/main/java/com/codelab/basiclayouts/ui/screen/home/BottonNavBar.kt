// BottomNavBar.kt – 极致美化与创意微交互
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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 极致美化的底部导航栏
 * - 流畅的缩放与多层次色彩过渡
 * - 精美的发光光晕效果，带有脉动动画
 * - 优雅的标签淡入淡出，带有弹性曲线
 * - 点击涟漪效果与微妙位移反馈
 * - 增强的视觉层次感与空间感
 * 原有逻辑（回调/索引）保持不变
 */
@Composable
fun BottomNavBar(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit,
    context: Context
) {
    // 扩展主题色系
    val primaryColor = Color(0xFF6C56E0)
    val primaryVariant = Color(0xFF8C76FF)
    val primaryLight = Color(0xFFECE6FF)
    val primaryGlow = Color(0xFFA694FF)
    val inactiveColor = Color(0xFFA4A9B8)
    val backgroundColor = Color(0xFFF8F9FF)

    // 动画作用域
    val coroutineScope = rememberCoroutineScope()

    // 存储涟漪效果状态
    val rippleStates = remember {
        List(4) { mutableStateOf(false) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        // 导航栏阴影与背景
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val items = listOf(
                    Triple("Home", Icons.Filled.Home, 0),        // 改为Home
                    Triple("Analytics", Icons.Outlined.Analytics, 1), // 改为Analytics
                    Triple("Social", Icons.Outlined.Group, 2),   // 改为Social
                    Triple("Profile", Icons.Filled.Person, 3)    // 改为Profile
                )

                items.forEachIndexed { index, (title, icon, tabIndex) ->
                    val selected = selectedTab == tabIndex

                    // 动画参数
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.25f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "iconScale"
                    )

                    val iconTint by animateColorAsState(
                        targetValue = if (selected) primaryColor else inactiveColor,
                        animationSpec = tween(300),
                        label = "iconTint"
                    )

                    val containerSize by animateDpAsState(
                        targetValue = if (selected) 52.dp else 42.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "containerSize"
                    )

                    val containerAlpha by animateFloatAsState(
                        targetValue = if (selected) 0.15f else 0f,
                        animationSpec = tween(300),
                        label = "containerAlpha"
                    )

                    val textScale by animateFloatAsState(
                        targetValue = if (selected) 1f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "textScale"
                    )

                    val elevation by animateDpAsState(
                        targetValue = if (selected) 6.dp else 0.dp,
                        animationSpec = tween(350),
                        label = "elevation"
                    )

                    // 脉动光晕效果
                    var pulseState by remember { mutableStateOf(1f) }
                    val pulseAnimation by animateFloatAsState(
                        targetValue = pulseState,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )

                    // 选中时启动脉动动画
                    LaunchedEffect(selected) {
                        if (selected) {
                            while (true) {
                                pulseState = 0.85f
                                delay(1000)
                                pulseState = 1.15f
                                delay(1000)
                            }
                        } else {
                            pulseState = 1f
                        }
                    }

                    // 涟漪效果动画
                    val rippleScale by animateFloatAsState(
                        targetValue = if (rippleStates[index].value) 2f else 0f,
                        animationSpec = tween(400),
                        label = "rippleScale"
                    )

                    val rippleAlpha by animateFloatAsState(
                        targetValue = if (rippleStates[index].value) 0f else 0.5f,
                        animationSpec = tween(400),
                        label = "rippleAlpha"
                    )

                    // 导航项容器
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(60.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                // 触发涟漪效果
                                coroutineScope.launch {
                                    rippleStates[index].value = true
                                    delay(400)
                                    rippleStates[index].value = false
                                }

                                if (tabIndex == 3) {
                                    context.startActivity(Intent(context, com.codelab.basiclayouts.PersonalActivity::class.java))
                                } else {
                                    onTabSelect(tabIndex)
                                }
                            }
                    ) {
                        // 点击涟漪效果
                        Canvas(modifier = Modifier.size(40.dp)) {
                            drawCircle(
                                color = primaryColor.copy(alpha = rippleAlpha),
                                radius = 20.dp.toPx() * rippleScale,
                                center = center
                            )
                        }

                        // 背景圆形容器
                        if (containerAlpha > 0f) {
                            Box(
                                modifier = Modifier
                                    .size(containerSize)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = containerAlpha))
                                    .shadow(elevation, CircleShape)
                            )
                        }

                        // 发光光晕效果
                        if (selected) {
                            Canvas(
                                modifier = Modifier
                                    .size(60.dp)
                                    .scale(pulseAnimation)
                            ) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            primaryGlow.copy(alpha = 0.5f),
                                            primaryGlow.copy(alpha = 0.2f),
                                            primaryGlow.copy(alpha = 0f)
                                        )
                                    ),
                                    radius = size.minDimension / 3,
                                    center = center
                                )
                            }

                            // 额外的装饰性光晕
                            Canvas(
                                modifier = Modifier
                                    .size(60.dp)
                                    .blur(2.dp)
                            ) {
                                rotate(45f) {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                primaryVariant.copy(alpha = 0.3f),
                                                primaryVariant.copy(alpha = 0f)
                                            ),
                                            center = Offset(center.x + 10f, center.y - 10f)
                                        ),
                                        radius = size.minDimension / 4,
                                        center = Offset(center.x + 10f, center.y - 10f)
                                    )
                                }
                            }
                        }

                        // 图标
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconTint,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(scale)
                                .drawBehind {
                                    if (selected) {
                                        drawCircle(
                                            color = primaryColor.copy(alpha = 0.1f),
                                            radius = 24.dp.toPx()
                                        )
                                    }
                                }
                        )

                        // 标签文本
                        // 改进文本显示布局（调整偏移量和容器高度）
                        Box(
                            modifier = Modifier
                                .offset(y = 18.dp)  // 原22.dp调整为18.dp
                                .height(20.dp),      // 原16.dp调整为20.dp
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,     // 字号微调
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) primaryColor else inactiveColor,
                                modifier = Modifier
                                    .scale(textScale)
                                    .alpha(textScale.coerceIn(0f..1f)) // 添加安全范围限制
                            )
                        }
                    }
                }
            }
        }

        // 选中标记器（顶部小横条）
        val indicators = listOf(0.0f, 0.33f, 0.67f, 1f)
        val currentPosition = indicators[selectedTab]
        val indicatorPosition by animateFloatAsState(
            targetValue = currentPosition,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "indicatorPosition"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 50.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .height(3.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (indicatorPosition * (LocalConfiguration.current.screenWidthDp - 130)).dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                primaryVariant,
                                primaryColor
                            )
                        )
                    )
            )
        }
    }
}