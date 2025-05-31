// ============== component/TopBarComponents.kt ==============
package com.codelab.basiclayouts.ui.screen.home.component

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Enhanced RayVita Logo with rich theme color gradients
@Composable
fun EnhancedRayVitaLogo() {
    val primary = colorScheme.primary
    val secondary = colorScheme.secondary
    val tertiary = colorScheme.tertiary
    val primaryContainer = colorScheme.primaryContainer
    val secondaryContainer = colorScheme.secondaryContainer
    val tertiaryContainer = colorScheme.tertiaryContainer
    val onPrimary = colorScheme.onPrimary
    val onSecondary = colorScheme.onSecondary
    val onTertiary = colorScheme.onTertiary

    var triggerAnimation by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "logoAnimation")

    // 多层渐变动画
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GradientShift"
    )

    val secondaryGradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SecondaryGradientShift"
    )

    val colorCycleAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ColorCycle"
    )

    // Auto trigger animation
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            triggerAnimation = !triggerAnimation
        }
    }

    // 创建丰富的主题色渐变
    val createRichGradient = { offset: Float ->
        val cycle = colorCycleAnimation
        val baseColors = listOf(
            primary,
            secondary,
            tertiary,
            primaryContainer,
//            secondaryContainer,
//            tertiaryContainer
        )

        val dynamicColors = baseColors.mapIndexed { index, color ->
            val alpha = 0.8f + 0.2f * kotlin.math.sin((cycle * 2 * kotlin.math.PI + index * 0.5).toFloat())
            color.copy(alpha = alpha)
        }

        Brush.linearGradient(
            colors = dynamicColors + listOf(
                primary.copy(alpha = 0.9f),
                secondary.copy(alpha = 0.8f),
                tertiary.copy(alpha = 0.9f)
            ),
            start = Offset(
                (gradientShift + offset) % 1200f - 600f,
                (secondaryGradientShift + offset * 0.5f) % 800f - 400f
            ),
            end = Offset(
                (gradientShift + offset + 800f) % 1200f - 600f,
                (secondaryGradientShift + offset * 0.5f + 600f) % 800f - 400f
            )
        )
    }

    Row(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { triggerAnimation = !triggerAnimation }
                )
            }
    ) {
        "RayVita".forEachIndexed { index, char ->
            val animProgress by animateFloatAsState(
                targetValue = if (triggerAnimation) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 800,
                    delayMillis = index * 120,
                    easing = EaseOutBack
                ),
                label = "CharAnim$index"
            )

            // 增强的字符动画效果
            val rotation = when (char.lowercaseChar()) {
                'r' -> 12f * animProgress
                'a' -> -8f * animProgress
                'y' -> 15f * animProgress
                'v' -> -12f * animProgress
                'i' -> 8f * animProgress
                't' -> -10f * animProgress
                else -> 0f
            }

            val offsetY = when (char.lowercaseChar()) {
                'a', 'i' -> -10f * animProgress
                'y', 'v' -> -6f * animProgress
                'r', 't' -> -4f * animProgress
                else -> 0f
            }

            val offsetX = when (char.lowercaseChar()) {
                'y' -> 6f * animProgress
                'v' -> -4f * animProgress
                'i' -> 3f * animProgress
                else -> 0f
            }

            val scale = when (char.lowercaseChar()) {
                'r' -> 1f + 0.15f * animProgress
                'v' -> 1f + 0.25f * animProgress
                'a' -> 1f + 0.1f * animProgress
                else -> 1f + 0.05f * animProgress
            }

            // 动态阴影颜色
            val shadowColor = when (index % 3) {
                0 -> primary.copy(alpha = 0.4f + 0.2f * animProgress)
                1 -> secondary.copy(alpha = 0.4f + 0.2f * animProgress)
                else -> tertiary.copy(alpha = 0.4f + 0.2f * animProgress)
            }

            Text(
                text = char.toString(),
                fontSize = (26 + 2 * animProgress).sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    brush = createRichGradient(index * 100f),
                    shadow = Shadow(
                        color = shadowColor,
                        offset = Offset(
                            2f * (1f + animProgress),
                            4f * (1f + animProgress)
                        ),
                        blurRadius = 25f + 15f * animProgress
                    ),
                    letterSpacing = (1.5f + 0.5f * animProgress).sp
                ),
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = rotation
                        translationY = offsetY
                        translationX = offsetX
                        scaleX = scale
                        scaleY = scale
                        alpha = 0.9f + 0.1f * animProgress
                    }
            )
        }
    }
}

// 简化版RayVita Logo (性能优化版本)
@Composable
fun SimplifiedRayVitaLogo() {
    val primary = colorScheme.primary
    val secondary = colorScheme.secondary
    val tertiary = colorScheme.tertiary

    val infiniteTransition = rememberInfiniteTransition(label = "simpleLogoAnimation")

    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SimpleGradientShift"
    )

    Text(
        text = "RayVita",
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        style = TextStyle(
            brush = Brush.linearGradient(
                colors = listOf(
                    primary,
                    secondary,
                    tertiary,
                    primary.copy(alpha = 0.8f),
                    secondary.copy(alpha = 0.9f),
                    tertiary.copy(alpha = 0.8f)
                ),
                start = Offset(gradientShift % 600f - 300f, 0f),
                end = Offset((gradientShift % 600f) + 300f, 200f)
            ),
            shadow = Shadow(
                color = primary.copy(alpha = 0.3f),
                offset = Offset(0f, 3f),
                blurRadius = 20f
            ),
            letterSpacing = 1.5.sp
        )
    )
}

// Refined Top Bar with enhanced logo
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefinedTopBar(
    useSimplified: Boolean = false
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (useSimplified) {
                    SimplifiedRayVitaLogo()
                } else {
                    EnhancedRayVitaLogo()
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}