package com.example.model

enum class DrivingEventType(val labelFr: String, val iconRes: String) {
    SUDDEN_BRAKING("Freinage Brusque", "brake"),
    RAPID_ACCELERATION("Accélération Brutale", "speed"),
    SHARP_CORNERING("Virage Serré", "turn"),
    EXCESSIVE_SPEED("Vitesse Excessive", "speeding"),
    IDLING("Ralenti Moteur Prolongé", "timer")
}

data class DrivingTelemetryEvent(
    val id: String,
    val vehicleId: String,
    val driverName: String,
    val eventType: DrivingEventType,
    val gForce: Float, // e.g. -0.72f for braking, 0.58f for acceleration
    val speedBeforeKmH: Float,
    val speedAfterKmH: Float,
    val locationName: String,
    val timestamp: Long,
    val contextualRiskFactor: String
)

data class CoachingRecommendation(
    val category: String, // e.g. "Anticipation", "Souplesse Pédale", "Éco-Conduite"
    val title: String,
    val advice: String,
    val estimatedFuelSavingPct: Float,
    val safetyImpactPoints: Int
)

data class DriverCoachingReport(
    val reportId: String,
    val vehicleId: String,
    val driverName: String,
    val safetyScore: Int, // 0..100
    val ecoDrivingScore: Int, // 0..100
    val analyzedEventCount: Int,
    val suddenBrakingCount: Int,
    val rapidAccelerationCount: Int,
    val estimatedExtraFuelLitersPer100Km: Double,
    val aiCoachingSummary: String,
    val recommendations: List<CoachingRecommendation>,
    val timestamp: Long
)

data class InAppCoachingNotification(
    val id: String,
    val vehicleId: String,
    val driverName: String,
    val title: String,
    val message: String,
    val coachingCategory: String,
    val severity: AlertSeverity,
    val timestamp: Long,
    val isRead: Boolean = false,
    val actionableTip: String? = null
)

object DrivingBehaviorMock {
    fun generateTelemetryEvents(vehicle: Vehicle): List<DrivingTelemetryEvent> {
        val now = System.currentTimeMillis()
        val driver = vehicle.driverName
        val vId = vehicle.id

        return listOf(
            DrivingTelemetryEvent(
                id = "EVT-01-$vId",
                vehicleId = vId,
                driverName = driver,
                eventType = DrivingEventType.SUDDEN_BRAKING,
                gForce = -0.74f,
                speedBeforeKmH = 78f,
                speedAfterKmH = 22f,
                locationName = "Avenue Cheikh Anta Diop (Dakar)",
                timestamp = now - (12 * 60 * 1000),
                contextualRiskFactor = "Approche rapide d'un feu tricolore à fort trafic"
            ),
            DrivingTelemetryEvent(
                id = "EVT-02-$vId",
                vehicleId = vId,
                driverName = driver,
                eventType = DrivingEventType.RAPID_ACCELERATION,
                gForce = 0.62f,
                speedBeforeKmH = 0f,
                speedAfterKmH = 65f,
                locationName = "Sortie Autoroute de l'Avenir (Péage)",
                timestamp = now - (28 * 60 * 1000),
                contextualRiskFactor = "Démarrage en côte à fort régime moteur (3800 RPM)"
            ),
            DrivingTelemetryEvent(
                id = "EVT-03-$vId",
                vehicleId = vId,
                driverName = driver,
                eventType = DrivingEventType.SUDDEN_BRAKING,
                gForce = -0.68f,
                speedBeforeKmH = 92f,
                speedAfterKmH = 45f,
                locationName = "Corridor Dakar - Thies (Km 34)",
                timestamp = now - (45 * 60 * 1000),
                contextualRiskFactor = "Ralentissement brusque sur ralentisseur non signalé"
            ),
            DrivingTelemetryEvent(
                id = "EVT-04-$vId",
                vehicleId = vId,
                driverName = driver,
                eventType = DrivingEventType.SHARP_CORNERING,
                gForce = 0.55f,
                speedBeforeKmH = 58f,
                speedAfterKmH = 42f,
                locationName = "Rond-Point Camberene",
                timestamp = now - (68 * 60 * 1000),
                contextualRiskFactor = "Transfert de masse latéral élevé dans le virage"
            ),
            DrivingTelemetryEvent(
                id = "EVT-05-$vId",
                vehicleId = vId,
                driverName = driver,
                eventType = DrivingEventType.IDLING,
                gForce = 0.02f,
                speedBeforeKmH = 0f,
                speedAfterKmH = 0f,
                locationName = "Zone Portuaire de Dakar",
                timestamp = now - (90 * 60 * 1000),
                contextualRiskFactor = "Moteur tournant à l'arrêt pendant 18 minutes consécutives"
            )
        )
    }
}
