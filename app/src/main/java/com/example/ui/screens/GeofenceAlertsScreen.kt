package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import com.example.model.Alert
import com.example.model.GeofenceAndRouteOptimizationResult
import com.example.model.GeofenceZone
import com.example.model.RouteOptimizationProposal
import com.example.ui.components.GeofenceRouteOptimizationSheet
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald400
import com.example.ui.theme.TharaBluePill
import com.example.ui.theme.TharaBlueText
import com.example.ui.theme.TharaCardBorder
import com.example.ui.theme.TharaGrayPill
import com.example.ui.theme.TharaGrayText
import com.example.ui.theme.TharaRed
import com.example.ui.theme.TharaRedLight
import com.example.ui.theme.TharaTextMuted
import com.example.ui.theme.TharaTextPrimary
import com.example.ui.theme.TharaTextSecondary
import com.example.ui.theme.TharaYellowPill
import com.example.ui.theme.TharaYellowText

@Composable
fun GeofenceAlertsScreen(
    geofences: List<GeofenceZone>,
    alerts: List<Alert>,
    onAcknowledgeAlert: (String) -> Unit,
    onOpenGeofenceConfig: (() -> Unit)? = null,
    optimizationResult: GeofenceAndRouteOptimizationResult? = null,
    isAnalyzingGeofenceAndRoutes: Boolean = false,
    onRunGeofenceAndRouteOptimization: (() -> Unit)? = null,
    onApplyRouteProposal: ((RouteOptimizationProposal) -> Unit)? = null,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var showOptimizationSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Subtitle + Geofence Config Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Historique & Périmètres",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TharaTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Alertes géofencing, excès de vitesse et notifications Firestore.",
                    fontSize = 13.sp,
                    color = TharaTextSecondary
                )
            }

            if (onOpenGeofenceConfig != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(TharaRed)
                        .clickable { onOpenGeofenceConfig() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "⚙️ Configurer",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Gemini AI Assistant Contextual Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showOptimizationSheet = true
                    if (optimizationResult == null) {
                        onRunGeofenceAndRouteOptimization?.invoke()
                    }
                }
                .testTag("geofence_gemini_summary_banner"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Cyan500.copy(alpha = 0.08f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Cyan500.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Cyan500),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Assistant Gemini 3.5 Flash",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Cyan500.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "IA Live",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Cyan400,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (optimizationResult != null) {
                                "Score risque: ${optimizationResult.breachSummary.riskScore}/100 • ${optimizationResult.proposals.size} trajets optimisés disponibles"
                            } else {
                                "Synthétiser les alertes de zones et générer des optimisations de trajets"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Cyan400,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // All (24)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selectedFilter == "All") Color(0xFFE5E7EB) else Color.Transparent)
                    .border(1.dp, TharaCardBorder, RoundedCornerShape(20.dp))
                    .clickable { selectedFilter = "All" }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "All (24)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TharaTextPrimary
                )
            }

            // Critical (3)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(TharaRedLight)
                    .border(1.dp, TharaRedLight, RoundedCornerShape(20.dp))
                    .clickable { selectedFilter = "Critical" }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = TharaRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Critical (3)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TharaRed
                    )
                }
            }

            // Warning (8)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(TharaYellowPill)
                    .border(1.dp, TharaYellowPill, RoundedCornerShape(20.dp))
                    .clickable { selectedFilter = "Warning" }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = TharaYellowText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Warning (8)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TharaYellowText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section: TODAY
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            Text(
                text = "TODAY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TharaTextMuted,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
        }

        // Card 1: Engine Overheating
        AlertHistoryCard(
            accentColor = TharaRed,
            icon = Icons.Default.Shield,
            iconBg = TharaRedLight,
            title = "Engine Overheating",
            time = "10:42 AM",
            description = "Coolant temperature exceeded 220°F. Immediate inspection required to prevent permanent damage.",
            tags = listOf(
                TagItem("TRK-492", TharaGrayPill, TharaGrayText),
                TagItem("Action Required", TharaRedLight, TharaRed)
            )
        )

        // Card 2: Low Tire Pressure
        AlertHistoryCard(
            accentColor = TharaYellowText,
            icon = Icons.Default.TireRepair,
            iconBg = TharaYellowPill,
            title = "Low Tire Pressure",
            time = "09:15 AM",
            description = "Rear right tire pressure detected at 28 PSI. Recommended minimum is 32 PSI.",
            tags = listOf(
                TagItem("TRK-108", TharaGrayPill, TharaGrayText)
            )
        )

        // Card 3: Route Deviation
        AlertHistoryCard(
            accentColor = TharaBlueText,
            icon = Icons.Default.AltRoute,
            iconBg = TharaBluePill,
            title = "Route Deviation",
            time = "08:30 AM",
            description = "Vehicle deviated from planned route I-95 South. Likely due to reported traffic conditions.",
            tags = listOf(
                TagItem("VAN-33", TharaGrayPill, TharaGrayText),
                TagItem("J. Smith", TharaGrayPill, TharaGrayText)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Section: YESTERDAY
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            Text(
                text = "YESTERDAY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TharaTextMuted,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
        }

        // Card 4: Main Battery Failure
        AlertHistoryCard(
            accentColor = TharaRed,
            icon = Icons.Default.BatteryAlert,
            iconBg = TharaRedLight,
            title = "Main Battery Failure",
            time = "4:45 PM",
            description = "Voltage dropped below critical threshold. Vehicle immobilized.",
            tags = listOf(
                TagItem("TRK-772", TharaGrayPill, TharaGrayText),
                TagItem("Resolved", TharaGrayPill, TharaGrayText)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Load More Button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            OutlinedButton(
                onClick = { },
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TharaTextMuted),
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .testTag("load_more_alerts_button")
            ) {
                Text(
                    text = "Load More",
                    fontWeight = FontWeight.SemiBold,
                    color = TharaTextPrimary
                )
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

private data class TagItem(val text: String, val bg: Color, val textColor: Color)

@Composable
private fun AlertHistoryCard(
    accentColor: Color,
    icon: ImageVector,
    iconBg: Color,
    title: String,
    time: String,
    description: String,
    tags: List<TagItem>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TharaCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Colored Left Accent Bar
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(130.dp)
                    .background(accentColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TharaTextPrimary
                            )
                            Text(
                                text = time,
                                fontSize = 13.sp,
                                color = TharaTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = description,
                            fontSize = 13.sp,
                            color = TharaTextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(tag.bg)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tag.text,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = tag.textColor
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
