package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TharaCardBorder
import com.example.ui.theme.TharaGrayPill
import com.example.ui.theme.TharaGrayText
import com.example.ui.theme.TharaRed
import com.example.ui.theme.TharaRedLight
import com.example.ui.theme.TharaTextMuted
import com.example.ui.theme.TharaTextPrimary
import com.example.ui.theme.TharaTextSecondary

import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsCar

import android.widget.Toast
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.ui.platform.LocalContext
import com.example.data.export.ExportManager
import com.example.model.DriverCoachingReport
import com.example.model.MaintenancePredictionResult
import com.example.model.Vehicle

@Composable
fun ReportsScreen(
    vehicles: List<Vehicle> = emptyList(),
    selectedVehicle: Vehicle? = null,
    maintenanceResult: MaintenancePredictionResult? = null,
    coachingReport: DriverCoachingReport? = null,
    onOpenEngineMaintenance: (() -> Unit)? = null,
    onOpenDriverCoaching: (() -> Unit)? = null,
    onOpenRouteOptimization: (() -> Unit)? = null,
    onOpenExportModal: (() -> Unit)? = null,
    onOpenAiChat: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTimePeriod by remember { mutableStateOf("Last 30 Days") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val periods = listOf("Last 7 Days", "Last 30 Days", "Last 90 Days", "This Year")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HERO CARD: RAPPORT MENSUEL D'ACTIVITÉ DES VÉHICULES (TEMPLATE EXCEL & PDF THARA-SERVICES)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("monthly_report_hero_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, TharaRed.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Top Header Pill Banner matching user screenshot
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TharaRed)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TRACKING GPS THARA-SERVICES",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "VOTRE SÉCURITÉ C'EST NOTRE PRIORITÉ",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "AOÛT 2026",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Rapport Mensuel d'Activité des Véhicules",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TharaTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Générez et téléchargez le rapport d'activité mensuel officiel avec les onglets par véhicule (horodatage exact H:M:S, adresses GPS et état ACC moteur).",
                    fontSize = 12.sp,
                    color = TharaTextSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row: Télécharger PDF & Télécharger Excel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // PDF Download Button
                    Button(
                        onClick = {
                            val pdfFile = ExportManager.exportToPdf(
                                context = context,
                                vehicles = vehicles,
                                selectedVehicle = selectedVehicle,
                                maintenanceResult = maintenanceResult,
                                coachingReport = coachingReport,
                                periodLabel = "DU 01 AU 31 AOÛT 2026"
                            )
                            ExportManager.shareGeneratedFile(context, pdfFile, "application/pdf")
                            Toast.makeText(context, "Rapport PDF Mensuel généré !", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TharaRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("download_monthly_pdf_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Télécharger PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Excel Download Button
                    Button(
                        onClick = {
                            val excelFile = ExportManager.exportToExcelWorkbook(
                                context = context,
                                vehicles = vehicles,
                                selectedVehicle = selectedVehicle,
                                maintenanceResult = maintenanceResult,
                                coachingReport = coachingReport,
                                periodLabel = "DU 01 AU 31 AOÛT 2026"
                            )
                            ExportManager.shareGeneratedFile(context, excelFile, "application/vnd.ms-excel")
                            Toast.makeText(context, "Rapport Excel Multi-Onglets généré !", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("download_monthly_excel_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Télécharger Excel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
        // AI Copilot Conversational Chat Banner
        if (onOpenAiChat != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reports_ai_copilot_chat_banner")
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onOpenAiChat() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF818CF8))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(TharaRed),
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
                                    text = "Thara AI Copilot (Chat Direct)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(TharaRed)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("NOUVEAU", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                }
                            }
                            Text(
                                text = "Interrogez l'état de la flotte en direct (ralenti, carburant, vitesse)",
                                fontSize = 11.sp,
                                color = Color(0xFFC7D2FE)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(TharaRed)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Discuter ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // AI Engine Maintenance Highlight Card
        if (onOpenEngineMaintenance != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reports_engine_maintenance_banner")
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onOpenEngineMaintenance() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22D3EE))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF06B6D4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Maintenance Prédictive AI",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF22D3EE))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("GEMINI", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                }
                            }
                            Text(
                                text = "Prédiction des pannes moteur & usure des organes",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF06B6D4))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Diagnostiquer ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // AI Route Optimization Banner Card
        if (onOpenRouteOptimization != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reports_route_optimization_banner")
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onOpenRouteOptimization() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2942)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Optimisation d'Itinéraire IA",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF38BDF8))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("CARBURANT", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                }
                            }
                            Text(
                                text = "Trajets éco-énergétiques & trafic en temps réel",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0284C7))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Optimiser ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // AI Driver Coaching & Behavior Banner
        if (onOpenDriverCoaching != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reports_driver_coaching_banner")
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onOpenDriverCoaching() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF818CF8))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF6366F1)),
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
                                    text = "Coaching Chauffeur & Éco-Conduite",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF818CF8))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("IA EN DIRECT", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                }
                            }
                            Text(
                                text = "Analyse freinages, accélérations & notifications in-app",
                                fontSize = 11.sp,
                                color = Color(0xFFC7D2FE)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF6366F1))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Coacher ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Header Row: Title + Period Dropdown + Export Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Rapports & Analystique",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TharaTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Performances de flotte, trajets & exports.",
                    fontSize = 13.sp,
                    color = TharaTextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onOpenExportModal != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TharaRed)
                            .clickable { onOpenExportModal() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("reports_export_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exporter Excel/PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Period Selector Dropdown Button
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, TharaCardBorder, RoundedCornerShape(12.dp))
                            .clickable { isDropdownExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("reports_period_dropdown")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedTimePeriod,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TharaTextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TharaTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    periods.forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period, color = TharaTextPrimary) },
                            onClick = {
                                selectedTimePeriod = period
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }

        // Top 2 Metric Cards (Fleet Efficiency + Total Mileage)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Fleet Efficiency
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TharaCardBorder),
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_fleet_efficiency")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = TharaRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "FLEET EFFICIENCY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TharaTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "92.4%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TharaTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "↗ +2.1%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TharaRed // Dark red
                    )
                }
            }

            // Total Mileage
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TharaCardBorder),
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_total_mileage")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                tint = TharaRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "TOTAL MILEAGE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TharaTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "142k mi",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TharaTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "→ Stable",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TharaTextMuted
                    )
                }
            }
        }

        // Consumption & Mileage Card (Bar Chart)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TharaCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("consumption_mileage_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Consumption & Mileage",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TharaTextPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = TharaTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE5E7EB))
                Spacer(modifier = Modifier.height(16.dp))

                // Bar Chart Visualizer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Y Axis Labels
                    Column(
                        modifier = Modifier.height(180.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("High", fontSize = 11.sp, color = TharaTextMuted)
                        Text("Med", fontSize = 11.sp, color = TharaTextMuted)
                        Text("Low", fontSize = 11.sp, color = TharaTextMuted)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 5 Weekly Columns
                    val barData = listOf(
                        Pair(0.3f, 0.6f),  // W1 (red portion, total portion)
                        Pair(0.5f, 0.75f), // W2
                        Pair(0.2f, 0.5f),  // W3
                        Pair(0.6f, 0.85f), // W4
                        Pair(0.25f, 0.45f) // W5
                    )
                    val weekLabels = listOf("W1", "W2", "W3", "W4", "W5")

                    weekLabels.forEachIndexed { index, label ->
                        val (redRatio, totalRatio) = barData[index]

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.height(180.dp)
                        ) {
                            // Column Stack
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height((140 * totalRatio).dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE5E7EB)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                // Red Filled Lower Portion
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((140 * redRatio).dp)
                                        .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                                        .background(TharaRed)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TharaTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Efficiency Distribution Card (Donut Chart)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TharaCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("efficiency_distribution_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Efficiency Distribution",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TharaTextPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = TharaTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE5E7EB))
                Spacer(modifier = Modifier.height(20.dp))

                // Donut Chart Canvas
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val strokeWidth = 24.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeftOffset = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(diameter, diameter)

                        // 1. Optimal (65% = 234 deg)
                        drawArc(
                            color = TharaRed,
                            startAngle = -90f,
                            sweepAngle = 234f,
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // 2. Average (25% = 90 deg)
                        drawArc(
                            color = Color(0xFFD1D5DB),
                            startAngle = 148f,
                            sweepAngle = 86f,
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // 3. Under (10% = 36 deg)
                        drawArc(
                            color = Color(0xFF6B7280),
                            startAngle = 238f,
                            sweepAngle = 32f,
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Inner Donut Text
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "65%",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = TharaTextPrimary
                        )
                        Text(
                            text = "Optimal",
                            fontSize = 12.sp,
                            color = TharaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Donut Legend Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = TharaRed, label = "Optimal")
                    LegendDot(color = Color(0xFFD1D5DB), label = "Average")
                    LegendDot(color = Color(0xFF6B7280), label = "Under")
                }
            }
        }

        // Export Full Report Action Button
        Button(
            onClick = { onOpenExportModal?.invoke() },
            colors = ButtonDefaults.buttonColors(containerColor = TharaRed),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("export_full_report_button")
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Exporter Rapport Complet (Excel / PDF)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TharaTextSecondary
        )
    }
}
