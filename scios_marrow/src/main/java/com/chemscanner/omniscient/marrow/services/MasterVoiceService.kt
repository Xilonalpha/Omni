package com.chemscanner.omniscient.marrow.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * THE SENTIENT MASTER VOICE SERVICE: v5.6 (FOREGROUND WRAPPER).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Keep the MasterVoiceManager active in the background.
 */
@AndroidEntryPoint
class MasterVoiceService : Service() {

    @Inject lateinit var voiceManager: MasterVoiceManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startSovereignForeground()
    }

    private fun startSovereignForeground() {
        val notification = createNotification()
        val notificationId = 3

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ requires specific types and checks
            val hasMicPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            // On Android 14+, we MUST include microphone type if we want to use the mic in background.
            // If we don't have the permission yet, starting with microphone type will throw SecurityException.
            // However, the MasterVoiceService is intended for microphone use.
            
            val foregroundServiceType = if (hasMicPermission) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            }

            try {
                startForeground(notificationId, notification, foregroundServiceType)
                Timber.d("Foreground service started with type(s): $foregroundServiceType")
            } catch (e: Exception) {
                Timber.e(e, "Failed to start foreground with combined types, attempting specialUse only")
                try {
                    startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                } catch (e2: Exception) {
                    Timber.e(e2, "Critical failure starting foreground service")
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-13
            startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            // Pre-Android 10
            startForeground(notificationId, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Only start listening if we have permissions on API 34+
        val hasMicPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasMicPermission) {
            voiceManager.startListening()
        } else {
            Timber.w("MasterVoiceService started but lacks RECORD_AUDIO permission")
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "MARROW_VOICE")
            .setContentTitle("Master Voice Active")
            .setContentText("Listening for Architect commands...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MARROW_VOICE",
                "Marrow Voice Control",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Marrow background voice processing"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        voiceManager.stop()
        super.onDestroy()
    }
}
