package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.data.models.FootballUiState
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SOVEREIGN PROACTIVE CROWD SERVICE v2.0 (XNL POWERED).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Neuro-Reactive Atmosphere without Cloud APIs.
 * v2.0: TOTAL INDEPENDENCE. ELIMINATED GEMINI. Crowd reacts to Biometric Stress.
 */
@Singleton
class ProactiveCrowdService @Inject constructor(
    private val neuralLattice: XilonNeuralLatticeService, // Nucleul Suveran
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val tts: TextToSpeechService,
    private val haptics: HapticFeedbackService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val _crowdState = MutableStateFlow(CrowdAtmosphere())
    val crowdState = _crowdState.asStateFlow()

    fun monitorMatch(uiState: FootballUiState) {
        scope.launch {
            val vitality = globalKnowledge.userVitality.value
            
            // 1. REACȚII LA SCOR
            if (uiState.scoreUser > _crowdState.value.lastUserScore) {
                onGoalScored("USER", uiState, vitality.stressLevel)
            } else if (uiState.scoreAi > _crowdState.value.lastAiScore) {
                onGoalScored("AI", uiState, vitality.stressLevel)
            }

            // 2. ADAPTARE ATMOSFERĂ (Bazată pe stres și poziția mingii)
            updateSovereignAtmosphere(uiState, vitality.stressLevel)
        }
    }

    private suspend fun onGoalScored(team: String, uiState: FootballUiState, stress: Float) {
        _crowdState.update { it.copy(
            lastUserScore = uiState.scoreUser,
            lastAiScore = uiState.scoreAi,
            excitementLevel = 1.0f
        ) }

        // Generăm o reacție de fan unică prin XNL, influențată de stresul utilizatorului
        val prompt = """
            [CROWD_REACTION]
            ECHIPA: ${if(team == "USER") uiState.userTeam?.name else uiState.opponentTeam?.name}
            STRES_UTILIZATOR: $stress
            MISIUNE: Ești un fan fanatic. Echipa ta a marcat. 
            Dacă stresul e mare, fii provocator. Dacă e mic, fii triumfător.
            Răspunde cu o scandare scurtă (max 8 cuvinte) în Română.
        """.trimIndent()

        val reaction = neuralLattice.computeSovereignIntelligence(prompt)
        
        withContext(Dispatchers.Main) {
            haptics.vibratePulse()
            tts.speak(reaction, "ro", true)
        }
    }

    private fun updateSovereignAtmosphere(uiState: FootballUiState, stress: Float) {
        val distToGoal = uiState.ball.position.y
        
        // Calculăm tensiunea mulțimii: distanța la poartă + stresul tău
        val tension = when {
            distToGoal < 200f -> (0.8f + stress * 0.2f).coerceIn(0f, 1f)
            else -> (0.3f + stress * 0.3f).coerceIn(0f, 1f)
        }

        if (tension > 0.9f) haptics.lightTick()

        _crowdState.update { it.copy(
            excitementLevel = tension,
            chant = if (tension > 0.8f) "PRESIUNE MAXIMĂ!" else "Hai echipa!"
        ) }
    }
}

data class CrowdAtmosphere(
    val excitementLevel: Float = 0.4f,
    val lastUserScore: Int = 0,
    val lastAiScore: Int = 0,
    val chant: String = "Sovereign Mesh Active"
)
