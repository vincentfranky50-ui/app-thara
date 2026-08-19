package com.example.model

import java.util.UUID

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class MessageStatus {
    SENDING,
    SENT,
    ERROR
}

enum class AiQueryCategory {
    IDLING_TRUCKS,
    FUEL_STATUS,
    SPEED_ALERT,
    GEOFENCE_STATUS,
    MAINTENANCE_HEALTH,
    GENERAL_FLEET_SUMMARY
}

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val referencedVehicles: List<VehicleSummaryCard> = emptyList(),
    val suggestedActions: List<String> = emptyList()
)

data class VehicleSummaryCard(
    val vehicleId: String,
    val name: String,
    val licensePlate: String,
    val status: VehicleStatus,
    val speedKmH: Float,
    val fuelLevelPct: Int,
    val driverName: String,
    val locationDescription: String
)
