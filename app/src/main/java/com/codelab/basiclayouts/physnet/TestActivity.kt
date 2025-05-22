package com.codelab.basiclayouts.physnet

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class TestActivity : AppCompatActivity() {
   
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inference = RppgInference(this)

        // 正确的嵌套结构 Array<Array<...>>
        val input = Array(1) {
            Array(3) {
                Array(500) {
                    Array(128) {
                        Array(128) { 0.5f }
                    }
                }
            }
        }

        Thread {
            val result = inference.runInference(input)
            Log.d("ONNX-RPPG", "预测结果：${result.joinToString()}")
        }.start()
    }
}
