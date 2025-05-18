package com.codelab.basiclayouts.network

import com.codelab.basiclayouts.network.model.ChatRequest
import com.codelab.basiclayouts.network.model.ChatResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface DeepSeekApi {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    fun chatCompletion(
        @Body request: ChatRequest
    ): Call<ChatResponse>
}
