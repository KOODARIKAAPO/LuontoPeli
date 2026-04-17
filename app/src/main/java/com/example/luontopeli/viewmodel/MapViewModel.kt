package com.example.luontopeli.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.location.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val natureSpotDao: NatureSpotDao,
    private val locationManager: LocationManager
) : ViewModel() {

    val routePoints: StateFlow<List<GeoPoint>> =
        locationManager.routePoints
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val currentLocation: StateFlow<Location?> =
        locationManager.currentLocation
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    private val _natureSpots = kotlinx.coroutines.flow.MutableStateFlow<List<NatureSpot>>(emptyList())
    val natureSpots: StateFlow<List<NatureSpot>> = _natureSpots.asStateFlow()

    init {
        loadNatureSpots()
    }

    fun startTracking() {
        locationManager.startTracking()
    }

    fun stopTracking() {
        locationManager.stopTracking()
    }

    fun resetRoute() {
        locationManager.resetRoute()
    }

    fun getTotalDistanceMeters(): Float {
        return locationManager.calculateTotalDistance()
    }

    private fun loadNatureSpots() {
        viewModelScope.launch {
            natureSpotDao.getSpotsWithLocation().collect { spots ->
                _natureSpots.value = spots
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationManager.stopTracking()
    }
}