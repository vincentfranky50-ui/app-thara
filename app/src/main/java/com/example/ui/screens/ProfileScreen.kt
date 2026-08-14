package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.TharaCardBorder
import com.example.ui.theme.TharaGrayPill
import com.example.ui.theme.TharaGrayText
import com.example.ui.theme.TharaRed
import com.example.ui.theme.TharaRedLight
import com.example.ui.theme.TharaTextMuted
import com.example.ui.theme.TharaTextPrimary
import com.example.ui.theme.TharaTextSecondary

import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import com.example.model.UserSettings

@Composable
fun ProfileScreen(
    onNavigateToFleetList: () -> Unit,
    userSettings: UserSettings = UserSettings(),
    onOpenSettings: (() -> Unit)? = null,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var criticalAlertsEnabled by remember { mutableStateOf(userSettings.alertPreferences.pushCriticalAlerts) }
    var weeklyReportsEnabled by remember { mutableStateOf(userSettings.alertPreferences.emailWeeklyDigest) }
    var systemUpdatesEnabled by remember { mutableStateOf(false) }

    // Dynamic Figma / Google Stitch Theme Token Adaptations
    val bgCanvas by animateColorAsState(if (isDarkTheme) Color(0xFF0F172A) else MaterialTheme.colorScheme.background, label = "bg")
    val cardBg by animateColorAsState(if (isDarkTheme) Color(0xFF1E293B) else Color.White, label = "cardBg")
    val cardBorder by animateColorAsState(if (isDarkTheme) Color(0xFF334155) else TharaCardBorder, label = "cardBorder")
    val textPrimary by animateColorAsState(if (isDarkTheme) Color.White else TharaTextPrimary, label = "textPrimary")
    val textSecondary by animateColorAsState(if (isDarkTheme) Color(0xFF94A3B8) else TharaTextSecondary, label = "textSecondary")
    val avatarBg by animateColorAsState(if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF3F4F6), label = "avatarBg")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgCanvas)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Night Mode Active Banner Indicator (if Dark Theme enabled)
        if (isDarkTheme) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0284C7).copy(alpha = 0.15f))
                    .border(1.dp, Cyan400, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NightlightRound,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Mode Conduite Nocturne Actif",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Cyan400
                        )
                        Text(
                            text = "Contraste anti-éblouissement Figma / Stitch optimisé pour l'habitacle",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }

        // User Profile Card Header
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(avatarBg)
                        .border(2.dp, TharaRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Jean Dupont",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ID Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDarkTheme) Color(0xFF334155) else TharaGrayPill)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ID: TS-9042",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) Color(0xFFE2E8F0) else TharaGrayText
                        )
                    }

                    // Sector Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TharaRedLight)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Secteur: Dakar Nord",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TharaRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Edit Profile Button
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = TharaRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("edit_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Modifier le profil", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Quick Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TharaRedLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = TharaRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Statut", fontSize = 12.sp, color = textSecondary)
                        Text("Actif", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                }
            }

            // Tenure Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDarkTheme) Color(0xFF334155) else TharaGrayPill),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Ancienneté", fontSize = 12.sp, color = textSecondary)
                        Text("3 Ans", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                }
            }
        }

        // Fleet Directory Navigation Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToFleetList() }
                .testTag("open_fleet_directory_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(TharaRedLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = TharaRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Annuaire des Véhicules de Flotte", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text("Afficher les 142 véhicules et métriques", fontSize = 12.sp, color = textSecondary)
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = textSecondary
                )
            }
        }

        // Settings & Preferences Navigation Card (Alerts, Push, Email & Units)
        if (onOpenSettings != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Cyan400.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSettings() }
                    .testTag("open_settings_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Cyan500.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Cyan500,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Paramètres & Préférences", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(TharaRedLight)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${userSettings.measurementPreferences.speedUnit.symbol} • ${userSettings.measurementPreferences.distanceUnit.symbol}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TharaRed
                                    )
                                }
                            }
                            Text(
                                text = "Alertes (Push, Email) et Unités de mesure",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = textSecondary
                    )
                }
            }
        }

        // Account Information Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Informations du Compte",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                InfoRow("Prénom", "Jean", textPrimary, textSecondary)
                InfoRow("Nom", "Dupont", textPrimary, textSecondary)
                InfoRow("Adresse e-mail", "jean.dupont@tharaservices.com", textPrimary, textSecondary)
                InfoRow("Téléphone", "+221 77 123 45 67", textPrimary, textSecondary, isLast = true)
            }
        }

        // Preferences Card with Theme Toggle for Night Driving
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Préférences & Ergonomie Conduite",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                SwitchRow(
                    title = "Mode Sombre / Conduite Nocturne",
                    subtitle = "Réduit les éblouissements et l'astigmatisme en habitacle",
                    checked = isDarkTheme,
                    onCheckedChange = { onToggleTheme() },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    testTag = "dark_theme_switch"
                )
                SwitchRow(
                    title = "Alertes Push Critiques",
                    subtitle = "Notifications vocales et visuelles d'urgence",
                    checked = criticalAlertsEnabled,
                    onCheckedChange = { criticalAlertsEnabled = it },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
                SwitchRow(
                    title = "Rapports Hebdomadaires",
                    subtitle = "Bilan de consommation de carburant par e-mail",
                    checked = weeklyReportsEnabled,
                    onCheckedChange = { weeklyReportsEnabled = it },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
                SwitchRow(
                    title = "Mises à jour du Système OBD-II",
                    subtitle = "Téléchargement automatique du micrologiciel",
                    checked = systemUpdatesEnabled,
                    onCheckedChange = { systemUpdatesEnabled = it },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    isLast = true
                )
            }
        }

        // Security Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Sécurité & Accès",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Authentification Double Facteur (2FA)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                        Text("Activé via SMS / OTP", fontSize = 12.sp, color = textSecondary)
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Emerald500,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = textPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Changer le mot de passe", fontWeight = FontWeight.Bold, color = textPrimary)
                }
            }
        }

        // Logout Button
        OutlinedButton(
            onClick = onLogout,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TharaRed),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TharaRed),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = TharaRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Déconnexion Sécurisée", fontWeight = FontWeight.Bold, color = TharaRed)
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    textPrimary: Color,
    textSecondary: Color,
    isLast: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 14.sp, color = textSecondary)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
        }
        if (!isLast) {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    testTag: String? = null,
    isLast: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                if (subtitle != null) {
                    Text(text = subtitle, fontSize = 11.sp, color = textSecondary)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Cyan500,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFF64748B)
                )
            )
        }
        if (!isLast) {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

