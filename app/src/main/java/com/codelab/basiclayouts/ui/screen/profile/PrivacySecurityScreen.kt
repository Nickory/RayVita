package com.codelab.basiclayouts.ui.screen.profile


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelab.basiclayouts.R
import kotlinx.coroutines.delay

data class PrivacySecurityItem(
    val icon: ImageVector,
    val titleRes: Int,
    val descriptionRes: Int,
    val containerColor: Color? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PrivacySecurityScreen(
    onBackClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showEasterEggDialog by remember { mutableStateOf(false) }

    val privacyItems = listOf(
        PrivacySecurityItem(
            icon = Icons.Default.Lock,
            titleRes = R.string.privacy_password_security_title,
            descriptionRes = R.string.privacy_password_security_desc
        ),
        PrivacySecurityItem(
            icon = Icons.Default.Folder,
            titleRes = R.string.privacy_local_first_title,
            descriptionRes = R.string.privacy_local_first_desc
        ),
        PrivacySecurityItem(
            icon = Icons.Default.Cloud,
            titleRes = R.string.privacy_cloud_sync_title,
            descriptionRes = R.string.privacy_cloud_sync_desc
        ),
        PrivacySecurityItem(
            icon = Icons.Default.Handshake,
            titleRes = R.string.privacy_data_usage_title,
            descriptionRes = R.string.privacy_data_usage_desc
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        // 艺术背景
        PrivacyArtisticBackground()

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                LargeTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = stringResource(R.string.privacy_title),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.privacy_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
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
                // 隐私与安全介绍卡片
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
                        IntroCard()
                    }
                }

                // 隐私政策项目列表
                itemsIndexed(privacyItems) { index, item ->
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay((index + 1) * 150L)
                        isVisible = true
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(dampingRatio = 0.8f)
                        ) + fadeIn()
                    ) {
                        PrivacyItemCard(item = item)
                    }
                }

                // 彩蛋区域
                item {
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(800)
                        isVisible = true
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = expandVertically(spring(dampingRatio = 0.8f)) + fadeIn(),
                        exit = shrinkVertically(spring(dampingRatio = 0.8f)) + fadeOut()
                    ) {
                        EasterEggCard(
                            onEasterEggTriggered = { showEasterEggDialog = true }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // 彩蛋对话框
    if (showEasterEggDialog) {
        PrivacyEasterEggDialog(
            onDismiss = { showEasterEggDialog = false }
        )
    }
}

@Composable
private fun PrivacyArtisticBackground() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        drawPrivacyArtisticBackground(
            drawScope = this,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            tertiaryColor = tertiaryColor,
            surfaceColor = surfaceColor
        )
    }
}

@Composable
fun IntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.98f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🛡️", fontSize = 28.sp)
                    }
                }

                Column {
                    Text(
                        text = stringResource(R.string.privacy_intro_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.privacy_intro_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = stringResource(R.string.privacy_intro_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun PrivacyItemCard(item: PrivacySecurityItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = item.containerColor ?: MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(item.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(item.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EasterEggCard(
    onEasterEggTriggered: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🔮",
                fontSize = 24.sp
            )

            Text(
                text = stringResource(R.string.privacy_easter_egg_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.combinedClickable(
                    onClick = { },
                    onLongClick = onEasterEggTriggered
                )
            )
        }
    }
}

@Composable
fun PrivacyEasterEggDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(text = "🎉", fontSize = 32.sp)
        },
        title = {
            Text(
                text = stringResource(R.string.privacy_easter_egg_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.privacy_easter_egg_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.privacy_easter_egg_signature),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.privacy_easter_egg_close))
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// 绘制隐私页面专用的艺术背景
private fun drawPrivacyArtisticBackground(
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
                    primaryColor.copy(alpha = 0.015f),
                    secondaryColor.copy(alpha = 0.01f)
                )
            )
        )

        // 安全主题装饰形状 - 类似盾牌和锁的抽象形状
        val shieldRadius = size.width * 0.15f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.08f),
                    primaryColor.copy(alpha = 0.03f),
                    Color.Transparent
                ),
                radius = shieldRadius
            ),
            radius = shieldRadius,
            center = Offset(size.width * 0.85f, size.height * 0.15f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.06f),
                    secondaryColor.copy(alpha = 0.02f),
                    Color.Transparent
                ),
                radius = shieldRadius * 0.8f
            ),
            radius = shieldRadius * 0.8f,
            center = Offset(size.width * 0.15f, size.height * 0.7f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    tertiaryColor.copy(alpha = 0.04f),
                    tertiaryColor.copy(alpha = 0.01f),
                    Color.Transparent
                ),
                radius = shieldRadius * 0.6f
            ),
            radius = shieldRadius * 0.6f,
            center = Offset(size.width * 0.9f, size.height * 0.85f)
        )

        // 添加微妙的几何形状暗示安全感
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.02f),
                    Color.Transparent
                ),
                radius = shieldRadius * 1.2f
            ),
            radius = shieldRadius * 1.2f,
            center = Offset(size.width * 0.5f, size.height * 0.4f)
        )
    }
}