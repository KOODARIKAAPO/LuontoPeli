package com.example.luontopeli.ml

import android.content.Context
import android.net.Uri

class NatureClassifier(
    private val plantClassifier: PlantClassifier = PlantClassifier(),
    private val animalMushroomClassifier: AnimalMushroomClassifier = AnimalMushroomClassifier()
) {

    suspend fun classify(imageUri: Uri, context: Context): ClassificationResult {
        val plantResult = plantClassifier.classify(imageUri, context)
        val animalMushroomResult = animalMushroomClassifier.classify(imageUri, context)

        val candidates = listOfNotNull(
            plantResult as? ClassificationResult.Success,
            animalMushroomResult as? ClassificationResult.Success
        )

        if (candidates.isNotEmpty()) {
            return candidates.maxByOrNull { it.confidence }!!
        }

        val error = when {
            plantResult is ClassificationResult.Error -> plantResult
            animalMushroomResult is ClassificationResult.Error -> animalMushroomResult
            else -> null
        }

        if (error != null) {
            return error
        }

        val fallbackLabels = when {
            plantResult is ClassificationResult.NotNature && plantResult.allLabels.isNotEmpty() ->
                plantResult.allLabels

            animalMushroomResult is ClassificationResult.NotNature && animalMushroomResult.allLabels.isNotEmpty() ->
                animalMushroomResult.allLabels

            else -> emptyList()
        }

        return ClassificationResult.NotNature(fallbackLabels)
    }

    fun close() {
        plantClassifier.close()
        animalMushroomClassifier.close()
    }
}