package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.export.ExportManager
import com.example.model.DriverCoachingReport
import com.example.model.InAppCoachingNotification
import com.example.model.MaintenancePredictionResult
import com.example.model.Trip
import com.example.model.Vehicle
import com.example.ui.theme.Emerald500
import com.example.ui.theme.TharaRed
import com.example.ui.theme.TharaTextPrimary
import com.example.ui.theme.TharaTextSecondary

enum class ExportFormat(val label: String, val extension: String, val mimeType: String) {
    EXCEL_WORKBOOK("Excel Multi-Feuilles (.xls / .xlsx)", ".xls", "application/vnd.ms-excel"),
    PDF_DOCUMENT("Rapport Officiel PDF (.pdf)", ".pdf", "application/pdf")
}

@Composable
fun ExportModal(
    vehicles: List<Vehicle>,
    selectedVehicle: Vehicle?,
    maintenanceResult: MaintenancePredictionResult? = null,
    coachingReport: DriverCoachingReport? = null,
    coachingNotifs: List<InAppCoachingNotification> = emptyList(),
    trips: List<Trip> = emptyList(),
    isDarkTheme: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf(ExportFormat.EXCEL_WORKBOOK) }
    var exportScopeAllVehicles by remember { mutableStateOf(selectedVehicle == null) }
    var selectedPeriod by remember { mutableStateOf("DU 01 AU 31 AOÛT 2026") }
    var isPeriodDropdownOpen by remember { mutableStateOf(false) }
    var isSuccessExported by remember { mutableStateOf(false) }

    val periodsList = listOf(
        "DU 01 AU 31 AOÛT 2026",
        "DU 01 AU 31 JUILLET 2026",
        "DU 01 AU 07 SEPTEMBRE 2025",
        "DU 01 AU 30 JUIN 2026",
        "ANNÉE 2026 - RAPPORT GLOBAL"
    )

    val dialogBg = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textPrimary = if (isDarkTheme) Color.White else TharaTextPrimary
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else TharaTextSecondary
    val cardBorder = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = dialogBg,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("export_dialog_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header with Brand Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(TharaRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Générer Rapport Mensuel",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "Modèle Thara-Services (Excel & PDF)",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Banner Preview Badge matching user template
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TharaRed)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Column {
                        Text(
                            text = "TRACKING GPS THARA-SERVICES $selectedPeriod",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "THARA SERVICES - VOTRE SÉCURITÉ C'EST NOTRE PRIORITÉ",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. SELECT PERIOD (MONTH)
                Text("1. Période & Mois du Rapport", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .clickable { isPeriodDropdownOpen = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedPeriod,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = textSecondary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isPeriodDropdownOpen,
                        onDismissRequest = { isPeriodDropdownOpen = false },
                        modifier = Modifier.background(if (isDarkTheme) Color(0xFF1E293B) else Color.White)
                    ) {
                        periodsList.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period, color = textPrimary, fontSize = 12.sp) },
                                onClick = {
                                    selectedPeriod = period
                                    isPeriodDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. SELECT FORMAT
                Text("2. Format du Fichier", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Excel Multi-sheets Option
                    val isExcel = selectedFormat == ExportFormat.EXCEL_WORKBOOK
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExcel) Emerald500.copy(alpha = 0.12f) else if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(if (isExcel) 2.dp else 1.dp, if (isExcel) Emerald500 else cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFormat = ExportFormat.EXCEL_WORKBOOK }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = Emerald500, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Excel (.xls)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("Onglet par Véhicule", fontSize = 9.5.sp, color = textSecondary)
                        }
                    }

                    // PDF Multi-pages Option
                    val isPdf = selectedFormat == ExportFormat.PDF_DOCUMENT
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPdf) TharaRed.copy(alpha = 0.12f) else if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(if (isPdf) 2.dp else 1.dp, if (isPdf) TharaRed else cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFormat = ExportFormat.PDF_DOCUMENT }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = TharaRed, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Rapport PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("Document Officiel", fontSize = 9.5.sp, color = textSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. SELECT SCOPE
                Text("3. Périmètre des Données", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                Spacer(modifier = Modifier.height(4.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { exportScopeAllVehicles = true }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = exportScopeAllVehicles,
                            onClick = { exportScopeAllVehicles = true },
                            colors = RadioButtonDefaults.colors(selectedColor = TharaRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Toute la Flotte (${vehicles.size} Véhicules & Onglets)", fontSize = 12.sp, color = textPrimary)
                    }

                    if (selectedVehicle != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { exportScopeAllVehicles = false }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = !exportScopeAllVehicles,
                                onClick = { exportScopeAllVehicles = false },
                                colors = RadioButtonDefaults.colors(selectedColor = TharaRed)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Uniquement ${selectedVehicle.name} (${selectedVehicle.licensePlate})", fontSize = 12.sp, color = textPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gemini AI Insights Included Indicator
                if (maintenanceResult != null || coachingReport != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFEFF6FF))
                            .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Inclut les diagnostics IA Gemini & comportement de conduite",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1D4ED8)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Action Buttons (Download & Share)
                Button(
                    onClick = {
                        val activeVeh = if (exportScopeAllVehicles) null else selectedVehicle
                        if (selectedFormat == ExportFormat.EXCEL_WORKBOOK) {
                            val generatedExcel = ExportManager.exportToExcelWorkbook(
                                context = context,
                                vehicles = vehicles,
                                selectedVehicle = activeVeh,
                                trips = trips,
                                maintenanceResult = maintenanceResult,
                                coachingReport = coachingReport,
                                periodLabel = selectedPeriod
                            )
                            ExportManager.shareGeneratedFile(context, generatedExcel, ExportFormat.EXCEL_WORKBOOK.mimeType)
                            Toast.makeText(context, "Rapport Excel généré avec succès !", Toast.LENGTH_SHORT).show()
                        } else {
                            val generatedPdf = ExportManager.exportToPdf(
                                context = context,
                                vehicles = vehicles,
                                selectedVehicle = activeVeh,
                                trips = trips,
                                maintenanceResult = maintenanceResult,
                                coachingReport = coachingReport,
                                periodLabel = selectedPeriod
                            )
                            ExportManager.shareGeneratedFile(context, generatedPdf, ExportFormat.PDF_DOCUMENT.mimeType)
                            Toast.makeText(context, "Rapport PDF généré avec succès !", Toast.LENGTH_SHORT).show()
                        }
                        isSuccessExported = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("export_confirm_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TharaRed)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Générer et Télécharger (${selectedFormat.label.split(" ").first()})",
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
