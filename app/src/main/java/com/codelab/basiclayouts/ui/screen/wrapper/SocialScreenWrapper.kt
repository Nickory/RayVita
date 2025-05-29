package com.codelab.basiclayouts.ui.screen.wrapper

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelab.basiclayouts.ui.social.SocialScreen
import com.codelab.basiclayouts.viewmodel.social.SocialViewModel
import com.codelab.basiclayouts.viewmodel.social.SocialViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SocialScreenWrapper() {
    val context = LocalContext.current

    // ✅ 修正：正确的ViewModel创建语法
    val viewModel: SocialViewModel = viewModel(
        factory = SocialViewModelFactory(context.applicationContext as Application)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val uiState by viewModel.uiState.collectAsState()

        // ✅ 图片选择器
        val pickMedia = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                val cachedImageUri = cacheImageLocally(context, it)
                viewModel.setSelectedImageUri(cachedImageUri)
            }
        }

        // ✅ 好友搜索启动器
        val friendSearchLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedId = result.data?.getIntExtra("selected_user_id", -1)
                if (selectedId != null && selectedId != -1) {
                    viewModel.sendFriendRequest(selectedId)
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // ✅ 离线模式提示
            AnimatedVisibility(visible = uiState.isOfflineMode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "离线",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "当前为离线模式，显示缓存数据",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            viewModel.refreshFeed()
                            viewModel.refreshFriends()
                            viewModel.refreshFriendRequests()
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        Text("重新连接", Modifier.padding(start = 4.dp))
                    }
                }
            }

            // ✅ 主要社交界面
            SocialScreen(
                uiState = uiState,
                onNavigateToProfile = {
                    // 在导航架构中，这里可以留空或添加导航逻辑
                },
                onRefreshFeed = { viewModel.refreshFeed() },
                onLikePost = { viewModel.likePost(it) },
                onCommentClick = { viewModel.getPostWithComments(it) },
                onAddComment = { postId, comment ->
                    viewModel.commentOnPost(postId, comment)
                },
                onCreatePost = { text, imageUri ->
                    viewModel.createPost(text, imageUri?.toString())
                },
                onPickImage = {
                    pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onAddFriend = {
                    // ✅ 修正：安全的Activity启动
                    try {
                        val intent = Intent().apply {
                            setClassName(
                                context.packageName,
                                "com.codelab.basiclayouts.ui.social.FriendSearchActivity"
                            )
                        }
                        friendSearchLauncher.launch(intent)
                    } catch (e: Exception) {
                        // 如果Activity不存在，可以添加错误处理或日志
                        android.util.Log.w("SocialScreenWrapper", "FriendSearchActivity not found", e)
                        // 可以显示一个提示信息或使用其他方式处理
                    }
                },
                onDeletePost = { viewModel.deletePost(it) },
                onBlockFriend = { friendId, block ->
                    viewModel.blockFriend(friendId, block)
                },
                onProcessFriendRequest = { request, accept ->
                    viewModel.processFriendRequest(
                        requestId = request.request_id,
                        accept = accept
                    )
                },
                onHideCreatePostDialog = { viewModel.hideCreatePostDialog() },
                onShowCreatePostDialog = { viewModel.showCreatePostDialog() },
                onHideCommentsDialog = { viewModel.hideCommentsDialog() },
                onClearErrorMessage = { viewModel.clearErrorMessage() }
            )
        }
    }
}

// ✅ 辅助函数：缓存图片到本地
private fun cacheImageLocally(context: android.content.Context, uri: Uri): Uri {
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "JPEG_${timeStamp}_"
            val imageFile = File.createTempFile(fileName, ".jpg", context.cacheDir)

            FileOutputStream(imageFile).use { output ->
                input.copyTo(output)
            }
            Uri.fromFile(imageFile)
        } ?: uri
    } catch (e: Exception) {
        android.util.Log.e("SocialScreenWrapper", "Failed to cache image", e)
        uri // 返回原始URI作为后备
    }
}