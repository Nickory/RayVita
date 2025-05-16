package com.codelab.basiclayouts.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelab.basiclayouts.ui.viewmodel.home.HealthTip

@Composable
fun HealthTipsCard(healthTips: List<HealthTip>) {
    if (healthTips.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (healthTips.firstOrNull()?.priority) {
                "high" -> Color(0xFFFFF4E6) // 轻橙色背景表示重要提醒
                "normal" -> Color.White
                else -> Color(0xFFF8F9FA)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (healthTips.firstOrNull()?.priority) {
                        "high" -> "重要提醒"
                        "normal" -> "健康建议"
                        else -> "健康建议"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = when (healthTips.firstOrNull()?.priority) {
                        "high" -> Color(0xFFFF9500)
                        else -> Color.Black
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 显示第一条建议
            healthTips.firstOrNull()?.let { tip ->
                Text(
                    text = tip.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // 如果有多条建议，显示查看更多
            if (healthTips.size > 1) {
                Text(
                    text = "查看更多建议 (${healthTips.size})",
                    color = Color(0xFF007AFF),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable { /* Open detailed view */ }
                )
            } else {
                Text(
                    text = "了解更多健康知识",
                    color = Color(0xFF007AFF),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable { /* Open detailed view */ }
                )
            }
        }
    }
}