package com.chemscanner.omniscient.marrow.services

import androidx.compose.ui.geometry.Offset
import com.chemscanner.omniscient.marrow.data.models.FootballPlayerInstance
import com.chemscanner.omniscient.marrow.data.models.QuantumBall
import com.chemscanner.omniscient.marrow.data.models.FootballUiState
import com.chemscanner.omniscient.marrow.data.models.GameEvent
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * SOVEREIGN REFEREE SERVICE v2.1
 * REPARAT: Logica de Out, Corner și Offside echitabil.
 */
@Singleton
class FootballRefereeService @Inject constructor(
    private val tts: TextToSpeechService,
    private val haptics: HapticFeedbackService
) {
    
    private val PITCH_WIDTH = 1000f
    private val PITCH_HEIGHT = 1500f
    private val GOAL_WIDTH = 200f
    private val GOAL_LINE_DEPTH = 30f

    fun checkRules(state: FootballUiState, lastTouchTeam: String?): GameEvent? {
        val ballPos = state.ball.position
        
        // 1. Verificare GOL
        val goalEvent = checkGoal(ballPos)
        if (goalEvent != null) return goalEvent

        // 2. Verificare Ieșire din Teren
        val outEvent = checkBallOut(ballPos, lastTouchTeam)
        if (outEvent != null) return outEvent

        return null
    }

    private fun checkGoal(pos: Offset): GameEvent? {
        if (pos.y < GOAL_LINE_DEPTH && abs(pos.x - 500f) < (GOAL_WIDTH / 2)) {
            return GameEvent.GOAL_USER
        }
        if (pos.y > (PITCH_HEIGHT - GOAL_LINE_DEPTH) && abs(pos.x - 500f) < (GOAL_WIDTH / 2)) {
            return GameEvent.GOAL_AI
        }
        return null
    }

    private fun checkBallOut(pos: Offset, lastTouchTeam: String?): GameEvent? {
        // Out lateral
        if (pos.x < 0 || pos.x > PITCH_WIDTH) {
            return GameEvent.OUT_OF_BOUNDS
        }
        
        // Liniile de fund
        if (pos.y < 0) {
            if (abs(pos.x - 500f) >= (GOAL_WIDTH / 2)) {
                // Dacă mingea iese prin fundul AI (sus)
                return if (lastTouchTeam == "USER") GameEvent.OUT_OF_BOUNDS else GameEvent.CORNER
            }
        }
        
        if (pos.y > PITCH_HEIGHT) {
            if (abs(pos.x - 500f) >= (GOAL_WIDTH / 2)) {
                // Dacă mingea iese prin fundul USER (jos)
                return if (lastTouchTeam == "AI") GameEvent.OUT_OF_BOUNDS else GameEvent.CORNER
            }
        }

        return null
    }

    fun processCollision(p1: FootballPlayerInstance, p2: FootballPlayerInstance): GameEvent? {
        val dist = (p1.position - p2.position).getDistance()
        if (dist < 25f) {
            val isViolent = (p1.animationState == "SPRINT" || p2.animationState == "SPRINT")
            if (isViolent && Math.random() < 0.15) {
                tts.speak("Fault detectat!", "ro", true)
                haptics.heavyImpact()
                return GameEvent.FOUL
            }
        }
        return null
    }

    /**
     * REPARAT: Logica de Offside este acum validă pentru ambele echipe.
     */
    fun isOffside(attacker: FootballPlayerInstance, defenders: List<FootballPlayerInstance>, ball: QuantumBall): Boolean {
        val teamDefenders = defenders.filter { it.team != attacker.team }
        if (teamDefenders.size < 2) return false // Regula celor 2 apărători (portar + 1)

        return if (attacker.team == "USER") {
            val lastDefenderY = teamDefenders.map { it.position.y }.minOrNull() ?: 0f
            attacker.position.y < lastDefenderY && attacker.position.y < ball.position.y && attacker.position.y < 750f
        } else {
            val lastDefenderY = teamDefenders.map { it.position.y }.maxOrNull() ?: 1500f
            attacker.position.y > lastDefenderY && attacker.position.y > ball.position.y && attacker.position.y > 750f
        }
    }
}
