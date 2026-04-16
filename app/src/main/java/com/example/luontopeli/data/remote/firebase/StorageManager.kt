package com.example.luontopeli.data.remote.firebase
import javax.inject.Inject
// 📁 data/remote/firebase/StorageManager.kt

/**
 * Offline-tilassa toimiva tallennushallinta (no-op -toteutus).
 * Korvaa alkuperäisen Firebase Storage -toteutuksen.
 */
class StorageManager @Inject constructor() {

    /**
     * Simuloi kuvan lataamista pilvipalveluun.
     * Offline-tilassa palauttaa paikallisen tiedostopolun.
     */
    suspend fun uploadImage(localFilePath: String, spotId: String): Result<String> {
        return Result.success(localFilePath)
    }

    /** Simuloi kuvan poistamista pilvipalvelusta. Offline-tilassa ei tee mitään. */
    suspend fun deleteImage(spotId: String): Result<Unit> {
        return Result.success(Unit)
    }
}