package com.codelab.basiclayouts.ui.screen.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.codelab.basiclayouts.ui.theme.RayVitaTheme
import com.codelab.basiclayouts.ui.viewmodel.home.HomeViewModel

class HomeActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RayVitaTheme {
                // Wrap in Surface to provide MaterialTheme background
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF4F4F4)
                ) {
                    RayVitaApp(viewModel = viewModel)
                }
            }
        }
    }
}