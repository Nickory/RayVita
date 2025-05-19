package com.codelab.basiclayouts.viewModel

//├── viewmodel/
//│   ├── HomeViewModel.kt
//│   ├── InsightViewModel.kt
//│   ├── SocialViewModel.kt
//│   └── ProfileViewModel.kt
//每个模块一个 ViewModel，使用 StateFlow / LiveData 持有状态
//
//统一继承 BaseViewModel 提供协程管理与错误处理