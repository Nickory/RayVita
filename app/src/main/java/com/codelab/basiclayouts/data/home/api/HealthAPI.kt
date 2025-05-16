package com.codelab.basiclayouts.data.home.api

import com.codelab.basiclayouts.ui.viewmodel.home.HealthData
import com.codelab.basiclayouts.ui.viewmodel.home.HealthTip
import com.codelab.basiclayouts.ui.viewmodel.home.RecentScan
import com.codelab.basiclayouts.ui.viewmodel.home.TrendData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Flask API响应包装类
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

// 扫描请求参数
data class ScanRequest(
    val deviceId: String? = null,
    val duration: Int = 30, // 扫描时长(秒)
    val scanType: String = "full" // full, quick, custom
)

// 扫描响应
data class ScanResponse(
    val scanId: String,
    val status: String,
    val progress: Int = 0,
    val estimatedTime: Int = 0,
    val healthData: HealthData? = null
)

// 设备状态响应
data class DeviceStatus(
    val isConnected: Boolean,
    val deviceId: String? = null,
    val batteryLevel: Int = 0,
    val signalStrength: Int = 0,
    val lastSeen: Long = 0L
)

/**
 * Flask后端API接口定义
 */
interface HealthAPI {

    // 获取当前健康数据
    @GET("/api/health/current")
    suspend fun getCurrentHealthData(): Response<ApiResponse<HealthData>>

    // 获取健康建议
    @GET("/api/health/tips")
    suspend fun getHealthTips(
        @Query("limit") limit: Int = 10,
        @Query("priority") priority: String? = null
    ): Response<ApiResponse<List<HealthTip>>>

    // 获取最近扫描记录
    @GET("/api/health/scans/recent")
    suspend fun getRecentScans(
        @Query("limit") limit: Int = 5,
        @Query("days") days: Int = 7
    ): Response<ApiResponse<List<RecentScan>>>

    // 获取健康趋势数据
    @GET("/api/health/trends")
    suspend fun getHealthTrends(
        @Query("period") period: String = "week" // day, week, month
    ): Response<ApiResponse<List<TrendData>>>

    // 开始扫描
    @POST("/api/health/scan/start")
    suspend fun startScan(
        @Body request: ScanRequest
    ): Response<ApiResponse<ScanResponse>>

    // 停止扫描
    @POST("/api/health/scan/stop/{scanId}")
    suspend fun stopScan(
        @Path("scanId") scanId: String
    ): Response<ApiResponse<Boolean>>

    // 获取扫描状态
    @GET("/api/health/scan/status/{scanId}")
    suspend fun getScanStatus(
        @Path("scanId") scanId: String
    ): Response<ApiResponse<ScanResponse>>

    // 检查设备连接状态
    @GET("/api/device/status")
    suspend fun getDeviceStatus(): Response<ApiResponse<DeviceStatus>>

    // 连接设备
    @POST("/api/device/connect")
    suspend fun connectDevice(
        @Query("deviceId") deviceId: String? = null
    ): Response<ApiResponse<DeviceStatus>>

    // 断开设备连接
    @POST("/api/device/disconnect")
    suspend fun disconnectDevice(): Response<ApiResponse<Boolean>>

    // 获取历史数据
    @GET("/api/health/history")
    suspend fun getHealthHistory(
        @Query("start") startTime: Long,
        @Query("end") endTime: Long,
        @Query("type") dataType: String // heartRate, spO2, hrv
    ): Response<ApiResponse<List<HealthData>>>

    // 上传健康数据
    @POST("/api/health/upload")
    suspend fun uploadHealthData(
        @Body healthData: HealthData
    ): Response<ApiResponse<Boolean>>

    // 获取用户配置
    @GET("/api/user/settings")
    suspend fun getUserSettings(): Response<ApiResponse<Map<String, Any>>>

    // 更新用户配置
    @PUT("/api/user/settings")
    suspend fun updateUserSettings(
        @Body settings: Map<String, Any>
    ): Response<ApiResponse<Boolean>>
}