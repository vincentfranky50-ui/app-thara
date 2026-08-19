package com.example.ui.components.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PoiCategory
import com.example.model.PoiItem
import com.example.model.RouteResult
import com.example.model.TravelMode
import com.example.model.UserLocation

/**
 * Barre de recherche moderne flottante avec suggestions en temps réel
 */
@Composable
fun LocationMapSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    searchResults: List<PoiItem>,
    isSearching: Boolean,
    onResultSelected: (PoiItem) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                placeholder = {
                    Text(
                        "Rechercher une adresse, hôpital, station...",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFF10B981)
                        )
                    } else if (query.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Effacer",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("map_search_input")
            )
        }

        // Résultats de recherche
        if (searchResults.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                ) {
                    items(searchResults) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onResultSelected(item) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(item.category.badgeBgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.category.icon,
                                    contentDescription = null,
                                    tint = item.category.primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.address,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (item.distanceMeters > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.formattedDistance,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                        Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    }
                }
            }
        }
    }
}

/**
 * Barre horizontale de filtres par catégories de Points d'Intérêt
 */
@Composable
fun PoiCategoryFilterBar(
    selectedCategory: PoiCategory?,
    onCategorySelected: (PoiCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            // Chip "Tous"
            val isAllSelected = selectedCategory == null
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isAllSelected) Color(0xFF0F172A) else Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onCategorySelected(null) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = if (isAllSelected) Color.White else Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tous",
                        fontSize = 13.sp,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isAllSelected) Color.White else Color(0xFF1E293B)
                    )
                }
            }
        }

        items(PoiCategory.values()) { category ->
            val isSelected = selectedCategory == category
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) category.primaryColor else Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onCategorySelected(category) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else category.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = category.titleFr,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF1E293B)
                    )
                }
            }
        }
    }
}

/**
 * Panneau de boutons d'actions flottantes sur la carte
 */
@Composable
fun MapFloatingControlPanel(
    isFollowMode: Boolean,
    onToggleFollow: () -> Unit,
    onRecenter: () -> Unit,
    compassHeading: Float,
    onResetNorth: () -> Unit,
    onOpenLayersMenu: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Bouton Boussole / Réorienter vers le Nord
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .clickable { onResetNorth() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Nord",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(-compassHeading)
                )
            }
        }

        // Bouton Sélecteur de Couches cartographiques
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .clickable { onOpenLayersMenu() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Couches",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Bouton Mode Suivi automatique
        Surface(
            shape = CircleShape,
            color = if (isFollowMode) Color(0xFF10B981) else Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .clickable { onToggleFollow() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = "Mode Suivi",
                    tint = if (isFollowMode) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Bouton Ma Position (Recentrer)
        FloatingActionButton(
            onClick = onRecenter,
            containerColor = Color.White,
            contentColor = Color(0xFF0284C7),
            elevation = FloatingActionButtonDefaults.elevation(4.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (isFollowMode) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                contentDescription = "Ma position",
                modifier = Modifier.size(24.dp)
            )
        }

        // Contrôles Zoom In / Out
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onZoomIn, modifier = Modifier.size(42.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom avant", tint = Color(0xFF0F172A))
                }
                Divider(modifier = Modifier.width(28.dp), color = Color(0xFFE2E8F0))
                IconButton(onClick = onZoomOut, modifier = Modifier.size(42.dp)) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom arrière", tint = Color(0xFF0F172A))
                }
            }
        }
    }
}

/**
 * Fiche détaillée du lieu ou POI sélectionné
 */
@Composable
fun PoiDetailBottomSheet(
    poi: PoiItem,
    onCalculateRoute: (PoiItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Category Icon + Title + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(poi.category.badgeBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = poi.category.icon,
                            contentDescription = null,
                            tint = poi.category.primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = poi.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = poi.category.titleFr,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = poi.category.primaryColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "•", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = poi.formattedDistance,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Address & Info
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = poi.address,
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )
            }

            if (poi.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = poi.description,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Itinéraire + Appel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onCalculateRoute(poi) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Itinéraire", fontWeight = FontWeight.Bold, color = Color.White)
                }

                if (poi.phone != null) {
                    OutlinedButton(
                        onClick = { /* Appel téléphonique */ },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Appeler",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Carte supérieure de navigation active avec distance, durée et étapes
 */
@Composable
fun RouteNavigationBanner(
    route: RouteResult,
    travelMode: TravelMode,
    onChangeMode: (TravelMode) -> Unit,
    onCloseRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(10.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Mode selector: Voiture | À pied | Vélo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TravelMode.values().forEach { mode ->
                        val isSelected = travelMode == mode
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color(0xFF10B981) else Color(0xFF1E293B),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onChangeMode(mode) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = mode.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = mode.titleFr,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onCloseRoute,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Quitter",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time & Distance metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = route.formattedDuration,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "${route.formattedDistance} • Vers ${route.destinationName}",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "En route",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bannière d'explication et de demande de permission de localisation
 */
@Composable
fun GpsPermissionBanner(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFEF3C7)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Localisation désactivée",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF92400E)
                )
                Text(
                    text = "Activez le GPS pour afficher votre position et suivre vos déplacements en direct.",
                    fontSize = 12.sp,
                    color = Color(0xFFB45309)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Activer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
