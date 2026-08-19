package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GeofenceZone
import com.example.model.Vehicle
import com.example.model.VehicleStatus
import com.example.ui.components.OfflineMapTileCacheSheet
import com.example.ui.components.OpenStreetMapView
import com.example.ui.components.PhoneTrackingAuthorizationSheet
import com.example.ui.components.SatelliteHybridWebViewMap
import com.example.ui.components.VehicleDetailBottomSheet
import com.example.ui.components.VehicleRealTimeStatusOverlay
import com.example.ui.components.ZoneConfigurationSheet
import com.example.ui.screens.LocationMapScreen
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Red500
import com.example.ui.theme.TharaRed
import com.example.util.MapTileCacheManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    vehicles: List<Vehicle>,
    geofences: List<GeofenceZone>,
    selectedVehicle: Vehicle?,
    statusFilter: VehicleStatus?,
    onSelectVehicle: (Vehicle) -> Unit,
    onCloseVehicleSheet: () -> Unit,
    onStatusFilterSelected: (VehicleStatus?) -> Unit,
    onToggleLock: (String) -> Unit,
    isDarkTheme: Boolean,
    onNavigateToTrips: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showZoneConfigSheet by remember { mutableStateOf(false) }
    var isSatelliteMode by remember { mutableStateOf(true) }
    var isDarkMode by remember { mutableStateOf(false) }
    var isTrafficMode by remember { mutableStateOf(false) }
    var isOnlyUserVehicle by remember { mutableStateOf(false) }
    var isPhoneTrackingAuthorized by remember { mutableStateOf(true) }
    var phoneImei by remember { mutableStateOf("358941098421033") }
    var showPhoneTrackingSheet by remember { mutableStateOf(false) }
    var useOsmdroidMap by remember { mutableStateOf(false) }
    var showOfflineCacheSheet by remember { mutableStateOf(false) }
    var isLiveLocationPoiMode by remember { mutableStateOf(false) }

    if (isLiveLocationPoiMode) {
        Box(modifier = modifier.fillMaxSize()) {
            LocationMapScreen(modifier = Modifier.fillMaxSize())

            // Floating Switcher Back to Fleet Telematics
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { isLiveLocationPoiMode = false }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🛰️ Retour Flotte",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .testTag("map_screen")
        ) {
        if (useOsmdroidMap) {
            // Osmdroid Native Offline Map View with persistent tile caching
            OpenStreetMapView(
                vehicles = if (isOnlyUserVehicle && selectedVehicle != null) listOf(selectedVehicle) else vehicles,
                geofences = geofences,
                selectedVehicle = selectedVehicle,
                onSelectVehicle = onSelectVehicle,
                isDarkMode = isDarkMode || isDarkTheme,
                isTrafficEnabled = isTrafficMode,
                onOpenCacheSheet = { showOfflineCacheSheet = true },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Interactive Google Maps View (Satellite Hybrid / Plan Vectoriel / Relief)
            SatelliteHybridWebViewMap(
                vehicles = vehicles,
                geofences = geofences,
                selectedVehicle = selectedVehicle,
                onSelectVehicle = onSelectVehicle,
                showOnlyUserVehicle = isOnlyUserVehicle,
                isPhoneTrackingAuthorized = isPhoneTrackingAuthorized,
                phoneLat = 3.863000,
                phoneLng = 11.538000,
                phoneImei = phoneImei,
                isDarkMode = isDarkMode,
                isSatelliteMode = isSatelliteMode
            )
        }

        // Top Controls Column (Filters + Satellite Mode Toggle & Smartphone Auth Pill)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            // Status Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChipPill(
                    label = if (isOnlyUserVehicle) "Mon Véhicule (1)" else "Tous (${vehicles.size})",
                    isSelected = statusFilter == null,
                    onClick = { onStatusFilterSelected(null) },
                    testTag = "filter_status_all"
                )

                FilterChipPill(
                    label = "En Mouvement",
                    isSelected = statusFilter == VehicleStatus.MOVING,
                    onClick = { onStatusFilterSelected(VehicleStatus.MOVING) },
                    activeColor = Emerald400,
                    testTag = "filter_status_moving"
                )

                FilterChipPill(
                    label = "En Arrêt",
                    isSelected = statusFilter == VehicleStatus.STOPPED,
                    onClick = { onStatusFilterSelected(VehicleStatus.STOPPED) },
                    activeColor = Cyan400,
                    testTag = "filter_status_stopped"
                )

                FilterChipPill(
                    label = "Alerte Zone",
                    isSelected = statusFilter == VehicleStatus.ALERT_GEOFENCE,
                    onClick = { onStatusFilterSelected(VehicleStatus.ALERT_GEOFENCE) },
                    activeColor = Red500,
                    testTag = "filter_status_alert"
                )
            }

            // Connectivity Warning Banner for Offline / Stale Vehicles
            val currentTime = System.currentTimeMillis()
            val offlineOrStaleVehicles = vehicles.filter { v ->
                v.status == VehicleStatus.OFFLINE || (currentTime - v.lastUpdateTimestamp) > 5 * 60 * 1000L
            }

            if (offlineOrStaleVehicles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .testTag("connectivity_warning_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.92f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚠️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${offlineOrStaleVehicles.size} véhicule(s) hors ligne / données de localisation obsolètes",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = offlineOrStaleVehicles.joinToString(" • ") { "${it.name.take(16)} (${((currentTime - it.lastUpdateTimestamp) / 60000L)}m)" },
                                fontSize = 10.sp,
                                color = Color(0xFFFCA5A5)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Map Mode & Smartphone Traçage Floating Selectors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Smartphone Traçage & IMEI pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xCC8B5CF6))
                        .clickable { showPhoneTrackingSheet = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("open_phone_tracking_pill")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isOnlyUserVehicle) "📱 Mon Véhicule & Phone" else "📱 Traçage Smartphone",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isPhoneTrackingAuthorized) Emerald400 else Color.LightGray)
                        )
                    }
                }

                // Map Engine & Layer Toggles (Osmdroid Offline, Satellite, Mode Nuit, Plan)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location & POI Module Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF10B981))
                            .clickable { isLiveLocationPoiMode = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("open_live_location_poi_mode_button")
                    ) {
                        Text(
                            text = "📍 Suivi & POIs",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Offline Tile Cache Manager Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xEE0284C7))
                            .clickable { showOfflineCacheSheet = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("open_offline_cache_manager_button")
                    ) {
                        Text(
                            text = "💾 Cache",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Osmdroid Engine Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (useOsmdroidMap) Color(0xFF10B981) else Color(0xCC0F172A))
                            .clickable { useOsmdroidMap = !useOsmdroidMap }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("toggle_osmdroid_map")
                    ) {
                        Text(
                            text = if (useOsmdroidMap) "⚡ Osmdroid" else "🗺️ OSM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Real-Time Traffic Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isTrafficMode) Color(0xEEEAB308) else Color(0xCC0F172A))
                            .clickable {
                                isTrafficMode = !isTrafficMode
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("traffic_mode_toggle")
                    ) {
                        Text(text = "🚦 Trafic", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isTrafficMode) Color.Black else Color.White)
                    }

                    // Satellite Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSatelliteMode && !isDarkMode && !isTrafficMode && !useOsmdroidMap) Color(0xEE0284C7) else Color(0xCC0F172A))
                            .clickable {
                                useOsmdroidMap = false
                                isSatelliteMode = true
                                isDarkMode = false
                                isTrafficMode = false
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("satellite_mode_toggle")
                    ) {
                        Text(text = "🛰️ Sat", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Zone Configuration Floating Button (Bottom End)
        if (!showZoneConfigSheet && selectedVehicle == null) {
            FloatingActionButton(
                onClick = { showZoneConfigSheet = true },
                containerColor = TharaRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("open_zone_config_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EditLocation,
                        contentDescription = "Zone Configuration",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Zone Config",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Selected Vehicle Bottom Sheet Layer
        selectedVehicle?.let { vehicle ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                VehicleDetailBottomSheet(
                    vehicle = vehicle,
                    onClose = onCloseVehicleSheet,
                    onToggleLock = { onToggleLock(vehicle.id) },
                    onOpenTripHistory = onNavigateToTrips
                )
            }
        }

        // Zone Configuration Bottom Sheet Modal
        if (showZoneConfigSheet) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                ZoneConfigurationSheet(
                    initialZoneName = "Downtown Sector",
                    initialRadiusMeters = 1500f,
                    onRadiusChanged = { },
                    onSaveZone = { name, radius ->
                        showZoneConfigSheet = false
                    },
                    onClose = { showZoneConfigSheet = false }
                )
            }
        }

        // Phone Tracking & Focus Authorization Bottom Sheet Modal
        if (showPhoneTrackingSheet) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                PhoneTrackingAuthorizationSheet(
                    isOnlyUserVehicle = isOnlyUserVehicle,
                    onToggleOnlyUserVehicle = { isOnlyUserVehicle = it },
                    isPhoneTrackingAuthorized = isPhoneTrackingAuthorized,
                    onTogglePhoneTracking = { isPhoneTrackingAuthorized = it },
                    phoneImei = phoneImei,
                    onImeiChanged = { phoneImei = it },
                    distanceToVehicleKm = 0.62f,
                    onClose = { showPhoneTrackingSheet = false }
                )
            }
        }

        // Real-time Status Indicator Component for connection quality and sync timestamp
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 90.dp)
        ) {
            VehicleRealTimeStatusOverlay(vehicle = selectedVehicle ?: vehicles.firstOrNull())
        }

        // Offline Map Tile Cache Management Bottom Sheet Modal
        if (showOfflineCacheSheet) {
            OfflineMapTileCacheSheet(
                activeVehicle = selectedVehicle ?: vehicles.firstOrNull(),
                vehicles = vehicles,
                onDismiss = { showOfflineCacheSheet = false },
                isDarkTheme = isDarkTheme
            )
        }
    }
}
}

@Composable
private fun FilterChipPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    activeColor: Color = Cyan400,
    testTag: String
) {
    Box(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) activeColor else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
        )
    }
}
