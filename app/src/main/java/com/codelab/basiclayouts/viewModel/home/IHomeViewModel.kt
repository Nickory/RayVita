package com.codelab.basiclayouts.ui.viewmodel.home

import kotlinx.coroutines.flow.StateFlow

interface IHomeViewModel {
    // UI状态
    val uiState: StateFlow<HomeUIState>

    // 数据加载
    fun loadInitialData()
    fun refreshData()

    // 扫描操作
    fun startScan()



    // 错误处理
    fun dismissError()

    // 特定数据获取
    fun loadHealthData()
    fun loadHealthTips()
    fun loadRecentScans()
    fun loadTrendData()

    // 设备连接
    fun checkDeviceConnection()

    // 清理资源
    fun onCleared()
}