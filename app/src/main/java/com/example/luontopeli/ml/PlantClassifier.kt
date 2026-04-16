package com.example.luontopeli.ml

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PlantClassifier {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
    )

    private val plantKeywords = setOf(
        "plant", "flower", "tree", "shrub", "leaf", "fern", "moss",
        "grass", "herb", "bush", "berry", "pine", "birch", "spruce",
        "algae", "lichen", "bark", "botanical", "flora"
    )

    suspend fun classify(imageUri: Uri, context: Context): ClassificationResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)

                labeler.process(inputImage)
                    .addOnSuccessListener { labels ->
                        val plantLabels = labels.filter { label ->
                            plantKeywords.any { keyword ->
                                label.text.contains(keyword, ignoreCase = true)
                            }
                        }

                        val result = if (plantLabels.isNotEmpty()) {
                            val best = plantLabels.maxByOrNull { it.confidence }!!
                            ClassificationResult.Success(
                                category = NatureCategory.PLANT,
                                label = best.text,
                                confidence = best.confidence,
                                allLabels = labels.take(5)
                            )
                        } else {
                            ClassificationResult.NotNature(
                                allLabels = labels.take(3)
                            )
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
}