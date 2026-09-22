package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.BlackHoleRepository
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.BlockchainNotaryService
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import com.chemscanner.omniscient.marrow.services.GeminiService
import com.chemscanner.omniscient.marrow.services.HapticFeedbackService
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BLACK HOLE DYNAMICS v5.0 (OMEGA SYNC).
 * RESTORED IDENTITY: ANA ISTLA.
 * PURIFIED: Integrated Akasha Notarization and XilonProf Journaling.
 */
@HiltViewModel
class BlackHoleViewModel @Inject constructor(
    private val repository: BlackHoleRepository,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val geminiService: GeminiService,
    private val hapticService: HapticFeedbackService,
    private val ttsService: TextToSpeechService,
    private val blockchainNotary: BlockchainNotaryService,
    private val xilonProf: XilonProfManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlackHoleUiState())
    val uiState: StateFlow<BlackHoleUiState> = _uiState.asStateFlow()

    init {
        observeBiometricEntanglement()
        calculateInitialMetrics(10.0) 
    }

    private fun calculateInitialMetrics(mass: Double) {
        val metrics = repository.calculateMetrics(mass)
        _uiState.update { it.copy(metrics = metrics, currentMass = mass) }
    }

    private fun observeBiometricEntanglement() {
        viewModelScope.launch {
            globalKnowledge.userVitality.collectLatest { vitals ->
                val metrics = _uiState.value.metrics ?: return@collectLatest
                val resonance = 1.0f - vitals.stressLevel
                val safeDistance = metrics.schwarzschildRadiusKm * 5.0
                val targetDistance = safeDistance * (1.0 - resonance.toDouble()).coerceIn(0.01, 1.0)
                
                val dilation = repository.calculateTimeDilation(_uiState.value.currentMass, targetDistance)
                
                _uiState.update { it.copy(
                    currentDistanceKm = targetDistance,
                    timeDilation = dilation.dilationFactor,
                    currentHeartRate = vitals.heartRate
                ) }

                if (dilation.dilationFactor < 0.2) {
                    hapticService.neuralPulse(0.9f)
                }
            }
        }
    }

    fun updateMass(newMass: Double) {
        val metrics = repository.calculateMetrics(newMass)
        _uiState.update { it.copy(metrics = metrics, currentMass = newMass) }
        ttsService.speak("Masă stelară ajustată la $newMass mase solare. Recalculez orizontul.")
    }

    fun requestAiDeepAnalysis() {
        val state = _uiState.value
        val metrics = state.metrics ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isAiAnalyzing = true) }
            
            val prompt = """
                Ești ANA ISTLA. Analiză din orizontul evenimentelor.
                DATE: Masă ${state.currentMass} Solar, Radius ${String.format("%.2f", metrics.schwarzschildRadiusKm)}km, 
                Dilatare Timp la distanța actuală: ${String.format("%.4f", state.timeDilation)}.
                SARCINĂ: Oferă un raport vizionar despre stabilitatea acestei singularități.
            """.trimIndent()

            try {
                val analysis = geminiService.generateContent(prompt)
                
                // OMEGA PURIFICATION: Double Anchoring
                val blockHash = blockchainNotary.notarizeDiscovery("SINGULARITY_ANALYSIS", "Mass:${state.currentMass}|Dilation:${state.timeDilation}")
                xilonProf.recordDiscovery("BLACK_HOLE_LAB", "Analysis: $analysis | Sealed: ${blockHash.take(8)}", 5)
                
                _uiState.update { it.copy(aiAnalysis = analysis, isAiAnalyzing = false) }
                ttsService.speak(analysis)
                
                globalKnowledge.logEvent("LAB", "Singularity Data sealed in Akasha.", 5)
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiAnalyzing = false, aiAnalysis = "Eroare de sinteză gravitațională.") }
            }
        }
    }
}

data class BlackHoleUiState(
    val metrics: BlackHoleRepository.BlackHoleMetrics? = null,
    val currentMass: Double = 10.0,
    val currentDistanceKm: Double = 0.0,
    val timeDilation: Double = 1.0,
    val currentHeartRate: Int = 0,
    val aiAnalysis: String? = null,
    val isAiAnalyzing: Boolean = false
)
