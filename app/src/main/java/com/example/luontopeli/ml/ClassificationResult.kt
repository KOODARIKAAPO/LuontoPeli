package com.example.luontopeli.ml

import com.google.mlkit.vision.label.ImageLabel

sealed class ClassificationResult {

    data class Success(
        val category: NatureCategory,
        val label: String,
        val confidence: Float,
        val allLabels: List<ImageLabel>
    ) : ClassificationResult()

    data class NotNature(
        val allLabels: List<ImageLabel>
    ) : ClassificationResult()

    data class Error(
        val message: String
    ) : ClassificationResult()
}

enum class NatureCategory {
    ANIMAL,
    MUSHROOM,
    PLANT
}