package com.chemscanner.omniscient.ui.viewmodels

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import com.chemscanner.omniscient.marrow.services.BlockchainNotaryService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

data class VehicleState(
    val position: Offset = Offset(0f, 0f),
    val velocity: Float = 0f,
    val heading: Float = 0f,
    val acceleration: Float = 0f,
    val lorentzFactor: Float = 1.0f
)

data class HighVelocityUiState(
    val vehicle: VehicleState = VehicleState(),
    val isEngineActive: Boolean = false,
    val distanceTraveledLy: Double = 0.0,
    val trackAnomalies: Int = 0,
    val statusReport: String = "Sisteme în standby.",
    val inertialStress: Float = 0f,
    val neuralEnergyReserve: Double = 0.0
)

/**
 * QUANTUM VELOCITY v32.0 (HARD REALISM).
 * MISSION: Real-time Relativistic Physics linked to physical G-Force and Neural Energy.
 * AUTHORITY: ARCHITECT XILON.
 * v32.0: ACTIVATED REALISM PROTOCOL. Vibration affects acceleration. Velocity linked to light speed.
 */
@HiltViewModel
class HighVelocityLabViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val notary: BlockchainNotaryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HighVelocityUiState())
    val uiState: StateFlow<HighVelocityUiState> = _uiState.asStateFlow()

    private var physicsJob: kotlinx.coroutines.Job? = null
    
    // REALITATE ACTIVATĂ: Constantele fizice sunt acum legi imuabile
    private val C_REAL = 299792.458 // km/s

    init {
        observePhysicalConstraints()
    }

    private fun observePhysicalConstraints() {
        viewModelScope.launch {
            globalKnowledge.realSignals.collectLatest { signals ->
                val stress = signals.seismicIntensity.coerceIn(0f, 2f)
                _uiState.update { it.copy(inertialStress = stress) }
            }
        }
        viewModelScope.launch {
            globalKnowledge.neuralEnergy.collectLatest { energy ->
                _uiState.update { it.copy(neuralEnergyReserve = energy) }
            }
        }
    }

    fun toggleEngine() {
        val newState = !_uiState.value.isEngineActive
        _uiState.update { it.copy(isEngineActive = newState) }
        
        if (newState) {
            startPhysicsEngine()
            ttsService.speak("Motor Relativist activat. Fizica Hard este acum activă.")
        } else {
            physicsJob?.cancel()
            ttsService.speak("Propulsie oprită. Notarizez datele de zbor.")
            notarizeFlight()
        }
    }

    private fun startPhysicsEngine() {
        physicsJob = viewModelScope.launch {
            while (_uiState.value.isEngineActive) {
                delay(16)
                _uiState.update { state ->
                    val v = state.vehicle
                    
                    // PENALIZARE REALĂ: Vibrația telefonului (G-Force) reduce eficiența accelerației
                    // Dacă tremuri telefonul, nava nu poate accelera din cauza instabilității structurale
                    val efficiency = (1.0f - state.inertialStress * 0.4f).coerceAtLeast(0.1f)
                    
                    val newAcc = (v.acceleration + 0.005f * efficiency).coerceIn(0f, 5.0f)
                    val newVel = v.velocity + newAcc
                    
                    // CALCUL RELATIVIST REAL (Viteza este în km/s)
                    val beta = (newVel / C_REAL).coerceIn(0.0, 0.999999)
                    val gamma = (1.0 / sqrt(1.0 - beta * beta)).toFloat()

                    // Distanța în Ani Lumină parcursă (simulată pe timp scurs)
                    val newDist = state.distanceTraveledLy + (newVel / C_REAL) * 0.0001
                    
                    // CONSUM REAL DE ENERGIE: Scade energia neurală a sistemului
                    globalKnowledge.adjustNeuralEnergy(-0.001 * newAcc)

                    state.copy(
                        vehicle = v.copy(
                            velocity = newVel.toFloat(),
                            acceleration = newAcc,
                            lorentzFactor = gamma
                        ),
                        distanceTraveledLy = newDist,
                        statusReport = if (gamma > 2.0f) "Dilatare temporală masivă detectată!" else "Zbor stabil sub-luminic."
                    )
                }
            }
        }
    }

    private fun notarizeFlight() {
        viewModelScope.launch {
            val state = _uiState.value
            val summary = "Zbor finalizat la viteza de ${state.vehicle.velocity} km/s. Factor Lorentz: ${state.vehicle.lorentzFactor}"
            notary.notarizeDiscovery("VELOCITY_LOG", summary)
            globalKnowledge.logEvent("VELOCITY", summary, 4)
        }
    }

    fun applySteering(delta: Float) {
        _uiState.update { it.copy(vehicle = it.vehicle.copy(heading = it.vehicle.heading + delta)) }
    }
}
