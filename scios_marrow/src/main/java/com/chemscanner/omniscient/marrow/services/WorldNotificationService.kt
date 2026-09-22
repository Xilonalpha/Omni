package com.chemscanner.omniscient.marrow.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE SOVEREIGN NOTIFICATION HUB v1.1.
 * Breaking the 4th wall: World events alert the Architect in real life.
 * Optimized to prevent notification flooding.
 */
@Singleton
class WorldNotificationService @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "SOVEREIGN_WORLD_ALERTS"
    
    // Fixed IDs to prevent hitting the 50-notification system limit
    private val ID_EMERGENCY = 1001
    private val ID_COUNCIL = 1002
    private val ID_DEFENSE = 1003

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sovereign World Alerts"
            val descriptionText = "Emergency notifications from the persistent world simulation."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * TRIMITERE NOTIFICARE URGENȚĂ:
     * Folosită când un eveniment cu impact mare are loc în absența Arhitectului.
     */
    fun sendEmergencyAlert(title: String, message: String, targetEntityId: String? = null) {
        val intent = Intent().apply {
            setClassName(context.packageName, "com.chemscanner.omniscient.ui.MainActivity")
            if (targetEntityId != null) {
                putExtra("TARGET_ID", targetEntityId)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setGroup("MARROW_ALERTS")

        // Using a consistent ID based on content to update existing notifications
        // instead of creating new ones and hitting the system limit.
        val notificationId = when {
            title.contains("Consens", ignoreCase = true) -> ID_COUNCIL
            title.contains("Defense", ignoreCase = true) -> ID_DEFENSE
            else -> ID_EMERGENCY
        }

        notificationManager.notify(notificationId, builder.build())
    }
}
