package com.codelab.basiclayouts.ui.screen.language

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.data.language.model.Language
import com.codelab.basiclayouts.data.language.model.LanguagePreferences
import com.codelab.basiclayouts.data.language.model.LanguageRepository
import com.codelab.basiclayouts.data.theme.model.ThemePreferences
import com.codelab.basiclayouts.data.theme.model.ThemeRepository
import com.codelab.basiclayouts.ui.theme.DynamicRayVitaTheme
import com.codelab.basiclayouts.viewModel.language.LanguageViewModel
import com.codelab.basiclayouts.viewModel.language.LanguageViewModelFactory
import com.codelab.basiclayouts.viewModel.theme.DarkModeOption
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModel
import com.codelab.basiclayouts.viewModel.theme.ThemeViewModelFactory
import kotlinx.coroutines.delay

class LanguageSelectActivity : ComponentActivity() {

    private lateinit var languageRepository: LanguageRepository
    private lateinit var languagePreferences: LanguagePreferences
    private lateinit var themeRepository: ThemeRepository
    private lateinit var themePreferences: ThemePreferences

    private val languageViewModel: LanguageViewModel by viewModels {
        LanguageViewModelFactory(languageRepository)
    }

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(themeRepository, this@LanguageSelectActivity)
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
        languageRepository = LanguageRepository(this, languagePreferences)

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
                                            Icons.Default.Translate,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.language_settings),
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
                        LanguageContent(
                            modifier = Modifier.padding(paddingValues),
                            languageViewModel = languageViewModel
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
    private fun LanguageContent(
        modifier: Modifier = Modifier,
        languageViewModel: LanguageViewModel
    ) {
        val uiState by languageViewModel.uiState.collectAsState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 欢迎卡片
            WelcomeCard()

            Spacer(modifier = Modifier.height(24.dp))

            // 语言列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(uiState.availableLanguages) { index, language ->
                    var isVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        delay(index * 100L) // 错开动画时间
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
                        EnhancedLanguageItem(
                            language = language,
                            isSelected = language == uiState.currentLanguage,
                            isLoading = uiState.isLoading && language == uiState.currentLanguage,
                            onLanguageSelected = { selectedLanguage ->
                                languageViewModel.selectLanguage(selectedLanguage, this@LanguageSelectActivity)
                            }
                        )
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
                            Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.language_welcome),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = stringResource(R.string.language_select_description),
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
    private fun EnhancedLanguageItem(
        language: Language,
        isSelected: Boolean,
        isLoading: Boolean,
        onLanguageSelected: (Language) -> Unit
    ) {
        val animatedElevation by animateFloatAsState(
            targetValue = if (isSelected) 12f else 4f,
            animationSpec = spring(dampingRatio = 0.8f)
        )

        val animatedScale by animateFloatAsState(
            targetValue = if (isSelected) 1.02f else 1f,
            animationSpec = spring(dampingRatio = 0.8f)
        )

        val containerColor by animateColorAsState(
            targetValue = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.99f)
            },
            animationSpec = tween(300)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(animatedScale)
                .clip(RoundedCornerShape(16.dp))
                .selectable(
                    selected = isSelected,
                    onClick = { onLanguageSelected(language) },
                    role = Role.RadioButton
                ),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box {
                // 选中状态的渐变背景
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = stringResource(language.displayNameRes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )

                            if (language != Language.FOLLOW_SYSTEM) {
                                Text(
                                    text = getLanguageNativeName(language),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }

                    // 状态指示器
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isLoading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            isSelected -> {
                                // 修正 AnimatedVisibility 问题
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = true,
                                    enter = scaleIn() + fadeIn(),
                                    exit = scaleOut() + fadeOut()
                                ) {
                                    Surface(
                                        modifier = Modifier.size(24.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = stringResource(R.string.selected),
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getLanguageNativeName(language: Language): String {
        return when (language) {
            Language.ENGLISH -> "English"
            Language.CHINESE -> "简体中文"
            Language.JAPANESE -> "日本語"
            Language.GERMAN -> "Deutsch"
            Language.FRENCH -> "Français"
            Language.SPANISH -> "Español"
            Language.RUSSIAN -> "Русский"
            Language.KOREAN -> "한국어"
            else -> ""
        }
    }
}

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