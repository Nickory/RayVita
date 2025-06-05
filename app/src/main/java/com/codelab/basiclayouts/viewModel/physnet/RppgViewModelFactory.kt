package com.codelab.basiclayouts.viewModel.physnet

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codelab.basiclayouts.data.physnet.RppgDependencyManager

/**
 * 增强的rPPG ViewModel工厂类
 * 使用单例依赖管理器避免重复初始化
 */
class EnhancedRppgViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    companion object {
        private const val TAG = "EnhancedRppgViewModelFactory"
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.d(TAG, "创建ViewModel: ${modelClass.simpleName}")

        when {
            modelClass.isAssignableFrom(EnhancedRppgViewModel::class.java) -> {
                // 使用单例依赖管理器获取共享实例
                val dependencies = RppgDependencyManager.getInstance(context)

                Log.d(TAG, "依赖项初始化状态: ${dependencies.getInitializationStatus()}")

                return EnhancedRppgViewModel(
                    context = context,
                    videoRecorder = dependencies.videoRecorder,
                    rppgProcessor = dependencies.rppgProcessor,
                    repository = dependencies.repository,
                    storageViewModel = MeasurementStorageViewModel(context)
                ) as T
            }

            modelClass.isAssignableFrom(MeasurementStorageViewModel::class.java) -> {
                return MeasurementStorageViewModel(context) as T
            }

            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}

/**
 * 兼容性工厂类，用于替换原有的RppgViewModelFactory
 */
class RppgViewModelFactory(
    private val context: Context,
    @Deprecated("不再使用，将通过依赖管理器自动创建")
    private val videoRecorder: com.codelab.basiclayouts.data.physnet.VideoRecorder? = null,
    @Deprecated("不再使用，将通过依赖管理器自动创建")
    private val rppgProcessor: com.codelab.basiclayouts.data.physnet.EnhancedRppgProcessor? = null,
    @Deprecated("不再使用，将通过依赖管理器自动创建")
    private val repository: com.codelab.basiclayouts.data.physnet.EnhancedRppgRepository? = null
) : ViewModelProvider.Factory {

    companion object {
        private const val TAG = "RppgViewModelFactory"
    }

    init {
        Log.w(TAG, "使用了已废弃的RppgViewModelFactory，建议迁移到EnhancedRppgViewModelFactory")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EnhancedRppgViewModel::class.java)) {
            // 忽略传入的参数，使用新的依赖管理器
            val dependencies = RppgDependencyManager.getInstance(context)

            return EnhancedRppgViewModel(
                context = context,
                videoRecorder = dependencies.videoRecorder,
                rppgProcessor = dependencies.rppgProcessor,
                repository = dependencies.repository,
                storageViewModel = MeasurementStorageViewModel(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}