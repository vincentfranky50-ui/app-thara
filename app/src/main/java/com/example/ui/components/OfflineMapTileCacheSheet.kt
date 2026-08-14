package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet4Bar
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Vehicle
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.TharaCardBorder
import com.example.ui.theme.TharaRed
import com.example.ui.theme.TharaRedLight
import com.example.ui.theme.TharaTextPrimary
import com.example.ui.theme.TharaTextSecondary
import com.example.util.CacheStats
import com.example.util.MapTileCacheManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineMapTileCacheSheet(
    activeVehicle: Vehicle?,
    vehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean = false,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var cacheStats by remember { mutableStateOf(MapTileCacheManager.getCacheStats(context)) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadCount by remember { mutableStateOf(0) }
    var downloadTotal by remember { mutableStateOf(0) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSimulatingOffline by remember { mutableStateOf(MapTileCacheManager.isSimulateOffline()) }

    val isOnline = MapTileCacheManager.isNetworkAvailable(context)

    val bgCanvas = if (isDarkTheme) Color(0xFF0F172A) else Color.White
    val cardBg = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val cardBorder = if (isDarkTheme) Color(0xFF334155) else TharaCardBorder
    val textPrimary = if (isDarkTheme) Color.White else TharaTextPrimary
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else TharaTextSecondary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bgCanvas,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("offline_tile_cache_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Cyan500.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = Cyan500,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Cache des Tuiles Hors-Ligne",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "Visualisation des véhicules sans connexion Internet",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                }

                // Connectivity pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isOnline) Emerald500.copy(alpha = 0.15f) else TharaRedLight)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Emerald500 else TharaRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOnline) "En Ligne" else "Hors-Ligne",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOnline) Emerald500 else TharaRed
                        )
                    }
                }
            }

            // Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = Cyan500,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "État du Cache Disque (Osmdroid)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                cacheStats = MapTileCacheManager.getCacheStats(context)
                                Toast.makeText(context, "Cache actualisé", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualiser",
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Metric 1: Tiles count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgCanvas)
                                .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Tuiles Stockées",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${cacheStats.tileCount}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textPrimary
                                )
                            }
                        }

                        // Metric 2: Storage Size
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgCanvas)
                                .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Espace Utilisé",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cacheStats.formattedSize,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Cyan500
                                )
                            }
                        }
                    }
                }
            }

            // Download Progress Bar if active
            AnimatedVisibility(visible = isDownloading) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Cyan500.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Cyan500.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Téléchargement des tuiles cartographiques...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "$downloadCount / $downloadTotal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cyan500
                            )
                        }

                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Cyan500,
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            if (statusMessage != null && !isDownloading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Emerald500.copy(alpha = 0.1f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Emerald500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusMessage ?: "",
                        fontSize = 12.sp,
                        color = Emerald500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Button 1: Download around active vehicle
                Button(
                    onClick = {
                        if (!isOnline) {
                            Toast.makeText(context, "Connexion Internet requise pour précharger les tuiles", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val centerLat = activeVehicle?.latitude ?: 14.7167
                        val centerLng = activeVehicle?.longitude ?: -17.4677

                        isDownloading = true
                        statusMessage = null
                        coroutineScope.launch {
                            val count = MapTileCacheManager.precacheOsmdroidArea(
                                context = context,
                                centerLat = centerLat,
                                centerLng = centerLng,
                                radiusKm = 12.0,
                                minZoom = 12,
                                maxZoom = 16,
                                onProgress = { current, total ->
                                    downloadCount = current
                                    downloadTotal = total
                                    downloadProgress = if (total > 0) current.toFloat() / total.toFloat() else 0f
                                }
                            )
                            isDownloading = false
                            cacheStats = MapTileCacheManager.getCacheStats(context)
                            statusMessage = "$count tuiles téléchargées avec succès pour ${activeVehicle?.name ?: "le véhicule"} !"
                        }
                    },
                    enabled = !isDownloading && isOnline,
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("download_vehicle_zone_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mettre en cache la zone (${activeVehicle?.name ?: "Véhicule"})",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Button 2: Download entire fleet zones
                OutlinedButton(
                    onClick = {
                        if (!isOnline) {
                            Toast.makeText(context, "Connexion Internet requise pour précharger les tuiles", Toast.LENGTH_SHORT).show()
                            return@OutlinedButton
                        }

                        isDownloading = true
                        statusMessage = null
                        coroutineScope.launch {
                            var totalSaved = 0
                            val targets = vehicles.ifEmpty { listOfNotNull(activeVehicle) }
                            for (v in targets) {
                                val c = MapTileCacheManager.precacheOsmdroidArea(
                                    context = context,
                                    centerLat = v.latitude,
                                    centerLng = v.longitude,
                                    radiusKm = 8.0,
                                    minZoom = 12,
                                    maxZoom = 15,
                                    onProgress = { current, total ->
                                        downloadCount = current
                                        downloadTotal = total
                                        downloadProgress = if (total > 0) current.toFloat() / total.toFloat() else 0f
                                    }
                                )
                                totalSaved += c
                            }
                            isDownloading = false
                            cacheStats = MapTileCacheManager.getCacheStats(context)
                            statusMessage = "Flotte complète ($totalSaved tuiles) enregistrée dans le cache hors-ligne !"
                        }
                    },
                    enabled = !isDownloading && isOnline,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("download_all_fleet_zones_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Précharger toutes les positions de la flotte",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                    }
                }
            }

            HorizontalDivider(color = cardBorder)

            // Simulate Offline Toggle (for instant testing)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSimulatingOffline) TharaRedLight else Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSimulatingOffline) Icons.Default.WifiOff else Icons.Default.Wifi,
                            contentDescription = null,
                            tint = if (isSimulatingOffline) TharaRed else Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Simulation Mode Hors-Ligne",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "Force la carte à utiliser exclusivement les tuiles en cache",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                }

                Switch(
                    checked = isSimulatingOffline,
                    onCheckedChange = { checked ->
                        isSimulatingOffline = checked
                        MapTileCacheManager.setSimulateOffline(checked)
                        Toast.makeText(
                            context,
                            if (checked) "Mode Hors-Ligne activé (Simulation)" else "Mode En Ligne rétabli",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.testTag("toggle_simulate_offline"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TharaRed,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF94A3B8)
                    )
                )
            }

            // Clear Cache Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            MapTileCacheManager.clearCache(context)
                            cacheStats = MapTileCacheManager.getCacheStats(context)
                            statusMessage = null
                            Toast.makeText(context, "Cache vidé avec succès", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("clear_tile_cache_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Vider le cache",
                        tint = TharaRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Vider le cache de la carte",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TharaRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
