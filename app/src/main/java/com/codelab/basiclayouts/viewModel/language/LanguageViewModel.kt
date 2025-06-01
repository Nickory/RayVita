package com.codelab.basiclayouts.viewModel.language

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.data.language.model.Language
import com.codelab.basiclayouts.data.language.model.LanguageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LanguageUiState(
    val currentLanguage: Language = Language.FOLLOW_SYSTEM,
    val availableLanguages: List<Language> = Language.getAvailableLanguages(),
    val isLoading: Boolean = false
)

class LanguageViewModel(
    private val languageRepository: LanguageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageUiState())
    val uiState: StateFlow<LanguageUiState> = _uiState.asStateFlow()

    init {
        loadCurrentLanguage()
    }

    private fun loadCurrentLanguage() {
        viewModelScope.launch {
            languageRepository.getCurrentLanguage().collect { language ->
                _uiState.value = _uiState.value.copy(currentLanguage = language)
            }
        }
    }

    fun selectLanguage(language: Language, activity: Activity? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            languageRepository.setLanguage(language)

            // 如果提供了activity，重启它以应用语言更改
            activity?.let {
                languageRepository.restartActivity(it)
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}