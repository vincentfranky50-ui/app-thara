package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.TharaRed
import com.example.model.GeofenceZone
import com.example.model.Vehicle
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet4Bar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.util.MapTileCacheManager
import kotlinx.coroutines.launch

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex

val CyclOSMSource = XYTileSource(
    "CyclOSM",
    0, 20, 256, ".png",
    arrayOf(
        "https://a.tile-cyclosm.openstreetmap.fr/cyclosm/",
        "https://b.tile-cyclosm.openstreetmap.fr/cyclosm/",
        "https://c.tile-cyclosm.openstreetmap.fr/cyclosm/"
    ),
    "© OpenStreetMap, CyclOSM"
)

val OpenTopoSource = XYTileSource(
    "OpenTopoMap",
    0, 17, 256, ".png",
    arrayOf(
        "https://a.tile.opentopomap.org/",
        "https://b.tile.opentopomap.org/",
        "https://c.tile.opentopomap.org/"
    ),
    "© OpenTopoMap, CC-BY-SA"
)

val SatelliteSource = object : OnlineTileSourceBase(
    "Esri_World_Imagery",
    0, 19, 256, "",
    arrayOf(
        "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"
    ),
    "© Esri World Imagery"
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return baseUrl + zoom + "/" + y + "/" + x
    }
}

val CartoVoyagerSource = object : OnlineTileSourceBase(
    "Carto_Voyager",
    0, 20, 256, ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
    ),
    "© OpenStreetMap contributors, CARTO"
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return baseUrl + zoom + "/" + x + "/" + y + ".png"
    }
}

/**
 * Osmdroid-based OpenStreetMap & Satellite Hybrid Component for Fleet Infrastructure.
 * Completely free, no API keys required, featuring OpenStreetMap infrastructure and satellite tiles.
 */
@Composable
fun OpenStreetMapView(
    vehicles: List<Vehicle>,
    geofences: List<GeofenceZone>,
    selectedVehicle: Vehicle?,
    onSelectVehicle: (Vehicle) -> Unit,
    isDarkMode: Boolean = false,
    isTrafficEnabled: Boolean = false,
    onOpenCacheSheet: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val activeVehicle = selectedVehicle ?: vehicles.firstOrNull()
    val centerLat = activeVehicle?.latitude ?: 14.7167
    val centerLng = activeVehicle?.longitude ?: -17.4677

    var activeSourceType by remember { mutableStateOf("Mapnik") }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var cacheStats by remember { mutableStateOf(MapTileCacheManager.getCacheStats(context)) }
    var isSimulatingOffline by remember { mutableStateOf(MapTileCacheManager.isSimulateOffline()) }
    val isOnline = MapTileCacheManager.isNetworkAvailable(context)

    val tileSource = if (isTrafficEnabled) {
        CartoVoyagerSource
    } else {
        when (activeSourceType) {
            "Cycle" -> CyclOSMSource
            "Topo" -> OpenTopoSource
            "Satellite" -> SatelliteSource
            else -> TileSourceFactory.MAPNIK
        }
    }

    var showLayersMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().testTag("osmdroid_map_container")) {
        AndroidView(
            factory = { ctx ->
                MapTileCacheManager.configureOsmdroid(ctx)

                val mv = MapView(ctx).apply {
                    setTileSource(tileSource)
                    setMultiTouchControls(true)
                    setUseDataConnection(isOnline)
                    controller.setZoom(14.0)
                    controller.setCenter(GeoPoint(centerLat, centerLng))
                }
                mapViewRef = mv
                mv
            },
            update = { mapView ->
                mapViewRef = mapView
                mapView.setTileSource(tileSource)
                mapView.setUseDataConnection(MapTileCacheManager.isNetworkAvailable(context))
                mapView.controller.setCenter(GeoPoint(centerLat, centerLng))
                mapView.overlays.clear()

                // 1. Geofences
                geofences.forEach { zone ->
                    val polygon = Polygon().apply {
                        points = Polygon.pointsAsCircle(
                            GeoPoint(zone.centerLat, zone.centerLng),
                            zone.radiusMeters.toDouble()
                        )
                        val fillColorVal = if (zone.type.name == "RESTRICTED") {
                            android.graphics.Color.argb(45, 245, 158, 11)
                        } else {
                            android.graphics.Color.argb(45, 239, 68, 68)
                        }
                        val strokeColorVal = if (zone.type.name == "RESTRICTED") {
                            android.graphics.Color.argb(220, 245, 158, 11)
                        } else {
                            android.graphics.Color.argb(220, 239, 68, 68)
                        }
                        fillColor = fillColorVal
                        strokeColor = strokeColorVal
                        strokeWidth = 4f
                        title = "${zone.name} (${zone.radiusMeters.toInt()}m)"
                    }
                    mapView.overlays.add(polygon)
                }

                // 2. Trajectory polyline for active vehicle (history path)
                if (activeVehicle != null) {
                    val trajectoryPoints = listOf(
                        GeoPoint(activeVehicle.latitude - 0.008, activeVehicle.longitude - 0.012),
                        GeoPoint(activeVehicle.latitude - 0.004, activeVehicle.longitude - 0.006),
                        GeoPoint(activeVehicle.latitude, activeVehicle.longitude)
                    )
                    val polyline = org.osmdroid.views.overlay.Polyline().apply {
                        setPoints(trajectoryPoints)
                        outlinePaint.color = android.graphics.Color.parseColor("#00E5FF")
                        outlinePaint.strokeWidth = 8f
                        outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                        outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                        title = "Trajet de ${activeVehicle.name}"
                    }
                    mapView.overlays.add(polyline)

                    // Departure Marker
                    val startMarker = Marker(mapView).apply {
                        position = GeoPoint(activeVehicle.latitude - 0.008, activeVehicle.longitude - 0.012)
                        title = "Point de Départ"
                        snippet = "Trajet Récent Flotte"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(startMarker)
                }

                // 3. Vehicles
                vehicles.forEach { vehicle ->
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(vehicle.latitude, vehicle.longitude)
                        title = "🚗 ${vehicle.name} (${vehicle.licensePlate})"
                        snippet = "Vitesse: ${vehicle.speedKmH.toInt()} km/h | Carburant: ${vehicle.fuelLevelPct}% | Chauffeur: ${vehicle.driverName}"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { m, _ ->
                            onSelectVehicle(vehicle)
                            m.showInfoWindow()
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }

                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top-Right Live Cache & Connectivity Status Pill
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .shadow(6.dp, RoundedCornerShape(12.dp))
                .clickable {
                    cacheStats = MapTileCacheManager.getCacheStats(context)
                    onOpenCacheSheet?.invoke()
                }
                .testTag("map_cache_status_pill"),
            color = Color(0xF20F172A),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) Emerald500 else TharaRed)
                )
                Spacer(modifier = Modifier.width(7.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isOnline) "Osmdroid En Ligne" else "Osmdroid Hors-Ligne",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Cache",
                            tint = Cyan400,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = "Cache Tuiles : ${cacheStats.formattedSize} (${cacheStats.tileCount})",
                        fontSize = 9.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Bottom-Right Floating Controls (Layer Selector, Offline Manager, Zoom)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showLayersMenu) {
                    Surface(
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
                        color = Color(0xF20F172A),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "FONDS DE CARTE (OSMDROID)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )

                            val options = listOf(
                                Triple("Mapnik", "Standard OSM", "Routes & Villes"),
                                Triple("Satellite", "Satellite Hybride", "Imagerie Aérienne"),
                                Triple("Cycle", "CyclOSM", "Infrastructures"),
                                Triple("Topo", "OpenTopo", "Relief & Altitude")
                            )

                            options.forEach { (type, title, desc) ->
                                val isSelected = activeSourceType == type
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF0284C7) else Color.Transparent)
                                        .clickable {
                                            activeSourceType = type
                                            showLayersMenu = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .width(190.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = desc,
                                            color = if (isSelected) Color(0xFFE0F2FE) else Color(0xFF94A3B8),
                                            fontSize = 9.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Cache Sheet Trigger Floating Button
                if (onOpenCacheSheet != null) {
                    Surface(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(6.dp, CircleShape)
                            .clickable { onOpenCacheSheet() }
                            .testTag("open_tile_cache_sheet_fab"),
                        color = Color(0xFF0F172A),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Cyan400)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Gestionnaire de Cache Hors-Ligne",
                                tint = Cyan400,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Unified Zoom Controls
                Surface(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
                    color = Color(0xF20F172A),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(44.dp)
                    ) {
                        IconButton(
                            onClick = { mapViewRef?.controller?.zoomIn() },
                            modifier = Modifier.size(44.dp).testTag("zoom_in_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Zoom In",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF334155))
                        )

                        IconButton(
                            onClick = { mapViewRef?.controller?.zoomOut() },
                            modifier = Modifier.size(44.dp).testTag("zoom_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Zoom Out",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Layers Selector FAB
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(8.dp, CircleShape)
                        .clickable { showLayersMenu = !showLayersMenu }
                        .testTag("layers_selector_fab"),
                    color = Color(0xFF0284C7),
                    shape = CircleShape
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Toggle Layers Overlay",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-Definition Satellite Hybrid Interactive Map (Leaflet Google & Esri Satellite Hybride)
 * Guarantees REAL satellite photo imagery without API key limits, fully responsive in WebView.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SatelliteHybridWebViewMap(
    vehicles: List<Vehicle>,
    geofences: List<GeofenceZone>,
    selectedVehicle: Vehicle?,
    onSelectVehicle: (Vehicle) -> Unit,
    showOnlyUserVehicle: Boolean = false,
    isPhoneTrackingAuthorized: Boolean = false,
    phoneLat: Double = 3.863000,
    phoneLng: Double = 11.538000,
    phoneImei: String = "358941098421033",
    isDarkMode: Boolean = false,
    isSatelliteMode: Boolean = true,
    isTrafficMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val activeVehicle = selectedVehicle ?: vehicles.firstOrNull() ?: Vehicle(
        id = "default-1",
        name = "Véhicule Principal",
        licensePlate = "DK-1002-AA",
        imei = "358941098421033",
        driverName = "Mamadou Diallo",
        driverPhone = "+221 77 123 45 67",
        status = com.example.model.VehicleStatus.MOVING,
        speedKmH = 45f,
        batteryPct = 92,
        fuelLevelPct = 78,
        latitude = 3.863940,
        longitude = 11.540120,
        heading = 90f,
        enterpriseId = "ENT-01",
        enterpriseName = "Flotte Thara",
        lastUpdateTimestamp = System.currentTimeMillis(),
        address = "Dakar, Sénégal",
        ignitionOn = true
    )
    val centerLat = activeVehicle.latitude
    val centerLng = activeVehicle.longitude

    val displayVehicles = if (showOnlyUserVehicle) {
        listOf(activeVehicle)
    } else {
        vehicles.ifEmpty { listOf(activeVehicle) }
    }

    fun getRechartsPopupHtml(v: Vehicle): String {
        val fuelJson = v.fuelHistory.joinToString(prefix = "[", postfix = "]") { log ->
            "{\"day\": \"${log.day}\", \"l100\": ${log.litersPer100Km}}"
        }
        val safeId = v.id.replace("-", "_")
        return """
            <div style='width: 240px; padding: 4px; color: #fff; font-family: system-ui;'>
                <div style='font-weight: bold; font-size: 13px; color: #38bdf8; margin-bottom: 2px;'>🚗 ${v.name}</div>
                <div style='font-size: 10px; color: #94a3b8; margin-bottom: 6px;'>Plaque: ${v.licensePlate} | ${v.speedKmH.toInt()} km/h | ⛽ ${v.fuelLevelPct}%</div>
                <div style='font-size: 10px; font-weight: 600; color: #38bdf8; margin-bottom: 2px;'>📈 Recharts Carburant (L/100km):</div>
                <div id='recharts-fuel-$safeId' style='width: 100%; height: 120px; background: rgba(15, 23, 42, 0.9); border-radius: 6px;'></div>
            </div>
            <script type='text/babel'>
                try {
                    const fuelData_$safeId = $fuelJson;
                    function FuelChart_$safeId() {
                        return (
                            <Recharts.ResponsiveContainer width='100%' height='100%'>
                                <Recharts.LineChart data={fuelData_$safeId} margin={{ top: 5, right: 5, bottom: 5, left: -25 }}>
                                    <Recharts.XAxis dataKey='day' stroke='#94a3b8' fontSize={8} tick={{fill: '#94a3b8'}} />
                                    <Recharts.YAxis stroke='#94a3b8' fontSize={8} domain={[8, 16]} tick={{fill: '#94a3b8'}} />
                                    <Recharts.Tooltip contentStyle={{backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: 6, color: '#fff', fontSize: '9px'}} />
                                    <Recharts.Line type='monotone' dataKey='l100' stroke='#38bdf8' strokeWidth={2} dot={{r: 2, fill: '#38bdf8'}} />
                                </Recharts.LineChart>
                            </Recharts.ResponsiveContainer>
                        );
                    }
                    setTimeout(() => {
                        ReactDOM.render(<FuelChart_$safeId />, document.getElementById('recharts-fuel-$safeId'));
                    }, 150);
                } catch(e) { console.error('Recharts error:', e); }
            </script>
        """.trimIndent().replace("\n", " ").replace("'", "\\'")
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var cacheStats by remember { mutableStateOf(MapTileCacheManager.getCacheStats(context)) }
    var isPrecaching by remember { mutableStateOf(false) }
    var precacheMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?
                        ): android.webkit.WebResourceResponse? {
                            val c = view?.context ?: ctx
                            return MapTileCacheManager.handleWebResourceRequest(c, request)
                                ?: super.shouldInterceptRequest(view, request)
                        }

                        override fun onRenderProcessGone(
                            view: WebView?,
                            detail: android.webkit.RenderProcessGoneDetail?
                        ): Boolean {
                            try {
                                view?.let {
                                    val parent = it.parent as? android.view.ViewGroup
                                    parent?.removeView(it)
                                    it.destroy()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            return true
                        }
                    }
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowContentAccess = true
                        allowFileAccess = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = "Mozilla/5.0 (Linux; Android 11; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Mobile Safari/537.36"
                        
                        val isOnline = MapTileCacheManager.isNetworkAvailable(ctx)
                        cacheMode = if (isOnline) android.webkit.WebSettings.LOAD_DEFAULT else android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
                    }
                    
                    val htmlContent = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="utf-8" />
                            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.css" />
                            <script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.js"></script>
                            <script src="https://cdnjs.cloudflare.com/ajax/libs/react/18.2.0/umd/react.production.min.js"></script>
                            <script src="https://cdnjs.cloudflare.com/ajax/libs/react-dom/18.2.0/umd/react-dom.production.min.js"></script>
                            <script src="https://cdnjs.cloudflare.com/ajax/libs/recharts/2.12.0/Recharts.min.js"></script>
                            <script src="https://cdnjs.cloudflare.com/ajax/libs/babel-standalone/7.23.5/babel.min.js"></script>
                            <style>
                                * { box-sizing: border-box; }
                                html, body, #map {
                                    height: 100%;
                                    width: 100%;
                                    margin: 0;
                                    padding: 0;
                                    background: transparent;
                                }
                                .leaflet-container { background: transparent !important; font-family: system-ui, -apple-system, sans-serif; }
                                .custom-popup .leaflet-popup-content-wrapper {
                                    background: rgba(15, 23, 42, 0.95);
                                    color: #fff;
                                    border-radius: 10px;
                                    padding: 6px 10px;
                                    box-shadow: 0 10px 25px rgba(0,0,0,0.3);
                                    border: 1px solid rgba(255,255,255,0.15);
                                }
                                .custom-popup .leaflet-popup-tip { background: rgba(15, 23, 42, 0.95); }
                                .speed-badge {
                                    background: #ffffff;
                                    color: #0f172a;
                                    padding: 4px 8px;
                                    border-radius: 6px;
                                    font-weight: 700;
                                    font-size: 12px;
                                    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                                    border: 1px solid #e2e8f0;
                                }
                                .label-depart {
                                    background: #f97316;
                                    color: white;
                                    padding: 3px 6px;
                                    border-radius: 6px;
                                    font-size: 10px;
                                    font-weight: 700;
                                }
                                .phone-badge {
                                    background: #8b5cf6;
                                    color: white;
                                    padding: 4px 8px;
                                    border-radius: 8px;
                                    font-size: 11px;
                                    font-weight: 700;
                                    box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
                                }
                            </style>
                        </head>
                        <body>
                            <div id="map"></div>
                            <script>
                                function initMap() {
                                    if (typeof L === 'undefined') {
                                        setTimeout(initMap, 150);
                                        return;
                                    }

                                    var map = L.map('map', { zoomControl: false, maxZoom: 20 }).setView([$centerLat, $centerLng], 15);
                                    
                                    // 1. Esri World Imagery (Free Satellite HD)
                                    var esriSat = L.tileLayer('https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
                                        maxZoom: 19,
                                        attribution: '© Esri World Imagery'
                                    });

                                    // 2. CartoDB Voyager (Free OSM Standard Vector Style)
                                    var cartoVoyager = L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                                        maxZoom: 20,
                                        subdomains: ['a', 'b', 'c', 'd'],
                                        attribution: '© OpenStreetMap contributors, CARTO'
                                    });

                                    // 3. OpenStreetMap Standard
                                    var osmStandard = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                                        maxZoom: 19,
                                        attribution: '© OpenStreetMap contributors'
                                    });

                                    // 4. CartoDB Dark (Night Mode)
                                    var cartoDark = L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
                                        maxZoom: 20,
                                        subdomains: ['a', 'b', 'c', 'd'],
                                        attribution: '© CARTO Dark'
                                    });

                                    // 5. OpenTopoMap (Relief / Terrain)
                                    var openTopo = L.tileLayer('https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
                                        maxZoom: 17,
                                        attribution: '© OpenTopoMap, CC-BY-SA'
                                    });

                                    // Set Active Layer based on Mode
                                    var defaultLayer = ${if (isSatelliteMode) "esriSat" else "cartoVoyager"};
                                    defaultLayer.addTo(map);

                                    // Tile error automatic fallback
                                    defaultLayer.on('tileerror', function() {
                                        if (!map.hasLayer(cartoVoyager) && !map.hasLayer(osmStandard)) {
                                            cartoVoyager.addTo(map);
                                        }
                                    });

                                    // Layer switcher control in top right
                                    var baseMaps = {
                                        "🛰️ Esri Satellite": esriSat,
                                        "🗺️ Carto Voyager": cartoVoyager,
                                        "🌍 OpenStreetMap": osmStandard,
                                        "🌙 Carto Dark": cartoDark,
                                        "⛰️ OpenTopo Relief": openTopo
                                    };
                                    L.control.layers(baseMaps, null, { position: 'topright' }).addTo(map);

                                    setTimeout(function() {
                                        map.invalidateSize();
                                    }, 200);

                                    // Add Geofence Zones
                                    ${geofences.joinToString("\n") { zone ->
                                        """
                                        L.circle([${zone.centerLat}, ${zone.centerLng}], {
                                            color: '${if (zone.type.name == "RESTRICTED") "#f59e0b" else "#ef4444"}',
                                            fillColor: '${if (zone.type.name == "RESTRICTED") "#f59e0b" else "#ef4444"}',
                                            fillOpacity: 0.25,
                                            radius: ${zone.radiusMeters}
                                        }).addTo(map).bindTooltip("${zone.name}", {permanent: true, direction: 'center'});
                                        """
                                    }}

                                    // Add Trajectory Polyline for Vehicle
                                    var latlngs = [
                                        [${centerLat - 0.008}, ${centerLng - 0.012}],
                                        [${centerLat - 0.004}, ${centerLng - 0.006}],
                                        [$centerLat, $centerLng]
                                    ];
                                    L.polyline(latlngs, {
                                        color: '#00e5ff',
                                        weight: 4,
                                        opacity: 0.9,
                                        lineJoin: 'round'
                                    }).addTo(map);

                                    // Start Marker
                                    L.marker([${centerLat - 0.008}, ${centerLng - 0.012}]).addTo(map)
                                        .bindPopup("<div class='label-depart'>Départ Trajet</div>");

                                    // Vehicle End Marker
                                    L.marker([$centerLat, $centerLng]).addTo(map)
                                        .bindPopup("<div class='speed-badge'><b>🚗 ${activeVehicle?.name ?: "Mon Véhicule"}</b><br/>${activeVehicle?.speedKmH?.toInt() ?: 23} km/h</div>")
                                        .openPopup();

                                    // Render Display Vehicles
                                    ${displayVehicles.joinToString("\n") { v ->
                                        """
                                        L.marker([${v.latitude}, ${v.longitude}]).addTo(map)
                                            .bindTooltip("🚗 ${v.name} (${v.speedKmH.toInt()} km/h)", {permanent: false});
                                        """
                                    }}

                                    // Smartphone Location & Polyline (If Background Tracking Authorized)
                                    ${if (isPhoneTrackingAuthorized) """
                                        var phoneLatLng = [$phoneLat, $phoneLng];
                                        
                                        // Smartphone Pulse Range
                                        L.circle(phoneLatLng, {
                                            color: '#a855f7',
                                            fillColor: '#c084fc',
                                            fillOpacity: 0.35,
                                            radius: 120
                                        }).addTo(map);

                                        // Smartphone Marker
                                        L.marker(phoneLatLng).addTo(map)
                                            .bindPopup("<div class='phone-badge'>📱 Smartphone Propriétaire<br/><small>IMEI: $phoneImei</small></div>")
                                            .openPopup();

                                        // Dashed Proximity Line (Phone -> Vehicle)
                                        L.polyline([phoneLatLng, [$centerLat, $centerLng]], {
                                            color: '#c084fc',
                                            weight: 3,
                                            dashArray: '6, 8',
                                            opacity: 0.95
                                        }).addTo(map).bindTooltip("Distance Téléphone-Véhicule", {permanent: false});
                                    """ else ""}
                                }

                                if (document.readyState === 'loading') {
                                    document.addEventListener('DOMContentLoaded', initMap);
                                } else {
                                    initMap();
                                }
                            </script>
                        </body>
                        </html>
                    """.trimIndent()

                    loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                // Refresh map
            },
            modifier = Modifier.fillMaxSize()
        )

        // Clean map surface without colliding top overlays
    }
}
