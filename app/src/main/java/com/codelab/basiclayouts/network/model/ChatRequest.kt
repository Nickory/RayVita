//package com.codelab.basiclayouts.network.model
//
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class ChatRequest(
//    val model: String = "deepseek-chat",
//    val messages: List<Message>,
//    val temperature: Double = 0.7,
//    val max_tokens: Int = 2000,
//    val stream: Boolean = false
//)
//
//@Serializable
//data class Message(
//    val role: String,
//    val content: String
//)
//
//@Serializable
//data class ChatResponse(
//    val id: String,
//    val created: Long,
//    val model: String,
//    val choices: List<Choice>
//)
//
//@Serializable
//data class Choice(
//    val index: Int,
//    val message: Message,
//    val finish_reason: String
//)
//
//// network/DeepSeekApiService.kt
//package com.codelab.basiclayouts.network
//
//import com.codelab.basiclayouts.network.model.ChatRequest
//import com.codelab.basiclayouts.network.model.ChatResponse
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.Body
//import retrofit2.http.Header
//import retrofit2.http.Headers
//import retrofit2.http.POST
//import java.util.concurrent.TimeUnit
//
//interface DeepSeekApiService {
//    @POST("v1/chat/completions")
//    suspend fun chatCompletion(
//        @Header("Authorization") authorization: String,
//        @Body request: ChatRequest
//    ): ChatResponse
//}
//
//object DeepSeekApiClient {
//    private const val BASE_URL = "https://api.deepseek.com/"
//
//    private val loggingInterceptor = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }
//
//    private val client = OkHttpClient.Builder()
//        .addInterceptor(loggingInterceptor)
//        .connectTimeout(30, TimeUnit.SECONDS)
//        .readTimeout(30, TimeUnit.SECONDS)
//        .build()
//
//    val api: DeepSeekApiService = Retrofit.Builder()
//        .baseUrl(BASE_URL)
//        .client(client)
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//        .create(DeepSeekApiService::class.java)
//}