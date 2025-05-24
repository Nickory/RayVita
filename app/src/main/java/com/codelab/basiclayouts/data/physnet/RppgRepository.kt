package com.codelab.basiclayouts.data.physnet

import android.content.Context
import android.util.Log
import com.codelab.basiclayouts.data.physnet.model.RppgApiResponse
import com.codelab.basiclayouts.data.physnet.model.RppgResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * rPPG数据仓库 - 负责本地存储和远程同步
 */
class RppgRepository(private val context: Context) {

    companion object {
        private const val TAG = "RppgRepository"
        private const val LOCAL_DATA_DIR = "rppg_results"
        private const val RESULTS_FILE = "results.json"
        private const val BASE_URL = "http://47.96.237.130:5000//api/rppg"  // TODO: 替换为实际后端地址
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 30L
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .build()

    private val dataDir: File by lazy {
        File(context.filesDir, LOCAL_DATA_DIR).apply {
            if (!exists()) mkdirs()
        }
    }

    private val resultsFile: File by lazy {
        File(dataDir, RESULTS_FILE)
    }

    /**
     * 保存测量结果到本地
     */
    suspend fun saveResult(result: RppgResult): Boolean = withContext(Dispatchers.IO) {
        try {
            // 读取现有结果
            val results = loadAllResults().toMutableList()

            // 添加新结果
            results.add(result)

            // 保持最新的100条记录
            val trimmedResults = results.takeLast(100)

            // 保存到文件
            resultsFile.writeText(json.encodeToString(trimmedResults))

            // 保存原始信号数据（可选）
            saveRawSignal(result)

            Log.d(TAG, "结果已保存: sessionId=${result.sessionId}")
            true

        } catch (e: Exception) {
            Log.e(TAG, "保存结果失败", e)
            false
        }
    }

    /**
     * 加载所有本地结果
     */
    suspend fun loadAllResults(): List<RppgResult> = withContext(Dispatchers.IO) {
        try {
            if (!resultsFile.exists()) {
                return@withContext emptyList()
            }

            val jsonString = resultsFile.readText()
            json.decodeFromString<List<RppgResult>>(jsonString)

        } catch (e: Exception) {
            Log.e(TAG, "加载结果失败", e)
            emptyList()
        }
    }

    /**
     * 获取最近的N条结果
     */
    suspend fun getRecentResults(count: Int): List<RppgResult> = withContext(Dispatchers.IO) {
        loadAllResults().takeLast(count).reversed()
    }

    /**
     * 获取特定会话的结果
     */
    suspend fun getResultBySessionId(sessionId: String): RppgResult? = withContext(Dispatchers.IO) {
        loadAllResults().find { it.sessionId == sessionId }
    }

    /**
     * 上传结果到服务器
     */
    suspend fun uploadResult(result: RppgResult): Boolean = withContext(Dispatchers.IO) {
        try {
            // 准备请求数据
            val requestData = result.toApiRequest()
            val requestBody = json.encodeToString(requestData)
                .toRequestBody("application/json".toMediaType())

            // 构建请求
            val request = Request.Builder()
                .url("$BASE_URL/add")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            // 发送请求
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val apiResponse = json.decodeFromString<RppgApiResponse>(responseBody)
                    Log.d(TAG, "上传成功: ${apiResponse.msg}, sample_id=${apiResponse.sample_id}")

                    // 更新本地记录的同步状态
                    updateSyncStatus(result.sessionId, true)

                    return@withContext true
                }
            } else {
                Log.e(TAG, "上传失败: ${response.code} - ${response.message}")
            }

            false

        } catch (e: Exception) {
            Log.e(TAG, "上传异常", e)
            false
        }
    }

    /**
     * 批量同步未上传的结果
     */
    suspend fun syncPendingResults() = withContext(Dispatchers.IO) {
        try {
            val results = loadAllResults()
            val pendingResults = results.filter { !it.isSynced }

            Log.d(TAG, "待同步结果数: ${pendingResults.size}")

            pendingResults.forEach { result ->
                val success = uploadResult(result)
                if (success) {
                    Log.d(TAG, "同步成功: ${result.sessionId}")
                } else {
                    Log.w(TAG, "同步失败: ${result.sessionId}")
                }

                // 避免请求过快
                kotlinx.coroutines.delay(500)
            }

        } catch (e: Exception) {
            Log.e(TAG, "批量同步失败", e)
        }
    }

    /**
     * 更新同步状态
     */
    private suspend fun updateSyncStatus(sessionId: String, isSynced: Boolean) {
        try {
            val results = loadAllResults().map { result ->
                if (result.sessionId == sessionId) {
                    result.copy(isSynced = isSynced)
                } else {
                    result
                }
            }

            resultsFile.writeText(json.encodeToString(results))

        } catch (e: Exception) {
            Log.e(TAG, "更新同步状态失败", e)
        }
    }

    /**
     * 保存原始信号数据
     */
    private suspend fun saveRawSignal(result: RppgResult) {
        try {
            val signalFile = File(dataDir, "${result.sessionId}_signal.json")
            signalFile.writeText(json.encodeToString(result.rppgSignal))

        } catch (e: Exception) {
            Log.e(TAG, "保存原始信号失败", e)
        }
    }

    /**
     * 加载原始信号数据
     */
    suspend fun loadRawSignal(sessionId: String): List<Float>? = withContext(Dispatchers.IO) {
        try {
            val signalFile = File(dataDir, "${sessionId}_signal.json")
            if (!signalFile.exists()) return@withContext null

            json.decodeFromString<List<Float>>(signalFile.readText())

        } catch (e: Exception) {
            Log.e(TAG, "加载原始信号失败", e)
            null
        }
    }

    /**
     * 删除结果
     */
    suspend fun deleteResult(sessionId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val results = loadAllResults().filter { it.sessionId != sessionId }
            resultsFile.writeText(json.encodeToString(results))

            // 删除原始信号文件
            val signalFile = File(dataDir, "${sessionId}_signal.json")
            if (signalFile.exists()) {
                signalFile.delete()
            }

            true

        } catch (e: Exception) {
            Log.e(TAG, "删除结果失败", e)
            false
        }
    }

    /**
     * 清除所有数据
     */
    suspend fun clearAllData(): Boolean = withContext(Dispatchers.IO) {
        try {
            dataDir.listFiles()?.forEach { file ->
                file.delete()
            }
            true

        } catch (e: Exception) {
            Log.e(TAG, "清除数据失败", e)
            false
        }
    }

    /**
     * 获取存储统计信息
     */
    suspend fun getStorageStats(): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val results = loadAllResults()
            val totalSize = dataDir.listFiles()?.sumOf { it.length() } ?: 0L

            mapOf(
                "totalResults" to results.size,
                "syncedResults" to results.count { it.isSynced },
                "pendingResults" to results.count { !it.isSynced },
                "totalSizeBytes" to totalSize,
                "totalSizeMB" to String.format("%.2f", totalSize / 1024.0 / 1024.0)
            )

        } catch (e: Exception) {
            Log.e(TAG, "获取统计信息失败", e)
            emptyMap()
        }
    }
}