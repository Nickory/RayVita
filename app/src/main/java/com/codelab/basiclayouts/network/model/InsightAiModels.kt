package com.codelab.basiclayouts.network.model

data class Message(
    val role: String,
    val content: String
)

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int? = null,
    val system: String? = null // 添加 system 字段用于前置提示词
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
