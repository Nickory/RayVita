package com.codelab.basiclayouts.ui.screen.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.viewmodel.profile.NotificationSettingsViewModel
import com.codelab.basiclayouts.viewmodel.profile.ReminderFrequency
import com.codelab.basiclayouts.viewmodel.profile.ReminderType
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel,
    onBackClick: () -> Unit,
    onRequestPermission: () -> Unit = {},
    onOpenSystemSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDropdownMenu by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    // 显示保存成功的Snackbar
    LaunchedEffect(uiState.showSaveSuccess) {
        if (uiState.showSaveSuccess) {
            snackbarHostState.showSnackbar(
                message = "Settings saved successfully!",
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            viewModel.hideSaveSuccessMessage()
        }
    }

    // 显示测试通知成功的Snackbar
    LaunchedEffect(uiState.showTestSuccess) {
        if (uiState.showTestSuccess) {
            snackbarHostState.showSnackbar(
                message = "Test notification sent successfully!",
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            viewModel.hideTestSuccessMessage()
        }
    }

    // 显示权限错误的Snackbar
    LaunchedEffect(uiState.showPermissionError) {
        if (uiState.showPermissionError) {
            snackbarHostState.showSnackbar(
                message = "Notification permission required. Please enable in system settings.",
                duration = androidx.compose.material3.SnackbarDuration.Long
            )
            viewModel.hidePermissionErrorMessage()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        // 艺术背景
        NotificationArtisticBackground()

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                LargeTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = stringResource(R.string.notification_title),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.notification_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showDropdownMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.notification_menu),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            DropdownMenu(
                                expanded = showDropdownMenu,
                                onDismissRequest = { showDropdownMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.notification_test_notification)) },
                                    onClick = {
                                        showDropdownMenu = false
                                        viewModel.testNotification()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.notification_system_settings)) },
                                    onClick = {
                                        showDropdownMenu = false
                                        onOpenSystemSettings()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Settings, contentDescription = null)
                                    }
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 主要开关卡片
                item {
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(100)
                        isVisible = true
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = spring(dampingRatio = 0.8f)
                        ) + fadeIn()
                    ) {
                        MainToggleCard(
                            isNotificationEnabled = uiState.isNotificationEnabled,
                            onToggleNotification = { viewModel.toggleNotificationEnabled() }
                        )
                    }
                }

                // 定时提醒设置卡片
                item {
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(200)
                        isVisible = true
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(dampingRatio = 0.8f)
                        ) + fadeIn()
                    ) {
                        TimedReminderCard(
                            isEnabled = uiState.isNotificationEnabled,
                            isTimedReminderEnabled = uiState.isTimedReminderEnabled,
                            reminderFrequency = uiState.reminderFrequency,
                            customReminderHour = uiState.customReminderHour,
                            customReminderMinute = uiState.customReminderMinute,
                            onToggleTimedReminder = { viewModel.toggleTimedReminderEnabled() },
                            onUpdateFrequency = { frequency -> viewModel.updateReminderFrequency(frequency) },
                            onShowTimePicker = { showTimePickerDialog = true }
                        )
                    }
                }

                // 提醒类型设置卡片
                item {
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(300)
                        isVisible = true
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(dampingRatio = 0.8f)
                        ) + fadeIn()
                    ) {
                        ReminderTypeCard(
                            isEnabled = uiState.isNotificationEnabled && uiState.isTimedReminderEnabled,
                            selectedType = uiState.reminderType,
                            onTypeSelected = { type -> viewModel.updateReminderType(type) }
                        )
                    }
                }

                // 提醒内容预览卡片
                item {
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(400)
                        isVisible = true
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(dampingRatio = 0.8f)
                        ) + fadeIn()
                    ) {
                        PreviewCard(
                            isEnabled = uiState.isNotificationEnabled && uiState.isTimedReminderEnabled,
                            previewText = uiState.previewText,
                            includeAiHealthTips = uiState.includeAiHealthTips,
                            onToggleAiTips = { viewModel.toggleAiHealthTips() }
                        )
                    }
                }

                // 操作按钮卡片
                item {
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(500)
                        isVisible = true
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(dampingRatio = 0.8f)
                        ) + fadeIn()
                    ) {
                        ActionButtonsCard(
                            isLoading = uiState.isLoading,
                            onSaveSettings = { viewModel.saveSettings() },
                            onRestoreDefaults = { viewModel.restoreDefaults() }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // 时间选择器对话框
    if (showTimePickerDialog) {
        TimePickerDialog(
            initialHour = uiState.customReminderHour,
            initialMinute = uiState.customReminderMinute,
            onTimeSelected = { hour, minute ->
                viewModel.updateCustomReminderTime(hour, minute)
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }
}

@Composable
private fun NotificationArtisticBackground() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        drawNotificationArtisticBackground(
            drawScope = this,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            tertiaryColor = tertiaryColor,
            surfaceColor = surfaceColor
        )
    }
}

@Composable
fun MainToggleCard(
    isNotificationEnabled: Boolean,
    onToggleNotification: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp), // External padding for screen edge spacing
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.98f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Reduced internal padding for compact layout
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false) // Prevent text from expanding too much
            ) {
                Surface(
                    modifier = Modifier.size(48.dp), // Smaller icon container for balance
                    shape = CircleShape,
                    color = if (isNotificationEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isNotificationEnabled) {
                                Icons.Default.Notifications
                            } else {
                                Icons.Default.NotificationsOff
                            },
                            contentDescription = null,
                            tint = if (isNotificationEnabled) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(24.dp) // Smaller icon for proportion
                        )
                    }
                }

                Column {
                    Text(
                        text = stringResource(R.string.notification_enable_notifications),
                        style = MaterialTheme.typography.titleMedium, // Smaller text style
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1, // Prevent overflow
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.notification_enable_notifications_desc),
                        style = MaterialTheme.typography.bodySmall, // Smaller description text
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(48.dp) // MD3-compliant touch target
                    .clip(CircleShape)
                    .clickable { onToggleNotification() }, // Improve clickability
                color = Color.Transparent
            ) {
                Switch(
                    checked = isNotificationEnabled,
                    onCheckedChange = null, // Handled by Surface click
                    modifier = Modifier.padding(4.dp) // Center switch within touch target
                )
            }
        }
    }
}

@Composable
fun TimedReminderCard(
    isEnabled: Boolean,
    isTimedReminderEnabled: Boolean,
    reminderFrequency: ReminderFrequency,
    customReminderHour: Int,
    customReminderMinute: Int,
    onToggleTimedReminder: () -> Unit,
    onUpdateFrequency: (ReminderFrequency) -> Unit,
    onShowTimePicker: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题和开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.notification_timed_reminder),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.notification_timed_reminder_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isTimedReminderEnabled,
                    onCheckedChange = { onToggleTimedReminder() },
                    enabled = isEnabled
                )
            }

            // 频率设置
            AnimatedVisibility(
                visible = isEnabled && isTimedReminderEnabled,
                enter = expandVertically(spring(dampingRatio = 0.8f)) + fadeIn(),
                exit = shrinkVertically(spring(dampingRatio = 0.8f)) + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = stringResource(R.string.notification_reminder_frequency),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(ReminderFrequency.values()) { index, frequency ->
                            var isVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 100L)
                                isVisible = true
                            }

                            AnimatedVisibility(
                                visible = isVisible,
                                enter = fadeIn() + slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = spring(dampingRatio = 0.8f)
                                )
                            ) {
                                FrequencyChip(
                                    frequency = frequency,
                                    isSelected = frequency == reminderFrequency,
                                    onClick = { onUpdateFrequency(frequency) }
                                )
                            }
                        }
                    }

                    // 自定义时间选择
                    if (reminderFrequency == ReminderFrequency.ONCE_DAILY) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onShowTimePicker() },
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(
                                        R.string.notification_custom_time,
                                        String.format("%02d:%02d", customReminderHour, customReminderMinute)
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FrequencyChip(
    frequency: ReminderFrequency,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        tonalElevation = if (isSelected) 6.dp else 2.dp
    ) {
        Text(
            text = stringResource(frequency.displayKeyId),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
fun ReminderTypeCard(
    isEnabled: Boolean,
    selectedType: ReminderType,
    onTypeSelected: (ReminderType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🔔", fontSize = 24.sp)
                    }
                }

                Column {
                    Text(
                        text = stringResource(R.string.notification_reminder_type),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.notification_reminder_type_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReminderType.values().forEachIndexed { index, type ->
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(index * 100L)
                        isVisible = true
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(dampingRatio = 0.8f)
                        ) + fadeIn()
                    ) {
                        ReminderTypeItem(
                            type = type,
                            isSelected = type == selectedType,
                            isEnabled = isEnabled,
                            onClick = { onTypeSelected(type) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderTypeItem(
    type: ReminderType,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val icon = when (type) {
        ReminderType.NOTIFICATION_BAR -> Icons.Outlined.Notifications
        ReminderType.POPUP -> Icons.Default.NotificationsActive
        ReminderType.SILENT -> Icons.Outlined.VolumeOff
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .selectable(
                selected = isSelected,
                onClick = onClick,
                enabled = isEnabled
            ),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isEnabled) {
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                },
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = stringResource(type.displayKeyId),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isEnabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                },
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                enabled = isEnabled
            )
        }
    }
}

@Composable
fun PreviewCard(
    isEnabled: Boolean,
    previewText: String,
    includeAiHealthTips: Boolean,
    onToggleAiTips: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded },
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "👁️", fontSize = 24.sp)
                            }
                        }

                        Column {
                            Text(
                                text = stringResource(R.string.notification_preview),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.notification_preview_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 预览内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(spring(dampingRatio = 0.8f)) + fadeIn(),
                exit = shrinkVertically(spring(dampingRatio = 0.8f)) + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // AI健康提示开关
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🤖", fontSize = 20.sp)
                            Text(
                                text = stringResource(R.string.notification_ai_health_tips),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Switch(
                            checked = includeAiHealthTips,
                            onCheckedChange = { onToggleAiTips() },
                            enabled = isEnabled
                        )
                    }

                    // 预览框
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEnabled) {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.95f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "RayVita",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            Text(
                                text = if (isEnabled) previewText else stringResource(R.string.notification_preview_disabled),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isEnabled) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                },
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButtonsCard(
    isLoading: Boolean,
    onSaveSettings: () -> Unit,
    onRestoreDefaults: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.notification_actions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 改为垂直布局以避免按钮超出卡片
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 保存按钮
                FilledTonalButton(
                    onClick = onSaveSettings,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.notification_save_settings),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // 恢复默认按钮
                OutlinedButton(
                    onClick = onRestoreDefaults,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text(
                        text = stringResource(R.string.notification_restore_defaults),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.notification_select_time),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.notification_cancel))
                    }

                    FilledTonalButton(
                        onClick = {
                            onTimeSelected(timePickerState.hour, timePickerState.minute)
                        }
                    ) {
                        Text(stringResource(R.string.notification_confirm))
                    }
                }
            }
        }
    }
}

// 绘制通知设置专用的艺术背景
private fun drawNotificationArtisticBackground(
    drawScope: DrawScope,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    surfaceColor: Color
) {
    with(drawScope) {
        // 主背景
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    surfaceColor,
                    primaryColor.copy(alpha = 0.02f),
                    secondaryColor.copy(alpha = 0.01f)
                )
            )
        )

        // 装饰性形状 - 类似通知气泡
        val bubbleRadius = size.width * 0.2f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.06f),
                    primaryColor.copy(alpha = 0.02f),
                    Color.Transparent
                ),
                radius = bubbleRadius
            ),
            radius = bubbleRadius,
            center = Offset(size.width * 0.9f, size.height * 0.1f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.04f),
                    secondaryColor.copy(alpha = 0.01f),
                    Color.Transparent
                ),
                radius = bubbleRadius * 0.7f
            ),
            radius = bubbleRadius * 0.7f,
            center = Offset(size.width * 0.1f, size.height * 0.8f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    tertiaryColor.copy(alpha = 0.03f),
                    tertiaryColor.copy(alpha = 0.01f),
                    Color.Transparent
                ),
                radius = bubbleRadius * 0.5f
            ),
            radius = bubbleRadius * 0.5f,
            center = Offset(size.width * 0.8f, size.height * 0.9f)
        )
    }
}