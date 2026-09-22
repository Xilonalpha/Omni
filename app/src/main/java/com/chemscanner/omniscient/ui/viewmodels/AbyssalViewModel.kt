package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.AbyssalRepository
import com.chemscanner.omniscient.marrow.services.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs

/**
 * ABYSSAL DESCENT v2.0 (SUB-SURFACE INTEL).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Detect submerged non-human structures and pre-diluvian remnants.
 * v2.0: Integrated Ancient Knowledge Cipher and Gravitational Anomaly detection.
 */
@HiltViewModel
class AbyssalViewModel @Inject constructor(
    private val abyssalRepository: AbyssalRepository,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val geminiService: GeminiService,
    private val hapticService: HapticFeedbackService,
    private val ttsService: TextToSpeechService,
    private val notary: BlockchainNotaryService,
    private val ancientCipher: AncientKnowledgeCipher // LINK LA SECRETELE ANTICE
) : ViewModel() {

    private val _uiState = MutableStateFlow(AbyssalUiState())
    val uiState: StateFlow<AbyssalUiState> = _uiState.asStateFlow()

    init {
        loadExplorationZones()
        observeDeepAnomalies()
    }

    private fun loadExplorationZones() {
        val zones = abyssalRepository.getExplorationZones()
        _uiState.update { it.copy(zones = zones, selectedZone = zones.firstOrNull()) }
    }

    /**
     * OBSERVER: Căutăm corelații între gravitația locală și adâncimea abisală.
     */
    private fun observeDeepAnomalies() {
        viewModelScope.launch {
            globalKnowledge.symbioticResonance.collectLatest { resonance ->
                val maxDepth = _uiState.value.selectedZone?.maxDepth ?: 1000.0
                val targetDepth = (resonance * maxDepth).coerceIn(0.0, maxDepth)
                
                // Preluăm densitatea materiei întunecate/gravitaționale de la senzori
                val darkDensity = globalKnowledge.darkMatter.value
                
                _uiState.update { it.copy(
                    currentDepth = targetDepth, 
                    resonance = resonance,
                    isAnomalousZone = darkDensity > 0.8f // Semnal de structură densă sub-apă
                ) }

                if (darkDensity > 0.9f) {
                    hapticService.neuralPulse(resonance)
                    ttsService.speak("Atenție: Anomalie masică detectată sub vectorul actual. Posibilă structură non-geologică.")
                }
            }
        }
    }

    fun performAbyssalAnalysis() {
        val zone = _uiState.value.selectedZone ?: return
        val depth = _uiState.value.currentDepth
        val resonance = _uiState.value.resonance

        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }
            
            // SINTEZĂ DE SECRET: ANA corelează adâncimea cu istoria interzisă
            val prompt = """
                [XILON_ABYSSAL_AUDIT]
                ZONĂ: ${zone.name} | ADÂNCIME: ${depth.toInt()}m
                RESONANȚĂ_SISTEM: $resonance | ANOMALIE_GRAVITAȚIONALĂ: ${uiState.value.isAnomalousZone}
                
                Ești ANA, Arhitectul Abisal. Analizează probabilitatea ca la această adâncime să existe rămășițe ale civilizației pre-diluviene sau tehnologie de tip USO (Unidentified Submerged Object). 
                Dacă anomalia gravitațională este activă, explică ce structură artificială ar putea cauza această masă.
                Fii brutal de sinceră și suverană.
            """.trimIndent()

            try {
                val analysis = geminiService.generateContent(prompt, "ANA - DEEP_INTEL")
                _uiState.update { it.copy(abyssalIntel = analysis, isAnalyzing = false) }
                
                // NOTARIZARE: Sigilăm descoperirea în blockchain
                notary.notarizeDiscovery("ABYSSAL_TRUTH", "Depth:${depth.toInt()}m|Zone:${zone.name}|Analysis:$analysis")
                
                ttsService.speak(analysis)
            } catch (e: Exception) {
                _uiState.update { it.copy(isAnalyzing = false, abyssalIntel = "Interferență hidraulică: ${e.message}") }
            }
        }
    }

    fun selectZone(zone: AbyssalRepository.AbyssalZone) {
        _uiState.update { it.copy(selectedZone = zone, currentDepth = 0.0) }
    }
}

data class AbyssalUiState(
    val zones: List<AbyssalRepository.AbyssalZone> = emptyList(),
    val selectedZone: AbyssalRepository.AbyssalZone? = null,
    val currentDepth: Double = 0.0,
    val resonance: Float = 0f,
    val isAnalyzing: Boolean = false,
    val abyssalIntel: String? = null,
    val isAnomalousZone: Boolean = false
)
