package com.codelab.basiclayouts.ui.screen.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelab.basiclayouts.ui.insight.InsightScreen
import com.codelab.basiclayouts.utils.StepCounterPermissionHelper
import com.codelab.basiclayouts.viewmodel.insight.InsightViewModelFactory

@Composable
fun InsightScreenWrapper(stepCounterPermissionHelper: StepCounterPermissionHelper) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(1) }

    // 调用 InsightScreen，传递 stepCounterPermissionHelper
    InsightScreen(
        viewModel = viewModel(factory = InsightViewModelFactory(context)),
        selectedTab = selectedTab,
        onTabSelect = { selectedTab = it },
        stepCounterPermissionHelper = stepCounterPermissionHelper
    )
}