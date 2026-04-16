package com.example.luontopeli.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.dao.WalkSessionDao
import com.example.luontopeli.data.local.entity.WalkSession
import com.example.luontopeli.sensor.StepCounterManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WalkViewModel @Inject constructor(
    application: Application,
    private val walkSessionDao: WalkSessionDao
) : AndroidViewModel(application) {

    private val stepManager = StepCounterManager(application)

    private val _currentSession = MutableStateFlow<WalkSession?>(null)
    val currentSession: StateFlow<WalkSession?> = _currentSession.asStateFlow()

    private val _isWalking = MutableStateFlow(false)
    val isWalking: StateFlow<Boolean> = _isWalking.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private var timerJob: Job? = null

    fun startWalk() {
        if (_isWalking.value) return

        val start = System.currentTimeMillis()
        val session = WalkSession(startTime = start)

        _currentSession.value = session
        _isWalking.value = true
        _elapsedSeconds.value = 0L

        startTimer(start)

        stepManager.startStepCounting {
            _currentSession.update { current ->
                current?.copy(
                    stepCount = current.stepCount + 1,
                    distanceMeters = current.distanceMeters + StepCounterManager.STEP_LENGTH_METERS
                )
            }
        }
    }

    private fun startTimer(startTime: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isWalking.value) {
                _elapsedSeconds.value = (System.currentTimeMillis() - startTime) / 1000
                delay(1000)
            }
        }
    }

    fun stopWalk() {
        stepManager.stopStepCounting()
        _isWalking.value = false
        timerJob?.cancel()

        val endTime = System.currentTimeMillis()

        _currentSession.update { current ->
            current?.copy(
                endTime = endTime,
                isActive = false
            )
        }

        viewModelScope.launch {
            _currentSession.value?.let { session ->
                walkSessionDao.insert(session)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        stepManager.stopAll()
    }
}