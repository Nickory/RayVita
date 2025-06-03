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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.R
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
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
                            text = stringResource(R.string.theme_explan_color_analysis_title, theme.name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!isExpanded) {
                            Text(
                                text = stringResource(R.string.theme_explan_click_to_view),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) {
                            stringResource(R.string.theme_explan_collapse)
                        } else {
                            stringResource(R.string.theme_explan_expand)
                        },
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
                    text = stringResource(R.string.theme_explan_theme_concept),
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
        title = stringResource(R.string.theme_explan_primary_colors),
        description = stringResource(R.string.theme_explan_primary_colors_desc)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorItem(
                color = colors.primary,
                name = stringResource(R.string.theme_explan_primary_color),
                description = stringResource(R.string.theme_explan_primary_color_desc)
            )
            ColorItem(
                color = colors.secondary,
                name = stringResource(R.string.theme_explan_secondary_color),
                description = stringResource(R.string.theme_explan_secondary_color_desc)
            )
            ColorItem(
                color = colors.tertiary,
                name = stringResource(R.string.theme_explan_tertiary_color),
                description = stringResource(R.string.theme_explan_tertiary_color_desc)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupportingColorsSection(colors: ColorSchemeData) {
    ColorSection(
        title = stringResource(R.string.theme_explan_functional_colors),
        description = stringResource(R.string.theme_explan_functional_colors_desc)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorItem(
                color = colors.error,
                name = stringResource(R.string.theme_explan_error_color),
                description = stringResource(R.string.theme_explan_error_color_desc)
            )
            ColorItem(
                color = colors.outline,
                name = stringResource(R.string.theme_explan_outline_color),
                description = stringResource(R.string.theme_explan_outline_color_desc)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SurfaceColorsSection(colors: ColorSchemeData) {
    ColorSection(
        title = stringResource(R.string.theme_explan_surface_colors),
        description = stringResource(R.string.theme_explan_surface_colors_desc)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorItem(
                color = colors.background,
                name = stringResource(R.string.theme_explan_background_color),
                description = stringResource(R.string.theme_explan_background_color_desc)
            )
            ColorItem(
                color = colors.surface,
                name = stringResource(R.string.theme_explan_surface_color),
                description = stringResource(R.string.theme_explan_surface_color_desc)
            )
            ColorItem(
                color = colors.surfaceVariant,
                name = stringResource(R.string.theme_explan_surface_variant_color),
                description = stringResource(R.string.theme_explan_surface_variant_color_desc)
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
                text = stringResource(R.string.theme_explan_ai_generation_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.theme_explan_ai_generation_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Start
            )
        }
    }
}