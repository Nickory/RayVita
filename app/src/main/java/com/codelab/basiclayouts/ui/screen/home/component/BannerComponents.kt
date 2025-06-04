package com.codelab.basiclayouts.ui.screen.home.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.ui.viewmodel.home.BannerItem
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedBannerCarousel(
    banners: List<BannerItem>,
    onBannerClick: (BannerItem) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // 如果没有banner数据，创建默认的3个banner
    val displayBanners = if (banners.isEmpty()) {
        listOf(
            BannerItem(
                title = "健康监测",
                subtitle = "专业健康数据分析",
                actionType = "feature",
                imageUrl = "banner_health.png", // 本地文件名
                id = "default_1",
                actionData = "",
                isActive = true
            ),
            BannerItem(
                title = "健康之旅",
                subtitle = "跟踪您的日常健康活动",
                actionType = "activity",
                imageUrl = "banner_wellness.png",
                id = "default_2",
                actionData = "",
                isActive = true
            ),
            BannerItem(
                title = "AI推荐",
                subtitle = "个性化健康洞察",
                actionType = "recommendation",
                imageUrl = "banner_ai.png",
                id = "default_3",
                actionData = "",
                isActive = true
            )
        )
    } else banners

    val pagerState = rememberPagerState { displayBanners.size }

    // 自动轮播
    LaunchedEffect(pagerState) {
        while (true) {
            delay(5000)
            val nextPage = (pagerState.currentPage + 1) % displayBanners.size
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(1000, easing = FastOutSlowInEasing)
            )
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 16.dp
        ) { page ->
            EnhancedBannerCard(
                banner = displayBanners[page],
                onClick = { onBannerClick(displayBanners[page]) }
            )
        }

        // 现代指示器
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(displayBanners.size) { index ->
                val isSelected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(
                            width = if (isSelected) 24.dp else 8.dp,
                            height = 8.dp
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isSelected) colorScheme.primary
                            else colorScheme.outline.copy(alpha = 0.3f)
                        )
                        .animateContentSize()
                )
            }
        }
    }
}

@Composable
fun EnhancedBannerCard(
    banner: BannerItem,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (banner.actionType) {
                "feature" -> colorScheme.tertiary
                "activity" -> colorScheme.secondary
                "recommendation" -> colorScheme.primary
                "web" -> colorScheme.primary

                else -> colorScheme.primaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 背景渐变
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            radius = 400f
                        )
                    )
            )

            // PNG图片
            AsyncImage(
                model = when (banner.imageUrl) {
                    "banner_health.png" -> R.drawable.ab1_inversions
                    "banner_wellness.png" -> R.drawable.ab3_stretching
                    "banner_ai.png" -> R.drawable.fc3_stress_and_anxiety
                    "theme.jpg"->R.drawable.theme
                    "banner_synapse.png"->R.drawable.web
                    else -> banner.imageUrl // 支持远程 URL
                },
                contentDescription = banner.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f,
                placeholder = painterResource(R.drawable.ab1_inversions), // 占位图
                error = painterResource(R.drawable.ab5_hiit) // 错误图
            )

            // 内容层
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 装饰图标
                Icon(
                    imageVector = when (banner.actionType) {
                        "feature" -> Icons.Default.AutoAwesome
                        "activity" -> Icons.Default.EmojiEvents
                        "recommendation" -> Icons.Default.Favorite
                        "web"-> Icons.Default.Web
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.White.copy(alpha = 0.9f)
                )

                // 文本内容
                Column {
                    Text(
                        text = banner.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = banner.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}