package com.codelab.basiclayouts

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelab.basiclayouts.ui.theme.MySootheTheme

class HeartRateRecordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySootheTheme {
                HeartRateRecordScreen()
            }
        }
    }
}



@Composable
fun HeartRateRecordScreen() {
    val context = LocalContext.current
    val activity = (context as? ComponentActivity) // 获取宿主Activity

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // 导航栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { activity?.finish() }, // 关闭当前Activity
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    "心率数据记录",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            val sharedPreferences =
                remember { context.getSharedPreferences("heart_rate_data", Context.MODE_PRIVATE) }
            val heartRateData =
                remember { mutableStateOf(sharedPreferences.getString("data", "") ?: "") }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        "心率数据记录",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 显示历史心率数据
                    val heartRates = heartRateData.value.split(",").filter { it.isNotEmpty() }
                        .mapNotNull { it.toIntOrNull() }
                    if (heartRates.isNotEmpty()) {
                        Text(
                            "历史数据: ${heartRates.joinToString(", ")} BPM",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // 健康评估
                        val averageHeartRate = heartRates.average().toInt()
                        val assessment = when {
                            averageHeartRate < 60 -> "心率偏低，可能需要关注。"
                            averageHeartRate > 100 -> "心率偏高，请注意休息。"
                            else -> "心率正常，保持健康生活习惯。"
                        }
                        Text(
                            "平均心率: $averageHeartRate BPM",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            "健康评估: $assessment",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Text(
                            "暂无心率数据",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
