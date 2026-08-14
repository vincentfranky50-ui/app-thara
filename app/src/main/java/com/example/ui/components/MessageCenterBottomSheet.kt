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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AlarmMessage(
    val deviceName: String,
    val time: String,
    val messageType: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCenterBottomSheet(
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Message d'alarme, 1: Notification

    val alarms = listOf(
        AlarmMessage("corolla armel", "2026-08-13 14:25:26", "Shock"),
        AlarmMessage("corolla armel", "2026-08-13 14:21:17", "Shock"),
        AlarmMessage("corolla armel", "2026-08-13 14:18:36", "Acc"),
        AlarmMessage("corolla armel", "2026-08-13 14:18:15", "Shock"),
        AlarmMessage("corolla armel", "2026-08-13 13:34:56", "Shock")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
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
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Color(0xFF1E293B))
                }
                Text(
                    text = "Centre de messages",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                // Profile Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFCBD5E1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "VA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tabs Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val tabs = listOf("Message d'alarme", "Notification")
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
                                .width(120.dp)
                                .height(3.dp)
                                .background(if (isSelected) Color(0xFF10B981) else Color.Transparent)
                        )
                    }
                }
            }

            Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Content List
            if (selectedTab == 0) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(alarms) { alarm ->
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
                                    Text(text = "Nom de l'appareil", fontSize = 14.sp, color = Color.Gray)
                                    Text(text = alarm.deviceName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Heure", fontSize = 14.sp, color = Color.Gray)
                                    Text(text = alarm.time, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Type de message", fontSize = 14.sp, color = Color.Gray)
                                    Text(text = alarm.messageType, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Divider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Opérer", fontSize = 14.sp, color = Color.Gray)

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedButton(
                                            onClick = { /* View action */ },
                                            shape = RoundedCornerShape(20.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                                            modifier = Modifier
                                                .height(36.dp)
                                                .width(80.dp)
                                        ) {
                                            Text(text = "Voir", color = Color(0xFF10B981), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { /* Stop action */ },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            shape = RoundedCornerShape(20.dp),
                                            modifier = Modifier
                                                .height(36.dp)
                                                .width(90.dp)
                                        ) {
                                            Text(text = "Arrêter", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Aucune nouvelle notification", color = Color.Gray, fontSize = 15.sp)
                }
            }
        }
    }
}
