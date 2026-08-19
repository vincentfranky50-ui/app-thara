package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.AlertEntity
import com.example.data.db.TharaDatabase
import com.example.model.Alert
import com.example.model.AlertSeverity
import com.example.model.AlertType
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TharaFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "TharaFCMService"
        const val CHANNEL_CRITICAL_ALERTS = "thara_critical_alerts_channel"
        const val CHANNEL_GEOFENCE_ALERTS = "thara_geofence_channel"
        const val CHANNEL_FUEL_MAINTENANCE = "thara_fuel_maintenance_channel"
        const val CHANNEL_GENERAL_NOTIFS = "thara_general_channel"

        const val EXTRA_VEHICLE_ID = "extra_vehicle_id"
        const val EXTRA_ALERT_TYPE = "extra_alert_type"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nouveau token FCM reçu: $token")
        
        // Sauvegarder dans SharedPreferences pour l'associer à l'utilisateur/véhicule
        val prefs = applicationContext.getSharedPreferences("thara_fleet_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_device_token", token).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message FCM reçu de: ${remoteMessage.from}")

        // 1. Extraction des données de payload
        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = notification?.title ?: data["title"] ?: "Alerte Flotte Thara"
        val body = notification?.body ?: data["body"] ?: data["message"] ?: "Une alerte a été signalée sur votre flotte."
        
        val vehicleId = data["vehicleId"] ?: "VH-RAV4"
        val vehicleName = data["vehicleName"] ?: data["vehicle"] ?: "Véhicule"
        val licensePlate = data["licensePlate"] ?: ""
        val alertTypeStr = data["alertType"] ?: "GENERAL"
        val severityStr = data["severity"] ?: "WARNING"
        val fuelPctStr = data["fuelLevelPct"]
        val zoneName = data["zoneName"]

        val alertType = mapAlertType(alertTypeStr)
        val alertSeverity = mapAlertSeverity(severityStr)

        // 2. Persister l'alerte en base de données Room locale pour mise à jour UI immédiate
        serviceScope.launch {
            try {
                val database = TharaDatabase.getDatabase(applicationContext)
                val alertEntity = AlertEntity(
                    id = "ALT-FCM-${System.currentTimeMillis()}",
                    vehicleId = vehicleId,
                    vehicleName = vehicleName,
                    licensePlate = licensePlate,
                    type = alertType.name,
                    severity = alertSeverity.name,
                    message = body,
                    timestamp = System.currentTimeMillis(),
                    acknowledged = false
                )
                database.fleetDao().insertAlert(alertEntity)
                Log.d(TAG, "Alerte FCM enregistrée en base Room avec succès: ${alertEntity.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de l'enregistrement de l'alerte FCM", e)
            }
        }

        // 3. Afficher la notification système Android avec canal adapté
        showSystemNotification(
            title = title,
            message = body,
            vehicleId = vehicleId,
            alertType = alertType,
            severity = alertSeverity,
            fuelLevel = fuelPctStr,
            zoneName = zoneName
        )
    }

    private fun showSystemNotification(
        title: String,
        message: String,
        vehicleId: String,
        alertType: AlertType,
        severity: AlertSeverity,
        fuelLevel: String?,
        zoneName: String?
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Création des canaux de notifications (Android 8.0+)
        createNotificationChannels(notificationManager)

        val channelId = when (alertType) {
            AlertType.GEOFENCE_EXIT, AlertType.GEOFENCE_ENTRY -> CHANNEL_GEOFENCE_ALERTS
            AlertType.LOW_BATTERY, AlertType.DISCONNECTED -> CHANNEL_FUEL_MAINTENANCE
            AlertType.SPEEDING, AlertType.RAPID_ACCELERATION -> CHANNEL_CRITICAL_ALERTS
            else -> {
                if (fuelLevel != null || message.contains("carburant", ignoreCase = true) || message.contains("fuel", ignoreCase = true)) {
                    CHANNEL_FUEL_MAINTENANCE
                } else if (severity == AlertSeverity.CRITICAL) {
                    CHANNEL_CRITICAL_ALERTS
                } else {
                    CHANNEL_GENERAL_NOTIFS
                }
            }
        }

        // Intent de navigation quand l'utilisateur clique sur la notification
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_VEHICLE_ID, vehicleId)
            putExtra(EXTRA_ALERT_TYPE, alertType.name)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val priority = when (severity) {
            AlertSeverity.CRITICAL -> NotificationCompat.PRIORITY_MAX
            AlertSeverity.WARNING -> NotificationCompat.PRIORITY_HIGH
            AlertSeverity.INFO -> NotificationCompat.PRIORITY_DEFAULT
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        // Couleur et vibrations pour les alertes critiques
        if (severity == AlertSeverity.CRITICAL) {
            notificationBuilder.setColor(Color.RED)
            notificationBuilder.setVibrate(longArrayOf(0, 500, 200, 500))
        } else {
            notificationBuilder.setColor(Color.parseColor("#10B981"))
        }

        val notificationId = (System.currentTimeMillis() % 10000).toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannels(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal Alertes Critiques & Urgences
            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL_ALERTS,
                "Alertes Critiques Flotte",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications prioritaires : survitesse extrême, choc, vol suspect"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            // Canal Géofence (Entrée / Sortie de zone)
            val geofenceChannel = NotificationChannel(
                CHANNEL_GEOFENCE_ALERTS,
                "Alertes de Géolocalisation & Zones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertes de franchissement, entrée ou sortie de périmètre sécurisé"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }

            // Canal Carburant & Maintenance
            val fuelChannel = NotificationChannel(
                CHANNEL_FUEL_MAINTENANCE,
                "Alertes Carburant & Diagnostic",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alertes de niveau de carburant bas, anomalie moteur ou révision"
                enableLights(true)
                lightColor = Color.YELLOW
            }

            // Canal Général
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL_NOTIFS,
                "Informations Générales",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mises à jour et messages généraux de la plateforme"
            }

            manager.createNotificationChannels(
                listOf(criticalChannel, geofenceChannel, fuelChannel, generalChannel)
            )
        }
    }

    private fun mapAlertType(type: String): AlertType {
        return try {
            AlertType.valueOf(type.uppercase())
        } catch (e: Exception) {
            when {
                type.contains("EXIT", ignoreCase = true) || type.contains("BREACH", ignoreCase = true) || type.contains("SORTIE", ignoreCase = true) -> AlertType.GEOFENCE_EXIT
                type.contains("ENTRY", ignoreCase = true) || type.contains("ENTREE", ignoreCase = true) -> AlertType.GEOFENCE_ENTRY
                type.contains("ZONE", ignoreCase = true) || type.contains("GEOFENCE", ignoreCase = true) -> AlertType.GEOFENCE_EXIT
                type.contains("BATTERY", ignoreCase = true) || type.contains("BATTERIE", ignoreCase = true) -> AlertType.LOW_BATTERY
                type.contains("SPEED", ignoreCase = true) || type.contains("VITESSE", ignoreCase = true) -> AlertType.SPEEDING
                type.contains("ACCEL", ignoreCase = true) || type.contains("BRAKE", ignoreCase = true) || type.contains("FREIN", ignoreCase = true) -> AlertType.RAPID_ACCELERATION
                type.contains("DISCONNECT", ignoreCase = true) -> AlertType.DISCONNECTED
                else -> AlertType.SPEEDING
            }
        }
    }

    private fun mapAlertSeverity(severity: String): AlertSeverity {
        return try {
            AlertSeverity.valueOf(severity.uppercase())
        } catch (e: Exception) {
            when (severity.lowercase()) {
                "critical", "critique", "high", "danger" -> AlertSeverity.CRITICAL
                "warning", "avertissement", "medium" -> AlertSeverity.WARNING
                else -> AlertSeverity.INFO
            }
        }
    }
}
