package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GeofenceZone
import com.example.model.Vehicle
import com.example.model.VehicleStatus
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Red500
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GeofenceMapCanvas(
    vehicles: List<Vehicle> = emptyList(),
    geofences: List<GeofenceZone> = emptyList(),
    selectedVehicle: Vehicle? = null,
    onSelectVehicle: (Vehicle) -> Unit = {},
    isDarkTheme: Boolean = false,
    isSatelliteMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    // Map bounds anchor (centered on Dakar Region)
    val mapCenterLat = 14.7200
    val mapCenterLng = -17.3800
    val latSpan = 0.18
    val lngSpan = 0.45

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("geofence_map_canvas")
            .background(if (isDarkTheme) Color(0xFF070D1B) else Color(0xFFE2E8F0))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(vehicles) {
                    detectTapGestures { tapOffset ->
                        val canvasWidth = size.width.toFloat()
                        val canvasHeight = size.height.toFloat()

                        // Find closest vehicle to tap
                        val clicked = vehicles.minByOrNull { v ->
                            val px = ((v.longitude - (mapCenterLng - lngSpan / 2)) / lngSpan) * canvasWidth
                            val py = canvasHeight - (((v.latitude - (mapCenterLat - latSpan / 2)) / latSpan) * canvasHeight)
                            val dx = px - tapOffset.x
                            val dy = py - tapOffset.y
                            (dx * dx + dy * dy)
                        }

                        if (clicked != null) {
                            val px = ((clicked.longitude - (mapCenterLng - lngSpan / 2)) / lngSpan) * canvasWidth
                            val py = canvasHeight - (((clicked.latitude - (mapCenterLat - latSpan / 2)) / latSpan) * canvasHeight)
                            val dx = px - tapOffset.x
                            val dy = py - tapOffset.y
                            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                            if (dist < 80f) {
                                onSelectVehicle(clicked)
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw Map Base Grid Lines & Topography
            drawMapTopography(width, height, isDarkTheme, isSatelliteMode)

            // 2. Draw Geofence Zones
            geofences.forEach { zone ->
                val zx = ((zone.centerLng - (mapCenterLng - lngSpan / 2)) / lngSpan) * width
                val zy = height - (((zone.centerLat - (mapCenterLat - latSpan / 2)) / latSpan) * height)
                val radiusPx = (zone.radiusMeters / 12000f) * width

                val zoneColor = when (zone.type) {
                    com.example.model.ZoneType.SAFE -> Emerald400
                    com.example.model.ZoneType.RESTRICTED -> Amber500
                    com.example.model.ZoneType.NO_GO -> Red500
                }

                drawCircle(
                    color = zoneColor.copy(alpha = if (isDarkTheme) 0.12f else 0.18f),
                    radius = radiusPx,
                    center = Offset(zx.toFloat(), zy.toFloat())
                )
                drawCircle(
                    color = zoneColor.copy(alpha = 0.8f),
                    radius = radiusPx,
                    center = Offset(zx.toFloat(), zy.toFloat()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )

                // Label
                val labelLayout = textMeasurer.measure(
                    text = "🛡️ ${zone.name}",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = zoneColor
                    )
                )
                drawText(
                    textLayoutResult = labelLayout,
                    topLeft = Offset(zx.toFloat() - labelLayout.size.width / 2, zy.toFloat() - radiusPx - 18)
                )
            }

            // 3. Draw Route Traces for selected vehicle
            selectedVehicle?.let { sel ->
                val sx = ((sel.longitude - (mapCenterLng - lngSpan / 2)) / lngSpan) * width
                val sy = height - (((sel.latitude - (mapCenterLat - latSpan / 2)) / latSpan) * height)

                // Target Pulse Halo
                drawCircle(
                    color = Cyan400.copy(alpha = 0.35f),
                    radius = pulseRadius * 2.2f,
                    center = Offset(sx.toFloat(), sy.toFloat())
                )
                drawCircle(
                    color = Cyan500,
                    radius = 36f,
                    center = Offset(sx.toFloat(), sy.toFloat()),
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // 4. Draw Vehicle Markers
            val currentTime = System.currentTimeMillis()
            vehicles.forEach { vehicle ->
                val vx = ((vehicle.longitude - (mapCenterLng - lngSpan / 2)) / lngSpan) * width
                val vy = height - (((vehicle.latitude - (mapCenterLat - latSpan / 2)) / latSpan) * height)
                val isSelected = vehicle.id == selectedVehicle?.id

                val isOfflineOrStale = vehicle.status == VehicleStatus.OFFLINE || (currentTime - vehicle.lastUpdateTimestamp) > 5 * 60 * 1000L
                val statusColor = if (isOfflineOrStale) {
                    Color(0xFFEF4444) // Red warning indicator for offline / connectivity stale data
                } else {
                    when (vehicle.status) {
                        VehicleStatus.MOVING -> Emerald400
                        VehicleStatus.IDLE -> Amber500
                        VehicleStatus.STOPPED -> Cyan400
                        VehicleStatus.OFFLINE -> Color(0xFF94A3B8)
                        VehicleStatus.ALERT_GEOFENCE -> Red500
                    }
                }

                val centerOffset = Offset(vx.toFloat(), vy.toFloat())

                // Glow ring
                drawCircle(
                    color = statusColor.copy(alpha = if (isOfflineOrStale) 0.45f else 0.25f),
                    radius = if (isSelected) 32f else 20f,
                    center = centerOffset
                )

                // Directional Heading Indicator Arrow
                rotate(degrees = vehicle.heading, pivot = centerOffset) {
                    val arrowPath = Path().apply {
                        moveTo(centerOffset.x, centerOffset.y - 18f)
                        lineTo(centerOffset.x - 10f, centerOffset.y + 12f)
                        lineTo(centerOffset.x, centerOffset.y + 6f)
                        lineTo(centerOffset.x + 10f, centerOffset.y + 12f)
                        close()
                    }
                    drawPath(
                        path = arrowPath,
                        color = statusColor
                    )
                }

                // Vehicle Dot Center
                drawCircle(
                    color = Color.White,
                    radius = 5f,
                    center = centerOffset
                )

                // Name Pill Label Tag with Connectivity / Stale / Offline Status Indicator
                val labelText = if (isOfflineOrStale) {
                    val minsAgo = (currentTime - vehicle.lastUpdateTimestamp) / 60000L
                    "${vehicle.name.take(10)} • ⚠️ OFFLINE (${minsAgo}m)"
                } else {
                    "${vehicle.name.take(12)} • ${vehicle.speedKmH.toInt()}km/h"
                }

                val labelMeasurer = textMeasurer.measure(
                    text = labelText,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isOfflineOrStale) Color(0xFFFCA5A5) else (if (isDarkTheme) Color.White else Color(0xFF0F172A))
                    )
                )

                val pillWidth = labelMeasurer.size.width + 16f
                val pillHeight = labelMeasurer.size.height + 8f
                val pillX = vx.toFloat() - pillWidth / 2
                val pillY = vy.toFloat() + 22f

                drawRoundRect(
                    color = if (isDarkTheme) Color(0xCC0F172A) else Color(0xEEFFFFFF),
                    topLeft = Offset(pillX, pillY),
                    size = Size(pillWidth, pillHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )

                drawRoundRect(
                    color = statusColor,
                    topLeft = Offset(pillX, pillY),
                    size = Size(pillWidth, pillHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                    style = Stroke(width = 1.dp.toPx())
                )

                drawText(
                    textLayoutResult = labelMeasurer,
                    topLeft = Offset(pillX + 8f, pillY + 4f)
                )
            }
        }
    }
}

private fun DrawScope.drawMapTopography(width: Float, height: Float, isDarkTheme: Boolean, isSatelliteMode: Boolean = true) {
    if (isSatelliteMode) {
        // Satellite Hybrid Photorealistic Terrain Base
        // Deep Ocean / Coastal Water Gradient Base
        drawRect(color = Color(0xFF08121E)) // Dark Atlantic ocean base

        // Senegal Peninsular Landmass Polygon (Dakar / Cap-Vert Peninsula Satellite Texture)
        val landPath = Path().apply {
            moveTo(width * 0.15f, height)
            lineTo(width * 0.12f, height * 0.75f) // Dakar Almadies point
            lineTo(width * 0.30f, height * 0.45f) // Technopole / Cambérène
            lineTo(width * 0.60f, height * 0.35f) // Diamniadio / Rufisque
            lineTo(width, height * 0.15f)
            lineTo(width, height)
            close()
        }
        drawPath(path = landPath, color = Color(0xFF131D12)) // Dense vegetation / urban satellite texture

        // Coastal Shallow Water Reef / Coral Shelf (Light Turquoise glow)
        drawPath(
            path = landPath,
            color = Color(0xFF1E3A5F).copy(alpha = 0.6f),
            style = Stroke(width = 16.dp.toPx())
        )
        drawPath(
            path = landPath,
            color = Color(0xFF00A896).copy(alpha = 0.25f),
            style = Stroke(width = 6.dp.toPx())
        )

        // Urban Dense Clusters (Satellite Rooftops & Industrial Districts)
        drawCircle(
            color = Color(0xFF2A342B).copy(alpha = 0.7f),
            radius = width * 0.18f,
            center = Offset(width * 0.22f, height * 0.72f) // Dakar Centre / Plateau
        )
        drawCircle(
            color = Color(0xFF233025).copy(alpha = 0.6f),
            radius = width * 0.15f,
            center = Offset(width * 0.55f, height * 0.42f) // Diamniadio Industrial Zone
        )

        // Satellite Road Overlay Corridors (Highways in Neon / Cyan / Amber)
        val autorouteA1 = Path().apply {
            moveTo(width * 0.12f, height * 0.85f)
            quadraticTo(width * 0.25f, height * 0.55f, width * 0.55f, height * 0.42f)
            lineTo(width * 0.95f, height * 0.20f)
        }
        drawPath(
            path = autorouteA1,
            color = Color(0xFF00E5FF).copy(alpha = 0.75f), // Cyan Illuminated Autoroute A1
            style = Stroke(width = 3.dp.toPx())
        )

        val roadN1 = Path().apply {
            moveTo(width * 0.20f, height * 0.90f)
            quadraticTo(width * 0.38f, height * 0.65f, width * 0.75f, height * 0.50f)
        }
        drawPath(
            path = roadN1,
            color = Color(0xFFFFB300).copy(alpha = 0.65f), // Amber Highway N1
            style = Stroke(width = 2.dp.toPx())
        )

        // Subtle Satellite Coordinate Grid Overlay
        val gridColor = Color(0xFF2A3A4E).copy(alpha = 0.4f)
        val gridSpacing = 80f
        var x = 0f
        while (x < width) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
            x += gridSpacing
        }
        var y = 0f
        while (y < height) {
            drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
            y += gridSpacing
        }
    } else {
        // Standard Vector Topography Mode
        val gridColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFCBD5E1)

        val gridSpacing = 60f
        var x = 0f
        while (x < width) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
            x += gridSpacing
        }
        var y = 0f
        while (y < height) {
            drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
            y += gridSpacing
        }

        val highwayColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFF94A3B8)
        val mainArteryPath = Path().apply {
            moveTo(width * 0.1f, height * 0.8f)
            quadraticTo(width * 0.25f, height * 0.5f, width * 0.5f, height * 0.45f)
            lineTo(width * 0.8f, height * 0.3f)
        }
        drawPath(mainArteryPath, highwayColor, style = Stroke(width = 4.dp.toPx()))

        val coastPath = Path().apply {
            moveTo(0f, height * 0.9f)
            lineTo(width * 0.2f, height * 0.6f)
            lineTo(width * 0.35f, height * 0.1f)
            lineTo(width, 0f)
        }
        drawPath(
            coastPath,
            if (isDarkTheme) Color(0xFF0F223A) else Color(0xFFBAE6FD),
            style = Stroke(width = 12.dp.toPx())
        )
    }
}
