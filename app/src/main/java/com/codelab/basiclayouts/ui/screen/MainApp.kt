package com.codelab.basiclayouts.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codelab.basiclayouts.utils.StepCounterPermissionHelper

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainApp(
    stepCounterPermissionHelper: StepCounterPermissionHelper // 只保留原有参数
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    // 定义需要显示底部导航栏的页面（保持不变）
    val bottomNavRoutes = listOf("home", "insight", "social", "profile")
    val shouldShowBottomNav = currentDestination?.route in bottomNavRoutes

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = shouldShowBottomNav,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                BottomNavBar(
                    navController = navController,
                    currentDestination = currentDestination
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppNavGraph(
                navController = navController,
                stepCounterPermissionHelper = stepCounterPermissionHelper // 只传递原有参数
            )
        }
    }
}