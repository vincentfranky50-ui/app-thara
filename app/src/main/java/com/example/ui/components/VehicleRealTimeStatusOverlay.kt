package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Vehicle
import kotlinx.coroutines.delay

@Composable
fun VehicleRealTimeStatusOverlay(
    vehicle: Vehicle?,
    modifier: Modifier = Modifier
) {
    if (vehicle == null) return

    // Live simulated seconds ago ticker for last sync timestamp
    var secondsAgo by remember(vehicle.id) { mutableStateOf(2) }
    
    LaunchedEffect(vehicle.id) {
        while (true) {
            delay(1000L)
            secondsAgo++
        }
    }

    val signalQuality = when {
        vehicle.batteryPct > 80 -> "4G LTE (Excellente • 32ms)"
        vehicle.batteryPct > 40 -> "4G (Bonne • 58ms)"
        else -> "3G (Faible • 140ms)"
    }

    val signalColor = when {
        vehicle.batteryPct > 80 -> Color(0xFF10B981)
        vehicle.batteryPct > 40 -> Color(0xFF3B82F6)
        else -> Color(0xFFF59E0B)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xDD0F172A))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = "Qualité réseau",
                    tint = signalColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = signalQuality,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Dernière synchro",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Synchro : Il y a ${secondsAgo}s (${vehicle.licensePlate})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
