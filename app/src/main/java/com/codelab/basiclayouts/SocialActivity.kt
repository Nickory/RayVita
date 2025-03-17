package com.codelab.basiclayouts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.android.volley.toolbox.ImageRequest
import com.codelab.basiclayouts.ui.theme.MySootheTheme
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// 用户数据模型
data class User(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val connections: List<Connection>
)

data class Connection(
    val userId: String,
    val name: String,
    val avatarUrl: String?,
    val strength: Float // 关联强度，0.0 - 1.0
)

class SocialActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySootheTheme {
                val testUser = generateTestUser()
                SocialNetworkScreen(
                    currentUser = testUser,
                    onUserClick = { userId ->
                        // 处理用户点击
                    },
                    onDelete = { userId ->
                        // 处理删除好友
                    },
                    onChallenge = { userId ->
                        // 处理挑战好友
                    },
                    onRecommend = { fromId, toId ->
                        // 处理推荐
                    },
                    onSearch = { query ->
                        // 处理搜索
                    }
                )
            }
        }
    }
}

// 生成测试用户数据
fun generateTestUser(): User {
    val avatarUrls = listOf(
        "https://randomuser.me/api/portraits/men/1.jpg",
        "https://randomuser.me/api/portraits/women/2.jpg",
        "https://randomuser.me/api/portraits/men/3.jpg",
        "https://randomuser.me/api/portraits/women/4.jpg"
    )

    val connections = List(6) { index ->
        Connection(
            userId = "friend$index",
            name = "Friend ${index + 1}",
            avatarUrl = avatarUrls[index % avatarUrls.size],
            strength = 0.3f + (index / 10f)
        )
    }

    return User(
        id = "me",
        name = "You",
        avatarUrl = "https://randomuser.me/api/portraits/men/10.jpg",
        connections = connections
    )
}

@Composable
fun SocialNetworkScreen(
    currentUser: User,
    onUserClick: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onChallenge: (String) -> Unit = {},
    onRecommend: (String, String) -> Unit = { _, _ -> },
    onSearch: (String) -> Unit = {}
) {
    // 状态管理
    var networkOffsetX by remember { mutableStateOf(0f) }
    var networkOffsetY by remember { mutableStateOf(0f) }
    var networkScale by remember { mutableStateOf(1f) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var draggedUserId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var showDialog by remember { mutableStateOf<String?>(null) }
    var boxSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    // 动画
    val networkScaleAnimated by animateFloatAsState(
        targetValue = networkScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // 屏幕尺寸适配
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // 节点大小计算
    val mainNodeSize = min(screenWidth, screenHeight) * 0.2f
    val friendNodeSize = mainNodeSize * 0.6f
    val orbitRadius = min(screenWidth, screenHeight) * 0.35f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                    )
                )
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (draggedUserId == null) {
                            // 只有当没有拖动用户节点时，才移动整个网络
                            networkOffsetX += dragAmount.x
                            networkOffsetY += dragAmount.y
                        }
                    }
                )
            }
            .onSizeChanged { boxSize = it.toSize() }
    ) {
        if (boxSize != androidx.compose.ui.geometry.Size.Zero) {
            val center = Offset(boxSize.width / 2 + networkOffsetX, boxSize.height / 2 + networkOffsetY)
            val friends = currentUser.connections
            val angleStep = if (friends.isNotEmpty()) 360f / friends.size else 0f

            // 计算朋友节点位置
            val friendOffsets = friends.mapIndexed { index, _ ->
                val angle = angleStep * index
                val radiusInPx = with(LocalDensity.current) { orbitRadius.toPx() }
                Offset(
                    center.x + radiusInPx * cos(Math.toRadians(angle.toDouble())).toFloat(),
                    center.y + radiusInPx * sin(Math.toRadians(angle.toDouble())).toFloat()
                )
            }

            // 绘制连接线
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(networkScaleAnimated)
            ) {
                friends.forEachIndexed { index, connection ->
                    val friendOffset = friendOffsets[index]

                    // 基于连接强度选择颜色
                    val connectionColor = when {
                        connection.strength > 0.8f -> Color(0xFF4CAF50) // 强联系 - 绿色
                        connection.strength > 0.5f -> Color(0xFF2196F3) // 中等联系 - 蓝色
                        else -> Color(0xFFFF9800) // 弱联系 - 橙色
                    }

                    // 计算最终位置（考虑拖动状态）
                    val finalOffset = if (draggedUserId == connection.userId) {
                        friendOffset + dragOffset
                    } else {
                        friendOffset
                    }

                    // 绘制连接线
                    drawLine(
                        color = connectionColor.copy(alpha = 0.7f * connection.strength),
                        start = center,
                        end = finalOffset,
                        strokeWidth = 2f + 6f * connection.strength,
                        cap = StrokeCap.Round
                    )

                    // 添加发光效果
                    drawLine(
                        color = connectionColor.copy(alpha = 0.3f * connection.strength),
                        start = center,
                        end = finalOffset,
                        strokeWidth = 10f * connection.strength,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 渲染中心用户节点
            UserNode(
                user = currentUser,
                offset = center,
                size = mainNodeSize,
                isMainUser = true,
                onTap = { onUserClick(currentUser.id) },
                onLongPress = { showDialog = currentUser.id },
                scale = networkScaleAnimated
            )

            // 渲染朋友节点
            friends.forEachIndexed { index, connection ->
                val friendOffset = friendOffsets[index]
                val finalOffset = if (draggedUserId == connection.userId) {
                    friendOffset + dragOffset
                } else {
                    friendOffset
                }

                UserNode(
                    user = connection,
                    offset = finalOffset,
                    size = friendNodeSize,
                    isMainUser = false,
                    isBeingDragged = draggedUserId == connection.userId,
                    onTap = { onUserClick(connection.userId) },
                    onLongPress = { showDialog = connection.userId },
                    onDragStart = { draggedUserId = connection.userId },
                    onDrag = { change, amount ->
                        change.consume()
                        dragOffset += amount
                    },
                    onDragEnd = {
                        // 检查是否拖到其他节点附近
                        friends.forEach { target ->
                            if (target.userId != connection.userId) {
                                val targetIndex = friends.indexOf(target)
                                val targetOffset = friendOffsets[targetIndex]
                                val distance = sqrt(
                                    (finalOffset.x - targetOffset.x).pow(2) +
                                            (finalOffset.y - targetOffset.y).pow(2)
                                )

                                // 如果距离小于阈值，触发推荐操作
                                val thresholdInPx = with(LocalDensity.current) { (friendNodeSize * 1.2f).toPx() }
                                if (distance < thresholdInPx) {
                                    onRecommend(connection.userId, target.userId)
                                }
                            }
                        }

                        // 重置拖动状态
                        draggedUserId = null
                        dragOffset = Offset.Zero
                    },
                    scale = networkScaleAnimated
                )
            }
        }

        // 显示操作对话框
        showDialog?.let { userId ->
            UserActionDialog(
                userId = userId,
                isCurrentUser = userId == currentUser.id,
                onDelete = {
                    onDelete(userId)
                    showDialog = null
                },
                onChallenge = {
                    onChallenge(userId)
                    showDialog = null
                },
                onDismiss = { showDialog = null }
            )
        }

        // 底部控制栏
        ControlBar(
            showSearch = showSearch,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onSearchSubmit = {
                onSearch(searchQuery)
                showSearch = false
            },
            onToggleSearch = { showSearch = !showSearch },
            onZoomIn = { networkScale = min(networkScale * 1.2f, 2f) },
            onZoomOut = { networkScale = max(networkScale * 0.8f, 0.5f) },
            onResetView = {
                networkOffsetX = 0f
                networkOffsetY = 0f
                networkScale = 1f
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun UserNode(
    user: Any, // 可以是User或Connection
    offset: Offset,
    size: androidx.compose.ui.unit.Dp,
    isMainUser: Boolean,
    isBeingDragged: Boolean = false,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onDragStart: (() -> Unit)? = null,
    onDrag: ((change: androidx.compose.ui.input.pointer.PointerInputChange, amount: Offset) -> Unit)? = null,
    onDragEnd: @Composable (() -> Unit)? = null,
    scale: Float = 1f
) {
    // 提取用户信息
    val userId = when(user) {
        is User -> user.id
        is Connection -> user.userId
        else -> ""
    }

    val name = when(user) {
        is User -> user.name
        is Connection -> user.name
        else -> ""
    }

    val avatarUrl = when(user) {
        is User -> user.avatarUrl
        is Connection -> user.avatarUrl
        else -> null
    }

    val strength = when(user) {
        is Connection -> user.strength
        else -> 1.0f
    }

    // 动画状态
    var isPressed by remember { mutableStateOf(false) }
    val nodeScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else if (isBeingDragged) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200)
    )

    val pulseAnimation = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse Scale"
    )

    // 节点颜色
    val nodeColor = if (isMainUser) {
        MaterialTheme.colorScheme.primary
    } else {
        when {
            strength > 0.8f -> Color(0xFF4CAF50) // 强联系 - 绿色
            strength > 0.5f -> Color(0xFF2196F3) // 中等联系 - 蓝色
            else -> Color(0xFFFF9800) // 弱联系 - 橙色
        }
    }

    // 构建修饰符
    var modifier = Modifier
        .offset {
            IntOffset(
                (offset.x - with(LocalDensity) { size.toPx() } / 2).toInt(),
                (offset.y - with(LocalDensity) { size.toPx() } / 2).toInt()
            )
        }
        .size(size)
        .scale(nodeScale * scale * if (isMainUser) pulseScale else 1f)
        .shadow(if (isBeingDragged) 12.dp else 6.dp, CircleShape)
        .clip(CircleShape)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    nodeColor,
                    nodeColor.copy(alpha = 0.8f)
                )
            )
        )
        .border(
            width = if (isBeingDragged) 3.dp else 1.5.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            shape = CircleShape
        )
        .pointerInput(userId) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = { onTap() },
                onLongPress = { onLongPress() }
            )
        }

    // 如果可以拖动，添加拖动手势
    if (onDragStart != null && onDrag != null && onDragEnd != null) {
        modifier = modifier.pointerInput(userId) {
            detectDragGestures(
                onDragStart = {
                    isPressed = true
                    onDragStart()
                },
                onDrag = onDrag,
                onDragEnd = {
                    isPressed = false
//                    onDragEnd()
                },
                onDragCancel = {
                    isPressed = false
//                    onDragEnd()
                }
            )
        }
    }

    Box(modifier = modifier) {
        // 用户头像
        if (avatarUrl != null) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isMainUser) 4.dp else 3.dp)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = name,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(size / 2)
                    .align(Alignment.Center)
            )
        }

        // 连接强度指示器（只对朋友节点显示）
        if (!isMainUser && !isBeingDragged) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(size / 4)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .border(1.dp, nodeColor, CircleShape)
            ) {
                // 显示1-5颗星表示连接强度
                val stars = (strength * 5).toInt()
                Text(
                    text = "★".repeat(stars),
                    color = nodeColor,
                    fontSize = (size.value / 10).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // 用户名标签
        if (!isBeingDragged) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        nodeColor.copy(alpha = 0.7f)
                    )
                    .padding(if (isMainUser) 4.dp else 2.dp)
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = if (isMainUser) (size.value / 8).sp else (size.value / 10).sp,
                    fontWeight = if (isMainUser) FontWeight.Bold else FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // 拖动时的指示文本
        if (isBeingDragged) {
            Text(
                text = "拖到好友节点推荐",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = (size.value / 12).sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-size * 0.6f))
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(4.dp)
            )
        }
    }
}



@Composable
fun UserActionDialog(
    userId: String,
    isCurrentUser: Boolean,
    onDelete: () -> Unit,
    onChallenge: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isCurrentUser) "您的操作选项" else "好友操作选项",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Divider(Modifier.padding(bottom = 16.dp))

                if (!isCurrentUser) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE57373)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("删除好友")
                    }

                    Button(
                        onClick = onChallenge,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "挑战",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("向好友发起挑战")
                    }
                } else {
                    // 当前用户的操作选项
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "个人资料",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("查看个人资料")
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "取消",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun ControlBar(
    showSearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onToggleSearch: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetView: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, MaterialTheme.shapes.medium)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        MaterialTheme.shapes.medium
                    ),
                placeholder = { Text("搜索用户...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                trailingIcon = {
                    IconButton(onClick = onSearchSubmit) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                }
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, MaterialTheme.shapes.medium)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        MaterialTheme.shapes.medium
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 搜索按钮
                FloatingActionButton(
                    onClick = onToggleSearch,
                    modifier = Modifier.size(40.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 缩放控制
                IconButton(
                    onClick = onZoomIn,
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(
                        text = "+",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onZoomOut,
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(
                        text = "-",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 重置视图
                Button(
                    onClick = onResetView,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        "重置视图",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}