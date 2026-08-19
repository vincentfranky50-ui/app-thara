package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.location.LocationTracker
import com.example.data.service.OsmGeocodingAndPoiService
import com.example.data.service.OsmRoutingService
import com.example.model.PoiCategory
import com.example.model.PoiItem
import com.example.model.RoutePoint
import com.example.model.RouteResult
import com.example.model.TravelMode
import com.example.model.UserLocation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * État d'interface du module de carte et de suivi de localisation
 */
data class LocationMapUiState(
    val userLocation: UserLocation? = null,
    val isGpsActive: Boolean = true,
    val isPermissionGranted: Boolean = false,
    val isFollowMode: Boolean = true,
    val compassHeading: Float = 0f,
    val selectedCategory: PoiCategory? = null,
    val nearbyPois: List<PoiItem> = emptyList(),
    val selectedPoi: PoiItem? = null,
    val searchQuery: String = "",
    val searchResults: List<PoiItem> = emptyList(),
    val isSearching: Boolean = false,
    val activeRoute: RouteResult? = null,
    val isCalculatingRoute: Boolean = false,
    val travelMode: TravelMode = TravelMode.DRIVING,
    val currentTileSource: String = "OSM_STANDARD", // OSM_STANDARD, SATELLITE, OPEN_TOPO, CYCL_OSM
    val isGpsDialogShown: Boolean = false,
    val cameraTargetPosition: Pair<Double, Double>? = null
)

class LocationMapViewModel(application: Application) : AndroidViewModel(application) {

    private val locationTracker = LocationTracker(application.applicationContext)
    private val poiService = OsmGeocodingAndPoiService()
    private val routingService = OsmRoutingService()

    private val _uiState = MutableStateFlow(LocationMapUiState())
    val uiState: StateFlow<LocationMapUiState> = _uiState.asStateFlow()

    private var locationUpdatesJob: Job? = null
    private var compassUpdatesJob: Job? = null
    private var searchDebounceJob: Job? = null

    init {
        // Démarrer l'écoute de la boussole dès le début
        startCompassTracking()
    }

    /**
     * Appelé lorsque les permissions de localisation sont accordées
     */
    fun onPermissionGranted() {
        _uiState.update { it.copy(isPermissionGranted = true, isGpsActive = locationTracker.isGpsEnabled()) }
        startLocationTracking()
    }

    /**
     * Appelé en cas de refus de permission
     */
    fun onPermissionDenied() {
        _uiState.update { it.copy(isPermissionGranted = false) }
        // Fournir une position initiale de démonstration (ex: Douala / Yaoundé / Paris)
        if (_uiState.value.userLocation == null) {
            val fallback = UserLocation(
                latitude = 4.0511,
                longitude = 9.7679,
                accuracyMeters = 15f,
                bearingDegrees = 45f,
                isGpsActive = false
            )
            _uiState.update { it.copy(userLocation = fallback) }
            refreshNearbyPois(fallback.latitude, fallback.longitude, _uiState.value.selectedCategory)
        }
    }

    /**
     * Démarre la réception en continu des coordonnées GPS
     */
    fun startLocationTracking() {
        locationUpdatesJob?.cancel()
        locationUpdatesJob = viewModelScope.launch {
            locationTracker.getLocationUpdates(intervalMs = 1500L)
                .catch { e ->
                    _uiState.update { it.copy(isGpsActive = false) }
                }
                .collect { location ->
                    _uiState.update { current ->
                        val shouldFollow = current.isFollowMode
                        current.copy(
                            userLocation = location,
                            isGpsActive = location.isGpsActive,
                            cameraTargetPosition = if (shouldFollow) Pair(location.latitude, location.longitude) else current.cameraTargetPosition
                        )
                    }

                    // Rafraîchir les POIs à proximité si la liste est vide
                    if (_uiState.value.nearbyPois.isEmpty()) {
                        refreshNearbyPois(location.latitude, location.longitude, _uiState.value.selectedCategory)
                    }
                }
        }
    }

    /**
     * Démarre l'écouteur de la boussole matérielle
     */
    private fun startCompassTracking() {
        compassUpdatesJob?.cancel()
        compassUpdatesJob = viewModelScope.launch {
            locationTracker.getCompassBearingUpdates().collect { bearing ->
                _uiState.update { it.copy(compassHeading = bearing) }
            }
        }
    }

    /**
     * Active ou désactive le mode Suivi automatique de caméra
     */
    fun toggleFollowMode(enable: Boolean? = null) {
        val newMode = enable ?: !_uiState.value.isFollowMode
        _uiState.update { current ->
            val target = if (newMode && current.userLocation != null) {
                Pair(current.userLocation.latitude, current.userLocation.longitude)
            } else current.cameraTargetPosition
            current.copy(isFollowMode = newMode, cameraTargetPosition = target)
        }
    }

    /**
     * Appelé lorsque l'utilisateur touche ou glisse manuellement sur la carte
     */
    fun onUserDraggedMap() {
        if (_uiState.value.isFollowMode) {
            _uiState.update { it.copy(isFollowMode = false) }
        }
    }

    /**
     * Recentrer la caméra sur la position actuelle de l'utilisateur
     */
    fun recenterOnUser() {
        val loc = _uiState.value.userLocation
        if (loc != null) {
            _uiState.update {
                it.copy(
                    isFollowMode = true,
                    cameraTargetPosition = Pair(loc.latitude, loc.longitude)
                )
            }
        } else {
            // Relancer la localisation si non disponible
            startLocationTracking()
        }
    }

    /**
     * Filtre les points d'intérêt par catégorie
     */
    fun selectPoiCategory(category: PoiCategory?) {
        val newCategory = if (_uiState.value.selectedCategory == category) null else category
        _uiState.update { it.copy(selectedCategory = newCategory) }

        val loc = _uiState.value.userLocation ?: UserLocation(4.0511, 9.7679)
        refreshNearbyPois(loc.latitude, loc.longitude, newCategory)
    }

    /**
     * Sélectionne un point d'intérêt pour afficher ses détails
     */
    fun selectPoi(poi: PoiItem?) {
        _uiState.update { current ->
            current.copy(
                selectedPoi = poi,
                isFollowMode = false,
                cameraTargetPosition = poi?.let { Pair(it.latitude, it.longitude) } ?: current.cameraTargetPosition
            )
        }
    }

    /**
     * Recherche d'adresses ou de lieux avec debounce
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchDebounceJob?.cancel()

        if (query.trim().length < 2) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchDebounceJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            delay(350L) // Debounce utilisateur

            val userLoc = _uiState.value.userLocation
            val results = poiService.searchPlaces(query, userLoc?.latitude, userLoc?.longitude)
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    /**
     * Sélectionne un résultat de recherche
     */
    fun selectSearchResult(result: PoiItem) {
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
                selectedPoi = result,
                isFollowMode = false,
                cameraTargetPosition = Pair(result.latitude, result.longitude)
            )
        }
    }

    /**
     * Calcule l'itinéraire vers le point d'intérêt sélectionné
     */
    fun calculateRouteTo(destination: PoiItem, mode: TravelMode? = null) {
        val userLoc = _uiState.value.userLocation ?: UserLocation(4.0511, 9.7679)
        val selectedMode = mode ?: _uiState.value.travelMode

        viewModelScope.launch {
            _uiState.update { it.copy(isCalculatingRoute = true, travelMode = selectedMode) }

            val origin = RoutePoint(latitude = userLoc.latitude, longitude = userLoc.longitude)
            val dest = RoutePoint(latitude = destination.latitude, longitude = destination.longitude)

            val route = routingService.calculateRoute(
                origin = origin,
                destination = dest,
                destinationName = destination.name,
                mode = selectedMode
            )

            _uiState.update {
                it.copy(
                    activeRoute = route,
                    isCalculatingRoute = false,
                    selectedPoi = destination
                )
            }
        }
    }

    /**
     * Modifie le mode de transport pour l'itinéraire en cours
     */
    fun changeTravelMode(mode: TravelMode) {
        _uiState.update { it.copy(travelMode = mode) }
        val currentPoi = _uiState.value.selectedPoi
        if (currentPoi != null) {
            calculateRouteTo(currentPoi, mode)
        }
    }

    /**
     * Annule l'itinéraire actif
     */
    fun clearRoute() {
        _uiState.update { it.copy(activeRoute = null) }
    }

    /**
     * Change le fond de carte (Standard, Satellite, CyclOSM, Topo)
     */
    fun setTileSource(source: String) {
        _uiState.update { it.copy(currentTileSource = source) }
    }

    /**
     * Ferme la fiche de détail du POI
     */
    fun dismissPoiSheet() {
        _uiState.update { it.copy(selectedPoi = null) }
    }

    private fun refreshNearbyPois(lat: Double, lon: Double, category: PoiCategory?) {
        viewModelScope.launch {
            val pois = poiService.getNearbyPois(lat, lon, category)
            _uiState.update { it.copy(nearbyPois = pois) }
        }
    }
}
