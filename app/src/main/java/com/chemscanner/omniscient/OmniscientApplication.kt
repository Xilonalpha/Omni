package com.chemscanner.omniscient

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.chemscanner.omniscient.marrow.services.ModelProvider
import com.chemscanner.omniscient.marrow.services.OmniscientOrchestrator
import com.chemscanner.omniscient.marrow.services.DreamingService
import com.chemscanner.omniscient.marrow.services.MasterVoiceService
import com.chemscanner.omniscient.marrow.services.NeuralImmunityService
import com.google.android.filament.Filament
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * MAIN SOVEREIGN APPLICATION.
 * AUTHORITY: ARCHITECT XILON.
 * v1.1: Forced Orchestrator instantiation to ensure Mesh is always ONLINE.
 */
@HiltAndroidApp
class OmniscientApplication : Application() {

    @Inject lateinit var orchestrator: OmniscientOrchestrator
    @Inject lateinit var dreamingService: DreamingService

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // FORȚARE PORNIRE NUCLEU: Referențiem orchestratorul pentru a-l trezi
        Timber.d("Initializing Marrow Core v4.8: ${orchestrator.hashCode()}")
        
        createNotificationChannels()
        
        // Initialize native components
        ModelProvider.initializeRegistry()
        Filament.init()

        // Start core services as actual Android Services
        startCoreMarrowServices()

        // Activate Subconscious simulation
        dreamingService.startDreamCycle()
        
        Timber.d("Marrow Application: Sovereign Registry and Core Services Online.")
    }

    private fun startCoreMarrowServices() {
        val voiceIntent = Intent(this, MasterVoiceService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(voiceIntent)
        } else {
            startService(voiceIntent)
        }

        val immunityIntent = Intent(this, NeuralImmunityService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(immunityIntent)
        } else {
            startService(immunityIntent)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MARROW_CORE",
                "Marrow Core Services",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Essential background processes for Omniscient Scanner"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
