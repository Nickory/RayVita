package com.codelab.basiclayouts.ui.screen.wrapper


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelab.basiclayouts.ui.screen.home.ModernRayVitaApp
import com.codelab.basiclayouts.ui.viewmodel.home.HomeViewModel
import com.codelab.basiclayouts.ui.viewmodel.home.HomeViewModelFactory

@Composable
fun HomeScreenWrapper() {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(context))

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ModernRayVitaApp(viewModel = viewModel)
    }
}