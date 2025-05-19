package com.codelab.basiclayouts.ui.profile

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codelab.basiclayouts.viewmodel.profile.AuthViewModel

/**
 * 个人中心屏幕控制器 - 负责处理所有的导航逻辑
 * 通过简化的方式管理各个屏幕之间的切换
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreenController(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authUiState by authViewModel.uiState.collectAsState()

    // 确定初始目的地
    val startDestination = if (authUiState.isLoggedIn) "profile" else "login"

    LaunchedEffect(Unit) {
        Log.d("ProfileController", "Initial navigation: $startDestination, loggedIn: ${authUiState.isLoggedIn}")
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 个人资料页面
        composable("profile") {
            ProfileScreen(
                navController = navController,
                authViewModel = authViewModel
            )

            // 如果用户登出，跳转到登录页面
            LaunchedEffect(authUiState.isLoggedIn) {
                if (!authUiState.isLoggedIn) {
                    Log.d("ProfileController", "User logged out, navigating to login")
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                }
            }
        }

        // 登录页面
        composable("login") {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )

            // 如果登录成功，跳转到个人资料页面
            LaunchedEffect(authUiState.isLoggedIn) {
                if (authUiState.isLoggedIn) {
                    Log.d("ProfileController", "User logged in, navigating to profile")
                    navController.navigate("profile") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }

        // 注册页面
        composable("register") {
            RegisterScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // 重置密码页面
        composable("reset_password") {
            ResetPasswordScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}