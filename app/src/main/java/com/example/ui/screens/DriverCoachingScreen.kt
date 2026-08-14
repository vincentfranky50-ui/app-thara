package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlertSeverity
import com.example.model.CoachingRecommendation
import com.example.model.DriverCoachingReport
import com.example.model.DrivingEventType
import com.example.model.DrivingTelemetryEvent
import com.example.model.InAppCoachingNotification
import com.example.model.Vehicle
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Red500
import com.example.ui.theme.TharaCardBorder
import com.example.ui.theme.TharaRed
import com.example.ui.theme.TharaTextPrimary
import com.example.ui.theme.TharaTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DriverCoachingScreen(
    vehicles: List<Vehicle>,
    selectedVehicle: Vehicle?,
    coachingReport: DriverCoachingReport?,
    coachingNotifications: List<InAppCoachingNotification>,
    telemetryEvents: List<DrivingTelemetryEvent>,
    isAnalyzing: Boolean,
    isDarkTheme: Boolean,
    onSelectVehicle: (Vehicle) -> Unit,
    onRunAnalysis: (Vehicle) -> Unit,
    onSimulateEvent: (Vehicle, DrivingEventType) -> Unit,
    onMarkNotificationRead: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeVehicle = selectedVehicle ?: vehicles.firstOrNull()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Theme Colors
    val bgCanvas by animateColorAsState(if (isDarkTheme) Color(0xFF0F172A) else MaterialTheme.colorScheme.background, label = "bgCanvas")
    val cardBg by animateColorAsState(if (isDarkTheme) Color(0xFF1E293B) else Color.White, label = "cardBg")
    val cardBorder by animateColorAsState(if (isDarkTheme) Color(0xFF334155) else TharaCardBorder, label = "cardBorder")
    val textPrimary by animateColorAsState(if (isDarkTheme) Color.White else TharaTextPrimary, label = "textPrimary")
    val textSecondary by animateColorAsState(if (isDarkTheme) Color(0xFF94A3B8) else TharaTextSecondary, label = "textSecondary")

    LaunchedEffect(activeVehicle?.id) {
        if (activeVehicle != null && coachingReport == null) {
            onRunAnalysis(activeVehicle)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgCanvas)
    ) {
        // Top Bar / Title Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDarkTheme) Color(0xFF1E293B) else Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF2563EB)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Coaching & Éco-Conduite",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Analyse IA du comportement au volant & notifications en temps réel",
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }

                if (activeVehicle != null) {
                    IconButton(
                        onClick = { onRunAnalysis(activeVehicle) },
                        enabled = !isAnalyzing,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Cyan400)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Réanalyser", tint = textPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Vehicle Selector Chips
        if (vehicles.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vehicles) { vehicle ->
                    val isSelected = vehicle.id == activeVehicle?.id
                    val chipBg by animateColorAsState(
                        if (isSelected) TharaRed else if (isDarkTheme) Color(0xFF1E293B) else Color.White,
                        label = "chipBg"
                    )
                    val chipTextColor by animateColorAsState(
                        if (isSelected) Color.White else textPrimary,
                        label = "chipText"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(chipBg)
                            .border(
                                1.dp,
                                if (isSelected) TharaRed else cardBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { onSelectVehicle(vehicle) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${vehicle.name} (${vehicle.driverName})",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = chipTextColor
                            )
                        }
                    }
                }
            }
        }

        // Navigation Tabs (Analyse IA vs Notifications in-App)
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White,
            contentColor = TharaRed,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = TharaRed
                    )
                }
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bilan & Coaching IA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    val unreadCount = coachingNotifications.count { !it.isRead }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Notifications (${coachingNotifications.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(TharaRed)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (activeVehicle == null) {
                item {
                    Text("Aucun véhicule sélectionné.", color = textSecondary)
                }
            } else if (selectedTabIndex == 0) {
                // TAB 0: COACHING & TELEMETRY ANALYSIS

                // Simulation Control Panel Banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = Amber500, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Simulateur d'Événements Télématiques", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                }
                                Text("Test IA", fontSize = 10.sp, color = textSecondary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Cliquez sur un événement ci-dessous pour injecter une fausse télométrie d'accéléromètre et redéclencher le coaching Gemini AI en temps réel :",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onSimulateEvent(activeVehicle, DrivingEventType.SUDDEN_BRAKING) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500)
                                ) {
                                    Text("🛑 Freinage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { onSimulateEvent(activeVehicle, DrivingEventType.RAPID_ACCELERATION) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber500)
                                ) {
                                    Text("⚡ Accélération", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { onSimulateEvent(activeVehicle, DrivingEventType.SHARP_CORNERING) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Indigo400)
                                ) {
                                    Text("🔄 Virage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Driver Score Cards (Safety & Eco Scores)
                item {
                    val safetyScore = coachingReport?.safetyScore ?: 88
                    val ecoScore = coachingReport?.ecoDrivingScore ?: 82
                    val extraFuel = coachingReport?.estimatedExtraFuelLitersPer100Km ?: 1.45

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Safety Gauge Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Score Sécurité", fontSize = 12.sp, color = textSecondary, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = (safetyScore / 100f).coerceIn(0f, 1f),
                                        modifier = Modifier.size(64.dp),
                                        strokeWidth = 6.dp,
                                        color = if (safetyScore > 80) Emerald500 else if (safetyScore > 60) Amber500 else Red500,
                                        trackColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
                                    )
                                    Text("$safetyScore", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (safetyScore > 80) "Très Prudent" else "Amélioration requise",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (safetyScore > 80) Emerald500 else Amber500
                                )
                            }
                        }

                        // Eco Gauge Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Score Éco-Conduite", fontSize = 12.sp, color = textSecondary, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = (ecoScore / 100f).coerceIn(0f, 1f),
                                        modifier = Modifier.size(64.dp),
                                        strokeWidth = 6.dp,
                                        color = Cyan400,
                                        trackColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
                                    )
                                    Text("$ecoScore", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Cyan400, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+${String.format(Locale.getDefault(), "%.1f", extraFuel)}L/100km", fontSize = 11.sp, color = Cyan400, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Gemini AI Coaching Summary Box
                item {
                    val summaryText = coachingReport?.aiCoachingSummary ?: "Analyse en cours..."

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Cyan400),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(Cyan400, Color(0xFF2563EB)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Feedback Pédagogique Gemini AI", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Cyan400)
                                    Text("Recommandations personnalisées pour le chauffeur", fontSize = 11.sp, color = textSecondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = summaryText,
                                fontSize = 12.sp,
                                color = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF334155),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Actionable Coaching Recommendations List
                item {
                    Text("Conseils d'Action Immédiate", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                }

                val recs = coachingReport?.recommendations ?: emptyList()
                if (recs.isEmpty()) {
                    item {
                        Text("Aucune recommandation enregistrée.", fontSize = 12.sp, color = textSecondary)
                    }
                } else {
                    items(recs) { rec ->
                        CoachingRecommendationCard(
                            recommendation = rec,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }
                }

                // Recent Telemetry Events Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Événements Télématiques Récents (Boîtier OBD-II)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                }

                if (telemetryEvents.isEmpty()) {
                    item {
                        Text("Aucun événement de conduite détecté.", fontSize = 12.sp, color = textSecondary)
                    }
                } else {
                    items(telemetryEvents) { evt ->
                        TelemetryEventRow(
                            event = evt,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }
                }

            } else {
                // TAB 1: IN-APP NOTIFICATIONS DRAWER
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notifications de Coaching Envoyées", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text("${coachingNotifications.size} au total", fontSize = 12.sp, color = textSecondary)
                    }
                }

                if (coachingNotifications.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                Text("Aucune notification pour le moment.", fontSize = 13.sp, color = textSecondary)
                            }
                        }
                    }
                } else {
                    items(coachingNotifications) { notif ->
                        InAppNotificationCard(
                            notification = notif,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onMarkRead = { onMarkNotificationRead(notif.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachingRecommendationCard(
    recommendation: CoachingRecommendation,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TharaRed.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(recommendation.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TharaRed)
                }

                if (recommendation.estimatedFuelSavingPct > 0) {
                    Text(
                        "Économie : -${recommendation.estimatedFuelSavingPct}% d'essence",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald500
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(recommendation.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)

            Spacer(modifier = Modifier.height(4.dp))

            Text(recommendation.advice, fontSize = 12.sp, color = textSecondary, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun TelemetryEventRow(
    event: DrivingTelemetryEvent,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val (icon, iconColor) = when (event.eventType) {
        DrivingEventType.SUDDEN_BRAKING -> Pair(Icons.Default.Warning, Red500)
        DrivingEventType.RAPID_ACCELERATION -> Pair(Icons.Default.Speed, Amber500)
        DrivingEventType.SHARP_CORNERING -> Pair(Icons.Default.Refresh, Indigo400)
        DrivingEventType.EXCESSIVE_SPEED -> Pair(Icons.Default.Speed, Red500)
        DrivingEventType.IDLING -> Pair(Icons.Default.Info, Cyan400)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(event.eventType.labelFr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text(
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.timestamp)),
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${event.locationName} • Force G : ${event.gForce}g",
                    fontSize = 11.sp,
                    color = textSecondary
                )

                Text(
                    text = "Vitesse : ${event.speedBeforeKmH.toInt()} ➔ ${event.speedAfterKmH.toInt()} km/h (${event.contextualRiskFactor})",
                    fontSize = 10.sp,
                    color = iconColor
                )
            }
        }
    }
}

@Composable
private fun InAppNotificationCard(
    notification: InAppCoachingNotification,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    onMarkRead: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(!notification.isRead) }

    val borderColor = if (!notification.isRead) Cyan400 else cardBorder

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(if (!notification.isRead) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_item_${notification.id}")
            .clickable {
                isExpanded = !isExpanded
                if (!notification.isRead) onMarkRead()
            }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Cyan400)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = notification.coachingCategory,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Cyan400
                    )
                }

                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(notification.timestamp)),
                    fontSize = 11.sp,
                    color = textSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(notification.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)

            Spacer(modifier = Modifier.height(4.dp))

            Text(notification.message, fontSize = 12.sp, color = textSecondary, lineHeight = 17.sp)

            if (notification.actionableTip != null) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Cyan400.copy(alpha = 0.1f))
                                .border(1.dp, Cyan400.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Cyan400, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Action Conseillée par l'IA :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Cyan400)
                                    Text(notification.actionableTip, fontSize = 11.sp, color = textPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
