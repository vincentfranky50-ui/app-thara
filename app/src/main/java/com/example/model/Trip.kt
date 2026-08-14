package com.example.model

data class Waypoint(
    val latitude: Double,
    val longitude: Double,
    val speedKmH: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class StopPoint(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val durationMinutes: Int,
    val arrivalTime: String,
    val reason: String
)

data class Trip(
    val id: String,
    val vehicleId: String,
    val vehicleName: String,
    val licensePlate: String,
    val dateLabel: String,
    val startTime: String,
    val endTime: String,
    val departureAddress: String,
    val arrivalAddress: String,
    val distanceKm: Double,
    val avgSpeedKmH: Float,
    val maxSpeedKmH: Float,
    val durationMinutes: Int,
    val fuelConsumedLiters: Double,
    val stopPoints: List<StopPoint>,
    val waypoints: List<Waypoint>
)
