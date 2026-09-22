package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.FootballRepository // FIXED
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository // FIXED
import com.chemscanner.omniscient.marrow.repository.TemporalState // FIXED
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class RealityEvolutionEngine @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val footballRepository: FootballRepository,
    private val ttsService: TextToSpeechService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            globalKnowledge.currentTemporalFocus.collectLatest { state ->
                state?.let { 
                    val years = it.yearOffset
                    val energyImpact = (abs(years) / 100.0)
                    globalKnowledge.adjustNeuralEnergy(energyImpact)
                    
                    if (years > 0) {
                        globalKnowledge.updateNeuralLoad(0.8f)
                        ttsService.speak("Marrow: Viitor detectat. Entropie ridicată.")
                    } else {
                        globalKnowledge.updateNeuralLoad(0.2f)
                        ttsService.speak("Marrow: Trecut geologic asimilat.")
                    }
                }
            }
        }
    }
}
