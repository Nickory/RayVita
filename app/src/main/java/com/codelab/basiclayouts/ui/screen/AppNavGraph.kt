package com.codelab.basiclayouts.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.codelab.basiclayouts.ui.screen.wrapper.HomeScreenWrapper
import com.codelab.basiclayouts.ui.screen.wrapper.InsightScreenWrapper
import com.codelab.basiclayouts.ui.screen.wrapper.ProfileScreenWrapper
import com.codelab.basiclayouts.ui.screen.wrapper.SocialScreenWrapper

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        composable("home") {
            HomeScreenWrapper()
        }

        composable("insight") {
            InsightScreenWrapper()
        }

        composable("social") {
            SocialScreenWrapper()
        }

        composable("profile") {
            ProfileScreenWrapper()
        }
    }
}