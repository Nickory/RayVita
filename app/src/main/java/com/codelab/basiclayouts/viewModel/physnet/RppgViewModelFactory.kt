
package com.codelab.basiclayouts.viewModel.physnet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codelab.basiclayouts.data.physnet.EnhancedRppgProcessor
import com.codelab.basiclayouts.data.physnet.EnhancedRppgRepository
import com.codelab.basiclayouts.data.physnet.VideoRecorder

class EnhancedRppgViewModelFactory(
    private val context: Context,
    private val videoRecorder: VideoRecorder,
    private val rppgProcessor: EnhancedRppgProcessor,
    private val repository: EnhancedRppgRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EnhancedRppgViewModel::class.java)) {
            return EnhancedRppgViewModel(
                context,
                videoRecorder,
                rppgProcessor,
                repository,
                MeasurementStorageViewModel(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
