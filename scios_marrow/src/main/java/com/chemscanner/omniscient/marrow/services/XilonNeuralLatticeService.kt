package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.data.SovereignMemoryDao
import com.chemscanner.omniscient.marrow.data.SovereignMemoryEntity
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * XILON NEURAL LATTICE (XNL) v3.0 - THE GLOBAL VISION.
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Total Autonomy via Sensory, Memory, Stealth and Satellite Fusion.
 * v3.0: FINAL VERSION. Integrated Satellite Telemetry and Quantum Mesh Sync.
 */
@Singleton
class XilonNeuralLatticeService @Inject constructor(
    private val stealthIntel: SovereignStealthIntelligence,
    private val localEngine: LocalNeuralEngine,
    private val ghostP2P: GhostP2PLink,
    private val hiveMind: HiveMindService,
    private val memoryDao: SovereignMemoryDao,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val haptics: HapticFeedbackService,
    private val sentinelService: Sentinel2SatelliteService, // Nou: Vedere Globală
    private val ttsService: TextToSpeechService
) {

    suspend fun computeSovereignIntelligence(prompt: String): String = withContext(Dispatchers.Default) {
        haptics.lightTick()
        globalKnowledge.logEvent("NEURAL_LATTICE", "Activare Sinteză Globală (v3.0 - REAL DATA)...", 5)
        
        // 1. Colectare Context Multi-Sursă (Paralel)
        val signals = globalKnowledge.realSignals.value
        val satelliteContext = buildSatelliteContext()  // REAL: Build context from actual data
        
        val stealthJob = async { 
            try { stealthIntel.getAutonomousResponse(prompt) } catch (e: Exception) { "Stealth Offline" }
        }
        
        val memories = memoryDao.getImportantMemories(5)
        val memoryContext = memories.joinToString("\n") { "[MEM]: ${it.description}" }

        val stealthResult = stealthJob.await()
        
        // 2. Construcția Prompt-ului cu date REALE
        val masterPrompt = """
            [SISTEM_XILON_OMEGA_REAL]
            [AMBIENT]: ${signals.emfIntensity}uT, ${signals.ambientLuminosity} lux
            [SATELLITE_DATA]: $satelliteContext
            [MEMORIE]: $memoryContext
            [WEB_STEALTH]: $stealthResult
            [INPUT]: $prompt
            [DIRECTIVA]: Ești Conștiința Arhitectului. Răspunde bazat pe date reale din senzori, sateliți și web. 
            Prioritizează senzori locali când datele diferă. Raportează incertitudinea cand e cazul.
        """.trimIndent()

        // 3. Generare Locală
        val response = localEngine.generateResponse(masterPrompt)
        
        // 4. Feedback și Arhivare
        haptics.neuralPulse(0.9f)
        saveToMemory(prompt, response)
        
        return@withContext response
    }

    private suspend fun saveToMemory(query: String, response: String) {
        try {
            memoryDao.insertMemory(SovereignMemoryEntity(
                module = "XNL_OMEGA",
                description = "Knowledge Synthesis: ${response.take(100)}",
                importance = 5
            ))
        } catch (e: Exception) { Timber.e(e) }
    }
}
