package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Modes de transport supportés
 */
enum class TravelMode(
    val titleFr: String,
    val icon: ImageVector,
    val averageSpeedKmh: Float,
    val osrmProfile: String
) {
    DRIVING("Voiture", Icons.Default.DirectionsCar, 45f, "driving"),
    WALKING("À pied", Icons.Default.DirectionsWalk, 4.5f, "walking"),
    BICYCLE("Vélo", Icons.Default.DirectionsBike, 18f, "cycling")
}

/**
 * Coordonnée géographique sur le tracé
 */
data class RoutePoint(
    val latitude: Double,
    val longitude: Double
)

/**
 * Étape d'instruction de navigation
 */
data class RouteStep(
    val instruction: String,
    val distanceMeters: Float,
    val durationSeconds: Long,
    val maneuverType: String = "straight"
) {
    val formattedDistance: String
        get() = if (distanceMeters < 1000) "${distanceMeters.toInt()} m" else String.format(java.util.Locale.US, "%.1f km", distanceMeters / 1000f)
}

/**
 * Résultat complet d'itinéraire
 */
data class RouteResult(
    val origin: RoutePoint,
    val destination: RoutePoint,
    val destinationName: String,
    val totalDistanceMeters: Float,
    val totalDurationSeconds: Long,
    val polylinePoints: List<RoutePoint>,
    val steps: List<RouteStep>,
    val mode: TravelMode = TravelMode.DRIVING
) {
    /**
     * Distance formatée
     */
    val formattedDistance: String
        get() = if (totalDistanceMeters < 1000) {
            "${totalDistanceMeters.toInt()} m"
        } else {
            String.format(java.util.Locale.US, "%.1f km", totalDistanceMeters / 1000f)
        }

    /**
     * Durée estimée formatée (ex: "14 min", "1 h 25 min")
     */
    val formattedDuration: String
        get() {
            val minutes = (totalDurationSeconds / 60).coerceAtLeast(1)
            return if (minutes < 60) {
                "$minutes min"
            } else {
                val hours = minutes / 60
                val remMin = minutes % 60
                if (remMin > 0) "${hours} h ${remMin} min" else "${hours} h"
            }
        }
}
