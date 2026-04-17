package com.example.luontopeli.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class StorageManager @Inject constructor() {

    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadImage(localFilePath: String, spotId: String): Result<String> {
        return try {
            val file = File(localFilePath)
            if (!file.exists()) {
                return Result.failure(Exception("File does not exist"))
            }

            val uri = Uri.fromFile(file)
            val ref = storage.reference.child("nature_spots/$spotId.jpg")

            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteImage(spotId: String): Result<Unit> {
        return try {
            storage.reference.child("nature_spots/$spotId.jpg").delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}