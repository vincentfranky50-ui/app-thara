package com.example.model

enum class MaintenanceUrgency(val labelFr: String) {
    NORMAL("Normal - Bon État"),
    ATTENTION("Surveillance Requise"),
    URGENT("Maintenance Prioritaire"),
    CRITICAL("Arrêt / Remplacement Immédiat")
}

data class EngineDataLog(
    val id: String,
    val vehicleId: String,
    val timestamp: Long,
    val rpm: Int,
    val coolantTempC: Int,
    val oilPressureBar: Double,
    val batteryVoltage: Double,
    val transmissionTempC: Int,
    val fuelInjectionRateLh: Double,
    val cylinderMisfires: Int,
    val obdDtcCodes: List<String>,
    val odometryKm: Double
)

data class PredictedIssue(
    val componentName: String,
    val failureRiskPct: Int,
    val estimatedRemainingKm: Int,
    val estimatedCostFcfa: Int,
    val rootCauseAnalysis: String,
    val recommendedAction: String
)

data class MaintenancePredictionResult(
    val vehicleId: String,
    val vehicleName: String,
    val overallHealthScore: Int, // 0 to 100%
    val urgency: MaintenanceUrgency,
    val predictedIssues: List<PredictedIssue>,
    val aiDiagnosticReport: String,
    val recommendedPartsToOrder: List<String>,
    val analyzedLogCount: Int,
    val timestamp: Long
)

object EngineTelemetryMock {
    fun generateHistoricalLogs(vehicle: Vehicle): List<EngineDataLog> {
        val now = System.currentTimeMillis()
        val logs = mutableListOf<EngineDataLog>()

        val baseTemp = vehicle.engineTempC
        val isCritical = vehicle.fuelLevelPct < 25 || vehicle.batteryPct < 20 || vehicle.speedKmH > 90

        for (i in 0 until 12) {
            val timeOffset = (11 - i) * 10 * 60 * 1000L // every 10 mins over last 2 hours
            val tempVariation = if (isCritical && i > 6) (i - 6) * 3 else (i % 3 - 1)
            val rpmVar = 850 + ((i * 320) % 2400) + (if (isCritical) 400 else 0)
            val oilVar = 3.8 - (i * 0.12)
            val voltVar = 14.1 - (if (vehicle.batteryPct < 20) 1.8 else 0.2)
            val dtcList = mutableListOf<String>()

            if (baseTemp + tempVariation > 102) {
                dtcList.add("P0118 - Température LDR Élevée")
            }
            if (oilVar < 2.0) {
                dtcList.add("P0522 - Pression Huile Basse")
            }
            if (voltVar < 12.2) {
                dtcList.add("P0562 - Tension Système Basse")
            }
            if (i == 11 && isCritical) {
                dtcList.add("P0300 - Ratés d'allumage multiples")
            }

            logs.add(
                EngineDataLog(
                    id = "LOG-${vehicle.id}-$i",
                    vehicleId = vehicle.id,
                    timestamp = now - timeOffset,
                    rpm = rpmVar,
                    coolantTempC = (baseTemp + tempVariation).coerceIn(70, 118),
                    oilPressureBar = String.format("%.2f", oilVar.coerceAtLeast(1.1)).toDouble(),
                    batteryVoltage = String.format("%.1f", voltVar.coerceAtLeast(10.8)).toDouble(),
                    transmissionTempC = 82 + (i * 2),
                    fuelInjectionRateLh = String.format("%.1f", 2.4 + (rpmVar / 1000.0) * 1.8).toDouble(),
                    cylinderMisfires = if (isCritical) (i * 2) else 0,
                    obdDtcCodes = dtcList,
                    odometryKm = vehicle.odometryKm - (11 - i) * 8.5
                )
            )
        }
        return logs
    }
}
