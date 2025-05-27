package com.codelab.basiclayouts.data.api

import com.codelab.basiclayouts.data.model.HealthMeasurementDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HealthApiService {
    @POST("health_measurements")
    suspend fun createMeasurement(@Body measurement: HealthMeasurementDto): Response<Map<String, String>>

    @GET("health_measurements/pending")
    suspend fun getPendingMeasurements(@Query("user_id") userId: String): List<HealthMeasurementDto> // 改为查询参数

    @POST("health_measurements/sync")
    suspend fun syncMeasurements(@Body body: Map<String, List<String>>): Response<Map<String, String>> // 移除通配符
}