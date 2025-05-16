package com.codelab.basiclayouts.ui.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelab.basiclayouts.ui.theme.MySootheTheme
import com.codelab.basiclayouts.ui.viewmodel.home.HomeViewModel

@Composable
fun RayVitaApp(viewModel: HomeViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 处理错误消息
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar()
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelect = { selectedTab = it },
                context = LocalContext.current
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF4F4F4)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Dashboard Header
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            // Health Statistics & Body Visualization
            DashboardSection(
                healthData = uiState.healthData,
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Action Buttons Row (更紧凑的按钮布局)
            QuickActionsRow(
                onScanClick = { viewModel.startScan() },
                isScanning = uiState.isScanning,
                onRefreshClick = { viewModel.refreshData()
                },
                        context = LocalContext.current
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Scans Card
            RecentScansCard(recentScans = uiState.recentScans)

            Spacer(modifier = Modifier.height(12.dp))

            // Health Trends Card
            HealthTrendsCard(trendData = uiState.trendData)

            Spacer(modifier = Modifier.height(12.dp))

            // Health Recommendations
            HealthTipsCard(
                healthTips = uiState.healthTips
            )

            // 底部间距以防止被导航栏遮挡
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, name = "RayVita App Preview")
@Composable
fun RayVitaAppPreview() {
    MySootheTheme {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF4F4F4)
        ) {
            // 在预览中使用模拟ViewModel
            val mockViewModel = HomeViewModel()
            RayVitaApp(viewModel = mockViewModel)
        }
    }
}