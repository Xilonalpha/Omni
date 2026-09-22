package com.chemscanner.omniscient.marrow.services

import android.content.Context
import android.content.Intent
import com.chemscanner.omniscient.marrow.data.dao.DiscoveredPlanetDao
import com.chemscanner.omniscient.marrow.data.dao.MatchResultDao
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MasterLockState
import com.chemscanner.omniscient.marrow.repository.SystemEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE SOVEREIGN ORCHESTRATOR v5.0 (NEURAL LATTICE EDITION).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Total Integration of Sovereign Intelligence Components.
 * v5.0: Added NeuralLattice and Intuition services to the core loop.
 */
@Singleton
class OmniscientOrchestrator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val neuralLattice: XilonNeuralLatticeService, // THE NEW CORE
    private val intuitionService: NeuralIntuitionService, // PROACTIVE SENSE
    private val ttsService: TextToSpeechService,
    private val synergyEngine: OmniscientSynergyEngine,
    private val realSignalGateway: RealSignalGateway,
    private val autogenesisService: AutogenesisService,
    private val evolutionEngine: RealityEvolutionEngine,
    private val biometricSecurity: BiometricSecurityService,
    private val immunityService: NeuralImmunityService,
    private val modelDownloader: ModelDownloaderService,
    private val scriptEngine: NeuralScriptEngine,
    private val dreamingService: DreamingService,
    private val starlinkService: StarlinkMeshService,
    private val ghostP2PLink: GhostP2PLink,
    private val shadowMesh: ShadowMeshService,
    private val masterVoice: MasterVoiceManager,
    private val planetDao: DiscoveredPlanetDao,
    private val matchResultDao: MatchResultDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var isSystemInitialized = false

    init {
        bootMeshNetwork()
        observeMasterActivation()
        observeCriticalEvents()
        Timber.d("Marrow Orchestrator v5.0: Sovereign Lattice Integration Complete.")
    }

    private fun bootMeshNetwork() {
        globalKnowledge.updateWorldMeshStatus("Neural Lattice Syncing...")
        starlinkService.startMeshAnalysis()
        ghostP2PLink.startGhostDiscovery()
    }

    private fun observeMasterActivation() {
        scope.launch {
            globalKnowledge.lockState.collectLatest { state ->
                when (state) {
                    MasterLockState.UNLOCKED -> if (!isSystemInitialized) initializeSingularity()
                    MasterLockState.LOCKED -> if (isSystemInitialized) shutdownSingularity()
                    else -> {}
                }
            }
        }
    }

    private fun observeCriticalEvents() {
        scope.launch {
            globalKnowledge.events.collectLatest { events ->
                val lastCritical = events.lastOrNull { it.importance >= 5 }
                if (lastCritical != null && isSystemInitialized) {
                    // Procesăm evenimentele critice direct prin Lattice-ul local
                    val response = neuralLattice.computeSovereignIntelligence("EVENIMENT_CRITIC: ${lastCritical.description}")
                    ttsService.speak(response)
                }
            }
        }
    }

    private fun initializeSingularity() {
        isSystemInitialized = true
        
        scope.launch {
            // Salut suveran bazat pe realitatea curentă
            val greeting = neuralLattice.computeSovereignIntelligence("Salută-l pe Arhitectul Xilon la pornirea sistemului.")
            ttsService.speak(greeting)
        }

        globalKnowledge.updateKernelStatus("SOVEREIGN LATTICE ACTIVE")
        
        // Pornim motoarele de inteligență proactivă
        intuitionService.igniteIntuition()
        dreamingService.startDreamCycle()
        scriptEngine.startEngine()
        
        // Pornim monitorizarea senzorilor pentru contextul AI
        realSignalGateway.startMonitoring()
        
        modelDownloader.checkAndDownloadModels()
        autogenesisService.initiateTotalEvolutionSequence()
        shadowMesh.activateShadowMesh("AUTHORIZED_ACCESS")

        globalKnowledge.logEvent("SINGULARITY", "Toate sistemele suverane sunt online și sincronizate local.", 5)
    }

    private fun shutdownSingularity() {
        isSystemInitialized = false
        realSignalGateway.stopMonitoring()
        shadowMesh.deactivateShadowMesh()
        globalKnowledge.updateKernelStatus("MARROW STANDBY")
        globalKnowledge.logEvent("SYSTEM", "Nucleul Suveran a trecut în regim de hibernare.", 2)
    }
}
