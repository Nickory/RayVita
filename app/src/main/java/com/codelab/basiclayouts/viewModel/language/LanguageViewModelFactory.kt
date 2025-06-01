package com.codelab.basiclayouts.viewModel.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codelab.basiclayouts.data.language.model.LanguageRepository

class LanguageViewModelFactory(
    private val languageRepository: LanguageRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LanguageViewModel::class.java)) {
            return LanguageViewModel(languageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}