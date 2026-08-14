package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Undo
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Alert
import com.example.model.GeofenceZone
import com.example.model.LatLngPoint
import com.example.model.ZoneGeometryType
import com.example.model.ZoneType
import com.example.ui.theme.TharaBluePill
import com.example.ui.theme.TharaBlueText
import com.example.ui.theme.TharaCardBorder
import com.example.ui.theme.TharaGrayPill
import com.example.ui.theme.TharaRed
import com.example.ui.theme.TharaRedLight
import com.example.ui.theme.TharaTextMuted
import com.example.ui.theme.TharaTextPrimary
import com.example.ui.theme.TharaTextSecondary
import com.example.ui.theme.TharaYellowPill
import com.example.ui.theme.TharaYellowText

data class PoiPreset(
    val name: String,
    val category: String,
    val lat: Double,
    val lng: Double,
    val defaultRadius: Float,
    val defaultPolygon: List<LatLngPoint>
)

val POI_PRESETS = listOf(
    PoiPreset(
        name = "Port Autonome de Dakar (Mole 2)",
        category = "Zone Portuaire",
        lat = 14.6850,
        lng = -17.4300,
        defaultRadius = 1500f,
        defaultPolygon = listOf(
            LatLngPoint(14.6920, -17.4380),
            LatLngPoint(14.6950, -17.4220),
            LatLngPoint(14.6780, -17.4180),
            LatLngPoint(14.6750, -17.4350)
        )
    ),
    PoiPreset(
        name = "Dépôt BTP Diamniadio Pôle",
        category = "Logistique & BTP",
        lat = 14.7200,
        lng = -17.1800,
        defaultRadius = 2000f,
        defaultPolygon = listOf(
            LatLngPoint(14.7280, -17.1900),
            LatLngPoint(14.7300, -17.1700),
            LatLngPoint(14.7120, -17.1680),
            LatLngPoint(14.7100, -17.1880)
        )
    ),
    PoiPreset(
        name = "Aéroport International AIBD",
        category = "Aéroportuaire",
        lat = 14.6700,
        lng = -17.0733,
        defaultRadius = 3500f,
        defaultPolygon = listOf(
            LatLngPoint(14.6850, -17.0900),
            LatLngPoint(14.6880, -17.0600),
            LatLngPoint(14.6550, -17.0550),
            LatLngPoint(14.6520, -17.0850)
        )
    ),
    PoiPreset(
        name = "Zone Industrielle Bargny",
        category = "Industriel",
        lat = 14.6950,
        lng = -17.2250,
        defaultRadius = 1800f,
        defaultPolygon = listOf(
            LatLngPoint(14.7020, -17.2350),
            LatLngPoint(14.7050, -17.2150),
            LatLngPoint(14.6880, -17.2100),
            LatLngPoint(14.6850, -17.2300)
        )
    )
)

@Composable
fun GeofenceConfigScreen(
    geofences: List<GeofenceZone>,
    alerts: List<Alert>,
    onSaveGeofence: (GeofenceZone) -> Unit,
    onDeleteGeofence: (String) -> Unit,
    onTestNotif: ((String, Boolean) -> Unit)? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGeometryType by remember { mutableStateOf(ZoneGeometryType.POLYGON) }
    var zoneName by remember { mutableStateOf("Nouveau Périmètre Sécurisé") }
    var selectedCategory by remember { mutableStateOf("Logistique & BTP") }
    var selectedZoneType by remember { mutableStateOf(ZoneType.RESTRICTED) }
    var radiusMeters by remember { mutableFloatStateOf(1500f) }

    var centerLat by remember { mutableStateOf(14.7167) }
    var centerLng by remember { mutableStateOf(-17.4677) }

    var polygonPoints by remember {
        mutableStateOf(
            listOf(
                LatLngPoint(14.7250, -17.4750),
                LatLngPoint(14.7280, -17.4550),
                LatLngPoint(14.7080, -17.4500),
                LatLngPoint(14.7050, -17.4700)
            )
        )
    }

    var notifyOnEntry by remember { mutableStateOf(true) }
    var notifyOnExit by remember { mutableStateOf(true) }
    var syncToFirestore by remember { mutableStateOf(true) }

    var showNotificationDrawer by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Figma dark ergonomic canvas
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Zones & Géofencing",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Configuration polygones & notifications Firestore",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Live Firestore Notifications Drawer Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                    .clickable { showNotificationDrawer = !showNotificationDrawer }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Flux Firestore (${alerts.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // --- Notification Feed Modal Drawer ---
        AnimatedVisibility(visible = showNotificationDrawer) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Événements Firestore Cloud en Temps Réel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "Fermer",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TharaRed,
                            modifier = Modifier.clickable { showNotificationDrawer = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (alerts.isEmpty()) {
                        Text(
                            text = "Aucune alerte géofence enregistrée.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.height(140.dp)) {
                            items(alerts.take(5)) { alert ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = alert.message,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${alert.vehicleName} (${alert.licensePlate})",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF0F172A))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Firestore ✓",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                                HorizontalDivider(color = Color(0xFF334155))
                            }
                        }
                    }
                }
            }
        }

        // --- Main Content Scroll View ---
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- POI Presets Quick Selector Chips ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "POINTS D'INTÉRÊT PRÉRÉGLÉS (POI)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFF94A3B8)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(POI_PRESETS) { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                .clickable {
                                    zoneName = preset.name
                                    selectedCategory = preset.category
                                    centerLat = preset.lat
                                    centerLng = preset.lng
                                    radiusMeters = preset.defaultRadius
                                    polygonPoints = preset.defaultPolygon
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = TharaRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = preset.name.take(20) + "...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // --- Geometry Mode Selector (Polygon vs Circle) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. MODE DE GÉOMÉTRIE DE LA ZONE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Polygon Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedGeometryType == ZoneGeometryType.POLYGON) TharaRed else Color(0xFF0F172A))
                                .border(
                                    1.dp,
                                    if (selectedGeometryType == ZoneGeometryType.POLYGON) TharaRed else Color(0xFF334155),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedGeometryType = ZoneGeometryType.POLYGON }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Polyline,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Polygone",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Circle Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedGeometryType == ZoneGeometryType.CIRCLE) TharaRed else Color(0xFF0F172A))
                                .border(
                                    1.dp,
                                    if (selectedGeometryType == ZoneGeometryType.CIRCLE) TharaRed else Color(0xFF334155),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedGeometryType = ZoneGeometryType.CIRCLE }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RadioButtonChecked,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cercle",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Zone Name Input
                    OutlinedTextField(
                        value = zoneName,
                        onValueChange = { zoneName = it },
                        label = { Text("Nom de la zone POI", color = Color(0xFF94A3B8)) },
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
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("geofence_zone_name_input")
                    )

                    if (selectedGeometryType == ZoneGeometryType.CIRCLE) {
                        // Radius Slider for Circle
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rayon de couverture",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "${(radiusMeters / 1000f).let { String.format("%.1f km", it) }} (${radiusMeters.toInt()}m)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Slider(
                                value = radiusMeters,
                                onValueChange = { radiusMeters = it },
                                valueRange = 100f..10000f,
                                colors = SliderDefaults.colors(
                                    thumbColor = TharaRed,
                                    activeTrackColor = TharaRed,
                                    inactiveTrackColor = Color(0xFF334155)
                                )
                            )
                        }
                    } else {
                        // Polygon Vertex Tools
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sommets du polygone : ${polygonPoints.size} points",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        if (polygonPoints.isNotEmpty()) {
                                            polygonPoints = polygonPoints.dropLast(1)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Undo, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Annuler", fontSize = 11.sp, color = Color.White)
                                }

                                OutlinedButton(
                                    onClick = { polygonPoints = emptyList() },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TharaRed),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = TharaRed, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Effacer", fontSize = 11.sp, color = TharaRed)
                                }
                            }
                        }
                    }
                }
            }

            // --- Interactive Map Canvas for Drawing Polygon/Circle ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    InteractiveGeofenceCanvas(
                        geometryType = selectedGeometryType,
                        centerLat = centerLat,
                        centerLng = centerLng,
                        radiusMeters = radiusMeters,
                        polygonPoints = polygonPoints,
                        onAddPolygonPoint = { point ->
                            polygonPoints = polygonPoints + point
                        },
                        onCenterChanged = { lat, lng ->
                            centerLat = lat
                            centerLng = lng
                        }
                    )

                    // Overlay Instructions Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC0F172A))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (selectedGeometryType == ZoneGeometryType.POLYGON)
                                "👆 Touchez la carte pour placer les sommets du polygone"
                            else
                                "👆 Touchez la carte pour positionner le centre du cercle",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // --- Alert Triggers Configuration ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "2. DÉCLENCHEURS & NOTIFICATIONS FIRESTORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFF94A3B8)
                    )

                    // Entry Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Login, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Alerte à l'Entrée", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Notifier quand un véhicule pénètre la zone", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        Switch(
                            checked = notifyOnEntry,
                            onCheckedChange = { notifyOnEntry = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = TharaRed)
                        )
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    // Exit Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = TharaRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Alerte à la Sortie", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Notifier quand un véhicule quitte la zone", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        Switch(
                            checked = notifyOnExit,
                            onCheckedChange = { notifyOnExit = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = TharaRed)
                        )
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    // Firestore Sync Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Synchro Firestore Cloud", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Pousser en temps réel sur la base Firebase", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        Switch(
                            checked = syncToFirestore,
                            onCheckedChange = { syncToFirestore = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF3B82F6))
                        )
                    }
                }
            }

            // Save Zone Action Button
            Button(
                onClick = {
                    val newZone = GeofenceZone(
                        id = "ZONE-${System.currentTimeMillis() % 10000}",
                        name = zoneName.ifBlank { "Zone Périmètre" },
                        centerLat = centerLat,
                        centerLng = centerLng,
                        radiusMeters = radiusMeters,
                        type = selectedZoneType,
                        enterpriseId = "ENT-01",
                        activeVehicleCount = 0,
                        geometryType = selectedGeometryType,
                        polygonPoints = polygonPoints,
                        poiCategory = selectedCategory,
                        notifyOnEntry = notifyOnEntry,
                        notifyOnExit = notifyOnExit,
                        syncToFirestore = syncToFirestore
                    )
                    onSaveGeofence(newZone)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TharaRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_geofence_button")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enregistrer le Périmètre de Géofencing", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }

            // --- List of Configured Geofence Zones ---
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "PÉRIMÈTRES ACTIFS CONFIGURÉS (${geofences.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFF94A3B8)
                )

                geofences.forEach { zone ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (zone.geometryType == ZoneGeometryType.POLYGON)
                                                TharaRedLight
                                            else
                                                TharaBluePill
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (zone.geometryType == ZoneGeometryType.POLYGON) Icons.Default.Polyline else Icons.Default.RadioButtonChecked,
                                        contentDescription = null,
                                        tint = if (zone.geometryType == ZoneGeometryType.POLYGON) TharaRed else TharaBlueText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = zone.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${zone.geometryType.labelFr} • ${zone.poiCategory}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        if (zone.syncToFirestore) {
                                            Text("• Firestore ☁️", fontSize = 11.sp, color = Color(0xFF10B981))
                                        }
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (onTestNotif != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0F172A))
                                            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                                            .clickable { onTestNotif(zone.id, true) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("test_geofence_entry_button_${zone.id}")
                                    ) {
                                        Text("🚨 Test Entrée", fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0F172A))
                                            .border(1.dp, TharaRed, RoundedCornerShape(8.dp))
                                            .clickable { onTestNotif(zone.id, false) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("test_geofence_exit_button_${zone.id}")
                                    ) {
                                        Text("📍 Test Sortie", fontSize = 10.sp, color = TharaRed, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                IconButton(
                                    onClick = { onDeleteGeofence(zone.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Supprimer",
                                        tint = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveGeofenceCanvas(
    geometryType: ZoneGeometryType,
    centerLat: Double,
    centerLng: Double,
    radiusMeters: Float,
    polygonPoints: List<LatLngPoint>,
    onAddPolygonPoint: (LatLngPoint) -> Unit,
    onCenterChanged: (Double, Double) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()

    val mapCenterLat = 14.7200
    val mapCenterLng = -17.3800
    val latSpan = 0.18
    val lngSpan = 0.45

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08121E))
            .pointerInput(geometryType) {
                detectTapGestures { tapOffset ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()

                    val tappedLng = (mapCenterLng - lngSpan / 2) + (tapOffset.x / w) * lngSpan
                    val tappedLat = (mapCenterLat - latSpan / 2) + ((h - tapOffset.y) / h) * latSpan

                    if (geometryType == ZoneGeometryType.POLYGON) {
                        onAddPolygonPoint(LatLngPoint(tappedLat, tappedLng))
                    } else {
                        onCenterChanged(tappedLat, tappedLng)
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        // Topography Grid
        val gridColor = Color(0xFF1E293B)
        var x = 0f
        while (x < width) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
            x += 60f
        }
        var y = 0f
        while (y < height) {
            drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
            y += 60f
        }

        if (geometryType == ZoneGeometryType.CIRCLE) {
            val cx = ((centerLng - (mapCenterLng - lngSpan / 2)) / lngSpan) * width
            val cy = height - (((centerLat - (mapCenterLat - latSpan / 2)) / latSpan) * height)
            val radiusPx = (radiusMeters / 12000f) * width

            drawCircle(
                color = Color(0x33EF4444),
                radius = radiusPx,
                center = Offset(cx.toFloat(), cy.toFloat())
            )
            drawCircle(
                color = Color(0xFFEF4444),
                radius = radiusPx,
                center = Offset(cx.toFloat(), cy.toFloat()),
                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f))
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = Offset(cx.toFloat(), cy.toFloat())
            )
        } else {
            if (polygonPoints.isNotEmpty()) {
                val polyPath = Path()
                polygonPoints.forEachIndexed { idx, pt ->
                    val px = ((pt.longitude - (mapCenterLng - lngSpan / 2)) / lngSpan) * width
                    val py = height - (((pt.latitude - (mapCenterLat - latSpan / 2)) / latSpan) * height)
                    if (idx == 0) {
                        polyPath.moveTo(px.toFloat(), py.toFloat())
                    } else {
                        polyPath.lineTo(px.toFloat(), py.toFloat())
                    }

                    // Vertex marker dot
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 8f,
                        center = Offset(px.toFloat(), py.toFloat())
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(px.toFloat(), py.toFloat())
                    )
                }

                if (polygonPoints.size >= 3) {
                    polyPath.close()
                    drawPath(
                        path = polyPath,
                        color = Color(0x3310B981)
                    )
                }

                drawPath(
                    path = polyPath,
                    color = Color(0xFF10B981),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}
