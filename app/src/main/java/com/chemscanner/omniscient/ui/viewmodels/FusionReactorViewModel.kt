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
import kotlinx.coroutines.isActive
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.*

data class FusionUiState(
    val reactorStatus: String = "IDLE",
    val plasmaTemperature: Double = 0.0,
    val magneticStability: Float = 1.0f,
    val isReactorActive: Boolean = false,
    val targetMagneticField: Float = 0.5f,
    val qFactor: Double = 0.0,
    val solarSyncBonus: Double = 0.0,
    val aiDiagnostic: String? = null
)

/**
 * FUSION REACTOR ENGINE v20.2 (ZERO RANDOM).
 * AUTHORITY: ARCHITECT XILON.
 * v20.2: ELIMINATED ALL RANDOM GENERATORS. Fluctuations linked to Starlink Mesh Jitter.
 */
@HiltViewModel
class FusionReactorViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val geminiService: GeminiService,
    private val omniAiService: OmniAiService,
    private val ttsService: TextToSpeechService,
    private val notary: BlockchainNotaryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(FusionUiState())
    val uiState: StateFlow<FusionUiState> = _uiState.asStateFlow()

    init {
        startHeliosSync()
        startPlasmaDynamics()
    }

    private fun startHeliosSync() {
        viewModelScope.launch {
            while (isActive) {
                try {
                    val solarIntel = omniAiService.generateSupremeInsight(
                        "Current Solar Activity (X-ray flux, Sunspot number) for today 2026. Output ONLY: Flux:X, Sunspots:Y",
                        OmniAiService.AiModel.TAVILY_SEARCH
                    )
                    val flux = solarIntel.substringAfter("Flux:").substringBefore(",").trim().toDoubleOrNull() ?: 1.0
                    val bonus = (flux / 10.0).coerceIn(0.0, 5.0)
                    _uiState.update { it.copy(solarSyncBonus = bonus) }
                } catch (e: Exception) {
                    Timber.e("Helios Sync Failed")
                }
                delay(300000)
            }
        }
    }

    private fun startPlasmaDynamics() {
        viewModelScope.launch {
            while (isActive) {
                if (_uiState.value.isReactorActive && _uiState.value.reactorStatus == "STABLE") {
                    val stability = _uiState.value.magneticStability
                    val baseQ = 0.85 + (stability * 0.5)
                    val currentQ = baseQ + (_uiState.value.solarSyncBonus / 100.0)
                    
                    // DETERMINISM: Fluctuația temperaturii este legată de latența rețelei (Mesh Jitter)
                    val latency = globalKnowledge.starlinkMesh.value.networkLatencyMs
                    val jitterFactor = (latency % 10) / 5.0 // Valoare între 0 și 2 bazată pe realitate
                    val temp = 150.0 * stability + jitterFactor
                    
                    _uiState.update { it.copy(
                        plasmaTemperature = temp,
                        qFactor = currentQ
                    )}
                }
                delay(500)
            }
        }
    }

    fun updateMagneticField(value: Float) {
        _uiState.update { it.copy(targetMagneticField = value) }
    }

    fun toggleReactor() {
        viewModelScope.launch {
            val nextState = !_uiState.value.isReactorActive
            if (nextState) {
                _uiState.update { it.copy(reactorStatus = "IGNITING", isReactorActive = true) }
                ttsService.speak("Inițiez aprinderea termonucleară.")
                delay(3000)
                _uiState.update { it.copy(reactorStatus = "STABLE") }
                notary.notarizeDiscovery("FUSION_IGNITION", "Reactor Stable")
            } else {
                _uiState.update { it.copy(isReactorActive = false, reactorStatus = "COOLING", qFactor = 0.0) }
                ttsService.speak("Reactor în siguranță.")
            }
        }
    }

    fun requestAiDiagnostic() {
        performQuantumDiagnostic()
    }

    fun performQuantumDiagnostic() {
        viewModelScope.launch {
            _uiState.update { it.copy(aiDiagnostic = "Analizez instabilitățile magnetice...") }
            val state = _uiState.value
            val prompt = "Fusion Analysis: Temp ${state.plasmaTemperature}, Q ${state.qFactor}, Stability ${state.magneticStability}"
            val analysis = omniAiService.generateSupremeInsight(prompt, OmniAiService.AiModel.NVIDIA_OVERDRIVE)
            _uiState.update { it.copy(aiDiagnostic = analysis) }
            ttsService.speak(analysis)
        }
    }
}
