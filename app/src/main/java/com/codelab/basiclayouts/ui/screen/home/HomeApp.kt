// ============== ModernRayVitaApp.kt ==============
package com.codelab.basiclayouts.ui.screen.home

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelab.basiclayouts.ui.screen.home.component.EnhancedAIAssistantCard
import com.codelab.basiclayouts.ui.screen.home.component.EnhancedBannerCarousel
import com.codelab.basiclayouts.ui.screen.home.component.EnhancedHealthOverviewCard
import com.codelab.basiclayouts.ui.screen.home.component.PremiumAchievementsCard
import com.codelab.basiclayouts.ui.screen.home.component.PremiumBreathingCard
import com.codelab.basiclayouts.ui.screen.home.component.RefinedTopBar
import com.codelab.basiclayouts.ui.screen.physnet.PhysnetActivity
import com.codelab.basiclayouts.ui.viewmodel.home.HomeViewModel
import com.codelab.basiclayouts.ui.viewmodel.home.HomeViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernRayVitaApp(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.dismissError()
        }
    }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            RefinedTopBar()
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colorScheme.background,
        contentColor = colorScheme.onSurface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
        ) {
            // Enhanced Banner Carousel with PNG support
            EnhancedBannerCarousel(
                banners = uiState.banners,
                onBannerClick = { banner ->
                    // 预留跳转逻辑
                    when (banner.actionType) {
                        "feature" -> {
                            // 跳转到功能介绍页面
                        }
                        "activity" -> {
                            // 跳转到活动页面
                        }
                        "recommendation" -> {
                            // 跳转到推荐页面
                        }
                        else -> {
                            viewModel.onBannerClick(banner)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main content
            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                // Enhanced Health Overview Card
                EnhancedHealthOverviewCard(
                    healthData = uiState.healthData,
                    trendData = uiState.trendData,
                    isLoading = uiState.isLoading,
                    onRefresh = { viewModel.refreshData() }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Enhanced AI Assistant Card
                uiState.healthTip?.let { tip ->
                    EnhancedAIAssistantCard(
                        tip = tip,
                        onGetNewTip = { viewModel.requestAITip() }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Premium Breathing Training Card
                uiState.breathingSession?.let { session ->
                    PremiumBreathingCard(
                        session = session,
                        onStartRPPG = {
                            // 跳转到PhysnetActivity
                            val intent = Intent(context, PhysnetActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

//                // Enhanced Measurement History
//                if (uiState.recentScans.isNotEmpty()) {
//                    EnhancedMeasurementHistoryCard(
//                        scans = uiState.recentScans
//                    )
//                    Spacer(modifier = Modifier.height(20.dp))
//                }

                // Achievement system
                if (uiState.achievements.isNotEmpty()) {
                    PremiumAchievementsCard(
                        achievements = uiState.achievements,
                        onViewAll = { /* Navigate to achievements */ }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// Wrapper component for easy integration
@Composable
fun ModernHomeScreenWrapper() {
    val context = LocalContext.current
    val viewModel: HomeViewModel =
        viewModel(
            factory = HomeViewModelFactory(context)
        )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ModernRayVitaApp(viewModel = viewModel)
    }
}