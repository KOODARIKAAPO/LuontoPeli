package com.example.luontopeli.data.repository


import javax.inject.Inject
import com.example.luontopeli.data.remote.firebase.AuthManager
import com.example.luontopeli.data.remote.firebase.FirestoreManager
import com.example.luontopeli.data.remote.firebase.StorageManager
import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.entity.NatureSpot
import kotlinx.coroutines.flow.Flow

// 📁 data/repository/NatureSpotRepository.kt

class NatureSpotRepository(
    private val dao: NatureSpotDao,
    private val firestoreManager: FirestoreManager,
    private val storageManager: StorageManager,
    private val authManager: AuthManager
) {
    val allSpots: Flow<List<NatureSpot>> = dao.getAllSpots()

    suspend fun insertSpot(spot: NatureSpot) {
        val spotWithUser = spot.copy(userId = authManager.currentUserId)

        dao.insert(spotWithUser.copy(synced = false))

        syncSpotToFirebase(spotWithUser)
    }

    private suspend fun syncSpotToFirebase(spot: NatureSpot) {
        try {
            val firebaseImageUrl = spot.imageLocalPath?.let { localPath ->
                storageManager.uploadImage(localPath, spot.id).getOrNull()
            }

            val spotWithUrl = spot.copy(imageFirebaseUrl = firebaseImageUrl)
            firestoreManager.saveSpot(spotWithUrl).getOrThrow()

            dao.markSynced(spot.id, firebaseImageUrl ?: "")
        } catch (e: Exception) {
            // jätetään synced = false
        }
    }

    suspend fun syncPendingSpots() {
        val unsyncedSpots = dao.getUnsyncedSpots()
        unsyncedSpots.forEach { spot ->
            syncSpotToFirebase(spot)
        }
    }
}