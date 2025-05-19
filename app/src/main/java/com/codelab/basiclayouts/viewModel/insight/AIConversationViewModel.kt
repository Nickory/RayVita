package com.codelab.basiclayouts.viewmodel.insight

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.network.RetrofitClient
import com.codelab.basiclayouts.network.model.ChatRequest
import com.codelab.basiclayouts.network.model.ChatResponse
import com.codelab.basiclayouts.network.model.Message
import com.codelab.basiclayouts.ui.screen.insight.ChatMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AIConversationViewModel(private val context: Context) : ViewModel() {
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("AIChatPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        loadChatHistory()
        if (_chatMessages.value.isEmpty()) {
            val timestamp = getCurrentTimestamp()
            _chatMessages.value = listOf(
                ChatMessage("Hello! I'm your health AI assistant. How can I help you today?", false, timestamp)
            )
            saveChatHistory()
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val timestamp = getCurrentTimestamp()
        val userMessage = ChatMessage(content, true, timestamp)
        _chatMessages.value = _chatMessages.value + userMessage
        _isTyping.value = true
        saveChatHistory()

        // 定义前置提示词
        val systemPrompt = "You are a concise health AI assistant in RayVita App (rPPG health app) called litte R. Provide accurate, helpful health advice in a friendly tone, keeping responses under 200 words 或200字，根据用户语言切换,用户英文提问则回答全英文，中文则中文回答"
        val messages = listOf(
            Message("system", systemPrompt), // 添加 system 提示词
            Message("user", content) // 用户消息
        )
        val request = ChatRequest(
            model = "deepseek-chat",
            messages = messages,
            system = systemPrompt // 兼容 API 可能需要的单独 system 字段
        )

        RetrofitClient.deepseekApi.chatCompletion(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                viewModelScope.launch {
                    delay(800)
                    val aiResponse = response.body()?.choices?.firstOrNull()?.message?.content
                        ?.take(200) // 额外截断确保不超过 200 字
                        ?: "Sorry, I couldn't process your request. Try again."
                    Log.d("AIConversation", "AI Response: $aiResponse, length: ${aiResponse.length}")
                    val aiMessage = ChatMessage(aiResponse, false, getCurrentTimestamp())
                    _chatMessages.value = _chatMessages.value + aiMessage
                    _isTyping.value = false
                    saveChatHistory()
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                viewModelScope.launch {
                    delay(500)
                    val errorMessage = ChatMessage(
                        "Connection error. Please check your network and try again.",
                        false,
                        getCurrentTimestamp()
                    )
                    _chatMessages.value = _chatMessages.value + errorMessage
                    _isTyping.value = false
                    saveChatHistory()
                }
            }
        })
    }

    fun markMessageAsHelpful(message: ChatMessage) {
        val updatedMessages = _chatMessages.value.map {
            if (it == message) it.copy(isHelpful = true, isNotHelpful = false) else it
        }
        _chatMessages.value = updatedMessages
        saveChatHistory()
    }

    fun markMessageAsNotHelpful(message: ChatMessage) {
        val updatedMessages = _chatMessages.value.map {
            if (it == message) it.copy(isHelpful = false, isNotHelpful = true) else it
        }
        _chatMessages.value = updatedMessages
        saveChatHistory()
    }

    fun regenerateResponse(message: ChatMessage) {
        val userMessageIndex = _chatMessages.value.indexOfLast { it.isFromUser && it.timestamp < message.timestamp }
        if (userMessageIndex != -1) {
            val userMessage = _chatMessages.value[userMessageIndex]
            val updatedMessages = _chatMessages.value.toMutableList().apply { remove(message) }
            _chatMessages.value = updatedMessages
            saveChatHistory()
            sendMessage(userMessage.content) // Re-send the original user message to get a new response
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = listOf(
            ChatMessage("Chat history cleared. How can I assist you now?", false, getCurrentTimestamp())
        )
        saveChatHistory()
    }

    private fun loadChatHistory() {
        val json = sharedPreferences.getString("chat_history", null)
        if (json != null) {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            _chatMessages.value = gson.fromJson(json, type) ?: emptyList()
        }
    }

    private fun saveChatHistory() {
        val json = gson.toJson(_chatMessages.value)
        sharedPreferences.edit { putString("chat_history", json) }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}