package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FleetOperationalStatus
import com.example.model.FirestoreFleetVehicle
import com.example.ui.theme.TharaCardBorder
import com.example.ui.theme.TharaRed
import com.example.ui.theme.TharaRedLight
import com.example.ui.theme.TharaTextMuted
import com.example.ui.theme.TharaTextPrimary
import com.example.ui.theme.TharaTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Filter categories for fleet vehicle operational statuses.
 */
enum class FleetFilterCategory(val label: String) {
    ALL("Tous"),
    ACTIVE("🟢 Actifs"),
    IDLE("🟠 Au Ralenti"),
    LOW_FUEL("🔴 Carburant Bas")
}

/**
 * Modern Jetpack Compose UI component that renders a list of fleet vehicles
 * fetched from Firestore, with live operational status badges, fuel indicators,
 * filters (Active, Idle, Low Fuel), search, and fast action triggers.
 */
@Composable
fun FirestoreFleetVehicleList(
    vehicles: List<FirestoreFleetVehicle>,
    isLiveFirestoreSync: Boolean = true,
    onVehicleClick: (FirestoreFleetVehicle) -> Unit = {},
    onLocateOnMap: (FirestoreFleetVehicle) -> Unit = {},
    onOpenDiagnostics: (FirestoreFleetVehicle) -> Unit = {},
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(FleetFilterCategory.ALL) }

    // Aggregate counts
    val totalCount = vehicles.size
    val activeCount = vehicles.count { it.operationalStatus == FleetOperationalStatus.ACTIVE }
    val idleCount = vehicles.count { it.operationalStatus == FleetOperationalStatus.IDLE }
    val lowFuelCount = vehicles.count { it.operationalStatus == FleetOperationalStatus.LOW_FUEL }

    // Filter vehicles according to search and operational status
    val filteredVehicles = remember(vehicles, searchQuery, selectedFilter) {
        vehicles.filter { vehicle ->
            val matchesSearch = searchQuery.isBlank() ||
                    vehicle.name.contains(searchQuery, ignoreCase = true) ||
                    vehicle.licensePlate.contains(searchQuery, ignoreCase = true) ||
                    vehicle.driverName.contains(searchQuery, ignoreCase = true) ||
                    vehicle.address.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                FleetFilterCategory.ALL -> true
                FleetFilterCategory.ACTIVE -> vehicle.operationalStatus == FleetOperationalStatus.ACTIVE
                FleetFilterCategory.IDLE -> vehicle.operationalStatus == FleetOperationalStatus.IDLE
                FleetFilterCategory.LOW_FUEL -> vehicle.operationalStatus == FleetOperationalStatus.LOW_FUEL
            }

            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("firestore_fleet_vehicle_list")
    ) {
        // Top Firestore Sync Status & Stats Banner
        FirestoreSyncStatusBar(
            isLiveSync = isLiveFirestoreSync,
            totalVehicles = totalCount,
            activeCount = activeCount,
            idleCount = idleCount,
            lowFuelCount = lowFuelCount,
            onRefresh = onRefresh
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("firestore_vehicle_search_input"),
            placeholder = {
                Text("Rechercher véhicule, chauffeur, immatriculation...", fontSize = 13.sp, color = TharaTextMuted)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TharaTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.testTag("clear_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Effacer la recherche",
                            tint = TharaTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = TharaRed,
                unfocusedBorderColor = TharaCardBorder
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                StatusFilterChip(
                    label = "Tous ($totalCount)",
                    isSelected = selectedFilter == FleetFilterCategory.ALL,
                    onClick = { selectedFilter = FleetFilterCategory.ALL },
                    testTag = "filter_all"
                )
            }
            item {
                StatusFilterChip(
                    label = "Actifs ($activeCount)",
                    isSelected = selectedFilter == FleetFilterCategory.ACTIVE,
                    badgeColor = Color(0xFF16A34A),
                    onClick = { selectedFilter = FleetFilterCategory.ACTIVE },
                    testTag = "filter_active"
                )
            }
            item {
                StatusFilterChip(
                    label = "Ralenti ($idleCount)",
                    isSelected = selectedFilter == FleetFilterCategory.IDLE,
                    badgeColor = Color(0xFFD97706),
                    onClick = { selectedFilter = FleetFilterCategory.IDLE },
                    testTag = "filter_idle"
                )
            }
            item {
                StatusFilterChip(
                    label = "Carburant bas ($lowFuelCount)",
                    isSelected = selectedFilter == FleetFilterCategory.LOW_FUEL,
                    badgeColor = Color(0xFFDC2626),
                    onClick = { selectedFilter = FleetFilterCategory.LOW_FUEL },
                    testTag = "filter_low_fuel"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Vehicle Cards List
        if (filteredVehicles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = TharaTextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aucun véhicule correspondant",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TharaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "Modifiez votre recherche ou réinitialisez les filtres." else "Aucun véhicule ne correspond au statut sélectionné.",
                        fontSize = 13.sp,
                        color = TharaTextSecondary,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredVehicles, key = { it.id }) { vehicle ->
                    FirestoreVehicleCard(
                        vehicle = vehicle,
                        onClick = { onVehicleClick(vehicle) },
                        onLocateOnMap = { onLocateOnMap(vehicle) },
                        onOpenDiagnostics = { onOpenDiagnostics(vehicle) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Top Status Bar displaying real-time Firestore sync status and fleet metric counters.
 */
@Composable
private fun FirestoreSyncStatusBar(
    isLiveSync: Boolean,
    totalVehicles: Int,
    activeCount: Int,
    idleCount: Int,
    lowFuelCount: Int,
    onRefresh: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("firestore_sync_status_bar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Firestore Connection Pill + Refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isLiveSync) Color(0xFF22C55E).copy(alpha = pulseAlpha) else Color(0xFFE2E8F0))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLiveSync) "Firestore Cloud Sync • En direct" else "Mode Local / Cache",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("refresh_firestore_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualiser Firestore",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick telemetry metric badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricCounterBadge(label = "Flotte", count = totalVehicles.toString(), color = Color.White)
                MetricCounterBadge(label = "En mouvement", count = activeCount.toString(), color = Color(0xFF4ADE80))
                MetricCounterBadge(label = "Ralenti", count = idleCount.toString(), color = Color(0xFFFBBF24))
                MetricCounterBadge(label = "Réserve", count = lowFuelCount.toString(), color = Color(0xFFF87171))
            }
        }
    }
}

@Composable
private fun MetricCounterBadge(
    label: String,
    count: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF94A3B8)
        )
    }
}

/**
 * Filter Chip with custom styling and testTag support.
 */
@Composable
private fun StatusFilterChip(
    label: String,
    isSelected: Boolean,
    badgeColor: Color? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (badgeColor != null && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TharaTextPrimary
            )
        }
    }
}

/**
 * Individual Vehicle Card presenting real-time vehicle details from Firestore.
 */
@Composable
fun FirestoreVehicleCard(
    vehicle: FirestoreFleetVehicle,
    onClick: () -> Unit,
    onLocateOnMap: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Visual attributes depending on status
    val statusColor: Color
    val statusBg: Color
    val statusLabel: String

    when (vehicle.operationalStatus) {
        FleetOperationalStatus.ACTIVE -> {
            statusColor = Color(0xFF15803D)
            statusBg = Color(0xFFDCFCE7)
            statusLabel = "Actif • ${vehicle.speedKmH.toInt()} km/h"
        }
        FleetOperationalStatus.IDLE -> {
            statusColor = Color(0xFFB45309)
            statusBg = Color(0xFFFEF3C7)
            statusLabel = "Ralenti (Idling)"
        }
        FleetOperationalStatus.LOW_FUEL -> {
            statusColor = Color(0xFFB91C1C)
            statusBg = Color(0xFFFEE2E2)
            statusLabel = "Carburant bas (${vehicle.fuelLevelPct}%)"
        }
        FleetOperationalStatus.STOPPED -> {
            statusColor = Color(0xFF475569)
            statusBg = Color(0xFFF1F5F9)
            statusLabel = "À l'arrêt"
        }
        FleetOperationalStatus.OFFLINE -> {
            statusColor = Color(0xFF64748B)
            statusBg = Color(0xFFF8FAFC)
            statusLabel = "Hors ligne"
        }
    }

    // Fuel progress bar color
    val fuelProgressColor = when {
        vehicle.fuelLevelPct <= 25 -> Color(0xFFDC2626)
        vehicle.fuelLevelPct <= 50 -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("firestore_vehicle_card_${vehicle.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (vehicle.operationalStatus == FleetOperationalStatus.LOW_FUEL) Color(0xFFFCA5A5) else TharaCardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Card Top Row: Vehicle Name, Plate & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (vehicle.operationalStatus == FleetOperationalStatus.LOW_FUEL) Color(0xFFFEE2E2) else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (vehicle.operationalStatus == FleetOperationalStatus.LOW_FUEL) Icons.Default.Warning else Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = if (vehicle.operationalStatus == FleetOperationalStatus.LOW_FUEL) Color(0xFFDC2626) else TharaRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = vehicle.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TharaTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${vehicle.licensePlate} • ${vehicle.enterpriseName}",
                            fontSize = 12.sp,
                            color = TharaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Operational Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("status_badge_${vehicle.id}")
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Address Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TharaTextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = vehicle.address,
                    fontSize = 12.sp,
                    color = TharaTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fuel Level Gauge & Stats Indicator
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = fuelProgressColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Carburant: ${vehicle.fuelLevelPct}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TharaTextPrimary
                        )
                        if (vehicle.operationalStatus == FleetOperationalStatus.LOW_FUEL) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⚠️ Réserve critique",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }

                    Text(
                        text = "Autonomie ~${vehicle.estimatedRangeKm} km",
                        fontSize = 11.sp,
                        color = TharaTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { vehicle.fuelLevelPct / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = fuelProgressColor,
                    trackColor = Color(0xFFE2E8F0)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Driver & Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Driver Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = vehicle.driverName.take(1).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TharaTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = vehicle.driverName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TharaTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Call Driver Button
                    if (vehicle.driverPhone.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${vehicle.driverPhone}"))
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("call_driver_${vehicle.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Appeler chauffeur",
                                tint = TharaTextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Locate On Map Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEFF6FF))
                            .clickable { onLocateOnMap() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("locate_vehicle_${vehicle.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Localiser",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Carte",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }
            }
        }
    }
}
