package com.codelab.basiclayouts.network.model

import com.codelab.basiclayouts.data.physnet.model.HealthMeasurementDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HealthApiService {
    @POST("health_measurements")
    suspend fun createMeasurement(@Body measurement: HealthMeasurementDto): Map<String, Any>

    @GET("health_measurements/pending")
    suspend fun getPendingMeasurements(@Query("user_id") userId: Long): List<HealthMeasurementDto>

    @POST("health_measurements/sync")
    suspend fun syncMeasurements(@Body body: Map<String, List<String>>): Map<String, Any>
}