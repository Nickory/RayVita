package com.codelab.basiclayouts.ui.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.data.home.network.RetrofitInstance
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class HomeViewModel : ViewModel(), IHomeViewModel {

    private val healthAPI = RetrofitInstance.healthAPI
    private val _uiState = MutableStateFlow(HomeUIState())
    override val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

    private var currentScanId: String? = null
    private var scanStatusJob: Job? = null

    init {
        loadInitialData()
    }

    override fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val healthDataJob = launch { loadHealthData() }
                val tipsJob = launch { loadHealthTips() }
                val scansJob = launch { loadRecentScans() }
                val trendsJob = launch { loadTrendData() }
                val deviceJob = launch { checkDeviceConnection() }

                healthDataJob.join()
                tipsJob.join()
                scansJob.join()
                trendsJob.join()
                deviceJob.join()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastRefreshTime = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                handleError("初始化数据失败", e)
                loadMockData()
            }
        }
    }

    override fun refreshData() {
        loadInitialData()
    }

    override fun loadHealthData() {
        viewModelScope.launch {
            try {
                // 注释掉 API 调用，模拟 API 未接入
                /*
                val response = healthAPI.getCurrentHealthData()
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { healthData ->
                        _uiState.value = _uiState.value.copy(
                            healthData = healthData,
                            isConnected = healthData.isConnected
                        )
                    } ?: handleApiError(response)
                } else {
                    handleApiError(response)
                }
                */
                // API 未接入，使用默认数据
                _uiState.value = _uiState.value.copy(healthData = createMockHealthData())
            } catch (e: Exception) {
                handleError("获取健康数据失败", e)
                _uiState.value = _uiState.value.copy(healthData = createMockHealthData())
            }
        }
    }

    override fun loadHealthTips() {
        viewModelScope.launch {
            try {
                // 注释掉 API 调用
                /*
                val response = healthAPI.getHealthTips(limit = 5)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { tips ->
                        _uiState.value = _uiState.value.copy(healthTips = tips)
                    } ?: handleApiError(response)
                } else {
                    handleApiError(response)
                }
                */
                // API 未接入，使用默认数据
                _uiState.value = _uiState.value.copy(healthTips = createMockHealthTips())
            } catch (e: Exception) {
                handleError("获取健康建议失败", e)
                _uiState.value = _uiState.value.copy(healthTips = createMockHealthTips())
            }
        }
    }

    override fun loadRecentScans() {
        viewModelScope.launch {
            try {
                // 注释掉 API 调用
                /*
                val response = healthAPI.getRecentScans(limit = 5)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { scans ->
                        _uiState.value = _uiState.value.copy(recentScans = scans)
                    } ?: handleApiError(response)
                } else {
                    handleApiError(response)
                }
                */
                // API 未接入，使用默认数据
                _uiState.value = _uiState.value.copy(recentScans = createMockRecentScans())
            } catch (e: Exception) {
                handleError("获取扫描记录失败", e)
                _uiState.value = _uiState.value.copy(recentScans = createMockRecentScans())
            }
        }
    }

    override fun loadTrendData() {
        viewModelScope.launch {
            try {
                // 注释掉 API 调用
                /*
                val response = healthAPI.getHealthTrends(period = "week")
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { trends ->
                        _uiState.value = _uiState.value.copy(trendData = trends)
                    } ?: handleApiError(response)
                } else {
                    handleApiError(response)
                }
                */
                // API 未接入，使用默认数据
                _uiState.value = _uiState.value.copy(trendData = createMockTrendData())
            } catch (e: Exception) {
                handleError("获取趋势数据失败", e)
                _uiState.value = _uiState.value.copy(trendData = createMockTrendData())
            }
        }
    }

    override fun startScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, error = null)
            try {
                // 注释掉 API 调用
                /*
                val scanRequest = ScanRequest(duration = 30, scanType = "full")
                val response = healthAPI.startScan(scanRequest)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { scanResponse ->
                        currentScanId = scanResponse.scanId
                        startScanStatusMonitoring()
                    } ?: handleApiError(response)
                } else {
                    handleApiError(response)
                    _uiState.value = _uiState.value.copy(isScanning = false)
                }
                */
                // 模拟扫描
                currentScanId = "mock_scan_${System.currentTimeMillis()}"
                startMockScan()
            } catch (e: Exception) {
                handleError("开始扫描失败", e)
                _uiState.value = _uiState.value.copy(isScanning = false)
            }
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            currentScanId?.let { scanId ->
                try {
                    // 注释掉 API 调用
                    /*
                    val response = healthAPI.stopScan(scanId)
                    if (response.isSuccessful && response.body()?.success == true) {
                        _uiState.value = _uiState.value.copy(isScanning = false)
                        currentScanId = null
                        scanStatusJob?.cancel()
                        refreshData()
                    }
                    */
                    // 模拟停止扫描
                    _uiState.value = _uiState.value.copy(isScanning = false)
                    currentScanId = null
                    scanStatusJob?.cancel()
                    refreshData()
                } catch (e: Exception) {
                    handleError("停止扫描失败", e)
                }
            } ?: run {
                _uiState.value = _uiState.value.copy(isScanning = false)
                scanStatusJob?.cancel()
            }
        }
    }

    override fun checkDeviceConnection() {
        viewModelScope.launch {
            try {
                // 注释掉 API 调用
                /*
                val response = healthAPI.getDeviceStatus()
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { deviceStatus ->
                        _uiState.value = _uiState.value.copy(isConnected = deviceStatus.isConnected)
                    }
                }
                */
                // 默认设备状态
                _uiState.value = _uiState.value.copy(isConnected = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isConnected = false)
            }
        }
    }

    override fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        scanStatusJob?.cancel()
        currentScanId = null
        super.onCleared()
    }

    private fun startMockScan() {
        scanStatusJob?.cancel()
        scanStatusJob = viewModelScope.launch {
            val scanId = currentScanId ?: return@launch
            while (_uiState.value.isScanning) {
                try {
                    // 模拟扫描过程
                    delay(2000)
                    val newScan = RecentScan(
                        id = scanId,
                        timestamp = System.currentTimeMillis(),
                        heartRate = 78,
                        spO2 = 96,
                        hrv = 50,
                        status = "success",
                        duration = 30
                    )
                    val updatedScans = _uiState.value.recentScans + newScan
                    _uiState.value = _uiState.value.copy(
                        recentScans = updatedScans,
                        healthData = HealthData(
                            heartRate = 78,
                            spO2 = 96,
                            hrv = 50,
                            isConnected = false
                        ),
                        isScanning = false
                    )
                    currentScanId = null
                    return@launch
                } catch (e: Exception) {
                    handleError("模拟扫描失败", e)
                    _uiState.value = _uiState.value.copy(isScanning = false)
                    currentScanId = null
                    return@launch
                }
            }
        }
    }

    private fun startScanStatusMonitoring() {
        scanStatusJob?.cancel()
        scanStatusJob = viewModelScope.launch {
            val scanId = currentScanId ?: return@launch
            while (_uiState.value.isScanning) {
                try {
                    // 注释掉 API 调用
                    /*
                    val response = healthAPI.getScanStatus(scanId)
                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.data?.let { scanResponse ->
                            when (scanResponse.status) {
                                "completed" -> {
                                    scanResponse.healthData?.let { healthData ->
                                        _uiState.value = _uiState.value.copy(healthData = healthData)
                                    }
                                    _uiState.value = _uiState.value.copy(isScanning = false)
                                    currentScanId = null
                                    refreshData()
                                    return@launch
                                }
                                "error" -> {
                                    _uiState.value = _uiState.value.copy(
                                        isScanning = false,
                                        error = "扫描过程中发生错误"
                                    )
                                    currentScanId = null
                                    return@launch
                                }
                                "scanning" -> {
                                    delay(2000)
                                }
                            }
                        } ?: delay(2000)
                    } else {
                        handleApiError(response)
                        delay(2000)
                    }
                    */
                    // 模拟扫描状态监控，调用模拟扫描逻辑
                    startMockScan()
                    return@launch
                } catch (e: Exception) {
                    handleError("扫描状态监控失败", e)
                    delay(2000)
                }
            }
        }
    }

    private fun handleError(message: String, throwable: Throwable) {
        _uiState.value = _uiState.value.copy(
            error = "$message: ${throwable.message}",
            isLoading = false,
            isScanning = false
        )
    }

    private fun handleApiError(response: Response<*>) {
        val errorMessage = when (response.code()) {
            404 -> "API端点未找到"
            500 -> "服务器内部错误"
            401 -> "未授权访问"
            403 -> "访问被拒绝"
            else -> "网络请求失败 (${response.code()})"
        }
        _uiState.value = _uiState.value.copy(error = errorMessage, isLoading = false)
    }

    private fun loadMockData() {
        _uiState.value = _uiState.value.copy(
            healthData = createMockHealthData(),
            healthTips = createMockHealthTips(),
            recentScans = createMockRecentScans(),
            trendData = createMockTrendData(),
            isLoading = false
        )
    }

    private fun createMockHealthData() = HealthData(
        heartRate = 76,
        spO2 = 98,
        hrv = 52,
        isConnected = false
    )

    private fun createMockHealthTips() = listOf(
        HealthTip(
            id = "tip1",
            message = "您的心率变异性降至52ms（平时65ms），深度睡眠仅1小时4分钟。压力可能正在上升。建议今天进行20-30分钟散步，减少咖啡因摄入（晨起心率：82 vs 75），注意保暖（12°C，有风）。晚上可尝试轻度瑜伽。",
            priority = "high"
        )
    )

    private fun createMockRecentScans() = listOf(
        RecentScan(
            id = "scan1",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
            heartRate = 78,
            spO2 = 97,
            hrv = 50,
            status = "success",
            duration = 30
        ),
        RecentScan(
            id = "scan2",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
            heartRate = 74,
            spO2 = 98,
            hrv = 55,
            status = "success",
            duration = 30
        ),
        RecentScan(
            id = "scan3",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 6,
            heartRate = 82,
            spO2 = 96,
            hrv = 48,
            status = "warning",
            duration = 25
        )
    )

    private fun createMockTrendData() = listOf(
        TrendData("心率", "76 bpm", "+2%", true),
        TrendData("血氧", "98%", "0%", true),
        TrendData("HRV", "52ms", "-18%", false),
        TrendData("睡眠", "6.2h", "-1.2h", false)
    )
}