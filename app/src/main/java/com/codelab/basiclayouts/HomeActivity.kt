package com.codelab.basiclayouts

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelab.basiclayouts.ui.theme.MySootheTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySootheTheme {
                // Wrap in Surface to provide MaterialTheme background
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF4F4F4)
                ) {
                    RayVitaApp()
                }
            }
        }
    }
}

@Composable
fun RayVitaApp() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomNavigation(
                selectedTab = selectedTab,
                onTabSelect = { selectedTab = it },
                context = LocalContext.current  // 添加context参数
            )
        },
        containerColor = Color(0xFFF4F4F4)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            // Dashboard Header
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            // Health Statistics & Body Visualization
            MainDashboard()

            // Start Scan Button
            ScanButton()

            // Health Recommendations
            HealthTipsCard()
        }
    }
}

@Composable
fun TopBar() {
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

@Composable
fun MainDashboard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(Color.Transparent)
            .border(
                width = 1.5.dp,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    radius = 800f
                ),
                shape = RoundedCornerShape(36.dp)
            )
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(36.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
    ) {
        // Animation setup
        val infiniteTransition = rememberInfiniteTransition(label = "fusion_transition")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 18000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "gradient_angle"
        )
        val mistOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 600f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 22000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "mist_offset"
        )
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 12000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "pulse_scale"
        )

        // Dynamic color transitions
        val color1 by infiniteTransition.animateColor(
            initialValue = Color(0xFFCCE7FF).copy(alpha = 0.65f), // Soft cyan
            targetValue = Color(0xFFFFB6C1).copy(alpha = 0.6f), // Light coral
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "color1"
        )
        val color2 by infiniteTransition.animateColor(
            initialValue = Color(0xFFE6E6FA).copy(alpha = 0.55f), // Lavender
            targetValue = Color(0xFFFFD700).copy(alpha = 0.5f), // Gold
            animationSpec = infiniteRepeatable(
                animation = tween(24000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "color2"
        )
        val color3 by infiniteTransition.animateColor(
            initialValue = Color(0xFFB0E0E6).copy(alpha = 0.5f), // Powder blue
            targetValue = Color(0xFFFFA07A).copy(alpha = 0.45f), // Light salmon
            animationSpec = infiniteRepeatable(
                animation = tween(26000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "color3"
        )

        // Main gradient layer with rich colors
        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(pulseScale)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(color1, color2, color3, color1.copy(alpha = 0.3f)),
                        start = Offset(
                            x = 300f * cos(angle * PI / 180).toFloat(),
                            y = 300f * sin(angle * PI / 180).toFloat()
                        ),
                        end = Offset(
                            x = 300f * cos((angle + 180) * PI / 180).toFloat(),
                            y = 300f * sin((angle + 180) * PI / 180).toFloat()
                        )
                    )
                )
                .run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        this.blur(45.dp)
                    } else {
                        this.background(Color.White.copy(alpha = 0.15f))
                    }
                }
        )

        // Misty fusion layer with swirling effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            color2.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = Offset(
                            x = mistOffset,
                            y = 400f - mistOffset / 2
                        ),
                        radius = 900f
                    )
                )
                .run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        this.blur(30.dp)
                    } else {
                        this
                    }
                }
        )

        // Content layer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "76",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 72.sp,
                    letterSpacing = (-2).sp,
                    color = Color.Black.copy(alpha = 0.95f)
                )
                Text(
                    text = "bpm",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = Color.Black.copy(alpha = 0.75f)
                )
                Text(
                    text = "98",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 72.sp,
                    letterSpacing = (-2).sp,
                    color = Color.Black.copy(alpha = 0.95f)
                )
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "% SpO2",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black.copy(alpha = 0.75f)
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(top = 24.dp, end = 16.dp)
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    // HeartRateWaveform()
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BodyVisualization()
            }
        }
    }
}

@Composable
fun HeartRateWaveform() {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val animatedX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform"
    )

    val orange = Color(0xFFFF7D45)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val midY = height / 2

        // Starting point
        var currentX = 0f
        var currentY = midY

        // Draw the ECG-like pattern
        val segmentWidth = width / 10

        // Simple ECG pattern
        val points = listOf(
            Offset(currentX, midY),                 // Start
            Offset(currentX + segmentWidth * 0.2f, midY),          // Baseline
            Offset(currentX + segmentWidth * 0.3f, midY - 5),      // P wave
            Offset(currentX + segmentWidth * 0.4f, midY),          // Return to baseline
            Offset(currentX + segmentWidth * 0.5f, midY),          // Brief flat
            Offset(currentX + segmentWidth * 0.6f, midY + 15),     // Q wave
            Offset(currentX + segmentWidth * 0.7f, midY - 40),     // R wave (peak)
            Offset(currentX + segmentWidth * 0.8f, midY + 20),     // S wave
            Offset(currentX + segmentWidth * 0.9f, midY),          // Return to baseline
            Offset(currentX + segmentWidth * 1.1f, midY + 10),     // T wave
            Offset(currentX + segmentWidth * 1.3f, midY),          // Return to baseline
        )

        // Offset for animation
        val offset = (animatedX % (width * 0.5f))

        for (i in 0 until 3) { // Repeat pattern
            val patternOffset = i * segmentWidth * 3f - offset

            for (j in 0 until points.size - 1) {
                val start = Offset(points[j].x + patternOffset, points[j].y)
                val end = Offset(points[j + 1].x + patternOffset, points[j + 1].y)

                if (start.x >= 0 && start.x <= width && end.x >= 0 && end.x <= width) {
                    drawLine(
                        color = orange,
                        start = start,
                        end = end,
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun BodyVisualization() {
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseFactor by pulseTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background blur effect for the body
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .alpha(0.7f)
//                .blur(200.dp)
//                .background(
//                    brush = Brush.radialGradient(
//                        colors = listOf(
//                            Color(0xFFF5F5F5),
//                            Color(0xFFFAFAFA)
//                        )
//                    )
//                )
//        )

        // Virtual human image
//        Image(
//            painter = painterResource(id = R.drawable.man), // 替换成你实际的图片资源
//            contentDescription = "Virtual Human",
//            contentScale = ContentScale.Fit,
//            modifier = Modifier
//                .fillMaxHeight(0.9f) // 根据需要调整比例
//        )

        // The pulsing heart effect
//        Box(
//            modifier = Modifier
//                .size(80.dp)
//                .offset(x = (-20).dp, y = (-30).dp) // Position near the chest
//                .graphicsLayer {
//                    scaleX = pulseFactor
//                    scaleY = pulseFactor
//                }
//                .background(
//                    brush = Brush.radialGradient(
//                        colors = listOf(
//                            Color(0xFFFF7D45).copy(alpha = 0.8f),
//                            Color(0xFFFF7D45).copy(alpha = 0.1f)
//                        )
//                    ),
//                    shape = CircleShape
//                )
//        )
    }
}

@Composable
fun ScanButton() {
    val localContext = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Button(

// 假设 context 未定义，我们从 LocalContext.current 获取上下文
onClick = { localContext.startActivity(android.content.Intent(localContext, com.codelab.basiclayouts.MainActivity::class.java)) },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF0F0F0),
                contentColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Start Scan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun HealthTipsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Hi! Your HRV dropped to 52ms (vs. usual 65ms), with only 1h 4m deep sleep. Stress may be rising. Try a 20-30 min walk today, reduce caffeine (morning HR: 82 vs. 75), and dress warm (12°C, windy). Light yoga tonight could help.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Need more tips?",
                color = Color(0xFF2E7DFF),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                modifier = Modifier.clickable { /* Open detailed view */ }
            )
        }
    }
}


@Composable
fun BottomNavigation(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit,
    context: Context  // 添加context参数
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        containerColor = Color.White
    ) {
        val items = listOf(
            Triple("Home", Icons.Default.Home, 0),
            Triple("History", Icons.Outlined.AccessTime, 1),
            Triple("Analysis", Icons.Outlined.Analytics, 2),
            Triple("Mine", Icons.Default.Person, 3)
        )

        items.forEach { (title, icon, index) ->
            // 定义图标缩放动画
            val scale by animateFloatAsState(
                targetValue = if (selectedTab == index) 1.2f else 1.0f,
                animationSpec = tween(durationMillis = 200),
                label = "iconScale"
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier
                            .size(22.dp)
                            .scale(scale) // 应用缩放动画
                    )
                },
                label = {
                    Text(
                        text = title,
                        fontSize = 12.sp
                    )
                },
                selected = selectedTab == index,
                onClick = {
                    if (index == 3) {
                        // 点击 "Mine" 时跳转到 PersonalActivity.kt
                        val intent = android.content.Intent(context, com.codelab.basiclayouts.PersonalActivity::class.java)
                        context.startActivity(intent)
                    } else {
                        // 其他选项调用传入的选择回调
                        onTabSelect(index)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "RayVita App Preview")
@Composable
fun RayVitaAppPreview() {
    MySootheTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF4F4F4)
        ) {
            RayVitaApp()
        }
    }
}
