package com.example.model

data class GeofenceBreachSummary(
    val totalBreachesCount: Int,
    val unauthorizedZoneExits: Int,
    val noGoZoneEntries: Int,
    val topViolatedZones: List<String>,
    val riskScore: Int, // 0 - 100
    val executiveSummary: String
)

data class RouteOptimizationProposal(
    val id: String,
    val tripId: String,
    val vehicleName: String,
    val licensePlate: String,
    val origin: String,
    val destination: String,
    val originalDistanceKm: Double,
    val optimizedDistanceKm: Double,
    val originalDurationMinutes: Int,
    val optimizedDurationMinutes: Int,
    val estimatedFuelSavedLiters: Double,
    val estimatedCostSavedFcfa: Long,
    val recommendedRouteTitle: String,
    val rationale: String,
    val detourAvoided: String,
    val priority: String // "Haute", "Moyenne", "Éco"
)

data class ActionableDispatchRule(
    val id: String,
    val category: String, // "Sécurité", "Éco-Trajet", "Géofencing", "Horaires"
    val title: String,
    val description: String,
    val targetVehicles: List<String>,
    val impact: String
)

data class PredictiveViolationPrediction(
    val id: String,
    val vehicleId: String,
    val vehicleName: String,
    val licensePlate: String,
    val targetZoneName: String,
    val riskProbability: Float, // 0.0 to 1.0
    val estimatedTimeToBreachMinutes: Int,
    val confidenceInterval: Float, // 0.0 to 1.0
    val anomalyReason: String,
    val historicalPatternMatch: String,
    val recommendedIntervention: String,
    val predictedSpeedKmH: Float,
    val historicalAvgSpeedKmH: Float,
    val timestamp: Long = System.currentTimeMillis()
)

data class GeofenceAndRouteOptimizationResult(
    val analyzedAtTimestamp: Long = System.currentTimeMillis(),
    val analyzedVehiclesCount: Int,
    val analyzedTripsCount: Int,
    val analyzedAlertsCount: Int,
    val breachSummary: GeofenceBreachSummary,
    val proposals: List<RouteOptimizationProposal>,
    val actionRules: List<ActionableDispatchRule>,
    val predictiveViolations: List<PredictiveViolationPrediction> = emptyList(),
    val overallFuelSavingsPercentage: Float,
    val monthlyEstimatedSavingsFcfa: Long,
    val rawGeminiAnalysis: String
)
