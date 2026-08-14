package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@Composable
fun ZoneConfigurationSheet(
    initialZoneName: String = "Downtown Sector",
    initialRadiusMeters: Float = 1500f,
    onRadiusChanged: (Float) -> Unit = {},
    onSaveZone: (String, Float) -> Unit = { _, _ -> },
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var zoneName by remember { mutableStateOf(initialZoneName) }
    var radiusMeters by remember { mutableFloatStateOf(initialRadiusMeters) }

    var vehicleEntryAlert by remember { mutableStateOf(true) }
    var vehicleExitAlert by remember { mutableStateOf(true) }
    var dwellTimeAlert by remember { mutableStateOf(false) }

    val formattedRadius = if (radiusMeters >= 1000f) {
        String.format("%.1f km", radiusMeters / 1000f)
    } else {
        "${radiusMeters.toInt()}m"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TharaCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("zone_configuration_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Drag handle pill at top
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFD1D5DB))
                    .align(Alignment.CenterHorizontally)
            )

            // Header Row: Title + Live Edit Badge + Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Zone Configuration",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TharaTextPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Live Edit Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TharaRedLight)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(TharaRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Edit",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TharaRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TharaTextSecondary
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE5E7EB))

            // 1. Zone Name Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Zone Name",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TharaTextSecondary
                )

                OutlinedTextField(
                    value = zoneName,
                    onValueChange = { zoneName = it },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TharaRed
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TharaRed,
                        unfocusedBorderColor = TharaCardBorder,
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("zone_name_input")
                )
            }

            // 2. Radius Slider
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Radius",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TharaTextSecondary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TharaGrayPill)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = formattedRadius,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TharaTextPrimary
                        )
                    }
                }

                Slider(
                    value = radiusMeters,
                    onValueChange = { newRadius ->
                        radiusMeters = newRadius
                        onRadiusChanged(newRadius)
                    },
                    valueRange = 50f..5000f,
                    colors = SliderDefaults.colors(
                        thumbColor = TharaRed,
                        activeTrackColor = TharaRed,
                        inactiveTrackColor = Color(0xFFE5E7EB)
                    ),
                    modifier = Modifier.testTag("zone_radius_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("50m", fontSize = 11.sp, color = TharaTextMuted)
                    Text("5km", fontSize = 11.sp, color = TharaTextMuted)
                }
            }

            HorizontalDivider(color = Color(0xFFE5E7EB))

            // 3. Alert Triggers Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ALERT TRIGGERS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = TharaTextSecondary
                )

                // Vehicle Entry
                TriggerSwitchRow(
                    icon = Icons.Default.Login,
                    title = "Vehicle Entry",
                    subtitle = "Trigger alert on zone enter",
                    checked = vehicleEntryAlert,
                    onCheckedChange = { vehicleEntryAlert = it }
                )

                // Vehicle Exit
                TriggerSwitchRow(
                    icon = Icons.Default.Logout,
                    title = "Vehicle Exit",
                    subtitle = "Trigger alert on zone leave",
                    checked = vehicleExitAlert,
                    onCheckedChange = { vehicleExitAlert = it }
                )

                // Dwell Time
                TriggerSwitchRow(
                    icon = Icons.Default.Schedule,
                    title = "Dwell Time",
                    subtitle = "Alert if idle > 15 mins",
                    checked = dwellTimeAlert,
                    onCheckedChange = { dwellTimeAlert = it }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Action Buttons Row (Cancel / Save Zone)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClose,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TharaCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("cancel_zone_button")
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold, color = TharaTextPrimary)
                }

                Button(
                    onClick = { onSaveZone(zoneName, radiusMeters) },
                    colors = ButtonDefaults.buttonColors(containerColor = TharaRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_zone_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Zone", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun TriggerSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TharaCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TharaRedLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TharaRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TharaTextPrimary)
                    Text(text = subtitle, fontSize = 12.sp, color = TharaTextMuted)
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = TharaRed,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD1D5DB)
                )
            )
        }
    }
}
