package com.codelab.basiclayouts.data.theme.model

import android.content.Context
import com.codelab.basiclayouts.R
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
        val builtInThemes = ColorSchemes.getBuiltInThemes(context)
        val allThemes = builtInThemes + customThemes
        emit(allThemes)
    }

    /**
     * 获取当前选中的主题
     */
    fun getCurrentTheme(): Flow<ThemeProfile> = flow {
        val currentThemeId = themePreferences.getCurrentThemeId()
        val theme = findThemeById(currentThemeId) ?: ColorSchemes.getBuiltInThemes(context).first()
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
                description = "${colorResponse.themeDescription}\n\nDesign Idea${colorResponse.themeIntention}",
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

    /**
     * 获取内置主题列表（使用Context获取本地化字符串）
     */
    fun getBuiltInThemes(): List<ThemeProfile> {
        return ColorSchemes.getBuiltInThemes(context)
    }

    /**
     * 通过ID查找内置主题
     */
    fun findBuiltInThemeById(id: String): ThemeProfile? {
        return ColorSchemes.findBuiltInThemeById(context, id)
    }

    /**
     * 创建AI主题生成提示词（使用字符串资源，支持国际化）
     */
    private fun createThemeGenerationPrompt(userInput: String): String {
        // 获取本地化字符串
        val intro = context.getString(R.string.ai_prompt_intro, userInput)
        val formatInstruction = context.getString(R.string.ai_prompt_format_instruction)
        val themeNameDesc = context.getString(R.string.ai_prompt_theme_name_desc)
        val themeDescriptionDesc = context.getString(R.string.ai_prompt_theme_description_desc)
        val themeIntentionDesc = context.getString(R.string.ai_prompt_theme_intention_desc, userInput)
        val darkColorsComment = context.getString(R.string.ai_prompt_dark_colors_comment)
        val designRequirements = context.getString(R.string.ai_prompt_design_requirements)
        val requirement1 = context.getString(R.string.ai_prompt_requirement_1)
        val requirement2 = context.getString(R.string.ai_prompt_requirement_2)
        val requirement3 = context.getString(R.string.ai_prompt_requirement_3)
        val requirement4 = context.getString(R.string.ai_prompt_requirement_4)
        val requirement5 = context.getString(R.string.ai_prompt_requirement_5)
        val requirement6 = context.getString(R.string.ai_prompt_requirement_6)
        val conclusion = context.getString(R.string.ai_prompt_conclusion, userInput)

        return """
$intro

$formatInstruction

{
  "themeName": "$themeNameDesc",
  "themeDescription": "$themeDescriptionDesc",
  "themeIntention": "$themeIntentionDesc",
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
    // $darkColorsComment
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

$designRequirements
$requirement1
$requirement2
$requirement3
$requirement4
$requirement5
$requirement6

$conclusion
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
        findBuiltInThemeById(id)?.let { return it }

        // 再在缓存主题中查找
        return loadCachedThemes().find { it.id == id }
    }
}