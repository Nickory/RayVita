package com.codelab.basiclayouts.data.language.model

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class LanguageRepository(
    private val context: Context,
    private val languagePreferences: LanguagePreferences
) {

    fun getCurrentLanguage(): Flow<Language> {
        return languagePreferences.languageFlow
    }

    fun setLanguage(language: Language) {
        languagePreferences.setLanguage(language)
        applyLanguage(language)
    }

    private fun applyLanguage(language: Language) {
        val locale = when (language) {
            Language.FOLLOW_SYSTEM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.resources.configuration.locales[0]
                } else {
                    @Suppress("DEPRECATION")
                    context.resources.configuration.locale
                }
            }
            else -> language.locale
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun restartActivity(activity: Activity) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    companion object {
        fun updateAppLanguage(context: Context, language: Language) {
            val locale = if (language == Language.FOLLOW_SYSTEM) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.resources.configuration.locales[0]
                } else {
                    @Suppress("DEPRECATION")
                    context.resources.configuration.locale
                }
            } else {
                language.locale
            }

            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLocale(locale)
            } else {
                @Suppress("DEPRECATION")
                config.locale = locale
            }

            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }
}