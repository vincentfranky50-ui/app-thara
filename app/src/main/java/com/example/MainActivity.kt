package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddVehicleDialog
import com.example.ui.components.TharaTab
import com.example.ui.screens.FleetListScreen
import com.example.ui.screens.FleetOverviewScreen
import com.example.ui.screens.GeminiAssistantScreen
import com.example.ui.screens.GeofenceAlertsScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.TripHistoryScreen
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.theme.TharaTheme
import com.example.ui.viewmodel.FleetViewModel

class MainActivity : ComponentActivity() {
    private val fleetViewModel: FleetViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result if needed in future, otherwise just proceed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ask for notifications permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val uiState by fleetViewModel.uiState.collectAsStateWithLifecycle()
            var isAuthenticated by remember { mutableStateOf(false) }
            var currentTab by remember { mutableStateOf(TharaTab.FLEET) }
            var showGeofenceConfig by remember { mutableStateOf(false) }
            var showEngineMaintenance by remember { mutableStateOf(false) }
            var showDriverCoaching by remember { mutableStateOf(false) }
            var showRouteOptimization by remember { mutableStateOf(false) }
            var showExportModal by remember { mutableStateOf(false) }
            var showSettings by remember { mutableStateOf(false) }

            TharaTheme(darkTheme = uiState.isDarkTheme) {
                if (!isAuthenticated) {
                    LoginScreen(
                        onLoginSuccess = { email ->
                            isAuthenticated = true
                        }
                    )
                } else {
                    var showFleetDirectory by remember { mutableStateOf(false) }

                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                        if (showRouteOptimization) {
                            com.example.ui.screens.RouteOptimizationScreen(
                                vehicles = uiState.vehicles,
                                selectedVehicle = uiState.selectedVehicle ?: uiState.vehicles.firstOrNull(),
                                onSelectVehicle = { v -> fleetViewModel.selectVehicle(v.id) },
                                onBack = { showRouteOptimization = false },
                                optimizationResult = uiState.geofenceRouteOptimization,
                                isAnalyzing = uiState.isAnalyzingGeofenceAndRoutes,
                                onRunAnalysis = { fleetViewModel.runGeofenceAndRouteOptimization() },
                                isDarkTheme = uiState.isDarkTheme
                            )
                        } else if (showDriverCoaching) {
                            com.example.ui.screens.DriverCoachingScreen(
                                vehicles = uiState.vehicles,
                                selectedVehicle = uiState.selectedVehicle ?: uiState.vehicles.firstOrNull(),
                                coachingReport = uiState.coachingReport,
                                coachingNotifications = uiState.coachingNotifications,
                                telemetryEvents = uiState.drivingEvents,
                                isAnalyzing = uiState.isAnalyzingBehavior,
                                isDarkTheme = uiState.isDarkTheme,
                                onSelectVehicle = { v ->
                                    fleetViewModel.selectVehicle(v.id)
                                    fleetViewModel.runDrivingBehaviorAnalysis(v)
                                },
                                onRunAnalysis = { v -> fleetViewModel.runDrivingBehaviorAnalysis(v) },
                                onSimulateEvent = { v, evtType -> fleetViewModel.addSimulatedDrivingEvent(v, evtType) },
                                onMarkNotificationRead = { notifId -> fleetViewModel.markCoachingNotificationRead(notifId) }
                            )
                        } else if (showEngineMaintenance) {
                            com.example.ui.screens.EngineMaintenanceScreen(
                                vehicles = uiState.vehicles,
                                selectedVehicle = uiState.selectedVehicleForDiagnostics ?: uiState.vehicles.firstOrNull(),
                                prediction = uiState.maintenancePrediction,
                                isAnalyzing = uiState.isAnalyzingEngineLogs,
                                onSelectVehicle = { v -> fleetViewModel.selectVehicleForDiagnostics(v) },
                                onRunAnalysis = { v -> fleetViewModel.analyzeEngineLogsForVehicle(v) },
                                onBack = { showEngineMaintenance = false },
                                onOpenExportModal = { showExportModal = true }
                            )
                        } else if (showGeofenceConfig) {
                            com.example.ui.screens.GeofenceConfigScreen(
                                geofences = uiState.geofences,
                                alerts = uiState.alerts,
                                onSaveGeofence = { zone ->
                                    fleetViewModel.saveGeofenceZone(zone)
                                    showGeofenceConfig = false
                                },
                                onDeleteGeofence = { zoneId ->
                                    fleetViewModel.deleteGeofenceZone(zoneId)
                                },
                                onTestNotif = { zoneId, isEntry ->
                                    fleetViewModel.triggerManualGeofenceTest(zoneId, uiState.selectedVehicle?.id ?: "VH-101", isEntry)
                                },
                                onBack = { showGeofenceConfig = false }
                            )
                        } else {
                            when (currentTab) {
                                TharaTab.FLEET -> {
                                    FleetOverviewScreen(
                                        vehicles = uiState.filteredVehicles,
                                        geofences = uiState.geofences,
                                        selectedVehicle = uiState.selectedVehicle,
                                        onSelectVehicle = { v -> fleetViewModel.selectVehicle(v.id) },
                                        onNavigateToMap = {
                                            currentTab = TharaTab.MAP
                                        },
                                        onNavigateToVehicleDetail = { vehicleId ->
                                            fleetViewModel.selectVehicle(vehicleId)
                                            currentTab = TharaTab.MAP
                                        },
                                        onNavigateToTrips = {
                                            currentTab = TharaTab.TRIPS
                                        }
                                    )
                                }
                                TharaTab.TRIPS -> {
                                    TripHistoryScreen(
                                        vehicles = uiState.vehicles,
                                        selectedVehicleId = uiState.selectedVehicle?.id,
                                        onSelectVehicle = { vehicleId -> fleetViewModel.selectVehicle(vehicleId) },
                                        trips = uiState.trips,
                                        selectedTrip = uiState.selectedTrip,
                                        onSelectTrip = { trip -> fleetViewModel.selectTrip(trip.id) },
                                        onPushSimulatedTrip = { trip -> fleetViewModel.pushTripToFirestore(trip) },
                                        optimizationResult = uiState.geofenceRouteOptimization,
                                        isAnalyzingGeofenceAndRoutes = uiState.isAnalyzingGeofenceAndRoutes,
                                        onRunGeofenceAndRouteOptimization = { fleetViewModel.runGeofenceAndRouteOptimization() },
                                        isDarkTheme = uiState.isDarkTheme,
                                        onClose = { currentTab = TharaTab.FLEET },
                                        onOpenExportModal = { showExportModal = true }
                                    )
                                }
                                TharaTab.ALERTS -> {
                                    GeofenceAlertsScreen(
                                        geofences = uiState.geofences,
                                        alerts = uiState.alerts,
                                        onAcknowledgeAlert = { alertId -> fleetViewModel.acknowledgeAlert(alertId) },
                                        onOpenGeofenceConfig = { showGeofenceConfig = true },
                                        optimizationResult = uiState.geofenceRouteOptimization,
                                        isAnalyzingGeofenceAndRoutes = uiState.isAnalyzingGeofenceAndRoutes,
                                        onRunGeofenceAndRouteOptimization = { fleetViewModel.runGeofenceAndRouteOptimization() },
                                        isDarkTheme = uiState.isDarkTheme
                                    )
                                }
                                TharaTab.MAP -> {
                                    MapScreen(
                                        vehicles = uiState.filteredVehicles,
                                        geofences = uiState.geofences,
                                        selectedVehicle = uiState.selectedVehicle,
                                        statusFilter = uiState.statusFilter,
                                        onSelectVehicle = { v -> fleetViewModel.selectVehicle(v.id) },
                                        onCloseVehicleSheet = { fleetViewModel.selectVehicle(null) },
                                        onStatusFilterSelected = { status -> fleetViewModel.setStatusFilter(status) },
                                        onToggleLock = { vId -> fleetViewModel.toggleEngineLock(vId) },
                                        isDarkTheme = uiState.isDarkTheme,
                                        onNavigateToTrips = { currentTab = TharaTab.TRIPS }
                                    )
                                }
                                TharaTab.REPORTS -> {
                                    ReportsScreen(
                                        vehicles = uiState.vehicles,
                                        selectedVehicle = uiState.selectedVehicle,
                                        maintenanceResult = uiState.maintenancePrediction,
                                        coachingReport = uiState.coachingReport,
                                        onOpenEngineMaintenance = { showEngineMaintenance = true },
                                        onOpenDriverCoaching = { showDriverCoaching = true },
                                        onOpenRouteOptimization = { showRouteOptimization = true },
                                        onOpenExportModal = { showExportModal = true }
                                    )
                                }
                                TharaTab.PROFILE -> {
                                    if (showSettings) {
                                        com.example.ui.screens.SettingsScreen(
                                            userSettings = uiState.userSettings,
                                            onSaveSettings = { updated ->
                                                fleetViewModel.updateUserSettings(updated)
                                                showSettings = false
                                            },
                                            onBack = { showSettings = false },
                                            isDarkTheme = uiState.isDarkTheme
                                        )
                                    } else if (showFleetDirectory) {
                                        FleetListScreen(
                                            vehicles = uiState.filteredVehicles,
                                            searchQuery = uiState.searchQuery,
                                            onSearchQueryChanged = { query -> fleetViewModel.setSearchQuery(query) },
                                            onSelectVehicle = { v ->
                                                fleetViewModel.selectVehicle(v.id)
                                                currentTab = TharaTab.MAP
                                            },
                                            onToggleLock = { vId -> fleetViewModel.toggleEngineLock(vId) },
                                            onOpenAddVehicleDialog = { fleetViewModel.setAddVehicleDialogOpen(true) }
                                        )
                                    } else {
                                        ProfileScreen(
                                            onNavigateToFleetList = { showFleetDirectory = true },
                                            userSettings = uiState.userSettings,
                                            onOpenSettings = { showSettings = true },
                                            isDarkTheme = uiState.isDarkTheme,
                                            onToggleTheme = { fleetViewModel.toggleTheme() }
                                        )
                                    }
                                }
                            }
                        }

                        if (showExportModal) {
                            com.example.ui.components.ExportModal(
                                vehicles = uiState.vehicles,
                                selectedVehicle = uiState.selectedVehicle,
                                maintenanceResult = uiState.maintenancePrediction,
                                coachingReport = uiState.coachingReport,
                                coachingNotifs = uiState.coachingNotifications,
                                trips = uiState.trips,
                                isDarkTheme = uiState.isDarkTheme,
                                onDismiss = { showExportModal = false }
                            )
                        }

                        if (uiState.isAddVehicleDialogOpen) {
                            AddVehicleDialog(
                                onDismiss = { fleetViewModel.setAddVehicleDialogOpen(false) },
                                onSubmit = { name, plate, imei, driver, phone ->
                                    fleetViewModel.createVehicle(name, plate, imei, driver, phone)
                                },
                                errorMessage = uiState.errorMessage
                            )
                        }

                        if (uiState.isSyncing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 8.dp,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(48.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Synchronisation Firebase en cours...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
