package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository // FIXED IMPORT
import com.chemscanner.omniscient.marrow.repository.NeuroPhysRepository // FIXED IMPORT
import com.chemscanner.omniscient.marrow.services.HapticFeedbackService // FIXED IMPORT
import com.chemscanner.omniscient.marrow.services.NeuroSensorService // FIXED IMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NeuroPhysViewModel @Inject constructor(
    private val neuroPhysRepository: NeuroPhysRepository,
    private val neuroSensorService: NeuroSensorService,
    private val hapticService: HapticFeedbackService,
    private val globalKnowledge: GlobalKnowledgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NeuroPhysUiState())
    val uiState: StateFlow<NeuroPhysUiState> = _uiState.asStateFlow()

    init {
        loadScenarios()
        startSensorMonitoring()
    }

    private fun loadScenarios() {
        val scenarios = neuroPhysRepository.getScenarios()
        _uiState.update { it.copy(scenarios = scenarios, currentScenario = scenarios.firstOrNull()) }
    }

    private fun startSensorMonitoring() {
        viewModelScope.launch {
            neuroSensorService.getBrainWaveStream().collectLatest { waveState ->
                _uiState.update { it.copy(brainWaveState = waveState) }
                processPhysicsLogic(waveState)
                
                // Update Global Sci-OS Sync
                val syncFactor = (waveState.alpha * 0.7f + waveState.beta * 0.3f).coerceIn(0f, 1f)
                globalKnowledge.updateBioSync(syncFactor)
                
                // Haptic Feedback for Neuro-Sync
                if (waveState.alpha > 0.8f) {
                    hapticService.neuralPulse(waveState.alpha)
                }
            }
        }
    }

    private fun processPhysicsLogic(waveState: NeuroSensorService.BrainWaveState) {
        val scenario = _uiState.value.currentScenario ?: return
        
        when (scenario.id) {
            "telekinetic_ball" -> {
                val isFocused = waveState.alpha > 0.7f
                val targetX = if (isFocused) waveState.commandX else 0f
                val targetY = if (isFocused) waveState.commandY else 0f
                
                if (isFocused && !_uiState.value.isMentalControlActive) {
                    hapticService.lightTick() 
                }

                _uiState.update { it.copy(
                    objectPositionX = targetX,
                    objectPositionY = targetY,
                    isMentalControlActive = isFocused
                ) }
            }
            "electromagnetic_pulse" -> {
                if (waveState.alpha > 0.9f) {
                    _uiState.update { it.copy(mentalEnergyCharge = (it.mentalEnergyCharge + 0.015f).coerceIn(0f, 1f)) }
                    if (_uiState.value.mentalEnergyCharge >= 1.0f) {
                        hapticService.heavyImpact()
                        globalKnowledge.logEvent("NEURO_BCI", "Electromagnetic peak achieved via mental focus.", importance = 4)
                    }
                } else {
                    _uiState.update { it.copy(mentalEnergyCharge = (it.mentalEnergyCharge - 0.008f).coerceIn(0f, 1f)) }
                }
            }
            "neural_integration" -> {
                // Bridge to other modules
                if (waveState.beta > 0.8f) {
                    globalKnowledge.updateNeuralLoad((globalKnowledge.neuralLoad.value + 0.01f).coerceIn(0f, 1f))
                }
            }
        }
    }

    fun selectScenario(scenario: NeuroPhysRepository.NeuroScenario) {
        _uiState.update { it.copy(currentScenario = scenario, mentalEnergyCharge = 0f) }
        hapticService.lightTick()
        globalKnowledge.logEvent("NEURO_BCI", "Switched to scenario: ${scenario.title}", importance = 2)
    }
}

data class NeuroPhysUiState(
    val scenarios: List<NeuroPhysRepository.NeuroScenario> = emptyList(),
    val currentScenario: NeuroPhysRepository.NeuroScenario? = null,
    val brainWaveState: NeuroSensorService.BrainWaveState? = null,
    val objectPositionX: Float = 0f,
    val objectPositionY: Float = 0f,
    val isMentalControlActive: Boolean = false,
    val mentalEnergyCharge: Float = 0f
)
