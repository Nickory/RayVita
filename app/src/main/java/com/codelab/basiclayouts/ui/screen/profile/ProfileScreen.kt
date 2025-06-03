package com.codelab.basiclayouts.ui.profile

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.model.UserInfo
import com.codelab.basiclayouts.ui.screen.language.LanguageSelectActivity
import com.codelab.basiclayouts.ui.screen.profile.AboutActivity
import com.codelab.basiclayouts.ui.screen.profile.HelpCenterActivity
import com.codelab.basiclayouts.ui.screen.profile.NotificationSettingsActivity
import com.codelab.basiclayouts.ui.screen.profile.PrivacySecurityActivity
import com.codelab.basiclayouts.ui.screen.themeChange.ThemeSelectorActivity
import com.codelab.basiclayouts.viewmodel.profile.AuthUiState
import com.codelab.basiclayouts.viewmodel.profile.AuthViewModel
import com.codelab.basiclayouts.viewmodel.profile.AvatarOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllUsersContent(
    authViewModel: AuthViewModel,
    authUiState: AuthUiState
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.profile_all_users_in_database),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (authUiState.isLoadingAllUsers) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(
                    onClick = { authViewModel.getAllUsers() },
                    enabled = !authUiState.isLoadingAllUsers
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.profile_refresh_all_users),
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(
                    onClick = { authViewModel.clearAllUsersData() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.profile_clear_users_data),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (authUiState.allUsers.isEmpty() && !authUiState.isLoadingAllUsers) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.profile_empty_mailbox_emoji),
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.profile_no_users_loaded),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.profile_click_refresh_to_load),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            // Users list
            authUiState.allUsers.forEachIndexed { index, user ->
                UserInfoCard(
                    user = user,
                    index = index + 1,
                    isCurrentUser = user.user_id == authUiState.userInfo?.user_id
                )
            }
        }

    }
}

@Composable
fun UserInfoCard(
    user: UserInfo,
    index: Int,
    isCurrentUser: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isCurrentUser) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
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
                    text = stringResource(R.string.profile_user_number, user.user_id),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                if (isCurrentUser) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = stringResource(R.string.profile_current_user_label),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            UserDetailRow(stringResource(R.string.profile_email_label), user.email)
            user.nickname?.let {
                UserDetailRow(
                    stringResource(R.string.profile_nickname_label),
                    it.ifEmpty { stringResource(R.string.profile_not_set) }
                )
            }
            UserDetailRow(
                stringResource(R.string.profile_theme_label),
                user.theme?.replaceFirstChar { it.titlecase() } ?: stringResource(R.string.profile_not_set)
            )

            // Additional info if available
            if (user.toString().contains("registration_dt")) {
                // If we have more fields in the user model, we can display them here
                // This is for future extension
            }
        }
    }
}

@Composable
fun UserDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End
        )
    }
}


@Composable
fun StatisticRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // Get UI state
    val authUiState by authViewModel.uiState.collectAsState()

    // Local state management
    var selectedTab by remember { mutableStateOf(3) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showAvatarDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
//        bottomBar = {
//            BottomNavBar(
//                selectedTab = selectedTab,
//                onTabSelect = { selectedTab = it },
//                context = LocalContext.current
//            )
//        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Profile Header Card
                ProfileHeaderCard(
                    authUiState = authUiState,
                    currentAvatar = authViewModel.getCurrentAvatar(),
                    onLoginClick = { navController.navigate("login") },
                    onRegisterClick = { navController.navigate("register") },
                    onEditProfileClick = { /* TODO: Navigate to edit profile */ },
                    onAvatarClick = { showAvatarDialog = true },
                    onLogoutClick = { authViewModel.logout() }
                )
            }

            item {
                // Settings Card
                SettingsCard()
            }

            item {
                // Developer Debug Card
                DeveloperDebugCard(
                    authViewModel = authViewModel,
                    authUiState = authUiState
                )
            }
        }
    }

    // Avatar Selection Dialog
    if (showAvatarDialog) {
        AvatarSelectionDialog(
            currentAvatarIndex = authUiState.selectedAvatarIndex,
            avatarOptions = authViewModel.avatarOptions,
            onAvatarSelected = { avatarIndex ->
                authViewModel.updateSelectedAvatar(avatarIndex)
                showAvatarDialog = false
            },
            onDismiss = { showAvatarDialog = false }
        )
    }
}

@Composable
fun ProfileHeaderCard(
    authUiState: com.codelab.basiclayouts.viewmodel.profile.AuthUiState,
    currentAvatar: AvatarOption,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (authUiState.isLoggedIn && authUiState.userInfo != null) {
                // Logged in state
                LoggedInProfileHeader(
                    userInfo = authUiState.userInfo!!,
                    currentAvatar = currentAvatar,
                    onEditProfileClick = onEditProfileClick,
                    onAvatarClick = onAvatarClick,
                    onLogoutClick = onLogoutClick
                )
            } else {
                // Not logged in state
                NotLoggedInProfileHeader(
                    currentAvatar = currentAvatar,
                    onAvatarClick = onAvatarClick,
                    onLoginClick = onLoginClick,
                    onRegisterClick = onRegisterClick
                )
            }
        }
    }
}

@Composable
fun LoggedInProfileHeader(
    userInfo: com.codelab.basiclayouts.model.UserInfo,
    currentAvatar: AvatarOption,
    onEditProfileClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // Avatar with edit button
    Box(
        contentAlignment = Alignment.BottomEnd
    ) {
        Surface(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            onClick = onAvatarClick,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = currentAvatar.emoji,
                    fontSize = 48.sp
                )
            }
        }

        FloatingActionButton(
            onClick = onAvatarClick,
            modifier = Modifier.size(32.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.profile_change_avatar),
                modifier = Modifier.size(16.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // User info
    userInfo.nickname?.let {
        Text(
            text = it.ifEmpty { stringResource(R.string.profile_default_user_name) },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    Text(
        text = userInfo.email,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Action buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilledTonalButton(
            onClick = onEditProfileClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.profile_edit_profile))
        }

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.profile_sign_out))
        }
    }
}

@Composable
fun NotLoggedInProfileHeader(
    currentAvatar: AvatarOption,
    onAvatarClick: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    // Avatar
    Surface(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape),
        onClick = onAvatarClick,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = currentAvatar.emoji,
                fontSize = 48.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.profile_welcome_to_rayvita),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

    Text(
        text = stringResource(R.string.profile_sign_in_to_access_features),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Sign in buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onLoginClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.profile_sign_in))
        }

        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.profile_sign_up))
        }
    }
}


@Composable
fun QuickActionItem(action: QuickAction) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        FilledIconButton(
            onClick = { /* TODO: Handle action */ },
            modifier = Modifier.size(56.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = action.color.copy(alpha = 0.2f),
                contentColor = action.color
            )
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = action.title,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            maxLines = 2
        )
    }
}

@Composable
fun SettingsCard() {
    val context = LocalContext.current // 获取当前上下文

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_settings_and_support),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            val settingsItems = listOf(
                SettingsItem(
                    Icons.Outlined.Notifications,
                    stringResource(R.string.profile_notifications),
                    stringResource(R.string.profile_manage_alerts_and_reminders)
                ),
                SettingsItem(
                    Icons.Outlined.Security,
                    stringResource(R.string.profile_privacy_and_security),
                    stringResource(R.string.profile_account_and_data_protection)
                ),
                SettingsItem(
                    Icons.Outlined.Palette,
                    stringResource(R.string.profile_appearance),
                    stringResource(R.string.profile_theme_and_display_options)
                ),
                SettingsItem(
                    Icons.Outlined.Language,
                    stringResource(R.string.profile_language),
                    stringResource(R.string.profile_choose_preferred_language)
                ),
                SettingsItem(
                    Icons.Outlined.Help,
                    stringResource(R.string.profile_help_center),
                    stringResource(R.string.profile_faqs_and_support)
                ),
                SettingsItem(
                    Icons.Outlined.Info,
                    stringResource(R.string.profile_about),
                    stringResource(R.string.profile_app_info_and_terms)
                )
            )

            settingsItems.forEachIndexed { index, item ->
                SettingsListItem(
                    item = item,
                    onClick = {
                        // 处理点击事件
                        when (item.title) {
                            context.getString(R.string.profile_appearance) -> {
                                ThemeSelectorActivity.start(context) // 跳转到 ThemeSelectorActivity
                            }
                            context.getString(R.string.profile_language) -> {
                                // 跳转到语言选择Activity
                                val intent = Intent(context, LanguageSelectActivity::class.java)
                                context.startActivity(intent)
                            }
                            context.getString(R.string.profile_about) -> {
                                // 跳转到关于页面
                                AboutActivity.start(context)
                            }
                            context.getString(R.string.profile_help_center) -> {
                                // 跳转到帮助中心Activity
                                HelpCenterActivity.start(context)
                            }
                            context.getString(R.string.profile_notifications) -> {
                                // 跳转到帮助中心Activity
                                NotificationSettingsActivity.start(context)
                            }
                            context.getString(R.string.profile_privacy_and_security) -> {
                                // 跳转到帮助中心Activity
                                PrivacySecurityActivity.start(context)
                            }
                            // 其他设置项的点击事件可以在这里添加
                            else -> {
                                // TODO: 处理其他设置项的点击
                            }
                        }
                    }
                )
                if (index < settingsItems.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsListItem(
    item: SettingsItem,
    onClick: () -> Unit // 添加 onClick 参数
) {
    Surface(
        onClick = onClick, // 使用传入的 onClick 回调
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (item.subtitle.isNotEmpty()) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AvatarSelectionDialog(
    currentAvatarIndex: Int,
    avatarOptions: List<AvatarOption>,
    onAvatarSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_choose_your_avatar),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(avatarOptions) { avatar ->
                        AvatarOption(
                            avatar = avatar,
                            isSelected = avatar.index == currentAvatarIndex,
                            onClick = { onAvatarSelected(avatar.index) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.profile_cancel))
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarOption(
    avatar: AvatarOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(60.dp)
            .selectable(
                selected = isSelected,
                onClick = onClick
            ),
        shape = CircleShape,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null,
        tonalElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = avatar.emoji,
                fontSize = 28.sp
            )
        }
    }
}

// Data classes
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeveloperDebugCard(
    authViewModel: AuthViewModel,
    authUiState: AuthUiState
) {
    var showDeveloperInfo by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAllUsersTab by remember { mutableStateOf(false) }

    // Load all users when developer info is shown
    LaunchedEffect(showDeveloperInfo) {
        if (showDeveloperInfo && authUiState.allUsers.isEmpty() && !authUiState.isLoadingAllUsers) {
            authViewModel.getAllUsers()
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.profile_developer_debug),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                IconButton(
                    onClick = { showPasswordDialog = true }
                ) {
                    Icon(
                        imageVector = if (showDeveloperInfo) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = stringResource(R.string.profile_toggle_developer_info),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (showDeveloperInfo) {
                Spacer(modifier = Modifier.height(16.dp))

                // Tab selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { showAllUsersTab = false },
                        label = { Text(stringResource(R.string.profile_current_user)) },
                        selected = !showAllUsersTab
                    )
                    FilterChip(
                        onClick = { showAllUsersTab = true },
                        label = {
                            Text(stringResource(R.string.profile_all_users_with_count, authUiState.allUsers.size))
                        },
                        selected = showAllUsersTab
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (showAllUsersTab) {
                    AllUsersContent(authViewModel, authUiState)
                } else {
                    DeveloperInfoContent(authViewModel, authUiState)
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.profile_enter_developer_password),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }
        }
    }

    // Password Dialog
    if (showPasswordDialog) {
        DeveloperPasswordDialog(
            onPasswordCorrect = {
                showDeveloperInfo = true
                showPasswordDialog = false
            },
            onDismiss = { showPasswordDialog = false }
        )
    }
}

@Composable
fun DeveloperPasswordDialog(
    onPasswordCorrect: () -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val correctPassword = "RayVita"

    fun checkPassword() {
        if (password == correctPassword) {
            onPasswordCorrect()
        } else {
            error = true
            password = ""
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_developer_access),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        error = false
                    },
                    label = { Text(stringResource(R.string.profile_developer_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) stringResource(R.string.profile_hide_password) else stringResource(R.string.profile_show_password)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            checkPassword()
                        }
                    ),
                    isError = error,
                    supportingText = if (error) {
                        { Text(stringResource(R.string.profile_incorrect_password), color = MaterialTheme.colorScheme.error) }
                    } else null
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.profile_cancel))
                    }

                    Button(
                        onClick = { checkPassword() },
                        enabled = password.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.profile_access))
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeveloperInfoContent(
    authViewModel: AuthViewModel,
    authUiState: AuthUiState
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // User Session Info
        DeveloperInfoSection(
            title = stringResource(R.string.profile_user_session),
            items = listOf(
                stringResource(R.string.profile_login_status) to if (authUiState.isLoggedIn) stringResource(R.string.profile_logged_in) else stringResource(R.string.profile_not_logged_in),
                stringResource(R.string.profile_user_id) to (authUiState.userInfo?.user_id?.toString() ?: stringResource(R.string.profile_not_available)),
                stringResource(R.string.profile_email) to (authUiState.userInfo?.email ?: stringResource(R.string.profile_not_available)),
                stringResource(R.string.profile_nickname) to (authUiState.userInfo?.nickname ?: stringResource(R.string.profile_not_available)),
                stringResource(R.string.profile_theme) to (authUiState.userInfo?.theme ?: stringResource(R.string.profile_not_available)),
                stringResource(R.string.profile_avatar_index) to authUiState.selectedAvatarIndex.toString(),
                stringResource(R.string.profile_avatar_emoji) to authViewModel.getCurrentAvatar().emoji,
                stringResource(R.string.profile_avatar_description) to authViewModel.getCurrentAvatar().description
            )
        )

        // Authentication State
        DeveloperInfoSection(
            title = stringResource(R.string.profile_authentication_state),
            items = listOf(
                stringResource(R.string.profile_loading) to authUiState.isLoading.toString(),
                stringResource(R.string.profile_error_message) to (authUiState.errorMessage ?: stringResource(R.string.profile_none)),
                stringResource(R.string.profile_verification_sent) to authUiState.verificationSent.toString(),
                stringResource(R.string.profile_verification_email) to (authUiState.verificationEmail ?: stringResource(R.string.profile_not_available)),
                stringResource(R.string.profile_verification_timestamp) to (authUiState.verificationTimestamp?.toString() ?: stringResource(R.string.profile_not_available)),
                stringResource(R.string.profile_code_expired) to if (authUiState.verificationTimestamp != null)
                    authViewModel.isVerificationCodeExpired().toString() else stringResource(R.string.profile_not_available)
            )
        )

        // Available Avatars
        DeveloperInfoSection(
            title = stringResource(R.string.profile_available_avatars),
            items = authViewModel.avatarOptions.map { avatar ->
                stringResource(R.string.profile_avatar_number, avatar.index) to "${avatar.emoji} ${avatar.description}"
            }
        )

        // App Information
        DeveloperInfoSection(
            title = stringResource(R.string.profile_app_information),
            items = listOf(
                stringResource(R.string.profile_app_name) to stringResource(R.string.profile_app_name_rayvita),
                stringResource(R.string.profile_api_base_url) to stringResource(R.string.profile_api_url),
                stringResource(R.string.profile_session_expiry) to stringResource(R.string.profile_seven_days),
                stringResource(R.string.profile_current_time) to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date()),
                stringResource(R.string.profile_theme_mode) to stringResource(R.string.profile_system_default),
                stringResource(R.string.profile_build_type) to stringResource(R.string.profile_debug)
            )
        )

        // Actions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_developer_actions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { authViewModel.clearErrorMessage() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.profile_clear_errors), fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { authViewModel.getAllUsers() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text(stringResource(R.string.profile_refresh_users), fontSize = 12.sp)
                    }

                    Button(
                        onClick = { authViewModel.logout() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.profile_force_logout), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DeveloperInfoSection(
    title: String,
    items: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            items.forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

data class QuickAction(
    val icon: ImageVector,
    val title: String,
    val color: Color
)

data class SettingsItem(

    val icon: ImageVector,
    val title: String,
    val subtitle: String = ""
)