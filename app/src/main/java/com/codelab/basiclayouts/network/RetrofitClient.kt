package com.codelab.basiclayouts.network

import android.content.Context
import com.codelab.basiclayouts.data.api.HealthApiService
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
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // RayVita API 的客户端（支持动态 JWT 认证）
    private fun createRayvitaClient(context: Context?): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                // 可选：添加 JWT 认证（需 UserSessionManager）
                val token = context?.let {
                    // 示例：UserSessionManager(context).getToken() ?: ""
                    ""
                } ?: ""
                val request = chain.request().newBuilder()
                    .apply { if (token.isNotEmpty()) addHeader("Authorization", "Bearer $token") }
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // DeepSeek API Retrofit 实例
    private val deepseekRetrofit = Retrofit.Builder()
        .baseUrl(DEEPSEEK_BASE_URL)
        .client(deepseekClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // RayVita API Retrofit 实例（静态）
    private val rayvitaRetrofit = Retrofit.Builder()
        .baseUrl(RAYVITA_BASE_URL)
        .client(createRayvitaClient(null))
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 创建 RayVita Retrofit 实例（动态）
    private fun createRayvitaRetrofit(context: Context?): Retrofit {
        return Retrofit.Builder()
            .baseUrl(RAYVITA_BASE_URL)
            .client(createRayvitaClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // DeepSeek API 接口（静态）
    val deepseekApi: DeepSeekApi by lazy {
        deepseekRetrofit.create(DeepSeekApi::class.java)
    }

    // RayVita 认证 API 接口（静态）
    val authApi: AuthApi by lazy {
        rayvitaRetrofit.create(AuthApi::class.java)
    }

    // RayVita 社交 API 接口（静态）
    val socialApi: SocialApi by lazy {
        rayvitaRetrofit.create(SocialApi::class.java)
    }

    // 动态获取 RayVita 认证 API 接口
    fun getAuthApi(context: Context? = null): AuthApi {
        return createRayvitaRetrofit(context).create(AuthApi::class.java)
    }

    // 动态获取 RayVita 健康 API 接口
    fun getHealthApi(context: Context? = null): HealthApiService {
        return createRayvitaRetrofit(context).create(HealthApiService::class.java)
    }

    // 动态获取 RayVita 社交 API 接口
    fun getSocialApi(context: Context? = null): SocialApi {
        return createRayvitaRetrofit(context).create(SocialApi::class.java)
    }
}