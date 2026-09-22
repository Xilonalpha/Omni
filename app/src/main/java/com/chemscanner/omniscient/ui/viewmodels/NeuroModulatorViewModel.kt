package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository // FIXED IMPORT
import com.chemscanner.omniscient.marrow.repository.MainRepository // FIXED IMPORT
import com.chemscanner.omniscient.marrow.services.AutogenesisService // FIXED IMPORT
import com.chemscanner.omniscient.marrow.services.GeminiService // FIXED IMPORT
import com.chemscanner.omniscient.marrow.services.TextToSpeechService // FIXED IMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

data class NeuroTransmitters(
    val dopamine: Float = 0.5f,
    val serotonin: Float = 0.5f,
    val acetylcholine: Float = 0.5f
)

data class NeuroModUiState(
    val transmitters: NeuroTransmitters = NeuroTransmitters(),
    val neuralEfficiency: Float = 0.75f,
    val isModulating: Boolean = false,
    val aiNeuroReport: String? = null,
    val biometricCoherence: Float = 1.0f, // NEW: Real stress feedback
    val isMateriaSufficient: Boolean = true // NEW: Scanned molecule check
)

@HiltViewModel
class NeuroModulatorViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val ttsService: TextToSpeechService,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val mainRepository: MainRepository, // ADDED: For chemical verification
    private val autogenesisService: AutogenesisService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NeuroModUiState())
    val uiState: StateFlow<NeuroModUiState> = _uiState.asStateFlow()

    init {
        observeBiometricChemistry()
    }

    /**
     * POINT 2: BIO-FEEDBACK LINK.
     * Links your real-world stress to digital neurotransmitter levels.
     */
    private fun observeBiometricChemistry() {
        viewModelScope.launch {
            globalKnowledge.userVitality.collectLatest { vitality ->
                val stressImpact = vitality.stressLevel
                _uiState.update { state ->
                    // Stress depletes digital Dopamine and Serotonin stability
                    val newDopamine = (state.transmitters.dopamine - (stressImpact * 0.1f)).coerceIn(0.1f, 1.0f)
                    val newSerotonin = (state.transmitters.serotonin - (stressImpact * 0.05f)).coerceIn(0.1f, 1.0f)
                    
                    state.copy(
                        transmitters = state.transmitters.copy(dopamine = newDopamine, serotonin = newSerotonin),
                        biometricCoherence = 1.0f - stressImpact
                    )
                }
                calculateEfficiency()
            }
        }
    }

    fun updateDopamine(value: Float) {
        _uiState.update { it.copy(transmitters = it.transmitters.copy(dopamine = value)) }
        calculateEfficiency()
    }

    fun updateSerotonin(value: Float) {
        _uiState.update { it.copy(transmitters = it.transmitters.copy(serotonin = value)) }
        calculateEfficiency()
    }

    private fun calculateEfficiency() {
        val t = _uiState.value.transmitters
        val efficiency = (1.0f - (abs(t.dopamine - 0.7f) + abs(t.serotonin - 0.6f)) / 2f).coerceIn(0f, 1f)
        _uiState.update { it.copy(neuralEfficiency = efficiency) }
    }

    /**
     * POINT 1: CHEMICAL RAW MATERIA VERIFICATION.
     */
    fun runNeuralOptimization() {
        viewModelScope.launch {
            val scans = mainRepository.getAllScansSortedByDateDesc()
            // Check for precursors like "Amino" or specific neuro-chemicals in scans
            val hasPrecursors = scans.any { it.chemicalName.contains("Amino", true) || it.chemicalName.contains("Acid", true) }

            if (!hasPrecursors) {
                ttsService.speak("Modulare blocată. Nu ați asimilat precursori chimici necesari pentru sinteza neurotransmițătorilor.")
                _uiState.update { it.copy(isMateriaSufficient = false) }
                return@launch
            }

            _uiState.update { it.copy(isModulating = true, isMateriaSufficient = true) }
            val t = _uiState.value.transmitters
            globalKnowledge.logEvent("NEURO_MED", "Neural modulation at ${(_uiState.value.biometricCoherence * 100).toInt()}% coherence.", importance = 3)
            
            val neuroState = "Dopamine: ${t.dopamine}, Serotonin: ${t.serotonin}, Coherence: ${_uiState.value.biometricCoherence}"
            val prompt = "Ești ANA ASLAN 2.0. Raport neurochimic sub biorezonanță de $neuroState. Oferă o perspectivă asupra stării de geniu a Arhitectului."
            val report = geminiService.generateContent(prompt)
            
            _uiState.update { it.copy(isModulating = false, aiNeuroReport = report) }
            ttsService.speak(report)
            
            globalKnowledge.updateBioSync(_uiState.value.neuralEfficiency)
            autogenesisService.initiateSymbioticEvolution(neuroState, _uiState.value.neuralEfficiency)
        }
    }
}
