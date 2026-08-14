package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlertChannelPreferences
import com.example.model.DistanceUnit
import com.example.model.SpeedUnit
import com.example.model.UserMeasurementPreferences
import com.example.model.UserSettings
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.TharaCardBorder
import com.example.ui.theme.TharaRed
import com.example.ui.theme.TharaRedLight
import com.example.ui.theme.TharaTextPrimary
import com.example.ui.theme.TharaTextSecondary

@Composable
fun SettingsScreen(
    userSettings: UserSettings,
    onSaveSettings: (UserSettings) -> Unit,
    onBack: (() -> Unit)? = null,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Local mutable state initialized from userSettings
    var emailCritical by remember { mutableStateOf(userSettings.alertPreferences.emailCriticalAlerts) }
    var emailWeekly by remember { mutableStateOf(userSettings.alertPreferences.emailWeeklyDigest) }
    var emailGeofence by remember { mutableStateOf(userSettings.alertPreferences.emailGeofenceBreach) }
    var emailMaintenance by remember { mutableStateOf(userSettings.alertPreferences.emailMaintenanceDue) }

    var pushCritical by remember { mutableStateOf(userSettings.alertPreferences.pushCriticalAlerts) }
    var pushGeofence by remember { mutableStateOf(userSettings.alertPreferences.pushGeofenceBreach) }
    var pushSpeeding by remember { mutableStateOf(userSettings.alertPreferences.pushSpeedingAlerts) }
    var pushIgnition by remember { mutableStateOf(userSettings.alertPreferences.pushEngineIgnition) }

    var speedUnit by remember { mutableStateOf(userSettings.measurementPreferences.speedUnit) }
    var distanceUnit by remember { mutableStateOf(userSettings.measurementPreferences.distanceUnit) }
    var emailRecipient by remember { mutableStateOf(userSettings.emailRecipient) }

    // Colors matching Figma / Google Stitch spec
    val bgCanvas by animateColorAsState(if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC), label = "bg")
    val cardBg by animateColorAsState(if (isDarkTheme) Color(0xFF1E293B) else Color.White, label = "cardBg")
    val cardBorder by animateColorAsState(if (isDarkTheme) Color(0xFF334155) else TharaCardBorder, label = "cardBorder")
    val textPrimary by animateColorAsState(if (isDarkTheme) Color.White else TharaTextPrimary, label = "textPrimary")
    val textSecondary by animateColorAsState(if (isDarkTheme) Color(0xFF94A3B8) else TharaTextSecondary, label = "textSecondary")
    val inputBg by animateColorAsState(if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9), label = "inputBg")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgCanvas)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP APP BAR / HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0))
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TharaRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Paramètres & Préférences",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textPrimary
                    )
                    Text(
                        text = "Alertes Push, Email & Unités de Mesure",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
            }
        }

        // 1. UNITES DE MESURE (KM/H vs MPH & KM vs MILES)
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("measurement_units_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Cyan500.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = Cyan500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Unités de Mesure Télématique",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "Personnalisation des tachymètres et odomètres de la flotte",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vitesse (Speed Unit Selector)
                Text(
                    text = "Unité de Vitesse",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option km/h
                    val isKmh = speedUnit == SpeedUnit.KMH
                    UnitSelectorPill(
                        title = "Kilomètres/h (km/h)",
                        subtitle = "Système métrique standard",
                        isSelected = isKmh,
                        onClick = { speedUnit = SpeedUnit.KMH },
                        cardBg = cardBg,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        modifier = Modifier.weight(1f),
                        testTag = "unit_kmh_selector"
                    )

                    // Option mph
                    val isMph = speedUnit == SpeedUnit.MPH
                    UnitSelectorPill(
                        title = "Miles/h (mph)",
                        subtitle = "Système impérial",
                        isSelected = isMph,
                        onClick = { speedUnit = SpeedUnit.MPH },
                        cardBg = cardBg,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        modifier = Modifier.weight(1f),
                        testTag = "unit_mph_selector"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Distance (Distance Unit Selector)
                Text(
                    text = "Unité de Distance (Odomètre & Trajets)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isKm = distanceUnit == DistanceUnit.KM
                    UnitSelectorPill(
                        title = "Kilomètres (km)",
                        subtitle = "1.000 mètres",
                        isSelected = isKm,
                        onClick = { distanceUnit = DistanceUnit.KM },
                        cardBg = cardBg,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        modifier = Modifier.weight(1f),
                        testTag = "unit_km_selector"
                    )

                    val isMiles = distanceUnit == DistanceUnit.MILES
                    UnitSelectorPill(
                        title = "Miles (mi)",
                        subtitle = "1 mi ≈ 1.609 km",
                        isSelected = isMiles,
                        onClick = { distanceUnit = DistanceUnit.MILES },
                        cardBg = cardBg,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        modifier = Modifier.weight(1f),
                        testTag = "unit_miles_selector"
                    )
                }
            }
        }

        // 2. ALERTES NOTIFICATIONS PUSH (MOBILE & HABITACLE)
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("push_notifications_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(TharaRedLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = TharaRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Notifications Push (En direct)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "Alertes instantanées sur l'application et la barre de notification",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                SettingsToggleRow(
                    icon = Icons.Default.Warning,
                    iconTint = TharaRed,
                    title = "Alertes Critiques & Urgences (SOS / Choc)",
                    subtitle = "Accéléromètre anormal, freinage d'urgence ou coupure tracker",
                    checked = pushCritical,
                    onCheckedChange = { pushCritical = it },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    testTag = "toggle_push_critical"
                )

                HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))

                SettingsToggleRow(
                    icon = Icons.Default.LocationOn,
                    iconTint = Cyan500,
                    title = "Franchissement de Géofencing",
                    subtitle = "Entrée et sortie des zones autorisées (Dakar Port, Dépôt)",
                    checked = pushGeofence,
                    onCheckedChange = { pushGeofence = it },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    testTag = "toggle_push_geofence"
                )

                HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))

                SettingsToggleRow(
                    icon = Icons.Default.Speed,
                    iconTint = Color(0xFFF59E0B),
                    title = "Dépassement de Vitesse Limite",
                    subtitle = "Alerte en temps réel si vitesse > seuil maximal configuré",
                    checked = pushSpeeding,
                    onCheckedChange = { pushSpeeding = it },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    testTag = "toggle_push_speeding"
                )

                HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))

                SettingsToggleRow(
                    icon = Icons.Default.PowerSettingsNew,
                    iconTint = Emerald500,
                    title = "Démarrage / Coupure Moteur (ACC)",
                    subtitle = "Notification à chaque allumage et arrêt du contact véhicule",
                    checked = pushIgnition,
                    onCheckedChange = { pushIgnition = it },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    testTag = "toggle_push_ignition"
                )
            }
        }

        // 3. ALERTES PAR EMAIL & COMPTES-RENDUS
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_notifications_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Notifications & Rapports par Email",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "Rapports hebdomadaires et alertes de sécurité par courrier électronique",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Email recipient field
                OutlinedTextField(
                    value = emailRecipient,
                    onValueChange = { emailRecipient = it },
                    label = { Text("Adresse email de réception des alertes", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_recipient_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TharaRed,
                        unfocusedBorderColor = cardBorder,
                        focusedContainerColor = inputBg,
                        unfocusedContainerColor = inputBg
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingsToggleRow(
                    icon = Icons.Default.Warning,
                    iconTint = TharaRed,
                    title = "Emails d'Alertes Critiques",
                    subtitle = "Transmission immédiate des incidents majeurs",
                    checked = emailCritical,
                    onCheckedChange = { emailCritical = it },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    testTag = "toggle_email_critical"
                )

                HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))

                SettingsToggleRow(
                    icon = Icons.Default.AutoAwesome,
                    iconTint = Color(0xFF8B5CF6),
                    title = "Synthèse Hebdomadaire Flotte (PDF / Excel)",
                    subtitle = "Bilan hebdomadaire automatisé avec diagnostics IA Gemini",
                    checked = emailWeekly,
                    onCheckedChange = { emailWeekly = it },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    testTag = "toggle_email_weekly"
                )

                HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))

                SettingsToggleRow(
                    icon = Icons.Default.LocationOn,
                    iconTint = Cyan500,
                    title = "Rapports d'Incursion Géofence",
                    subtitle = "Notification par email lors de sorties de zone non autorisées",
                    checked = emailGeofence,
                    onCheckedChange = { emailGeofence = it },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    testTag = "toggle_email_geofence"
                )

                HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))

                SettingsToggleRow(
                    icon = Icons.Default.Tune,
                    iconTint = Emerald500,
                    title = "Rappels de Maintenance Prédictive OBD-II",
                    subtitle = "Prévision d'usure des freins, batterie et vidange moteur",
                    checked = emailMaintenance,
                    onCheckedChange = { emailMaintenance = it },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    testTag = "toggle_email_maintenance"
                )
            }
        }

        // SAVE BUTTON (Figma floating bar action)
        Button(
            onClick = {
                val updatedSettings = UserSettings(
                    alertPreferences = AlertChannelPreferences(
                        emailCriticalAlerts = emailCritical,
                        emailWeeklyDigest = emailWeekly,
                        emailGeofenceBreach = emailGeofence,
                        emailMaintenanceDue = emailMaintenance,
                        pushCriticalAlerts = pushCritical,
                        pushGeofenceBreach = pushGeofence,
                        pushSpeedingAlerts = pushSpeeding,
                        pushEngineIgnition = pushIgnition,
                        smsEmergencyOnly = true
                    ),
                    measurementPreferences = UserMeasurementPreferences(
                        speedUnit = speedUnit,
                        distanceUnit = distanceUnit,
                        fuelUnit = "L/100km",
                        temperatureUnit = "°C"
                    ),
                    emailRecipient = emailRecipient
                )
                onSaveSettings(updatedSettings)
                Toast.makeText(context, "Préférences et unités enregistrées avec succès !", Toast.LENGTH_SHORT).show()
                onBack?.invoke()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_settings_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TharaRed),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Enregistrer les Préférences",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun UnitSelectorPill(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val borderColor = if (isSelected) TharaRed else Color(0xFFE2E8F0)
    val pillBg = if (isSelected) TharaRed.copy(alpha = 0.08f) else cardBg

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(pillBg)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) TharaRed else textPrimary
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = TharaRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = textSecondary
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    testTag: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = textSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TharaRed,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF94A3B8)
            )
        )
    }
}
