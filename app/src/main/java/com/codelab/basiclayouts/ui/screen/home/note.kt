package com.codelab.basiclayouts.ui.screen.home

//com/
//└── codelab/
//└── basiclayouts/
//├── ui/
//│   ├── screen/
//│   │   ├── home/
//│   │   │   ├── HomeActivity.kt
//│   │   │   ├── HomeScreen.kt
//│   │   │   ├── HomeTopBar.kt
//│   │   │   ├── DashboardSection.kt
//│   │   │   ├── ScanButton.kt
//│   │   │   ├── HealthTipsCard.kt
//│   │   │   └── BottomNavBar.kt

//HomeActivity.kt - 包含 Activity 类
//HomeScreen.kt - 包含主要的 RayVitaApp composable
//HomeTopBar.kt - 包含顶部栏相关的组件 (TopBar, CreativeInteractiveAppName, SearchButton)
//DashboardSection.kt - 包含仪表板相关的组件 (MainDashboard, HeartRateWaveform, BodyVisualization)
//ScanButton.kt - 包含扫描按钮组件
//HealthTipsCard.kt - 包含健康建议卡片组件
//BottomNavBar.kt - 包含底部导航栏组件

//ui/
//└── screen/
//└── home/
//├── HomeActivity.kt        # 注入 ViewModel, 负责跳转
//├── HomeScreen.kt          # 顶层 UI，注入 state 和 callback
//├── HomeTopBar.kt          # 静态 UI
//├── DashboardSection.kt    # 传入数据：HR, HRV, SpO2
//├── ScanButton.kt          # 传入 onClick 回调
//├── HealthTipsCard.kt      # 传入 tips 数据
//└── BottomNavBar.kt        # 提供 onTabSelect 回调

//数据api结构

//
//ui/
//└── home/
//├── HomeScreen.kt
//├── components/
//│   ├── BannerCarousel.kt
//│   ├── HealthOverviewCard.kt
//│   ├── BreathingTrainingCard.kt
//│   ├── AiTipCard.kt
//│   ├── AchievementCard.kt
//│   ├── QuickActionGrid.kt
//└── HomeViewModel.kt
//
//viewmodel/
//└── home/
//└── HomeUiState.kt
//
//data/
//└── model/
//├── HealthData.kt
//├── AiTip.kt
//├── Achievement.kt

