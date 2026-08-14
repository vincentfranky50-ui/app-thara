package com.example.ui.components

import android.graphics.Color
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.Trip

@Composable
fun TripMapCanvas(
    trip: Trip,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val htmlContent = remember(trip, isDarkTheme) {
        generateTripMapHtml(trip, isDarkTheme)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .testTag("trip_map_canvas")
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    webViewClient = object : WebViewClient() {
                        override fun onRenderProcessGone(
                            view: WebView?,
                            detail: RenderProcessGoneDetail?
                        ): Boolean {
                            view?.destroy()
                            return true
                        }
                    }
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = false
                        allowContentAccess = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                    }
                    loadDataWithBaseURL("https://leafletjs.com", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://leafletjs.com", htmlContent, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun generateTripMapHtml(trip: Trip, isDarkTheme: Boolean): String {
    val waypointsJson = trip.waypoints.joinToString(",") {
        "{\"lat\": ${it.latitude}, \"lng\": ${it.longitude}, \"speed\": ${it.speedKmH}, \"time\": ${it.timestamp}}"
    }

    val stopsJson = trip.stopPoints.joinToString(",") { stop ->
        """
        {
            "id": "${stop.id}",
            "lat": ${stop.latitude},
            "lng": ${stop.longitude},
            "address": "${stop.address.replace("\"", "\\\"")}",
            "duration": ${stop.durationMinutes},
            "time": "${stop.arrivalTime}",
            "reason": "${stop.reason.replace("\"", "\\\"")}"
        }
        """.trimIndent()
    }

    val startLat = trip.waypoints.firstOrNull()?.latitude ?: 14.7167
    val startLng = trip.waypoints.firstOrNull()?.longitude ?: -17.4677

    val tileUrl = if (isDarkTheme) {
        "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
    } else {
        "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
    }

    val labelTileUrl = "https://services.arcgisonline.com/ArcGIS/rest/services/Reference/World_Boundaries_and_Places/MapServer/tile/{z}/{y}/{x}"

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map {
                    width: 100%;
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    background: transparent;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                }
                .leaflet-container { background: #0f172a !important; }
                .custom-popup .leaflet-popup-content-wrapper {
                    background: rgba(15, 23, 42, 0.95);
                    color: #ffffff;
                    border-radius: 14px;
                    padding: 10px 14px;
                    box-shadow: 0 12px 30px rgba(0,0,0,0.5);
                    border: 1px solid rgba(255,255,255,0.15);
                    backdrop-filter: blur(8px);
                }
                .custom-popup .leaflet-popup-tip { background: rgba(15, 23, 42, 0.95); }
                .stop-badge {
                    background: #dc2626;
                    color: #ffffff;
                    border: 2px solid #ffffff;
                    border-radius: 20px;
                    padding: 4px 10px;
                    font-weight: 800;
                    font-size: 11px;
                    box-shadow: 0 4px 14px rgba(220, 38, 38, 0.7);
                    text-align: center;
                    white-space: nowrap;
                }
                .start-badge {
                    background: #059669;
                    color: #ffffff;
                    border: 2px solid #ffffff;
                    border-radius: 20px;
                    padding: 4px 10px;
                    font-weight: 800;
                    font-size: 11px;
                    box-shadow: 0 4px 14px rgba(5, 150, 105, 0.7);
                    white-space: nowrap;
                }
                .end-badge {
                    background: #2563eb;
                    color: #ffffff;
                    border: 2px solid #ffffff;
                    border-radius: 20px;
                    padding: 4px 10px;
                    font-weight: 800;
                    font-size: 11px;
                    box-shadow: 0 4px 14px rgba(37, 99, 235, 0.7);
                    white-space: nowrap;
                }
                .vehicle-replay-marker {
                    background: #f59e0b;
                    border: 3px solid #ffffff;
                    width: 22px;
                    height: 22px;
                    border-radius: 50%;
                    box-shadow: 0 0 16px #f59e0b;
                    animation: pulse 1.5s infinite;
                }
                @keyframes pulse {
                    0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(245, 158, 11, 0.7); }
                    70% { transform: scale(1.15); box-shadow: 0 0 0 10px rgba(245, 158, 11, 0); }
                    100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(245, 158, 11, 0); }
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                document.addEventListener("DOMContentLoaded", function() {
                    var waypoints = [$waypointsJson];
                    var stops = [$stopsJson];

                    var map = L.map('map', { zoomControl: false }).setView([$startLat, $startLng], 12);

                    L.tileLayer('$tileUrl', {
                        maxZoom: 19,
                        attribution: 'Thara GPS Satellite'
                    }).addTo(map);

                    L.tileLayer('$labelTileUrl', {
                        maxZoom: 19,
                        attribution: 'Places & Roads'
                    }).addTo(map);

                    if (waypoints.length > 0) {
                        var latlngs = waypoints.map(function(pt) { return [pt.lat, pt.lng]; });

                        // 1. Ambient Glow Outer Polyline
                        L.polyline(latlngs, {
                            color: '#0284c7',
                            weight: 10,
                            opacity: 0.35,
                            lineCap: 'round',
                            lineJoin: 'round'
                        }).addTo(map);

                        // 2. Speed-encoded Polyline Segments
                        for (var i = 0; i < waypoints.length - 1; i++) {
                            var p1 = waypoints[i];
                            var p2 = waypoints[i + 1];
                            var avgSpeed = (p1.speed + p2.speed) / 2;

                            // Color coding by speed (Figma / Stitch Palette)
                            var segColor = '#38bdf8'; // < 40 km/h (Cyan / Moderate)
                            if (avgSpeed >= 80) {
                                segColor = '#ef4444'; // > 80 km/h (Red / High speed)
                            } else if (avgSpeed >= 60) {
                                segColor = '#f59e0b'; // 60 - 80 km/h (Amber / Fast)
                            } else if (avgSpeed >= 30) {
                                segColor = '#10b981'; // 30 - 60 km/h (Emerald / Cruise)
                            }

                            var segment = L.polyline([[p1.lat, p1.lng], [p2.lat, p2.lng]], {
                                color: segColor,
                                weight: 5,
                                opacity: 0.95,
                                lineCap: 'round',
                                lineJoin: 'round'
                            }).addTo(map);

                            (function(speed, idx) {
                                segment.bindPopup(
                                    "<div class='custom-popup'>" +
                                    "<div style='font-weight:bold; color:" + segColor + "; font-size:12px;'>⚡ Segment de Trajet #" + (idx + 1) + "</div>" +
                                    "<div style='font-size:11px; margin-top:2px;'><b>Vitesse enregistrée:</b> " + Math.round(speed) + " km/h</div>" +
                                    "<div style='font-size:10px; color:#94a3b8;'>Source: Télématique Firestore Live</div>" +
                                    "</div>",
                                    { className: 'custom-popup' }
                                );
                            })(avgSpeed, i);
                        }

                        // 3. Start Point Milestone
                        var startIcon = L.divIcon({
                            className: 'start-badge',
                            html: '🟢 DÉPART (${trip.startTime})',
                            iconSize: [110, 26],
                            iconAnchor: [55, 13]
                        });
                        L.marker(latlngs[0], { icon: startIcon }).addTo(map)
                            .bindPopup(
                                "<div class='custom-popup'>" +
                                "<div style='font-weight:bold; color:#10b981; font-size:12px;'>🟢 Point de Départ</div>" +
                                "<div style='font-size:11px; margin-top:2px;'><b>Lieu:</b> ${trip.departureAddress}</div>" +
                                "<div style='font-size:11px;'><b>Heure:</b> ${trip.startTime}</div>" +
                                "</div>",
                                { className: 'custom-popup' }
                            );

                        // 4. End Point Milestone
                        if (latlngs.length > 1) {
                            var endIcon = L.divIcon({
                                className: 'end-badge',
                                html: '🏁 ARRIVÉE (${trip.endTime})',
                                iconSize: [110, 26],
                                iconAnchor: [55, 13]
                            });
                            L.marker(latlngs[latlngs.length - 1], { icon: endIcon }).addTo(map)
                                .bindPopup(
                                    "<div class='custom-popup'>" +
                                    "<div style='font-weight:bold; color:#3b82f6; font-size:12px;'>🏁 Terminus / Arrivée</div>" +
                                    "<div style='font-size:11px; margin-top:2px;'><b>Lieu:</b> ${trip.arrivalAddress}</div>" +
                                    "<div style='font-size:11px;'><b>Heure:</b> ${trip.endTime}</div>" +
                                    "<div style='font-size:11px;'><b>Distance parcourue:</b> ${trip.distanceKm} km</div>" +
                                    "</div>",
                                    { className: 'custom-popup' }
                                );
                        }

                        // 5. Auto Fit Bounds with padding
                        var bounds = L.latLngBounds(latlngs);
                        map.fitBounds(bounds, { padding: [40, 40] });
                    }

                    // 6. Stop Points Markers
                    stops.forEach(function(stop) {
                        var stopIcon = L.divIcon({
                            className: 'stop-badge',
                            html: '🛑 Arrêt (' + stop.duration + ' min)',
                            iconSize: [110, 24],
                            iconAnchor: [55, 12]
                        });
                        var marker = L.marker([stop.lat, stop.lng], { icon: stopIcon }).addTo(map);
                        marker.bindPopup(
                            "<div class='custom-popup'>" +
                            "<div style='font-weight:bold; color:#f87171; font-size:12px;'>🛑 Arrêt Prolongé</div>" +
                            "<div style='font-size:11px; margin-top:2px;'><b>Adresse:</b> " + stop.address + "</div>" +
                            "<div style='font-size:11px;'><b>Arrivée:</b> " + stop.time + " (" + stop.duration + " min)</div>" +
                            "<div style='font-size:11px; color:#cbd5e1;'><b>Motif:</b> " + stop.reason + "</div>" +
                            "</div>",
                            { className: 'custom-popup' }
                        );
                    });
                });
            </script>
        </body>
        </html>
    """.trimIndent()
}
