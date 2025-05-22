package com.codelab.basiclayouts.physnet

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import java.nio.FloatBuffer

class RppgInference(context: Context) {
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val session: OrtSession

    init {
        val modelBytes = context.assets.open("physnet_rppg.onnx").readBytes()
        session = env.createSession(modelBytes)
    }

    fun runInference(inputData: Array<Array<Array<Array<Array<Float>>>>>): FloatArray {
        val shape = longArrayOf(1, 3, 500, 128, 128)
        val flatInput = FloatArray(1 * 3 * 500 * 128 * 128)
        var idx = 0
        for (c in 0 until 3)
            for (t in 0 until 500)
                for (h in 0 until 128)
                    for (w in 0 until 128)
                        flatInput[idx++] = inputData[0][c][t][h][w]

        val buffer = FloatBuffer.wrap(flatInput)
        val tensor = OnnxTensor.createTensor(env, buffer, shape)

        val results = session.run(mapOf("input" to tensor))
        val output = results[0].value as Array<FloatArray>
        return output[0] // rPPG 预测结果
    }
}
