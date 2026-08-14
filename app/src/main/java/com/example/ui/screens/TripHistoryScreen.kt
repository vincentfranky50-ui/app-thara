package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TripRepository
import com.example.model.StopPoint
import com.example.model.Trip
import com.example.model.Vehicle
import com.example.ui.components.TripMapCanvas
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Red500
import com.example.ui.theme.TharaCardBorder
import com.example.ui.theme.TharaRed
import com.example.ui.theme.TharaRedBorder
import com.example.ui.theme.TharaTextMuted
import com.example.ui.theme.TharaTextPrimary
import com.example.ui.theme.TharaTextSecondary

import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GpsFixed
import com.example.model.GeofenceAndRouteOptimizationResult
import com.example.model.RouteOptimizationProposal
import com.example.model.Waypoint
import com.example.ui.components.GeofenceRouteOptimizationSheet

@Composable
fun TripHistoryScreen(
    vehicles: List<Vehicle>,
    selectedVehicleId: String?,
    onSelectVehicle: (String) -> Unit,
    trips: List<Trip> = emptyList(),
    selectedTrip: Trip? = null,
    onSelectTrip: ((Trip) -> Unit)? = null,
    onPushSimulatedTrip: ((Trip) -> Unit)? = null,
    optimizationResult: GeofenceAndRouteOptimizationResult? = null,
    isAnalyzingGeofenceAndRoutes: Boolean = false,
    onRunGeofenceAndRouteOptimization: (() -> Unit)? = null,
    onApplyRouteProposal: ((RouteOptimizationProposal) -> Unit)? = null,
    isDarkTheme: Boolean = false,
    onClose: (() -> Unit)? = null,
    onOpenExportModal: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeVehicle = vehicles.firstOrNull { it.id == selectedVehicleId } ?: vehicles.firstOrNull()

    var vehicleDropdownExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Tous") }
    var showWaypointsList by remember { mutableStateOf(false) }
    var showOptimizationSheet by remember { mutableStateOf(false) }

    val vehicleTrips = remember(activeVehicle?.id, trips) {
        val filtered = if (activeVehicle != null) {
            trips.filter { it.vehicleId == activeVehicle.id }
        } else {
            trips
        }
        if (filtered.isNotEmpty()) {
            filtered
        } else if (activeVehicle != null) {
            TripRepository.getTripsForVehicle(activeVehicle.id)
        } else {
            TripRepository.getAllTrips()
        }
    }

    val filteredTrips = remember(vehicleTrips, selectedFilter) {
        when (selectedFilter) {
            "Aujourd'hui" -> vehicleTrips.filter { it.dateLabel.contains("Aujourd'hui") }
            "Hier" -> vehicleTrips.filter { it.dateLabel.contains("Hier") }
            else -> vehicleTrips
        }
    }

    val currentSelectedTrip = selectedTrip ?: filteredTrips.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("trip_history_screen")
    ) {
        // --- Top Header ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(TharaRed.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                tint = TharaRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Historique des Trajets",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Emerald400,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Firestore Live (${vehicleTrips.size} tracés polylignes)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Emerald400,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Gemini AI Optimizer Action Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(Cyan500, TharaRed)
                                    )
                                )
                                .clickable {
                                    showOptimizationSheet = true
                                    if (optimizationResult == null) {
                                        onRunGeofenceAndRouteOptimization?.invoke()
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("trips_gemini_optimize_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Assistant IA Géofence", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (onOpenExportModal != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TharaRed)
                                    .clickable { onOpenExportModal() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("trips_export_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Exporter", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        if (onClose != null) {
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier
                                    .testTag("close_trip_history_button")
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Vehicle Selector Dropdown Button
                Box(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vehicleDropdownExpanded = true }
                            .testTag("vehicle_selector_dropdown"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TharaCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = TharaRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = activeVehicle?.name ?: "Sélectionner un véhicule",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Immatriculation: ${activeVehicle?.licensePlate ?: "--"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "Changer ▾",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TharaRed
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = vehicleDropdownExpanded,
                        onDismissRequest = { vehicleDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        vehicles.forEach { vehicle ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(vehicle.name, fontWeight = FontWeight.Medium)
                                        Text(vehicle.licensePlate, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TharaTextMuted)
                                    }
                                },
                                onClick = {
                                    onSelectVehicle(vehicle.id)
                                    vehicleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Gemini AI Quick Recommendation Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable {
                            showOptimizationSheet = true
                            if (optimizationResult == null) {
                                onRunGeofenceAndRouteOptimization?.invoke()
                            }
                        }
                        .testTag("gemini_quick_optimization_banner"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Cyan500.copy(alpha = 0.08f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Cyan500.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Cyan500.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Cyan500,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (optimizationResult != null) {
                                        "⚡ Gain potentiel : -${String.format(java.util.Locale.US, "%.1f", optimizationResult.overallFuelSavingsPercentage)}% carburant"
                                    } else {
                                        "Assistant Gemini : Optimiser itinéraires & Géofencing"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (optimizationResult != null) {
                                        "${optimizationResult.proposals.size} itinéraires recommandés • ${optimizationResult.breachSummary.totalBreachesCount} alertes géofence"
                                    } else {
                                        "Touchez pour analyser les alertes de zones et recalculer les tracés optimaux"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Cyan400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Date Filter Chips & Live Simulator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Tous", "Aujourd'hui", "Hier").forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TharaRed,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }

                    if (onPushSimulatedTrip != null && activeVehicle != null) {
                        Button(
                            onClick = {
                                val now = System.currentTimeMillis()
                                val simTrip = Trip(
                                    id = "TRIP-LIVE-${activeVehicle.id}-${now % 10000}",
                                    vehicleId = activeVehicle.id,
                                    vehicleName = activeVehicle.name,
                                    licensePlate = activeVehicle.licensePlate,
                                    dateLabel = "Aujourd'hui, Direct Firestore",
                                    startTime = "14:10",
                                    endTime = "15:45",
                                    departureAddress = "Plateau, Dakar",
                                    arrivalAddress = "Aéroport International Blaise Diagne (AIBD)",
                                    distanceKm = 54.2,
                                    avgSpeedKmH = 68.5f,
                                    maxSpeedKmH = 102.0f,
                                    durationMinutes = 95,
                                    fuelConsumedLiters = 6.4,
                                    stopPoints = listOf(
                                        StopPoint(
                                            id = "SP-LIVE-1",
                                            latitude = 14.7160,
                                            longitude = -17.3800,
                                            address = "Péage Autoroute de l'Avenir",
                                            durationMinutes = 8,
                                            arrivalTime = "14:35",
                                            reason = "Péage & Ticket"
                                        )
                                    ),
                                    waypoints = listOf(
                                        Waypoint(14.6737, -17.4372, 35f, now),
                                        Waypoint(14.6950, -17.4150, 52f, now + 60000),
                                        Waypoint(14.7160, -17.3800, 20f, now + 120000),
                                        Waypoint(14.7350, -17.3100, 88f, now + 180000),
                                        Waypoint(14.7210, -17.2500, 95f, now + 240000),
                                        Waypoint(14.7225, -17.1812, 75f, now + 300000),
                                        Waypoint(14.6710, -17.0730, 102f, now + 360000),
                                        Waypoint(14.6711, -17.0680, 40f, now + 420000)
                                    )
                                )
                                onPushSimulatedTrip(simTrip)
                                onSelectTrip?.invoke(simTrip)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.AddRoad, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Tracé Live", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        if (filteredTrips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aucun trajet enregistré pour cette période.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Trip selector carousel
                item {
                    Text(
                        text = "Sélectionner un Trajet (${filteredTrips.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(filteredTrips, key = { it.id }) { trip ->
                            val isSelected = currentSelectedTrip?.id == trip.id
                            TripSummaryCard(
                                trip = trip,
                                isSelected = isSelected,
                                onClick = {
                                    onSelectTrip?.invoke(trip)
                                }
                            )
                        }
                    }
                }

                currentSelectedTrip?.let { currentTrip ->
                    // Map view container
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(270.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TharaCardBorder)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                TripMapCanvas(
                                    trip = currentTrip,
                                    isDarkTheme = isDarkTheme,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )

                                // Speed Color Legend Bar (Figma Design Pattern)
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Légende Vitesse:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Cyan400))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("<40", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Cyan400)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Emerald400))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("40-60", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Emerald400)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("60-80", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF59E0B))
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Red500))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(">80", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Red500)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Detailed Trajectory Metrics Grid (Vitesse Moyenne, Distance, Durée, Arrêts)
                    item {
                        Text(
                            text = "Analyse & Statistiques du Trajet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Vitesse Moyenne Card (HIGHLIGHTED)
                            MetricDetailCard(
                                icon = Icons.Default.Speed,
                                iconColor = Cyan400,
                                label = "Vitesse Moyenne",
                                value = "${currentTrip.avgSpeedKmH.toInt()} km/h",
                                subtext = "Max: ${currentTrip.maxSpeedKmH.toInt()} km/h",
                                modifier = Modifier.weight(1f)
                            )

                            // Distance
                            MetricDetailCard(
                                icon = Icons.Default.Route,
                                iconColor = TharaRed,
                                label = "Distance Total",
                                value = "${currentTrip.distanceKm} km",
                                subtext = "${currentTrip.durationMinutes} min de route",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Durée
                            MetricDetailCard(
                                icon = Icons.Default.Schedule,
                                iconColor = Emerald400,
                                label = "Créneau Horaire",
                                value = "${currentTrip.startTime} - ${currentTrip.endTime}",
                                subtext = "${currentTrip.dateLabel}",
                                modifier = Modifier.weight(1f)
                            )

                            // Points d'Arrêt
                            MetricDetailCard(
                                icon = Icons.Default.PauseCircle,
                                iconColor = Red500,
                                label = "Points d'Arrêt",
                                value = "${currentTrip.stopPoints.size} Arrêt(s)",
                                subtext = "${currentTrip.stopPoints.sumOf { it.durationMinutes }} min de pause",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Waypoint GPS Telemetry toggle card
                    if (currentTrip.waypoints.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showWaypointsList = !showWaypointsList },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TharaCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.GpsFixed,
                                            contentDescription = null,
                                            tint = Cyan400,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Données Télématiques Polyligne Firestore",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${currentTrip.waypoints.size} coordonnées GPS & vitesses enregistrées",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (showWaypointsList) "Masquer ▲" else "Afficher ▼",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Cyan400
                                    )
                                }
                            }
                        }

                        if (showWaypointsList) {
                            items(currentTrip.waypoints.take(15)) { wp ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TharaCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.FiberManualRecord,
                                                contentDescription = null,
                                                tint = if (wp.speedKmH >= 80f) Red500 else if (wp.speedKmH >= 40f) Emerald400 else Cyan400,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Lat: ${String.format(java.util.Locale.US, "%.4f", wp.latitude)}, Lng: ${String.format(java.util.Locale.US, "%.4f", wp.longitude)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = "${wp.speedKmH.toInt()} km/h",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (wp.speedKmH >= 80f) Red500 else if (wp.speedKmH >= 40f) Emerald400 else Cyan400
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Stop points timeline section
                    if (currentTrip.stopPoints.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Détail des Points d'Arrêt 🛑",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }

                        items(currentTrip.stopPoints, key = { it.id }) { stopPoint ->
                            StopPointCard(stopPoint = stopPoint)
                        }
                    }
                }
            }
        }
    }

    if (showOptimizationSheet) {
        GeofenceRouteOptimizationSheet(
            optimizationResult = optimizationResult,
            isLoading = isAnalyzingGeofenceAndRoutes,
            onRefresh = { onRunGeofenceAndRouteOptimization?.invoke() },
            onDismiss = { showOptimizationSheet = false },
            onApplyProposal = onApplyRouteProposal,
            isDarkTheme = isDarkTheme
        )
    }
}

@Composable
private fun TripSummaryCard(
    trip: Trip,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable { onClick() }
            .testTag("trip_card_${trip.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TharaRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) TharaRed else TharaCardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TharaRed
                )
                Text(
                    text = "${trip.startTime} ➔ ${trip.endTime}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Start -> End locations
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Emerald400, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trip.departureAddress.split(",").firstOrNull() ?: trip.departureAddress,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Red500, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trip.arrivalAddress.split(",").firstOrNull() ?: trip.arrivalAddress,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = TharaCardBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Stats Pills Row (Vitesse Moyenne & Distance)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vitesse Moyenne Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Cyan400.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = Cyan500, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${trip.avgSpeedKmH.toInt()} km/h moy",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Cyan500
                        )
                    }
                }

                // Distance
                Text(
                    text = "${trip.distanceKm} km",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MetricDetailCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, TharaCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StopPointCard(stopPoint: StopPoint) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, TharaCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Red500.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PauseCircle,
                    contentDescription = null,
                    tint = Red500,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stopPoint.reason,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stopPoint.arrivalTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stopPoint.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Red500.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "${stopPoint.durationMinutes} min",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Red500,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
