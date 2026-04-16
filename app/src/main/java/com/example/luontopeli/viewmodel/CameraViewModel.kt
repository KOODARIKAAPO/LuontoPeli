package com.example.luontopeli.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.repository.NatureSpotRepository
import com.example.luontopeli.ml.ClassificationResult
import com.example.luontopeli.ml.PlantClassifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    application: Application,
    private val repository: NatureSpotRepository
) : AndroidViewModel(application) {

    private val _capturedImagePath = MutableStateFlow<String?>(null)
    val capturedImagePath: StateFlow<String?> = _capturedImagePath.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    var currentLatitude: Double = 0.0
    var currentLongitude: Double = 0.0

    private val classifier = PlantClassifier()

    private val _classificationResult = MutableStateFlow<ClassificationResult?>(null)
    val classificationResult: StateFlow<ClassificationResult?> = _classificationResult.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    fun updateNote(newNote: String) {
        _note.value = newNote
    }

    fun takePhoto(context: Context, imageCapture: ImageCapture) {
        _isLoading.value = true

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())

        val outputDir = File(context.filesDir, "nature_photos").also { it.mkdirs() }
        val outputFile = File(outputDir, "IMG_${timestamp}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    _capturedImagePath.value = outputFile.absolutePath
                    classifyImage(context, outputFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    _isLoading.value = false
                    _classificationResult.value =
                        ClassificationResult.Error("Camera error: ${exception.message}")
                }
            }
        )
    }

    fun classifyImage(context: Context, file: File) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    classifier.classify(Uri.fromFile(file), context)
                }
                _classificationResult.value = result
            } catch (e: Exception) {
                _classificationResult.value =
                    ClassificationResult.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCapturedImage() {
        _capturedImagePath.value = null
        _classificationResult.value = null
        _isLoading.value = false
    }

    fun saveCurrentSpot() {
        val imagePath = _capturedImagePath.value ?: return
        val result = _classificationResult.value as? ClassificationResult.Success ?: return

        viewModelScope.launch {
            val spot = NatureSpot(
                name = result.label,
                note = _note.value.takeIf { it.isNotBlank() },
                latitude = currentLatitude,
                longitude = currentLongitude,
                imageLocalPath = imagePath,
                plantLabel = result.label,
                confidence = result.confidence
            )

            repository.insertSpot(spot)
            clearCapturedImage()
            _note.value = ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        classifier.close()
    }
}