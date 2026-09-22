package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.BrainActivity
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BciIntegrationService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val commander: UniversalCommanderService,
    private val gateway: OmnipresenceGateway
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isProcessingIntent = false
    private var lastFocusLevel = 0f
    private var lastHeartRate = 0

    init {
        observeWatchBiometrics()
        
        // ACTIVARE commander & gateway: Sincronizare inițială cu nucleul de control
        scope.launch {
            Timber.d("BCI: Link established with Commander (${commander.hashCode()}) and Gateway (${gateway.hashCode()})")
        }
    }

    fun startNeuralStreaming() {
        globalKnowledge.logEvent("BCI", "Neural Streaming Active", 2)
    }

    fun startCalibration(reason: String) {
        calibrateNeuralLink(reason)
    }

    fun calibrateNeuralLink(reason: String) {
        scope.launch {
            globalKnowledge.logEvent("BCI", "Calibration: $reason", 3)
            ttsService.speak("Calibrare link neural: $reason")
        }
    }

    private fun observeWatchBiometrics() {
        scope.launch {
            globalKnowledge.watchStatus.collectLatest { status ->
                if (!status.isConnected) return@collectLatest
                
                // ACTIVARE lastHeartRate & lastFocusLevel: Monitorizare trend biometric
                val hr = status.externalHeartRate
                val focusFactor = if (hr in 55..85) 0.9f else 0.5f
                
                if (hr != lastHeartRate || focusFactor != lastFocusLevel) {
                    lastHeartRate = hr
                    lastFocusLevel = focusFactor
                    
                    // ACTIVARE isProcessingIntent: Marcăm procesarea stării neurale
                    isProcessingIntent = true
                    try {
                        globalKnowledge.updateBrainActivity(BrainActivity(beta = focusFactor, focusLevel = focusFactor))
                        
                        // Utilizăm gateway pentru a raporta vectorul de intenție dacă focusul este critic
                        if (focusFactor > 0.85f) {
                            gateway.onIntentionVectorDetected("DEEP_FOCUS_STATE", focusFactor)
                        }
                    } finally {
                        isProcessingIntent = false
                    }
                }
            }
        }
    }
}
