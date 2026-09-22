package com.chemscanner.omniscient.ui.viewmodels

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.data.dao.BlockchainDao
import com.chemscanner.omniscient.marrow.data.models.BlockchainEntry
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.UniversalLaw
import com.chemscanner.omniscient.marrow.services.UniversalCommanderService
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import com.chemscanner.omniscient.marrow.utils.CyberShield
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.*
import kotlin.random.Random

data class PlanetaryConnection(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val position: Offset = Offset.Zero,
    val signalStrength: Float = 1.0f
)

data class CrystalNode(
    val block: BlockchainEntry,
    var position: Offset = Offset.Zero,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val colorAlpha: Float = 0.8f
)

data class SentinelState(
    val complexity: Int = 6,
    val transparency: Float = 1.0f,
    val isGlitching: Boolean = false,
    val auraRadius: Float = 100f
)

data class MiningBit(
    val id: Long = Random.nextLong(),
    val startPos: Offset,
    val targetPos: Offset,
    val progress: Float = 0f,
    val alpha: Float = 1f
)

data class LedgerUiState(
    val crystals: List<CrystalNode> = emptyList(),
    val globalConnections: List<PlanetaryConnection> = emptyList(),
    val selectedBlock: BlockchainEntry? = null,
    val isLoading: Boolean = false,
    val realityCoherence: Float = 1.0f,
    val viewRotation: Float = 0f,
    val activeDirectives: Int = 0,
    val currentBpm: Int = 70,
    val sentinelState: SentinelState = SentinelState(),
    val isFirewallActive: Boolean = false,
    val isOmegaZeroTriggered: Boolean = false,
    val activeLaws: Set<UniversalLaw> = emptySet(),
    val miningBits: List<MiningBit> = emptyList(),
    val totalMinedEnergy: Double = 0.0
)

/**
 * NEURAL LEDGER VIEWMODEL v5.2 (AKASHA CORE RECOVERY).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Materialize the Neural Ledger (Akasha Archive).
 * v5.2: FIXED Crystal positioning and forced UI synchronization.
 */
@HiltViewModel
class OmniscientNeuralLedgerViewModel @Inject constructor(
    private val blockchainDao: BlockchainDao,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val cyberShield: CyberShield,
    private val commander: UniversalCommanderService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState: StateFlow<LedgerUiState> = _uiState.asStateFlow()
    
    private var miningJob: Job? = null

    init {
        loadLedgerCrystals()
        observeReality()
        observeGlobalGrid()
        observeXilonVitals()
        observeSecurityAndLaws()
        startNeuralMining()
    }

    /**
     * Încarcă și poziționează blocurile de date (Cristale) din Akasha Archive.
     * Reparat pentru a asigura vizibilitatea în centrul ecranului.
     */
    private fun loadLedgerCrystals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            blockchainDao.getFullLedger().collectLatest { blocks ->
                Timber.d("Akasha Archive: Found ${blocks.size} notarized blocks.")
                
                // Algoritm de distribuție centrat pentru vizibilitate garantată
                val nodes = blocks.mapIndexed { index, block ->
                    // Folosim un cerc restrâns pentru a fi siguri că sunt pe ecran
                    val angle = index * (2.0 * PI / blocks.size.coerceAtLeast(1))
                    val radius = if (blocks.size == 1) 0f else 200f + (index * 5f)
                    
                    // Centru aproximativ (presupunând ecrane standard)
                    val centerX = 400f 
                    val centerY = 600f
                    
                    val x = centerX + cos(angle).toFloat() * radius
                    val y = centerY + sin(angle).toFloat() * radius
                    
                    CrystalNode(
                        block = block,
                        position = Offset(x, y),
                        scale = 1.0f + (Random.nextFloat() * 0.2f),
                        rotation = (index * 45f) % 360f
                    )
                }
                _uiState.update { it.copy(crystals = nodes, isLoading = false) }
                
                if (nodes.isNotEmpty()) {
                    globalKnowledge.logEvent("AKASHA", "Neural Ledger Materialized: ${nodes.size} crystals visible.", 4)
                }
            }
        }
    }

    private fun startNeuralMining() {
        miningJob = viewModelScope.launch {
            while (true) {
                delay(3000)
                if (!_uiState.value.isOmegaZeroTriggered && _uiState.value.crystals.isNotEmpty()) {
                    val randomCrystal = _uiState.value.crystals.random()
                    val newBit = MiningBit(
                        startPos = randomCrystal.position,
                        targetPos = Offset(400f, 400f)
                    )
                    _uiState.update { it.copy(
                        miningBits = (it.miningBits + newBit).takeLast(10),
                        totalMinedEnergy = it.totalMinedEnergy + 0.1
                    ) }
                }
            }
        }
    }

    private fun observeGlobalGrid() {
        viewModelScope.launch {
            globalKnowledge.iotNodes.collectLatest { nodes ->
                val connections = nodes.values.map { node ->
                    PlanetaryConnection(
                        id = node.id,
                        name = node.name,
                        type = node.type,
                        status = node.status,
                        signalStrength = if (node.status == "ACTIVE") 1.0f else 0.2f
                    )
                }
                _uiState.update { it.copy(globalConnections = connections, activeDirectives = connections.count { it.status == "ACTIVE" }) }
            }
        }
    }

    private fun observeReality() {
        viewModelScope.launch {
            globalKnowledge.omegaState.collectLatest { omega ->
                _uiState.update { it.copy(realityCoherence = omega.realityIntegrity) }
            }
        }
    }

    private fun observeXilonVitals() {
        viewModelScope.launch {
            globalKnowledge.userVitality.collectLatest { vitals ->
                _uiState.update { it.copy(currentBpm = vitals.heartRate) }
            }
        }
    }

    private fun observeSecurityAndLaws() {
        viewModelScope.launch {
            globalKnowledge.activeUniversalLaws.collectLatest { laws ->
                _uiState.update { it.copy(activeLaws = laws) }
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isFirewallActive = cyberShield.isSystemSecure()) }
        }
    }

    fun triggerOmegaZero() {
        _uiState.update { it.copy(isOmegaZeroTriggered = true) }
        ttsService.speak("Protocol OMEGA-ZERO activat. Deconectare globală inițiată.")
        commander.emergencyKillAll()
        viewModelScope.launch {
            delay(5000)
            _uiState.update { it.copy(isOmegaZeroTriggered = false) }
        }
    }

    fun selectCrystal(node: CrystalNode) {
        _uiState.update { it.copy(selectedBlock = node.block) }
        ttsService.speak("Accesare pachet Akasha #${node.block.blockHash.take(6)}. Proveniență: ${node.block.module}.")
    }

    fun updateRotation(delta: Float) {
        _uiState.update { it.copy(viewRotation = it.viewRotation + delta) }
    }
}
