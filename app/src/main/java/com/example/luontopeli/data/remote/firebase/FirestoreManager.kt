package com.example.luontopeli.data.remote.firebase

import com.example.luontopeli.data.local.entity.NatureSpot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreManager @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val spotsCollection = db.collection("nature_spots")

    suspend fun saveSpot(spot: NatureSpot): Result<Unit> {
        return try {
            val data = mapOf(
                "id" to spot.id,
                "name" to spot.name,
                "note" to spot.note,
                "latitude" to spot.latitude,
                "longitude" to spot.longitude,
                "imageLocalPath" to spot.imageLocalPath,
                "imageFirebaseUrl" to spot.imageFirebaseUrl,
                "plantLabel" to spot.plantLabel,
                "confidence" to spot.confidence,
                "userId" to spot.userId,
                "timestamp" to spot.timestamp,
                "synced" to true
            )

            spotsCollection.document(spot.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserSpots(userId: String): Flow<List<NatureSpot>> = callbackFlow {
        val listener = spotsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val spots = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        NatureSpot(
                            id = doc.getString("id") ?: "",
                            name = doc.getString("name") ?: "",
                            note = doc.getString("note"),
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0,
                            imageLocalPath = doc.getString("imageLocalPath"),
                            imageFirebaseUrl = doc.getString("imageFirebaseUrl"),
                            plantLabel = doc.getString("plantLabel"),
                            confidence = doc.getDouble("confidence")?.toFloat(),
                            userId = doc.getString("userId"),
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            synced = true
                        )
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(spots)
            }

        awaitClose { listener.remove() }
    }
}