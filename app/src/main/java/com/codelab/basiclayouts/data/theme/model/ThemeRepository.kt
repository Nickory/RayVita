package com.codelab.basiclayouts.data.theme.model

import android.content.Context
import com.codelab.basiclayouts.network.RetrofitClient
import com.codelab.basiclayouts.network.model.ChatRequest
import com.codelab.basiclayouts.network.model.Message
import com.codelab.basiclayouts.ui.theme.ColorSchemes
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ThemeRepository(
    private val context: Context,
    private val themePreferences: ThemePreferences
) {
    private val gson = Gson()
    private val cacheDir = File(context.cacheDir, "themes")

    // 使用现有的DeepSeek API客户端
    private val deepseekApi = RetrofitClient.deepseekApi

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    /**
     * 获取所有主题（内置主题 + 缓存的自定义主题）
     */
    fun getAllThemes(): Flow<List<ThemeProfile>> = flow {
        val customThemes = loadCachedThemes()
        val allThemes = ColorSchemes.builtInThemes + customThemes
        emit(allThemes)
    }

    /**
     * 获取当前选中的主题
     */
    fun getCurrentTheme(): Flow<ThemeProfile> = flow {
        val currentThemeId = themePreferences.getCurrentThemeId()
        val theme = findThemeById(currentThemeId) ?: ColorSchemes.builtInThemes.first()
        emit(theme)
    }

    /**
     * 设置当前主题
     */
    suspend fun setCurrentTheme(themeId: String) {
        themePreferences.setCurrentThemeId(themeId)
    }

    /**
     * 通过DeepSeek API生成新主题
     */
    suspend fun generateThemeFromText(userInput: String): Result<ThemeProfile> {
        return try {
            val prompt = createThemeGenerationPrompt(userInput)
            val request = ChatRequest(
                messages = listOf(
                    Message(role = "user", content = prompt)
                ),
                model = "deepseek-chat"
            )

            // 使用现有的DeepSeek API客户端
            val response = suspendCancellableCoroutine { continuation ->
                val call = deepseekApi.chatCompletion(request)
                call.enqueue(object : retrofit2.Callback<com.codelab.basiclayouts.network.model.ChatResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<com.codelab.basiclayouts.network.model.ChatResponse>,
                        response: retrofit2.Response<com.codelab.basiclayouts.network.model.ChatResponse>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let { continuation.resume(it) }
                                ?: continuation.resumeWithException(Exception("Empty response body"))
                        } else {
                            continuation.resumeWithException(
                                Exception("API call failed: ${response.code()} ${response.message()}")
                            )
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.codelab.basiclayouts.network.model.ChatResponse>,
                        t: Throwable
                    ) {
                        continuation.resumeWithException(t)
                    }
                })

                continuation.invokeOnCancellation {
                    call.cancel()
                }
            }

            val responseContent = response.choices.firstOrNull()?.message?.content
                ?: throw Exception("Empty response from DeepSeek API")

            // 解析JSON响应
            val colorResponse = parseColorResponse(responseContent)

            // 创建主题配置
            val themeProfile = ThemeProfile(
                id = UUID.randomUUID().toString(),
                name = colorResponse.themeName,
                description = "${colorResponse.themeDescription}\n\n设计理念：${colorResponse.themeIntention}",
                lightColors = colorResponse.lightColors,
                darkColors = colorResponse.darkColors,
                isBuiltIn = false
            )

            // 保存到缓存
            saveThemeToCache(themeProfile)

            Result.success(themeProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除自定义主题
     */
    suspend fun deleteCustomTheme(themeId: String): Boolean {
        return try {
            val file = File(cacheDir, "$themeId.json")
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun createThemeGenerationPrompt(userInput: String): String {
        return """
你是一个专业的UI/UX设计师和色彩专家。请根据用户的描述"$userInput"，为Android Material Design 3应用设计一套完整的主题配色方案。

请严格按照以下JSON格式返回，确保包含所有必需的颜色值：

{
  "themeName": "主题名称（简洁有意义）",
  "themeDescription": "主题描述（50字以内，描述视觉特点和适用场景）",
  "themeIntention": "设计理念（100字以内，解释色彩选择的意义和情感表达，紧扣描述词"$userInput"）",
  "lightColors": {
    "primary": "#RRGGBB",
    "onPrimary": "#RRGGBB",
    "primaryContainer": "#RRGGBB",
    "onPrimaryContainer": "#RRGGBB",
    "secondary": "#RRGGBB",
    "onSecondary": "#RRGGBB",
    "secondaryContainer": "#RRGGBB",
    "onSecondaryContainer": "#RRGGBB",
    "tertiary": "#RRGGBB",
    "onTertiary": "#RRGGBB",
    "tertiaryContainer": "#RRGGBB",
    "onTertiaryContainer": "#RRGGBB",
    "error": "#RRGGBB",
    "errorContainer": "#RRGGBB",
    "onError": "#RRGGBB",
    "onErrorContainer": "#RRGGBB",
    "background": "#RRGGBB",
    "onBackground": "#RRGGBB",
    "surface": "#RRGGBB",
    "onSurface": "#RRGGBB",
    "surfaceVariant": "#RRGGBB",
    "onSurfaceVariant": "#RRGGBB",
    "outline": "#RRGGBB",
    "inverseOnSurface": "#RRGGBB",
    "inverseSurface": "#RRGGBB",
    "inversePrimary": "#RRGGBB",
    "shadow": "#000000",
    "surfaceTint": "#RRGGBB",
    "outlineVariant": "#RRGGBB",
    "scrim": "#000000"
  },
  "darkColors": {
    // 相同结构，但适配暗色模式
    "primary": "#RRGGBB",
    "onPrimary": "#RRGGBB",
    "primaryContainer": "#RRGGBB",
    "onPrimaryContainer": "#RRGGBB",
    "secondary": "#RRGGBB",
    "onSecondary": "#RRGGBB",
    "secondaryContainer": "#RRGGBB",
    "onSecondaryContainer": "#RRGGBB",
    "tertiary": "#RRGGBB",
    "onTertiary": "#RRGGBB",
    "tertiaryContainer": "#RRGGBB",
    "onTertiaryContainer": "#RRGGBB",
    "error": "#RRGGBB",
    "errorContainer": "#RRGGBB",
    "onError": "#RRGGBB",
    "onErrorContainer": "#RRGGBB",
    "background": "#RRGGBB",
    "onBackground": "#RRGGBB",
    "surface": "#RRGGBB",
    "onSurface": "#RRGGBB",
    "surfaceVariant": "#RRGGBB",
    "onSurfaceVariant": "#RRGGBB",
    "outline": "#RRGGBB",
    "inverseOnSurface": "#RRGGBB",
    "inverseSurface": "#RRGGBB",
    "inversePrimary": "#RRGGBB",
    "shadow": "#000000",
    "surfaceTint": "#RRGGBB",
    "outlineVariant": "#RRGGBB",
    "scrim": "#000000"
  }
}

设计要求：
1. 确保足够的对比度，符合WCAG无障碍标准
2. 颜色搭配和谐，符合色彩理论
3. 考虑用户情感和使用场景
4. light和dark主题要相互呼应
5. 所有颜色值必须是有效的十六进制格式
6. 请直接返回JSON，不要包含其他文字

请根据"$userInput"这个描述，生成一套专业的主题配色方案。
        """.trimIndent()
    }

    private fun parseColorResponse(responseContent: String): DeepSeekColorResponse {
        // 提取JSON部分（去除可能的额外文字）
        val jsonStart = responseContent.indexOf('{')
        val jsonEnd = responseContent.lastIndexOf('}')

        if (jsonStart == -1 || jsonEnd == -1) {
            throw Exception("Invalid JSON response format")
        }

        val jsonContent = responseContent.substring(jsonStart, jsonEnd + 1)

        return try {
            gson.fromJson(jsonContent, DeepSeekColorResponse::class.java)
        } catch (e: JsonSyntaxException) {
            throw Exception("Failed to parse color response: ${e.message}")
        }
    }

    private fun saveThemeToCache(theme: ThemeProfile) {
        try {
            val file = File(cacheDir, "${theme.id}.json")
            val json = gson.toJson(theme)
            file.writeText(json)
        } catch (e: Exception) {
            // 静默处理缓存错误
        }
    }

    private fun loadCachedThemes(): List<ThemeProfile> {
        return try {
            cacheDir.listFiles()?.mapNotNull { file ->
                if (file.extension == "json") {
                    try {
                        val json = file.readText()
                        gson.fromJson(json, ThemeProfile::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            }?.sortedByDescending { it.createdAt } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun findThemeById(id: String): ThemeProfile? {
        // 先在内置主题中查找
        ColorSchemes.builtInThemes.find { it.id == id }?.let { return it }

        // 再在缓存主题中查找
        return loadCachedThemes().find { it.id == id }
    }
}