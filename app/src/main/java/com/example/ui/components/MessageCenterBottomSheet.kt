package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Alert
import com.example.model.AlertSeverity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCenterBottomSheet(
    alerts: List<Alert> = emptyList(),
    onAcknowledgeAlert: ((String) -> Unit)? = null,
    onClearAllAlerts: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Message d'alarme, 1: Notification

    val unacknowledgedAlerts = remember(alerts) {
        alerts.filter { !it.acknowledged }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color(0xFF1E293B)
                    )
                }
                Text(
                    text = "Centre de messages",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                if (alerts.isNotEmpty()) {
                    IconButton(onClick = { onClearAllAlerts?.invoke() }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Tout effacer",
                            tint = Color(0xFFEF4444)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "0", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tabs Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val tabs = listOf(
                    "Message d'alarme (${unacknowledgedAlerts.size})",
                    "Notifications (0)"
                )
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedTab = index }
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF10B981) else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .height(3.dp)
                                .background(if (isSelected) Color(0xFF10B981) else Color.Transparent)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Content List
            if (selectedTab == 0) {
                if (unacknowledgedAlerts.isEmpty()) {
                    // Empty state : 0 Notification / Aucune alarme
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Text(
                                text = "Aucune alarme active",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )

                            Text(
                                text = "Vos notifications sont à 0. Tous les éléments de test ont été supprimés et la flotte opère normalement.",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(unacknowledgedAlerts, key = { it.id }) { alert ->
                            val timeStr = remember(alert.timestamp) {
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(alert.timestamp))
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Nom du véhicule", fontSize = 14.sp, color = Color.Gray)
                                        Text(
                                            text = alert.vehicleName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Heure", fontSize = 14.sp, color = Color.Gray)
                                        Text(
                                            text = timeStr,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Type d'alerte", fontSize = 14.sp, color = Color.Gray)
                                        Text(
                                            text = alert.type.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (alert.severity == AlertSeverity.CRITICAL) Color(0xFFDC2626) else Color(0xFFD97706)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Opérer", fontSize = 14.sp, color = Color.Gray)

                                        Button(
                                            onClick = { onAcknowledgeAlert?.invoke(alert.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            shape = RoundedCornerShape(20.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Marquer lu",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "0 nouvelle notification",
                            color = Color(0xFF64748B),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
