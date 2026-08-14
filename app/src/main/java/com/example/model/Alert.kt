package com.example.model

enum class AlertType(val labelFr: String) {
    SPEEDING("Excès de vitesse"),
    GEOFENCE_EXIT("Sortie de zone"),
    GEOFENCE_ENTRY("Entrée zone restreinte"),
    LOW_BATTERY("Batterie GPS faible"),
    RAPID_ACCELERATION("Freinage / Accélération brusque"),
    DISCONNECTED("Déconnexion boîtier IMEI")
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class Alert(
    val id: String,
    val vehicleId: String,
    val vehicleName: String,
    val licensePlate: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val message: String,
    val timestamp: Long,
    val acknowledged: Boolean = false
)
