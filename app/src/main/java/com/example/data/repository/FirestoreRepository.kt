package com.example.data.repository

import android.util.Log
import com.example.model.Vehicle
import com.example.model.VehicleStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Direct Firebase Firestore repository exposing methods for real-time tracking
 * data synchronization and live location updates.
 */
class FirestoreRepository {

    val firestoreInstance: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "Firebase Firestore unavailable, running in local fallback mode: ${e.message}")
            null
        }
    }

    /**
     * Exposes a Flow emitting real-time vehicle location updates whenever Firestore collection changes.
     */
    fun observeLiveVehicleTracking(): Flow<List<Vehicle>> = callbackFlow {
        val db = firestoreInstance
        if (db == null) {
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = db.collection("vehicles")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreRepository", "Firestore real-time tracking error: ", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val vehicles = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            Vehicle(
                                id = doc.id,
                                name = data["name"] as? String ?: "Véhicule ${doc.id}",
                                licensePlate = data["licensePlate"] as? String ?: "DK-0000",
                                imei = data["imei"] as? String ?: "000000000000000",
                                driverName = data["driverName"] as? String ?: "Non assigné",
                                driverPhone = data["driverPhone"] as? String ?: "+221 00 000 00 00",
                                status = (data["status"] as? String)?.let {
                                    runCatching { VehicleStatus.valueOf(it) }.getOrNull()
                                } ?: VehicleStatus.STOPPED,
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
                                odometryKm = (data["odometryKm"] as? Number)?.toDouble() ?: 12450.0,
                                engineTempC = (data["engineTempC"] as? Number)?.toInt() ?: 88,
                                lockState = data["lockState"] as? Boolean ?: true
                            )
                        } catch (e: Exception) {
                            Log.e("FirestoreRepository", "Parsing error for document ${doc.id}", e)
                            null
                        }
                    }
                    trySend(vehicles)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Exposes a Flow emitting typed FirestoreFleetVehicle models directly from Firestore.
     */
    fun observeFirestoreFleetVehicles(): Flow<List<com.example.model.FirestoreFleetVehicle>> = callbackFlow {
        val db = firestoreInstance
        if (db == null) {
            close()
            return@callbackFlow
        }

        val listener = db.collection("vehicles")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreRepository", "Error observing Firestore vehicles: ", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val fleetVehicles = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            com.example.model.FirestoreFleetVehicle(
                                id = doc.id,
                                name = data["name"] as? String ?: "Véhicule ${doc.id}",
                                licensePlate = data["licensePlate"] as? String ?: "DK-0000",
                                imei = data["imei"] as? String ?: "000000000000000",
                                driverName = data["driverName"] as? String ?: "Non assigné",
                                driverPhone = data["driverPhone"] as? String ?: "+221 00 000 00 00",
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
                                odometryKm = (data["odometryKm"] as? Number)?.toDouble() ?: 12450.0,
                                engineTempC = (data["engineTempC"] as? Number)?.toInt() ?: 88,
                                lockState = data["lockState"] as? Boolean ?: true,
                                statusRaw = data["status"] as? String ?: "STOPPED"
                            )
                        } catch (e: Exception) {
                            Log.e("FirestoreRepository", "Parsing FirestoreFleetVehicle error: ${doc.id}", e)
                            null
                        }
                    }
                    trySend(fleetVehicles)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Updates real-time location metrics for a single vehicle.
     */
    suspend fun updateVehicleLocation(
        vehicleId: String,
        latitude: Double,
        longitude: Double,
        speedKmH: Float,
        heading: Float,
        address: String? = null
    ): Boolean {
        val db = firestoreInstance ?: return false
        return try {
            val updates = mutableMapOf<String, Any>(
                "latitude" to latitude,
                "longitude" to longitude,
                "speedKmH" to speedKmH,
                "heading" to heading,
                "lastUpdateTimestamp" to System.currentTimeMillis()
            )
            if (address != null) {
                updates["address"] = address
            }
            db.collection("vehicles").document(vehicleId).update(updates).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to update location for $vehicleId", e)
            false
        }
    }

    /**
     * Pushes complete vehicle state for real-time tracking data synchronization.
     */
    suspend fun syncVehicleData(vehicle: Vehicle): Boolean {
        val db = firestoreInstance ?: return false
        return try {
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
                "lockState" to vehicle.lockState
            )
            db.collection("vehicles").document(vehicle.id).set(vehicleData).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to sync vehicle ${vehicle.id}", e)
            false
        }
    }
}
