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
    selectedVehicle: Vehicle? = null,
    onSelectVehicle: ((Vehicle) -> Unit)? = null,
    onNavigateToMap: () -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit,
    onNavigateToTrips: (() -> Unit)? = null,
    onOpenGeofenceConfig: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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

            // Bell / Notification Button with Badge "99"
            Box {
                FloatingCircleButton(
                    icon = Icons.Default.Notifications,
                    contentDescription = "Alertes",
                    onClick = {
                        showMessageCenter = true
                    }
                )
                // Red Badge "99"
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color(0xFFD32F2F))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "99",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
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

    // MODAL 1: VEHICLE DIRECTORY / SELECTOR SHEET (BAANOOL IOT HIERARCHY)
    if (showVehicleDirectorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showVehicleDirectorySheet = false },
            sheetState = bottomSheetState
        ) {
            var searchQuery by remember { mutableStateOf("") }
            var selectedTab by remember { mutableStateOf(0) } // 0: Total, 1: En ligne, 2: Hors ligne

            val groups = listOf(
                Pair("admin", 0),
                Pair("CUSTOMER", 16),
                Pair("ARMEL MFOSSI", 7),
                Pair("prince fashion", 0),
                Pair("willy car", 5)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Numéro de l'appareil / numéro...", fontSize = 14.sp, color = Color.Gray) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Tabs Row: Total (31) | En ligne (15) | Hors ligne (...)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf("Total (31)", "En ligne (15)", "Hors ligne (... )")
                    tabs.forEachIndexed { index, tabTitle ->
                        val isSelected = selectedTab == index
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedTab = index }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = tabTitle,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF10B981) else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(2.dp)
                                    .background(if (isSelected) Color(0xFF10B981) else Color.Transparent)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Hierarchy Groups List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    groups.forEach { (groupName, count) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(context, "Groupe $groupName sélectionné ($count véhicules)", Toast.LENGTH_SHORT).show()
                                    showVehicleDirectorySheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$groupName ($count)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Ouvrir",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Also list vehicles if search query matches or user wants direct vehicle access
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Véhicules Directs",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    vehicles.forEach { v ->
                        val (dotColor, statusLabel) = when (v.status) {
                            VehicleStatus.MOVING -> Pair(Color(0xFF22C55E), "En mouvement")
                            VehicleStatus.IDLE -> Pair(Color(0xFFF59E0B), "Ralenti")
                            VehicleStatus.STOPPED -> Pair(Color(0xFF64748B), "À l'arrêt")
                            VehicleStatus.OFFLINE -> Pair(Color(0xFF94A3B8), "Hors ligne")
                            VehicleStatus.ALERT_GEOFENCE -> Pair(Color(0xFFEF4444), "Alerte Zone")
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (v.id == activeVehicle.id) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectVehicle?.invoke(v)
                                    showVehicleDirectorySheet = false
                                    showTelemetryDetailsSheet = true
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = v.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "${v.licensePlate} • ${v.speedKmH.toInt()} km/h", fontSize = 12.sp, color = Color.Gray)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = statusLabel, fontSize = 11.sp, color = dotColor, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // MODAL: MESSAGE CENTER (NOTIFICATION BELL)
    if (showMessageCenter) {
        com.example.ui.components.MessageCenterBottomSheet(
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

