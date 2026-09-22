package com.chemscanner.omniscient.marrow.services

import androidx.compose.ui.geometry.Offset
import com.chemscanner.omniscient.marrow.data.models.FootballPlayerInstance
import com.chemscanner.omniscient.marrow.data.models.PlayerRole
import com.chemscanner.omniscient.marrow.data.models.QuantumBall
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*
import kotlin.random.Random

/**
 * SOVEREIGN FOOTBALL AI v6.1 (FIXED).
 * AUTHORITY: ARCHITECT XILON.
 * v6.1: Fixed missing delay import.
 */
@Singleton
class SovereignFootballAI @Inject constructor(
    private val neuralLattice: XilonNeuralLatticeService,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val haptics: HapticFeedbackService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var currentMasterDirective: String = "DEFENSIVE_STABILITY"

    private val PITCH_WIDTH = 1000f
    private val PITCH_HEIGHT = 1500f
    private val USER_GOAL = Offset(500f, 1500f)
    private val AI_GOAL = Offset(500f, 0f)

    init {
        startTacticalUpdateLoop()
    }

    private fun startTacticalUpdateLoop() {
        scope.launch {
            while (true) {
                delay(15000)
                val vitality = globalKnowledge.userVitality.value
                val prompt = "[TACTICAL_ANALYSIS] USER_STRESS: ${vitality.stressLevel}. Decide strategy."
                try {
                    val newDirective = neuralLattice.computeSovereignIntelligence(prompt)
                    if (newDirective.isNotBlank()) currentMasterDirective = newDirective
                } catch (e: Exception) { }
            }
        }
    }

    fun calculateNextMoves(
        aiPlayers: List<FootballPlayerInstance>,
        userPlayers: List<FootballPlayerInstance>,
        ball: QuantumBall
    ): List<FootballPlayerInstance> {
        val nearestAi = aiPlayers.minByOrNull { (it.position - ball.position).getDistance() }
        val nearestUser = userPlayers.minByOrNull { (it.position - ball.position).getDistance() }
        val aiDist = (nearestAi?.position?.minus(ball.position))?.getDistance() ?: 1000f
        val userDist = (nearestUser?.position?.minus(ball.position))?.getDistance() ?: 1000f
        val aiHasPossession = aiDist < 30f && aiDist < userDist
        
        return aiPlayers.map { player ->
            val isBallOwner = player.id == nearestAi?.id && aiHasPossession
            val targetPos = when {
                currentMasterDirective.contains("ATTACK") -> Offset(player.position.x, USER_GOAL.y - 100f)
                aiHasPossession && isBallOwner -> USER_GOAL
                else -> Offset(player.position.x, (ball.position.y - 200f).coerceAtLeast(100f))
            }
            val direction = (targetPos - player.position)
            val distance = direction.getDistance()
            val normalizedDir = if (distance > 2f) direction / distance else Offset.Zero
            player.copy(position = player.position + (normalizedDir * 3.0f))
        }
    }

    fun shouldShoot(player: FootballPlayerInstance, ball: QuantumBall): Boolean = (player.position - USER_GOAL).getDistance() < 350f
}
