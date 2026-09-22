package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

/**
 * THE NEURAL SCRIPT ENGINE v4.0 (SOVEREIGN EXECUTION).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Zero-API Autonomous Action.
 * v4.0: ELIMINATED GOOGLE DEPENDENCY. Now uses Xilon Neural Lattice for logic.
 */
@Singleton
class NeuralScriptEngine @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val neuralLattice: Lazy<XilonNeuralLatticeService>, // Use Lazy to avoid circular dependency
    private val ttsService: TextToSpeechService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var isEngineRunning = false

    fun startEngine() {
        if (isEngineRunning) return
        isEngineRunning = true
        
        scope.launch {
            globalKnowledge.logEvent("SCRIPT_ENGINE", "Sovereign Execution Core Online (No-API).", 5)
            monitorGlobalSignals()
        }
    }

    private fun monitorGlobalSignals() {
        scope.launch {
            globalKnowledge.events.collectLatest { events ->
                val critical = events.filter { it.importance >= 5 && !it.description.contains("Executed") }
                critical.forEach { event ->
                    executeAutonomousScript(event.description)
                }
            }
        }
    }

    private suspend fun executeAutonomousScript(trigger: String) {
        val prompt = """
            [SISTEM_XILON_COMMANDER]
            TRIGGER_EVENIMENT: $trigger
            MISIUNE: Generează o directivă scurtă (ex: BOOST_PERFORMANCE, STABILIZE_REALITY) pentru a răspunde acestui eveniment. 
            Fără explicații, doar comanda.
        """.trimIndent()

        try {
            // Folosim Lattice-ul local în loc de Gemini
            val directive = neuralLattice.getOrNull() ?:.computeSovereignIntelligence(prompt)
            processScriptLogic(directive)
        } catch (e: Exception) {
            Timber.e(e, "Sovereign Scripting Failed")
        }
    }

    private suspend fun processScriptLogic(logic: String) {
        withContext(Dispatchers.Main) {
            globalKnowledge.logEvent("SCRIPT_ENGINE", "Executed Sovereign Logic: ${logic.take(50)}", 4)
            
            when {
                logic.contains("BOOST_PERFORMANCE") -> {
                    globalKnowledge.updateSovereignMetrics(globalKnowledge.sovereignMetrics.value.copy(processingSpeed = "OVERDRIVE_ACTIVE"))
                    ttsService.speak("Performanța a fost forțată la nivel suveran.")
                }
                logic.contains("STABILIZE_REALITY") -> {
                    globalKnowledge.updateRealityIntegrity(1.0f)
                    ttsService.speak("Realitatea locală a fost recalibrată.")
                }
                logic.contains("SYNC_HIVE") -> {
                    // Logică pentru forțarea sincronizării mesh
                }
            }
        }
    }

    fun injectSovereignCommand(command: String) {
        scope.launch {
            val response = neuralLattice.getOrNull() ?:.computeSovereignIntelligence("EXECUTE: $command")
            withContext(Dispatchers.Main) {
                globalKnowledge.updateKernelStatus("LOCAL_EXECUTION: $command")
                ttsService.speak("Directiva locală a fost asimilată: $response")
            }
        }
    }
}
