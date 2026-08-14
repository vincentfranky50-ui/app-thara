package com.example.model

enum class VehicleStatus(val labelFr: String) {
    MOVING("En mouvement"),
    IDLE("Ralenti (Moteur ON)"),
    STOPPED("En arrêt"),
    OFFLINE("Hors ligne"),
    ALERT_GEOFENCE("Alerte Zone")
}

data class FuelLog(
    val day: String,
    val litersPer100Km: Float
)

data class Vehicle(
    val id: String,
    val name: String,
    val licensePlate: String,
    val imei: String,
    val driverName: String,
    val driverPhone: String,
    val status: VehicleStatus,
    val speedKmH: Float,
    val batteryPct: Int,
    val fuelLevelPct: Int,
    val latitude: Double,
    val longitude: Double,
    val heading: Float, // 0..360 degrees
    val enterpriseId: String,
    val enterpriseName: String,
    val lastUpdateTimestamp: Long,
    val address: String,
    val ignitionOn: Boolean,
    val geofenceZoneId: String? = null,
    val odometryKm: Double = 12450.0,
    val engineTempC: Int = 88,
    val lockState: Boolean = true,
    val fuelHistory: List<FuelLog> = listOf(
        FuelLog("Lun", 12.4f),
        FuelLog("Mar", 14.1f),
        FuelLog("Mer", 11.8f),
        FuelLog("Jeu", 13.5f),
        FuelLog("Ven", 12.0f),
        FuelLog("Sam", 10.2f),
        FuelLog("Dim", 9.5f)
    )
)
