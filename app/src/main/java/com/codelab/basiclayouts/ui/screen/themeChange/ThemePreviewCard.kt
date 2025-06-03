package com.codelab.basiclayouts.ui.screen.themeChange

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.data.theme.model.ColorSchemeData
import com.codelab.basiclayouts.data.theme.model.ThemeProfile

@Composable
fun ThemePreviewCard(
    theme: ThemeProfile,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = if (isDarkMode) theme.darkColors else theme.lightColors
    var showColorDetails by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = theme.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.theme_explan_selected),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (!theme.isBuiltIn) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = stringResource(R.string.theme_explan_ai_generated),
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row {
                    // 配色说明按钮
                    IconButton(
                        onClick = { showColorDetails = !showColorDetails },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (showColorDetails) Icons.Default.ExpandLess else Icons.Default.Info,
                            contentDescription = if (showColorDetails) {
                                stringResource(R.string.theme_explan_hide_color_details)
                            } else {
                                stringResource(R.string.theme_explan_show_color_details)
                            },
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 删除按钮（仅对自定义主题显示）
                    if (!theme.isBuiltIn && onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.theme_explan_delete_theme),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 主题描述
            Text(
                text = theme.description.split("\n\n")[0], // 只显示第一段描述
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 颜色预览
            ColorPreview(colors = colors)

            // 配色详情（可展开）
            AnimatedVisibility(
                visible = showColorDetails,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Spacer(modifier = Modifier.height(12.dp))

                    // 快速配色解释
                    QuickColorExplanation(theme = theme, colors = colors)
                }
            }
        }
    }
}

@Composable
private fun QuickColorExplanation(
    theme: ThemeProfile,
    colors: ColorSchemeData
) {
    Column {
        Text(
            text = stringResource(R.string.theme_explan_color_meaning),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 显示设计理念（如果有的话）
        val designConcept = theme.description.split("\n\n").getOrNull(1)?.removePrefix("设计理念：")
        if (!designConcept.isNullOrBlank()) {
            Text(
                text = designConcept,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.3
            )
        } else {
            // 如果没有设计理念，显示通用的配色说明
            val colorMeaning = when {
                theme.name.contains("森林") || theme.name.contains("绿") || theme.name.contains("Forest") || theme.name.contains("Green") ->
                    stringResource(R.string.theme_explan_forest_meaning)
                theme.name.contains("海洋") || theme.name.contains("蓝") || theme.name.contains("Ocean") || theme.name.contains("Blue") ->
                    stringResource(R.string.theme_explan_ocean_meaning)
                theme.name.contains("紫") || theme.name.contains("梦") || theme.name.contains("Purple") || theme.name.contains("Violet") || theme.name.contains("Dream") ->
                    stringResource(R.string.theme_explan_purple_meaning)
                theme.name.contains("橙") || theme.name.contains("日落") || theme.name.contains("Orange") || theme.name.contains("Sunset") ->
                    stringResource(R.string.theme_explan_orange_meaning)
                theme.name.contains("iOS") ->
                    stringResource(R.string.theme_explan_ios_meaning)
                else ->
                    stringResource(R.string.theme_explan_default_meaning)
            }

            Text(
                text = colorMeaning,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.3
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 快速色彩组合展示
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorDot(colors.primary, stringResource(R.string.theme_explan_primary_short))
            ColorDot(colors.secondary, stringResource(R.string.theme_explan_secondary_short))
            ColorDot(colors.tertiary, stringResource(R.string.theme_explan_tertiary_short))

            Spacer(modifier = Modifier.weight(1f))

            if (!theme.isBuiltIn) {
                Text(
                    text = stringResource(R.string.theme_explan_ai_generated_short),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ColorDot(
    colorHex: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(colorHex)))
                .border(
                    0.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    CircleShape
                )
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ColorPreview(colors: ColorSchemeData) {
    Column {
        // 主要颜色行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorCircle(
                color = Color(android.graphics.Color.parseColor(colors.primary)),
                label = stringResource(R.string.theme_explan_primary_label),
                modifier = Modifier.weight(1f)
            )
            ColorCircle(
                color = Color(android.graphics.Color.parseColor(colors.secondary)),
                label = stringResource(R.string.theme_explan_secondary_label),
                modifier = Modifier.weight(1f)
            )
            ColorCircle(
                color = Color(android.graphics.Color.parseColor(colors.tertiary)),
                label = stringResource(R.string.theme_explan_tertiary_label),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 表面颜色预览
        SurfacePreview(colors = colors)
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun SurfacePreview(colors: ColorSchemeData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
    ) {
        // Background
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(android.graphics.Color.parseColor(colors.background)))
        ) {
            Text(
                text = stringResource(R.string.theme_explan_background_label),
                style = MaterialTheme.typography.labelSmall,
                color = Color(android.graphics.Color.parseColor(colors.onBackground)),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Surface
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(android.graphics.Color.parseColor(colors.surface)))
        ) {
            Text(
                text = stringResource(R.string.theme_explan_surface_label),
                style = MaterialTheme.typography.labelSmall,
                color = Color(android.graphics.Color.parseColor(colors.onSurface)),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Surface Variant
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(android.graphics.Color.parseColor(colors.surfaceVariant)))
        ) {
            Text(
                text = stringResource(R.string.theme_explan_surface_variant_label),
                style = MaterialTheme.typography.labelSmall,
                color = Color(android.graphics.Color.parseColor(colors.onSurfaceVariant)),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}