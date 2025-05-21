package com.codelab.basiclayouts.ui.social

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.ui.theme.MySootheTheme
import com.codelab.basiclayouts.viewmodel.social.SocialViewModel
import com.codelab.basiclayouts.viewmodel.social.SocialViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SocialActivity : ComponentActivity() {
    companion object {
        private const val TAG = "SocialActivity"
    }

    private val viewModel: SocialViewModel by viewModels {
        SocialViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MySootheTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uiState by viewModel.uiState.collectAsState()

                    // ✅ 如果用户未登录（无 user_id），可以跳转到登录
                    if (uiState.currentUserId == null) {
                        Log.e(TAG, "用户未登录，应跳转登录页")
                        // startActivity(Intent(this, LoginActivity::class.java))
                        // finish()
                        // return@Surface
                    }

                    // 📷 图像选择器
                    val pickMedia = rememberLauncherForActivityResult(
                        ActivityResultContracts.PickVisualMedia()
                    ) { uri ->
                        uri?.let {
                            val cachedImageUri = cacheImageLocally(it)
                            viewModel.setSelectedImageUri(cachedImageUri)
                            Log.d(TAG, "图片缓存到: $cachedImageUri")
                        }
                    }

                    // 🔍 查找好友页
                    val friendSearchLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        if (result.resultCode == RESULT_OK) {
                            val selectedId = result.data?.getIntExtra("selected_user_id", -1)
                            if (selectedId != null && selectedId != -1) {
                                viewModel.sendFriendRequest(selectedId)
                            }
                        }
                    }

                    // 🎯 UI 主体
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // 🔴 离线提示
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

                            // 🧩 社交页面主内容
                            SocialScreen(
                                uiState = uiState,
                                onNavigateToProfile = {
                                    // TODO: 打开 Profile 页面
                                },
                                onRefreshFeed = { viewModel.refreshFeed() },
                                onLikePost = { viewModel.likePost(it) },
                                onCommentClick = { viewModel.getPostWithComments(it) },
                                onAddComment = { postId, comment -> viewModel.commentOnPost(postId, comment) },
                                onCreatePost = { text, imageUri -> viewModel.createPost(text, imageUri?.toString()) },
                                onPickImage = {
                                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                onAddFriend = {
                                    val intent = Intent(this@SocialActivity, FriendSearchActivity::class.java)
                                    friendSearchLauncher.launch(intent)
                                },
                                onDeletePost = { viewModel.deletePost(it) },
                                onBlockFriend = { friendId, block -> viewModel.blockFriend(friendId, block) },
                                onProcessFriendRequest = { request, accept ->
                                    // 这里只传递必要的参数
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
            }
        }
    }

    private fun cacheImageLocally(uri: Uri): Uri {
        contentResolver.openInputStream(uri)?.use { input ->
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "JPEG_${timeStamp}_"
            val imageFile = File.createTempFile(fileName, ".jpg", cacheDir)

            FileOutputStream(imageFile).use { output -> input.copyTo(output) }
            return Uri.fromFile(imageFile)
        }
        return uri
    }
}