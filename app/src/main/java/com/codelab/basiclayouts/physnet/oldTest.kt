//package com.codelab.basiclayouts.physnet
//
//import android.os.Bundle
//import android.util.Log
//import androidx.appcompat.app.AppCompatActivity
//import kotlin.concurrent.thread
//
//class TestActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        thread {
//            try {
//                val inference = RppgInference(this)
//
//                // 使用扁平化数组代替嵌套结构,节省初始化内存
//                val flatInput = FloatArray(1 * 3 * 500 * 128 * 128) { (0.4f + Math.random() * 0.2).toFloat() }
//
//                val result = inference.runInference(flatInput)
//                Log.d("ONNX-RPPG", "预测结果:${result.joinToString()}")
//            } catch (e: Exception) {
//                Log.e("ONNX-RPPG", "推理错误", e)
//            }
//        }
//    }
//}