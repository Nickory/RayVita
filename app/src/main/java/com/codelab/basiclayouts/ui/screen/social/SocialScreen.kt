package com.codelab.basiclayouts.ui.social

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.model.Friend
import com.codelab.basiclayouts.model.FriendRequest
import com.codelab.basiclayouts.model.Post
import com.codelab.basiclayouts.viewmodel.social.SocialUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    uiState: SocialUiState,
    onNavigateToProfile: () -> Unit,
    onRefreshFeed: () -> Unit,
    onLikePost: (Int) -> Unit,
    onCommentClick: (Int) -> Unit,
    onAddComment: (Int, String) -> Unit,
    onCreatePost: (String, Uri?) -> Unit,
    onPickImage: () -> Unit,
    onAddFriend: () -> Unit,
    onDeletePost: (Int) -> Unit,
    onBlockFriend: (Int, Boolean) -> Unit,
    onProcessFriendRequest: (FriendRequest, Boolean) -> Unit,
    onHideCreatePostDialog: () -> Unit,
    onShowCreatePostDialog: () -> Unit,
    onHideCommentsDialog: () -> Unit,
    onClearErrorMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message if any
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                onClearErrorMessage()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Health Network") },
                actions = {
                    IconButton(onClick = onRefreshFeed) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) { // Only show on Feed tab
                FloatingActionButton(onClick = onShowCreatePostDialog) {
                    Icon(Icons.Default.Add, contentDescription = "Create Post")
                }
            } else if (selectedTabIndex == 1) { // Add friend button on Friends tab
                FloatingActionButton(onClick = onAddFriend) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Friend")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Feed") },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Friends") },
                    icon = { Icon(Icons.Default.People, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Health Graph") },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) }
                )
            }

            when (selectedTabIndex) {
                0 -> FeedContent(
                    posts = uiState.feedPosts,
                    isLoading = uiState.isLoading,
                    onLikePost = onLikePost,
                    onCommentClick = onCommentClick,
                    onDeletePost = onDeletePost,
                    currentUserId = uiState.currentUserId ?: -1
                )
                1 -> FriendListContent(
                    friends = uiState.friends,
                    friendRequests = uiState.friendRequests,
                    onBlockFriend = onBlockFriend,
                    onProcessRequest = onProcessFriendRequest
                )
                2 -> FriendGraphContent(
                    friends = uiState.friends,
                    currentUserId = uiState.currentUserId ?: -1
                )
            }
        }
    }

    // Dialogs
    if (uiState.isCreatingPost) {
        CreatePostDialog(
            onDismiss = onHideCreatePostDialog,
            onCreatePost = onCreatePost,
            onPickImage = onPickImage,
            selectedImageUri = uiState.selectedImageUri,
            modifier = Modifier
        )
    }

    if (uiState.isViewingComments && uiState.selectedPost != null) {
        CommentDialog(
            post = uiState.selectedPost.post,
            comments = uiState.selectedPost.comments,
            onDismiss = onHideCommentsDialog,
            onAddComment = onAddComment
        )
    }
}

@Composable
fun FeedContent(
    posts: List<Post>,
    isLoading: Boolean,
    onLikePost: (Int) -> Unit,
    onCommentClick: (Int) -> Unit,
    onDeletePost: (Int) -> Unit,
    currentUserId: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading && posts.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (posts.isEmpty()) {
            Text(
                text = "No posts yet. Create a new post to get started!",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(posts.size) { index ->
                    val post = posts[index]
                    PostCard(
                        post = post,
                        onLikeClick = { onLikePost(post.post_id) },
                        onCommentClick = { onCommentClick(post.post_id) },
                        onDeleteClick = if (post.user_id == currentUserId) {
                            { onDeletePost(post.post_id) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
fun FriendListContent(
    friends: List<Friend>,
    friendRequests: List<com.codelab.basiclayouts.model.FriendRequest>,
    onBlockFriend: (Int, Boolean) -> Unit,
    onProcessRequest: (FriendRequest, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (friends.isEmpty() && friendRequests.isEmpty()) {
            Text(
                text = "No friends yet. Find people to connect with!",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Friend requests section
                if (friendRequests.isNotEmpty()) {
                    item {
                        Text(
                            text = "Friend Requests",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    items(friendRequests.size) { index ->
                        val request = friendRequests[index]
                        FriendRequestItem(
                            request = request,
                            onAccept = { onProcessRequest(request, true) },
                            onReject = { onProcessRequest(request, false) }
                        )
                    }

                    item {
                        androidx.compose.material3.Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                // Friends section
                if (friends.isNotEmpty()) {
                    item {
                        Text(
                            text = "My Friends",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    items(friends.size) { index ->
                        val friend = friends[index]
                        FriendItem(
                            friend = friend,
                            onBlockClick = { block -> onBlockFriend(friend.user_id, block) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendRequestItem(
    request: com.codelab.basiclayouts.model.FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${request.from_user_nickname ?: "User ${request.from_user_id}"} wants to be your friend",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                androidx.compose.material3.TextButton(onClick = onReject) {
                    Text("Decline")
                }

                androidx.compose.material3.Button(
                    onClick = onAccept,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Accept")
                }
            }
        }
    }
}

@Composable
fun FriendItem(
    friend: Friend,
    onBlockClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (friend.avatar_index) {
                        1 -> "😊"
                        2 -> "😎"
                        3 -> "⭐"
                        4 -> "❤️"
                        5 -> "👍"
                        6 -> "🚀"
                        else -> "👤"
                    },
                    style = MaterialTheme.typography.headlineMedium
                )
                Column {
                    Text(text = friend.nickname, style = MaterialTheme.typography.titleMedium)
                    friend.email?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            IconButton(onClick = { onBlockClick(!friend.is_blocked) }) {
                Icon(
                    imageVector = if (friend.is_blocked) Icons.Default.PersonOff else Icons.Default.Block,
                    contentDescription = if (friend.is_blocked) "Unblock" else "Block",
                    tint = if (friend.is_blocked) MaterialTheme.colorScheme.error else LocalContentColor.current
                )
            }
        }
    }
}

@Composable
fun FriendGraphContent(
    friends: List<Friend>,
    currentUserId: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        FriendGraph(
            friends = friends,
            currentUserId = currentUserId,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}