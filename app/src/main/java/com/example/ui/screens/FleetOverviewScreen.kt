package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GeofenceZone
import com.example.model.Vehicle
import com.example.model.FirestoreFleetVehicle
import com.example.ui.components.FirestoreFleetVehicleList
import com.example.model.VehicleStatus
import com.example.ui.components.OfflineMapTileCacheSheet
import com.example.ui.components.OpenStreetMapView
import com.example.ui.components.SatelliteHybridWebViewMap
import com.example.ui.components.VehicleRealTimeStatusOverlay
import com.example.ui.theme.TharaCardBorder
import com.example.ui.theme.TharaRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetOverviewScreen(
    vehicles: List<Vehicle> = emptyList(),
    geofences: List<GeofenceZone> = emptyList(),
    alerts: List<com.example.model.Alert> = emptyList(),
    selectedVehicle: Vehicle? = null,
    onSelectVehicle: ((Vehicle) -> Unit)? = null,
    onNavigateToMap: () -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit,
    onNavigateToTrips: (() -> Unit)? = null,
    onOpenGeofenceConfig: (() -> Unit)? = null,
    onOpenAiAssistant: (() -> Unit)? = null,
    onAcknowledgeAlert: ((String) -> Unit)? = null,
    onClearAllAlerts: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val unreadAlertsCount = remember(alerts) { alerts.count { !it.acknowledged } }

    // Default vehicle: Rav4(NDJESSA) or selected vehicle
    val activeVehicle = selectedVehicle ?: vehicles.firstOrNull() ?: Vehicle(
        id = "VH-RAV4",
        name = "Rav4(NDJESSA)",
        licensePlate = "LT-894-AK",
        imei = "358941098421033",
        driverName = "Vincent Franky",
        driverPhone = "+237690000000",
        status = VehicleStatus.MOVING,
        speedKmH = 48.0f,
        batteryPct = 94,
        fuelLevelPct = 82,
        latitude = 3.863940,
        longitude = 11.540120,
        heading = 120.0f,
        enterpriseId = "ENT-001",
        enterpriseName = "Transport Sarl",
        lastUpdateTimestamp = System.currentTimeMillis(),
        address = "Yaoundé, Cameroun",
        ignitionOn = true
    )

    // UI States for Modals and Toggles
    var isArmed by remember { mutableStateOf(true) }
    var isEngineLocked by remember { mutableStateOf(false) }
    var isSatelliteMode by remember { mutableStateOf(true) }
    var useOsmdroidMap by remember { mutableStateOf(false) }
    var showOfflineCacheSheet by remember { mutableStateOf(false) }
    var isBluetoothConnected by remember { mutableStateOf(true) }
    var isBottomPanelExpanded by remember { mutableStateOf(true) }
    var showVehicleDirectorySheet by remember { mutableStateOf(false) }
    var showMessageCenter by remember { mutableStateOf(false) }
    var showControlModal by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }
    var showTelemetryDetailsSheet by remember { mutableStateOf(false) }
    var showExtraPlusSheet by remember { mutableStateOf(false) }
    var showGeofencePopupMenu by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showStatisticalReports by remember { mutableStateOf(false) }
    var showControlInstructions by remember { mutableStateOf(false) }
    var showToastMessage by remember { mutableStateOf<String?>(null) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("fleet_live_tracking_screen")
    ) {
        if (useOsmdroidMap) {
            // Osmdroid Native Map Canvas with Offline Tile Caching
            OpenStreetMapView(
                vehicles = if (vehicles.isEmpty()) listOf(activeVehicle) else vehicles,
                geofences = geofences,
                selectedVehicle = activeVehicle,
                onSelectVehicle = { v -> onSelectVehicle?.invoke(v) },
                isDarkMode = false,
                isTrafficEnabled = false,
                onOpenCacheSheet = { showOfflineCacheSheet = true },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // 1. FULLSCREEN INTERACTIVE GOOGLE MAP CANVAS
            SatelliteHybridWebViewMap(
                vehicles = if (vehicles.isEmpty()) listOf(activeVehicle) else vehicles,
                geofences = geofences,
                selectedVehicle = activeVehicle,
                onSelectVehicle = { v -> onSelectVehicle?.invoke(v) },
                showOnlyUserVehicle = false,
                isPhoneTrackingAuthorized = true,
                phoneLat = activeVehicle.latitude,
                phoneLng = activeVehicle.longitude,
                phoneImei = "358941098421033",
                isDarkMode = false,
                isSatelliteMode = isSatelliteMode,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. TOP FLOATING ACTION BAR (LEFT SIDE)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hamburger Menu Button (≡)
            FloatingCircleButton(
                icon = Icons.Default.Menu,
                contentDescription = "Menu",
                onClick = { showVehicleDirectorySheet = true }
            )

            // Fullscreen Frame Button (⛶)
            FloatingCircleButton(
                icon = Icons.Default.CropFree,
                contentDescription = "Plein Écran",
                onClick = {
                    Toast.makeText(context, "Mode Plein Écran Carte Activé", Toast.LENGTH_SHORT).show()
                }
            )

            // Bell / Notification Button with dynamic unread Badge (0 = no badge)
            Box {
                FloatingCircleButton(
                    icon = Icons.Default.Notifications,
                    contentDescription = "Alertes",
                    onClick = {
                        showMessageCenter = true
                    }
                )
                // Red Badge only shown if notifications > 0 (zero by default)
                if (unreadAlertsCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(Color(0xFFD32F2F))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (unreadAlertsCount > 99) "99+" else unreadAlertsCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. TOP FLOATING VERTICAL STACK (RIGHT SIDE)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // AI Copilot Circle Button (Red / AutoAwesome)
            FloatingCircleButton(
                icon = Icons.Default.AutoAwesome,
                contentDescription = "Thara AI Copilot",
                containerColor = TharaRed,
                iconColor = Color.White,
                onClick = {
                    onOpenAiAssistant?.invoke()
                }
            )

            // Profile Circle Button (Blue)
            FloatingCircleButton(
                icon = Icons.Default.Person,
                contentDescription = "Profil Conducteur",
                containerColor = Color(0xFF0288D1),
                iconColor = Color.White,
                onClick = {
                    showProfileDialog = true
                }
            )

            // Walking / Person Circle Button (Light Blue)
            FloatingCircleButton(
                icon = Icons.Default.DirectionsWalk,
                contentDescription = "Mode Piéton / Distance",
                containerColor = Color(0xFF03A9F4),
                iconColor = Color.White,
                onClick = {
                    Toast.makeText(context, "Mode Suivi Piéton : 12m du véhicule", Toast.LENGTH_SHORT).show()
                }
            )

        }

        // 4. LOWER RIGHT QUICK ACTION CONTROLS (Floating above the bottom sheet)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 250.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bluetooth Circle Button (Blue)
            FloatingCircleButton(
                icon = Icons.Default.Bluetooth,
                contentDescription = "Connexion Bluetooth OBD-II",
                containerColor = if (isBluetoothConnected) Color(0xFF1976D2) else Color(0xFF757575),
                iconColor = Color.White,
                onClick = {
                    isBluetoothConnected = !isBluetoothConnected
                    Toast.makeText(
                        context,
                        if (isBluetoothConnected) "OBD-II Bluetooth Connecté" else "Bluetooth Déconnecté",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            // Crosshair / Target Button (White)
            FloatingCircleButton(
                icon = Icons.Default.MyLocation,
                contentDescription = "Recentrer sur le véhicule",
                containerColor = Color.White,
                iconColor = Color(0xFF212121),
                onClick = {
                    Toast.makeText(context, "Position Recentrée sur ${activeVehicle.name}", Toast.LENGTH_SHORT).show()
                }
            )

            // Offline Tile Cache Manager Button (Cyan)
            FloatingCircleButton(
                icon = Icons.Default.CloudDownload,
                contentDescription = "Cache Hors-Ligne Osmdroid",
                containerColor = Color(0xFF0F172A),
                iconColor = Color(0xFF00E5FF),
                onClick = {
                    showOfflineCacheSheet = true
                }
            )

            // Satellite Layer Toggle Button (White)
            FloatingCircleButton(
                icon = Icons.Default.Satellite,
                contentDescription = "Basculer Mode Carte",
                containerColor = Color.White,
                iconColor = if (isSatelliteMode && !useOsmdroidMap) Color(0xFF1976D2) else Color(0xFF616161),
                onClick = {
                    if (useOsmdroidMap) {
                        useOsmdroidMap = false
                        isSatelliteMode = true
                    } else {
                        isSatelliteMode = !isSatelliteMode
                    }
                    Toast.makeText(
                        context,
                        if (isSatelliteMode) "Mode Satellite Google Maps" else "Mode Plan Vectoriel Google Maps",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        // Real-time Status Indicator Component for connection quality and sync timestamp
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 220.dp)
        ) {
            VehicleRealTimeStatusOverlay(vehicle = activeVehicle)
        }

        // 5. BOTTOM FLOATING SHEET ("Rav4(NDJESSA)") - COLLAPSIBLE / EXPANDABLE
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                .testTag("vehicle_floating_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xAA0F172A), // Dark slate glassmorphism
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .clickable { isBottomPanelExpanded = !isBottomPanelExpanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag Handle / Toggle Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0x66FFFFFF))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title Row: Vehicle Icon + Name + Collapse/Expand Arrow + Google Watermark
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isBottomPanelExpanded = !isBottomPanelExpanded }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x3338BDF8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = activeVehicle.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isBottomPanelExpanded) "Appuyer pour baisser / réduire" else "Souléver pour voir tous les dispositifs",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = if (isBottomPanelExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Google Watermark Text & Dispositifs button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { showVehicleDirectorySheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(text = "Dispositifs", fontSize = 11.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0x99FFFFFF)
                        )
                    }
                }

                if (isBottomPanelExpanded) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // STREAMLINED ACTION BAR (Position, Contrôle, Géorepérage, Trajectoire)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        GridActionButton(
                            title = "Position",
                            icon = Icons.Default.LocationOn,
                            backgroundColor = Color(0xFF1E88E5),
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Position GPS : ${activeVehicle.latitude}° N, ${activeVehicle.longitude}° E (${activeVehicle.address})",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )

                        GridActionButton(
                            title = "Contrôle",
                            icon = Icons.Default.Tune,
                            backgroundColor = Color(0xFFEF5350),
                            onClick = { showControlModal = true }
                        )

                        GridActionButton(
                            title = "Géorepérage",
                            icon = Icons.Default.GridOn,
                            backgroundColor = Color(0xFFFFA726),
                            onClick = { onOpenGeofenceConfig?.invoke() }
                        )

                        GridActionButton(
                            title = "Trajectoire",
                            icon = Icons.Default.AltRoute,
                            backgroundColor = Color(0xFF2979FF),
                            onClick = { onNavigateToTrips?.invoke() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // MODAL 1: VEHICLE DIRECTORY / SELECTOR SHEET (FIRESTORE REAL-TIME VEHICLE LIST)
    if (showVehicleDirectorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showVehicleDirectorySheet = false },
            sheetState = bottomSheetState
        ) {
            val firestoreFleetVehicles = remember(vehicles) {
                vehicles.map { v ->
                    FirestoreFleetVehicle(
                        id = v.id,
                        name = v.name,
                        licensePlate = v.licensePlate,
                        imei = v.imei,
                        driverName = v.driverName,
                        driverPhone = v.driverPhone,
                        speedKmH = v.speedKmH,
                        fuelLevelPct = v.fuelLevelPct,
                        batteryPct = v.batteryPct,
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
                        lockState = v.lockState,
                        statusRaw = v.status.name
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
                    .padding(top = 8.dp)
            ) {
                FirestoreFleetVehicleList(
                    vehicles = firestoreFleetVehicles,
                    isLiveFirestoreSync = true,
                    onVehicleClick = { fv ->
                        val matchedVehicle = vehicles.find { it.id == fv.id }
                        if (matchedVehicle != null) {
                            onSelectVehicle?.invoke(matchedVehicle)
                        }
                        showVehicleDirectorySheet = false
                        showTelemetryDetailsSheet = true
                    },
                    onLocateOnMap = { fv ->
                        val matchedVehicle = vehicles.find { it.id == fv.id }
                        if (matchedVehicle != null) {
                            onSelectVehicle?.invoke(matchedVehicle)
                        }
                        showVehicleDirectorySheet = false
                    },
                    onOpenDiagnostics = { fv ->
                        onNavigateToVehicleDetail(fv.id)
                        showVehicleDirectorySheet = false
                    },
                    onRefresh = {
                        Toast.makeText(context, "Actualisation Firestore réussie", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // MODAL: MESSAGE CENTER (NOTIFICATION BELL)
    if (showMessageCenter) {
        com.example.ui.components.MessageCenterBottomSheet(
            alerts = alerts,
            onAcknowledgeAlert = onAcknowledgeAlert,
            onClearAllAlerts = onClearAllAlerts,
            onDismiss = { showMessageCenter = false }
        )
    }

    // MODAL 2: CONTROL MODAL
    if (showControlModal) {
        com.example.ui.components.ControlBottomSheet(
            onDismiss = { showControlModal = false }
        )
    }

    // MODAL 3: VEHICLE DETAILS SHEET
    if (showTelemetryDetailsSheet) {
        com.example.ui.components.VehicleDetailBottomSheet(
            vehicle = activeVehicle,
            onDismiss = { showTelemetryDetailsSheet = false }
        )
    }



    // MODAL 5: SETTINGS MODAL
    if (showSettingsModal) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsModal = false },
            sheetState = bottomSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Paramètres du Suivi • ${activeVehicle.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Alerte de Survitesse (110 km/h)", fontSize = 14.sp)
                    androidx.compose.material3.Switch(checked = true, onCheckedChange = {})
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Notification de Sortie de Zone", fontSize = 14.sp)
                    androidx.compose.material3.Switch(checked = true, onCheckedChange = {})
                }
            }
        }
    }

    // PROFILE DIALOG POPUP
    if (showProfileDialog) {
        com.example.ui.components.UserProfileDialog(
            onDismiss = { showProfileDialog = false }
        )
    }

    // STATISTICAL REPORTS SCREEN DIALOG
    if (showStatisticalReports) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showStatisticalReports = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                StatisticalReportScreen(
                    onBack = { showStatisticalReports = false }
                )
            }
        }
    }

    // CONTROL INSTRUCTIONS SCREEN DIALOG
    if (showControlInstructions) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showControlInstructions = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                ControlInstructionsScreen(
                    onBack = { showControlInstructions = false }
                )
            }
        }
    }

    // OFFLINE MAP TILE CACHE MANAGEMENT BOTTOM SHEET
    if (showOfflineCacheSheet) {
        OfflineMapTileCacheSheet(
            activeVehicle = activeVehicle,
            vehicles = vehicles,
            onDismiss = { showOfflineCacheSheet = false },
            isDarkTheme = false
        )
    }
}

// HELPER COMPOSABLE: FLOATING CIRCULAR BUTTON
@Composable
private fun FloatingCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    containerColor: Color = Color.White,
    iconColor: Color = Color(0xFF212121),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

// HELPER COMPOSABLE: GRID ACTION BUTTON (2x4 GRID)
@Composable
private fun GridActionButton(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

