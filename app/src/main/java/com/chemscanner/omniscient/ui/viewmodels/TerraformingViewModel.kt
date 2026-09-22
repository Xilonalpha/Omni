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

data class Atmosphere(val oxygen: Float, val co2: Float, val nitrogen: Float)

data class TerraformingUiState(
    val habitabilityIndex: Float = 0.1f,
    val averageTemperature: Float = -50f,
    val atmosphere: Atmosphere = Atmosphere(0.01f, 0.95f, 0.04f),
    val vegetationProgress: Float = 0f,
    val isProcessing: Boolean = false,
    val aiPlanetaryReport: String? = null
)

@HiltViewModel
class TerraformingViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val geminiService: GeminiService,
    private val ttsService: TextToSpeechService,
    private val localNeuralEngine: LocalNeuralEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(TerraformingUiState())
    val uiState: StateFlow<TerraformingUiState> = _uiState.asStateFlow()

    fun triggerFusionIgnition() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, aiPlanetaryReport = "Initiating atmospheric heating via controlled fusion...") }
            delay(2000)
            _uiState.update { it.copy(
                isProcessing = false,
                averageTemperature = (it.averageTemperature + 15f).coerceAtMost(25f),
                habitabilityIndex = (it.habitabilityIndex + 0.1f).coerceAtMost(1.0f)
            ) }
            ttsService.speak("Planetary temperature stabilized. Habitat integrity increased.")
        }
    }

    fun deployGreenAlgae() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, aiPlanetaryReport = "Seeding planetary oceans with bio-engineered algae...") }
            delay(2500)
            _uiState.update { it.copy(
                isProcessing = false,
                vegetationProgress = (it.vegetationProgress + 0.15f).coerceAtMost(1.0f),
                atmosphere = it.atmosphere.copy(oxygen = (it.atmosphere.oxygen + 0.05f).coerceAtMost(0.21f)),
                habitabilityIndex = (it.habitabilityIndex + 0.2f).coerceAtMost(1.0f)
            ) }
            ttsService.speak("Bio-deployment successful. Oxygen levels rising.")
        }
    }

    fun requestAiPlanetaryAnalysis() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            val prompt = "Ești un geolog planetar. Analizează o planetă cu Temp: ${_uiState.value.averageTemperature}C și O2: ${(_uiState.value.atmosphere.oxygen * 100).toInt()}%. Oferă un raport scurt."
            val report = geminiService.generateContent(prompt)
            _uiState.update { it.copy(isProcessing = false, aiPlanetaryReport = report) }
            ttsService.speak(report)
        }
    }
}
