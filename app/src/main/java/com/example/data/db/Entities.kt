package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Alert
import com.example.model.AlertSeverity
import com.example.model.AlertType
import com.example.model.GeofenceZone
import com.example.model.Vehicle
import com.example.model.VehicleStatus
import com.example.model.ZoneType

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val licensePlate: String,
    val imei: String,
    val driverName: String,
    val driverPhone: String,
    val status: String,
    val speedKmH: Float,
    val batteryPct: Int,
    val fuelLevelPct: Int,
    val latitude: Double,
    val longitude: Double,
    val heading: Float,
    val enterpriseId: String,
    val enterpriseName: String,
    val lastUpdateTimestamp: Long,
    val address: String,
    val ignitionOn: Boolean,
    val geofenceZoneId: String?,
    val odometryKm: Double,
    val engineTempC: Int,
    val lockState: Boolean
) {
    fun toModel(): Vehicle = Vehicle(
        id = id,
        name = name,
        licensePlate = licensePlate,
        imei = imei,
        driverName = driverName,
        driverPhone = driverPhone,
        status = runCatching { VehicleStatus.valueOf(status) }.getOrDefault(VehicleStatus.MOVING),
        speedKmH = speedKmH,
        batteryPct = batteryPct,
        fuelLevelPct = fuelLevelPct,
        latitude = latitude,
        longitude = longitude,
        heading = heading,
        enterpriseId = enterpriseId,
        enterpriseName = enterpriseName,
        lastUpdateTimestamp = lastUpdateTimestamp,
        address = address,
        ignitionOn = ignitionOn,
        geofenceZoneId = geofenceZoneId,
        odometryKm = odometryKm,
        engineTempC = engineTempC,
        lockState = lockState
    )

    companion object {
        fun fromModel(v: Vehicle) = VehicleEntity(
            id = v.id,
            name = v.name,
            licensePlate = v.licensePlate,
            imei = v.imei,
            driverName = v.driverName,
            driverPhone = v.driverPhone,
            status = v.status.name,
            speedKmH = v.speedKmH,
            batteryPct = v.batteryPct,
            fuelLevelPct = v.fuelLevelPct,
            latitude = v.latitude,
            longitude = v.longitude,
            heading = v.heading,
            enterpriseId = v.enterpriseId,
            enterpriseName = v.enterpriseName,
            lastUpdateTimestamp = v.lastUpdateTimestamp,
            address = v.address,
            ignitionOn = v.ignitionOn,
            geofenceZoneId = v.geofenceZoneId,
            odometryKm = v.odometryKm,
            engineTempC = v.engineTempC,
            lockState = v.lockState
        )
    }
}

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val vehicleName: String,
    val licensePlate: String,
    val type: String,
    val severity: String,
    val message: String,
    val timestamp: Long,
    val acknowledged: Boolean
) {
    fun toModel(): Alert = Alert(
        id = id,
        vehicleId = vehicleId,
        vehicleName = vehicleName,
        licensePlate = licensePlate,
        type = runCatching { AlertType.valueOf(type) }.getOrDefault(AlertType.SPEEDING),
        severity = runCatching { AlertSeverity.valueOf(severity) }.getOrDefault(AlertSeverity.WARNING),
        message = message,
        timestamp = timestamp,
        acknowledged = acknowledged
    )

    companion object {
        fun fromModel(a: Alert) = AlertEntity(
            id = a.id,
            vehicleId = a.vehicleId,
            vehicleName = a.vehicleName,
            licensePlate = a.licensePlate,
            type = a.type.name,
            severity = a.severity.name,
            message = a.message,
            timestamp = a.timestamp,
            acknowledged = a.acknowledged
        )
    }
}

@Entity(tableName = "geofences")
data class GeofenceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val centerLat: Double,
    val centerLng: Double,
    val radiusMeters: Float,
    val type: String,
    val enterpriseId: String,
    val activeVehicleCount: Int,
    val geometryType: String = "CIRCLE",
    val polygonPointsRaw: String = "",
    val poiCategory: String = "Dépôt & Logistique",
    val notifyOnEntry: Boolean = true,
    val notifyOnExit: Boolean = true,
    val syncToFirestore: Boolean = true
) {
    fun toModel(): GeofenceZone {
        val points = if (polygonPointsRaw.isNotBlank()) {
            polygonPointsRaw.split(";").mapNotNull { p ->
                val coords = p.split(",")
                if (coords.size == 2) {
                    val lat = coords[0].toDoubleOrNull()
                    val lng = coords[1].toDoubleOrNull()
                    if (lat != null && lng != null) com.example.model.LatLngPoint(lat, lng) else null
                } else null
            }
        } else emptyList()

        return GeofenceZone(
            id = id,
            name = name,
            centerLat = centerLat,
            centerLng = centerLng,
            radiusMeters = radiusMeters,
            type = runCatching { ZoneType.valueOf(type) }.getOrDefault(ZoneType.SAFE),
            enterpriseId = enterpriseId,
            activeVehicleCount = activeVehicleCount,
            geometryType = runCatching { com.example.model.ZoneGeometryType.valueOf(geometryType) }.getOrDefault(com.example.model.ZoneGeometryType.CIRCLE),
            polygonPoints = points,
            poiCategory = poiCategory,
            notifyOnEntry = notifyOnEntry,
            notifyOnExit = notifyOnExit,
            syncToFirestore = syncToFirestore
        )
    }

    companion object {
        fun fromModel(g: GeofenceZone): GeofenceEntity {
            val rawPoints = g.polygonPoints.joinToString(";") { "${it.latitude},${it.longitude}" }
            return GeofenceEntity(
                id = g.id,
                name = g.name,
                centerLat = g.centerLat,
                centerLng = g.centerLng,
                radiusMeters = g.radiusMeters,
                type = g.type.name,
                enterpriseId = g.enterpriseId,
                activeVehicleCount = g.activeVehicleCount,
                geometryType = g.geometryType.name,
                polygonPointsRaw = rawPoints,
                poiCategory = g.poiCategory,
                notifyOnEntry = g.notifyOnEntry,
                notifyOnExit = g.notifyOnExit,
                syncToFirestore = g.syncToFirestore
            )
        }
    }
}
