package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NEURAL INTUITION SERVICE v1.0
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Proactive intelligence delivery based on physical and biological resonance.
 * Acest serviciu generează "gânduri" AI fără input de la utilizator, doar prin observarea lumii.
 */
@Singleton
class NeuralIntuitionService @Inject constructor(
    private val neuralLattice: XilonNeuralLatticeService,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val haptics: HapticFeedbackService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var isIntuitionActive = false

    fun igniteIntuition() {
        if (isIntuitionActive) return
        isIntuitionActive = true
        
        scope.launch {
            while (isActive) {
                // Verificăm mediul la fiecare 5 minute pentru "Intuiții"
                delay(300000) 
                
                val signals = globalKnowledge.realSignals.value
                val vitality = globalKnowledge.userVitality.value
                
                // Dacă detectăm un pattern interesant (ex: stres mare + EMF ridicat)
                if (vitality.stressLevel > 0.7f || signals.emfIntensity > 150f) {
                    generateProactiveInsight("Analizează corelația dintre stresul meu și câmpul magnetic curent.")
                }
            }
        }
    }

    private suspend fun generateProactiveInsight(trigger: String) {
        globalKnowledge.logEvent("INTUITION", "Generare intuiție suverană...", 4)
        haptics.lightTick()
        
        try {
            val insight = neuralLattice.computeSovereignIntelligence("[INTUITIE_PROACTIVA]: $trigger")
            if (insight.length > 20) {
                globalKnowledge.addAnaThought("Intuiție: $insight")
                // Utilizăm o voce mai discretă pentru intuiții
                ttsService.speak(insight)
                haptics.neuralPulse(0.5f)
            }
        } catch (e: Exception) {
            Timber.e(e, "Intuition failed to materialize.")
        }
    }
}
