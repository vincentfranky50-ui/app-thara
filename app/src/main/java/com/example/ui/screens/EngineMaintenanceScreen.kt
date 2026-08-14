package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.model.EngineDataLog
import com.example.model.EngineTelemetryMock
import com.example.model.MaintenancePredictionResult
import com.example.model.MaintenanceUrgency
import com.example.model.PredictedIssue
import com.example.model.Vehicle
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Red500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.TharaRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EngineMaintenanceScreen(
    vehicles: List<Vehicle>,
    selectedVehicle: Vehicle?,
    prediction: MaintenancePredictionResult?,
    isAnalyzing: Boolean,
    onSelectVehicle: (Vehicle) -> Unit,
    onRunAnalysis: (Vehicle) -> Unit,
    onBack: (() -> Unit)? = null,
    onOpenExportModal: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showWorkOrderDialog by remember { mutableStateOf(false) }
    val currentVehicle = selectedVehicle ?: vehicles.firstOrNull()
    val logs = remember(currentVehicle?.id) {
        currentVehicle?.let { EngineTelemetryMock.generateHistoricalLogs(it) } ?: emptyList()
    }

    val bgCanvas = if (MaterialTheme.colorScheme.background == Color.White) Color(0xFF0F172A) else MaterialTheme.colorScheme.background
    val cardBg = Color(0xFF1E293B)
    val cardBorder = Color(0xFF334155)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgCanvas)
            .testTag("engine_maintenance_screen")
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Navigation Header (Figma Stitch Style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cardBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Cyan400)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "THARA AI • DIAGNOSTIC MOTEUR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Cyan400,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Maintenance Prédictive OBD-II",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Header Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onOpenExportModal != null) {
                    Box(
                        modifier = Modifier
                            .testTag("engine_export_button")
                            .clip(RoundedCornerShape(12.dp))
                            .background(TharaRed)
                            .clickable { onOpenExportModal() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Exporter",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (currentVehicle != null) {
                    Box(
                        modifier = Modifier
                            .testTag("reanalyze_engine_button")
                            .clip(RoundedCornerShape(12.dp))
                            .background(Cyan500)
                            .clickable(enabled = !isAnalyzing) { onRunAnalysis(currentVehicle) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAnalyzing) "Analyse..." else "Analyser",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

        // Vehicle Selector Horizontal Chips
        Text(
            text = "Sélectionnez un véhicule de la flotte :",
            fontSize = 13.sp,
            color = Color(0xFF94A3B8)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(vehicles) { v ->
                val isSelected = v.id == currentVehicle?.id
                Box(
                    modifier = Modifier
                        .testTag("diag_vehicle_chip_${v.id}")
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Cyan500 else cardBg)
                        .border(1.dp, if (isSelected) Cyan400 else cardBorder, RoundedCornerShape(14.dp))
                        .clickable { onSelectVehicle(v) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = v.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color.White
                            )
                            Text(
                                text = v.licensePlate,
                                fontSize = 11.sp,
                                color = if (isSelected) Color(0xFFE2E8F0) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        if (currentVehicle != null) {
            val lastLog = logs.lastOrNull() ?: EngineDataLog(
                id = "DEFAULT",
                vehicleId = currentVehicle.id,
                timestamp = System.currentTimeMillis(),
                rpm = 1450,
                coolantTempC = currentVehicle.engineTempC,
                oilPressureBar = 3.6,
                batteryVoltage = 13.8,
                transmissionTempC = 84,
                fuelInjectionRateLh = 4.2,
                cylinderMisfires = 0,
                obdDtcCodes = emptyList(),
                odometryKm = currentVehicle.odometryKm
            )

            // Health Overview Dashboard Banner (Figma / Stitch Card)
            val healthScore = prediction?.overallHealthScore ?: 88
            val urgency = prediction?.urgency ?: MaintenanceUrgency.NORMAL
            val (statusColor, statusBg) = when (urgency) {
                MaintenanceUrgency.NORMAL -> Pair(Emerald500, Color(0xFF064E3B))
                MaintenanceUrgency.ATTENTION -> Pair(Amber500, Color(0xFF78350F))
                MaintenanceUrgency.URGENT -> Pair(Color(0xFFF97316), Color(0xFF7C2D12))
                MaintenanceUrgency.CRITICAL -> Pair(Red500, Color(0xFF7F1D1D))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = urgency.labelFr.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentVehicle.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Matricule: ${currentVehicle.licensePlate} • Odomètre: ${currentVehicle.odometryKm.toInt()} km",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Score Circle Badge
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(statusBg)
                            .border(3.dp, statusColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$healthScore%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor
                            )
                            Text(
                                text = "SANTÉ",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            // Real-Time OBD-II Telemetry Gauges Grid (4 Metric Cards)
            Text(
                text = "Relevés Capteurs Moteur OBD-II (Temps Réel) :",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Coolant Temp Gauge Card
                GaugeCard(
                    title = "Temp. Liquide",
                    value = "${lastLog.coolantTempC}°C",
                    subValue = if (lastLog.coolantTempC > 100) "Alerte Surchauffe" else "Normal (85-98°C)",
                    icon = Icons.Default.Thermostat,
                    progress = (lastLog.coolantTempC / 120f).coerceIn(0f, 1f),
                    accentColor = if (lastLog.coolantTempC > 100) Red500 else Emerald500,
                    modifier = Modifier.weight(1f)
                )

                // Oil Pressure Gauge Card
                GaugeCard(
                    title = "Pression Huile",
                    value = "${lastLog.oilPressureBar} bar",
                    subValue = if (lastLog.oilPressureBar < 2.0) "Pression Basse" else "Pression Optimale",
                    icon = Icons.Default.OilBarrel,
                    progress = (lastLog.oilPressureBar / 5.0f).toFloat().coerceIn(0f, 1f),
                    accentColor = if (lastLog.oilPressureBar < 2.0) Amber500 else Cyan400,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Battery Voltage Card
                GaugeCard(
                    title = "Tension Batterie",
                    value = "${lastLog.batteryVoltage} V",
                    subValue = if (lastLog.batteryVoltage < 12.2) "Charge Faible" else "Alternateur Normal",
                    icon = Icons.Default.ElectricMeter,
                    progress = ((lastLog.batteryVoltage - 10.0) / 5.0).toFloat().coerceIn(0f, 1f),
                    accentColor = if (lastLog.batteryVoltage < 12.2) Red500 else Emerald500,
                    modifier = Modifier.weight(1f)
                )

                // Engine RPM Card
                GaugeCard(
                    title = "Régime Moteur",
                    value = "${lastLog.rpm} RPM",
                    subValue = "Ratés: ${lastLog.cylinderMisfires} détectés",
                    icon = Icons.Default.Speed,
                    progress = (lastLog.rpm / 4000f).coerceIn(0f, 1f),
                    accentColor = Cyan400,
                    modifier = Modifier.weight(1f)
                )
            }

            // OBD-II DTC Codes Banner
            val dtcCodes = logs.flatMap { it.obdDtcCodes }.distinct()
            if (dtcCodes.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF451A03)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF9A3412)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Amber500, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Codes d'Erreur OBD-II / DTC Détectés :",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber500
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            dtcCodes.forEach { code ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF78350F))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = code, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // AI Predictive Maintenance Analysis Section
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Cyan400, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Prédictions d'Organes Moteur à Risque (Gemini AI) :",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            val issues = prediction?.predictedIssues ?: emptyList()
            if (issues.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    issues.forEach { issue ->
                        PredictedIssueCard(issue = issue, cardBg = cardBg, cardBorder = cardBorder)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Aucune défaillance critique détectée par l'analyse prédictive pour ce véhicule.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Recommended Spare Parts & Repair Action Row
            if (prediction != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = Cyan400, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pièces Détachées à Commander (Gestion Stock) :",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        prediction.recommendedPartsToOrder.forEach { part ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald500, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = part, fontSize = 13.sp, color = Color(0xFFE2E8F0))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Generate Mechanic Work Order Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_work_order_button")
                                .clip(RoundedCornerShape(12.dp))
                                .background(Cyan500)
                                .clickable { showWorkOrderDialog = true }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📄 Générer l'Ordre de Réparation pour l'Atelier",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Historical Telemetry Timeline Cards
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Historique Récent des Relevés Télématiques (${logs.size} Séque.s) :",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                logs.takeLast(5).reversed().forEach { log ->
                    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.FRENCH).format(Date(log.timestamp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Heure: $timeStr", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Cyan400)
                                Text(
                                    text = "Temp: ${log.coolantTempC}°C • Pression: ${log.oilPressureBar} bar • Bat: ${log.batteryVoltage}V",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                            Text(
                                text = "${log.rpm} RPM",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Work Order Generated Dialog
    if (showWorkOrderDialog && currentVehicle != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showWorkOrderDialog = false },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Cyan500)
                        .clickable { showWorkOrderDialog = false }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Fermer / Transmettre", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = "📄 Ordre de Réparation Mécanique",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ordre de travail transmis à l'atelier central Thara :", fontSize = 13.sp, color = Color(0xFFCBD5E1))
                    Text("• Véhicule : ${currentVehicle.name} (${currentVehicle.licensePlate})", fontSize = 12.sp, color = Color.White)
                    Text("• Chef d'atelier récepteur : Amadou Diallo (Garage Dakar)", fontSize = 12.sp, color = Color.White)
                    Text("• Organes prioritaires : Pompe à Eau / Filtres / Capteurs OBD", fontSize = 12.sp, color = Color.White)
                    Text("• Statut : Pièces réservées en magasin", fontSize = 12.sp, color = Emerald500, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = cardBg,
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCBD5E1)
        )
    }
}

@Composable
fun GaugeCard(
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    progress: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8))
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subValue,
                fontSize = 10.sp,
                color = Color(0xFFCBD5E1)
            )
        }
    }
}

@Composable
fun PredictedIssueCard(
    issue: PredictedIssue,
    cardBg: Color,
    cardBorder: Color
) {
    val riskColor = when {
        issue.failureRiskPct > 75 -> Red500
        issue.failureRiskPct > 50 -> Amber500
        else -> Emerald500
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = issue.componentName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(riskColor.copy(alpha = 0.2f))
                        .border(1.dp, riskColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Risque ${issue.failureRiskPct}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "⏱️ Panne estimée dans : ~${issue.estimatedRemainingKm} km",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
                Text(
                    text = "💰 Coût : ${String.format("%,d", issue.estimatedCostFcfa)} FCFA",
                    fontSize = 12.sp,
                    color = Cyan400,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cause racine : ${issue.rootCauseAnalysis}",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .padding(10.dp)
            ) {
                Text(
                    text = "🛠️ Action Préventive : ${issue.recommendedAction}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Emerald500
                )
            }
        }
    }
}
