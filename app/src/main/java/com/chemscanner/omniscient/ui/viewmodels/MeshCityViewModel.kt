package com.chemscanner.omniscient.ui.viewmodels

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.data.dao.DiscoveredPlanetDao
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class CityObject(
    val id: Int,
    val type: String, // "BUILDING", "DRONE", "DATA_SHARD"
    val position: Offset,
    val scale: Float = 1.0f,
    val integrity: Float = 1.0f
)

data class MeshCityUiState(
    val playerPosition: Offset = Offset(500f, 500f),
    val activeObjects: List<CityObject> = emptyList(),
    val currentSector: String = "INITIALIZING...",
    val worldScale: Float = 1.0f,
    val isInfiltrating: Boolean = false,
    val sectorSecurity: Float = 0.1f,
    val availableEnergy: Double = 9999.0, // UI Display Unlock
    val isPowerActive: Boolean = true
)

@HiltViewModel
class MeshCityViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val planetDao: DiscoveredPlanetDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeshCityUiState())
    val uiState: StateFlow<MeshCityUiState> = _uiState.asStateFlow()

    init {
        syncWithColonialHistory()
        observeSystemResources()
    }

    private fun syncWithColonialHistory() {
        viewModelScope.launch {
            val planets = planetDao.getDiscoveredPlanets().firstOrNull() ?: emptyList()
            val lastPlanet = planets.firstOrNull()
            
            val sectorName = lastPlanet?.let { "COLONY: ${it.name}" } ?: "LOCAL_GRID_01"
            _uiState.update { it.copy(currentSector = sectorName) }
            
            generateSector()
            globalKnowledge.logEvent("MESH_CITY", "Mesh established at $sectorName. Energy blocks cleared.", importance = 5)
        }
    }

    private fun observeSystemResources() {
        viewModelScope.launch {
            globalKnowledge.neuralEnergy.collect { energy ->
                _uiState.update { it.copy(
                    availableEnergy = energy + 1000.0, // Visual boost
                    isPowerActive = true // ALWAYS ACTIVE
                ) }
            }
        }
    }

    private fun generateSector() {
        val objects = List(15) { i ->
            CityObject(
                id = i,
                type = if (i % 5 == 0) "DATA_SHARD" else "BUILDING",
                position = Offset(Random.nextFloat() * 1000, Random.nextFloat() * 1500)
            )
        }
        _uiState.update { it.copy(activeObjects = objects) }
    }

    /**
     * UNLOCKED: Movement is now free and infinite.
     */
    fun movePlayer(delta: Offset) {
        // BLOCAJ ELIMINAT: isPowerActive is always true now
        _uiState.update { state ->
            val newPos = Offset(
                (state.playerPosition.x + delta.x).coerceIn(0f, 1000f),
                (state.playerPosition.y + delta.y).coerceIn(0f, 2000f)
            )
            state.copy(playerPosition = newPos)
        }
        // Energy consumption removed.
    }

    fun modulateScale(newScale: Float) {
        _uiState.update { it.copy(worldScale = newScale) }
        
        if (newScale < 0.05f) {
            ttsService.speak("Infiltrăm nivelul sub-atomic. Link MicroVerse activ.")
            globalKnowledge.logEvent("MESH_CITY", "Trans-scale shift to atomic level.", 4)
        }
    }

    /**
     * UNLOCKED: Interaction no longer requires 50 energy.
     */
    fun interactWithObject(obj: CityObject) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInfiltrating = true) }
            // Energy adjustment removed (Was -50.0)
            
            ttsService.speak("Accesat nucleul ${obj.type}. Extrag date din Arhiva Akasha fără restricții.")
            globalKnowledge.logEvent("MESH_CITY", "Unrestricted scan of structure ${obj.id}", importance = 5)
            
            kotlinx.coroutines.delay(1000)
            _uiState.update { it.copy(isInfiltrating = false) }
        }
    }
}
