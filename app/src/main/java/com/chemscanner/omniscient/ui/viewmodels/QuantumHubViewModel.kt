package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.BlockchainNotaryService
import com.chemscanner.omniscient.marrow.services.GeminiService
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Qubit(
    val id: Int,
    val state0: Double,
    val state1: Double,
    val isEntangled: Boolean = false,
    val entangledWith: Int? = null,
    val phaseAngle: Float = 0f,
    val label: String = "SUPERPOSITION"
)

data class QuantumUiState(
    val qubits: List<Qubit> = emptyList(),
    val isComputing: Boolean = false,
    val processorLoad: Float = 0.0f,
    val quantumAnalysis: String? = null,
    val multiversalAnalysis: String? = null,
    val activeDiscoveryLink: String = "NONE",
    val coherenceIntegrity: Float = 1.0f, // BRIDGE
    val environmentalNoise: Float = 0.0f
)

/**
 * QUANTUM HUB v24.1 (STABILIZED).
 * AUTHORITY: ARCHITECT XILON.
 */
@HiltViewModel
class QuantumHubViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val ttsService: TextToSpeechService,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val notary: BlockchainNotaryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuantumUiState())
    val uiState: StateFlow<QuantumUiState> = _uiState.asStateFlow()

    init {
        resetSystem()
        syncWithCosmicIntel()
    }

    private fun syncWithCosmicIntel() {
        viewModelScope.launch {
            globalKnowledge.cosmicAlertHistory.collectLatest { alerts ->
                if (alerts.isNotEmpty()) {
                    val latest = alerts.first()
                    _uiState.update { it.copy(
                        activeDiscoveryLink = "LINKED: ${latest.objectId}",
                        coherenceIntegrity = latest.confidence 
                    ) }
                    processQuantumInference(latest.objectId, latest.type)
                }
            }
        }
    }

    fun entangleQubits() {
        _uiState.update { state ->
            val updated = state.qubits.map { it.copy(isEntangled = true, entangledWith = if (it.id == 0) 1 else 0) }
            state.copy(qubits = updated)
        }
        ttsService.speak("Sincronizare cuantică stabilită.")
    }

    fun measureQubit(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isComputing = true) }
            val currentObject = _uiState.value.activeDiscoveryLink
            val seed = currentObject.hashCode()
            val result = if ((seed shr id) and 1 == 1) 1 else 0
            val analysis = geminiService.generateContent("Analyze quantum state |$result> for object $currentObject")
            
            _uiState.update { state ->
                val newQubits = state.qubits.map { 
                    if (it.id == id || (it.isEntangled && it.entangledWith == id)) {
                        it.copy(state0 = if (result == 0) 1.0 else 0.0, state1 = if (result == 1) 1.0 else 0.0, isEntangled = false, label = "COLLAPSED")
                    } else it
                }
                state.copy(isComputing = false, quantumAnalysis = analysis, qubits = newQubits)
            }
        }
    }

    private suspend fun processQuantumInference(targetId: String, type: String) {
        val prompt = "[QUANTUM_INFERENCE] Target: $targetId. Deterministic multiversal analysis."
        val result = geminiService.generateContent(prompt, "ANA - QUANTUM ARCHITECT")
        _uiState.update { it.copy(multiversalAnalysis = result) }
    }

    fun resetSystem() {
        _uiState.update { QuantumUiState(
            qubits = List(8) { Qubit(it, 0.5, 0.5, phaseAngle = it * 45f) }
        ) }
    }

    fun resetProcessor() = resetSystem()
}
