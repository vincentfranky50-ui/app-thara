package com.example.data.repository

import android.util.Log
import com.example.data.db.FleetDao
import com.example.data.db.VehicleEntity
import com.example.model.StopPoint
import com.example.model.Trip
import com.example.model.Vehicle
import com.example.model.VehicleStatus
import com.example.model.Waypoint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreFleetRepository(
    private val fleetDao: FleetDao,
    private val externalScope: CoroutineScope,
    private val context: android.content.Context? = null
) {
    private val _firestoreSyncActive = MutableStateFlow(false)
    val firestoreSyncActive: StateFlow<Boolean> = _firestoreSyncActive.asStateFlow()

    private val _tripsFlow = MutableStateFlow<List<Trip>>(emptyList())
    val tripsFlow: StateFlow<List<Trip>> = _tripsFlow.asStateFlow()

    private var firestoreListener: ListenerRegistration? = null
    private var geofenceListener: ListenerRegistration? = null
    private var notificationListener: ListenerRegistration? = null
    private var tripsListener: ListenerRegistration? = null
    private var firestoreInstance: FirebaseFirestore? = null

    init {
        initFirestore()
    }

    private fun initFirestore() {
        try {
            firestoreInstance = FirebaseFirestore.getInstance()
            _firestoreSyncActive.value = true
            startRealtimeSnapshotListener()
            Log.d("FirestoreFleetRepo", "Firebase Firestore initialized successfully.")
        } catch (e: Exception) {
            _firestoreSyncActive.value = false
            Log.w("FirestoreFleetRepo", "Firestore initialization bypassed (running offline mode): ${e.message}")
        }
    }

    private fun startRealtimeSnapshotListener() {
        val db = firestoreInstance ?: return

        try {
            firestoreListener = db.collection("vehicles")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirestoreFleetRepo", "Listen failed: ", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        externalScope.launch(Dispatchers.IO) {
                            val vehiclesFromFirestore = snapshot.documents.mapNotNull { doc ->
                                try {
                                    val data = doc.data ?: return@mapNotNull null
                                    VehicleEntity(
                                        id = doc.id,
                                        name = data["name"] as? String ?: "Véhicule ${doc.id}",
                                        licensePlate = data["licensePlate"] as? String ?: "DK-0000",
                                        imei = data["imei"] as? String ?: "000000000000000",
                                        driverName = data["driverName"] as? String ?: "Non assigné",
                                        driverPhone = data["driverPhone"] as? String ?: "+221 00 000 00 00",
                                        status = data["status"] as? String ?: VehicleStatus.STOPPED.name,
                                        speedKmH = (data["speedKmH"] as? Number)?.toFloat() ?: 0f,
                                        batteryPct = (data["batteryPct"] as? Number)?.toInt() ?: 100,
                                        fuelLevelPct = (data["fuelLevelPct"] as? Number)?.toInt() ?: 100,
                                        latitude = (data["latitude"] as? Number)?.toDouble() ?: 14.7167,
                                        longitude = (data["longitude"] as? Number)?.toDouble() ?: -17.4677,
                                        heading = (data["heading"] as? Number)?.toFloat() ?: 0f,
                                        enterpriseId = data["enterpriseId"] as? String ?: "ENT-01",
                                        enterpriseName = data["enterpriseName"] as? String ?: "Flotte Thara",
                                        lastUpdateTimestamp = (data["lastUpdateTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                        address = data["address"] as? String ?: "Dakar, Sénégal",
                                        ignitionOn = data["ignitionOn"] as? Boolean ?: false,
                                        geofenceZoneId = data["geofenceZoneId"] as? String,
                                        odometryKm = (data["odometryKm"] as? Number)?.toDouble() ?: 0.0,
                                        engineTempC = (data["engineTempC"] as? Number)?.toInt() ?: 80,
                                        lockState = data["lockState"] as? Boolean ?: true
                                    )
                                } catch (ex: Exception) {
                                    null
                                }
                            }

                            if (vehiclesFromFirestore.isNotEmpty()) {
                                fleetDao.insertOrUpdateVehicles(vehiclesFromFirestore)
                            }
                        }
                    }
                }

            geofenceListener = db.collection("geofence_zones")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirestoreFleetRepo", "Geofence listen failed: ", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        externalScope.launch(Dispatchers.IO) {
                            val zonesFromFirestore = snapshot.documents.mapNotNull { doc ->
                                try {
                                    val data = doc.data ?: return@mapNotNull null
                                    val rawPolygon = data["polygonPoints"] as? List<Map<String, Any>> ?: emptyList()
                                    val polygonPointsRaw = rawPolygon.mapNotNull { p ->
                                        val lat = (p["lat"] as? Number)?.toDouble()
                                        val lng = (p["lng"] as? Number)?.toDouble()
                                        if (lat != null && lng != null) "$lat,$lng" else null
                                    }.joinToString(";")

                                    com.example.data.db.GeofenceEntity(
                                        id = doc.id,
                                        name = data["name"] as? String ?: "Zone ${doc.id}",
                                        centerLat = (data["centerLat"] as? Number)?.toDouble() ?: 14.7167,
                                        centerLng = (data["centerLng"] as? Number)?.toDouble() ?: -17.4677,
                                        radiusMeters = (data["radiusMeters"] as? Number)?.toFloat() ?: 1000f,
                                        type = data["type"] as? String ?: com.example.model.ZoneType.SAFE.name,
                                        enterpriseId = data["enterpriseId"] as? String ?: "ENT-01",
                                        activeVehicleCount = (data["activeVehicleCount"] as? Number)?.toInt() ?: 0,
                                        geometryType = data["geometryType"] as? String ?: com.example.model.ZoneGeometryType.CIRCLE.name,
                                        polygonPointsRaw = polygonPointsRaw,
                                        poiCategory = data["poiCategory"] as? String ?: "Dépôt & Logistique",
                                        notifyOnEntry = data["notifyOnEntry"] as? Boolean ?: true,
                                        notifyOnExit = data["notifyOnExit"] as? Boolean ?: true,
                                        syncToFirestore = true
                                    )
                                } catch (ex: Exception) {
                                    null
                                }
                            }

                            if (zonesFromFirestore.isNotEmpty()) {
                                fleetDao.insertGeofences(zonesFromFirestore)
                            }
                        }
                    }
                }

            notificationListener = db.collection("geofence_notifications")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirestoreFleetRepo", "Geofence notifications listen failed: ", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        externalScope.launch(Dispatchers.IO) {
                            for (dc in snapshot.documentChanges) {
                                if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                    val data = dc.document.data
                                    val alertId = dc.document.id
                                    val vehicleId = data["vehicleId"] as? String ?: "UNKNOWN"
                                    val vehicleName = data["vehicleName"] as? String ?: "Véhicule"
                                    val licensePlate = data["licensePlate"] as? String ?: ""
                                    val eventType = data["eventType"] as? String ?: "ENTRY"
                                    val zoneName = data["zoneName"] as? String ?: "Zone"
                                    val msg = data["message"] as? String ?: "Alerte Périmètre $eventType : $vehicleName dans $zoneName"
                                    val timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()

                                    val alertType = if (eventType == "EXIT") com.example.model.AlertType.GEOFENCE_EXIT else com.example.model.AlertType.GEOFENCE_ENTRY
                                    val alertEntity = com.example.data.db.AlertEntity(
                                        id = alertId,
                                        vehicleId = vehicleId,
                                        vehicleName = vehicleName,
                                        licensePlate = licensePlate,
                                        type = alertType.name,
                                        severity = if (eventType == "EXIT") com.example.model.AlertSeverity.INFO.name else com.example.model.AlertSeverity.WARNING.name,
                                        message = msg,
                                        timestamp = timestamp,
                                        acknowledged = false
                                    )

                                    fleetDao.insertAlert(alertEntity)

                                    context?.let { ctx ->
                                        val title = if (eventType == "EXIT") "📍 SORTIE DE ZONE (Firestore)" else "🚨 ENTRÉE DANS ZONE (Firestore)"
                                        com.example.util.GeofenceNotificationHelper.sendGeofenceNotification(
                                            context = ctx,
                                            title = title,
                                            message = msg
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            tripsListener = db.collection("trips")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirestoreFleetRepo", "Trips listen failed: ", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        externalScope.launch(Dispatchers.IO) {
                            val tripsFromFirestore = snapshot.documents.mapNotNull { doc ->
                                try {
                                    val data = doc.data ?: return@mapNotNull null
                                    val rawWaypoints = data["waypoints"] as? List<Map<String, Any>> ?: emptyList()
                                    val waypoints = rawWaypoints.mapNotNull { wp ->
                                        val latVal = (wp["latitude"] ?: wp["lat"]) as? Number
                                        val lngVal = (wp["longitude"] ?: wp["lng"]) as? Number
                                        val speedVal = (wp["speedKmH"] ?: wp["speed"]) as? Number
                                        val tsVal = wp["timestamp"] as? Number
                                        val lat = latVal?.toDouble()
                                        val lng = lngVal?.toDouble()
                                        val speed = speedVal?.toFloat() ?: 0f
                                        val ts = tsVal?.toLong() ?: 0L
                                        if (lat != null && lng != null) Waypoint(lat, lng, speed, ts) else null
                                    }

                                    val rawStops = data["stopPoints"] as? List<Map<String, Any>> ?: emptyList()
                                    val stopPoints = rawStops.mapNotNull { sp ->
                                        val latVal = (sp["latitude"] ?: sp["lat"]) as? Number
                                        val lngVal = (sp["longitude"] ?: sp["lng"]) as? Number
                                        val lat = latVal?.toDouble()
                                        val lng = lngVal?.toDouble()
                                        if (lat != null && lng != null) {
                                            StopPoint(
                                                id = sp["id"] as? String ?: "SP-${System.currentTimeMillis()}",
                                                latitude = lat,
                                                longitude = lng,
                                                address = sp["address"] as? String ?: "Point d'arrêt",
                                                durationMinutes = (sp["durationMinutes"] as? Number)?.toInt() ?: 10,
                                                arrivalTime = sp["arrivalTime"] as? String ?: "00:00",
                                                reason = sp["reason"] as? String ?: "Pause"
                                            )
                                        } else null
                                    }

                                    Trip(
                                        id = doc.id,
                                        vehicleId = data["vehicleId"] as? String ?: "VH-101",
                                        vehicleName = data["vehicleName"] as? String ?: "Véhicule",
                                        licensePlate = data["licensePlate"] as? String ?: "DK-0000",
                                        dateLabel = data["dateLabel"] as? String ?: "Aujourd'hui",
                                        startTime = data["startTime"] as? String ?: "08:00",
                                        endTime = data["endTime"] as? String ?: "10:00",
                                        departureAddress = data["departureAddress"] as? String ?: "Départ",
                                        arrivalAddress = data["arrivalAddress"] as? String ?: "Arrivée",
                                        distanceKm = (data["distanceKm"] as? Number)?.toDouble() ?: 0.0,
                                        avgSpeedKmH = (data["avgSpeedKmH"] as? Number)?.toFloat() ?: 0f,
                                        maxSpeedKmH = (data["maxSpeedKmH"] as? Number)?.toFloat() ?: 0f,
                                        durationMinutes = (data["durationMinutes"] as? Number)?.toInt() ?: 0,
                                        fuelConsumedLiters = (data["fuelConsumedLiters"] as? Number)?.toDouble() ?: 0.0,
                                        stopPoints = stopPoints,
                                        waypoints = waypoints
                                    )
                                } catch (ex: Exception) {
                                    null
                                }
                            }

                            if (tripsFromFirestore.isNotEmpty()) {
                                _tripsFlow.value = tripsFromFirestore
                            }
                        }
                    } else if (snapshot != null && snapshot.isEmpty) {
                        // Seed default trips to Firestore so collection is populated
                        externalScope.launch(Dispatchers.IO) {
                            seedDefaultTripsToFirestore()
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirestoreFleetRepo", "Error attaching Firestore snapshot listener", e)
        }
    }

    suspend fun seedDefaultTripsToFirestore() {
        val db = firestoreInstance ?: return
        try {
            val defaultTrips = TripRepository.getAllTrips()
            for (trip in defaultTrips) {
                pushTripToFirestore(trip)
            }
            Log.d("FirestoreFleetRepo", "Seeded ${defaultTrips.size} default trips to Firestore.")
        } catch (e: Exception) {
            Log.w("FirestoreFleetRepo", "Could not seed trips to Firestore: ${e.message}")
        }
    }

    suspend fun pushVehicleToFirestore(vehicle: Vehicle) {
        val db = firestoreInstance ?: return
        try {
            val vehicleData = hashMapOf(
                "name" to vehicle.name,
                "licensePlate" to vehicle.licensePlate,
                "imei" to vehicle.imei,
                "driverName" to vehicle.driverName,
                "driverPhone" to vehicle.driverPhone,
                "status" to vehicle.status.name,
                "speedKmH" to vehicle.speedKmH,
                "batteryPct" to vehicle.batteryPct,
                "fuelLevelPct" to vehicle.fuelLevelPct,
                "latitude" to vehicle.latitude,
                "longitude" to vehicle.longitude,
                "heading" to vehicle.heading,
                "enterpriseId" to vehicle.enterpriseId,
                "enterpriseName" to vehicle.enterpriseName,
                "lastUpdateTimestamp" to vehicle.lastUpdateTimestamp,
                "address" to vehicle.address,
                "ignitionOn" to vehicle.ignitionOn,
                "geofenceZoneId" to vehicle.geofenceZoneId,
                "odometryKm" to vehicle.odometryKm,
                "engineTempC" to vehicle.engineTempC,
                "lockState" to vehicle.lockState,
                "fuelHistory" to vehicle.fuelHistory.map { mapOf("day" to it.day, "litersPer100Km" to it.litersPer100Km) }
            )
            db.collection("vehicles").document(vehicle.id).set(vehicleData).await()
            Log.d("FirestoreFleetRepo", "Pushed vehicle ${vehicle.id} to Firestore.")
        } catch (e: Exception) {
            Log.w("FirestoreFleetRepo", "Could not push to Firestore (offline fallback): ${e.message}")
        }
    }

    suspend fun pushGeofenceToFirestore(zone: com.example.model.GeofenceZone) {
        val db = firestoreInstance ?: return
        try {
            val geofenceData = hashMapOf(
                "id" to zone.id,
                "name" to zone.name,
                "centerLat" to zone.centerLat,
                "centerLng" to zone.centerLng,
                "radiusMeters" to zone.radiusMeters,
                "type" to zone.type.name,
                "enterpriseId" to zone.enterpriseId,
                "geometryType" to zone.geometryType.name,
                "poiCategory" to zone.poiCategory,
                "notifyOnEntry" to zone.notifyOnEntry,
                "notifyOnExit" to zone.notifyOnExit,
                "polygonPoints" to zone.polygonPoints.map { mapOf("lat" to it.latitude, "lng" to it.longitude) },
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("geofence_zones").document(zone.id).set(geofenceData).await()
            Log.d("FirestoreFleetRepo", "Pushed geofence zone ${zone.id} to Firestore.")
        } catch (e: Exception) {
            Log.w("FirestoreFleetRepo", "Firestore geofence push fallback: ${e.message}")
        }
    }

    suspend fun pushGeofenceNotificationEvent(alert: com.example.model.Alert, zoneName: String, eventType: String) {
        val db = firestoreInstance ?: return
        try {
            val eventData = hashMapOf(
                "alertId" to alert.id,
                "vehicleId" to alert.vehicleId,
                "vehicleName" to alert.vehicleName,
                "licensePlate" to alert.licensePlate,
                "zoneName" to zoneName,
                "eventType" to eventType, // ENTRY or EXIT
                "message" to alert.message,
                "timestamp" to alert.timestamp,
                "syncedToCloud" to true
            )
            db.collection("geofence_notifications").document(alert.id).set(eventData).await()
            Log.d("FirestoreFleetRepo", "Pushed geofence notification event ${alert.id} to Firestore.")
        } catch (e: Exception) {
            Log.w("FirestoreFleetRepo", "Firestore event push fallback: ${e.message}")
        }
    }

    suspend fun pushTripToFirestore(trip: Trip) {
        val db = firestoreInstance ?: return
        try {
            val tripData = hashMapOf(
                "id" to trip.id,
                "vehicleId" to trip.vehicleId,
                "vehicleName" to trip.vehicleName,
                "licensePlate" to trip.licensePlate,
                "dateLabel" to trip.dateLabel,
                "startTime" to trip.startTime,
                "endTime" to trip.endTime,
                "departureAddress" to trip.departureAddress,
                "arrivalAddress" to trip.arrivalAddress,
                "distanceKm" to trip.distanceKm,
                "avgSpeedKmH" to trip.avgSpeedKmH,
                "maxSpeedKmH" to trip.maxSpeedKmH,
                "durationMinutes" to trip.durationMinutes,
                "fuelConsumedLiters" to trip.fuelConsumedLiters,
                "stopPoints" to trip.stopPoints.map { sp ->
                    mapOf(
                        "id" to sp.id,
                        "latitude" to sp.latitude,
                        "longitude" to sp.longitude,
                        "address" to sp.address,
                        "durationMinutes" to sp.durationMinutes,
                        "arrivalTime" to sp.arrivalTime,
                        "reason" to sp.reason
                    )
                },
                "waypoints" to trip.waypoints.map { wp ->
                    mapOf(
                        "latitude" to wp.latitude,
                        "longitude" to wp.longitude,
                        "speedKmH" to wp.speedKmH,
                        "timestamp" to wp.timestamp
                    )
                }
            )
            db.collection("trips").document(trip.id).set(tripData).await()
            Log.d("FirestoreFleetRepo", "Pushed trip ${trip.id} to Firestore.")
        } catch (e: Exception) {
            Log.w("FirestoreFleetRepo", "Could not push trip to Firestore: ${e.message}")
        }
    }

    suspend fun deleteGeofenceFromFirestore(zoneId: String) {
        val db = firestoreInstance ?: return
        try {
            db.collection("geofence_zones").document(zoneId).delete().await()
            Log.d("FirestoreFleetRepo", "Deleted geofence zone $zoneId from Firestore.")
        } catch (e: Exception) {
            Log.w("FirestoreFleetRepo", "Firestore geofence delete fallback: ${e.message}")
        }
    }

    fun stopListener() {
        firestoreListener?.remove()
        firestoreListener = null
        geofenceListener?.remove()
        geofenceListener = null
        notificationListener?.remove()
        notificationListener = null
        tripsListener?.remove()
        tripsListener = null
    }
}
