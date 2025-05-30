package com.codelab.basiclayouts.viewModel.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codelab.basiclayouts.R
import com.codelab.basiclayouts.data.theme.model.ThemeProfile
import com.codelab.basiclayouts.data.theme.model.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ThemeUiState(
    val allThemes: List<ThemeProfile> = emptyList(),
    val currentTheme: ThemeProfile? = null,
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ThemeViewModel(
    private val themeRepository: ThemeRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    init {
        loadThemes()
    }

    private fun loadThemes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                combine(
                    themeRepository.getAllThemes(),
                    themeRepository.getCurrentTheme()
                ) { allThemes, currentTheme ->
                    _uiState.value = _uiState.value.copy(
                        allThemes = allThemes,
                        currentTheme = currentTheme,
                        isLoading = false
                    )
                }.catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = context.getString(R.string.loading_failed, exception.message ?: context.getString(R.string.unknown_error))
                    )
                }.collect {}
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = context.getString(R.string.loading_failed, e.message ?: context.getString(R.string.unknown_error))
                )
            }
        }
    }

    fun selectTheme(themeId: String) {
        viewModelScope.launch {
            try {
                themeRepository.setCurrentTheme(themeId)
                val selectedTheme = _uiState.value.allThemes.find { it.id == themeId }
                if (selectedTheme != null) {
                    _uiState.value = _uiState.value.copy(
                        currentTheme = selectedTheme,
                        successMessage = context.getString(R.string.theme_switched, selectedTheme.name)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = context.getString(R.string.theme_switch_failed, e.message ?: context.getString(R.string.unknown_error))
                )
            }
        }
    }

    fun generateThemeFromText(userInput: String) {
        if (userInput.isBlank()) {
            _uiState.value = _uiState.value.copy(error = context.getString(R.string.input_description_required))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGenerating = true,
                error = null,
                successMessage = null
            )

            try {
                val result = themeRepository.generateThemeFromText(userInput.trim())
                if (result.isSuccess) {
                    val newTheme = result.getOrNull()!!
                    loadThemes()
                    selectTheme(newTheme.id)
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        successMessage = context.getString(R.string.theme_generated_success, newTheme.name)
                    )
                } else {
                    val exception = result.exceptionOrNull()
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        error = context.getString(R.string.theme_generation_failed, exception?.message ?: context.getString(R.string.unknown_error))
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    error = context.getString(R.string.theme_generation_failed, e.message ?: context.getString(R.string.unknown_error))
                )
            }
        }
    }

    fun deleteTheme(themeId: String) {
        viewModelScope.launch {
            try {
                val success = themeRepository.deleteCustomTheme(themeId)
                if (success) {
                    if (_uiState.value.currentTheme?.id == themeId) {
                        selectTheme("warm_earth")
                    }
                    loadThemes()
                    _uiState.value = _uiState.value.copy(
                        successMessage = context.getString(R.string.theme_deleted_success)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = context.getString(R.string.theme_delete_failed, context.getString(R.string.unknown_error))
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = context.getString(R.string.theme_delete_failed, e.message ?: context.getString(R.string.unknown_error))
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun refresh() {
        loadThemes()
    }
}

class ThemeViewModelFactory(
    private val themeRepository: ThemeRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(themeRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}