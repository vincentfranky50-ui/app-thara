package com.example.model

/**
 * Représente les données de géolocalisation en temps réel de l'utilisateur.
 */
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double = 0.0,
    val accuracyMeters: Float = 5f,
    val bearingDegrees: Float = 0f,
    val speedMps: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val isGpsActive: Boolean = true
) {
    /**
     * Vitesse convertie en kilomètres par heure (km/h)
     */
    val speedKmH: Float
        get() = (speedMps * 3.6f).coerceAtLeast(0f)

    /**
     * Niveau de précision GPS pour l'affichage utilisateur
     */
    val accuracyQuality: AccuracyQuality
        get() = when {
            accuracyMeters <= 8f -> AccuracyQuality.EXCELLENT
            accuracyMeters <= 20f -> AccuracyQuality.GOOD
            accuracyMeters <= 50f -> AccuracyQuality.MODERATE
            else -> AccuracyQuality.POOR
        }
}

enum class AccuracyQuality(val labelFr: String) {
    EXCELLENT("Précision haute (< 8m)"),
    GOOD("Précision bonne (< 20m)"),
    MODERATE("Précision moyenne (< 50m)"),
    POOR("Précision faible (> 50m)")
}
