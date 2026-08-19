package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiFleetService
import com.example.data.db.TharaDatabase
import com.example.data.repository.FleetRepository
import com.example.model.Alert
import com.example.model.AlertChannelPreferences
import com.example.model.DistanceUnit
import com.example.model.DriverCoachingReport
import com.example.model.DrivingBehaviorMock
import com.example.model.DrivingEventType
import com.example.model.DrivingTelemetryEvent
import com.example.model.EngineTelemetryMock
import com.example.model.GeofenceZone
import com.example.model.InAppCoachingNotification
import com.example.model.MaintenancePredictionResult
import com.example.model.SpeedUnit
import com.example.model.StopPoint
import com.example.model.Trip
import com.example.model.UserMeasurementPreferences
import com.example.model.UserRole
import com.example.model.UserSession
import com.example.model.UserSettings
import com.example.model.Vehicle
import com.example.model.VehicleStatus
import com.example.model.Waypoint
import com.example.model.GeofenceAndRouteOptimizationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FleetUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val filteredVehicles: List<Vehicle> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val selectedVehicleForDiagnostics: Vehicle? = null,
    val maintenancePrediction: MaintenancePredictionResult? = null,
    val isAnalyzingEngineLogs: Boolean = false,
    val alerts: List<Alert> = emptyList(),
    val geofences: List<GeofenceZone> = emptyList(),
    val trips: List<Trip> = emptyList(),
    val selectedTrip: Trip? = null,
    val searchQuery: String = "",
    val statusFilter: VehicleStatus? = null,
    val enterpriseFilter: String? = null,
    val userSession: UserSession = UserSession(),
    val isDarkTheme: Boolean = false,
    val isAiAnalyzing: Boolean = false,
    val aiResponseText: String? = null,
    val isAddVehicleDialogOpen: Boolean = false,
    val errorMessage: String? = null,
    val isSyncing: Boolean = true,
    val drivingEvents: List<DrivingTelemetryEvent> = emptyList(),
    val coachingReport: DriverCoachingReport? = null,
    val coachingNotifications: List<InAppCoachingNotification> = emptyList(),
    val isAnalyzingBehavior: Boolean = false,
    val geofenceRouteOptimization: GeofenceAndRouteOptimizationResult? = null,
    val isAnalyzingGeofenceAndRoutes: Boolean = false,
    val userSettings: UserSettings = UserSettings(),
    val chatMessages: List<com.example.model.AiChatMessage> = emptyList(),
    val isAiChatLoading: Boolean = false
)

class FleetViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TharaDatabase.getDatabase(application)
    private val repository = FleetRepository(database.fleetDao(), viewModelScope, application)
    private val geminiService = GeminiFleetService()

    private val _chatMessages = MutableStateFlow<List<com.example.model.AiChatMessage>>(
        listOf(
            com.example.model.AiChatMessage(
                sender = com.example.model.MessageSender.ASSISTANT,
                text = "👋 Bonjour ! Je suis **Thara AI Copilot**, votre assistant télématique propulsé par Gemini 3.5 Flash.\n\nPosez-moi des questions en langage naturel sur votre flotte, par exemple :\n• *\"Quels camions sont au ralenti (idling) ?\"*\n• *\"Y a-t-il des véhicules en réserve de carburant ?\"*\n• *\"Qui dépasse les limites de vitesse actuellement ?\"*",
                suggestedActions = listOf(
                    "Quels camions sont au ralenti ?",
                    "Y a-t-il des véhicules en réserve de carburant ?",
                    "Qui est en excès de vitesse ?",
                    "Bilan général de la flotte"
                )
            )
        )
    )
    private val _isAiChatLoading = MutableStateFlow(false)

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<VehicleStatus?>(null)
    private val _enterpriseFilter = MutableStateFlow<String?>(null)
    private val _selectedVehicleId = MutableStateFlow<String?>(null)
    private val _selectedTripId = MutableStateFlow<String?>(null)
    private val _userSession = MutableStateFlow(UserSession())
    private val prefs = application.getSharedPreferences("thara_fleet_prefs", Context.MODE_PRIVATE)
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", false))
    private val _isAiAnalyzing = MutableStateFlow(false)
    private val _aiResponseText = MutableStateFlow<String?>(null)
    private val _isAddVehicleDialogOpen = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isSyncing = MutableStateFlow(true)

    private val _selectedVehicleForDiagnostics = MutableStateFlow<Vehicle?>(null)
    private val _maintenancePrediction = MutableStateFlow<MaintenancePredictionResult?>(null)
    private val _isAnalyzingEngineLogs = MutableStateFlow(false)

    private val _drivingEvents = MutableStateFlow<List<DrivingTelemetryEvent>>(emptyList())
    private val _coachingReport = MutableStateFlow<DriverCoachingReport?>(null)
    private val _coachingNotifications = MutableStateFlow<List<InAppCoachingNotification>>(emptyList())
    private val _isAnalyzingBehavior = MutableStateFlow(false)

    private val _geofenceRouteOptimization = MutableStateFlow<GeofenceAndRouteOptimizationResult?>(null)
    private val _isAnalyzingGeofenceAndRoutes = MutableStateFlow(false)

    // User Settings & Alert/Unit Preferences
    private val _userSettings = MutableStateFlow(
        UserSettings(
            alertPreferences = AlertChannelPreferences(
                emailCriticalAlerts = prefs.getBoolean("email_critical", true),
                emailWeeklyDigest = prefs.getBoolean("email_weekly", true),
                emailGeofenceBreach = prefs.getBoolean("email_geofence", true),
                emailMaintenanceDue = prefs.getBoolean("email_maint", false),
                pushCriticalAlerts = prefs.getBoolean("push_critical", true),
                pushGeofenceBreach = prefs.getBoolean("push_geofence", true),
                pushSpeedingAlerts = prefs.getBoolean("push_speeding", true),
                pushEngineIgnition = prefs.getBoolean("push_ignition", false),
                smsEmergencyOnly = prefs.getBoolean("sms_emergency", true)
            ),
            measurementPreferences = UserMeasurementPreferences(
                speedUnit = if (prefs.getString("speed_unit", "KMH") == "MPH") SpeedUnit.MPH else SpeedUnit.KMH,
                distanceUnit = if (prefs.getString("dist_unit", "KM") == "MILES") DistanceUnit.MILES else DistanceUnit.KM
            ),
            emailRecipient = prefs.getString("email_recipient", "vincentfranky50@gmail.com") ?: "vincentfranky50@gmail.com"
        )
    )

    init {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            _isSyncing.value = false
        }
    }

    val uiState: StateFlow<FleetUiState> = combine(
        combine(
            repository.vehiclesFlow,
            repository.alertsFlow,
            repository.geofencesFlow,
            repository.tripsFlow,
            _searchQuery,
            _statusFilter,
            _enterpriseFilter,
            _selectedVehicleId,
            _selectedTripId,
            _userSession
        ) { args -> args },
        combine(
            _isDarkTheme,
            _isAiAnalyzing,
            _aiResponseText,
            _isAddVehicleDialogOpen,
            _errorMessage,
            _isSyncing,
            _selectedVehicleForDiagnostics,
            _maintenancePrediction,
            _isAnalyzingEngineLogs,
            _drivingEvents
        ) { args -> args },
        combine(
            _coachingReport,
            _coachingNotifications,
            _isAnalyzingBehavior,
            _geofenceRouteOptimization,
            _isAnalyzingGeofenceAndRoutes,
            _userSettings,
            _chatMessages,
            _isAiChatLoading
        ) { args -> args }
    ) { group1, group2, group3 ->
        val vehicles = group1[0] as List<Vehicle>
        val alerts = group1[1] as List<Alert>
        val geofences = group1[2] as List<GeofenceZone>
        val allTrips = group1[3] as List<Trip>
        val query = group1[4] as String
        val status = group1[5] as VehicleStatus?
        val enterprise = group1[6] as String?
        val selectedId = group1[7] as String?
        val selectedTripId = group1[8] as String?
        val session = group1[9] as UserSession

        val darkTheme = group2[0] as Boolean
        val aiAnalyzing = group2[1] as Boolean
        val aiResponse = group2[2] as String?
        val addDialogOpen = group2[3] as Boolean
        val error = group2[4] as String?
        val syncing = group2[5] as Boolean
        val diagVehicle = group2[6] as Vehicle?
        val prediction = group2[7] as MaintenancePredictionResult?
        val analyzingLogs = group2[8] as Boolean
        val drivingEvts = group2[9] as List<DrivingTelemetryEvent>

        val coachRep = group3[0] as DriverCoachingReport?
        val coachNotifs = group3[1] as List<InAppCoachingNotification>
        val analyzingBehavior = group3[2] as Boolean
        val geofenceOpt = group3[3] as GeofenceAndRouteOptimizationResult?
        val analyzingGeofenceRoutes = group3[4] as Boolean
        val settings = group3[5] as UserSettings
        val chatMsgs = group3[6] as List<com.example.model.AiChatMessage>
        val chatLoading = group3[7] as Boolean

        val filtered = vehicles.filter { v ->
            val matchesQuery = query.isBlank() ||
                    v.name.contains(query, ignoreCase = true) ||
                    v.licensePlate.contains(query, ignoreCase = true) ||
                    v.driverName.contains(query, ignoreCase = true) ||
                    v.imei.contains(query, ignoreCase = true)

            val matchesStatus = status == null || v.status == status

            val matchesRoleEnterprise = if (session.role == UserRole.SUPER_ADMIN) {
                enterprise == null || v.enterpriseId == enterprise
            } else {
                v.enterpriseId == session.enterpriseId
            }

            matchesQuery && matchesStatus && matchesRoleEnterprise
        }

        val selected = vehicles.find { it.id == selectedId } ?: vehicles.firstOrNull()
        val tripsForActiveVehicle = if (selected != null) {
            allTrips.filter { it.vehicleId == selected.id }
        } else {
            allTrips
        }
        val selectedTrip = tripsForActiveVehicle.find { it.id == selectedTripId } ?: tripsForActiveVehicle.firstOrNull()

        FleetUiState(
            vehicles = vehicles,
            filteredVehicles = filtered,
            selectedVehicle = selected,
            selectedVehicleForDiagnostics = diagVehicle,
            maintenancePrediction = prediction,
            isAnalyzingEngineLogs = analyzingLogs,
            alerts = alerts,
            geofences = geofences,
            trips = tripsForActiveVehicle,
            selectedTrip = selectedTrip,
            searchQuery = query,
            statusFilter = status,
            enterpriseFilter = enterprise,
            userSession = session,
            isDarkTheme = darkTheme,
            isAiAnalyzing = aiAnalyzing,
            aiResponseText = aiResponse,
            isAddVehicleDialogOpen = addDialogOpen,
            errorMessage = error,
            isSyncing = syncing,
            drivingEvents = drivingEvts,
            coachingReport = coachRep,
            coachingNotifications = coachNotifs,
            isAnalyzingBehavior = analyzingBehavior,
            geofenceRouteOptimization = geofenceOpt,
            isAnalyzingGeofenceAndRoutes = analyzingGeofenceRoutes,
            userSettings = settings,
            chatMessages = chatMsgs,
            isAiChatLoading = chatLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FleetUiState()
    )

    fun updateUserSettings(newSettings: UserSettings) {
        _userSettings.value = newSettings
        prefs.edit()
            .putBoolean("email_critical", newSettings.alertPreferences.emailCriticalAlerts)
            .putBoolean("email_weekly", newSettings.alertPreferences.emailWeeklyDigest)
            .putBoolean("email_geofence", newSettings.alertPreferences.emailGeofenceBreach)
            .putBoolean("email_maint", newSettings.alertPreferences.emailMaintenanceDue)
            .putBoolean("push_critical", newSettings.alertPreferences.pushCriticalAlerts)
            .putBoolean("push_geofence", newSettings.alertPreferences.pushGeofenceBreach)
            .putBoolean("push_speeding", newSettings.alertPreferences.pushSpeedingAlerts)
            .putBoolean("push_ignition", newSettings.alertPreferences.pushEngineIgnition)
            .putBoolean("sms_emergency", newSettings.alertPreferences.smsEmergencyOnly)
            .putString("speed_unit", newSettings.measurementPreferences.speedUnit.name)
            .putString("dist_unit", newSettings.measurementPreferences.distanceUnit.name)
            .putString("email_recipient", newSettings.emailRecipient)
            .apply()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: VehicleStatus?) {
        _statusFilter.value = if (_statusFilter.value == status) null else status
    }

    fun setEnterpriseFilter(enterpriseId: String?) {
        _enterpriseFilter.value = enterpriseId
    }

    fun selectVehicle(vehicleId: String?) {
        _selectedVehicleId.value = vehicleId
    }

    fun toggleTheme() {
        val nextValue = !_isDarkTheme.value
        _isDarkTheme.value = nextValue
        prefs.edit().putBoolean("is_dark_theme", nextValue).apply()
    }

    fun setRole(role: UserRole) {
        _userSession.value = _userSession.value.copy(
            role = role,
            enterpriseId = if (role == UserRole.ENTERPRISE_ADMIN) "ENT-01" else "ENT-ALL"
        )
    }

    fun toggleEngineLock(vehicleId: String) {
        viewModelScope.launch {
            repository.toggleEngineLock(vehicleId)
        }
    }

    fun acknowledgeAlert(alertId: String) {
        viewModelScope.launch {
            repository.acknowledgeAlert(alertId)
        }
    }

    fun clearAllAlerts() {
        viewModelScope.launch {
            repository.clearAllAlerts()
        }
    }

    fun setAddVehicleDialogOpen(open: Boolean) {
        _isAddVehicleDialogOpen.value = open
        _errorMessage.value = null
    }

    fun createVehicle(
        name: String,
        licensePlate: String,
        imei: String,
        driverName: String,
        driverPhone: String
    ) {
        viewModelScope.launch {
            val session = _userSession.value
            val result = repository.addNewVehicle(
                name = name,
                licensePlate = licensePlate,
                imei = imei,
                driverName = driverName,
                driverPhone = driverPhone,
                enterpriseId = session.enterpriseId,
                enterpriseName = session.enterpriseName
            )
            result.onSuccess {
                _isAddVehicleDialogOpen.value = false
            }.onFailure { ex ->
                _errorMessage.value = ex.message
            }
        }
    }

    fun sendMessageToAssistant(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) return
        val userMsg = com.example.model.AiChatMessage(
            sender = com.example.model.MessageSender.USER,
            text = trimmed,
            timestamp = System.currentTimeMillis()
        )
        val currentHistory = _chatMessages.value + userMsg
        _chatMessages.value = currentHistory
        _isAiChatLoading.value = true

        viewModelScope.launch {
            try {
                val currentVehicles = repository.vehiclesFlow.first()
                val (aiText, referencedCards) = geminiService.chatWithFleetAssistant(
                    history = currentHistory,
                    vehicles = currentVehicles,
                    userPrompt = trimmed
                )
                val assistantMsg = com.example.model.AiChatMessage(
                    sender = com.example.model.MessageSender.ASSISTANT,
                    text = aiText,
                    timestamp = System.currentTimeMillis(),
                    referencedVehicles = referencedCards,
                    status = com.example.model.MessageStatus.SENT
                )
                _chatMessages.value = _chatMessages.value + assistantMsg
            } catch (e: Exception) {
                val errorMsg = com.example.model.AiChatMessage(
                    sender = com.example.model.MessageSender.ASSISTANT,
                    text = "⚠️ Une erreur est survenue lors de la communication avec Thara AI : ${e.localizedMessage ?: "Vérifiez votre connexion internet."}",
                    status = com.example.model.MessageStatus.ERROR
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            } finally {
                _isAiChatLoading.value = false
            }
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = listOf(
            com.example.model.AiChatMessage(
                sender = com.example.model.MessageSender.ASSISTANT,
                text = "💬 Historique de conversation réinitialisé. Comment puis-je vous aider avec votre flotte aujourd'hui ?",
                suggestedActions = listOf(
                    "Quels camions sont au ralenti ?",
                    "Y a-t-il des véhicules en réserve de carburant ?",
                    "Qui est en excès de vitesse ?",
                    "Synthèse globale de la flotte"
                )
            )
        )
    }

    fun askGeminiAssistant(prompt: String) {
        sendMessageToAssistant(prompt)
    }

    fun clearAiResponse() {
        _aiResponseText.value = null
    }

    fun saveGeofenceZone(zone: GeofenceZone) {
        viewModelScope.launch {
            repository.saveGeofenceZone(zone)
        }
    }

    fun deleteGeofenceZone(zoneId: String) {
        viewModelScope.launch {
            repository.deleteGeofenceZone(zoneId)
        }
    }

    fun triggerManualGeofenceTest(zoneId: String, vehicleId: String, isEntry: Boolean) {
        viewModelScope.launch {
            repository.triggerManualGeofenceTest(zoneId, vehicleId, isEntry)
        }
    }

    fun selectVehicleForDiagnostics(vehicle: Vehicle?) {
        _selectedVehicleForDiagnostics.value = vehicle
        if (vehicle != null) {
            analyzeEngineLogsForVehicle(vehicle)
        } else {
            _maintenancePrediction.value = null
        }
    }

    fun analyzeEngineLogsForVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            _isAnalyzingEngineLogs.value = true
            _selectedVehicleForDiagnostics.value = vehicle
            val historicalLogs = EngineTelemetryMock.generateHistoricalLogs(vehicle)
            val prediction = geminiService.analyzeEngineTelemetryLogs(vehicle, historicalLogs)
            _maintenancePrediction.value = prediction
            _isAnalyzingEngineLogs.value = false
        }
    }

    fun runDrivingBehaviorAnalysis(vehicle: Vehicle) {
        viewModelScope.launch {
            _isAnalyzingBehavior.value = true
            var currentEvents = _drivingEvents.value
            if (currentEvents.isEmpty() || currentEvents.firstOrNull()?.vehicleId != vehicle.id) {
                currentEvents = DrivingBehaviorMock.generateTelemetryEvents(vehicle)
                _drivingEvents.value = currentEvents
            }
            val (report, notifications) = geminiService.analyzeDrivingBehavior(vehicle, currentEvents)
            _coachingReport.value = report
            _coachingNotifications.value = notifications
            _isAnalyzingBehavior.value = false
        }
    }

    fun markCoachingNotificationRead(id: String) {
        _coachingNotifications.value = _coachingNotifications.value.map { n ->
            if (n.id == id) n.copy(isRead = true) else n
        }
    }

    fun addSimulatedDrivingEvent(vehicle: Vehicle, eventType: DrivingEventType) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newEvent = DrivingTelemetryEvent(
                id = "SIM-${vehicle.id}-$now",
                vehicleId = vehicle.id,
                driverName = vehicle.driverName,
                eventType = eventType,
                gForce = if (eventType == DrivingEventType.SUDDEN_BRAKING) -0.82f else 0.71f,
                speedBeforeKmH = 85f,
                speedAfterKmH = 25f,
                locationName = "Simulateur Télématique - Voie Rapide Dakar",
                timestamp = now,
                contextualRiskFactor = "Événement simulé en temps réel par l'utilisateur"
            )
            val updatedEvents = listOf(newEvent) + _drivingEvents.value
            _drivingEvents.value = updatedEvents
            runDrivingBehaviorAnalysis(vehicle)
        }
    }

    fun selectTrip(tripId: String?) {
        _selectedTripId.value = tripId
    }

    fun pushTripToFirestore(trip: Trip) {
        viewModelScope.launch {
            repository.firestoreRepository.pushTripToFirestore(trip)
        }
    }

    fun runGeofenceAndRouteOptimization() {
        viewModelScope.launch {
            _isAnalyzingGeofenceAndRoutes.value = true
            val currentVehicles = repository.vehiclesFlow.first()
            val currentGeofences = repository.geofencesFlow.first()
            val currentAlerts = repository.alertsFlow.first()
            val currentTrips = repository.tripsFlow.first()
            val result = geminiService.analyzeGeofencingAndRouteOptimizations(
                vehicles = currentVehicles,
                geofences = currentGeofences,
                alerts = currentAlerts,
                trips = currentTrips
            )
            _geofenceRouteOptimization.value = result
            _isAnalyzingGeofenceAndRoutes.value = false
        }
    }
}
