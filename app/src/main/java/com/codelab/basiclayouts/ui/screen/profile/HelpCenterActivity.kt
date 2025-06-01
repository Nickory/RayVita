package com.codelab.basiclayouts.ui.screen.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Support
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.data.language.model.LanguagePreferences
import com.codelab.basiclayouts.data.language.model.LanguageRepository
import com.codelab.basiclayouts.data.theme.model.ThemePreferences
import com.codelab.basiclayouts.data.theme.model.ThemeRepository
import com.codelab.basiclayouts.ui.theme.DynamicRayVitaTheme
import com.codelab.basiclayouts.viewModel.theme.DarkModeOption
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModel
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModelFactory
import kotlinx.coroutines.delay

class HelpCenterActivity : ComponentActivity() {

    private lateinit var languagePreferences: LanguagePreferences
    private lateinit var themeRepository: ThemeRepository
    private lateinit var themePreferences: ThemePreferences

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(themeRepository, this@HelpCenterActivity)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, HelpCenterActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            // 在Activity启动时应用语言设置
            val languagePrefs = LanguagePreferences(newBase)
            val currentLanguage = languagePrefs.getCurrentLanguage()
            LanguageRepository.updateAppLanguage(newBase, currentLanguage)
            super.attachBaseContext(newBase)
        } else {
            super.attachBaseContext(newBase)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化语言相关组件
        languagePreferences = LanguagePreferences(this)

        // 初始化主题相关组件
        themePreferences = ThemePreferences(this)
        themeRepository = ThemeRepository(this, themePreferences)

        // 应用当前语言设置
        val currentLanguage = languagePreferences.getCurrentLanguage()
        LanguageRepository.updateAppLanguage(this, currentLanguage)

        setContent {
            DynamicThemeWrapper(
                themeRepository = themeRepository,
                themeViewModel = themeViewModel
            ) {
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 艺术背景
                    ArtisticBackground()

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
                                            Icons.AutoMirrored.Filled.Help,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.help_center),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.back),
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
                        HelpCenterContent(
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ArtisticBackground() {
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        val surfaceColor = MaterialTheme.colorScheme.surface

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawArtisticBackground(
                drawScope = this,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                tertiaryColor = tertiaryColor,
                surfaceColor = surfaceColor
            )
        }
    }

    @Composable
    private fun DynamicThemeWrapper(
        themeRepository: ThemeRepository,
        themeViewModel: ThemeViewModel,
        content: @Composable () -> Unit
    ) {
        val currentThemeFlow = themeRepository.getCurrentTheme()
        val currentTheme by currentThemeFlow.collectAsState(initial = null)
        val uiState by themeViewModel.uiState.collectAsState()

        // 根据用户的深色模式选择确定是否使用深色主题
        val isSystemInDarkTheme = isSystemInDarkTheme()
        val shouldUseDarkTheme = when (uiState.darkModeOption) {
            DarkModeOption.FOLLOW_SYSTEM -> isSystemInDarkTheme
            DarkModeOption.LIGHT -> false
            DarkModeOption.DARK -> true
        }

        currentTheme?.let { theme ->
            DynamicRayVitaTheme(
                themeProfile = theme,
                darkTheme = shouldUseDarkTheme,
                content = content
            )
        } ?: run {
            // 如果主题还在加载中，使用默认主题
            content()
        }
    }

    @Composable
    private fun HelpCenterContent(
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 欢迎卡片
            WelcomeCard()

            Spacer(modifier = Modifier.height(24.dp))

            // FAQ 列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val faqCategories = getFaqCategories()

                itemsIndexed(faqCategories) { categoryIndex, category ->
                    var isVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        delay(categoryIndex * 150L) // 错开动画时间
                        isVisible = true
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(dampingRatio = 0.8f)
                        ) + fadeIn(),
                        exit = fadeOut()
                    ) {
                        FaqCategoryCard(category = category)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    @Composable
    private fun WelcomeCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.98f)
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box {
                // 装饰性背景图案
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    drawDecorativePattern(this)
                }

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Support,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.help_welcome),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = stringResource(R.string.help_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun FaqCategoryCard(category: FaqCategory) {
        var isExpanded by remember { mutableStateOf(false) }

        val animatedElevation by animateFloatAsState(
            targetValue = if (isExpanded) 12f else 6f,
            animationSpec = spring(dampingRatio = 0.8f)
        )

        val containerColor by animateColorAsState(
            targetValue = if (isExpanded) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.95f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            },
            animationSpec = tween(300)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { isExpanded = !isExpanded }
                .animateContentSize(
                    animationSpec = spring(dampingRatio = 0.8f)
                ),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 类别标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = category.icon,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Column {
                            Text(
                                text = stringResource(category.titleRes),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.faq_count, category.faqs.size),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val rotation by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        animationSpec = tween(300)
                    )

                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded)
                            stringResource(R.string.collapse) else stringResource(R.string.expand),
                        modifier = Modifier.rotate(rotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // FAQ 项目
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(300)) + slideInVertically(),
                    exit = fadeOut(tween(300))
                ) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        category.faqs.forEachIndexed { index, faq ->
                            FaqItem(
                                faq = faq,
                                index = index + 1
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun FaqItem(
        faq: Faq,
        index: Int
    ) {
        var isExpanded by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { isExpanded = !isExpanded }
                .animateContentSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = index.toString(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = stringResource(faq.questionRes),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val rotation by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        animationSpec = tween(200)
                    )

                    Icon(
                        imageVector = Icons.Default.ExpandLess,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200))
                ) {
                    Text(
                        text = stringResource(faq.answerRes),
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Justify
                    )
                }
            }
        }
    }

    // 获取FAQ分类数据
    private fun getFaqCategories(): List<FaqCategory> {
        return listOf(
            FaqCategory(
                icon = "🩺",
                titleRes = R.string.faq_category_rppg,
                faqs = listOf(
                    Faq(R.string.faq_rppg_what_is, R.string.faq_rppg_what_is_answer),
                    Faq(R.string.faq_rppg_contact, R.string.faq_rppg_contact_answer),
                    Faq(R.string.faq_rppg_equipment, R.string.faq_rppg_equipment_answer),
                    Faq(R.string.faq_rppg_metrics, R.string.faq_rppg_metrics_answer),
                    Faq(R.string.faq_rppg_future, R.string.faq_rppg_future_answer),
                    Faq(R.string.faq_rppg_duration, R.string.faq_rppg_duration_answer),
                    Faq(R.string.faq_rppg_tips, R.string.faq_rppg_tips_answer),
                    Faq(R.string.faq_rppg_failure, R.string.faq_rppg_failure_answer),
                    Faq(R.string.faq_rppg_multitask, R.string.faq_rppg_multitask_answer),
                    Faq(R.string.faq_rppg_accuracy, R.string.faq_rppg_accuracy_answer)
                )
            ),
            FaqCategory(
                icon = "📊",
                titleRes = R.string.faq_category_data,
                faqs = listOf(
                    Faq(R.string.faq_data_auto_save, R.string.faq_data_auto_save_answer),
                    Faq(R.string.faq_data_history, R.string.faq_data_history_answer),
                    Faq(R.string.faq_data_clear, R.string.faq_data_clear_answer),
                    Faq(R.string.faq_data_export, R.string.faq_data_export_answer),
                    Faq(R.string.faq_data_trends, R.string.faq_data_trends_answer)
                )
            ),
            FaqCategory(
                icon = "☁️",
                titleRes = R.string.faq_category_cloud,
                faqs = listOf(
                    Faq(R.string.faq_cloud_enable, R.string.faq_cloud_enable_answer),
                    Faq(R.string.faq_cloud_realtime, R.string.faq_cloud_realtime_answer),
                    Faq(R.string.faq_cloud_device_change, R.string.faq_cloud_device_change_answer),
                    Faq(R.string.faq_cloud_sync_fail, R.string.faq_cloud_sync_fail_answer),
                    Faq(R.string.faq_cloud_security, R.string.faq_cloud_security_answer)
                )
            ),
            FaqCategory(
                icon = "🤖",
                titleRes = R.string.faq_category_ai,
                faqs = listOf(
                    Faq(R.string.faq_ai_theme, R.string.faq_ai_theme_answer),
                    Faq(R.string.faq_ai_suggestions, R.string.faq_ai_suggestions_answer),
                    Faq(R.string.faq_ai_network, R.string.faq_ai_network_answer),
                    Faq(R.string.faq_ai_edit, R.string.faq_ai_edit_answer),
                    Faq(R.string.faq_ai_theme_basis, R.string.faq_ai_theme_basis_answer)
                )
            ),
            FaqCategory(
                icon = "🌐",
                titleRes = R.string.faq_category_web,
                faqs = listOf(
                    Faq(R.string.faq_web_synapse, R.string.faq_web_synapse_answer),
                    Faq(R.string.faq_web_features, R.string.faq_web_features_answer),
                    Faq(R.string.faq_web_usage, R.string.faq_web_usage_answer),
                    Faq(R.string.faq_web_share, R.string.faq_web_share_answer),
                    Faq(R.string.faq_web_security, R.string.faq_web_security_answer)
                )
            )
        )
    }
}

// 数据类
data class FaqCategory(
    val icon: String,
    val titleRes: Int,
    val faqs: List<Faq>
)

data class Faq(
    val questionRes: Int,
    val answerRes: Int
)

// 绘制艺术背景
private fun drawArtisticBackground(
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
                    primaryColor.copy(alpha = 0.05f),
                    secondaryColor.copy(alpha = 0.03f)
                )
            )
        )

        // 装饰性圆形
        val circleRadius = size.width * 0.3f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.1f),
                    primaryColor.copy(alpha = 0.02f),
                    Color.Transparent
                ),
                radius = circleRadius
            ),
            radius = circleRadius,
            center = Offset(size.width * 0.8f, size.height * 0.2f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.08f),
                    secondaryColor.copy(alpha = 0.02f),
                    Color.Transparent
                ),
                radius = circleRadius * 0.7f
            ),
            radius = circleRadius * 0.7f,
            center = Offset(size.width * 0.2f, size.height * 0.7f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    tertiaryColor.copy(alpha = 0.06f),
                    tertiaryColor.copy(alpha = 0.01f),
                    Color.Transparent
                ),
                radius = circleRadius * 0.5f
            ),
            radius = circleRadius * 0.5f,
            center = Offset(size.width * 0.9f, size.height * 0.8f)
        )
    }
}

// 绘制装饰图案
private fun drawDecorativePattern(drawScope: DrawScope) {
    with(drawScope) {
        val patternSize = 20.dp.toPx()
        val alpha = 0.1f

        for (x in 0 until (size.width / patternSize).toInt()) {
            for (y in 0 until (size.height / patternSize).toInt()) {
                if ((x + y) % 3 == 0) {
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = 2.dp.toPx(),
                        center = Offset(
                            x * patternSize + patternSize / 2,
                            y * patternSize + patternSize / 2
                        )
                    )
                }
            }
        }
    }
}