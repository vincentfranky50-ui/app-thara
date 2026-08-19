package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.model.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Gestionnaire matériel de localisation GPS et de capteur d'orientation (Boussole).
 */
class LocationTracker(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val sensorManager: SensorManager? =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    /**
     * Vérifie si le module GPS ou réseau est activé dans les paramètres du téléphone
     */
    fun isGpsEnabled(): Boolean {
        return try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Émet les mises à jour de position GPS en temps réel sous forme de Flow
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(intervalMs: Long = 2000L): Flow<UserLocation> = callbackFlow {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setMinUpdateDistanceMeters(1.5f)
            .setWaitForAccurateLocation(false)
            .build()

        // Émission initiale de la dernière position connue si disponible
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    trySend(
                        UserLocation(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            altitudeMeters = loc.altitude,
                            accuracyMeters = if (loc.hasAccuracy()) loc.accuracy else 10f,
                            bearingDegrees = if (loc.hasBearing()) loc.bearing else 0f,
                            speedMps = if (loc.hasSpeed()) loc.speed else 0f,
                            timestamp = loc.time,
                            isGpsActive = isGpsEnabled()
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            // Permission manquante gérée au niveau UI
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    val userLocation = UserLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        altitudeMeters = location.altitude,
                        accuracyMeters = if (location.hasAccuracy()) location.accuracy else 5f,
                        bearingDegrees = if (location.hasBearing()) location.bearing else 0f,
                        speedMps = if (location.hasSpeed()) location.speed else 0f,
                        timestamp = location.time,
                        isGpsActive = isGpsEnabled()
                    )
                    trySend(userLocation)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * Émet les variations de cap de la boussole matérielle (Azimuth en degrés 0..360°)
     */
    fun getCompassBearingUpdates(): Flow<Float> = callbackFlow {
        if (sensorManager == null) {
            trySend(0f)
            awaitClose { }
            return@callbackFlow
        }

        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        var lastAccelerometer = FloatArray(3)
        var lastMagnetometer = FloatArray(3)
        var lastAccelerometerSet = false
        var lastMagnetometerSet = false
        val rMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rMatrix, event.values)
                    SensorManager.getOrientation(rMatrix, orientation)
                    val azimuthInRadians = orientation[0]
                    var azimuthInDeg = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
                    if (azimuthInDeg < 0) azimuthInDeg += 360f
                    trySend(azimuthInDeg)
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                    lastAccelerometerSet = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                    lastMagnetometerSet = true
                }

                if (lastAccelerometerSet && lastMagnetometerSet) {
                    if (SensorManager.getRotationMatrix(rMatrix, null, lastAccelerometer, lastMagnetometer)) {
                        SensorManager.getOrientation(rMatrix, orientation)
                        val azimuthInRadians = orientation[0]
                        var azimuthInDeg = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
                        if (azimuthInDeg < 0) azimuthInDeg += 360f
                        trySend(azimuthInDeg)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometer?.let { sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI) }
            magnetometer?.let { sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI) }
        }

        awaitClose {
            sensorManager.unregisterListener(sensorListener)
        }
    }
}
