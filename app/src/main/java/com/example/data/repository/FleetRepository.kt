package com.example.data.repository

import com.example.data.db.AlertEntity
import com.example.data.db.FleetDao
import com.example.data.db.GeofenceEntity
import com.example.data.db.VehicleEntity
import com.example.model.Alert
import com.example.model.AlertSeverity
import com.example.model.AlertType
import com.example.model.GeofenceZone
import com.example.model.Trip
import com.example.model.Vehicle
import com.example.model.VehicleStatus
import com.example.model.ZoneType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class FleetRepository(
    private val fleetDao: FleetDao,
    private val externalScope: CoroutineScope,
    private val context: android.content.Context? = null
) {
    val firestoreRepository = FirestoreFleetRepository(fleetDao, externalScope, context)
    private val activeVehicleZonesMap = java.util.concurrent.ConcurrentHashMap<String, MutableSet<String>>()

    val vehiclesFlow: Flow<List<Vehicle>> = fleetDao.getAllVehicles().map { list ->
        list.map { it.toModel() }
    }

    val alertsFlow: Flow<List<Alert>> = fleetDao.getAllAlerts().map { list ->
        list.map { it.toModel() }
    }

    val geofencesFlow: Flow<List<GeofenceZone>> = fleetDao.getAllGeofences().map { list ->
        list.map { it.toModel() }
    }

    val tripsFlow: Flow<List<Trip>> = firestoreRepository.tripsFlow.map { firestoreTrips ->
        if (firestoreTrips.isNotEmpty()) firestoreTrips else TripRepository.getAllTrips()
    }

    init {
        externalScope.launch(Dispatchers.IO) {
            val existing = fleetDao.getAllVehicles().first()
            if (existing.isEmpty()) {
                seedInitialData()
            }
            startRealtimeSimulationLoop()
        }
    }

    private suspend fun seedInitialData() {
        val initialVehicles = listOf(
            Vehicle(
                id = "VH-101",
                name = "Toyota Hilux 4x4 - Express 1",
                licensePlate = "DK-1048-A",
                imei = "864920184019201",
                driverName = "Mamadou Diallo",
                driverPhone = "+221 77 638 90 12",
                status = VehicleStatus.MOVING,
                speedKmH = 74.5f,
                batteryPct = 96,
                fuelLevelPct = 82,
                latitude = 14.7167, // Dakar
                longitude = -17.4677,
                heading = 45f,
                enterpriseId = "ENT-01",
                enterpriseName = "Logistique Dakar Express",
                lastUpdateTimestamp = System.currentTimeMillis(),
                address = "Avenue Cheikh Anta Diop, Dakar",
                ignitionOn = true,
                geofenceZoneId = "ZONE-PORT",
                odometryKm = 24890.0,
                engineTempC = 86,
                lockState = true
            ),
            Vehicle(
                id = "VH-102",
                name = "Peugeot Partner - Livraisons Yoff",
                licensePlate = "DK-8920-TH",
                imei = "864920184019202",
                driverName = "Awa Seck",
                driverPhone = "+221 78 112 34 56",
                status = VehicleStatus.MOVING,
                speedKmH = 48.0f,
                batteryPct = 88,
                fuelLevelPct = 64,
                latitude = 14.7522, // Yoff
                longitude = -17.4720,
                heading = 120f,
                enterpriseId = "ENT-01",
                enterpriseName = "Logistique Dakar Express",
                lastUpdateTimestamp = System.currentTimeMillis() - 120000,
                address = "Route de l'Aéroport, Yoff",
                ignitionOn = true,
                geofenceZoneId = "ZONE-AIBD",
                odometryKm = 18420.0,
                engineTempC = 90,
                lockState = true
            ),
            Vehicle(
                id = "VH-103",
                name = "Volvo FH16 Truck - Camion Citerne",
                licensePlate = "SN-4512-B",
                imei = "864920184019203",
                driverName = "Ousmane Sow",
                driverPhone = "+221 70 889 01 23",
                status = VehicleStatus.IDLE,
                speedKmH = 0.0f,
                batteryPct = 72,
                fuelLevelPct = 45,
                latitude = 14.6850, // Port de Dakar
                longitude = -17.4300,
                heading = 180f,
                enterpriseId = "ENT-02",
                enterpriseName = "Sénégal Fret & Transport",
                lastUpdateTimestamp = System.currentTimeMillis() - 45000,
                address = "Zone Portuaire Mole 2, Dakar",
                ignitionOn = true,
                geofenceZoneId = "ZONE-PORT",
                odometryKm = 142050.0,
                engineTempC = 92,
                lockState = true
            ),
            Vehicle(
                id = "VH-104",
                name = "Renault Master - Navette Rufisque",
                licensePlate = "DK-3021-AC",
                imei = "864920184019204",
                driverName = "Fatou Ndiaye",
                driverPhone = "+221 76 543 21 09",
                status = VehicleStatus.STOPPED,
                speedKmH = 0.0f,
                batteryPct = 100,
                fuelLevelPct = 91,
                latitude = 14.7150, // Rufisque
                longitude = -17.2720,
                heading = 270f,
                enterpriseId = "ENT-02",
                enterpriseName = "Sénégal Fret & Transport",
                lastUpdateTimestamp = System.currentTimeMillis() - 600000,
                address = "Gare Routière, Rufisque",
                ignitionOn = false,
                geofenceZoneId = null,
                odometryKm = 63100.0,
                engineTempC = 35,
                lockState = true
            ),
            Vehicle(
                id = "VH-105",
                name = "Mercedes Sprinter - VIP Shuttle",
                licensePlate = "DK-7701-VIP",
                imei = "864920184019205",
                driverName = "Abdoulaye Kane",
                driverPhone = "+221 77 999 88 77",
                status = VehicleStatus.MOVING,
                speedKmH = 105.0f, // Speeding alert!
                batteryPct = 68,
                fuelLevelPct = 52,
                latitude = 14.7300, // Autoroute A1
                longitude = -17.3500,
                heading = 90f,
                enterpriseId = "ENT-01",
                enterpriseName = "Logistique Dakar Express",
                lastUpdateTimestamp = System.currentTimeMillis(),
                address = "Autoroute de l'Avenir A1, km 18",
                ignitionOn = true,
                geofenceZoneId = null,
                odometryKm = 42800.0,
                engineTempC = 89,
                lockState = true
            )
        )

        fleetDao.insertOrUpdateVehicles(initialVehicles.map { VehicleEntity.fromModel(it) })

        val initialGeofences = listOf(
            GeofenceZone(
                id = "ZONE-PORT",
                name = "Zone Portuaire de Dakar",
                centerLat = 14.6850,
                centerLng = -17.4300,
                radiusMeters = 1500f,
                type = ZoneType.RESTRICTED,
                enterpriseId = "ENT-02",
                activeVehicleCount = 2
            ),
            GeofenceZone(
                id = "ZONE-AIBD",
                name = "Aéroport International AIBD",
                centerLat = 14.6700,
                centerLng = -17.0733,
                radiusMeters = 3000f,
                type = ZoneType.SAFE,
                enterpriseId = "ENT-01",
                activeVehicleCount = 1
            ),
            GeofenceZone(
                id = "ZONE-DEPOT",
                name = "Dépôt Central Diamniadio",
                centerLat = 14.7200,
                centerLng = -17.1800,
                radiusMeters = 2000f,
                type = ZoneType.SAFE,
                enterpriseId = "ENT-01",
                activeVehicleCount = 0
            )
        )

        fleetDao.insertGeofences(initialGeofences.map { GeofenceEntity.fromModel(it) })

        // Initialisation propre : Zéro alerte de test par défaut
        fleetDao.deleteAllAlerts()
    }

    private suspend fun startRealtimeSimulationLoop() {
        while (externalScope.isActive) {
            delay(3000) // update GPS coordinates every 3 seconds
            try {
                val currentList = fleetDao.getAllVehicles().first().map { it.toModel() }
                val currentGeofences = fleetDao.getAllGeofences().first().map { it.toModel() }

                if (currentList.isNotEmpty()) {
                    val updated = currentList.map { vehicle ->
                        val movedVehicle = if (vehicle.status == VehicleStatus.MOVING) {
                            // Move vehicle along its heading
                            val rad = Math.toRadians(vehicle.heading.toDouble())
                            val dLat = (cos(rad) * 0.0003) * (vehicle.speedKmH / 60.0)
                            val dLng = (sin(rad) * 0.0003) * (vehicle.speedKmH / 60.0)

                            val newLat = vehicle.latitude + dLat
                            val newLng = vehicle.longitude + dLng

                            // Slight speed variations
                            val speedDelta = Random.nextFloat() * 4f - 2f
                            val newSpeed = (vehicle.speedKmH + speedDelta).coerceIn(30f, 110f)

                            // Check speeding alert
                            if (newSpeed > 95f && vehicle.speedKmH <= 95f) {
                                val newAlert = Alert(
                                    id = "ALT-${System.currentTimeMillis() % 10000}",
                                    vehicleId = vehicle.id,
                                    vehicleName = vehicle.name,
                                    licensePlate = vehicle.licensePlate,
                                    type = AlertType.SPEEDING,
                                    severity = AlertSeverity.CRITICAL,
                                    message = "Vitesse excessive : ${newSpeed.toInt()} km/h enregistrée !",
                                    timestamp = System.currentTimeMillis(),
                                    acknowledged = false
                                )
                                fleetDao.insertAlert(AlertEntity.fromModel(newAlert))
                            }

                            vehicle.copy(
                                latitude = newLat,
                                longitude = newLng,
                                speedKmH = newSpeed,
                                lastUpdateTimestamp = System.currentTimeMillis(),
                                odometryKm = vehicle.odometryKm + (newSpeed * 0.0008)
                            )
                        } else vehicle

                        // Evaluate Geofences (Entry / Exit) for moving or idle vehicles
                        evaluateGeofencesForVehicle(movedVehicle, currentGeofences)
                    }
                    fleetDao.insertOrUpdateVehicles(updated.map { VehicleEntity.fromModel(it) })
                }
            } catch (e: Exception) {
                // Ignore transient loop errors
            }
        }
    }

    private suspend fun evaluateGeofencesForVehicle(
        vehicle: Vehicle,
        geofences: List<GeofenceZone>
    ): Vehicle {
        if (geofences.isEmpty()) return vehicle

        val vehicleId = vehicle.id
        val currentlyInZones = activeVehicleZonesMap.getOrPut(vehicleId) {
            if (vehicle.geofenceZoneId != null) mutableSetOf(vehicle.geofenceZoneId) else mutableSetOf()
        }

        var currentActiveZoneId: String? = vehicle.geofenceZoneId

        geofences.forEach { zone ->
            val isInside = isVehicleInGeofence(vehicle.latitude, vehicle.longitude, zone)
            val wasInside = currentlyInZones.contains(zone.id)

            if (isInside && !wasInside) {
                // ENTRY EVENT
                currentlyInZones.add(zone.id)
                currentActiveZoneId = zone.id

                if (zone.notifyOnEntry) {
                    val severity = if (zone.type == ZoneType.NO_GO) AlertSeverity.CRITICAL else AlertSeverity.WARNING
                    val title = "🚨 ALERTE ENTRÉE GÉOFENCE"
                    val msg = "Le véhicule ${vehicle.name} (${vehicle.licensePlate}) est entré dans la zone ${zone.name} (${zone.type.labelFr})."

                    val alert = Alert(
                        id = "ALT-GEO-IN-${System.currentTimeMillis() % 10000}",
                        vehicleId = vehicle.id,
                        vehicleName = vehicle.name,
                        licensePlate = vehicle.licensePlate,
                        type = AlertType.GEOFENCE_ENTRY,
                        severity = severity,
                        message = msg,
                        timestamp = System.currentTimeMillis(),
                        acknowledged = false
                    )

                    fleetDao.insertAlert(AlertEntity.fromModel(alert))

                    context?.let { ctx ->
                        com.example.util.GeofenceNotificationHelper.sendGeofenceNotification(
                            context = ctx,
                            title = title,
                            message = msg
                        )
                    }

                    firestoreRepository.pushGeofenceNotificationEvent(alert, zone.name, "ENTRY")
                }
            } else if (!isInside && wasInside) {
                // EXIT EVENT
                currentlyInZones.remove(zone.id)
                if (currentActiveZoneId == zone.id) {
                    currentActiveZoneId = null
                }

                if (zone.notifyOnExit) {
                    val isSafeZone = zone.type == ZoneType.SAFE
                    val title = if (isSafeZone) "🚨 SORTIE ZONE DE SÉCURITÉ" else "📍 ALERTE SORTIE GÉOFENCE"
                    val msg = if (isSafeZone) {
                        "Le véhicule ${vehicle.name} (${vehicle.licensePlate}) est SORTI de sa zone de sécurité autorisée : ${zone.name} !"
                    } else {
                        "Le véhicule ${vehicle.name} (${vehicle.licensePlate}) a quitté la zone ${zone.name}."
                    }
                    val severity = if (isSafeZone) AlertSeverity.CRITICAL else AlertSeverity.INFO

                    val alert = Alert(
                        id = "ALT-GEO-OUT-${System.currentTimeMillis() % 10000}",
                        vehicleId = vehicle.id,
                        vehicleName = vehicle.name,
                        licensePlate = vehicle.licensePlate,
                        type = AlertType.GEOFENCE_EXIT,
                        severity = severity,
                        message = msg,
                        timestamp = System.currentTimeMillis(),
                        acknowledged = false
                    )

                    fleetDao.insertAlert(AlertEntity.fromModel(alert))

                    context?.let { ctx ->
                        com.example.util.GeofenceNotificationHelper.sendGeofenceNotification(
                            context = ctx,
                            title = title,
                            message = msg
                        )
                    }

                    firestoreRepository.pushGeofenceNotificationEvent(alert, zone.name, "EXIT")
                }
            }
        }

        return vehicle.copy(geofenceZoneId = currentActiveZoneId)
    }

    suspend fun triggerManualGeofenceTest(zoneId: String, vehicleId: String, isEntry: Boolean) {
        val geofences = fleetDao.getAllGeofences().first().map { it.toModel() }
        val vehicles = fleetDao.getAllVehicles().first().map { it.toModel() }
        val zone = geofences.find { it.id == zoneId } ?: return
        val vehicle = vehicles.find { it.id == vehicleId } ?: vehicles.firstOrNull() ?: return

        val eventType = if (isEntry) "ENTRY" else "EXIT"
        val alertType = if (isEntry) AlertType.GEOFENCE_ENTRY else AlertType.GEOFENCE_EXIT
        val title = if (isEntry) "🚨 TEST NOTIFICATION ENTRÉE" else "📍 TEST NOTIFICATION SORTIE"
        val msg = if (isEntry) {
            "TEST : ${vehicle.name} (${vehicle.licensePlate}) est entré dans la zone ${zone.name} (${zone.type.labelFr})."
        } else {
            "TEST : ${vehicle.name} (${vehicle.licensePlate}) a quitté la zone ${zone.name}."
        }

        val alert = Alert(
            id = "ALT-TEST-${System.currentTimeMillis() % 10000}",
            vehicleId = vehicle.id,
            vehicleName = vehicle.name,
            licensePlate = vehicle.licensePlate,
            type = alertType,
            severity = if (zone.type == ZoneType.NO_GO) AlertSeverity.CRITICAL else AlertSeverity.WARNING,
            message = msg,
            timestamp = System.currentTimeMillis(),
            acknowledged = false
        )

        fleetDao.insertAlert(AlertEntity.fromModel(alert))

        context?.let { ctx ->
            com.example.util.GeofenceNotificationHelper.sendGeofenceNotification(
                context = ctx,
                title = title,
                message = msg
            )
        }

        firestoreRepository.pushGeofenceNotificationEvent(alert, zone.name, eventType)
    }

    suspend fun addNewVehicle(
        name: String,
        licensePlate: String,
        imei: String,
        driverName: String,
        driverPhone: String,
        enterpriseId: String,
        enterpriseName: String
    ): Result<Unit> {
        if (imei.length < 10) return Result.failure(Exception("Le code IMEI doit contenir au moins 10 chiffres"))
        if (licensePlate.isBlank()) return Result.failure(Exception("La plaque d'immatriculation est obligatoire"))

        val newVehicle = Vehicle(
            id = "VH-${100 + Random.nextInt(100, 999)}",
            name = name,
            licensePlate = licensePlate,
            imei = imei,
            driverName = driverName,
            driverPhone = driverPhone,
            status = VehicleStatus.STOPPED,
            speedKmH = 0f,
            batteryPct = 100,
            fuelLevelPct = 100,
            latitude = 14.7167 + (Random.nextDouble() - 0.5) * 0.05,
            longitude = -17.4677 + (Random.nextDouble() - 0.5) * 0.05,
            heading = Random.nextFloat() * 360f,
            enterpriseId = enterpriseId,
            enterpriseName = enterpriseName,
            lastUpdateTimestamp = System.currentTimeMillis(),
            address = "Dakar Centre, Sénégal",
            ignitionOn = false
        )

        fleetDao.insertVehicle(VehicleEntity.fromModel(newVehicle))
        firestoreRepository.pushVehicleToFirestore(newVehicle)
        return Result.success(Unit)
    }

    suspend fun toggleEngineLock(vehicleId: String) {
        val list = fleetDao.getAllVehicles().first().map { it.toModel() }
        val found = list.find { it.id == vehicleId } ?: return
        val updated = found.copy(
            lockState = !found.lockState,
            ignitionOn = if (!found.lockState) false else found.ignitionOn,
            status = if (!found.lockState) VehicleStatus.STOPPED else found.status
        )
        fleetDao.updateVehicle(VehicleEntity.fromModel(updated))
        firestoreRepository.pushVehicleToFirestore(updated)
    }

    suspend fun saveGeofenceZone(zone: GeofenceZone) {
        fleetDao.insertGeofence(GeofenceEntity.fromModel(zone))
        if (zone.syncToFirestore) {
            firestoreRepository.pushGeofenceToFirestore(zone)
        }
    }

    suspend fun deleteGeofenceZone(zoneId: String) {
        fleetDao.deleteGeofence(zoneId)
        firestoreRepository.deleteGeofenceFromFirestore(zoneId)
    }

    suspend fun acknowledgeAlert(alertId: String) {
        fleetDao.acknowledgeAlert(alertId)
    }

    suspend fun clearAllAlerts() {
        fleetDao.deleteAllAlerts()
    }

    suspend fun acknowledgeAllAlerts() {
        fleetDao.acknowledgeAllAlerts()
    }

    // --- Geofence Geometry Utilities ---
    fun isVehicleInGeofence(vLat: Double, vLng: Double, zone: GeofenceZone): Boolean {
        return if (zone.geometryType == com.example.model.ZoneGeometryType.POLYGON && zone.polygonPoints.size >= 3) {
            isPointInPolygon(vLat, vLng, zone.polygonPoints)
        } else {
            val distMeters = calculateDistanceMeters(vLat, vLng, zone.centerLat, zone.centerLng)
            distMeters <= zone.radiusMeters
        }
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun isPointInPolygon(lat: Double, lng: Double, polygon: List<com.example.model.LatLngPoint>): Boolean {
        var inside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val pi = polygon[i]
            val pj = polygon[j]
            if ((pi.longitude > lng) != (pj.longitude > lng) &&
                (lat < (pj.latitude - pi.latitude) * (lng - pi.longitude) / (pj.longitude - pi.longitude) + pi.latitude)
            ) {
                inside = !inside
            }
            j = i
        }
        return inside
    }
}
