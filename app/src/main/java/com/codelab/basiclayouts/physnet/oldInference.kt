//package com.codelab.basiclayouts.physnet
//
//import ai.onnxruntime.OnnxTensor
//import ai.onnxruntime.OrtEnvironment
//import ai.onnxruntime.OrtSession
//import android.content.Context
//import android.util.Log
//import java.nio.FloatBuffer
//
//class RppgInference(context: Context) {
//    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
//    private val session: OrtSession
//
//    init {
//        // ✅ 加载 .ort 模型
//        val modelBytes = context.assets
//            .open("com/codelab/basiclayouts/physnet/physnet_rppg.ort")
//            .readBytes()
//
//        // ✅ 开启 GPU(若不支持会 fallback 到 CPU)
//        val opts = OrtSession.SessionOptions()
//        try {
//            opts.addNnapi() // ✅ 尝试使用 Android NNAPI 硬件加速(兼容性高)
//            Log.d("RppgInference", "Using NNAPI execution provider")
//        } catch (e: Exception) {
//            Log.w("RppgInference", "NNAPI not available: ${e.message}")
//        }
//        session = env.createSession(modelBytes, opts)
//        Log.d("RppgInference", "ORT model loaded successfully")
//    }
//
//    fun runInference(flatInput: FloatArray): FloatArray {
//        val shape = longArrayOf(1, 3, 500, 128, 128)  // 模型输入形状
//        val buffer = FloatBuffer.wrap(flatInput)
//        val inputTensor = OnnxTensor.createTensor(env, buffer, shape)
//
//        val results = session.run(mapOf("input" to inputTensor))
//        val output = results[0].value
//
//        // ✅ 兼容多种输出类型,防止强转异常
//        return when (output) {
//            is Array<*> -> {
//                when (val inner = output[0]) {
//                    is FloatArray -> inner // 正确结构
//                    is Array<*> -> {
//                        if (inner[0] is FloatArray) {
//                            (inner[0] as FloatArray) // 三维时处理
//                        } else {
//                            throw IllegalStateException("Unsupported nested output format")
//                        }
//                    }
//                    else -> throw IllegalStateException("Unknown inner output type")
//                }
//            }
//            is FloatArray -> output
//            else -> throw IllegalStateException("Unsupported output type: ${output?.javaClass}")
//        }
//    }
//}