package com.chemscanner.omniscient.ui.viewmodels

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.FootballRepository
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.data.models.*
import com.chemscanner.omniscient.marrow.services.*
import com.google.ar.core.Anchor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*
import kotlin.random.Random

/**
 * QUANTUM FOOTBALL VIEWMODEL v4.0 - FIFA LEVEL CONTROL.
 * REPARAT: Inerție Joystick, Schimbare Jucător Automată, Control Realist.
 */
@HiltViewModel
class QuantumFootballViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val footballRepository: FootballRepository,
    private val footballAI: SovereignFootballAI,
    private val referee: FootballRefereeService,
    private val tts: TextToSpeechService,
    private val haptics: HapticFeedbackService,
    private val proactiveCrowd: ProactiveCrowdService,
    private val competitionService: CompetitionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(FootballUiState())
    val uiState = _uiState.asStateFlow()

    private var gameLoopJob: Job? = null
    
    // Constante Fizice Rafinate
    private val GRAVITY = 0.18f
    private val AIR_RESISTANCE = 0.995f
    private val GROUND_FRICTION = 0.92f
    private val BOUNCE_RESTITUTION = 0.6f
    private val PLAYER_RADIUS = 25f
    private val BALL_RADIUS = 12f
    
    private val JOYSTICK_INERTIA = 0.15f // Inerție pentru mișcare lină

    private var lastTouchTeam: String? = null
    private var smoothedInput = Offset.Zero

    fun initMatch(userTeamId: String, opponentTeamId: String, stadiumId: String, mode: GameState = GameState.Playing) {
        viewModelScope.launch {
            val userTeam = footballRepository.getTeamById(userTeamId)
            val opponentTeam = footballRepository.getTeamById(opponentTeamId)
            val initialPlayers = generateInitialPlayers(userTeam, opponentTeam, mode == GameState.Training)

            _uiState.update { it.copy(
                userTeam = userTeam,
                opponentTeam = opponentTeam,
                players = initialPlayers,
                stadiumName = if (mode == GameState.Training) "Xilon Training Ground" else "Arena: $stadiumId",
                gameState = mode,
                matchTimeSeconds = 0,
                scoreUser = 0,
                scoreAi = 0,
                ball = QuantumBall(position = Offset(500f, 750f))
            ) }
            
            startGameLoop()
        }
    }

    private fun generateInitialPlayers(user: Team?, ai: Team?, training: Boolean): List<FootballPlayerInstance> {
        val players = mutableListOf<FootballPlayerInstance>()
        user?.players?.filter { it.isStartingXI }?.take(11)?.forEachIndexed { i, p ->
            players.add(FootballPlayerInstance(p.id, p.name, "USER", Offset(200f + (i % 3) * 200f, 800f + (i / 3) * 150f), isSelected = i == 0, speedStat = p.stats.speed, shootingStat = p.stats.shooting, role = p.role))
        }
        ai?.players?.filter { it.isStartingXI }?.take(if (training) 4 else 11)?.forEachIndexed { i, p ->
            players.add(FootballPlayerInstance(p.id, p.name, "AI", Offset(200f + (i % 3) * 200f, 100f + (i / 3) * 150f), speedStat = p.stats.speed, shootingStat = p.stats.shooting, role = p.role))
        }
        return players
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (_uiState.value.gameState == GameState.Playing) {
                updateEngineCycle()
                delay(16)
            }
        }
    }

    private fun updateEngineCycle() {
        _uiState.update { state ->
            // 1. INPUT SMOOTHING (Inerție tip FIFA)
            smoothedInput += (state.joystickOffset - smoothedInput) * JOYSTICK_INERTIA
            
            // 2. AUTO-SWITCH JUCĂTOR (Schimbă la cel mai apropiat de minge)
            val playersWithSwitch = autoSwitchPlayer(state)

            // 3. MIȘCARE JUCĂTORI
            val updatedPlayers = movePlayers(state.copy(players = playersWithSwitch), smoothedInput)
            
            // 4. ACTUALIZARE AI
            val aiPlayers = updatedPlayers.filter { it.team == "AI" }
            val userPlayers = updatedPlayers.filter { it.team == "USER" }
            val nextAiMoves = footballAI.calculateNextMoves(aiPlayers, userPlayers, state.ball)
            
            val finalPlayers = userPlayers + nextAiMoves

            // 5. FIZICĂ MINGE
            var updatedBall = applyBallPhysics(state.ball)
            
            // 6. COLIZIUNI & CROWD MONITORING
            val (ballAfterCollisions, touchTeam) = handleCollisions(updatedBall, finalPlayers)
            updatedBall = ballAfterCollisions
            if (touchTeam != null) {
                lastTouchTeam = touchTeam
                proactiveCrowd.monitorMatch(state.copy(ball = updatedBall, players = finalPlayers))
            }

            // 7. REGULI
            val event = referee.checkRules(state.copy(ball = updatedBall), lastTouchTeam)
            handleGameEvent(event)

            state.copy(
                players = finalPlayers,
                ball = updatedBall,
                matchTimeSeconds = state.matchTimeSeconds + if (Random.nextInt(100) > 98) 1 else 0 
            )
        }
    }

    private fun autoSwitchPlayer(state: FootballUiState): List<FootballPlayerInstance> {
        val userPlayers = state.players.filter { it.team == "USER" }
        val nearestToBall = userPlayers.minByOrNull { (it.position - state.ball.position).getDistance() }
        
        return state.players.map { p ->
            if (p.team == "USER") {
                p.copy(isSelected = p.id == nearestToBall?.id)
            } else p
        }
    }

    private fun movePlayers(state: FootballUiState, input: Offset): List<FootballPlayerInstance> {
        return state.players.map { p ->
            if (p.isSelected && p.team == "USER") {
                val staminaFactor = p.stamina.coerceIn(0.2f, 1.0f)
                val speed = if (state.isSprinting) 5.2f else 3.2f
                val moveDelta = input * (speed * (p.speedStat / 100f) * staminaFactor)
                
                val newPos = (p.position + moveDelta).let { 
                    Offset(it.x.coerceIn(0f, 1000f), it.y.coerceIn(0f, 1500f)) 
                }
                
                // Consum stamină doar la mișcare
                val newStamina = if (moveDelta.getDistance() > 0.5f) {
                    (p.stamina - (if (state.isSprinting) 0.003f else 0.0008f)).coerceAtLeast(0.1f)
                } else (p.stamina + 0.001f).coerceAtMost(1.0f) // Recuperare stamină pe loc
                
                p.copy(
                    position = newPos,
                    stamina = newStamina,
                    animationState = if (moveDelta.getDistance() > 0.1f) (if(state.isSprinting) "SPRINT" else "RUN") else "IDLE",
                    rotation = if (moveDelta.getDistance() > 0.1f) atan2(moveDelta.y, moveDelta.x) * (180/PI).toFloat() else p.rotation
                )
            } else p
        }
    }

    private fun handleCollisions(ball: QuantumBall, players: List<FootballPlayerInstance>): Pair<QuantumBall, String?> {
        var currentBall = ball
        var touchTeam: String? = null

        players.forEach { p ->
            val dist = (p.position - currentBall.position).getDistance()
            if (dist < (PLAYER_RADIUS + BALL_RADIUS) && currentBall.altitude < 40f) {
                touchTeam = p.team
                val impulse = (currentBall.position - p.position)
                val normalizedImpulse = impulse / impulse.getDistance().coerceAtLeast(1f)
                
                // Dribbling: mingea rămâne mai aproape de jucător la viteză mică
                val power = if (p.animationState == "SPRINT") 12f else 4f
                
                currentBall = currentBall.copy(
                    velocity = normalizedImpulse * power,
                    spin = Random.nextFloat() * 15f
                )
                if (p.isSelected) haptics.lightImpact()
            }
        }
        return Pair(currentBall, touchTeam)
    }

    private fun applyBallPhysics(ball: QuantumBall): QuantumBall {
        var newVel = ball.velocity * AIR_RESISTANCE
        var newVVel = ball.verticalVelocity - GRAVITY
        
        if (ball.altitude <= 0f) {
            newVel *= GROUND_FRICTION
            newVVel = if (abs(newVVel) > 0.5f) abs(newVVel) * BOUNCE_RESTITUTION else 0f
        }

        val newPos = ball.position + newVel
        val newAlt = (ball.altitude + newVVel).coerceAtLeast(0f)

        return ball.copy(
            position = Offset(newPos.x.coerceIn(-20f, 1020f), newPos.y.coerceIn(-20f, 1520f)),
            velocity = if (newVel.getDistance() < 0.05f) Offset.Zero else newVel,
            altitude = newAlt,
            verticalVelocity = newVVel
        )
    }

    private fun handleGameEvent(event: GameEvent?) {
        if (event == null) return
        viewModelScope.launch {
            when (event) {
                GameEvent.GOAL_USER -> {
                    _uiState.update { it.copy(scoreUser = it.scoreUser + 1) }
                    tts.speak("GOOOL! Xilon domină terenul!", "ro", true)
                    haptics.heavyImpact()
                    resetBall()
                }
                GameEvent.GOAL_AI -> {
                    _uiState.update { it.copy(scoreAi = it.scoreAi + 1) }
                    tts.speak("Gol pentru adversar.", "ro", true)
                    resetBall()
                }
                GameEvent.OUT_OF_BOUNDS, GameEvent.CORNER -> resetBall()
                else -> {}
            }
        }
    }

    private fun resetBall() {
        _uiState.update { it.copy(ball = QuantumBall(position = Offset(500f, 750f), velocity = Offset.Zero)) }
    }

    fun performPass() {
        _uiState.update { state ->
            val selected = state.players.find { it.isSelected } ?: return@update state
            val dir = Offset(cos(selected.rotation * PI/180).toFloat(), sin(selected.rotation * PI/180).toFloat())
            state.copy(ball = state.ball.copy(velocity = dir * 18f, altitude = 5f))
        }
    }

    fun performThroughBall() {
        _uiState.update { state ->
            val selected = state.players.find { it.isSelected } ?: return@update state
            val dir = Offset(cos(selected.rotation * PI/180).toFloat(), sin(selected.rotation * PI/180).toFloat())
            state.copy(ball = state.ball.copy(velocity = dir * 25f, verticalVelocity = 1.2f))
        }
    }

    fun releaseShoot() {
        _uiState.update { state ->
            val selected = state.players.find { it.isSelected } ?: return@update state
            val targetDir = (Offset(500f, 0f) - state.ball.position)
            val normalized = targetDir / targetDir.getDistance().coerceAtLeast(1f)
            state.copy(ball = state.ball.copy(velocity = normalized * 42f, verticalVelocity = 3.5f))
        }
    }

    fun onStadiumPlaced(anchor: Anchor?) { _uiState.update { it.copy(stadiumAnchor = anchor, isTrackingActive = true) } }
    fun onJoystickMove(offset: Offset) { _uiState.update { it.copy(joystickOffset = offset) } }
    fun setSprint(active: Boolean) { _uiState.update { it.copy(isSprinting = active) } }
    fun resetToIdle() { _uiState.update { FootballUiState() } }
}
