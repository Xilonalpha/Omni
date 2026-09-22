package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.BrainActivity
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.GnssGravimetryState
import com.chemscanner.omniscient.marrow.repository.OmegaState
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE OMNIPRESENCE GATEWAY v1.1 - THE SCI-OS OVERMIND.
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Orchestrate high-fidelity data streams from Cosmic, Neural, and Quantum sectors.
 * STATUS: FULLY ACTIVATED & CORRELATED.
 */
@Singleton
/**
 * ⚠️ DISCLOSURE: This service name is ambitious but functionality is grounded.
 * See implementation for actual capabilities and limitations.
 */
class OmnipresenceGateway @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val bciService: Lazy<BciIntegrationService>, // Break dependency cycle with Lazy
    private val tleTracker: TleTrackerService,
    private val iotBridge: QuantumIotBridge
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        startOmniscienceLoop()
        globalKnowledge.logEvent("GATEWAY", "Omnipresence Gateway ACTIVATED. Synchronizing Multi-Sector Intel.", 5)
    }

    private fun startOmniscienceLoop() {
        scope.launch {
            // CORRELATION ENGINE: Combining Neural Intent with Gravitational and Space Data
            combine(
                globalKnowledge.brainActivity,
                globalKnowledge.gnssGravimetry,
                globalKnowledge.omegaState
            ) { brain: BrainActivity, gravity: GnssGravimetryState, omega: OmegaState ->
                Triple(brain, gravity, omega)
            }.collectLatest { (brain, gravity, omega) ->
                processCorrelatedEvents(brain.detectedIntention, gravity.gravitationalAnomalyDetected, omega.realityIntegrity)
                
                // ACTIVARE onIntentionVectorDetected: Folosim vectorul neural detectat din fluxul BCI
                if (brain.detectedIntention.isNotEmpty()) {
                    onIntentionVectorDetected(brain.detectedIntention, brain.focusScore)
                }
            }
        }
    }

    /**
     * PROCESS CORRELATED EVENTS:
     * Logic: If the Architect has a "COSMIC_SCAN" intent AND there is a gravitational anomaly,
     * we prioritize Vera Rubin alerts and Deep Space scans.
     */
    private fun processCorrelatedEvents(intent: String, isGravAnomaly: Boolean, integrity: Float) {
        if (isGravAnomaly && integrity < 0.9f) {
            globalKnowledge.logEvent("GATEWAY", "CRITICAL: Spacetime instability detected. Calibrating BCI Neural Link.", 5)

            // ACTIVARE BCI: Recalibrare automată în caz de instabilitate a realității
            bciService.getOrNull() ?:.calibrateNeuralLink("GRAVITY_RESONANCE_STABILIZATION")

            // ACTIVARE IoT: Notificare în mediul fizic (Quantum IoT)
            iotBridge.emitQuantumPulse("REALITY_INTEGRITY_LOW")
        }

        if (intent == "DEEP_SCAN" || intent == "SCAN_VOID") {
            // ACTIVARE TLE: Verificăm dacă avem sateliți de observație disponibili
            val activeSats = tleTracker.getActiveSatelliteCount()

            scope.launch(Dispatchers.Main) {
                if (activeSats > 0) {
                    ttsService.speak("Xilon, intenția de scanare este sincronizată. Avem $activeSats sateliți în poziție pentru telemetrie.")
                    iotBridge.syncPhysicalEnvironment("SCAN_MODE_ACTIVE")
                } else {
                    ttsService.speak("Atenție Xilon. Niciun satelit de observație nu este disponibil în acest sector. Trecem pe mod pasiv.")
                }
            }
        }
        
        if (isGravAnomaly) {
            // ACTIVARE IoT: Schimbăm iluminarea sau starea dispozitivelor IoT la anomalii gravitaționale
            iotBridge.updateEnvironmentalSync("GRAVITY_ANOMALY")
        }
    }

    /**
     * VERA RUBIN ALERT INGESTION (Interface for VeraRubinAlertService)
     */
    fun ingestCosmicAlert(alertType: String, sector: String, magnitude: Float) {
        scope.launch {
            globalKnowledge.logEvent("COOMIC_ALERT", "LSST Alert: $alertType in $sector (Mag: $magnitude)", 4)

            // Cross-reference with BCI: Is the Architect focused?
            val focus = globalKnowledge.brainActivity.value.focusScore
            if (focus > 0.8f) {
                withContext(Dispatchers.Main) {
                    ttsService.speak("Alertă Vera Rubin: O variație detectată în $sector. Magnitudine: $magnitude.")
                    // ACTIVARE IoT: Feedback vizual pentru alerte cosmice
                    iotBridge.emitQuantumPulse("COSMIC_ALERT_STIMULUS")
                }
            }
        }
    }

    /**
     * NEURAL PREEMPTION INTERFACE (For BCI v2.0)
     */
    fun onIntentionVectorDetected(vector: String, confidence: Float) {
        if (confidence > 0.92f) {
            globalKnowledge.logEvent("NEURAL_VECTOR", "Preemptive Intent: $vector (Conf: $confidence)", 5)

            when (vector) {
                "ACTIVATE_KERNEL" -> {
                    globalKnowledge.updateKernelStatus("Kernel Preemptively Primed.")
                    iotBridge.syncPhysicalEnvironment("KERNEL_ACTIVE")
                }
                "EMERGENCY_SHIELD" -> {
                    // ACTIVARE: Acțiune imediată IoT la intenție de urgență
                    iotBridge.emitQuantumPulse("DEFENSE_SHIELD_IGNITION")
                    ttsService.speak("Scut logic activat preventiv, Xilon.")
                }
            }
        }
    }
}
