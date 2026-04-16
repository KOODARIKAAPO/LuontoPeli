package com.example.luontopeli.viewmodel

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun formatDistance(meters: Float): String {
    return if (meters >= 1000f) {
        "%.2f km".format(Locale.getDefault(), meters / 1000f)
    } else {
        "${meters.roundToInt()} m"
    }
}

fun formatDuration(startTime: Long, endTime: Long): String {
    val durationMs = endTime - startTime
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes} min ${seconds} s"
}
