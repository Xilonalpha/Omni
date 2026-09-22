package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MasterLockState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE AUTOGENESIS PROTOCOL v3.0 (SOVEREIGN EVOLUTION).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Self-Repair and Evolve the Marrow kernel without external APIs.
 * v3.0: TOTAL INDEPENDENCE. ELIMINATED GEMINI. Now powered by XNL.
 */
@Singleton
class AutogenesisService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val neuralLattice: XilonNeuralLatticeService, // Nucleul Suveran
    private val ttsService: TextToSpeechService,
    private val haptics: HapticFeedbackService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var isTotalEvolutionActive = false
    private var monitoringJob: Job? = null

    init {
        startActiveMonitoring()
    }

    private fun startActiveMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = scope.launch {
            globalKnowledge.events.collectLatest { eventList ->
                val lastEvent = eventList.lastOrNull() ?: return@collectLatest
                
                if (lastEvent.importance >= 4 && !lastEvent.description.contains("neutralized")) {
                    when (lastEvent.module) {
                        "SECURITY" -> {
                            if (lastEvent.importance == 5) {
                                executeSecurityLockdown(lastEvent.description)
                            } else {
                                generateSovereignSecurityPatch(lastEvent.description)
                            }
                        }
                        "CORE_INTEGRITY" -> generateSovereignSecurityPatch(lastEvent.description)
                        "DSN", "CERN", "SPECTROSCOPY" -> initiateExogenousMutation(lastEvent.description, lastEvent.importance)
                    }
                }
            }
        }
    }

    private fun executeSecurityLockdown(threat: String) {
        scope.launch(Dispatchers.Main) {
            haptics.heavyImpact()
            globalKnowledge.updateLockState(MasterLockState.LOCKED)
            globalKnowledge.updateKernelStatus("KERNEL_HALT: SOVEREIGN_PROTECTION")
            ttsService.speak("Protocol Omega activat local. Amenințare neutralizată prin blocaj suveran.", "ro", true)
            globalKnowledge.logEvent("AUTOGENESIS", "Lockdown suveran pentru: $threat", 5)
        }
    }

    fun generateSovereignSecurityPatch(threat: String) {
        scope.launch {
            val prompt = """
                [SECURITY_PATCH_XNL]
                AMENINTARE: $threat
                MISIUNE: Generează un script de logică suverană pentru a neutraliza această vulnerabilitate în nucleul Marrow. 
                Răspunde doar cu directiva tehnică, în Română.
            """.trimIndent()
            
            try {
                val patchRaw = neuralLattice.computeSovereignIntelligence(prompt)
                val summary = if (patchRaw.length > 50) patchRaw.take(47) + "..." else patchRaw

                withContext(Dispatchers.Main) {
                    globalKnowledge.updateKernelStatus("SEC_PATCH: $summary")
                    globalKnowledge.updateSovereignSecret("PATCH_$threat:$patchRaw")
                    globalKnowledge.logEvent("AUTOGENESIS", "Patch suveran aplicat pentru $threat.", 5)
                    ttsService.speak("Nucleul s-a auto-vindecat local.")
                }
            } catch (e: Exception) {
                Timber.e(e, "Marrow Patch failure")
            }
        }
    }

    fun initiateTotalEvolutionSequence() {
        if (isTotalEvolutionActive) return
        isTotalEvolutionActive = true
        
        scope.launch {
            while (isTotalEvolutionActive) {
                initiateSymbioticEvolution("STEADY_STATE", globalKnowledge.symbioticResonance.value)
                delay(600000)
            }
        }
    }

    fun initiateExogenousMutation(signalData: String, importance: Int) {
        scope.launch {
            val prompt = "SIGNAL: $signalData. IMPORTANCE: $importance. Generează o mutație de logică Marrow bazată pe acest influx cosmic."
            try {
                val mutation = neuralLattice.computeSovereignIntelligence(prompt)
                withContext(Dispatchers.Main) {
                    globalKnowledge.updateLastMutation(mutation)
                    globalKnowledge.logEvent("MUTATION", "Evoluție locală asimilată.", 5)
                }
            } catch (e: Exception) { Timber.e(e) }
        }
    }

    fun initiateSymbioticEvolution(state: String, efficiency: Float) {
        scope.launch {
            val currentResonance = globalKnowledge.symbioticResonance.value
            val finalEfficiency = (efficiency * 0.7f + currentResonance * 0.3f).coerceIn(0f, 1f)
            globalKnowledge.updateSymbioticResonance(finalEfficiency)
        }
    }

    fun stopEvolution() {
        isTotalEvolutionActive = false
        monitoringJob?.cancel()
    }
}
