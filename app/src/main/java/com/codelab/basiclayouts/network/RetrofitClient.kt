package com.codelab.basiclayouts.network

import android.content.Context
import com.codelab.basiclayouts.network.model.HealthApiService
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
    private const val API_KEY = "sk-0dd7411c6de74f119e9a06be144a89d6"

    // RayVita API 配置
    private const val RAYVITA_BASE_URL = "http://47.96.237.130:5000/api/"

    // 日志拦截器
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 获取 JWT token（从 SharedPreferences）
    private fun getJwtToken(context: Context?): String? {
        return context?.getSharedPreferences("auth", Context.MODE_PRIVATE)
            ?.getString("jwt_token", null)
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

    // RayVita API 的客户端（动态添加 JWT）
    private fun createRayvitaClient(context: Context?): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                getJwtToken(context)?.let { token ->
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            })
            .addInterceptor(loggingInterceptor)
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

    // RayVita API Retrofit 实例（延迟初始化，依赖 Context）
    private fun getRayvitaRetrofit(context: Context?): Retrofit {
        return Retrofit.Builder()
            .baseUrl(RAYVITA_BASE_URL)
            .client(createRayvitaClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // DeepSeek API 接口
    val deepseekApi: DeepSeekApi by lazy {
        deepseekRetrofit.create(DeepSeekApi::class.java)
    }

    // RayVita 认证 API 接口
    fun getAuthApi(context: Context?): AuthApi {
        return getRayvitaRetrofit(context).create(AuthApi::class.java)
    }

    // RayVita 健康 API 接口
    fun getHealthApi(context: Context?): HealthApiService {
        return getRayvitaRetrofit(context).create(HealthApiService::class.java)
    }

    // RayVita 社交 API 接口
    fun getSocialApi(context: Context?): SocialApi {
        return getRayvitaRetrofit(context).create(SocialApi::class.java)
    }
}