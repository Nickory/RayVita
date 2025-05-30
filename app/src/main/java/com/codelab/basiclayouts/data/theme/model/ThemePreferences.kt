package com.codelab.basiclayouts.data.theme.model


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemePreferences(private val context: Context) {

    companion object {
        private val CURRENT_THEME_ID = stringPreferencesKey("current_theme_id")
        private val DEFAULT_THEME_ID = "warm_earth" // 默认主题
    }

    /**
     * 获取当前主题ID的Flow
     */
    fun getCurrentThemeIdFlow(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[CURRENT_THEME_ID] ?: DEFAULT_THEME_ID
        }
    }

    /**
     * 获取当前主题ID（挂起函数）
     */
    suspend fun getCurrentThemeId(): String {
        return getCurrentThemeIdFlow().first()
    }

    /**
     * 设置当前主题ID
     */
    suspend fun setCurrentThemeId(themeId: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_THEME_ID] = themeId
        }
    }

    /**
     * 重置为默认主题
     */
    suspend fun resetToDefault() {
        setCurrentThemeId(DEFAULT_THEME_ID)
    }
}