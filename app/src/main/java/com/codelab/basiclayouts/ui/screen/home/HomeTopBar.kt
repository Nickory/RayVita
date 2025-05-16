package com.codelab.basiclayouts.ui.screen.home

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CreativeInteractiveAppName(
            text = "RayVita",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
        )

        SearchButton()
    }
}

@Composable
fun CreativeInteractiveAppName(
    text: String,
    fontSize: TextUnit,
    fontWeight: FontWeight
) {
    var triggerAnimation by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "GradientFlow")

    val gradientShift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GradientShift"
    )

    Row(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { triggerAnimation = !triggerAnimation }
                )
            }
    ) {
        text.forEachIndexed { index, c ->
            val animProgress by animateFloatAsState(
                targetValue = if (triggerAnimation) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 600,
                    delayMillis = index * 100,
                    easing = EaseOutBack
                ),
                label = "CharAnim$index"
            )

            val rotation = when (c.lowercaseChar()) {
                'r' -> 10f * animProgress
                't' -> -10f * animProgress
                else -> 0f
            }

            val offsetY = when (c.lowercaseChar()) {
                'a', 'i' -> -8f * animProgress
                else -> 0f
            }

            val offsetX = when (c.lowercaseChar()) {
                'y' -> 8f * animProgress
                else -> 0f
            }

            val scale = when (c.lowercaseChar()) {
                'v' -> 1f + 0.2f * animProgress
                else -> 1f
            }

            Text(
                text = c.toString(),
                fontSize = fontSize,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFff94af), // 淡蓝
                            Color(0xFF9b7bcf), // 淡紫
                            Color(0xFF18bf85),
                            Color(0xFF182d73) // 淡粉
                        ),
                        start = Offset(gradientShift % 1000f - 600f, 0f),
                        end = Offset((gradientShift % 1000f) + 600f, 1000f)
                    ),
                    shadow = Shadow(
                        color = Color(0xFFB0CFFF).copy(alpha = 0.4f),
                        offset = Offset(0f, 4f),
                        blurRadius = 30f
                    ),
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = rotation
                        translationY = offsetY
                        translationX = offsetX
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}

@Composable
fun SearchButton() {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 0.7f,
        label = "scaleAnimation"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.05f),
                        Color.White.copy(alpha = 0.08f)
                    ),
                    center = Offset(0f, 0f),
                    radius = 10f
                )
            )
            .blur(2.dp) // 加强磨砂质感
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.Black,
            modifier = Modifier.size(32.dp)
        )
    }
}