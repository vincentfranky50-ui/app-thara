package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GeofenceAndRouteOptimizationResult
import com.example.model.RouteOptimizationProposal
import com.example.model.Vehicle
import com.example.ui.theme.*

@Composable
fun RouteOptimizationScreen(
    vehicles: List<Vehicle>,
    selectedVehicle: Vehicle?,
    onSelectVehicle: (Vehicle) -> Unit,
    onBack: () -> Unit,
    optimizationResult: GeofenceAndRouteOptimizationResult? = null,
    isAnalyzing: Boolean = false,
    onRunAnalysis: (() -> Unit)? = null,
    onApplyProposal: ((RouteOptimizationProposal) -> Unit)? = null,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    var departurePoint by remember { mutableStateOf("Port de Dakar (Terminal Conteneurs)") }
    var destinationPoint by remember { mutableStateOf("Hub Logistique Diamniadio") }
    var activeSubTab by remember { mutableStateOf(0) } // 0: Trajets Recommandés, 1: Synthèse Géofencing, 2: Plan Dispatcheur
    var appliedIds by remember { mutableStateOf(setOf<String>()) }
    var actionToast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(actionToast) {
        if (actionToast != null) {
            kotlinx.coroutines.delay(2500)
            actionToast = null
        }
    }

    LaunchedEffect(Unit) {
        if (optimizationResult == null) {
            onRunAnalysis?.invoke()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("route_optimization_screen")
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Optimisation IA & Géofences",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Cyan500.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Gemini 3.5",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cyan500,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Algorithme prédictif de carburant, alertes géofence & trajets optimaux",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { onRunAnalysis?.invoke() },
                enabled = !isAnalyzing,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Cyan500.copy(alpha = 0.15f))
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Cyan500)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Recalculer", tint = Cyan400)
                }
            }
        }

        // Action Toast
        if (actionToast != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Emerald500.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald500, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(actionToast ?: "", color = Emerald500, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Vehicle Selector Pill
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, TharaCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Véhicule de référence :",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    vehicles.take(3).forEach { v ->
                        val isSelected = selectedVehicle?.id == v.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectVehicle(v) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${v.name} (${v.licensePlate})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Summary Metric Grid
        if (optimizationResult != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, TharaCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Risque Zone", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${optimizationResult.breachSummary.riskScore}/100", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (optimizationResult.breachSummary.riskScore > 50) TharaRed else Emerald400)
                        Text("${optimizationResult.breachSummary.totalBreachesCount} alertes", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, TharaCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Gain Conso", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("-${String.format(java.util.Locale.US, "%.1f", optimizationResult.overallFuelSavingsPercentage)}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Emerald400)
                        Text("${optimizationResult.proposals.sumOf { it.estimatedFuelSavedLiters }}L économisés", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, TharaCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Économie Flotte", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format(java.util.Locale.FRANCE, "%,d", optimizationResult.monthlyEstimatedSavingsFcfa)} F", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Cyan400)
                        Text("/ mois estimé", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Subtabs Selector
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, TharaCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    "🗺️ Trajets IA" to 0,
                    "🛡️ Géofencing" to 1,
                    "📋 Plan Dispatcheur" to 2
                ).forEach { (label, idx) ->
                    val isSelected = activeSubTab == idx
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeSubTab = idx },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        border = if (isSelected) BorderStroke(1.dp, TharaCardBorder) else null
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Cyan400 else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Cyan500)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Calcul des corridors optimaux et synthèse des alertes...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else if (optimizationResult != null) {
            when (activeSubTab) {
                0 -> {
                    // Proposals Tab
                    optimizationResult.proposals.forEach { proposal ->
                        val isApplied = appliedIds.contains(proposal.id)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isApplied) Emerald500.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = BorderStroke(1.dp, if (isApplied) Emerald500.copy(alpha = 0.5f) else TharaCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AltRoute, contentDescription = null, tint = Cyan400, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${proposal.vehicleName} (${proposal.licensePlate})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (proposal.priority == "Haute") TharaRed.copy(alpha = 0.15f) else Cyan500.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Priorité ${proposal.priority}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (proposal.priority == "Haute") TharaRed else Cyan500,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TripOrigin, contentDescription = null, tint = Emerald400, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(proposal.origin, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TharaRed, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(proposal.destination, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, TharaCardBorder)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "✨ ${proposal.recommendedRouteTitle}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Cyan400
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = proposal.rationale,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "🛡️ ${proposal.detourAvoided}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Emerald400
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Durée estimée", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("${proposal.originalDurationMinutes}m", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(" ➔ ", fontSize = 11.sp, color = Cyan400)
                                            Text("${proposal.optimizedDurationMinutes}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald400)
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Gain Carburant", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "-${proposal.estimatedFuelSavedLiters} L (${proposal.estimatedCostSavedFcfa} FCFA)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald400
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        appliedIds = appliedIds + proposal.id
                                        onApplyProposal?.invoke(proposal)
                                        actionToast = "Itinéraire optimisé affecté à ${proposal.vehicleName} !"
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isApplied) Emerald600 else Cyan500)
                                ) {
                                    Icon(
                                        imageVector = if (isApplied) Icons.Default.Check else Icons.Default.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isApplied) "Itinéraire Transmis au Véhicule" else "Valider & Transmettre au Chauffeur",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Geofencing Summary Tab
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, TharaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = TharaRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Synthèse Exécutive Géofencing",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = optimizationResult.breachSummary.executiveSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Text(
                        text = "Zones Critiques de Franchissement Répété",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    optimizationResult.breachSummary.topViolatedZones.forEach { zoneName ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, TharaCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(TharaRed)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = zoneName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Détour non planifié ou goulot d'étranglement",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = TharaRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Alerte Fréquente",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TharaRed,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Dispatcher Action Plan Tab
                    optimizationResult.actionRules.forEach { rule ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, TharaCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (rule.category) {
                                                "Géofencing" -> Icons.Default.Shield
                                                "Éco-Trajet" -> Icons.Default.LocalGasStation
                                                else -> Icons.Default.Speed
                                            },
                                            contentDescription = null,
                                            tint = Cyan400,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = rule.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Cyan500.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = rule.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Cyan400,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = rule.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, TharaCardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Impact Estimé :",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = rule.impact,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald400
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

