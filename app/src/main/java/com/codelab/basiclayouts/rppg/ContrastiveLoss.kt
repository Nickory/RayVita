package com.codelab.basiclayouts.rppg

import kotlin.math.exp
import kotlin.math.sqrt
import kotlin.math.ln
class ContrastiveLoss(private val temperature: Float = 0.07f) {
    fun compute(
        anchor: FloatArray,
        positive: FloatArray,
        negatives: List<FloatArray>
    ): Float {
        val posSim = cosineSimilarity(anchor, positive)
        val negSims = negatives.map { cosineSimilarity(anchor, it) }

        val numerator = exp(posSim / temperature).toFloat()
        val denominator = numerator + negSims.sumOf { exp(it / temperature).toDouble() }.toFloat() // 修正

        return -ln(numerator / denominator)
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "Vectors must have same length" }
        val dot = a.zip(b).sumOf { (x, y) -> (x * y).toDouble() }.toFloat()
        val normA = sqrt(a.map { it * it }.sum())
        val normB = sqrt(b.map { it * it }.sum())
        return dot / (normA * normB)
    }
}