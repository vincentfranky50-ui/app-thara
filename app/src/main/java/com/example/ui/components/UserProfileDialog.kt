package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun UserProfileDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var notificationEnabled by remember { mutableStateOf(true) }
    var soundNotificationEnabled by remember { mutableStateOf(true) }
    val primaryBlue = Color(0xFF00A2FF)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Close Button
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(primaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Email
                Text(
                    text = "kmouafo@gmail.com",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Menu Items
                ProfileMenuItem(
                    icon = Icons.Default.Devices,
                    title = "Gestion des appareils",
                    onClick = { Toast.makeText(context, "Gestion des appareils", Toast.LENGTH_SHORT).show() }
                )
                ProfileMenuItem(
                    icon = Icons.Default.PersonOutline,
                    title = "Informations personnelles",
                    onClick = { Toast.makeText(context, "Informations personnelles", Toast.LENGTH_SHORT).show() }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Info,
                    title = "À propos",
                    onClick = { Toast.makeText(context, "À propos de TRARA TRACK", Toast.LENGTH_SHORT).show() }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Changer le mot de passe",
                    onClick = { Toast.makeText(context, "Changer le mot de passe", Toast.LENGTH_SHORT).show() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Notification Switch
                ProfileSwitchItem(
                    icon = Icons.Default.ChatBubbleOutline,
                    title = "Notification",
                    checked = notificationEnabled,
                    onCheckedChange = { notificationEnabled = it }
                )

                // Sound Notification Switch
                ProfileSwitchItem(
                    icon = Icons.Default.VolumeUp,
                    title = "Son de notification",
                    checked = soundNotificationEnabled,
                    onCheckedChange = { soundNotificationEnabled = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Logout Button
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Déconnexion réussie", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF00B074)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00B074)),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Déconnexion",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
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
                tint = Color(0xFF64748B),
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
}

@Composable
fun ProfileSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                tint = Color(0xFF64748B),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                fontSize = 15.sp,
                color = Color(0xFF1E293B),
                fontWeight = FontWeight.Medium
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00B074)
            )
        )
    }
}
