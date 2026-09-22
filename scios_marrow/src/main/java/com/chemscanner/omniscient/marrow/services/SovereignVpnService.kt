package com.chemscanner.omniscient.marrow.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject

/**
 * SOVEREIGN VPN v4.5 (ARCHITECT XILON - GHOST EDITION).
 * PROTOCOL: RoNaQCI-v7 (2026 Stealth Standard).
 * MISSION: Zero-Latency Data Channels & Real-Time Mesh Routing.
 */
@AndroidEntryPoint
class SovereignVpnService : VpnService() {

    @Inject lateinit var globalKnowledge: GlobalKnowledgeRepository
    @Inject lateinit var shadowMesh: ShadowMeshService
    @Inject lateinit var ttsService: TextToSpeechService
    @Inject lateinit var iotBridge: QuantumIotBridge

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isVpnActive = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startSovereignForeground()
    }

    private fun startSovereignForeground() {
        val notification = createNotification()
        val notificationId = 104

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(notificationId, notification)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "MARROW_VPN")
            .setContentTitle("Sovereign VPN Active")
            .setContentText("Xilon Ghost Tunneling Engaged")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MARROW_VPN",
                "Marrow VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Secure data tunneling for the Sovereign Mesh"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopVpn()
            return START_NOT_STICKY
        }
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (isVpnActive) return
        isVpnActive = true

        iotBridge.setVpnCallback { data ->
            writeResponseToTunnel(data)
        }

        serviceScope.launch {
            try {
                val builder = Builder()
                    .addAddress("10.0.0.2", 24)
                    .addAddress("fd00:xilon::2", 64) 
                    .addRoute("0.0.0.0", 0) 
                    .addRoute("::", 0) 
                    .addDnsServer("9.9.9.9") 
                    .addDnsServer("149.112.112.112")
                    .addDnsServer("2620:fe::fe")
                    .setSession("XILON_GHOST_TUNNEL")
                    .setMtu(1350) 
                    .setBlocking(true) 
                    .allowBypass()

                try {
                    builder.addDisallowedApplication(packageName)
                } catch (e: Exception) {
                    Timber.w("Could not disallow self-package")
                }

                vpnInterface = builder.establish()
                
                if (vpnInterface != null) {
                    ttsService.speak("Sistem RoNaQCI ancorat. Navigare securizată activă sub mască Xilon.")
                    runRealVpnLoop()
                } else {
                    Timber.e("VPN Interface establishment failed (null)")
                    stopVpn()
                }
            } catch (e: Exception) {
                Timber.e(e, "VPN Deployment failed")
                stopVpn()
            }
        }
    }

    private suspend fun runRealVpnLoop() = withContext(Dispatchers.IO) {
        val tunnel = vpnInterface ?: return@withContext
        val inChannel = FileInputStream(tunnel.fileDescriptor).channel
        val buffer = ByteBuffer.allocate(65535)

        globalKnowledge.logEvent("VPN_CORE", "Architect Xilon Mode: Zero-latency channels active.", 5)

        while (isVpnActive && isActive) {
            try {
                buffer.clear()
                val length = inChannel.read(buffer)
                if (length > 0) {
                    buffer.flip()
                    val packet = ByteArray(length)
                    buffer.get(packet)
                    iotBridge.tunnelVpnPacket(packet)
                } else if (length == 0) {
                    yield() 
                }
            } catch (e: Exception) {
                if (isVpnActive) yield()
            }
        }
    }

    private fun writeResponseToTunnel(data: ByteArray) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                vpnInterface?.let {
                    val outChannel = FileOutputStream(it.fileDescriptor).channel
                    val buffer = ByteBuffer.wrap(data)
                    while (buffer.hasRemaining()) {
                        outChannel.write(buffer)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to write back to TUN interface")
            }
        }
    }

    private fun stopVpn() {
        isVpnActive = false
        vpnInterface?.close()
        vpnInterface = null
        serviceScope.cancel()
        ttsService.speak("Tunelul Quantum a fost retras.")
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
