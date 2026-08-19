package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.PoiCategory
import com.example.model.PoiItem
import com.example.model.TravelMode
import com.example.model.UserLocation
import com.example.ui.components.CyclOSMSource
import com.example.ui.components.OpenTopoSource
import com.example.ui.components.SatelliteSource
import com.example.ui.components.map.GpsPermissionBanner
import com.example.ui.components.map.LocationMapSearchBar
import com.example.ui.components.map.MapFloatingControlPanel
import com.example.ui.components.map.PoiCategoryFilterBar
import com.example.ui.components.map.PoiDetailBottomSheet
import com.example.ui.components.map.RouteNavigationBanner
import com.example.ui.viewmodel.LocationMapViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

/**
 * Écran complet de Carte de localisation, suivi en temps réel et navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationMapScreen(
    modifier: Modifier = Modifier,
    viewModel: LocationMapViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLayersSheet by remember { mutableStateOf(false) }

    // Configuration Osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "TharaFleetTrackerAndroid/2.0"
    }

    // Gestionnaire de permissions de géolocalisation
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    // Demande initiale de permissions
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Cycle de vie de la localisation (Arrêter les requêtes quand l'écran est en arrière-plan)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.startLocationTracking()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Référence vers la MapView native
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("location_map_screen")
    ) {
        // 1. CARTE PLEIN ÉCRAN OSMDROID / MAPLIBRE TILES
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true
                    zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(16.5)

                    val initialCenter = GeoPoint(4.0511, 9.7679)
                    controller.setCenter(initialCenter)

                    // Détecter les mouvements manuels de l'utilisateur pour suspendre le mode suivi
                    addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            if (isAnimating == false) {
                                viewModel.onUserDraggedMap()
                            }
                            return true
                        }

                        override fun onZoom(event: ZoomEvent?): Boolean {
                            return true
                        }
                    })

                    mapViewRef = this
                }
            },
            update = { mapView ->
                // Mise à jour de la source de tuiles
                when (uiState.currentTileSource) {
                    "SATELLITE" -> mapView.setTileSource(SatelliteSource)
                    "OPEN_TOPO" -> mapView.setTileSource(OpenTopoSource)
                    "CYCL_OSM" -> mapView.setTileSource(CyclOSMSource)
                    else -> mapView.setTileSource(TileSourceFactory.MAPNIK)
                }

                // Animation de la caméra vers la position cible
                uiState.cameraTargetPosition?.let { (lat, lon) ->
                    val targetPoint = GeoPoint(lat, lon)
                    mapView.controller.animateTo(targetPoint)
                }

                // Nettoyage et reconstruction optimisée des overlays
                mapView.overlays.clear()

                val userLoc = uiState.userLocation

                // A. Cercle de précision GPS
                if (userLoc != null && userLoc.accuracyMeters > 0) {
                    val accuracyCircle = Polygon(mapView).apply {
                        points = Polygon.pointsAsCircle(
                            GeoPoint(userLoc.latitude, userLoc.longitude),
                            userLoc.accuracyMeters.toDouble()
                        )
                        fillPaint.color = android.graphics.Color.argb(35, 14, 165, 233)
                        outlinePaint.color = android.graphics.Color.argb(90, 2, 132, 199)
                        outlinePaint.strokeWidth = 2f
                    }
                    mapView.overlays.add(accuracyCircle)
                }

                // B. Tracé de l'itinéraire actif (Polyline)
                uiState.activeRoute?.let { route ->
                    val routePolyline = Polyline(mapView).apply {
                        val geoPoints = route.polylinePoints.map { GeoPoint(it.latitude, it.longitude) }
                        setPoints(geoPoints)
                        outlinePaint.color = when (route.mode) {
                            TravelMode.DRIVING -> android.graphics.Color.parseColor("#10B981")
                            TravelMode.WALKING -> android.graphics.Color.parseColor("#3B82F6")
                            TravelMode.BICYCLE -> android.graphics.Color.parseColor("#F59E0B")
                        }
                        outlinePaint.strokeWidth = 12f
                        outlinePaint.strokeCap = Paint.Cap.ROUND
                        outlinePaint.strokeJoin = Paint.Join.ROUND
                    }
                    mapView.overlays.add(routePolyline)
                }

                // C. Marqueurs des Points d'Intérêt (POIs)
                uiState.nearbyPois.forEach { poi ->
                    val poiMarker = Marker(mapView).apply {
                        position = GeoPoint(poi.latitude, poi.longitude)
                        title = poi.name
                        snippet = "${poi.category.titleFr} • ${poi.formattedDistance}"
                        icon = createPoiCategoryIcon(context, poi.category)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            viewModel.selectPoi(poi)
                            true
                        }
                    }
                    mapView.overlays.add(poiMarker)
                }

                // D. Marqueur de l'Utilisateur avec orientation dynamique
                if (userLoc != null) {
                    val userMarker = Marker(mapView).apply {
                        position = GeoPoint(userLoc.latitude, userLoc.longitude)
                        title = "Ma Position"
                        icon = createUserLocationIcon(
                            context = context,
                            bearing = if (userLoc.bearingDegrees > 0f) userLoc.bearingDegrees else uiState.compassHeading
                        )
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                    mapView.overlays.add(userMarker)
                }

                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. BANNIÈRE D'AVERTISSEMENT PERMISSION GPS SI REFUSÉE
        if (!uiState.isPermissionGranted) {
            GpsPermissionBanner(
                onRequestPermission = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }

        // 3. EN-TÊTE SUPÉRIEUR : BARRE DE RECHERCHE + FILTRES POI OU ITINÉRAIRE
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            AnimatedVisibility(
                visible = uiState.activeRoute == null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column {
                    LocationMapSearchBar(
                        query = uiState.searchQuery,
                        onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        searchResults = uiState.searchResults,
                        isSearching = uiState.isSearching,
                        onResultSelected = { viewModel.selectSearchResult(it) },
                        onClearSearch = { viewModel.onSearchQueryChanged("") }
                    )

                    PoiCategoryFilterBar(
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = { viewModel.selectPoiCategory(it) }
                    )
                }
            }

            // Bandeau supérieur d'itinéraire actif
            uiState.activeRoute?.let { route ->
                RouteNavigationBanner(
                    route = route,
                    travelMode = uiState.travelMode,
                    onChangeMode = { viewModel.changeTravelMode(it) },
                    onCloseRoute = { viewModel.clearRoute() }
                )
            }
        }

        // 4. PANNEAU LATÉRAL DE CONTRÔLES FLOTTANTS (DROITE)
        MapFloatingControlPanel(
            isFollowMode = uiState.isFollowMode,
            onToggleFollow = { viewModel.toggleFollowMode() },
            onRecenter = { viewModel.recenterOnUser() },
            compassHeading = uiState.compassHeading,
            onResetNorth = {
                mapViewRef?.mapOrientation = 0f
                mapViewRef?.invalidate()
            },
            onOpenLayersMenu = { showLayersSheet = true },
            onZoomIn = {
                mapViewRef?.controller?.zoomIn()
            },
            onZoomOut = {
                mapViewRef?.controller?.zoomOut()
            },
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        // 5. FICHE INFÉRIEURE : DÉTAILS DU POI SÉLECTIONNÉ
        uiState.selectedPoi?.let { poi ->
            AnimatedVisibility(
                visible = uiState.activeRoute == null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                PoiDetailBottomSheet(
                    poi = poi,
                    onCalculateRoute = { targetPoi ->
                        viewModel.calculateRouteTo(targetPoi)
                    },
                    onDismiss = { viewModel.dismissPoiSheet() }
                )
            }
        }

        // 6. MODAL SÉLECTEUR DE COUCHES CARTOGRAPHIQUES
        if (showLayersSheet) {
            val bottomSheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showLayersSheet = false },
                sheetState = bottomSheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Types de Carte & Couches",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    val layers = listOf(
                        Triple("OSM_STANDARD", "Standard OpenStreetMap", "Carte vectorielle urbaine détaillée et claire"),
                        Triple("SATELLITE", "Satellite Hybride ESRI", "Imagerie aérienne haute résolution avec routes"),
                        Triple("CYCL_OSM", "CyclOSM Pistes & Randonnée", "Itinéraires cyclables, pistes et sentiers"),
                        Triple("OPEN_TOPO", "OpenTopoMap Relief", "Courbes de niveau et topographie naturelle")
                    )

                    layers.forEach { (id, title, desc) ->
                        val isSelected = uiState.currentTileSource == id
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.setTileSource(id)
                                    showLayersSheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF10B981) else Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Sélectionné",
                                        tint = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

/**
 * Génère une icône vectorielle nette pour le marqueur utilisateur avec orientation
 */
private fun createUserLocationIcon(context: Context, bearing: Float): Drawable {
    val size = 96
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val cx = size / 2f
    val cy = size / 2f

    // 1. Cercle externe halo pulsant
    val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(70, 14, 165, 233)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, 38f, haloPaint)

    // 2. Bordure blanche
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, 22f, borderPaint)

    // 3. Point central bleu cyan
    val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0284C7")
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, 16f, centerPaint)

    // 4. Cône / Flèche de direction
    canvas.save()
    canvas.rotate(bearing, cx, cy)
    val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0284C7")
        style = Paint.Style.FILL
    }
    val path = Path().apply {
        moveTo(cx, cy - 36f)
        lineTo(cx - 10f, cy - 20f)
        lineTo(cx + 10f, cy - 20f)
        close()
    }
    canvas.drawPath(path, arrowPaint)
    canvas.restore()

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Génère une icône de marqueur personnalisée pour chaque catégorie de POI
 */
private fun createPoiCategoryIcon(context: Context, category: PoiCategory): Drawable {
    val size = 84
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val cx = size / 2f
    val cy = size * 0.42f
    val radius = 24f

    // Pin Shadow
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(60, 0, 0, 0)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, size - 10f, 10f, shadowPaint)

    // Pin Shape (Cercle + Pointe triangulaire)
    val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = category.primaryColor.toArgb()
        style = Paint.Style.FILL
    }

    val pinPath = Path().apply {
        moveTo(cx, size - 12f)
        lineTo(cx - 14f, cy + 12f)
        lineTo(cx + 14f, cy + 12f)
        close()
    }
    canvas.drawPath(pinPath, pinPaint)
    canvas.drawCircle(cx, cy, radius, pinPaint)

    // Cercle intérieur blanc
    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, radius * 0.75f, innerPaint)

    // Point central de couleur
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = category.primaryColor.toArgb()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(cx, cy, radius * 0.38f, dotPaint)

    return BitmapDrawable(context.resources, bitmap)
}
