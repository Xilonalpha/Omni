package com.chemscanner.omniscient.ui.viewmodels

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository // FIXED IMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class FallingParticle(
    val id: Long,
    val x: Float,
    var y: Float,
    val type: ParticleType,
    val speed: Float
)

enum class ParticleType { STABLE, UNSTABLE, BONUS }

data class GameUiState(
    val particles: List<FallingParticle> = emptyList(),
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val multiplier: Float = 1.0f,
    val gameActive: Boolean = false
)

@HiltViewModel
class ParticleGameViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameJob: Job? = null
    private var particleIdCounter = 0L

    fun startGame() {
        _uiState.update { GameUiState(gameActive = true) }
        globalKnowledge.logEvent("GAME", "Atomic Stabilizer initiated.", importance = 2)
        
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            var lastSpawnTime = 0L
            while (_uiState.value.gameActive && !_uiState.value.isGameOver) {
                val currentTime = System.currentTimeMillis()
                
                // Spawn new particle
                if (currentTime - lastSpawnTime > (1000 / _uiState.value.multiplier).toLong()) {
                    spawnParticle()
                    lastSpawnTime = currentTime
                }

                // Update particle positions
                updateParticles()
                
                delay(16) // ~60 FPS
            }
        }
    }

    private fun spawnParticle() {
        val type = when (Random.nextInt(10)) {
            in 0..6 -> ParticleType.STABLE
            in 7..8 -> ParticleType.UNSTABLE
            else -> ParticleType.BONUS
        }
        
        val newParticle = FallingParticle(
            id = particleIdCounter++,
            x = Random.nextFloat(),
            y = -0.1f,
            type = type,
            speed = (0.005f + Random.nextFloat() * 0.01f) * _uiState.value.multiplier
        )
        
        _uiState.update { it.copy(particles = it.particles + newParticle) }
    }

    private fun updateParticles() {
        _uiState.update { state ->
            val updated = state.particles.map { it.copy(y = it.y + it.speed) }
            val (missed, remaining) = updated.partition { it.y > 1.1f }
            
            if (missed.any { it.type == ParticleType.STABLE }) {
                // Penalize for missing stable atoms
                val newScore = (state.score - 5).coerceAtLeast(0)
                if (newScore == 0 && state.score > 0) {
                    // Game over condition if you fail too much
                }
                state.copy(particles = remaining, score = newScore)
            } else {
                state.copy(particles = remaining)
            }
        }
    }

    fun onParticleClick(id: Long) {
        val particle = _uiState.value.particles.find { it.id == id } ?: return
        
        _uiState.update { state ->
            val newScore = when (particle.type) {
                ParticleType.STABLE -> state.score + 10
                ParticleType.UNSTABLE -> (state.score - 20).coerceAtLeast(0)
                ParticleType.BONUS -> state.score + 50
            }
            
            val newMultiplier = 1.0f + (newScore / 500f)
            
            state.copy(
                particles = state.particles.filter { it.id != id },
                score = newScore,
                multiplier = newMultiplier
            )
        }
        
        if (particle.type == ParticleType.UNSTABLE) {
            globalKnowledge.updateNeuralLoad(globalKnowledge.neuralLoad.value + 0.05f)
        } else {
            globalKnowledge.updateBioSync((globalKnowledge.bioSyncFactor.value + 0.01f).coerceAtMost(1.0f))
        }
    }

    fun stopGame() {
        _uiState.update { it.copy(gameActive = false) }
        gameJob?.cancel()
        globalKnowledge.updateHighScore(_uiState.value.score)
    }

    override fun onCleared() {
        super.onCleared()
        stopGame()
    }
}
