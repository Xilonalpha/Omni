package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MainRepository
import com.chemscanner.omniscient.marrow.repository.TemporalPhysicsRepository
import com.chemscanner.omniscient.marrow.services.GeminiService
import com.chemscanner.omniscient.marrow.services.LocalNeuralEngine
import com.chemscanner.omniscient.marrow.services.HapticFeedbackService
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

data class TemporalPhysicsUiState(
    val currentVelocityFraction: Double = 0.0,
    val relativisticState: TemporalPhysicsRepository.RelativisticState? = null,
    val twinParadoxResult: TemporalPhysicsRepository.TwinParadoxResult? = null,
    val puzzles: List<TemporalPhysicsRepository.TimeParadoxPuzzle> = emptyList(),
    val aiExplanation: String? = null,
    val isAiExplaining: Boolean = false,
    val maxVelocityPossible: Double = 0.9999,
    val scannedMass: Double = 1.0,
    val availableEnergy: Double = 0.0,
    val availableDarkMatter: Double = 0.0,
    val isTimeFoldActive: Boolean = false
)

@HiltViewModel
class TemporalPhysicsViewModel @Inject constructor(
    private val repository: TemporalPhysicsRepository,
    private val geminiService: GeminiService,
    private val localAi: LocalNeuralEngine,
    private val hapticService: HapticFeedbackService,
    private val ttsService: TextToSpeechService,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val mainRepository: MainRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemporalPhysicsUiState())
    val uiState: StateFlow<TemporalPhysicsUiState> = _uiState.asStateFlow()

    init {
        loadPuzzles()
        observeSystemState()
    }

    private fun loadPuzzles() {
        val paradoxPuzzles = repository.getPuzzles()
        _uiState.update { it.copy(puzzles = paradoxPuzzles) }
    }

    private fun observeSystemState() {
        viewModelScope.launch {
            globalKnowledge.neuralEnergy.collect { energy ->
                _uiState.update { it.copy(availableEnergy = energy) }
            }
        }
        viewModelScope.launch {
            globalKnowledge.darkMatter.collect { amount ->
                // FIXED: Explicit conversion from Float to Double to match UiState
                _uiState.update { it.copy(availableDarkMatter = amount.toDouble()) }
            }
        }
    }

    fun updateVelocity(fractionOfC: Double) {
        val lorentzState = repository.calculateLorentz(fractionOfC.coerceAtMost(0.9999))
        _uiState.update { it.copy(
            currentVelocityFraction = fractionOfC,
            relativisticState = lorentzState
        ) }
        if (fractionOfC > 0.8) hapticService.heavyImpact()
    }

    fun calculateTwinParadox(earthYears: Double) {
        val fraction = _uiState.value.currentVelocityFraction
        val result = repository.calculateTwinParadox(fraction.coerceAtMost(0.9999), earthYears)
        _uiState.update { it.copy(twinParadoxResult = result) }
        ttsService.speak("Calcul temporal finalizat.", "ro", true)
    }

    fun explainRelativityWithAi(objectDescription: String) {
        val state = _uiState.value.relativisticState ?: return
        val prompt = "Explică dilatarea timpului la ${state.velocityPercentage}% din viteza luminii."
        _uiState.update { it.copy(isAiExplaining = true) }
        viewModelScope.launch {
            val explanation = geminiService.generateContent(prompt)
            _uiState.update { it.copy(aiExplanation = explanation, isAiExplaining = false) }
            ttsService.speak(explanation, "ro", false)
        }
    }
}
