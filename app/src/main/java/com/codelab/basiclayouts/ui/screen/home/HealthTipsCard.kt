package com.codelab.basiclayouts.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codelab.basiclayouts.ui.insight.AIConversationScreen
import com.codelab.basiclayouts.ui.viewmodel.home.HealthTip
import com.codelab.basiclayouts.viewmodel.insight.AIConversationViewModel
import com.codelab.basiclayouts.viewmodel.insight.AIConversationViewModelFactory


@Composable
fun HealthTipsCard(healthTips: List<HealthTip>) {
    if (healthTips.isEmpty()) return
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val isHighPriority = healthTips.firstOrNull()?.priority == "high"
    val aiViewModel: AIConversationViewModel = viewModel(factory = AIConversationViewModelFactory(context))

    var showChatScreen by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    if (showChatScreen) {
        AIConversationScreen(viewModel = aiViewModel, onBackPressed = { showChatScreen = false })
        return
    }


    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = when (healthTips.firstOrNull()?.priority) {
                "high" -> MaterialTheme.colorScheme.errorContainer
                "normal" -> MaterialTheme.colorScheme.surface
                else -> surfaceVariant
            }
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (healthTips.firstOrNull()?.priority) {
                        "high" -> "Important Alert"
                        "normal" -> "Health Tip"
                        else -> "Health Insight"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isHighPriority) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            healthTips.firstOrNull()?.let { tip ->
                Text(
                    text = tip.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            val linkText = if (healthTips.size > 1) {
                "View More Tips (${healthTips.size})"
            } else {
                "Learn More About Health"
            }

            Text(
                text = linkText,
                style = MaterialTheme.typography.labelLarge,
                color = primaryColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    //todo
                }
            )
        }
    }
}
