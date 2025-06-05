package com.codelab.basiclayouts.data.physnet

import android.content.Context
import android.util.Log

/**
 * rPPG依赖项管理器 - 使用单例模式避免重复初始化
 */
object RppgDependencyManager {
    private const val TAG = "RppgDependencyManager"

    @Volatile
    private var INSTANCE: RppgDependencies? = null

    /**
     * 获取单例实例
     */
    fun getInstance(context: Context): RppgDependencies {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: createDependencies(context.applicationContext).also {
                INSTANCE = it
                Log.d(TAG, "创建新的rPPG依赖项实例")
            }
        }
    }

    /**
     * 清理实例
     */
    fun clearInstance() {
        synchronized(this) {
            INSTANCE?.let { dependencies ->
                Log.d(TAG, "开始清理rPPG依赖项")
                dependencies.release()
                INSTANCE = null
                Log.d(TAG, "rPPG依赖项清理完成")
            }
        }
    }

    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean {
        return INSTANCE != null
    }

    /**
     * 创建依赖项实例
     */
    private fun createDependencies(context: Context): RppgDependencies {
        return RppgDependencies(context)
    }
}

/**
 * rPPG依赖项容器
 * 使用简单的nullable变量避免isInitialized问题
 */
class RppgDependencies(
    private val context: Context
) {
    companion object {
        private const val TAG = "RppgDependencies"
    }

    // 使用nullable变量，避免isInitialized的兼容性问题
    private var _videoRecorder: VideoRecorder? = null
    private var _rppgProcessor: EnhancedRppgProcessor? = null
    private var _repository: EnhancedRppgRepository? = null

    /**
     * 获取VideoRecorder实例，按需创建
     */
    val videoRecorder: VideoRecorder
        get() {
            if (_videoRecorder == null) {
                synchronized(this) {
                    if (_videoRecorder == null) {
                        Log.d(TAG, "初始化VideoRecorder")
                        _videoRecorder = VideoRecorder(context)
                    }
                }
            }
            return _videoRecorder!!
        }

    /**
     * 获取EnhancedRppgProcessor实例，按需创建
     */
    val rppgProcessor: EnhancedRppgProcessor
        get() {
            if (_rppgProcessor == null) {
                synchronized(this) {
                    if (_rppgProcessor == null) {
                        Log.d(TAG, "初始化EnhancedRppgProcessor")
                        _rppgProcessor = EnhancedRppgProcessor(context)
                    }
                }
            }
            return _rppgProcessor!!
        }

    /**
     * 获取EnhancedRppgRepository实例，按需创建
     */
    val repository: EnhancedRppgRepository
        get() {
            if (_repository == null) {
                synchronized(this) {
                    if (_repository == null) {
                        Log.d(TAG, "初始化EnhancedRppgRepository")
                        _repository = EnhancedRppgRepository(context)
                    }
                }
            }
            return _repository!!
        }

    /**
     * 释放所有资源
     */
    fun release() {
        try {
            // 简单检查null状态，避免isInitialized问题
            _videoRecorder?.let { recorder ->
                Log.d(TAG, "释放VideoRecorder")
                try {
                    recorder.release()
                } catch (e: Exception) {
                    Log.e(TAG, "释放VideoRecorder失败", e)
                }
                _videoRecorder = null
            }

            _rppgProcessor?.let { processor ->
                Log.d(TAG, "释放EnhancedRppgProcessor")
                try {
                    processor.release()
                } catch (e: Exception) {
                    Log.e(TAG, "释放EnhancedRppgProcessor失败", e)
                }
                _rppgProcessor = null
            }

            _repository?.let { repo ->
                Log.d(TAG, "repository已创建")
                // repository通常不需要显式释放，但设置为null
                _repository = null
            }

            Log.d(TAG, "所有依赖项释放完成")

        } catch (e: Exception) {
            Log.e(TAG, "释放依赖项时发生错误", e)
        }
    }

    /**
     * 获取初始化状态信息
     */
    fun getInitializationStatus(): Map<String, Boolean> {
        return mapOf(
            "videoRecorder" to (_videoRecorder != null),
            "rppgProcessor" to (_rppgProcessor != null),
            "repository" to (_repository != null)
        )
    }

    /**
     * 获取详细的初始化状态信息（用于调试）
     */
    fun getDetailedInitializationStatus(): String {
        val status = StringBuilder()
        status.append("RppgDependencies 初始化状态:\n")

        status.append("- VideoRecorder: ${if (_videoRecorder != null) "已初始化" else "未初始化"}\n")
        status.append("- RppgProcessor: ${if (_rppgProcessor != null) "已初始化" else "未初始化"}\n")
        status.append("- Repository: ${if (_repository != null) "已初始化" else "未初始化"}\n")

        return status.toString()
    }

    /**
     * 强制初始化所有依赖项（仅用于测试或预热）
     */
    fun preInitializeAll() {
        Log.d(TAG, "预初始化所有依赖项...")
        try {
            // 通过访问属性来触发初始化
            videoRecorder
            rppgProcessor
            repository
            Log.d(TAG, "所有依赖项预初始化完成")
        } catch (e: Exception) {
            Log.e(TAG, "预初始化失败", e)
        }
    }

    /**
     * 检查特定依赖项是否已初始化
     */
    fun isVideoRecorderInitialized(): Boolean = _videoRecorder != null
    fun isRppgProcessorInitialized(): Boolean = _rppgProcessor != null
    fun isRepositoryInitialized(): Boolean = _repository != null

    /**
     * 获取实例统计信息
     */
    fun getInstanceStats(): String {
        val initializedCount = listOf(_videoRecorder, _rppgProcessor, _repository).count { it != null }
        return "已初始化: $initializedCount/3 个依赖项"
    }
}