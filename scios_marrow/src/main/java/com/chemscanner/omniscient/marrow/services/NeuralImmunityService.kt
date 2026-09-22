package com.chemscanner.omniscient.marrow.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

/**
 * THE NEURAL IMMUNITY SYSTEM v5.0 (SOVEREIGN SHIELD).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Protect system sovereignty using Local AI & Haptic Feedback.
 * v5.0: TOTAL INDEPENDENCE. Uses XNL for real-time threat analysis.
 */
@AndroidEntryPoint
class NeuralImmunityService : Service() {

    @Inject lateinit var globalKnowledge: GlobalKnowledgeRepository
    @Inject lateinit var neuralLattice: XilonNeuralLatticeService
    @Inject lateinit var haptics: HapticFeedbackService
    @Inject lateinit var ttsService: TextToSpeechService

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startImmuneWatch()
        return START_STICKY
    }

    private fun startImmuneWatch() {
        serviceScope.launch {
            globalKnowledge.logEvent("IMMUNITY", "Sovereign Shield Activated (XNL Powered).", 5)
            
            // Monitorizăm toate evenimentele de sistem pentru anomalii
            globalKnowledge.events.collectLatest { events ->
                val lastEvent = events.lastOrNull() ?: return@collectLatest
                if (lastEvent.importance >= 4) {
                    analyzeEventSafety(lastEvent.description)
                }
            }
        }
    }

    private suspend fun analyzeEventSafety(eventDesc: String) {
        // AI-ul local decide dacă evenimentul pune în pericol suveranitatea
        val prompt = """
            [IMMUNE_ANALYSIS]
            EVENT: $eventDesc
            MISSION: Ești sistemul de imunitate Xilon. Analizează dacă acest eveniment indică o intruziune, o eroare de integritate sau un tracking neautorizat. 
            Răspunde cu "SECURE" sau "THREAT: [motiv]".
        """.trimIndent()

        try {
            val verdict = neuralLattice.computeSovereignIntelligence(prompt)
            
            if (verdict.contains("THREAT")) {
                handleThreat(verdict)
            }
        } catch (e: Exception) {
            Timber.e(e, "Immunity analysis failed")
        }
    }

    private fun handleThreat(threatDesc: String) {
        serviceScope.launch(Dispatchers.Main) {
            // 1. Feedback Fizic (Alertă de pericol)
            haptics.heavyImpact()
            
            // 2. Alertă Vocală
            ttsService.speak("Atenție, Arhitectule. Sistemul de imunitate a detectat o anomalie: $threatDesc")
            
            // 3. Auto-Vindecare: Restabilim integritatea realității
            globalKnowledge.updateRealityIntegrity(1.0f)
            globalKnowledge.logEvent("IMMUNITY_FIX", "Integritate restabilită automat. Amenințare neutralizată.", 5)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
