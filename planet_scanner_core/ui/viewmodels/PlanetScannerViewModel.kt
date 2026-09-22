package com.planetscanner.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planetscanner.app.data.models.Planet
import com.planetscanner.app.data.models.GeologicalLayer
import com.planetscanner.app.data.models.ExoCreature
import com.planetscanner.app.data.repository.PlanetRepository
import com.chemscanner.omniscient.data.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.services.GeminiVisionService
import com.chemscanner.omniscient.services.LocalNeuralEngine
import com.chemscanner.omniscient.services.TextToSpeechService
import com.chemscanner.omniscient.services.TransScaleBridge
import com.chemscanner.omniscient.services.RealityPulse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random

@HiltViewModel
class PlanetScannerViewModel @Inject constructor(
    private val planetRepository: PlanetRepository,
    private val geminiVisionService: GeminiVisionService,
    private val transScaleBridge: TransScaleBridge,
    private val globalKnowledge: GlobalKnowledgeRepository, // ADDED: Reality link
    private val localAi: LocalNeuralEngine, // ADDED: Offline processing
    private val ttsService: TextToSpeechService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanetScannerUiState())
    val uiState: StateFlow<PlanetScannerUiState> = _uiState.asStateFlow()

    private var lastEmfValue = 45f

    init {
        observeRealitySensors()
        observeTransScalePulses()
    }

    private fun observeRealitySensors() {
        viewModelScope.launch {
            globalKnowledge.realSignals.collectLatest { signals ->
                lastEmfValue = signals.emfIntensity
                _uiState.update { it.copy(magneticInterference = lastEmfValue) }
            }
        }
    }

    private fun observeTransScalePulses() {
        viewModelScope.launch {
            transScaleBridge.pulses.collectLatest { pulse ->
                when (pulse) {
                    is RealityPulse.AtmosphericShift -> {
                        _uiState.update { currentState ->
                            val updatedPlanet = currentState.detectedPlanet?.copy(
                                atmosphere = currentState.detectedPlanet.atmosphere + pulse.compositionChange.keys
                            )
                            currentState.copy(
                                detectedPlanet = updatedPlanet,
                                anomalyLog = "Atmospheric shift detected via micro-link: ${pulse.compositionChange}"
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * CORE BIOPSY: Uses EMF variation to "penetrate" ground layers.
     */
    fun runGeologicalBiopsy() {
        val planet = _uiState.value.detectedPlanet ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isBiopsyInProgress = true, biopsyProgress = 0f) }
            ttsService.speak("Inițiez biopsia geologică. Folosesc variațiile magnetice locale pentru penetrarea scoarței.")

            for (i in 1..100) {
                delay(50)
                // If EMF is high, biopsy is faster but potentially "noisy"
                val progressStep = 1f + (lastEmfValue / 100f)
                _uiState.update { it.copy(biopsyProgress = (it.biopsyProgress + (progressStep/100f)).coerceAtMost(1f)) }
            }

            // GENERATE LIFE BASED ON PHYSICS
            val gravity = planet.gravity
            val creature = generateExoLife(gravity, planet.atmosphere)
            
            _uiState.update { it.copy(
                isBiopsyInProgress = false,
                detectedCreature = creature,
                geologicalDiscovery = "Detectat nucleu de ${if(lastEmfValue > 60) "Fier Dens" else "Silicate"}"
            ) }

            generateAnaIstlaBioReport(creature, planet)
        }
    }

    private fun generateExoLife(gravity: Double, atmosphere: List<String>): ExoCreature {
        val isHighG = gravity > 15.0
        return ExoCreature(
            name = "Specimen-${Random.nextInt(100, 999)}",
            morphology = if (isHighG) "Multipedal Dens" else "Bilateral Aerodinamic",
            skinType = if (atmosphere.contains("Sulfur")) "Silica-Scales" else "Carbon-Membrane",
            metabolism = if (atmosphere.contains("Methane")) "Chemosynthetic" else "Oxygen-Based",
            homePlanetAtmosphere = atmosphere,
            gravityResistance = gravity.toFloat() * 1.2f,
            description = "Adaptare biologică optimizată pentru mediul de $gravity m/s^2.",
            evolutionStage = (Random.nextInt(1, 10))
        )
    }

    private suspend fun generateAnaIstlaBioReport(creature: ExoCreature, planet: Planet) {
        val prompt = """
            Ești ANA ISTLA (Offline Alpha). Analizează biopsia de pe ${planet.name}.
            CREATURĂ: ${creature.name}, Morfologie: ${creature.morphology}.
            GRAVITAȚIE: ${planet.gravity} m/s^2.
            Explică de ce această creatură a evoluat astfel în acest mediu.
            Răspunde vizionar, scurt, română.
        """.trimIndent()

        val report = if (localAi.isModelLoaded.value) localAi.generateResponse(prompt) else "Analiză locală finalizată."
        _uiState.update { it.copy(aiBiopsyReport = report) }
        ttsService.speak(report)
    }

    fun scanPlanetImage(imageFile: File) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // Simplified for now, in actual build we link to shared repositories
                val detected = planetRepository.getSolarSystemPlanets().random()
                _uiState.update { it.copy(isLoading = false, detectedPlanet = detected) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

data class PlanetScannerUiState(
    val isLoading: Boolean = false,
    val detectedPlanet: Planet? = null,
    val rawAiResponse: String? = null,
    val error: String? = null,
    val anomalyLog: String? = null,
    val isBiopsyInProgress: Boolean = false,
    val biopsyProgress: Float = 0f,
    val detectedCreature: ExoCreature? = null,
    val aiBiopsyReport: String? = null,
    val geologicalDiscovery: String? = null,
    val magneticInterference: Float = 0f
)
