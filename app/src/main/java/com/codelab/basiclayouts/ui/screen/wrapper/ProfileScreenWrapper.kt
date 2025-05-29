package com.codelab.basiclayouts.ui.screen.wrapper


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codelab.basiclayouts.ui.profile.LoginScreen
import com.codelab.basiclayouts.ui.profile.ProfileScreen
import com.codelab.basiclayouts.ui.profile.ProfileWelcomeScreen
import com.codelab.basiclayouts.ui.profile.RegisterScreen
import com.codelab.basiclayouts.ui.profile.ResetPasswordScreen
import com.codelab.basiclayouts.viewmodel.profile.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreenWrapper() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ProfileApp()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()

    // 收集登录状态
    val authUiState by authViewModel.uiState.collectAsState()

    // 监听登录状态变化
    LaunchedEffect(authUiState.isLoggedIn) {
        // 可以在这里添加日志或其他逻辑
    }

    NavHost(
        navController = navController,
        startDestination = "profile_main"
    ) {
        // 主个人中心页面
        composable("profile_main") {
            // 根据登录状态显示不同内容
            if (authUiState.isLoggedIn) {
                // 已登录，显示个人资料
                ProfileScreen(navController, authViewModel)
            } else {
                // 未登录，显示欢迎界面
                ProfileWelcomeScreen(
                    onLoginClick = { navController.navigate("login") },
                    onRegisterClick = { navController.navigate("register") },
                    onBackClick = {
                        // 这里可以添加返回逻辑，但在导航架构中通常不需要finish
                    }
                )
            }
        }

        // 登录页面
        composable("login") {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
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