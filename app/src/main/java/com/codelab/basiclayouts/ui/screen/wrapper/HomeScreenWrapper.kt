package com.codelab.basiclayouts.ui.screen.wrapper


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelab.basiclayouts.ui.screen.home.RayVitaApp
import com.codelab.basiclayouts.ui.viewmodel.home.HomeViewModel

@Composable
fun HomeScreenWrapper() {
    val viewModel: HomeViewModel = viewModel()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF4F4F4)
    ) {
        // 调用原有的RayVitaApp，而不是移除Scaffold的版本
        RayVitaApp(viewModel = viewModel)
    }
}
