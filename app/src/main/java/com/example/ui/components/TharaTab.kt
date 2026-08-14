package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.ui.graphics.vector.ImageVector

enum class TharaTab(val route: String, val titleFr: String, val icon: ImageVector) {
    FLEET("fleet", "Fleet", Icons.Default.Dashboard),
    TRIPS("trips", "Trajets", Icons.Default.Route),
    ALERTS("alerts", "Alerts", Icons.Default.WarningAmber),
    MAP("map", "Map", Icons.Default.Map),
    REPORTS("reports", "Reports", Icons.Default.BarChart),
    PROFILE("profile", "Profile", Icons.Default.Person)
}
