package com.example.luontopeli.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.location.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    application: Application,
    private val natureSpotDao: NatureSpotDao
) : AndroidViewModel(application) {

    private val locationManager = LocationManager(application)

    val routePoints: StateFlow<List<GeoPoint>> = locationManager.routePoints
    val currentLocation: StateFlow<Location?> = locationManager.currentLocation

    private val _natureSpots = MutableStateFlow<List<NatureSpot>>(emptyList())
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
