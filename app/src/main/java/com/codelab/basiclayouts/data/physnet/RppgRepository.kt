package com.codelab.basiclayouts.data.physnet

//import com.codelab.basiclayouts.data.physnet.model.RppgApiResponse
import android.content.Context
import android.util.Log
import com.codelab.basiclayouts.data.physnet.model.EnhancedRppgResult
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
 * 增强的rPPG数据仓库 - 支持HRV和SpO2数据
 * 保持与原版本的完全兼容性
 */
class EnhancedRppgRepository(private val context: Context) {

    companion object {
        private const val TAG = "EnhancedRppgRepository"
        private const val LOCAL_DATA_DIR = "rppg_results"
        private const val RESULTS_FILE = "results.json"
        private const val ENHANCED_RESULTS_FILE = "enhanced_results.json"
        private const val BASE_URL = "http://47.96.237.130:5000//api/rppg"
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

    private val enhancedResultsFile: File by lazy {
        File(dataDir, ENHANCED_RESULTS_FILE)
    }

    // ==================== 原有方法（保持兼容性）====================

    /**
     * 保存测量结果到本地（原版本兼容）
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
     * 加载所有本地结果（原版本兼容）
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
     * 获取最近的N条结果（原版本兼容）
     */
    suspend fun getRecentResults(count: Int): List<RppgResult> = withContext(Dispatchers.IO) {
        loadAllResults().takeLast(count).reversed()
    }

    /**
     * 获取特定会话的结果（原版本兼容）
     */
    suspend fun getResultBySessionId(sessionId: String): RppgResult? = withContext(Dispatchers.IO) {
        loadAllResults().find { it.sessionId == sessionId }
    }

    /**
     * 上传结果到服务器（原版本兼容）
     */
    suspend fun uploadResult(result: RppgResult): Boolean = withContext(Dispatchers.IO) {
        try {
//            // 准备请求数据
////            val requestData = result.toApiRequest()
//            val requestBody = json.encodeToString(requestData)
//                .toRequestBody("application/json".toMediaType())
//
//            // 构建请求
//            val request = Request.Builder()
//                .url("$BASE_URL/add")
//                .post(requestBody)
//                .addHeader("Content-Type", "application/json")
//                .build()
//
//            // 发送请求
//            val response = client.newCall(request).execute()
//
//            if (response.isSuccessful) {
//                val responseBody = response.body?.string()
//                if (responseBody != null) {
//                    val apiResponse = json.decodeFromString<RppgApiResponse>(responseBody)
//                    Log.d(TAG, "上传成功: ${apiResponse.msg}, sample_id=${apiResponse.sample_id}")
//
//                    // 更新本地记录的同步状态
//                    updateSyncStatus(result.sessionId, true)
//
//                    return@withContext true
//                }
//            } else {
//                Log.e(TAG, "上传失败: ${response.code} - ${response.message}")
//            }

            false

        } catch (e: Exception) {
            Log.e(TAG, "上传异常", e)
            false
        }
    }

    // ==================== 新增的增强方法 ====================

    /**
     * 保存增强的测量结果到本地
     */
    suspend fun saveEnhancedResult(result: EnhancedRppgResult): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. 保存为原始格式（保持兼容性）
            val basicResult = result.toRppgResult()
            saveResult(basicResult)

            // 2. 保存增强数据
            val enhancedResults = loadAllEnhancedResults().toMutableList()
            enhancedResults.add(result)

            // 保持最新的100条记录
            val trimmedResults = enhancedResults.takeLast(100)

            // 保存到增强结果文件
            enhancedResultsFile.writeText(json.encodeToString(trimmedResults))

            // 3. 保存额外的增强数据
            saveEnhancedData(result)

            Log.d(TAG, "增强结果已保存: sessionId=${result.sessionId}, " +
                    "HRV=${result.hrvResult?.isValid}, SpO2=${result.spo2Result?.isValid}")
            true

        } catch (e: Exception) {
            Log.e(TAG, "保存增强结果失败", e)
            false
        }
    }

    /**
     * 加载所有增强结果
     */
    suspend fun loadAllEnhancedResults(): List<EnhancedRppgResult> = withContext(Dispatchers.IO) {
        try {
            if (!enhancedResultsFile.exists()) {
                return@withContext emptyList()
            }

            val jsonString = enhancedResultsFile.readText()
            json.decodeFromString<List<EnhancedRppgResult>>(jsonString)

        } catch (e: Exception) {
            Log.e(TAG, "加载增强结果失败", e)
            emptyList()
        }
    }

    /**
     * 获取最近的N条增强结果
     */
    suspend fun getRecentEnhancedResults(count: Int): List<EnhancedRppgResult> = withContext(Dispatchers.IO) {
        loadAllEnhancedResults().takeLast(count).reversed()
    }

    /**
     * 获取特定会话的增强结果
     */
    suspend fun getEnhancedResultBySessionId(sessionId: String): EnhancedRppgResult? = withContext(Dispatchers.IO) {
        loadAllEnhancedResults().find { it.sessionId == sessionId }
    }

    /**
     * 上传增强结果到服务器
     */
    suspend fun uploadEnhancedResult(result: EnhancedRppgResult): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. 先上传基础数据（保持兼容性）
            val basicSuccess = uploadResult(result.toRppgResult())

            if (!basicSuccess) {
                Log.w(TAG, "基础数据上传失败")
                return@withContext false
            }

            // 2. 如果有增强数据，尝试上传增强数据
            if (result.hrvResult != null || result.spo2Result != null) {
                val enhancedSuccess = uploadEnhancedData(result)
                if (enhancedSuccess) {
                    Log.d(TAG, "增强数据上传成功")
                } else {
                    Log.w(TAG, "增强数据上传失败，但基础数据已上传")
                }
            }

            true

        } catch (e: Exception) {
            Log.e(TAG, "上传增强结果异常", e)
            false
        }
    }

    /**
     * 上传增强数据到服务器（扩展端点）
     */
    private suspend fun uploadEnhancedData(result: EnhancedRppgResult): Boolean {
        return try {
            // 准备增强数据
            val enhancedData = mapOf(
                "sessionId" to result.sessionId,
                "timestamp" to result.timestamp,
                "hrvData" to result.hrvResult?.let { hrv ->
                    mapOf(
                        "rmssd" to hrv.rmssd,
                        "pnn50" to hrv.pnn50,
                        "sdnn" to hrv.sdnn,
                        "meanRR" to hrv.meanRR,
                        "triangularIndex" to hrv.triangularIndex,
                        "stressIndex" to hrv.stressIndex,
                        "healthStatus" to hrv.getHealthStatus().name,
                        "stressLevel" to hrv.getStressLevel().name
                    )
                },
                "spo2Data" to result.spo2Result?.let { spo2 ->
                    mapOf(
                        "spo2" to spo2.spo2,
                        "confidence" to spo2.confidence,
                        "ratioOfRatios" to spo2.ratioOfRatios,
                        "healthStatus" to spo2.getHealthStatus().name
                    )
                },
                "signalQuality" to mapOf(
                    "snr" to result.signalQuality.snr,
                    "motionArtifact" to result.signalQuality.motionArtifact,
                    "illuminationQuality" to result.signalQuality.illuminationQuality,
                    "overallQuality" to result.signalQuality.overallQuality,
                    "qualityLevel" to result.signalQuality.getQualityLevel().name
                )
            )

            val requestBody = json.encodeToString(enhancedData)
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/enhanced")  // 新的增强数据端点
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful

        } catch (e: Exception) {
            Log.e(TAG, "上传增强数据失败", e)
            false
        }
    }

    /**
     * 保存增强数据的详细信息
     */
    private suspend fun saveEnhancedData(result: EnhancedRppgResult) {
        try {
            // 保存HRV详细数据
            result.hrvResult?.let { hrv ->
                val hrvFile = File(dataDir, "${result.sessionId}_hrv.json")
                hrvFile.writeText(json.encodeToString(hrv))
            }

            // 保存SpO2详细数据
            result.spo2Result?.let { spo2 ->
                val spo2File = File(dataDir, "${result.sessionId}_spo2.json")
                spo2File.writeText(json.encodeToString(spo2))
            }

            // 保存信号质量数据
            val qualityFile = File(dataDir, "${result.sessionId}_quality.json")
            qualityFile.writeText(json.encodeToString(result.signalQuality))

        } catch (e: Exception) {
            Log.e(TAG, "保存增强数据失败", e)
        }
    }

    /**
     * 加载HRV详细数据
     */
    suspend fun loadHrvData(sessionId: String): com.codelab.basiclayouts.data.physnet.model.HrvData? = withContext(Dispatchers.IO) {
        try {
            val hrvFile = File(dataDir, "${sessionId}_hrv.json")
            if (!hrvFile.exists()) return@withContext null

            json.decodeFromString<com.codelab.basiclayouts.data.physnet.model.HrvData>(hrvFile.readText())

        } catch (e: Exception) {
            Log.e(TAG, "加载HRV数据失败", e)
            null
        }
    }

    /**
     * 加载SpO2详细数据
     */
    suspend fun loadSpO2Data(sessionId: String): com.codelab.basiclayouts.data.physnet.model.SpO2Data? = withContext(Dispatchers.IO) {
        try {
            val spo2File = File(dataDir, "${sessionId}_spo2.json")
            if (!spo2File.exists()) return@withContext null

            json.decodeFromString<com.codelab.basiclayouts.data.physnet.model.SpO2Data>(spo2File.readText())

        } catch (e: Exception) {
            Log.e(TAG, "加载SpO2数据失败", e)
            null
        }
    }

    // ==================== 其他原有方法（保持不变）====================

    /**
     * 批量同步未上传的结果
     */
    suspend fun syncPendingResults() = withContext(Dispatchers.IO) {
        try {
            // 同步基础结果
//            val results = loadAllResults()
//            val pendingResults = results.filter { !it.isSynced }
//
//            Log.d(TAG, "待同步基础结果数: ${pendingResults.size}")
//
//            pendingResults.forEach { result ->
//                val success = uploadResult(result)
//                if (success) {
//                    Log.d(TAG, "基础结果同步成功: ${result.sessionId}")
//                } else {
//                    Log.w(TAG, "基础结果同步失败: ${result.sessionId}")
//                }
//                kotlinx.coroutines.delay(500)
//            }
//
//            // 同步增强结果
//            val enhancedResults = loadAllEnhancedResults()
//            val pendingEnhanced = enhancedResults.filter { !it.toRppgResult().isSynced }
//
//            Log.d(TAG, "待同步增强结果数: ${pendingEnhanced.size}")
//
//            pendingEnhanced.forEach { result ->
//                val success = uploadEnhancedResult(result)
//                if (success) {
//                    Log.d(TAG, "增强结果同步成功: ${result.sessionId}")
//                } else {
//                    Log.w(TAG, "增强结果同步失败: ${result.sessionId}")
//                }
//                kotlinx.coroutines.delay(500)
//            }

        } catch (e: Exception) {
            Log.e(TAG, "批量同步失败", e)
        }
    }

    /**
     * 更新同步状态
     */
    private suspend fun updateSyncStatus(sessionId: String, isSynced: Boolean) {
        try {
//            // 更新基础结果
//            val results = loadAllResults().map { result ->
//                if (result.sessionId == sessionId) {
//                    result.copy(isSynced = isSynced)
//                } else {
//                    result
//                }
//            }
//            resultsFile.writeText(json.encodeToString(results))
//
//            // 更新增强结果
//            val enhancedResults = loadAllEnhancedResults().map { result ->
//                if (result.sessionId == sessionId) {
//                    result.copy(
//                        heartRate = result.heartRate,
//                        rppgSignal = result.rppgSignal,
//                        frameCount = result.frameCount,
//                        processingTimeMs = result.processingTimeMs,
//                        confidence = result.confidence,
//                        sessionId = result.sessionId,
//                        timestamp = result.timestamp,
//                        hrvResult = result.hrvResult,
//                        spo2Result = result.spo2Result,
//                        signalQuality = result.signalQuality
//                    )
//                } else {
//                    result
//                }
//            }
//            enhancedResultsFile.writeText(json.encodeToString(enhancedResults))

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
     * 删除结果（包括增强数据）
     */
    suspend fun deleteResult(sessionId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 删除基础结果
            val results = loadAllResults().filter { it.sessionId != sessionId }
            resultsFile.writeText(json.encodeToString(results))

            // 删除增强结果
            val enhancedResults = loadAllEnhancedResults().filter { it.sessionId != sessionId }
            enhancedResultsFile.writeText(json.encodeToString(enhancedResults))

            // 删除所有相关文件
            listOf(
                "${sessionId}_signal.json",
                "${sessionId}_hrv.json",
                "${sessionId}_spo2.json",
                "${sessionId}_quality.json"
            ).forEach { filename ->
                val file = File(dataDir, filename)
                if (file.exists()) {
                    file.delete()
                }
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
     * 获取增强的存储统计信息
     */
    suspend fun getEnhancedStorageStats(): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val results = loadAllResults()
            val enhancedResults = loadAllEnhancedResults()
            val totalSize = dataDir.listFiles()?.sumOf { it.length() } ?: 0L

            mapOf(
                "totalResults" to results.size,
                "enhancedResults" to enhancedResults.size,
//                "syncedResults" to results.count { it.isSynced },
//                "pendingResults" to results.count { !it.isSynced },
                "resultsWithHRV" to enhancedResults.count { it.hrvResult?.isValid == true },
                "resultsWithSpO2" to enhancedResults.count { it.spo2Result?.isValid == true },
                "totalSizeBytes" to totalSize,
                "totalSizeMB" to String.format("%.2f", totalSize / 1024.0 / 1024.0)
            )

        } catch (e: Exception) {
            Log.e(TAG, "获取增强统计信息失败", e)
            emptyMap()
        }
    }

    /**
     * 原有的存储统计信息（保持兼容性）
     */
    suspend fun getStorageStats(): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val results = loadAllResults()
            val totalSize = dataDir.listFiles()?.sumOf { it.length() } ?: 0L

            mapOf(
                "totalResults" to results.size,
//                "syncedResults" to results.count { it.isSynced },
//                "pendingResults" to results.count { !it.isSynced },
                "totalSizeBytes" to totalSize,
                "totalSizeMB" to String.format("%.2f", totalSize / 1024.0 / 1024.0)
            )

        } catch (e: Exception) {
            Log.e(TAG, "获取统计信息失败", e)
            emptyMap()
        }
    }
}