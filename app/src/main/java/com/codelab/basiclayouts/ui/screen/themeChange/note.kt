//ui/
//theme/
//ThemeSelectorActivity.kt      // 主题切换入口 Activity（带返回键）
//ThemeSelectorScreen.kt        // Compose 页面 UI
//components/
//ThemePreviewCard.kt           // 可复用的主题预览组件
//
//viewmodel/
//theme/
//ThemeViewModel.kt             // 管理主题状态，发送 DeepSeek 请求
//
//data/
//model/
//ThemeProfile.kt               // 本地主题配置结构
//DeepSeekColorResponse.kt      // DeepSeek 响应数据结构
//
//repository/
//ThemeRepository.kt            // 本地缓存 + 网络获取
//
//local/
//ThemePreferences.kt           // 使用 DataStore 保存当前选择主题
//
//theme/
//ColorSchemes.kt                 // 所有预设配色方案
//ThemeManager.kt                 // 用于切换、生成、持久化主题
//Theme.kt                        // 更新的 RayVitaTheme，支持动态配色
