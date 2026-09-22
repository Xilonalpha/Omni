package com.chemscanner.omniscient.marrow.data.models

import android.os.Parcelable
import androidx.compose.ui.geometry.Offset
import com.google.ar.core.Anchor
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * SOVEREIGN FOOTBALL CORE MODELS v2.0
 * Suportă Fizică 3D, Animații Procedurale și Inteligență Tactică.
 */

@Parcelize
sealed class GameState : Parcelable {
    object Idle : GameState()
    object Loading : GameState()
    object Playing : GameState()
    object Training : GameState()
    object Paused : GameState()
    object Finished : GameState()
}

@Parcelize
data class QuantumBall(
    val position: @RawValue Offset = Offset(500f, 750f),
    val velocity: @RawValue Offset = Offset.Zero,
    val altitude: Float = 0f,
    val verticalVelocity: Float = 0f,
    val spin: Float = 0f // Efectul Magnus (curbură)
) : Parcelable

@Parcelize
data class FootballPlayerInstance(
    val id: String,
    val name: String,
    val team: String,
    val position: @RawValue Offset,
    val altitude: Float = 0f,
    val rotation: Float = 0f, // Direcția în care privește jucătorul
    val isSelected: Boolean = false,
    val animationState: String = "IDLE", // IDLE, RUN, SPRINT, SHOOT, PASS
    val speedStat: Float = 70f,
    val shootingStat: Float = 70f,
    var stamina: Float = 1.0f,
    val role: PlayerRole = PlayerRole.STRIKER
) : Parcelable

@Parcelize
data class FootballUiState(
    val userTeam: Team? = null,
    val opponentTeam: Team? = null,
    val stadiumName: String = "Detecting Floor...",
    val scoreUser: Int = 0,
    val scoreAi: Int = 0,
    val matchTimeSeconds: Int = 0,
    val isSprinting: Boolean = false,
    val isChargingShoot: Boolean = false,
    val shootPower: Float = 0f,
    val ball: QuantumBall = QuantumBall(),
    val players: List<FootballPlayerInstance> = emptyList(),
    val joystickOffset: @RawValue Offset = Offset.Zero,
    val isTrackingActive: Boolean = false,
    val stadiumAnchor: @RawValue Anchor? = null,
    val fieldScale: Float = 0.08f, 
    val gameState: GameState = GameState.Idle
) : Parcelable

enum class GameEvent { GOAL_USER, GOAL_AI, OUT_OF_BOUNDS, CORNER, FOUL }
