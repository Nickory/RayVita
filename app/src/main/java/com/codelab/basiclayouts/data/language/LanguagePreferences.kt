package com.codelab.basiclayouts.data.language.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LanguagePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _languageFlow = MutableStateFlow(getCurrentLanguage())
    val languageFlow: Flow<Language> = _languageFlow.asStateFlow()

    fun getCurrentLanguage(): Language {
        val savedCode = prefs.getString(KEY_LANGUAGE, Language.FOLLOW_SYSTEM.code)
        return Language.fromCode(savedCode ?: Language.FOLLOW_SYSTEM.code)
    }

    fun setLanguage(language: Language) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        _languageFlow.value = language
    }

    companion object {
        private const val PREFS_NAME = "language_prefs"
        private const val KEY_LANGUAGE = "selected_language"
    }
}