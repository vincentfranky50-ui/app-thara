package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
fun ControlInstructionsScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var speedLimitEnabled by remember { mutableStateOf(false) }
    var speedLimitValue by remember { mutableStateOf(110f) }

    // Dialog state controllers for precise task execution
    var showEngineCutDialog by remember { mutableStateOf(false) }
    var showFuelRestoreDialog by remember { mutableStateOf(false) }
    var showAlarmStopDialog by remember { mutableStateOf(false) }
    var showSilentArmingDialog by remember { mutableStateOf(false) }
    var showRequestFeesDialog by remember { mutableStateOf(false) }
    var showAudioListenDialog by remember { mutableStateOf(false) }
    var showSpeedLimitDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color(0xFF1E293B)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Commandes et Instructions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Instructions List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            ControlItem(
                icon = Icons.Default.PowerOff,
                iconTint = Color(0xFFEF4444),
                title = "Arrêter le moteur",
                subtitle = "Couper l'alimentation carburant/allumage à distance",
                onClick = { showEngineCutDialog = true }
            )
            ControlItem(
                icon = Icons.Default.FlashOn,
                iconTint = Color(0xFFF59E0B),
                title = "Restaurer le gaz et l'électricité",
                subtitle = "Rétablir les circuits d'allumage et de carburant",
                onClick = { showFuelRestoreDialog = true }
            )
            ControlItem(
                icon = Icons.Default.NotificationsOff,
                iconTint = Color(0xFF6366F1),
                title = "Arrêter l'alarme",
                subtitle = "Désactiver la sirène embarquée à distance",
                onClick = { showAlarmStopDialog = true }
            )
            ControlItem(
                icon = Icons.Default.Security,
                iconTint = Color(0xFFEAB308),
                title = "Armer silencieusement",
                subtitle = "Activer l'alarme silencieuse avec notification push",
                onClick = { showSilentArmingDialog = true }
            )
            ControlItem(
                icon = Icons.Default.Receipt,
                iconTint = Color(0xFF3B82F6),
                title = "Frais de requête",
                subtitle = "Consulter le journal et les coûts des requêtes SMS/GPRS",
                onClick = { showRequestFeesDialog = true }
            )
            ControlItem(
                icon = Icons.Default.Hearing,
                iconTint = Color(0xFF10B981),
                title = "Écouter",
                subtitle = "Activer le micro espion cabine en temps réel",
                onClick = { showAudioListenDialog = true }
            )
            
            // Speeding limit item with toggle switch and value
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSpeedLimitDialog = true }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = Color(0xFF06B6D4),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Dépassement de vitesse",
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Seuil actuel : ${speedLimitValue.toInt()} km/h",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Switch(
                        checked = speedLimitEnabled,
                        onCheckedChange = { 
                            speedLimitEnabled = it
                            Toast.makeText(context, "Alerte vitesse : ${if (it) "Activée (${speedLimitValue.toInt()} km/h)" else "Désactivée"}", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF10B981))
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // 1. ENGINE CUT DIALOG
    if (showEngineCutDialog) {
        AlertDialog(
            onDismissRequest = { showEngineCutDialog = false },
            icon = { Icon(Icons.Default.PowerOff, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(36.dp)) },
            title = { Text("Arrêt du moteur à distance") },
            text = { 
                Text("Attention : Cette commande va couper l'alimentation en carburant et en électricité du véhicule de manière sécurisée dès que la vitesse sera inférieure à 20 km/h.\n\nConfirmez-vous l'envoi de l'ordre d'arrêt d'urgence ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEngineCutDialog = false
                        Toast.makeText(context, "⚡ Ordre d'arrêt moteur transmis avec succès au traceur !", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Exécuter l'arrêt", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEngineCutDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // 2. FUEL & POWER RESTORE DIALOG
    if (showFuelRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showFuelRestoreDialog = false },
            icon = { Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(36.dp)) },
            title = { Text("Restaurer le gaz et l'électricité") },
            text = { 
                Text("Cette action rétablira l'alimentation en carburant et l'allumage du véhicule. Le moteur pourra à nouveau être démarré par le conducteur.\n\nVoulez-vous procéder au rétablissement ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFuelRestoreDialog = false
                        Toast.makeText(context, "⚡ Circuits de carburant et d'allumage rétablis avec succès.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Rétablir", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFuelRestoreDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // 3. ALARM STOP DIALOG
    if (showAlarmStopDialog) {
        AlertDialog(
            onDismissRequest = { showAlarmStopDialog = false },
            icon = { Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(36.dp)) },
            title = { Text("Arrêter l'alarme embarquée") },
            text = { 
                Text("Voulez-vous couper immédiatement la sirène de l'alarme anti-vol en cours sur le véhicule ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAlarmStopDialog = false
                        Toast.makeText(context, "🔕 Alarme sirène coupée à distance.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text("Couper l'alarme", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlarmStopDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    // 4. SILENT ARMING DIALOG
    if (showSilentArmingDialog) {
        AlertDialog(
            onDismissRequest = { showSilentArmingDialog = false },
            icon = { Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(36.dp)) },
            title = { Text("Armement silencieux") },
            text = { 
                Text("En mode armé silencieux, toute tentative d'effraction ou de déplacement non autorisé déclenchera une notification Push et SMS instantanée sur votre téléphone sans faire retentir la sirène extérieure.\n\nActiver le mode silencieux ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSilentArmingDialog = false
                        Toast.makeText(context, "🛡️ Véhicule armé en mode silencieux.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308))
                ) {
                    Text("Activer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSilentArmingDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // 5. REQUEST FEES DIALOG
    if (showRequestFeesDialog) {
        AlertDialog(
            onDismissRequest = { showRequestFeesDialog = false },
            title = { Text("Frais et Journal des Requêtes") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Historique des commandes et coût de communication GPRS / SMS :", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(4.dp))
                    RequestFeeItem("Arrêt moteur (MQTT)", "0.00 XAF (Gratuit)", "Succès")
                    RequestFeeItem("Position GPS (GPRS)", "0.00 XAF (Gratuit)", "Succès")
                    RequestFeeItem("Alerte SMS Géofence", "15.00 XAF", "Délivré")
                    RequestFeeItem("Mise à jour Firmware", "0.00 XAF", "Terminé")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Solde de crédit SMS actuel : 4,850 XAF", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                }
            },
            confirmButton = {
                Button(onClick = { showRequestFeesDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    // 6. AUDIO LISTEN DIALOG
    if (showAudioListenDialog) {
        AlertDialog(
            onDismissRequest = { showAudioListenDialog = false },
            icon = { Icon(Icons.Default.Hearing, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(36.dp)) },
            title = { Text("Écoute audio cabine en direct") },
            text = { 
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Le micro espion du boîtier GPS est en cours de connexion sécurisée...", fontSize = 13.sp, color = Color(0xFF64748B))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎙️ Canal Audio Ouvert (HD Audio)", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAudioListenDialog = false
                        Toast.makeText(context, "Session d'écoute audio terminée.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Raccrocher", color = Color.White)
                }
            }
        )
    }

    // 7. SPEED LIMIT DIALOG
    if (showSpeedLimitDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedLimitDialog = false },
            title = { Text("Configuration dépassement de vitesse") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Définissez la vitesse limite maximale tolérée. Une alerte instantanée sera émise en cas de dépassement.", fontSize = 13.sp, color = Color(0xFF64748B))
                    Text("Seuil : ${speedLimitValue.toInt()} km/h", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0284C7))
                    Slider(
                        value = speedLimitValue,
                        onValueChange = { speedLimitValue = it },
                        valueRange = 30f..180f,
                        steps = 15
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        speedLimitEnabled = true
                        showSpeedLimitDialog = false
                        Toast.makeText(context, "Seuil de vitesse enregistré : ${speedLimitValue.toInt()} km/h", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Enregistrer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSpeedLimitDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun RequestFeeItem(action: String, cost: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(action, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            Text(status, fontSize = 10.sp, color = Color(0xFF10B981))
        }
        Text(cost, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
    }
}

@Composable
fun ControlItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
        HorizontalDivider(
            color = Color(0xFFF1F5F9),
            thickness = 1.dp,
            modifier = Modifier.padding(start = 60.dp)
        )
    }
}
