package com.example.data.service

import com.example.model.RoutePoint
import com.example.model.RouteResult
import com.example.model.RouteStep
import com.example.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

/**
 * Service de calcul d'itinéraire routier basé sur OpenStreetMap OSRM (Open Source Routing Machine)
 */
class OsmRoutingService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    /**
     * Calcule l'itinéraire entre la position de départ et la destination selon le mode de transport
     */
    suspend fun calculateRoute(
        origin: RoutePoint,
        destination: RoutePoint,
        destinationName: String,
        mode: TravelMode = TravelMode.DRIVING
    ): RouteResult = withContext(Dispatchers.IO) {
        val osrmProfile = when (mode) {
            TravelMode.DRIVING -> "driving"
            TravelMode.WALKING -> "foot"
            TravelMode.BICYCLE -> "bike"
        }

        val url = "https://router.project-osrm.org/route/v1/$osrmProfile/${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}?overview=full&geometries=geojson&steps=true"

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "TharaFleetTrackerAndroidApp/2.0")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val jsonStr = response.body?.string() ?: ""
                    val root = JSONObject(jsonStr)
                    val code = root.optString("code", "")
                    if (code == "Ok") {
                        val routes = root.getJSONArray("routes")
                        if (routes.length() > 0) {
                            val firstRoute = routes.getJSONObject(0)
                            val distance = firstRoute.getDouble("distance").toFloat()
                            val duration = firstRoute.getDouble("duration").toLong()

                            // Polyline points
                            val geometry = firstRoute.getJSONObject("geometry")
                            val coordinates = geometry.getJSONArray("coordinates")
                            val polyline = mutableListOf<RoutePoint>()
                            for (i in 0 until coordinates.length()) {
                                val pt = coordinates.getJSONArray(i)
                                polyline.add(RoutePoint(latitude = pt.getDouble(1), longitude = pt.getDouble(0)))
                            }

                            // Steps
                            val stepsList = mutableListOf<RouteStep>()
                            val legs = firstRoute.getJSONArray("legs")
                            if (legs.length() > 0) {
                                val leg = legs.getJSONObject(0)
                                val steps = leg.getJSONArray("steps")
                                for (s in 0 until steps.length()) {
                                    val stepObj = steps.getJSONObject(s)
                                    val maneuver = stepObj.getJSONObject("maneuver")
                                    val instruction = formatManeuverInstruction(
                                        type = maneuver.optString("type", "turn"),
                                        modifier = maneuver.optString("modifier", ""),
                                        streetName = stepObj.optString("name", "")
                                    )
                                    val stepDist = stepObj.getDouble("distance").toFloat()
                                    val stepDur = stepObj.getDouble("duration").toLong()
                                    stepsList.add(
                                        RouteStep(
                                            instruction = instruction,
                                            distanceMeters = stepDist,
                                            durationSeconds = stepDur,
                                            maneuverType = maneuver.optString("type", "turn")
                                        )
                                    )
                                }
                            }

                            if (polyline.isNotEmpty()) {
                                return@withContext RouteResult(
                                    origin = origin,
                                    destination = destination,
                                    destinationName = destinationName,
                                    totalDistanceMeters = distance,
                                    totalDurationSeconds = duration,
                                    polylinePoints = polyline,
                                    steps = stepsList.ifEmpty { getDefaultSteps(destinationName, distance, duration) },
                                    mode = mode
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback géométrique si le service externe est injoignable
        }

        // Génération d'itinéraire de secours fluide basé sur interpolation géodésique
        generateFallbackRoute(origin, destination, destinationName, mode)
    }

    private fun formatManeuverInstruction(type: String, modifier: String, streetName: String): String {
        val street = if (streetName.isNotBlank()) " sur $streetName" else ""
        return when (type) {
            "depart" -> "Prenez le départ$street"
            "arrive" -> "Vous êtes arrivé à destination"
            "turn" -> when (modifier) {
                "left" -> "Tournez à gauche$street"
                "right" -> "Tournez à droite$street"
                "sharp left" -> "Prenez le virage serré à gauche$street"
                "sharp right" -> "Prenez le virage serré à droite$street"
                "slight left" -> "Serrez légèrement à gauche$street"
                "slight right" -> "Serrez légèrement à droite$street"
                else -> "Tournez$street"
            }
            "roundabout" -> "Prenez le rond-point vers$street"
            "continue" -> "Continuez tout droit$street"
            "fork" -> "Prenez la bifurcation$street"
            else -> "Continuez$street"
        }
    }

    private fun getDefaultSteps(destinationName: String, distance: Float, duration: Long): List<RouteStep> {
        return listOf(
            RouteStep("Prenez le départ", distance * 0.2f, duration / 5, "depart"),
            RouteStep("Continuez sur la voie principale", distance * 0.6f, duration * 3 / 5, "continue"),
            RouteStep("Arrivée à $destinationName", distance * 0.2f, duration / 5, "arrive")
        )
    }

    private fun generateFallbackRoute(
        origin: RoutePoint,
        destination: RoutePoint,
        destName: String,
        mode: TravelMode
    ): RouteResult {
        val polyline = mutableListOf<RoutePoint>()
        val segments = 12

        // Interpolation avec légère déviation urbaine réaliste
        for (i in 0..segments) {
            val fraction = i.toDouble() / segments
            val lat = origin.latitude + (destination.latitude - origin.latitude) * fraction
            val lon = origin.longitude + (destination.longitude - origin.longitude) * fraction

            // Ajout d'une courbure simulant les axes routiers
            val curveFactor = sin(fraction * Math.PI) * 0.0006
            polyline.add(RoutePoint(lat + curveFactor, lon - curveFactor))
        }

        // Calcul distance approximative
        var dist = 0f
        for (i in 0 until polyline.size - 1) {
            dist += com.example.model.PoiItem.computeHaversineDistance(
                polyline[i].latitude, polyline[i].longitude,
                polyline[i + 1].latitude, polyline[i + 1].longitude
            )
        }
        dist = dist.coerceAtLeast(300f)

        // Durée estimée selon vitesse moyenne du mode
        val speedMps = (mode.averageSpeedKmh * 1000f) / 3600f
        val duration = (dist / speedMps).toLong().coerceAtLeast(60)

        return RouteResult(
            origin = origin,
            destination = destination,
            destinationName = destName,
            totalDistanceMeters = dist,
            totalDurationSeconds = duration,
            polylinePoints = polyline,
            steps = getDefaultSteps(destName, dist, duration),
            mode = mode
        )
    }
}
