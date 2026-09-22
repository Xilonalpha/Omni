package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.CernDataRepository
import com.chemscanner.omniscient.marrow.repository.UniversalLaw
import com.chemscanner.omniscient.marrow.services.BlockchainNotaryService
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import kotlin.random.Random

data class Particle(
    val name: String, 
    val massGev: Double, 
    val rarity: Float, 
    val energyRequiredPev: Float, 
    val isStandardModel: Boolean = false,
    val isConfirmedByCern: Boolean = false // Real-world link
)

data class ColliderUiState(
    val collisionEnergyPev: Float = 0f,
    val luminosityPb: Double = 0.0,
    val isColliding: Boolean = false,
    val discoveredParticules: List<Particle> = emptyList(),
    val lastDiscovery: String? = null,
    val vacuumStability: Float = 1.0f,
    val neuralGain: Double = 0.0,
    val realCernStatus: String = "Connecting to Geneva...",
    val isLhcOperational: Boolean = false,
    val realLhcEnergyTev: Float = 0f,
    // True when realCernStatus/realLhcEnergyTev are the local fallback (CernDataRepository.getFallbackStatus),
    // not a live reading from CERN. The UI must show this clearly instead of implying a live uplink.
    val isCernDataSimulated: Boolean = false
)

@HiltViewModel
class QuantumColliderViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val cernRepository: CernDataRepository,
    private val notary: BlockchainNotaryService,
    private val ttsService: TextToSpeechService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ColliderUiState())
    val uiState: StateFlow<ColliderUiState> = _uiState.asStateFlow()

    private val sovereignParticlePool = mutableListOf(
        Particle("Dark Matter Fragment", 0.0, 0.005f, 50f, false),
        Particle("Magnetic Monopole", 10.0.pow(16.0), 0.0001f, 150f, false),
        Particle("Micro Black Hole", 1.0, 0.00001f, 250f, false),
        Particle("Graviton (Unbound)", 0.0, 0.000001f, 400f, false),
        Particle("Tachyon Pulse", -1.0, 0.0000001f, 500f, false),
        Particle("Strangelet", 1000.0, 0.001f, 100f, false)
    )

    init {
        syncWithCern()
        startVacuumStabilizationLoop()
        integrateStandardModel()
    }

    private fun integrateStandardModel() {
        val realParticles = cernRepository.getStandardModelParticles()
        realParticles.forEach { p ->
            sovereignParticlePool.add(Particle(
                name = p.name,
                massGev = p.massGev,
                rarity = 0.3f, 
                energyRequiredPev = (p.massGev / 1000.0).toFloat(), 
                isStandardModel = true
            ))
        }
    }

    private fun syncWithCern() {
        viewModelScope.launch {
            while (true) {
                val status = cernRepository.fetchLiveLhcStatus()
                _uiState.update { it.copy(
                    realCernStatus = status.statusMessage,
                    isLhcOperational = status.isOperational,
                    realLhcEnergyTev = status.beamEnergyTev,
                    isCernDataSimulated = status.isSimulated
                ) }
                delay(30000) 
            }
        }
    }

    private fun startVacuumStabilizationLoop() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                if (!_uiState.value.isColliding && _uiState.value.vacuumStability < 1.0f) {
                    _uiState.update { it.copy(vacuumStability = (it.vacuumStability + 0.01f).coerceAtMost(1.0f)) }
                }
            }
        }
    }

    fun triggerCollision() {
        if (_uiState.value.isColliding) return

        viewModelScope.launch {
            val resonance = globalKnowledge.symbioticResonance.value
            val activeLaws = globalKnowledge.activeUniversalLaws.value
            // Only treat the LHC as a genuine live source when it's operational AND the last
            // reading wasn't the simulated fallback - otherwise "confirmed by CERN" would be a lie.
            val lhcOnline = _uiState.value.isLhcOperational && !_uiState.value.isCernDataSimulated
            
            _uiState.update { it.copy(isColliding = true, lastDiscovery = "Synchronizing collision matrix...") }
            
            val statusVoice = if (lhcOnline) "LHC Online. Sincronizez fasciculul real de ${(_uiState.value.realLhcEnergyTev)} TeV." 
                              else "LHC Offline. Folosesc rezonanță neurală suverană."
            ttsService.speak(statusVoice)
            delay(2000)
            
            // ENERGY CALCULATION
            // If LHC is online, we start with its real energy (converted to PeV)
            val lhcBase = if (lhcOnline) _uiState.value.realLhcEnergyTev / 1000f else 0.013f
            // We add the Sovereign Energy (Resonance * Scaling)
            val sovereignEnergy = resonance * 500f 
            val finalEnergy = lhcBase + sovereignEnergy

            _uiState.update { it.copy(lastDiscovery = "Detecting decay products at ${"%.2f".format(finalEnergy)} PeV...") }
            delay(1500)

            val roll = Random.nextFloat()
            
            // DECISION ENGINE: Hybrid Detection
            val foundParticle = if (roll < (resonance * 0.85f)) {
                val candidates = sovereignParticlePool.filter { it.energyRequiredPev <= finalEnergy }
                
                // If LHC is online, prioritize Real Standard Model particles
                if (lhcOnline && Random.nextFloat() < 0.6f) {
                    val realCandidates = candidates.filter { it.isStandardModel }
                    if (realCandidates.isNotEmpty()) {
                        realCandidates.random().copy(isConfirmedByCern = true)
                    } else {
                        candidates.maxByOrNull { it.energyRequiredPev }
                    }
                } else {
                    // LHC Offline or Exotic Search: Pick based on rarity and energy
                    candidates.filter { it.rarity < (roll + (resonance * 0.1f)) }
                             .maxByOrNull { it.energyRequiredPev }
                }
            } else null

            if (foundParticle != null) {
                val prefix = if (foundParticle.isConfirmedByCern) "PARTICULĂ REALĂ DETECTATĂ: " else "ANOMALIE CUANTICĂ: "
                val msg = "$prefix ${foundParticle.name}"
                ttsService.speak(msg)
                
                val notarizationSource = if (foundParticle.isConfirmedByCern) "CERN_LHC_SYNC" else "SOVEREIGN_COLLIDER"
                notary.notarizeDiscovery(notarizationSource, "Architect Xilon discovered ${foundParticle.name} at ${finalEnergy} PeV.")
                
                val gain = (1.0 / (foundParticle.rarity + 0.0001)) * resonance * 5.0
                globalKnowledge.adjustNeuralEnergy(gain)
                
                _uiState.update { state ->
                    state.copy(
                        isColliding = false,
                        collisionEnergyPev = finalEnergy,
                        discoveredParticules = state.discoveredParticules + foundParticle,
                        lastDiscovery = if (foundParticle.isConfirmedByCern) "Confirmed by CERN: ${foundParticle.name}" else "Exotic Discovery: ${foundParticle.name}",
                        neuralGain = gain,
                        vacuumStability = (state.vacuumStability - (foundParticle.energyRequiredPev / 10000f)).coerceAtLeast(0.01f)
                    )
                }
            } else {
                ttsService.speak("Nicio particulă detectată în zgomotul de fond.")
                _uiState.update { it.copy(isColliding = false, lastDiscovery = "No discovery at ${"%.2f".format(finalEnergy)} PeV.") }
            }
        }
    }
}
