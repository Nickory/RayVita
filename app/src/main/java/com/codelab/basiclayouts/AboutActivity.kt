package com.codelab.basiclayouts


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelab.basiclayouts.ui.theme.MySootheTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySootheTheme {
                AboutScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("关于 RayVita") },
                navigationIcon = {
                    IconButton(onClick = { (context as Activity).finish() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // 核心功能模块
            ExpandableCard(
                title = "核心技术",
                icon = Icons.Default.Science,
                initialExpanded = true
            ) {
                TechStackList()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "通过融合rPPG光学传感与深度学习算法，实现非接触式生命体征监测。技术指标：",
                    style = MaterialTheme.typography.bodyMedium
                )
                FeatureItem("心率监测精度", "±2 BPM")
                FeatureItem("血氧监测范围", "70%-100%")
                FeatureItem("情绪识别准确率", "92.4%")
            }

            // 功能亮点
            ExpandableCard(
                title = "功能亮点",
                icon = Icons.Default.Info,
                initialExpanded = false
            ) {
                Text(
                    "健康管理的全方位解决方案：",
                    style = MaterialTheme.typography.bodyMedium
                )
                FeatureItem("实时监测", "心率/血氧/压力指数")
                FeatureItem("智能分析", "健康趋势预测与风险预警")
                FeatureItem("个性计划", "运动/饮食/睡眠优化方案")
                FeatureItem("数据安全", "端到端加密与本地存储")
            }

            // 技术架构
            ExpandableCard(
                title = "技术架构",
                icon = Icons.Default.Code,
                initialExpanded = false
            ) {
                Text(
                    "基于rPPG技术与模块化设计的现代技术堆栈：",
                    style = MaterialTheme.typography.bodyMedium
                )
                FeatureItem("前端框架", "Jetpack Compose")
                FeatureItem("视觉算法", "Contrast-Phys+")
                FeatureItem("核心算法", "CNN+LSTM 混合模型")
                FeatureItem("数据处理", "TensorFlow Lite")
                FeatureItem("云服务", "Firebase 实时同步")
            }

            // 开发团队
            ExpandableCard(
                title = "开发团队",
                icon = Icons.Default.Group,
                initialExpanded = false
            ) {
                TeamMemberList()
            }

            ExpandableCard(
                title = "未来方向",
                icon = Icons.Default.Science, // 可以使用自定义的未来感图标
                initialExpanded = false
            ) {
                TechRoadmap()
            }

            // 联系信息
            ContactSection()


        }
    }
}

@Composable
private fun ExpandableCard(
    title: String,
    icon: ImageVector,
    initialExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initialExpanded) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun TechStackList() {
    val techStacks = listOf(
        "rPPG 光学传感",
        "TensorFlow Lite",
        "Jetpack Compose",
        "Firebase ML",
        "ARCore"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        techStacks.forEach { tech ->
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .animateContentSize(),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = tech,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TeamMemberList() {
    val teamMembers = listOf(
        MemberInfo("王子恒 主席", "国家主席", "资金支持，行政管理"),
        MemberInfo("夏东旭 院士", "首席科学家", "AI算法,物联网系统"),
        MemberInfo("吴迪 研究员", "移动开发工程师", "Android开发,数据库搭建"),
        MemberInfo("汪紫衡 院长", "架构师", "系统设计,云服务"),
        MemberInfo("小东西 教授", "生物医学应用", "rPPG,生物信号处理"),
        MemberInfo("吴弟 设计师", "用户体验", "UI/UX")
    )

    Column(
        modifier = Modifier.fillMaxWidth(), // 移除宽度限制
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        teamMembers.forEach { member ->
            TeamMemberCard(member)
        }
    }
}

@Composable
private fun TeamMemberCard(member: MemberInfo) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .animateContentSize()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = member.name.first().toString(),
                        color = Color.White,
                        fontSize = 24.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = member.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Text(
                    text = "研究方向：${member.expertise}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
@Composable
private fun ContactSection() {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题部分
            Text(
                text = "联系我们",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
                textAlign = TextAlign.Center // 显式设置文本居中
            )

            // 联系信息带点击效果
            val emailStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            // 技术合作邮箱
            Text(
                text = "技术合作：tech@rayvita.com",
                style = emailStyle,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:tech@rayvita.com")
                                putExtra(Intent.EXTRA_SUBJECT, "技术合作咨询")
                            }
                        )
                    }
                    .padding(vertical = 4.dp)
            )

            // 客服支持邮箱
            Text(
                text = "客服支持：support@rayvita.com",
                style = emailStyle,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@rayvita.com")
                                putExtra(Intent.EXTRA_SUBJECT, "用户支持请求")
                            }
                        )
                    }
                    .padding(vertical = 4.dp)
            )

            // 版权信息
            Text(
                text = "© 2025 RayVita",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun TechRoadmap() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // 阶段式技术路线
        TechPhase(
            phase = "2025-2026",
            techs = listOf(
                "光子计算加速：PPG信号处理速度提升100倍 ",
                "生物芯片集成：皮肤表面代谢物无创检测"
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        TechPhase(
            phase = "2027-2028",
            techs = listOf(
                "神经接口预研：脑电-心率耦合分析",
                "量子传感阵列：5cm非接触血压监测"
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 跨维度技术融合
        Text(
            text = "多维融合创新：",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        CrossTechGrid(
            items = listOf(
                "光遗传传感：实时细胞级监测 (Δλ=5nm)",
                "代谢孪生体：分子动力学模拟 (>1M原子)",
                "空间健康云：卫星直连诊疗 (＜50ms延迟)"
            )
        )
    }
}

@Composable
private fun TechPhase(phase: String, techs: List<String>) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .animateContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = phase,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    techs.forEach { tech ->
                        Text(
                            text = "• $tech",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CrossTechGrid(items: List<String>) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .animateContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "技术融合",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items.forEach { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}


// 数据类定义
data class TechDetail(
    val title: String,
    val description: String,
    val target: String
)

data class CrossTechItem(
    val title: String,
    val techDetail: String,
    val metric: String
)

data class MemberInfo(val name: String, val role: String, val expertise: String)