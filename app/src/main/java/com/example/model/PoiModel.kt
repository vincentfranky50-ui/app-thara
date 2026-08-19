package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Catégories de Points d'Intérêt (POI)
 */
enum class PoiCategory(
    val titleFr: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val badgeBgColor: Color
) {
    PHARMACY(
        titleFr = "Pharmacies",
        icon = Icons.Default.LocalPharmacy,
        primaryColor = Color(0xFF10B981),
        badgeBgColor = Color(0xFFD1FAE5)
    ),
    HOSPITAL(
        titleFr = "Hôpitaux",
        icon = Icons.Default.LocalHospital,
        primaryColor = Color(0xFFEF4444),
        badgeBgColor = Color(0xFFFEE2E2)
    ),
    SCHOOL(
        titleFr = "Écoles",
        icon = Icons.Default.School,
        primaryColor = Color(0xFF8B5CF6),
        badgeBgColor = Color(0xFFEDE9FE)
    ),
    RESTAURANT(
        titleFr = "Restaurants",
        icon = Icons.Default.Restaurant,
        primaryColor = Color(0xFFF59E0B),
        badgeBgColor = Color(0xFFFEF3C7)
    ),
    BANK(
        titleFr = "Banques",
        icon = Icons.Default.AccountBalance,
        primaryColor = Color(0xFF0284C7),
        badgeBgColor = Color(0xFFE0F2FE)
    ),
    GAS_STATION(
        titleFr = "Stations-service",
        icon = Icons.Default.LocalGasStation,
        primaryColor = Color(0xFFEA580C),
        badgeBgColor = Color(0xFFFFEDD5)
    ),
    HOTEL(
        titleFr = "Hôtels",
        icon = Icons.Default.Hotel,
        primaryColor = Color(0xFF6366F1),
        badgeBgColor = Color(0xFFE0E7FF)
    ),
    SHOP(
        titleFr = "Commerces",
        icon = Icons.Default.ShoppingCart,
        primaryColor = Color(0xFFEC4899),
        badgeBgColor = Color(0xFFFCE7F3)
    ),
    ADMINISTRATION(
        titleFr = "Administrations",
        icon = Icons.Default.LocationCity,
        primaryColor = Color(0xFF475569),
        badgeBgColor = Color(0xFFF1F5F9)
    ),
    TRANSPORT(
        titleFr = "Transports",
        icon = Icons.Default.DirectionsBus,
        primaryColor = Color(0xFF0D9488),
        badgeBgColor = Color(0xFFCCFBF1)
    ),
    OTHER(
        titleFr = "Autres",
        icon = Icons.Default.Place,
        primaryColor = Color(0xFF64748B),
        badgeBgColor = Color(0xFFF1F5F9)
    )
}

/**
 * Modèle de Point d'Intérêt
 */
data class PoiItem(
    val id: String,
    val name: String,
    val category: PoiCategory,
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val phone: String? = null,
    val rating: Float = 4.5f,
    val openingHours: String = "08:00 - 20:00",
    val description: String = "",
    val distanceMeters: Float = 0f
) {
    /**
     * Formate la distance de manière conviviale (ex: "350 m" ou "2.4 km")
     */
    val formattedDistance: String
        get() = when {
            distanceMeters <= 0f -> "À proximité"
            distanceMeters < 1000f -> "${distanceMeters.toInt()} m"
            else -> String.format(java.util.Locale.US, "%.1f km", distanceMeters / 1000f)
        }

    /**
     * Calcule la distance géodésique avec une position donnée
     */
    fun withComputedDistance(userLat: Double, userLon: Double): PoiItem {
        val distance = computeHaversineDistance(userLat, userLon, latitude, longitude)
        return this.copy(distanceMeters = distance)
    }

    companion object {
        fun computeHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
            val r = 6371000.0 // Rayon de la Terre en mètres
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return (r * c).toFloat()
        }
    }
}
