package com.codelab.basiclayouts.ui.insight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.codelab.basiclayouts.ui.theme.MySootheTheme

class InsightActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySootheTheme {
                // Manage navigation state
                var selectedTab by remember { mutableStateOf(1) }
                InsightScreen(
                    selectedTab = selectedTab,
                    onTabSelect = { selectedTab = it }
                )
            }
        }
    }
}