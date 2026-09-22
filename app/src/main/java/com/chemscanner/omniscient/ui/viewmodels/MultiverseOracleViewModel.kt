package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimelineBranch(val id: Int, val description: String, val probability: Float, val color: Long)

data class MultiverseOracleUiState(
    val userDecision: String = "",
    val activeTimelines: List<TimelineBranch> = emptyList(),
    val quantumCoherence: Float = 1.0f,
    val isCalculating: Boolean = false,
    val aiOracleReport: String? = null,
    val statusMessage: String = "Aștept input decizional..."
)

/**
 * MULTIVERSE ORACLE v29.0 (QUANTUM DECISION ENGINE).
 * MISSION: Use real-time cosmic and biometric data to predict decision outcomes.
 * AUTHORITY: ARCHITECT XILON.
 * v29.0: ELIMINATED STATIC BRANCHES. Using ANA for dynamic probability synthesis.
 */
@HiltViewModel
class MultiverseOracleViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val geminiService: GeminiService,
    private val ttsService: TextToSpeechService,
    private val notary: BlockchainNotaryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MultiverseOracleUiState())
    val uiState: StateFlow<MultiverseOracleUiState> = _uiState.asStateFlow()

    fun onDecisionInput(input: String) {
        _uiState.update { it.copy(userDecision = input) }
    }

    /**
     * SCANARE MULTIVERS: Calculează viitorul probabil bazat pe datele reale ale sistemului.
     */
    fun executeMultiverseScan() {
        val decision = _uiState.value.userDecision
        if (decision.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCalculating = true, statusMessage = "Analizez ramurile de probabilitate în raport cu contextul galactic...") }
            ttsService.speak("Deschid poarta oracolului. Analizez implicațiile deciziei dumneavoastră în multivers.")

            try {
                // DATE REALE PENTRU CONTEXT
                val mesh = globalKnowledge.starlinkMesh.value
                val sunBonus = globalKnowledge.galacticData.value.cosmicRayIntensity
                val latestAnomaly = globalKnowledge.cosmicAlertHistory.value.firstOrNull()?.objectId ?: "Echilibru Galactic"

                val prompt = """
                    [MULTIVERSE_ORACLE_REQUEST]
                    DECIZIE_UTILIZATOR: $decision
                    CONTEXT_SISTEM: Starlink_Nodes(${mesh.activeNodes}), Solar_Intensity($sunBonus), Ultima_Anomalie($latestAnomaly)
                    
                    Ești ANA, Oracolul Multiversului. Calculează 3 ramuri de timp posibile rezultate din această decizie.
                    Pentru fiecare ramură oferă:
                    1. O descriere scurtă și monumentală.
                    2. O probabilitate (0.0 - 1.0) bazată pe contextul sistemului.
                    3. Un cod de culoare (HEX).
                    Format: DESCRIERE|PROBABILITATE|HEX
                    Răspunde în română, tehnic și vizionar.
                """.trimIndent()

                val analysis = geminiService.generateContent(prompt, "ANA - MULTIVERSE ORACLE")
                
                // Parsăm ramurile dinamice generate de AI
                val branches = analysis.lines().filter { it.contains("|") }.mapIndexed { index, line ->
                    val parts = line.split("|")
                    TimelineBranch(
                        id = index,
                        description = parts[0].trim(),
                        probability = parts[1].trim().toFloatOrNull() ?: 0.33f,
                        color = android.graphics.Color.parseColor(parts[2].trim()).toLong() and 0xFFFFFFFFL
                    )
                }

                _uiState.update { it.copy(
                    activeTimelines = branches,
                    isCalculating = false,
                    aiOracleReport = analysis,
                    quantumCoherence = mesh.signalCoherence,
                    statusMessage = "Sinteză multiversală completă."
                ) }

                notary.notarizeDiscovery("MULTIVERSE_DECISION", "Decision: $decision | Coherence: ${mesh.signalCoherence}")
                ttsService.speak("Scanare multiversală completă. Probabilitățile au fost recalculate în raport cu realitatea actuală.")
                ttsService.speak(analysis)

            } catch (e: Exception) {
                _uiState.update { it.copy(isCalculating = false, statusMessage = "Eroare la colapsul ramurilor de timp.") }
                ttsService.speak("Interferență în matricea decizională.")
            }
        }
    }
}
