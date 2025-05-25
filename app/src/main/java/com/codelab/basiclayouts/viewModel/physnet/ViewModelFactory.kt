package com.codelab.basiclayouts.viewModel.physnet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codelab.basiclayouts.data.physnet.EnhancedRppgProcessor
import com.codelab.basiclayouts.data.physnet.EnhancedRppgRepository
import com.codelab.basiclayouts.data.physnet.VideoRecorder

/**
 * 增强的ViewModel工厂类
 */
class EnhancedRppgViewModelFactory(
    private val context: Context,
    private val videoRecorder: VideoRecorder,
    private val rppgProcessor: EnhancedRppgProcessor,
    private val repository: EnhancedRppgRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(EnhancedRppgViewModel::class.java) -> {
                EnhancedRppgViewModel(
                    context = context,
                    videoRecorder = videoRecorder,
                    rppgProcessor = rppgProcessor,
                    repository = repository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

/**
 * 原版本兼容的ViewModel工厂类（如果需要）
 */
class RppgViewModelFactory(
    private val context: Context,
    private val videoRecorder: VideoRecorder,
    private val rppgProcessor: EnhancedRppgProcessor,
    private val repository: EnhancedRppgRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(EnhancedRppgViewModel::class.java) -> {
                EnhancedRppgViewModel(
                    context = context,
                    videoRecorder = videoRecorder,
                    rppgProcessor = rppgProcessor,
                    repository = repository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}