package com.codelab.basiclayouts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

// 数据模型
data class User(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val connections: List<Connection>
)

data class Connection(
    val userId: String,
    val strength: Float // 关联强度，0.0 - 1.0
)

//import android.content.Context
class SocialActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 生成测试用户数据
            val testUser = generateTestUser()
            // 调用 SocialNetworkScreen
            SocialNetworkScreen(
                currentUser = testUser,
                onUserClick = { userId ->
                    println("跳转到用户界面: $userId")
                    // 这里可以添加跳转逻辑，例如使用 Intent 或 Navigation
                },
                onDelete = { userId ->
                    println("删除好友: $userId")
                },
                onChallenge = { userId ->
                    println("挑战好友: $userId")
                },
                onRecommend = { fromId, toId ->
                    println("将 $fromId 推荐给 $toId")
                },
                onSearch = { query ->
                    println("搜索用户: $query")
                    // 这里可以实现搜索逻辑并更新中心用户
                }
            )
        }
    }
}

// 生成测试用户的函数
fun generateTestUser(): User {
    val friendsCount = Random.nextInt(5, 10) // 随机生成 3-6 个好友
    val connections = List(friendsCount) { index ->
        Connection(
            userId = "friend$index",
            strength = Random.nextFloat() // 随机生成关联强度 (0.0 - 1.0)
        )
    }
    return User(
        id = "me",
        name = "Test User",
        avatarUrl = "https://example.com/avatar.jpg",
        connections = connections
    )
}

// 主界面 Composable
@Composable
fun SocialNetworkScreen(
    currentUser: User,
    onUserClick: (String) -> Unit = {}, // 点击用户头像跳转
    onDelete: (String) -> Unit = {},   // 删除好友
    onChallenge: (String) -> Unit = {}, // 挑战好友
    onRecommend: (String, String) -> Unit = { _, _ -> }, // 推荐好友
    onSearch: (String) -> Unit = {}     // 搜索用户
) {
    var networkOffsetX by remember { mutableStateOf(0f) } // 整个网络的 X 偏移
    var networkOffsetY by remember { mutableStateOf(0f) } // 整个网络的 Y 偏移
    var showSearch by remember { mutableStateOf(false) }  // 是否显示搜索框
    var searchQuery by remember { mutableStateOf("") }    // 搜索输入内容
    var draggedUserId by remember { mutableStateOf<String?>(null) } // 被拖动的用户 ID
    var dragOffset by remember { mutableStateOf(Offset.Zero) }     // 拖动偏移量
    var showDialog by remember { mutableStateOf<String?>(null) }   // 显示长按对话框的用户 ID

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (draggedUserId == null) {
                        // 拖动整个网络
                        networkOffsetX += dragAmount.x
                        networkOffsetY += dragAmount.y
                    }
                }
            }
    ) {
        // 绘制社交网络
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(
                size.width / 2 + networkOffsetX,
                size.height / 2 + networkOffsetY
            )
            // 绘制中心用户头像
            drawCircle(Color.Gray, radius = 50f, center = center)

            val friends = currentUser.connections
            val angleStep = if (friends.isNotEmpty()) 360f / friends.size else 0f
            friends.forEachIndexed { index, connection ->
                val angle = angleStep * index
                val friendOffset = Offset(
                    center.x + 200 * cos(toRadians(angle.toDouble())).toFloat(),
                    center.y + 200 * sin(toRadians(angle.toDouble())).toFloat()
                )

                // 绘制连接线
                drawLine(
                    color = Color.Gray.copy(alpha = connection.strength),
                    start = center,
                    end = friendOffset,
                    strokeWidth = connection.strength * 10f
                )

                // 如果该用户被拖动，则使用拖动位置
                val finalOffset = if (draggedUserId == connection.userId) {
                    friendOffset + dragOffset
                } else {
                    friendOffset
                }

                // 绘制好友头像
                drawCircle(Color.Blue, radius = 30f, center = finalOffset)

                // 处理单击和长按
                Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onUserClick(connection.userId) },
                        onLongPress = { showDialog = connection.userId }
                    )
                    detectDragGestures(
                        onDragStart = { draggedUserId = connection.userId },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount
                        },
                        onDragEnd = {
                            // 检查是否拖到其他头像上
                            friends.forEach { target ->
                                if (target.userId != connection.userId) {
                                    val targetOffset = Offset(
                                        center.x + 200 * cos(toRadians(angleStep * friends.indexOf(target).toDouble())).toFloat(),
                                        center.y + 200 * sin(toRadians(angleStep * friends.indexOf(target).toDouble())).toFloat()
                                    )
                                    val distance = ((finalOffset.x - targetOffset.x).pow(2) + (finalOffset.y - targetOffset.y).pow(2)).pow(0.5f)
                                    if (distance < 60f) { // 60f 是两个头像的直径和
                                        onRecommend(connection.userId, target.userId)
                                    }
                                }
                            }
                            draggedUserId = null
                            dragOffset = Offset.Zero
                        }
                    )
                }
            }
        }

        // 长按对话框
        showDialog?.let { userId ->
            Dialog(onDismissRequest = { showDialog = null }) {
                Surface(
                    modifier = Modifier.padding(16.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("操作", fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            onDelete(userId)
                            showDialog = null
                        }) {
                            Text("删除")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            onChallenge(userId)
                            showDialog = null
                        }) {
                            Text("挑战")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showDialog = null }) {
                            Text("取消")
                        }
                    }
                }
            }
        }

        // 搜索功能
        if (showSearch) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("搜索用户") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    onSearch(searchQuery)
                    showSearch = false
                }),
                trailingIcon = {
                    IconButton(onClick = {
                        onSearch(searchQuery)
                        showSearch = false
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                }
            )
        } else {
            IconButton(
                onClick = { showSearch = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "搜索")
            }
        }
    }
}

@Preview(showBackground = true)
// 测试用例
@Composable
fun PreviewSocialNetworkScreen() {
    val testUser = User(
        id = "me",
        name = "Myself",
        avatarUrl = "",
        connections = listOf(
            Connection("friend1", 0.8f),
            Connection("friend2", 0.5f),
            Connection("friend3", 1.0f)
        )
    )
    SocialNetworkScreen(
        currentUser = testUser,
        onUserClick = { println("Clicked user: $it") },
        onDelete = { println("Deleted user: $it") },
        onChallenge = { println("Challenged user: $it") },
        onRecommend = { from, to -> println("Recommended $from to $to") },
        onSearch = { println("Searched for: $it") }
    )
}