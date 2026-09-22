package com.chemscanner.omniscient.ui.viewmodels

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpectralWave(
    val id: Int,
    val type: String,
    val frequency: Float,
    val intensity: Float,
    val color: Long,
    val label: String = ""
)

data class SpectralUiState(
    val waves: List<SpectralWave> = emptyList(),
    val isScanning: Boolean = false,
    val activeSensors: Int = 4,
    val detectedAnomalies: Int = 0, // FIXED: Added for Activity
    val anomalyReport: String? = null,
    val isAnalyzing: Boolean = false
)

/**
 * SPECTRAL EYE v26.0 (THE SPECTRAL ORACLE).
 * MISSION: Cross-sensor signal correlation and AI-driven anomaly detection.
 * AUTHORITY: ARCHITECT XILON.
 * v26.0: Transition from visual oscilloscope to real-time signal intelligence.
 */
@HiltViewModel
class SpectralEyeViewModel @Inject constructor(
    private val ttsService: TextToSpeechService,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val geminiService: GeminiService,
    private val notary: BlockchainNotaryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpectralUiState())
    val uiState: StateFlow<SpectralUiState> = _uiState.asStateFlow()

    init {
        startOscilloscopeSync()
    }

    private fun startOscilloscopeSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            
            // COLECTARE DATE REALE DIN MADUVA SISTEMULUI
            launch {
                globalKnowledge.realSignals.collectLatest { signals ->
                    updateMagneticWave(signals.emfIntensity)
                }
            }
            launch {
                globalKnowledge.starlinkMesh.collectLatest { mesh ->
                    updateMeshWave(mesh.networkLatencyMs)
                }
            }
            launch {
                globalKnowledge.galacticData.collectLatest { galactic ->
                    updateCosmicWave(galactic.cosmicRayIntensity)
                }
            }
            launch {
                globalKnowledge.spectralHexColor.collectLatest { hex ->
                    updateOpticalWave(hex)
                }
            }
        }
    }

    /**
     * NOU: ANALIZA CROSS-SENZOR (Realitatea 100%)
     * Trimite toate datele brute către ANA pentru a găsi corelații invizibile.
     */
    fun performCrossSensorAudit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }
            
            val emf = globalKnowledge.realSignals.value.emfIntensity
            val latency = globalKnowledge.starlinkMesh.value.networkLatencyMs
            val cosmic = globalKnowledge.galacticData.value.cosmicRayIntensity
            val optical = globalKnowledge.spectralHexColor.value

            val prompt = """
                [SPECTRAL_ORACLE_AUDIT]
                CÂMP MAGNETIC (EMF): $emf uT
                LATENȚĂ REȚEA: $latency ms
                RADIAȚIE COSMICĂ: $cosmic
                AMPRENTĂ OPTICĂ (HEX): $optical
                
                Ești ANA, Oracolul Spectral. Analizează corelația dintre aceste 4 dimensiuni ale realității locale. 
                Există vreo anomalie care indică o perturbare externă (ex: activitate solară, interferență electronică, sau un semnal necunoscut)? 
                Oferă un diagnostic tehnic și o recomandare XILON.
            """.trimIndent()

            val analysis = geminiService.generateContent(prompt, "ANA - SIGNAL INTELLIGENCE")
            
            // Increment anomalies for visual feedback if analysis is "scary"
            val anomalies = if (analysis.length > 100) 1 else 0

            _uiState.update { it.copy(isAnalyzing = false, anomalyReport = analysis, detectedAnomalies = it.detectedAnomalies + anomalies) }
            notary.notarizeDiscovery("SPECTRAL_AUDIT", "Cross-sensor correlation complete. EMF: $emf")
            ttsService.speak(analysis)
        }
    }

    private fun updateMagneticWave(emf: Float) {
        val wave = SpectralWave(0, "MAGNETIC", 0.5f + (emf / 50f), (emf / 100f).coerceIn(0.2f, 1.2f), 0xFF00E5FF, "EMF: ${"%.1f".format(emf)} uT")
        refreshWaveList(wave)
    }

    private fun updateMeshWave(latency: Int) {
        val wave = SpectralWave(1, "MESH", 2.0f / (latency.coerceAtLeast(1) / 20f), if (latency < 50) 0.8f else 0.4f, 0xFF7C4DFF, "MESH: ${latency}ms")
        refreshWaveList(wave)
    }

    private fun updateCosmicWave(intensity: Float) {
        val wave = SpectralWave(2, "COSMIC", 5.0f * intensity, intensity.coerceIn(0.3f, 1.0f), 0xFF00E676, "COSMIC: ${"%.2f".format(intensity)}")
        refreshWaveList(wave)
    }

    private fun updateOpticalWave(hex: String) {
        try {
            val colorInt = Color.parseColor(hex).toLong() and 0xFFFFFFFFL
            val wave = SpectralWave(3, "OPTICAL", 1.2f, 0.9f, colorInt, "OPTICAL: $hex")
            refreshWaveList(wave)
        } catch (e: Exception) { }
    }

    private fun refreshWaveList(newWave: SpectralWave) {
        _uiState.update { state ->
            val currentWaves = state.waves.toMutableList()
            val index = currentWaves.indexOfFirst { it.id == newWave.id }
            if (index != -1) currentWaves[index] = newWave else currentWaves.add(newWave)
            state.copy(waves = currentWaves.sortedBy { it.id })
        }
    }
}
