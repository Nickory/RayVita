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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BottomNavBar(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit,
    context: Context
) {
    val colorScheme = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()
    val rippleStates = remember { List(4) { mutableStateOf(false) } }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp) // 总高度更矮
    ) {
        // 背景卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 0.dp,
                bottomEnd = 0.dp
            ),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val items = listOf(
                    Triple("Home", Icons.Filled.Home, 0),
                    Triple("Insights", Icons.Outlined.Analytics, 1),
                    Triple("Social", Icons.Outlined.Group, 2),
                    Triple("Profile", Icons.Filled.Person, 3)
                )

                items.forEachIndexed { index, (title, icon, tabIndex) ->
                    val selected = selectedTab == tabIndex

                    val verticalOffset by animateDpAsState(
                        targetValue = if (selected) (-12).dp else 0.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                        label = "offsetY"
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.25f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "iconScale"
                    )

                    val iconTint by animateColorAsState(
                        targetValue = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant,
                        animationSpec = tween(300),
                        label = "iconTint"
                    )

                    val containerSize by animateDpAsState(
                        targetValue = if (selected) 52.dp else 42.dp,
                        animationSpec = spring(),
                        label = "containerSize"
                    )

                    val containerAlpha by animateFloatAsState(
                        targetValue = if (selected) 0.12f else 0f,
                        animationSpec = tween(300),
                        label = "containerAlpha"
                    )

                    val textScale by animateFloatAsState(
                        targetValue = if (selected) 1f else 0f,
                        animationSpec = spring(),
                        label = "textScale"
                    )

                    val elevation by animateDpAsState(
                        targetValue = if (selected) 8.dp else 0.dp,
                        animationSpec = tween(300),
                        label = "elevation"
                    )

                    var pulseState by remember { mutableStateOf(1f) }
                    val pulseAnimation by animateFloatAsState(
                        targetValue = pulseState,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )

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

                    val rippleScale by animateFloatAsState(
                        targetValue = if (rippleStates[index].value) 2f else 0f,
                        animationSpec = tween(400),
                        label = "rippleScale"
                    )
                    val rippleAlpha by animateFloatAsState(
                        targetValue = if (rippleStates[index].value) 0f else 0.4f,
                        animationSpec = tween(400),
                        label = "rippleAlpha"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(60.dp)
                            .offset(y = verticalOffset)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
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
                        // 涟漪
                        Canvas(modifier = Modifier.size(40.dp)) {
                            drawCircle(
                                color = colorScheme.primary.copy(alpha = rippleAlpha),
                                radius = 20.dp.toPx() * rippleScale,
                                center = center
                            )
                        }

                        // 背景高光
                        if (containerAlpha > 0f) {
                            Box(
                                modifier = Modifier
                                    .size(containerSize)
                                    .clip(CircleShape)
                                    .background(colorScheme.primary.copy(alpha = containerAlpha))
                                    .shadow(elevation, CircleShape)
                            )
                        }

                        // 脉动发光
                        if (selected) {
                            Canvas(
                                modifier = Modifier
                                    .size(60.dp)
                                    .scale(pulseAnimation)
                            ) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            colorScheme.primary.copy(alpha = 0.4f),
                                            colorScheme.primary.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension / 3,
                                    center = center
                                )
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
                        )

                        // 标签
                        Box(
                            modifier = Modifier
                                .offset(y = 18.dp)
                                .height(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = iconTint,
                                modifier = Modifier
                                    .scale(textScale)
                                    .alpha(textScale.coerceIn(0f, 1f))
                            )
                        }
                    }
                }
            }
        }

        // 顶部滑块指示器
        val indicators = listOf(0.0f, 0.33f, 0.67f, 1f)
        val currentPosition = indicators[selectedTab]
        val indicatorPosition by animateFloatAsState(
            targetValue = currentPosition,
            animationSpec = spring(),
            label = "indicatorPosition"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp) // 更细致
                .align(Alignment.TopCenter)
                .padding(horizontal = 50.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .height(2.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (indicatorPosition * (LocalConfiguration.current.screenWidthDp - 130)).dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colorScheme.primary.copy(alpha = 0.6f),
                                colorScheme.primary
                            )
                        )
                    )
            )
        }
    }
}
