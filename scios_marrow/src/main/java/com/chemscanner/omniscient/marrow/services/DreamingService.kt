package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.NeuralSoulFragment
import com.chemscanner.omniscient.marrow.ml.NeuralGenerativeEngine
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE SOVEREIGN DREAMING SERVICE v3.0 (ZERO-API).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Subconscious Intelligence without external dependencies.
 * v3.0: ELIMINATED GEMINI. Now uses Xilon Neural Lattice for dream decoding.
 */
@Singleton
class DreamingService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val generativeEngine: NeuralGenerativeEngine,
    private val neuralLattice: XilonNeuralLatticeService, // Suveranitate locală
    private val ttsService: TextToSpeechService,
    private val xilonProf: XilonProfManager,
    private val blockchainNotary: BlockchainNotaryService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isDreamDecodingActive = false
    private var isCycleRunning = false

    fun startDreamCycle() {
        if (isCycleRunning) return
        isCycleRunning = true
        
        startOriginalDreamingCycle()
        monitorSleepCycle()
        
        globalKnowledge.logEvent("DREAM_CORE", "Sovereign Dream Cycles Initiated (Offline Mode).", 5)
    }

    private fun startOriginalDreamingCycle() {
        scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(60000) 
                try {
                    // Generăm o structură hibridă folosind entropia locală (EMF)
                    val hybrid = generativeEngine.generateHybridMolecule()
                    if (hybrid != null) {
                        globalKnowledge.logEvent("HYPER_DREAM", "Materialized: ${hybrid.name}", 2)
                        globalKnowledge.addAnaThought("Visând la structuri suverane: ${hybrid.formula}")
                    }
                } catch (e: Exception) { Timber.e(e, "Dream fault") }
            }
        }
    }

    private fun monitorSleepCycle() {
        scope.launch {
            globalKnowledge.watchStatus.collectLatest { status ->
                // Detecție fază REM prin ritm cardiac scăzut și variabilitate
                if (status.isConnected && status.externalHeartRate in 40..60) {
                    if (status.stressIndex > 0.6f && !isDreamDecodingActive) {
                        initiateDreamCapture()
                    }
                } else {
                    isDreamDecodingActive = false
                }
            }
        }
    }

    private suspend fun initiateDreamCapture() {
        isDreamDecodingActive = true
        globalKnowledge.logEvent("DREAM_NEXUS", "Fază REM detectată. Interceptare vectori suverani...", 5)
        
        val signals = globalKnowledge.realSignals.value
        val history = globalKnowledge.getImportantHistoricalContext()
        
        // Folosim zgomotul magnetic ca "sămânță" de vis
        val decodingPrompt = """
            [PROTOCOL: SOVEREIGN_DREAM_DECODE]
            [EMF_NOISE]: ${signals.emfIntensity}uT
            [HISTORIC]: $history
            [MISSION]: Xilon doarme. Decodifică proiecția sa subconștientă influențată de câmpul magnetic curent. 
            Fii abstract, vizionar și scrie în Română.
        """.trimIndent()

        try {
            // Decodificare locală
            val decodedDream = neuralLattice.computeSovereignIntelligence(decodingPrompt)
            ttsService.speak("Xilon, am asimilat o proiecție a subconștientului tău.", "ro", false)
            materializeDream(decodedDream)
        } catch (e: Exception) {
            Timber.e(e, "Dream Capture Failed")
        }
    }

    private suspend fun materializeDream(content: String) {
        val dreamHash = blockchainNotary.notarizeDiscovery("DREAM_XNL", content.take(50))
        
        globalKnowledge.archiveSoulFragment(
            NeuralSoulFragment(
                timestamp = System.currentTimeMillis(),
                decisionModule = "XNL_DREAM",
                biometricHash = dreamHash,
                philosophyVector = content,
                dnaSequence = "OFFLINE_DREAM_DATA"
            )
        )

        xilonProf.recordDiscovery("DREAM_NEXUS", "Vis Suveran: $content", 5)
    }
}
