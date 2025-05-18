package com.codelab.basiclayouts.ui.profile

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
import com.codelab.basiclayouts.model.UserInfo
import com.codelab.basiclayouts.ui.screen.home.BottomNavBar
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
                text = "👥 All Users in Database",
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
                        contentDescription = "Refresh all users",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(
                    onClick = { authViewModel.clearAllUsersData() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear users data",
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
                            text = "📭",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No users loaded",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Click refresh to load all users",
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
                    text = "User #${user.user_id}",
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
                            text = "CURRENT",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            UserDetailRow("📧 Email", user.email)
            UserDetailRow("👤 Nickname", user.nickname.ifEmpty { "Not set" })
            UserDetailRow("🎨 Theme", user.theme.capitalize())

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
                        "Profile",
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
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelect = { selectedTab = it },
                context = LocalContext.current
            )
        },
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
                contentDescription = "Change Avatar",
                modifier = Modifier.size(16.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // User info
    Text(
        text = userInfo.nickname.ifEmpty { "User" },
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

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
            Text("Edit Profile")
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
            Text("Sign Out")
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
        text = "Welcome to RayVita",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

    Text(
        text = "Sign in to access all features",
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
            Text("Sign In")
        }

        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier.weight(1f)
        ) {
            Text("Sign Up")
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
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Settings & Support",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            val settingsItems = listOf(
                SettingsItem(Icons.Outlined.Notifications, "Notifications", "Manage alerts and reminders"),
                SettingsItem(Icons.Outlined.Security, "Privacy & Security", "Account and data protection"),
                SettingsItem(Icons.Outlined.Palette, "Appearance", "Theme and display options"),
                SettingsItem(Icons.Outlined.Language, "Language", "Choose your preferred language"),
                SettingsItem(Icons.Outlined.Help, "Help Center", "FAQs and support"),
                SettingsItem(Icons.Outlined.Info, "About", "App info and terms")
            )

            settingsItems.forEachIndexed { index, item ->
                SettingsListItem(item)
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
fun SettingsListItem(item: SettingsItem) {
    Surface(
        onClick = { /* TODO: Handle settings item click */ },
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
                    text = "Choose Your Avatar",
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
                        Text("Cancel")
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
                    text = "🔧 Developer Debug",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                IconButton(
                    onClick = { showPasswordDialog = true }
                ) {
                    Icon(
                        imageVector = if (showDeveloperInfo) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle developer info",
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
                        label = { Text("Current User") },
                        selected = !showAllUsersTab
                    )
                    FilterChip(
                        onClick = { showAllUsersTab = true },
                        label = {
                            Text("All Users (${authUiState.allUsers.size})")
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
                    text = "Enter developer password to view debug information",
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
                    text = "🔐 Developer Access",
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
                    label = { Text("Developer Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
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
                        { Text("Incorrect password", color = MaterialTheme.colorScheme.error) }
                    } else null
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { checkPassword() },
                        enabled = password.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Access")
                    }
                }
            }
        }
    }
}

@Composable
fun DeveloperInfoContent(
    authViewModel: AuthViewModel,
    authUiState: AuthUiState
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // User Session Info
        DeveloperInfoSection(
            title = "🧑 User Session",
            items = listOf(
                "Login Status" to if (authUiState.isLoggedIn) "✅ Logged In" else "❌ Not Logged In",
                "User ID" to (authUiState.userInfo?.user_id?.toString() ?: "N/A"),
                "Email" to (authUiState.userInfo?.email ?: "N/A"),
                "Nickname" to (authUiState.userInfo?.nickname ?: "N/A"),
                "Theme" to (authUiState.userInfo?.theme ?: "N/A"),
                "Avatar Index" to authUiState.selectedAvatarIndex.toString(),
                "Avatar Emoji" to authViewModel.getCurrentAvatar().emoji,
                "Avatar Description" to authViewModel.getCurrentAvatar().description
            )
        )

        // Authentication State
        DeveloperInfoSection(
            title = "🔐 Authentication State",
            items = listOf(
                "Loading" to authUiState.isLoading.toString(),
                "Error Message" to (authUiState.errorMessage ?: "None"),
                "Verification Sent" to authUiState.verificationSent.toString(),
                "Verification Email" to (authUiState.verificationEmail ?: "N/A"),
                "Verification Timestamp" to (authUiState.verificationTimestamp?.toString() ?: "N/A"),
                "Code Expired" to if (authUiState.verificationTimestamp != null)
                    authViewModel.isVerificationCodeExpired().toString() else "N/A"
            )
        )

        // Available Avatars
        DeveloperInfoSection(
            title = "🎭 Available Avatars",
            items = authViewModel.avatarOptions.map { avatar ->
                "Avatar ${avatar.index}" to "${avatar.emoji} ${avatar.description}"
            }
        )

        // App Information
        DeveloperInfoSection(
            title = "📱 App Information",
            items = listOf(
                "App Name" to "RayVita",
                "API Base URL" to "http://47.96.237.130:5000/api/",
                "Session Expiry" to "7 days",
                "Current Time" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date()),
                "Theme Mode" to "System Default",
                "Build Type" to "Debug"
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
                    text = "⚠️ Developer Actions",
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
                        Text("Clear Errors", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { authViewModel.getAllUsers() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("Refresh Users", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { authViewModel.logout() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Force Logout", fontSize = 12.sp)
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