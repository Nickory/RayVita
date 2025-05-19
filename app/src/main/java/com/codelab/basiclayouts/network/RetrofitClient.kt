package com.codelab.basiclayouts.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit客户端单例类
 * 提供多个API接口实例
 */
object RetrofitClient {
    // DeepSeek API 配置
    private const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/"
    private const val API_KEY = "sk-0dd7411c6de74f119e9a06be144a89d6" // Replace with your real key

    // RayVita API 配置
    private const val RAYVITA_BASE_URL = "http://47.96.237.130:5000/api/"

    // 日志拦截器
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // DeepSeek API 的客户端（带有 API 密钥认证）
    private val deepseekClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $API_KEY")
                .build()
            chain.proceed(request)
        })
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // RayVita API 的客户端（无需 API 密钥认证）
    private val rayvitaClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // DeepSeek API Retrofit 实例
    private val deepseekRetrofit = Retrofit.Builder()
        .baseUrl(DEEPSEEK_BASE_URL)
        .client(deepseekClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // RayVita API Retrofit 实例
    private val rayvitaRetrofit = Retrofit.Builder()
        .baseUrl(RAYVITA_BASE_URL)
        .client(rayvitaClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // DeepSeek API 接口
    val deepseekApi: DeepSeekApi by lazy {
        deepseekRetrofit.create(DeepSeekApi::class.java)
    }

    // RayVita 认证 API 接口
    val authApi: AuthApi by lazy {
        rayvitaRetrofit.create(AuthApi::class.java)
    }
}