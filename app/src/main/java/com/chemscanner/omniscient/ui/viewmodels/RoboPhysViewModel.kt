package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.RoboPhysRepository // FIXED IMPORT
import com.chemscanner.omniscient.marrow.services.GeminiService // FIXED IMPORT
import com.chemscanner.omniscient.marrow.services.HapticFeedbackService // FIXED IMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoboPhysViewModel @Inject constructor(
    private val roboPhysRepository: RoboPhysRepository,
    private val geminiService: GeminiService,
    private val hapticService: HapticFeedbackService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoboPhysUiState())
    val uiState: StateFlow<RoboPhysUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val components = roboPhysRepository.getComponents()
        val challenges = roboPhysRepository.getChallenges()
        _uiState.update { it.copy(
            availableComponents = components,
            challenges = challenges,
            currentChallenge = challenges.firstOrNull()
        ) }
    }

    fun addComponentToRobot(component: RoboPhysRepository.RobotComponent) {
        val currentRobot = _uiState.value.robotDesign.toMutableList()
        currentRobot.add(component)
        
        // Haptic Feedback: Feel the mass of the component
        if (component.mass > 1.0f) {
            hapticService.heavyImpact()
        } else {
            hapticService.lightTick()
        }
        
        _uiState.update { it.copy(robotDesign = currentRobot) }
        calculateStability()
    }

    private fun calculateStability() {
        val design = _uiState.value.robotDesign
        if (design.isEmpty()) return

        val totalMass = design.sumOf { it.mass.toDouble() }.toFloat()
        val wheelCount = design.count { it.type == RoboPhysRepository.ComponentType.WHEEL }
        val isStable = totalMass < 5.0f || wheelCount >= 4

        // Haptic Feedback for instability
        if (!isStable && _uiState.value.isStabilityValid) {
            hapticService.heavyImpact()
        }

        _uiState.update { it.copy(
            totalRobotMass = totalMass,
            isStabilityValid = isStable,
            stabilityMessage = if (isStable) "Design is stable." else "Warning: High mass! Add more wheels for stability."
        ) }
    }

    fun optimizeDesignWithAi() {
        val designNames = _uiState.value.robotDesign.map { it.name }.joinToString(", ")
        val challenge = _uiState.value.currentChallenge?.title ?: "General Engineering"
        
        val prompt = """
            Act as an autonomous Physical AI engineer. I am designing a robot for the challenge: "$challenge".
            My current components are: $designNames.
            Analyze the kinetic efficiency, friction factors, and mass distribution. 
            Suggest one specific optimization to improve autonomy and performance.
            Keep the response technical but concise.
        """

        _uiState.update { it.copy(isAiOptimizing = true) }
        hapticService.lightTick() // Start signal

        viewModelScope.launch {
            try {
                val suggestion = geminiService.generateContent(prompt)
                _uiState.update { it.copy(aiOptimizationSuggestion = suggestion, isAiOptimizing = false) }
                hapticService.lightTick() // Success signal
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiOptimizing = false, aiOptimizationSuggestion = "AI analysis failed. Check connectivity.") }
            }
        }
    }

    fun selectChallenge(challenge: RoboPhysRepository.RoboChallenge) {
        _uiState.update { it.copy(currentChallenge = challenge) }
        hapticService.lightTick()
    }
    
    fun clearDesign() {
        _uiState.update { it.copy(robotDesign = emptyList(), totalRobotMass = 0f, isStabilityValid = true) }
        hapticService.lightTick()
    }
}

data class RoboPhysUiState(
    val availableComponents: List<RoboPhysRepository.RobotComponent> = emptyList(),
    val challenges: List<RoboPhysRepository.RoboChallenge> = emptyList(),
    val currentChallenge: RoboPhysRepository.RoboChallenge? = null,
    val robotDesign: List<RoboPhysRepository.RobotComponent> = emptyList(),
    val totalRobotMass: Float = 0f,
    val isStabilityValid: Boolean = true,
    val stabilityMessage: String = "",
    val isAiOptimizing: Boolean = false,
    val aiOptimizationSuggestion: String? = null
)
