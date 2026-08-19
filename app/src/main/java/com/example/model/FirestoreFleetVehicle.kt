package com.example.model

/**
 * Operational status of a fleet vehicle in real-time.
 */
enum class FleetOperationalStatus(
    val labelFr: String,
    val description: String
) {
    ACTIVE("Actif / En mouvement", "Véhicule en déplacement avec moteur allumé"),
    IDLE("Au Ralenti", "Moteur tournant à l'arrêt (Idling)"),
    LOW_FUEL("Carburant Critique", "Niveau de carburant sous le seuil d'alerte (≤ 25%)"),
    STOPPED("À l'arrêt", "Contact coupé, stationné"),
    OFFLINE("Hors ligne", "Pas de signal GPS récent")
}

/**
 * Data model for a fleet vehicle fetched from Firebase Firestore.
 */
data class FirestoreFleetVehicle(
    val id: String = "",
    val name: String = "",
    val licensePlate: String = "",
    val imei: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val speedKmH: Float = 0f,
    val fuelLevelPct: Int = 100,
    val batteryPct: Int = 100,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val heading: Float = 0f,
    val enterpriseId: String = "ENT-01",
    val enterpriseName: String = "Flotte Thara",
    val lastUpdateTimestamp: Long = System.currentTimeMillis(),
    val address: String = "Dakar, Sénégal",
    val ignitionOn: Boolean = false,
    val geofenceZoneId: String? = null,
    val odometryKm: Double = 12450.0,
    val engineTempC: Int = 88,
    val lockState: Boolean = true,
    val statusRaw: String = "STOPPED"
) {
    /**
     * Resolves the primary operational status based on telemetry & business logic.
     */
    val operationalStatus: FleetOperationalStatus
        get() = when {
            fuelLevelPct <= 25 -> FleetOperationalStatus.LOW_FUEL
            statusRaw.equals("IDLE", ignoreCase = true) || (ignitionOn && speedKmH < 2f && !statusRaw.equals("STOPPED", ignoreCase = true)) -> FleetOperationalStatus.IDLE
            statusRaw.equals("MOVING", ignoreCase = true) || statusRaw.equals("ACTIVE", ignoreCase = true) || speedKmH > 2f -> FleetOperationalStatus.ACTIVE
            statusRaw.equals("OFFLINE", ignoreCase = true) -> FleetOperationalStatus.OFFLINE
            else -> FleetOperationalStatus.STOPPED
        }

    /**
     * Estimated remaining range in kilometers based on fuel level and average consumption.
     */
    val estimatedRangeKm: Int
        get() = (fuelLevelPct * 6.5f).toInt()
}
