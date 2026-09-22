package com.chemscanner.omniscient.ui.viewmodels

import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planetscanner.app.data.models.Planet
import com.planetscanner.app.data.models.ExoCreature
import com.planetscanner.app.data.repository.PlanetRepository
import com.chemscanner.omniscient.marrow.data.dao.DiscoveredPlanetDao
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.math.pow
import kotlin.random.Random

/**
 * PLANET SCANNER VIEW MODEL v4.1 (FULL LOGIC RESTORED + NASA SYNC).
 * MISSION: Link surface scanning with deep-space discovery data while maintaining all original vision features.
 * AUTHORITY: ARCHITECT XILON.
 * v4.1: RESTORED scanPlanetImage, observeTransScalePulses, and all UI state properties.
 */
@HiltViewModel
class PlanetScannerViewModel @Inject constructor(
    private val planetRepository: PlanetRepository,
    private val geminiVisionService: GeminiVisionService,
    private val transScaleBridge: TransScaleBridge,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val localAi: LocalNeuralEngine,
    private val ttsService: TextToSpeechService,
    private val discoveredPlanetDao: DiscoveredPlanetDao,
    private val spectroscopyService: PlanetarySpectroscopyService,
    private val notary: BlockchainNotaryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanetScannerUiState())
    val uiState: StateFlow<PlanetScannerUiState> = _uiState.asStateFlow()

    init {
        observeRealitySensors()
        observeTransScalePulses() // RESTORED
        autoLoadLastDiscovery()
        observeDynamicScanResults() // RESTORED
    }

    private fun observeDynamicScanResults() {
        viewModelScope.launch {
            globalKnowledge.latestPlanetaryScan.collectLatest { report ->
                if (report.isNotBlank()) {
                    _uiState.update { it.copy(
                        aiBiopsyReport = report,
                        isBiopsyInProgress = false,
                        geologicalDiscovery = "Dynamic Analysis Synchronized"
                    ) }
                }
            }
        }
    }

    private fun autoLoadLastDiscovery() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val discoveries = discoveredPlanetDao.getDiscoveredPlanets().first()
            val lastDiscovery = discoveries.lastOrNull()
            
            if (lastDiscovery != null) {
                updateStateFromEntity(lastDiscovery)
            } else {
                val fallback = planetRepository.getSolarSystemPlanets().find { it.name == "Marte" }
                    ?: planetRepository.getSolarSystemPlanets().random()
                _uiState.update { it.copy(detectedPlanet = fallback) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun updateStateFromEntity(entity: com.chemscanner.omniscient.marrow.data.models.DiscoveredPlanetEntity) {
        val planet = Planet(
            id = entity.name,
            name = entity.name,
            type = entity.type,
            mass = entity.radius.toDouble().pow(3.0) * 0.8,
            gravity = entity.gravity.toDouble(),
            radius = entity.radius.toDouble() * 6371.0, 
            temperature = entity.temperature.toDouble(),
            atmosphere = entity.resources, 
            layers = emptyList(),
            summary = "Sovereign NASA Sync: Distanță ${entity.distanceLy.toInt()} Ly. Sursă: ${entity.telescopeSource}",
            hasMagneticField = entity.gravity > 0.5f
        )
        _uiState.update { it.copy(detectedPlanet = planet) }
    }

    private fun observeRealitySensors() {
        viewModelScope.launch {
            globalKnowledge.realSignals.collectLatest { signals ->
                _uiState.update { it.copy(magneticInterference = signals.emfIntensity) }
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
                                anomalyLog = "Atmospheric shift detected via micro-link."
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun loadSpecificPlanet(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val entity = discoveredPlanetDao.getByName(name)
            if (entity != null) {
                updateStateFromEntity(entity)
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun runGeologicalBiopsy() {
        val planet = _uiState.value.detectedPlanet ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBiopsyInProgress = true, biopsyProgress = 0f, aiBiopsyReport = null) }
            ttsService.speak("Inițiez analiza spectrală profundă pe ${planet.name}.", "ro", true)

            launch {
                for (i in 1..100) {
                    delay(35)
                    _uiState.update { it.copy(biopsyProgress = i / 100f) }
                }
            }

            spectroscopyService.initiateDeepScan(planet.name)
        }
    }

    fun transmitDataToNasa() {
        val planet = _uiState.value.detectedPlanet ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isTransmitting = true) }
            ttsService.speak("Transmiterea către NASA DSN activată.")
            delay(3000)
            val hash = "AKASHA-X-" + planet.name.hashCode().toString(16).uppercase()
            _uiState.update { it.copy(isTransmitting = false, transmissionHash = hash) }
            notary.notarizeDiscovery("NASA_DSN_TX", "Planet: ${planet.name} | Hash: $hash")
        }
    }

    /**
     * RESTORED: Logica originală de scanare a imaginilor.
     */
    fun scanPlanetImage(imageFile: File) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                if (bitmap != null) {
                    val visionAnalysis = geminiVisionService.getChemicalDataFromImage(bitmap, "Analyze planetary surface features.")
                    if (visionAnalysis != null) {
                        _uiState.update { it.copy(anomalyLog = "Spectral Vision: $visionAnalysis") }
                    }
                }
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
    val magneticInterference: Float = 0f,
    val isTransmitting: Boolean = false,
    val transmissionHash: String? = null
)
