package com.codelab.basiclayouts.data.home.network

import com.codelab.basiclayouts.data.home.api.HealthAPI
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit单例实例管理（占位模式，暂无实际网络请求）
 */
object RetrofitInstance {

    // 占位 BASE_URL（无需真实地址）
    private const val BASE_URL = "https://placeholder.com/"

    // Gson配置（占位）
    private val gson by lazy {
        GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()
    }

    // 占位 Retrofit 实例（无需 OkHttpClient）
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // 占位 HealthAPI 实例
    val healthAPI: HealthAPI by lazy {
        retrofit.create(HealthAPI::class.java)
    }

    // 占位动态方法
    fun createHealthAPI(baseUrl: String): HealthAPI {
        return retrofit.create(HealthAPI::class.java)
    }
}