package com.example.luontopeli.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Map : Screen("map", "Kartta", Icons.Filled.Map)
    data object Camera : Screen("camera", "Kamera", Icons.Filled.CameraAlt)
    data object Discover : Screen("discover", "Löydöt", Icons.Filled.Explore)
    data object Stats : Screen("stats", "Tilastot", Icons.Filled.BarChart)

    data object Profile : Screen("profile", "Profiili", Icons.Filled.Person)

    companion object {
        val bottomNavScreens = listOf(Map, Camera, Discover, Stats, Profile)
    }
}