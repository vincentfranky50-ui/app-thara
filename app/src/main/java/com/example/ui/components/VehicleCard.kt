package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Vehicle
import com.example.model.VehicleStatus
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

private data class VehicleStatusVisual(
    val label: String,
    val dotColor: Color,
    val bgColor: Color,
    val textColor: Color
)

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit,
    onToggleLock: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val statusVisual = when (vehicle.status) {
        VehicleStatus.MOVING -> VehicleStatusVisual("En mouvement", Color(0xFF22C55E), Color(0xFFDCFCE7), Color(0xFF15803D))
        VehicleStatus.IDLE -> VehicleStatusVisual("Ralenti (ON)", Color(0xFFF59E0B), Color(0xFFFEF3C7), Color(0xFFB45309))
        VehicleStatus.STOPPED -> VehicleStatusVisual("En arrêt", Color(0xFF64748B), Color(0xFFF1F5F9), Color(0xFF475569))
        VehicleStatus.OFFLINE -> VehicleStatusVisual("Hors ligne", Color(0xFF94A3B8), Color(0xFFF8FAFC), Color(0xFF64748B))
        VehicleStatus.ALERT_GEOFENCE -> VehicleStatusVisual("Alerte Zone", Color(0xFFEF4444), Color(0xFFFEE2E2), Color(0xFFB91C1C))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vehicle_card_${vehicle.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) TharaRed else TharaCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Title + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = vehicle.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TharaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = vehicle.enterpriseName.ifEmpty { "Freightliner Cascadia" },
                        fontSize = 14.sp,
                        color = TharaTextSecondary
                    )
                }

                // Top Right Pill with Colored Status Dot & Label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusVisual.bgColor)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("status_pill_${vehicle.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Operational Status Dot Indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusVisual.dotColor)
                                .testTag("status_dot_${vehicle.id}")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusVisual.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusVisual.textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body Row: Photo / Truck Box + Info Column
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Truck Image / Icon Box Placeholder with Status Indicator Badge
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .border(1.dp, TharaCardBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(40.dp)
                    )

                    // Corner Status Badge Dot on Thumbnail
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(statusVisual.dotColor)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Info Details Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Location
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TharaTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = vehicle.address,
                            fontSize = 14.sp,
                            color = TharaTextPrimary,
                            maxLines = 1
                        )
                    }

                    // Metric / Alert
                    if (vehicle.status == VehicleStatus.ALERT_GEOFENCE) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = TharaRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Low Tire Pressure",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TharaRed
                            )
                        }
                    } else if (vehicle.status == VehicleStatus.STOPPED) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = TharaTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Est. completion 14:00",
                                fontSize = 14.sp,
                                color = TharaTextSecondary
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = TharaTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${vehicle.speedKmH.toInt()} mph",
                                fontSize = 14.sp,
                                color = TharaTextPrimary
                            )
                        }
                    }

                    // Fuel Progress Bar Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { vehicle.fuelLevelPct / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = TharaRed,
                            trackColor = Color(0xFFE5E7EB)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "${vehicle.fuelLevelPct}% Fuel",
                            fontSize = 13.sp,
                            color = TharaTextSecondary
                        )
                    }
                }
            }
        }
    }
}
