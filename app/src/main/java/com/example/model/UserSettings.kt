package com.example.model

enum class SpeedUnit(val symbol: String, val labelFr: String, val speedFactor: Double) {
    KMH("km/h", "Kilomètres par heure (km/h)", 1.0),
    MPH("mph", "Miles par heure (mph)", 0.621371)
}

enum class DistanceUnit(val symbol: String, val labelFr: String, val distanceFactor: Double) {
    KM("km", "Kilomètres (km)", 1.0),
    MILES("mi", "Miles (mi)", 0.621371)
}

data class AlertChannelPreferences(
    val emailCriticalAlerts: Boolean = true,
    val emailWeeklyDigest: Boolean = true,
    val emailGeofenceBreach: Boolean = true,
    val emailMaintenanceDue: Boolean = false,
    val pushCriticalAlerts: Boolean = true,
    val pushGeofenceBreach: Boolean = true,
    val pushSpeedingAlerts: Boolean = true,
    val pushEngineIgnition: Boolean = false,
    val smsEmergencyOnly: Boolean = true
)

data class UserMeasurementPreferences(
    val speedUnit: SpeedUnit = SpeedUnit.KMH,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val fuelUnit: String = "L/100km",
    val temperatureUnit: String = "°C"
)

data class UserSettings(
    val alertPreferences: AlertChannelPreferences = AlertChannelPreferences(),
    val measurementPreferences: UserMeasurementPreferences = UserMeasurementPreferences(),
    val emailRecipient: String = "vincentfranky50@gmail.com",
    val phoneRecipient: String = "+221 77 123 45 67"
)
