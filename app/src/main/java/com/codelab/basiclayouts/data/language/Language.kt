package com.codelab.basiclayouts.data.language.model

import androidx.annotation.StringRes
import com.codelab.basiclayouts.R
import java.util.Locale

enum class Language(
    val code: String,
    val locale: Locale,
    @StringRes val displayNameRes: Int
) {
    FOLLOW_SYSTEM("system", Locale.getDefault(), R.string.language_follow_system),
    ENGLISH("en", Locale.ENGLISH, R.string.language_english),
    CHINESE("zh", Locale.CHINA, R.string.language_chinese),
    JAPANESE("ja", Locale.JAPAN, R.string.language_japanese),
    GERMAN("de", Locale.GERMANY, R.string.language_german),
    FRENCH("fr", Locale.FRANCE, R.string.language_french),
    SPANISH("es", Locale("es", "ES"), R.string.language_spanish),
    RUSSIAN("ru", Locale("ru", "RU"), R.string.language_russian),
    KOREAN("ko", Locale.KOREA, R.string.language_korean);

    companion object {
        fun fromCode(code: String): Language {
            return values().find { it.code == code } ?: FOLLOW_SYSTEM
        }

        fun getAvailableLanguages(): List<Language> {
            return listOf(
                FOLLOW_SYSTEM,
                ENGLISH,
                CHINESE,
                JAPANESE,
                GERMAN,
                FRENCH,
                SPANISH,
                RUSSIAN,
                KOREAN
            )
        }
    }
}