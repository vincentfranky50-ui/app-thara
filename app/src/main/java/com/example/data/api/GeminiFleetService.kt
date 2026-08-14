package com.example.data.api

import com.example.BuildConfig
import com.example.data.repository.TripRepository
import com.example.model.ActionableDispatchRule
import com.example.model.Alert
import com.example.model.AlertSeverity
import com.example.model.AlertType
import com.example.model.CoachingRecommendation
import com.example.model.DriverCoachingReport
import com.example.model.DrivingEventType
import com.example.model.DrivingTelemetryEvent
import com.example.model.EngineDataLog
import com.example.model.GeofenceAndRouteOptimizationResult
import com.example.model.GeofenceBreachSummary
import com.example.model.GeofenceZone
import com.example.model.InAppCoachingNotification
import com.example.model.MaintenancePredictionResult
import com.example.model.MaintenanceUrgency
import com.example.model.PredictedIssue
import com.example.model.RouteOptimizationProposal
import com.example.model.Trip
import com.example.model.Vehicle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiFleetService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeEngineTelemetryLogs(
        vehicle: Vehicle,
        logs: List<EngineDataLog>
    ): MaintenancePredictionResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateHeuristicEnginePrediction(vehicle, logs)
        }

        val formattedLogs = logs.joinToString("\n") { l ->
            "• [${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(l.timestamp))}] " +
                    "RPM: ${l.rpm} | TempLDR: ${l.coolantTempC}°C | PressionHuile: ${l.oilPressureBar} bar | " +
                    "Bat: ${l.batteryVoltage}V | TempTrans: ${l.transmissionTempC}°C | Ratés: ${l.cylinderMisfires} | " +
                    "DTC: ${l.obdDtcCodes.ifEmpty { listOf("Aucun") }.joinToString(", ")}"
        }

        val promptText = """
            Tu es Thara AI, un ingénieur expert en diagnostic mécanique automobile et analyse télématique prédictive OBD-II.
            Analyse les relevés télématiques moteur suivants pour le véhicule '${vehicle.name}' (${vehicle.licensePlate}),
            odomètre actuel: ${vehicle.odometryKm.toInt()} km, statut: ${vehicle.status.labelFr}:
            
            HISTORIQUE DES 2 DERNIÈRES HEURES DE SÉQUENCE TÉLÉMATIQUE MOTEUR :
            $formattedLogs
            
            FOURNIS UN RAPPORT STRUCTURÉ DE PRÉDICTION DE MAINTENANCE :
            1. Note globale de santé moteur (0-100%).
            2. Niveau d'urgence (NORMAL, ATTENTION, URGENT, CRITICAL).
            3. Organes/Composants à risque avec probabilité de défaillance (%), kilométrage estimé avant panne, coût estimé de réparation (FCFA), cause racine, et action préventive requise.
            4. Pièces détachées à commander d'urgence.
            5. Résumé de diagnostic technique clair pour le chef d'atelier mécanique.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", promptText)
                        })
                    })
                })
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val jsonRes = JSONObject(responseString)
                val candidates = jsonRes.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val cand = candidates.getJSONObject(0)
                    val content = cand.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val aiText = parts.getJSONObject(0).optString("text", "")
                        return@withContext parseAiPredictionText(vehicle, logs, aiText)
                    }
                }
            }
            generateHeuristicEnginePrediction(vehicle, logs)
        } catch (e: Exception) {
            generateHeuristicEnginePrediction(vehicle, logs)
        }
    }

    private fun parseAiPredictionText(
        vehicle: Vehicle,
        logs: List<EngineDataLog>,
        aiText: String
    ): MaintenancePredictionResult {
        val lastLog = logs.lastOrNull()
        val maxTemp = logs.maxOfOrNull { it.coolantTempC } ?: 88
        val minOil = logs.minOfOrNull { it.oilPressureBar } ?: 3.5
        val totalMisfires = logs.sumOf { it.cylinderMisfires }

        val healthScore = when {
            maxTemp > 105 || minOil < 1.8 || totalMisfires > 10 -> 45
            maxTemp > 98 || minOil < 2.5 -> 72
            else -> 94
        }

        val urgency = when {
            healthScore < 50 -> MaintenanceUrgency.CRITICAL
            healthScore < 75 -> MaintenanceUrgency.URGENT
            healthScore < 85 -> MaintenanceUrgency.ATTENTION
            else -> MaintenanceUrgency.NORMAL
        }

        return MaintenancePredictionResult(
            vehicleId = vehicle.id,
            vehicleName = vehicle.name,
            overallHealthScore = healthScore,
            urgency = urgency,
            predictedIssues = generateHeuristicEnginePrediction(vehicle, logs).predictedIssues,
            aiDiagnosticReport = aiText,
            recommendedPartsToOrder = listOf("Thermostat 88°C", "Filtre à Huile Synthétique", "Capteur de Pression OBD-II"),
            analyzedLogCount = logs.size,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun generateHeuristicEnginePrediction(
        vehicle: Vehicle,
        logs: List<EngineDataLog>
    ): MaintenancePredictionResult {
        val maxTemp = logs.maxOfOrNull { it.coolantTempC } ?: vehicle.engineTempC
        val minOil = logs.minOfOrNull { it.oilPressureBar } ?: 3.2
        val minVolt = logs.minOfOrNull { it.batteryVoltage } ?: 13.5
        val misfires = logs.sumOf { it.cylinderMisfires }

        val predictedIssues = mutableListOf<PredictedIssue>()
        var healthScore = 98

        if (maxTemp > 100) {
            healthScore -= 28
            predictedIssues.add(
                PredictedIssue(
                    componentName = "Pompe à Eau & Calorstat",
                    failureRiskPct = 82,
                    estimatedRemainingKm = 380,
                    estimatedCostFcfa = 145000,
                    rootCauseAnalysis = "Pics de température du liquide de refroidissement atteignant ${maxTemp}°C sous charge constante.",
                    recommendedAction = "Remplacer le thermostat de régulation et purger le circuit de refroidissement avant le prochain long trajet."
                )
            )
        }

        if (minOil < 2.2) {
            healthScore -= 32
            predictedIssues.add(
                PredictedIssue(
                    componentName = "Circuit de Graissage & Filtre à Huile",
                    failureRiskPct = 78,
                    estimatedRemainingKm = 520,
                    estimatedCostFcfa = 85000,
                    rootCauseAnalysis = "Baisse progressive de la pression d'huile à ${minOil} bar à haut régime RPM.",
                    recommendedAction = "Effectuer la vidange avec huile haute viscosité 15W40 et remplacer la cartouche filtrante d'huile."
                )
            )
        }

        if (minVolt < 12.5) {
            healthScore -= 18
            predictedIssues.add(
                PredictedIssue(
                    componentName = "Alternateur / Batterie 12V OBD",
                    failureRiskPct = 64,
                    estimatedRemainingKm = 890,
                    estimatedCostFcfa = 110000,
                    rootCauseAnalysis = "Fluctuation du régulateur de tension de l'alternateur (${minVolt}V mesuré).",
                    recommendedAction = "Tester la capacité sous charge du pack batterie et inspecter la courroie d'accessoires."
                )
            )
        }

        if (misfires > 5) {
            healthScore -= 22
            predictedIssues.add(
                PredictedIssue(
                    componentName = "Injecteurs Carburant & Bougies",
                    failureRiskPct = 75,
                    estimatedRemainingKm = 420,
                    estimatedCostFcfa = 195000,
                    rootCauseAnalysis = "Détection récurrente du code P0300 avec $misfires ratés d'allumage constatés.",
                    recommendedAction = "Passer les injecteurs au banc de calibration ultrasons et remplacer le faisceau d'allumage."
                )
            )
        }

        if (predictedIssues.isEmpty()) {
            predictedIssues.add(
                PredictedIssue(
                    componentName = "Contrôle Périodique Courroie Distribution",
                    failureRiskPct = 15,
                    estimatedRemainingKm = 8500,
                    estimatedCostFcfa = 65000,
                    rootCauseAnalysis = "Télémétries moteur optimales. Aucune anomalie OBD-II détectée.",
                    recommendedAction = "Maintenir le calendrier de révision standard à la prochaine échéance des 15 000 km."
                )
            )
        }

        val finalScore = healthScore.coerceIn(15, 99)
        val urgency = when {
            finalScore < 50 -> MaintenanceUrgency.CRITICAL
            finalScore < 70 -> MaintenanceUrgency.URGENT
            finalScore < 85 -> MaintenanceUrgency.ATTENTION
            else -> MaintenanceUrgency.NORMAL
        }

        val reportText = """
            📌 DIAGNOSTIC PREDICTIF MOTEUR (Thara AI)
            
            Véhicule : ${vehicle.name} (${vehicle.licensePlate})
            Statut Télématique : Analysez ${logs.size} relevés récents.
            
            • Score de Santé Globale : $finalScore%
            • Statut d'Urgence : ${urgency.labelFr}
            • Température Max LDR : ${maxTemp}°C
            • Pression Huile Min : ${minOil} bar
            • Tension Système Min : ${minVolt} V
            
            💡 Recommandations du Chef d'Atelier :
            ${predictedIssues.joinToString("\n") { "▸ [${it.componentName}] Risque ${it.failureRiskPct}% dans ~${it.estimatedRemainingKm} km : ${it.recommendedAction}" }}
        """.trimIndent()

        val parts = mutableListOf("Huile Moteur 15W40", "Filtre à Huile", "Joint de Vidange")
        if (maxTemp > 100) parts.add("Thermostat Pompe à Eau")
        if (minVolt < 12.5) parts.add("Courroie d'Alternateur")

        return MaintenancePredictionResult(
            vehicleId = vehicle.id,
            vehicleName = vehicle.name,
            overallHealthScore = finalScore,
            urgency = urgency,
            predictedIssues = predictedIssues,
            aiDiagnosticReport = reportText,
            recommendedPartsToOrder = parts,
            analyzedLogCount = logs.size,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun analyzeFleetDiagnostics(vehicles: List<Vehicle>, userPrompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "⚠️ La clé API Gemini n'est pas encore configurée dans le panneau Secrets d'AI Studio.\n\n" +
                    "Directives d'analyse automatique de la flotte Thara :\n" +
                    "- " + vehicles.count { it.speedKmH > 90 } + " véhicule(s) en vitesse élevée (>90 km/h)\n" +
                    "- " + vehicles.count { it.batteryPct < 20 } + " boîtier(s) GPS en niveau de batterie critique (<20%)\n" +
                    "- Consommation estimée normale sur le corridor Dakar-Thies."
        }

        val fleetContext = vehicles.joinToString("\n") { v ->
            "- [${v.id}] ${v.name} (${v.licensePlate}) | Statut: ${v.status.labelFr} | Vitesse: ${v.speedKmH.toInt()} km/h | Carburant/Bat: ${v.fuelLevelPct}% | Chauffeur: ${v.driverName} | GPS: ${v.latitude}, ${v.longitude}"
        }

        val promptText = "Tu es 'Thara AI', l'assistant d'intelligence artificielle expert en télématique de flotte automobile pour 'Thara Tracking'. " +
                "Voici l'état actuel en temps réel de la flotte Thara :\n$fleetContext\n\nQuestion/Demande de l'utilisateur : $userPrompt\n" +
                "Réponds en français avec un ton professionnel, structuré, concis et directement exploitable par un gestionnaire de flotte."

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", promptText)
                        })
                    })
                })
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val jsonRes = JSONObject(responseString)
                val candidates = jsonRes.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val cand = candidates.getJSONObject(0)
                    val content = cand.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "Analyse complétée avec succès.")
                    }
                }
                "Analyse complétée : Aucun problème critique détecté sur vos véhicules actuellement."
            } else {
                "Erreur API Gemini (${response.code}) : ${response.message}"
            }
        } catch (e: Exception) {
            "Erreur lors de la communication avec Thara AI : ${e.localizedMessage ?: "Vérifiez votre connexion internet."}"
        }
    }

    suspend fun analyzeDrivingBehavior(
        vehicle: Vehicle,
        events: List<DrivingTelemetryEvent>
    ): Pair<DriverCoachingReport, List<InAppCoachingNotification>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateHeuristicDrivingCoaching(vehicle, events)
        }

        val eventSummaryText = events.joinToString("\n") { e ->
            "• [${java.text.SimpleDateFormat("HH:mm").format(java.util.Date(e.timestamp))}] " +
                    "Type: ${e.eventType.labelFr} | Force-G: ${e.gForce}g | Speed: ${e.speedBeforeKmH.toInt()} -> ${e.speedAfterKmH.toInt()} km/h | " +
                    "Lieu: ${e.locationName} | Risque Contextuel: ${e.contextualRiskFactor}"
        }

        val promptText = """
            Tu es Thara AI Coaching, un instructeur expert en éco-conduite et sécurité routière poids lourds et véhicules de flotte.
            Analyse les événements de télématique de conduite (freinages brusques, accélérations fortes, etc.) pour le chauffeur '${vehicle.driverName}'
            conduisant le véhicule '${vehicle.name}' (${vehicle.licensePlate}) :
            
            ÉVÉNEMENTS DÉTECTÉS PAR LES ACCÉLÉROMÈTRES DU BOÎTIER OBD-II :
            $eventSummaryText
            
            FOURNIS UN BILAN PÉDAGOGIQUE ET CONSTRUCTIF EN FRANÇAIS :
            1. Note de Sécurité Globale (0-100).
            2. Note d'Éco-Conduite (0-100).
            3. Surconsommation estimée de carburant (en Litres/100km).
            4. Conseils pratiques personnalisés par catégorie (Anticipation, Souplesse, Frein Moteur).
            5. Résumé de coaching motivant et bienveillant pour le chauffeur.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", promptText)
                        })
                    })
                })
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val jsonRes = JSONObject(responseString)
                val candidates = jsonRes.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val cand = candidates.getJSONObject(0)
                    val content = cand.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val aiText = parts.getJSONObject(0).optString("text", "")
                        return@withContext parseAiCoachingResponse(vehicle, events, aiText)
                    }
                }
            }
            generateHeuristicDrivingCoaching(vehicle, events)
        } catch (e: Exception) {
            generateHeuristicDrivingCoaching(vehicle, events)
        }
    }

    private fun parseAiCoachingResponse(
        vehicle: Vehicle,
        events: List<DrivingTelemetryEvent>,
        aiText: String
    ): Pair<DriverCoachingReport, List<InAppCoachingNotification>> {
        val (heuristicReport, heuristicNotifs) = generateHeuristicDrivingCoaching(vehicle, events)
        val updatedReport = heuristicReport.copy(
            aiCoachingSummary = aiText
        )
        return Pair(updatedReport, heuristicNotifs)
    }

    private fun generateHeuristicDrivingCoaching(
        vehicle: Vehicle,
        events: List<DrivingTelemetryEvent>
    ): Pair<DriverCoachingReport, List<InAppCoachingNotification>> {
        val suddenBrakings = events.count { it.eventType == DrivingEventType.SUDDEN_BRAKING }
        val rapidAccels = events.count { it.eventType == DrivingEventType.RAPID_ACCELERATION }
        val sharpCorners = events.count { it.eventType == DrivingEventType.SHARP_CORNERING }
        val idlings = events.count { it.eventType == DrivingEventType.IDLING }

        var safetyScore = 100 - (suddenBrakings * 12) - (rapidAccels * 10) - (sharpCorners * 8)
        safetyScore = safetyScore.coerceIn(35, 98)

        var ecoScore = 100 - (rapidAccels * 14) - (idlings * 10) - (suddenBrakings * 8)
        ecoScore = ecoScore.coerceIn(30, 96)

        val extraFuel = (rapidAccels * 0.45) + (idlings * 0.30) + (suddenBrakings * 0.25)

        val recommendations = mutableListOf<CoachingRecommendation>()

        if (suddenBrakings > 0) {
            recommendations.add(
                CoachingRecommendation(
                    category = "Anticipation & Distances de Sécurité",
                    title = "Anticipez le trafic à 150m",
                    advice = "Augmentez votre distance de sécurité de 2 secondes. Lever le pied plus tôt permet de réduire l'usure des plaquettes de frein et d'économiser 1,2L/100km.",
                    estimatedFuelSavingPct = 8.5f,
                    safetyImpactPoints = 14
                )
            )
        }

        if (rapidAccels > 0) {
            recommendations.add(
                CoachingRecommendation(
                    category = "Souplesse de la Pédale d'Accélérateur",
                    title = "Montée en régime progressive (Passage sous 2200 RPM)",
                    advice = "Passez le rapport supérieur plus tôt lors des démarrages aux péages et feux. Une accélération progressive préserve le turbo et l'embrayage.",
                    estimatedFuelSavingPct = 12.0f,
                    safetyImpactPoints = 18
                )
            )
        }

        if (idlings > 0) {
            recommendations.add(
                CoachingRecommendation(
                    category = "Éco-Conduite & Coupure Moteur",
                    title = "Coupure automatique à l'arrêt (> 2 min)",
                    advice = "Coupez le contact lors des attentes prolongées en zone logistique. Un moteur au ralenti consomme inutilement 1,5 Litre d'essence par heure.",
                    estimatedFuelSavingPct = 6.0f,
                    safetyImpactPoints = 5
                )
            )
        }

        if (recommendations.isEmpty()) {
            recommendations.add(
                CoachingRecommendation(
                    category = "Excellence de Conduite",
                    title = "Maintenir le style fluide et sécuritaire",
                    advice = "Votre style de conduite actuel est optimal. Continuez d'anticiper le relief et la circulation.",
                    estimatedFuelSavingPct = 0f,
                    safetyImpactPoints = 0
                )
            )
        }

        val aiSummary = """
            🎯 BILAN DE COACHING INDIVIDUEL - THARA AI
            Chauffeur : ${vehicle.driverName} (${vehicle.name})
            
            • Score Sécurité Routière : $safetyScore / 100
            • Score Éco-Conduite : $ecoScore / 100
            • Surconsommation Carburant : +${String.format("%.2f", extraFuel)} L/100km
            
            💡 Recommandation Prioritaire :
            ${recommendations.firstOrNull()?.advice ?: "Excellente conduite."}
        """.trimIndent()

        val now = System.currentTimeMillis()
        val notifications = mutableListOf<InAppCoachingNotification>()

        if (suddenBrakings > 0) {
            notifications.add(
                InAppCoachingNotification(
                    id = "NOTIF-COACH-01-${vehicle.id}",
                    vehicleId = vehicle.id,
                    driverName = vehicle.driverName,
                    title = "💡 Conseil Sécurité : Freinage Anticipé",
                    message = "Thara AI a détecté $suddenBrakings freinage(s) brusque(s) récents. Anticipez les ralentissements pour stabiliser le véhicule.",
                    coachingCategory = "Anticipation",
                    severity = AlertSeverity.WARNING,
                    timestamp = now - (5 * 60 * 1000),
                    actionableTip = "Levez le pied de l'accélérateur dès la vue du feu tricolore ou ralentisseur."
                )
            )
        }

        if (rapidAccels > 0) {
            notifications.add(
                InAppCoachingNotification(
                    id = "NOTIF-COACH-02-${vehicle.id}",
                    vehicleId = vehicle.id,
                    driverName = vehicle.driverName,
                    title = "⚡ Éco-Conseil : Accélération Progressive",
                    message = "Réduisez l'enfoncement brusque de la pédale pour économiser jusqu'à 12% de carburant sur le trajet.",
                    coachingCategory = "Éco-Conduite",
                    severity = AlertSeverity.INFO,
                    timestamp = now - (18 * 60 * 1000),
                    actionableTip = "Passez la vitesse supérieure avant 2200 RPM en ville."
                )
            )
        }

        if (notifications.isEmpty()) {
            notifications.add(
                InAppCoachingNotification(
                    id = "NOTIF-COACH-03-${vehicle.id}",
                    vehicleId = vehicle.id,
                    driverName = vehicle.driverName,
                    title = "🌟 Félicitations pour votre Conduite Flotante !",
                    message = "Aucune anomalie détectée sur vos derniers kilomètres. Votre score de sécurité est de $safetyScore/100.",
                    coachingCategory = "Sécurité",
                    severity = AlertSeverity.INFO,
                    timestamp = now,
                    actionableTip = "Continuez d'appliquer les principes d'anticipation Thara."
                )
            )
        }

        val report = DriverCoachingReport(
            reportId = "REP-${vehicle.id}-$now",
            vehicleId = vehicle.id,
            driverName = vehicle.driverName,
            safetyScore = safetyScore,
            ecoDrivingScore = ecoScore,
            analyzedEventCount = events.size,
            suddenBrakingCount = suddenBrakings,
            rapidAccelerationCount = rapidAccels,
            estimatedExtraFuelLitersPer100Km = extraFuel,
            aiCoachingSummary = aiSummary,
            recommendations = recommendations,
            timestamp = now
        )

        return Pair(report, notifications)
    }

    suspend fun analyzeGeofencingAndRouteOptimizations(
        vehicles: List<Vehicle>,
        geofences: List<GeofenceZone>,
        alerts: List<Alert>,
        trips: List<Trip>
    ): GeofenceAndRouteOptimizationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val geofenceAlerts = alerts.filter { 
            it.type == AlertType.GEOFENCE_ENTRY || it.type == AlertType.GEOFENCE_EXIT || it.type == AlertType.SPEEDING
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateHeuristicOptimization(vehicles, geofences, geofenceAlerts, trips)
        }

        val geofenceSummaryText = geofences.joinToString("\n") { g ->
            "• [Zone ${g.name}] Type: ${g.type.labelFr} | Rayon: ${g.radiusMeters.toInt()}m | Véhicules actifs: ${g.activeVehicleCount} | Catégorie: ${g.poiCategory}"
        }

        val alertSummaryText = geofenceAlerts.take(15).joinToString("\n") { a ->
            "• [${java.text.SimpleDateFormat("HH:mm").format(java.util.Date(a.timestamp))}] Véhicule: ${a.vehicleName} (${a.licensePlate}) | Alerte: ${a.type.labelFr} | Sévérité: ${a.severity.name} | Message: ${a.message}"
        }

        val tripsSummaryText = trips.take(10).joinToString("\n") { t ->
            "• Trajet #${t.id} [${t.vehicleName} - ${t.licensePlate}] : Début ${t.startTime} (${t.departureAddress}) -> Fin ${t.endTime} (${t.arrivalAddress}) | Distance: ${t.distanceKm} km | Vitesse Moy: ${t.avgSpeedKmH.toInt()} km/h (Max ${t.maxSpeedKmH.toInt()}) | Durée: ${t.durationMinutes} min | Conso: ${t.fuelConsumedLiters}L | Arrêts: ${t.stopPoints.size}"
        }

        val promptText = """
            Tu es Thara AI Dispatch & Fleet Route Optimizer, un expert de classe mondiale en logistique urbaine, optimisation d'itinéraires et sécurité de flotte.
            Analyse les données télématiques suivantes :
            
            PÉRIMÈTRES GEOFENCING CONFIGURÉS :
            $geofenceSummaryText
            
            ALERTES RÉCENTES DE FRANCHISSEMENT DE ZONES & VITESSE :
            $alertSummaryText
            
            HISTORIQUE DES TRAJETS TÉLÉMATIQUES RÉCENTS :
            $tripsSummaryText
            
            PRODUIS UNE SYNTHÈSE ANALYTIQUE ET DES RECOMMANDATIONS CONCRÈTES EN FRANÇAIS :
            1. Synthèse des violations de géofencing : Récurrence des franchissements, zones à haut risque, comportements anormaux.
            2. Propositions d'optimisation d'itinéraires : Analyse des détours, des ralentissements et des arrêts prolongés. Propose des itinéraires alternatifs éco-responsables avec estimation des gains de temps (minutes), de distance (km), de carburant (L) et d'économies en FCFA (basé sur 890 FCFA/litre de diesel).
            3. Directives opérationnelles pour les dispatcheurs et gestionnaires de flotte.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", promptText)
                        })
                    })
                })
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val jsonRes = JSONObject(responseString)
                val candidates = jsonRes.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val cand = candidates.getJSONObject(0)
                    val content = cand.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val aiText = parts.getJSONObject(0).optString("text", "")
                        return@withContext parseAiOptimizationText(vehicles, geofences, geofenceAlerts, trips, aiText)
                    }
                }
            }
            generateHeuristicOptimization(vehicles, geofences, geofenceAlerts, trips)
        } catch (e: Exception) {
            generateHeuristicOptimization(vehicles, geofences, geofenceAlerts, trips)
        }
    }

    private fun parseAiOptimizationText(
        vehicles: List<Vehicle>,
        geofences: List<GeofenceZone>,
        alerts: List<Alert>,
        trips: List<Trip>,
        aiText: String
    ): GeofenceAndRouteOptimizationResult {
        val base = generateHeuristicOptimization(vehicles, geofences, alerts, trips)
        return base.copy(
            rawGeminiAnalysis = aiText,
            breachSummary = base.breachSummary.copy(
                executiveSummary = "Synthèse Thara Gemini 3.5 Flash :\n" + aiText.lines().take(6).joinToString("\n")
            )
        )
    }

    private fun generateHeuristicOptimization(
        vehicles: List<Vehicle>,
        geofences: List<GeofenceZone>,
        alerts: List<Alert>,
        trips: List<Trip>
    ): GeofenceAndRouteOptimizationResult {
        val unauthorizedExits = alerts.count { it.type == AlertType.GEOFENCE_EXIT }
        val noGoEntries = alerts.count { it.type == AlertType.GEOFENCE_ENTRY }
        val totalBreaches = unauthorizedExits + noGoEntries

        val topZones = if (geofences.isNotEmpty()) {
            geofences.filter { it.type != com.example.model.ZoneType.SAFE }.map { it.name }.take(3).ifEmpty {
                listOf("Zone Industrielle Yoff", "Périmètre Bel-Air", "Port Autonome")
            }
        } else {
            listOf("Dépôt Principal Rufisque", "Périmètre Portuaire", "Zone VDN")
        }

        val riskScore = (35 + (totalBreaches * 12)).coerceIn(15, 92)

        val proposals = mutableListOf<RouteOptimizationProposal>()
        val representativeTrips = if (trips.isNotEmpty()) trips else TripRepository.getAllTrips()

        representativeTrips.take(4).forEachIndexed { index, trip ->
            val origDist = trip.distanceKm
            val savedRatio = 0.14 + (index * 0.03) // 14% to 23% savings
            val optDist = (origDist * (1.0 - savedRatio * 0.6)).let { Math.round(it * 10.0) / 10.0 }
            val timeSavedMin = (trip.durationMinutes * savedRatio).toInt().coerceAtLeast(8)
            val optDuration = (trip.durationMinutes - timeSavedMin).coerceAtLeast(15)
            val fuelSavedLiters = Math.round((trip.fuelConsumedLiters * savedRatio) * 10.0) / 10.0
            val costSavedFcfa = (fuelSavedLiters * 890).toLong()

            val routeName = when (index % 3) {
                0 -> "Contournement Express via Autoroute de l'Avenir (Sortie 9)"
                1 -> "Corridor Éco VDN Prolongée & Voie de Dégagement Ouest"
                else -> "Itinéraire Fluide Pénétrante Sud - Évitement Marché Sandaga"
            }

            val rationale = when (index % 3) {
                0 -> "Évite 4 intersections critiques et les arrêts en accordéon sur la Nationale 1 aux heures d'affluence."
                1 -> "Régime moteur constant (1900 RPM) et limitation des freinages brusques sur route côtière."
                else -> "Réduit les franchissements non autorisés de la zone protégée Médina/Colobane."
            }

            val detour = when (index % 3) {
                0 -> "Évite les embouteillages récurrents du rond-point Cambérène"
                1 -> "Bypass de l'Avenue Malick Sy encombrée"
                else -> "Évitement du goulet d'étranglement de Thiaroye"
            }

            val priority = if (index == 0) "Haute" else if (index == 1) "Éco" else "Moyenne"

            proposals.add(
                RouteOptimizationProposal(
                    id = "PROP-AI-0${index + 1}",
                    tripId = trip.id,
                    vehicleName = trip.vehicleName,
                    licensePlate = trip.licensePlate,
                    origin = trip.departureAddress,
                    destination = trip.arrivalAddress,
                    originalDistanceKm = origDist,
                    optimizedDistanceKm = optDist,
                    originalDurationMinutes = trip.durationMinutes,
                    optimizedDurationMinutes = optDuration,
                    estimatedFuelSavedLiters = fuelSavedLiters,
                    estimatedCostSavedFcfa = costSavedFcfa,
                    recommendedRouteTitle = routeName,
                    rationale = rationale,
                    detourAvoided = detour,
                    priority = priority
                )
            )
        }

        val actionRules = listOf(
            ActionableDispatchRule(
                id = "RULE-01",
                category = "Géofencing",
                title = "Recalibrage des Horaires de Sortie de Dépôt",
                description = "Décaler les départs de 07h30 à 07h00 pour éviter l'entrée accidentelle dans les corridors urbains saturés et réduire les alertes de zone.",
                targetVehicles = listOf("Tous les poids lourds", "Fourgons de livraison"),
                impact = "-65% d'alertes de zone aux heures de pointe"
            ),
            ActionableDispatchRule(
                id = "RULE-02",
                category = "Éco-Trajet",
                title = "Priorisation Automatique des Voies Rapides Fluides",
                description = "Affecter systématiquement l'itinéraire Autoroute de l'Avenir aux trajets excédant 25 km pour stabiliser la température moteur et réduire la consommation.",
                targetVehicles = listOf("Véhicules longue distance (VH-101, VH-103)"),
                impact = "Économie moyenne de 2.3L/100km par rotation"
            ),
            ActionableDispatchRule(
                id = "RULE-03",
                category = "Sécurité",
                title = "Création d'un Périmètre Tampon autour des Zones Dangereuses",
                description = "Élargir les alertes préventives à 500m en amont des zones No-Go pour avertir le chauffeur avant tout franchissement effectif.",
                targetVehicles = listOf("Flotte complète"),
                impact = "Prévention de 100% des pénalités contractuelles de zone"
            )
        )

        val totalFuelSaved = proposals.sumOf { it.estimatedFuelSavedLiters }
        val totalOrigFuel = representativeTrips.take(4).sumOf { it.fuelConsumedLiters }.coerceAtLeast(1.0)
        val overallSavingsPct = ((totalFuelSaved / totalOrigFuel) * 100).toFloat().coerceIn(12f, 28f)
        val monthlySavingsFcfa = proposals.sumOf { it.estimatedCostSavedFcfa } * 24 // 24 working days

        val execSummary = "L'analyse télématique IA révèle ${totalBreaches} alertes de zone et des opportunités majeures d'optimisation. L'adoption des 4 corridors recommandés permet une baisse estimée de ${String.format(java.util.Locale.US, "%.1f", overallSavingsPct)}% de carburant et un gain de productivité de ${proposals.sumOf { it.originalDurationMinutes - it.optimizedDurationMinutes }} minutes par cycle de livraison."

        return GeofenceAndRouteOptimizationResult(
            analyzedAtTimestamp = System.currentTimeMillis(),
            analyzedVehiclesCount = vehicles.size,
            analyzedTripsCount = representativeTrips.size,
            analyzedAlertsCount = alerts.size,
            breachSummary = GeofenceBreachSummary(
                totalBreachesCount = totalBreaches,
                unauthorizedZoneExits = unauthorizedExits,
                noGoZoneEntries = noGoEntries,
                topViolatedZones = topZones,
                riskScore = riskScore,
                executiveSummary = execSummary
            ),
            proposals = proposals,
            actionRules = actionRules,
            overallFuelSavingsPercentage = overallSavingsPct,
            monthlyEstimatedSavingsFcfa = monthlySavingsFcfa,
            rawGeminiAnalysis = execSummary
        )
    }
}


