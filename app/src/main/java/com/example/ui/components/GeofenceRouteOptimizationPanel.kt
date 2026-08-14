package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionableDispatchRule
import com.example.model.GeofenceAndRouteOptimizationResult
import com.example.model.RouteOptimizationProposal
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeofenceRouteOptimizationSheet(
    optimizationResult: GeofenceAndRouteOptimizationResult?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
    onApplyProposal: ((RouteOptimizationProposal) -> Unit)? = null,
    isDarkTheme: Boolean = false
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(44.dp)
                    .height(4.dp),
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            ) {}
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("geofence_route_optimization_sheet")
    ) {
        GeofenceRouteOptimizationPanel(
            optimizationResult = optimizationResult,
            isLoading = isLoading,
            onRefresh = onRefresh,
            onClose = onDismiss,
            onApplyProposal = onApplyProposal,
            isDarkTheme = isDarkTheme,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        )
    }
}

@Composable
fun GeofenceRouteOptimizationPanel(
    optimizationResult: GeofenceAndRouteOptimizationResult?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onClose: (() -> Unit)? = null,
    onApplyProposal: ((RouteOptimizationProposal) -> Unit)? = null,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Synthèse Géofencing, 1: Optimisation Trajets, 2: Plan d'Action
    var appliedProposals by remember { mutableStateOf(setOf<String>()) }
    var toastFeedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toastFeedback) {
        if (toastFeedback != null) {
            kotlinx.coroutines.delay(3000)
            toastFeedback = null
        }
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 18.dp)
    ) {
        // Top Header Bar (Figma High-End styling)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Cyan500, TharaRed)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Gemini Assistant Fleet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Cyan500.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "3.5 Flash",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cyan500,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Synthèse Géofences & Optimisation d'Itinéraires",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Cyan500)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                    }
                }

                if (onClose != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Toast Feedback Notice
        AnimatedVisibility(visible = toastFeedback != null, enter = fadeIn(), exit = fadeOut()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(10.dp),
                color = Emerald500.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald500, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = toastFeedback ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = Emerald500,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (isLoading && optimizationResult == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Cyan500)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analyse en cours par Gemini 3.5 Flash...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Corrélation des polylignes, alertes de zone et consommation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (optimizationResult != null) {
            // KPI Summary Grid (4 Modern cards)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Score Risque Géofencing
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, TharaCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = TharaRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Risque Zone", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${optimizationResult.breachSummary.riskScore}/100",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (optimizationResult.breachSummary.riskScore > 60) TharaRed else Emerald400
                        )
                        Text(
                            text = "${optimizationResult.breachSummary.totalBreachesCount} violations",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Gain Carburant
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, TharaCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Emerald400, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gain Conso", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "-${String.format(java.util.Locale.US, "%.1f", optimizationResult.overallFuelSavingsPercentage)}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Emerald400
                        )
                        Text(
                            text = "${optimizationResult.proposals.sumOf { it.estimatedFuelSavedLiters }}L sauvés",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Économie Mensuelle
                Surface(
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, TharaCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = Cyan400, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gain / Mois", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(java.util.Locale.FRANCE, "%,d", optimizationResult.monthlyEstimatedSavingsFcfa)} F",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Cyan400
                        )
                        Text(
                            text = "Sur base 890 F/L",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Segmented Tab Selector
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
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
                        "🛡️ Géofencing" to 0,
                        "🗺️ Trajets IA (${optimizationResult.proposals.size})" to 1,
                        "📋 Directives" to 2
                    ).forEach { (label, idx) ->
                        val isSelected = selectedTab == idx
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = idx },
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
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Cyan400 else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Content Body Based on Selected Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 0: Synthèse Géofencing
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                                border = BorderStroke(1.dp, TharaCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Synthèse Exécutive Géofencing Thara AI",
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
                        }

                        item {
                            Text(
                                text = "Zones Critiques de Franchissement Répété",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        items(optimizationResult.breachSummary.topViolatedZones) { zoneName ->
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

                    1 -> {
                        // TAB 1: Optimisation des Itinéraires
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Itinéraires Recommandés par l'IA",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Basé sur les polylignes GPS",
                                    fontSize = 11.sp,
                                    color = Cyan400
                                )
                            }
                        }

                        items(optimizationResult.proposals, key = { it.id }) { proposal ->
                            val isApplied = appliedProposals.contains(proposal.id)

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isApplied) Emerald500.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isApplied) Emerald500.copy(alpha = 0.5f) else TharaCardBorder
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.AltRoute,
                                                contentDescription = null,
                                                tint = Cyan400,
                                                modifier = Modifier.size(18.dp)
                                            )
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

                                    // Origin & Destination
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TripOrigin, contentDescription = null, tint = Emerald400, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(proposal.origin, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = TharaRed, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(proposal.destination, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Recommended Route Box
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, TharaCardBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Cyan400, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = proposal.recommendedRouteTitle,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Cyan400
                                                )
                                            }
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

                                    // Metrics before vs after
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Temps & Distance", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("${proposal.originalDurationMinutes}m", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(" ➔ ", fontSize = 11.sp, color = Cyan400)
                                                Text("${proposal.optimizedDurationMinutes}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald400)
                                                Text(" (-${proposal.originalDurationMinutes - proposal.optimizedDurationMinutes}m)", fontSize = 11.sp, color = Emerald400)
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Économie Carburant", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "-${proposal.estimatedFuelSavedLiters} L (${proposal.estimatedCostSavedFcfa} F)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald400
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action Button
                                    Button(
                                        onClick = {
                                            appliedProposals = appliedProposals + proposal.id
                                            onApplyProposal?.invoke(proposal)
                                            toastFeedback = "Itinéraire optimisé affecté à ${proposal.vehicleName} !"
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isApplied) Emerald600 else Cyan500
                                        )
                                    ) {
                                        Icon(
                                            imageVector = if (isApplied) Icons.Default.Check else Icons.Default.Navigation,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isApplied) "Itinéraire IA Appliqué" else "Valider & Transmettre au Chauffeur",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: Plan d'Action Dispatcheur
                        item {
                            Text(
                                text = "Directives Opérationnelles pour le Dispatcheur",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        items(optimizationResult.actionRules, key = { it.id }) { rule ->
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
}
