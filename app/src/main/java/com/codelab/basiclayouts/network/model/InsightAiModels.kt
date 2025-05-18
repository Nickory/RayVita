package com.codelab.basiclayouts.network.model

data class Message(
    val role: String,
    val content: String
)

data class ChatRequest(
    val model: String = "deepseek-chat",
    val messages: List<Message>
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
