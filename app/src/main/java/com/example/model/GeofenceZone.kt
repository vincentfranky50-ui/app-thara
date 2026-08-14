package com.example.model

enum class ZoneType(val labelFr: String) {
    SAFE("Zone Autorisée"),
    RESTRICTED("Zone Surveillée"),
    NO_GO("Zone Interdite")
}

enum class ZoneGeometryType(val labelFr: String) {
    CIRCLE("Cercle"),
    POLYGON("Polygone")
}

data class LatLngPoint(
    val latitude: Double,
    val longitude: Double
)

data class GeofenceZone(
    val id: String,
    val name: String,
    val centerLat: Double,
    val centerLng: Double,
    val radiusMeters: Float = 1000f,
    val type: ZoneType = ZoneType.SAFE,
    val enterpriseId: String = "ENT-01",
    val activeVehicleCount: Int = 0,
    val geometryType: ZoneGeometryType = ZoneGeometryType.CIRCLE,
    val polygonPoints: List<LatLngPoint> = emptyList(),
    val poiCategory: String = "Dépôt & Logistique",
    val notifyOnEntry: Boolean = true,
    val notifyOnExit: Boolean = true,
    val syncToFirestore: Boolean = true
)

