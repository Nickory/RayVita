package com.codelab.basiclayouts.ui.screen.themeChange


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.data.theme.model.ColorSchemeData
import com.codelab.basiclayouts.data.theme.model.ThemeProfile

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeColorExplanation(
    theme: ThemeProfile,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val colors = if (isDarkMode) theme.darkColors else theme.lightColors

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "${theme.name} 配色解析",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!isExpanded) {
                            Text(
                                text = "点击查看详细配色含义",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收起" else "展开",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 展开内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    // 主题描述
                    ThemeDescriptionSection(theme = theme)

                    Spacer(modifier = Modifier.height(20.dp))

                    // 主要颜色解析
                    PrimaryColorsSection(colors = colors)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 辅助颜色解析
                    SupportingColorsSection(colors = colors)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 表面颜色解析
                    SurfaceColorsSection(colors = colors)

                    if (!theme.isBuiltIn) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // AI 生成说明
                        AiGenerationNote()
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeDescriptionSection(theme: ThemeProfile) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "主题理念",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = theme.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrimaryColorsSection(colors: ColorSchemeData) {
    ColorSection(
        title = "主要配色",
        description = "应用的核心色彩，用于重要按钮、导航等关键元素"
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorItem(
                color = colors.primary,
                name = "主色调",
                description = "品牌色彩，营造整体氛围"
            )
            ColorItem(
                color = colors.secondary,
                name = "辅助色",
                description = "平衡主色，增加层次感"
            )
            ColorItem(
                color = colors.tertiary,
                name = "强调色",
                description = "突出重点，引导注意力"
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupportingColorsSection(colors: ColorSchemeData) {
    ColorSection(
        title = "功能配色",
        description = "用于状态提示和用户反馈的颜色"
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorItem(
                color = colors.error,
                name = "错误色",
                description = "警告和错误提示"
            )
            ColorItem(
                color = colors.outline,
                name = "边框色",
                description = "分割线和边框"
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SurfaceColorsSection(colors: ColorSchemeData) {
    ColorSection(
        title = "背景配色",
        description = "页面背景和卡片表面的颜色搭配"
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorItem(
                color = colors.background,
                name = "背景色",
                description = "主要背景，营造空间感"
            )
            ColorItem(
                color = colors.surface,
                name = "表面色",
                description = "卡片和组件背景"
            )
            ColorItem(
                color = colors.surfaceVariant,
                name = "变体色",
                description = "次级表面，增加层次"
            )
        }
    }
}

@Composable
private fun ColorSection(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        content()
    }
}

@Composable
private fun ColorItem(
    color: String,
    name: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(color)))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    CircleShape
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun AiGenerationNote() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "✨ AI 智能配色",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "这套配色方案由 DeepSeek AI 根据您的描述智能生成，确保色彩和谐且符合无障碍标准。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Start
            )
        }
    }
}