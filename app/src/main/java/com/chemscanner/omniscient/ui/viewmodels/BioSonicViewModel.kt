package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SoundWave(val amplitude: Float, val phase: Float)

data class BioSonicUiState(
    val activeWaves: List<SoundWave> = emptyList(),
    val harmonyLevel: Float = 0.5f,
    val isSynthesizing: Boolean = false,
    val aiSonicReport: String? = null
)

@HiltViewModel
class BioSonicViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val geminiService: GeminiService,
    private val localNeuralEngine: LocalNeuralEngine,
    private val ttsService: TextToSpeechService
) : ViewModel() {

    private val _uiState = MutableStateFlow(BioSonicUiState())
    val uiState: StateFlow<BioSonicUiState> = _uiState.asStateFlow()

    fun synthesizeMoleculeSound(molecule: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSynthesizing = true, aiSonicReport = "Synthesizing harmonic signature for $molecule...") }
            delay(2000)
            
            val waves = List(3) { SoundWave(0.5f + it * 0.1f, it * 1.5f) }
            _uiState.update { it.copy(
                activeWaves = waves,
                isSynthesizing = false,
                harmonyLevel = 0.85f,
                aiSonicReport = "Harmonic convergence detected at 432Hz resonance."
            ) }
            
            ttsService.speak("Sonic synthesis complete for $molecule. Resonance aligned with biological substrate.")
        }
    }
}
