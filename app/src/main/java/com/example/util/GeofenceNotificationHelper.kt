package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object GeofenceNotificationHelper {
    const val CHANNEL_ID = "geofence_alerts_channel"
    const val CHANNEL_NAME = "Alertes Géofence & Flotte"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications locales en temps réel pour l'entrée et la sortie des périmètres de sécurité."
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 200, 300)
                    setShowBadge(true)
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d("GeofenceNotifHelper", "Created notification channel: $CHANNEL_ID")
            } catch (e: Exception) {
                Log.e("GeofenceNotifHelper", "Error creating notification channel", e)
            }
        }
    }

    fun sendGeofenceNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        createNotificationChannel(context)

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, builder.build())
            Log.d("GeofenceNotifHelper", "Posted notification #$notificationId: $title")
        } catch (e: Exception) {
            Log.e("GeofenceNotifHelper", "Failed to send geofence notification", e)
        }
    }
}
