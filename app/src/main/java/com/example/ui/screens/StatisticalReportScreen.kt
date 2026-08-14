package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun StatisticalReportScreen(
    onBack: () -> Unit = {},
    onReportSelected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedReportTitle by remember { mutableStateOf<String?>(null) }
    var selectedReportIcon by remember { mutableStateOf<ImageVector>(Icons.Default.Speed) }
    var selectedReportColor by remember { mutableStateOf(Color(0xFF3B82F6)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAAECEFF1)) // 66% translucent slate-grey
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xCCFFFFFF)) // 80% translucent white
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color(0xFF1E293B)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rapport statistique",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            // User Profile Avatar on top right
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00A2FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reports List Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x99FFFFFF)) // 60% translucent white
        ) {
            ReportItem(
                icon = Icons.Default.Speed,
                iconTint = Color(0xFF3B82F6),
                title = "Rapport de vitesse",
                onClick = {
                    selectedReportTitle = "Rapport de vitesse"
                    selectedReportIcon = Icons.Default.Speed
                    selectedReportColor = Color(0xFF3B82F6)
                }
            )
            ReportItem(
                icon = Icons.Default.PowerOff,
                iconTint = Color(0xFFF59E0B),
                title = "Rapport d'inactivité",
                onClick = {
                    selectedReportTitle = "Rapport d'inactivité"
                    selectedReportIcon = Icons.Default.PowerOff
                    selectedReportColor = Color(0xFFF59E0B)
                }
            )
            ReportItem(
                icon = Icons.Default.LocalParking,
                iconTint = Color(0xFF6366F1),
                title = "Rapport de séjour",
                onClick = {
                    selectedReportTitle = "Rapport de séjour"
                    selectedReportIcon = Icons.Default.LocalParking
                    selectedReportColor = Color(0xFF6366F1)
                }
            )
            ReportItem(
                icon = Icons.Default.LocalFireDepartment,
                iconTint = Color(0xFFEF4444),
                title = "Rapport d'allumage",
                onClick = {
                    selectedReportTitle = "Rapport d'allumage"
                    selectedReportIcon = Icons.Default.LocalFireDepartment
                    selectedReportColor = Color(0xFFEF4444)
                }
            )
            ReportItem(
                icon = Icons.Default.Flag,
                iconTint = Color(0xFF10B981),
                title = "Statistiques de kilométrage",
                onClick = {
                    selectedReportTitle = "Statistiques de kilométrage"
                    selectedReportIcon = Icons.Default.Flag
                    selectedReportColor = Color(0xFF10B981)
                }
            )
            ReportItem(
                icon = Icons.Default.WaterDrop,
                iconTint = Color(0xFF06B6D4),
                title = "Statistiques de l'huile",
                onClick = {
                    selectedReportTitle = "Statistiques de l'huile"
                    selectedReportIcon = Icons.Default.WaterDrop
                    selectedReportColor = Color(0xFF06B6D4)
                }
            )
            ReportItem(
                icon = Icons.Default.NotificationsActive,
                iconTint = Color(0xFFEC4899),
                title = "Statistiques d'alarme",
                onClick = {
                    selectedReportTitle = "Statistiques d'alarme"
                    selectedReportIcon = Icons.Default.NotificationsActive
                    selectedReportColor = Color(0xFFEC4899)
                }
            )
            ReportItem(
                icon = Icons.Default.Lock,
                iconTint = Color(0xFF8B5CF6),
                title = "Rapport de clôture",
                onClick = {
                    selectedReportTitle = "Rapport de clôture"
                    selectedReportIcon = Icons.Default.Lock
                    selectedReportColor = Color(0xFF8B5CF6)
                }
            )
            ReportItem(
                icon = Icons.Default.DirectionsCar,
                iconTint = Color(0xFF3B82F6),
                title = "Rapport de comportement de conduite",
                onClick = {
                    selectedReportTitle = "Rapport de comportement de conduite"
                    selectedReportIcon = Icons.Default.DirectionsCar
                    selectedReportColor = Color(0xFF3B82F6)
                },
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Report Detail & Export Dialog
    if (selectedReportTitle != null) {
        Dialog(
            onDismissRequest = { selectedReportTitle = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header inside dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(selectedReportColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = selectedReportIcon,
                                    contentDescription = null,
                                    tint = selectedReportColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = selectedReportTitle ?: "",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        IconButton(onClick = { selectedReportTitle = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Report summary card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Période : Aujourd'hui (24h)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Véhicule", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                    Text(text = "Rav4 (NDJESSA)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Statut", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                    Text(text = "Conforme", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Detailed Data Table Preview
                    Text(
                        text = "Aperçu des données",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    repeat(4) { index ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Événement #${index + 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                    Text(text = "13 Août 2026 - 12:0${index}m", fontSize = 12.sp, color = Color(0xFF64748B))
                                }
                                Badge(
                                    containerColor = selectedReportColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Valeur : ${15 + index * 12}",
                                        color = selectedReportColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    // Export Buttons (PDF & Excel)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val pdfFile = com.example.data.export.ExportManager.exportToPdf(
                                    context = context,
                                    vehicles = emptyList(),
                                    periodLabel = "DU 01 AU 31 AOÛT 2026"
                                )
                                com.example.data.export.ExportManager.shareGeneratedFile(context, pdfFile, "application/pdf")
                                Toast.makeText(context, "Export PDF généré avec succès !", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Exporter PDF", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                val excelFile = com.example.data.export.ExportManager.exportToExcelWorkbook(
                                    context = context,
                                    vehicles = emptyList(),
                                    periodLabel = "DU 01 AU 31 AOÛT 2026"
                                )
                                com.example.data.export.ExportManager.shareGeneratedFile(context, excelFile, "application/vnd.ms-excel")
                                Toast.makeText(context, "Export Excel généré avec succès !", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Exporter Excel", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ReportItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    fontSize = 15.sp,
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(
                color = Color(0xFFF1F5F9),
                thickness = 1.dp,
                modifier = Modifier.padding(start = 60.dp)
            )
        }
    }
}

