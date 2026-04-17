package com.example.luontopeli.data.repository

import android.util.Log
import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.remote.firebase.AuthManager
import com.example.luontopeli.data.remote.firebase.FirestoreManager
import com.example.luontopeli.data.remote.firebase.StorageManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NatureSpotRepository @Inject constructor(
    private val dao: NatureSpotDao,
    private val firestoreManager: FirestoreManager,
    private val storageManager: StorageManager,
    private val authManager: AuthManager
) {

    fun getAllSpots(): Flow<List<NatureSpot>> = dao.getAllSpots()

    fun getSpotsWithLocation(): Flow<List<NatureSpot>> = dao.getSpotsWithLocation()

    suspend fun insertSpot(spot: NatureSpot): Result<Unit> {
        return try {
            val userId = authManager.signInAnonymouslyIfNeeded()

            val spotWithUser = spot.copy(
                userId = userId,
                synced = false
            )

            Log.d(TAG, "insertSpot start id=${spotWithUser.id} user=${spotWithUser.userId}")

            dao.insert(spotWithUser)

            val syncResult = syncSpotToFirebase(spotWithUser)
            if (syncResult.isFailure) {
                Log.e(TAG, "sync failed id=${spotWithUser.id}", syncResult.exceptionOrNull())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "insertSpot failed", e)
            Result.failure(e)
        }
    }

    suspend fun retryUnsyncedSpots(): Result<Unit> {
        return try {
            authManager.signInAnonymouslyIfNeeded()

            val unsyncedSpots = dao.getUnsyncedSpots()
            unsyncedSpots.forEach { spot ->
                val result = syncSpotToFirebase(spot)
                if (result.isFailure) {
                    Log.e(TAG, "retry failed id=${spot.id}", result.exceptionOrNull())
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "retryUnsyncedSpots failed", e)
            Result.failure(e)
        }
    }

    private suspend fun syncSpotToFirebase(spot: NatureSpot): Result<Unit> {
        return try {
            Log.d(TAG, "sync start id=${spot.id} localPath=${spot.imageLocalPath}")

            val firebaseImageUrl = spot.imageLocalPath?.let { localPath ->
                val uploadResult = storageManager.uploadImage(localPath, spot.id)
                if (uploadResult.isFailure) {
                    throw uploadResult.exceptionOrNull() ?: Exception("Image upload failed")
                }
                uploadResult.getOrThrow()
            }

            val spotWithUrl = spot.copy(
                imageFirebaseUrl = firebaseImageUrl,
                synced = true
            )

            val saveResult = firestoreManager.saveSpot(spotWithUrl)
            if (saveResult.isFailure) {
                throw saveResult.exceptionOrNull() ?: Exception("Firestore save failed")
            }

            dao.markSynced(spot.id, firebaseImageUrl ?: "")

            Log.d(TAG, "sync success id=${spot.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "syncSpotToFirebase failed id=${spot.id}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteSpot(spot: NatureSpot): Result<Unit> {
        return try {
            dao.delete(spot)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteSpot failed id=${spot.id}", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "NatureSpotRepository"
    }
}