package com.example.data.repository

import com.example.model.StopPoint
import com.example.model.Trip
import com.example.model.Waypoint

object TripRepository {

    fun getTripsForVehicle(vehicleId: String): List<Trip> {
        val allTrips = getAllTrips()
        val vehicleTrips = allTrips.filter { it.vehicleId == vehicleId }
        return if (vehicleTrips.isNotEmpty()) vehicleTrips else getDefaultTripsForVehicle(vehicleId)
    }

    fun getAllTrips(): List<Trip> {
        return listOf(
            // --- VH-101: Toyota Hilux ---
            Trip(
                id = "TRIP-101-01",
                vehicleId = "VH-101",
                vehicleName = "Toyota Hilux 4x4 - Express 1",
                licensePlate = "DK-1048-A",
                dateLabel = "Aujourd'hui, 10 Août",
                startTime = "08:15",
                endTime = "10:30",
                departureAddress = "Avenue Cheikh Anta Diop, Dakar",
                arrivalAddress = "Pôle Urbain, Diamniadio",
                distanceKm = 48.5,
                avgSpeedKmH = 58.4f,
                maxSpeedKmH = 94.0f,
                durationMinutes = 135,
                fuelConsumedLiters = 5.2,
                stopPoints = listOf(
                    StopPoint(
                        id = "SP-101-1",
                        latitude = 14.7160,
                        longitude = -17.3800,
                        address = "Péage de Rufisque - Poste de Contrôle",
                        durationMinutes = 15,
                        arrivalTime = "08:50",
                        reason = "Contrôle Douanier & Péage"
                    ),
                    StopPoint(
                        id = "SP-101-2",
                        latitude = 14.7210,
                        longitude = -17.2500,
                        address = "Zone Industrielle de Bargny",
                        durationMinutes = 25,
                        arrivalTime = "09:30",
                        reason = "Déchargement Matériel BTP"
                    )
                ),
                waypoints = generateInterpolatedWaypoints(
                    listOf(
                        14.7167 to -17.4677, // Dakar
                        14.7100 to -17.4300,
                        14.7160 to -17.3800, // Stop 1
                        14.7210 to -17.2500, // Stop 2
                        14.7225 to -17.1812  // Diamniadio
                    ),
                    avgSpeed = 58.4f
                )
            ),
            Trip(
                id = "TRIP-101-02",
                vehicleId = "VH-101",
                vehicleName = "Toyota Hilux 4x4 - Express 1",
                licensePlate = "DK-1048-A",
                dateLabel = "Hier, 09 Août",
                startTime = "14:00",
                endTime = "16:20",
                departureAddress = "Port Autonome de Dakar",
                arrivalAddress = "Route de Mbour, Thiès",
                distanceKm = 72.3,
                avgSpeedKmH = 62.1f,
                maxSpeedKmH = 102.0f,
                durationMinutes = 140,
                fuelConsumedLiters = 7.8,
                stopPoints = listOf(
                    StopPoint(
                        id = "SP-101-3",
                        latitude = 14.7500,
                        longitude = -17.1000,
                        address = "Station Total KM 36 - Autoroute A1",
                        durationMinutes = 18,
                        arrivalTime = "14:50",
                        reason = "Ravitaillement Carburant"
                    )
                ),
                waypoints = generateInterpolatedWaypoints(
                    listOf(
                        14.6850 to -17.4300, // Port Dakar
                        14.7200 to -17.3000,
                        14.7500 to -17.1000, // Stop
                        14.7800 to -17.0000,
                        14.7903 to -16.9260  // Thiès
                    ),
                    avgSpeed = 62.1f
                )
            ),

            // --- VH-102: Peugeot Partner ---
            Trip(
                id = "TRIP-102-01",
                vehicleId = "VH-102",
                vehicleName = "Peugeot Partner - Livraisons Yoff",
                licensePlate = "DK-8920-TH",
                dateLabel = "Aujourd'hui, 10 Août",
                startTime = "09:00",
                endTime = "11:15",
                departureAddress = "Aéroport Léopold Sédar Senghor, Yoff",
                arrivalAddress = "Point E, Dakar",
                distanceKm = 24.6,
                avgSpeedKmH = 38.2f,
                maxSpeedKmH = 72.0f,
                durationMinutes = 135,
                fuelConsumedLiters = 2.4,
                stopPoints = listOf(
                    StopPoint(
                        id = "SP-102-1",
                        latitude = 14.7450,
                        longitude = -17.5100,
                        address = "Les Almadies - Zone Résidentielle",
                        durationMinutes = 20,
                        arrivalTime = "09:35",
                        reason = "Livraison Colis Express 1"
                    ),
                    StopPoint(
                        id = "SP-102-2",
                        latitude = 14.7100,
                        longitude = -17.4800,
                        address = "Avenue Cheikh Anta Diop, Fann",
                        durationMinutes = 15,
                        arrivalTime = "10:25",
                        reason = "Dépôt Documents"
                    )
                ),
                waypoints = generateInterpolatedWaypoints(
                    listOf(
                        14.7522 to -17.4720, // Yoff
                        14.7450 to -17.5100, // Almadies (Stop 1)
                        14.7200 to -17.4900,
                        14.7100 to -17.4800, // Fann (Stop 2)
                        14.6980 to -17.4620  // Point E
                    ),
                    avgSpeed = 38.2f
                )
            ),

            // --- VH-103: Volvo FH16 Truck ---
            Trip(
                id = "TRIP-103-01",
                vehicleId = "VH-103",
                vehicleName = "Volvo FH16 Truck - Camion Citerne",
                licensePlate = "SN-4512-B",
                dateLabel = "Aujourd'hui, 10 Août",
                startTime = "06:30",
                endTime = "11:00",
                departureAddress = "Dépôt Pétrolier, Port de Dakar",
                arrivalAddress = "Centrale Électrique de Malicounda, Mbour",
                distanceKm = 88.4,
                avgSpeedKmH = 51.5f,
                maxSpeedKmH = 82.0f,
                durationMinutes = 270,
                fuelConsumedLiters = 28.5,
                stopPoints = listOf(
                    StopPoint(
                        id = "SP-103-1",
                        latitude = 14.7161,
                        longitude = -17.2738,
                        address = "Zone Industrielle Rufisque",
                        durationMinutes = 30,
                        arrivalTime = "07:45",
                        reason = "Inspecteur Sécurité Citerne"
                    ),
                    StopPoint(
                        id = "SP-103-2",
                        latitude = 14.5500,
                        longitude = -17.0800,
                        address = "Sortie Sindia - N1",
                        durationMinutes = 20,
                        arrivalTime = "09:30",
                        reason = "Pause Chauffeur"
                    )
                ),
                waypoints = generateInterpolatedWaypoints(
                    listOf(
                        14.6850 to -17.4300, // Port Dakar
                        14.7161 to -17.2738, // Rufisque (Stop 1)
                        14.6500 to -17.1800,
                        14.5500 to -17.0800, // Sindia (Stop 2)
                        14.4220 to -16.9638  // Mbour
                    ),
                    avgSpeed = 51.5f
                )
            )
        )
    }

    private fun getDefaultTripsForVehicle(vehicleId: String): List<Trip> {
        return listOf(
            Trip(
                id = "TRIP-$vehicleId-DEF",
                vehicleId = vehicleId,
                vehicleName = "Véhicule Flotte $vehicleId",
                licensePlate = "SN-0000-X",
                dateLabel = "Aujourd'hui",
                startTime = "08:00",
                endTime = "10:30",
                departureAddress = "Dakar Centre",
                arrivalAddress = "Rufisque Nord",
                distanceKm = 32.0,
                avgSpeedKmH = 48.0f,
                maxSpeedKmH = 75.0f,
                durationMinutes = 150,
                fuelConsumedLiters = 3.5,
                stopPoints = listOf(
                    StopPoint(
                        id = "SP-$vehicleId-1",
                        latitude = 14.7200,
                        longitude = -17.3500,
                        address = "Point d'arrêt intermédiaire",
                        durationMinutes = 20,
                        arrivalTime = "09:10",
                        reason = "Arrêt Client"
                    )
                ),
                waypoints = generateInterpolatedWaypoints(
                    listOf(
                        14.7167 to -17.4677,
                        14.7200 to -17.3500,
                        14.7161 to -17.2738
                    ),
                    avgSpeed = 48.0f
                )
            )
        )
    }

    private fun generateInterpolatedWaypoints(
        keyPoints: List<Pair<Double, Double>>,
        avgSpeed: Float
    ): List<Waypoint> {
        val waypoints = mutableListOf<Waypoint>()
        val startTime = System.currentTimeMillis() - 3600000 * 2

        for (i in 0 until keyPoints.size - 1) {
            val start = keyPoints[i]
            val end = keyPoints[i + 1]
            val steps = 8

            for (step in 0..steps) {
                val fraction = step.toDouble() / steps
                val lat = start.first + (end.first - start.first) * fraction
                val lng = start.second + (end.second - start.second) * fraction
                val speed = avgSpeed + (-10..12).random()
                waypoints.add(
                    Waypoint(
                        latitude = lat,
                        longitude = lng,
                        speedKmH = speed.coerceIn(20f, 110f),
                        timestamp = startTime + (i * steps + step) * 60000
                    )
                )
            }
        }
        return waypoints
    }
}
