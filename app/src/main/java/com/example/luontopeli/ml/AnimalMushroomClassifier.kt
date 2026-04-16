package com.example.luontopeli.ml

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AnimalMushroomClassifier {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
    )

    private val animalKeywords = setOf(
        "animal", "mammal", "bird", "dog", "cat", "fox", "wolf", "bear",
        "deer", "moose", "elk", "hare", "rabbit", "squirrel", "mouse",
        "rat", "horse", "cow", "sheep", "goat", "pig", "duck", "eagle",
        "owl", "insect", "butterfly", "spider", "bee", "fish", "frog",
        "reptile", "snake", "lizard"
    )

    private val mushroomKeywords = setOf(
        "mushroom", "fungus", "fungi", "toadstool", "mushrooms"
    )

    suspend fun classify(imageUri: Uri, context: Context): ClassificationResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)

                labeler.process(inputImage)
                    .addOnSuccessListener { labels ->
                        val animalLabels = labels.filterMatches(animalKeywords)
                        val mushroomLabels = labels.filterMatches(mushroomKeywords)

                        val result = when {
                            animalLabels.isNotEmpty() && mushroomLabels.isNotEmpty() -> {
                                val bestAnimal = animalLabels.maxByOrNull { it.confidence }
                                val bestMushroom = mushroomLabels.maxByOrNull { it.confidence }

                                if (bestAnimal != null && bestMushroom != null) {
                                    if (bestAnimal.confidence >= bestMushroom.confidence) {
                                        ClassificationResult.Success(
                                            category = NatureCategory.ANIMAL,
                                            label = bestAnimal.text,
                                            confidence = bestAnimal.confidence,
                                            allLabels = labels.take(5)
                                        )
                                    } else {
                                        ClassificationResult.Success(
                                            category = NatureCategory.MUSHROOM,
                                            label = bestMushroom.text,
                                            confidence = bestMushroom.confidence,
                                            allLabels = labels.take(5)
                                        )
                                    }
                                } else {
                                    ClassificationResult.NotNature(labels.take(3))
                                }
                            }

                            animalLabels.isNotEmpty() -> {
                                val best = animalLabels.maxByOrNull { it.confidence }!!
                                ClassificationResult.Success(
                                    category = NatureCategory.ANIMAL,
                                    label = best.text,
                                    confidence = best.confidence,
                                    allLabels = labels.take(5)
                                )
                            }

                            mushroomLabels.isNotEmpty() -> {
                                val best = mushroomLabels.maxByOrNull { it.confidence }!!
                                ClassificationResult.Success(
                                    category = NatureCategory.MUSHROOM,
                                    label = best.text,
                                    confidence = best.confidence,
                                    allLabels = labels.take(5)
                                )
                            }

                            else -> {
                                ClassificationResult.NotNature(
                                    allLabels = labels.take(3)
                                )
                            }
                        }

                        if (continuation.isActive) {
                            continuation.resume(result)
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) {
                            continuation.resume(
                                ClassificationResult.Error(
                                    exception.message ?: "Tuntematon virhe tunnistuksessa"
                                )
                            )
                        }
                    }

            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resume(
                        ClassificationResult.Error(
                            e.message ?: "Virhe kuvan käsittelyssä"
                        )
                    )
                }
            }
        }
    }

    fun close() {
        labeler.close()
    }

    private fun List<ImageLabel>.filterMatches(keywords: Set<String>): List<ImageLabel> {
        return this.filter { label ->
            keywords.any { keyword ->
                label.text.contains(keyword, ignoreCase = true)
            }
        }
    }
}