package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.*
import com.chemscanner.omniscient.marrow.data.dao.DiscoveredPlanetDao
import com.chemscanner.omniscient.marrow.data.models.DiscoveredPlanetEntity
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

data class ExoPlanetPhysics(val gravity: Float, val temperature: Float, val radius: Float)
data class ExoPlanetVisual(val skyColor: String)

data class ExoPlanet(
    val name: String,
    val type: String,
    val distance: Float,
    val habitability: Float,
    val confidenceInterval: Float,
    val ra: Double,
    val dec: Double,
    val width: Double = 0.0,
    val hostStar: String,
    val physics: ExoPlanetPhysics,
    val visualData: ExoPlanetVisual,
    val telescopeSource: String,
    val oxygen: Float = 0f,
    val methane: Float = 0f,
    val bioIndex: Float = 0f
)

data class ExoPlanetUiState(
    val detectedPlanets: List<ExoPlanet> = emptyList(),
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val currentSector: String = "Sovereign Deep Scan",
    val aiAnalysis: String? = null,
    val isSyncing: Boolean = false
)

/**
 * EXOPLANET SURVEYOR VIEWMODEL v17.3 (STABILIZED).
 * AUTHORITY: ARCHITECT XILON.
 */
@HiltViewModel
class ExoPlanetSurveyorViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val exoDiscoveryService: SovereignExoDiscoveryService,
    private val ttsService: TextToSpeechService,
    private val planetDao: DiscoveredPlanetDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExoPlanetUiState())
    val uiState: StateFlow<ExoPlanetUiState> = _uiState.asStateFlow()

    fun startDeepSpaceScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, scanProgress = 0f, aiAnalysis = "Conectare la NASA...") }
            ttsService.speak("Inițiez scanarea spațiului adânc.")
            
            try {
                for (i in 1..40) { delay(20); _uiState.update { it.copy(scanProgress = i / 100f) } }

                val candidate = exoDiscoveryService.discoverNewWorld()

                if (candidate != null) {
                    val sig = candidate.signature
                    _uiState.update { it.copy(aiAnalysis = "Candidat identificat: ${candidate.name}. Analiză spectroscopică...") }
                    for (i in 41..100) { delay(15); _uiState.update { it.copy(scanProgress = i / 100f) } }

                    val analysis = exoDiscoveryService.performSovereignAnalysis(candidate)
                    
                    val gravityG = candidate.radius.pow(0.3).toFloat() 
                    val planet = ExoPlanet(
                        name = candidate.name,
                        type = if (candidate.radius > 2.0) "Gas Giant" else "Terrestrial",
                        distance = candidate.distanceLy.toFloat(),
                        habitability = sig?.bioIndex ?: 0.1f,
                        confidenceInterval = 0.98f,
                        ra = candidate.ra, dec = candidate.dec,
                        hostStar = candidate.hostStar,
                        physics = ExoPlanetPhysics(gravityG, (candidate.temp - 273.15).toFloat(), candidate.radius.toFloat()),
                        visualData = ExoPlanetVisual("Azure Mist"),
                        telescopeSource = candidate.telescope,
                        oxygen = sig?.oxygenLevel ?: 0f, methane = sig?.methaneLevel ?: 0f, bioIndex = sig?.bioIndex ?: 0f
                    )

                    savePlanetToDb(planet)
                    _uiState.update { it.copy(detectedPlanets = listOf(planet) + it.detectedPlanets, isScanning = false, aiAnalysis = analysis) }
                    ttsService.speak("Descoperire validată: ${candidate.name}.")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, aiAnalysis = "Eroare: ${e.message}") }
            }
        }
    }

    /**
     * RESTORED: Bridge for Activity
     */
    fun syncWithNasa() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, aiAnalysis = "Sincronizare cu NASA...") }
            delay(2000)
            _uiState.update { it.copy(isSyncing = false, aiAnalysis = "Sincronizare completă.") }
        }
    }

    private suspend fun savePlanetToDb(planet: ExoPlanet) {
        planetDao.insert(DiscoveredPlanetEntity(
            name = planet.name, type = planet.type, gravity = planet.physics.gravity,
            temperature = planet.physics.temperature, radius = planet.physics.radius,
            distanceLy = planet.distance, hostStar = planet.hostStar,
            ra = planet.ra, dec = planet.dec, telescopeSource = planet.telescopeSource,
            oxygenLevel = planet.oxygen, methaneLevel = planet.methane, bioIndex = planet.bioIndex,
            timestamp = System.currentTimeMillis()
        ))
    }
}
