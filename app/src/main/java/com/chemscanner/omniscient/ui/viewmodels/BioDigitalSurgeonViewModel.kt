package com.chemscanner.omniscient.ui.viewmodels

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.GeminiService
import com.chemscanner.omniscient.marrow.services.LocalNeuralEngine
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

data class NanoSurgicalBot(val id: Int, val position: Offset)
data class Pathogen(val id: Int, val position: Offset, val type: String)

data class BioDigitalSurgeonUiState(
    val nanobots: List<NanoSurgicalBot> = emptyList(),
    val pathogens: List<Pathogen> = emptyList(),
    val cellIntegrity: Float = 1.0f,
    val bioSyncLevel: Float = 0.8f,
    val heartRate: Int = 72,
    val isOperationActive: Boolean = false,
    val aiMedicalReport: String? = null
)

@HiltViewModel
class BioDigitalSurgeonViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val geminiService: GeminiService,
    private val localNeuralEngine: LocalNeuralEngine,
    private val ttsService: TextToSpeechService
) : ViewModel() {

    private val _uiState = MutableStateFlow(BioDigitalSurgeonUiState())
    val uiState: StateFlow<BioDigitalSurgeonUiState> = _uiState.asStateFlow()

    init {
        generatePathogens()
        observeBioTelemetry()
    }

    private fun observeBioTelemetry() {
        // Link HUD to real-time biometrics from SCI-OS
        viewModelScope.launch {
            globalKnowledge.userVitality.collectLatest { vitality ->
                val sync = (1.0f - (vitality.stressLevel / 100f)).coerceIn(0.1f, 1.0f)
                _uiState.update { it.copy(
                    bioSyncLevel = sync,
                    heartRate = vitality.heartRate
                )}
                
                // Real-world logic: High stress generates more "biological noise" (pathogens)
                if (vitality.stressLevel > 70 && _uiState.value.pathogens.size < 3) {
                    addStressPathogen()
                }
            }
        }
    }

    private fun addStressPathogen() {
        val newPathogen = Pathogen(
            _uiState.value.pathogens.size + 100,
            Offset(Random.nextFloat() * 1000f, Random.nextFloat() * 1500f),
            "STRESS_TOXIN"
        )
        _uiState.update { it.copy(pathogens = it.pathogens + newPathogen) }
    }

    private fun generatePathogens() {
        val pathogens = List(5) { i ->
            Pathogen(i, Offset(Random.nextFloat() * 1000f, Random.nextFloat() * 1500f), if (Random.nextBoolean()) "VIRUS" else "BACTERIA")
        }
        _uiState.update { it.copy(pathogens = pathogens) }
    }

    fun deployNanobots(offset: Offset) {
        if (_uiState.value.bioSyncLevel < 0.3f) {
            ttsService.speak("Sincronizare prea mică. Calmați-vă pentru a opera.")
            return
        }

        viewModelScope.launch {
            val newBot = NanoSurgicalBot(_uiState.value.nanobots.size, offset)
            _uiState.update { it.copy(nanobots = it.nanobots + newBot, isOperationActive = true) }
            
            delay(400)
            
            // Logic: Find and neutralize the pathogen closest to the tap
            val currentPathogens = _uiState.value.pathogens.toMutableList()
            if (currentPathogens.isNotEmpty()) {
                val closest = currentPathogens.minByOrNull { pathogen ->
                    sqrt((pathogen.position.x - offset.x).toDouble().pow(2.0) + (pathogen.position.y - offset.y).toDouble().pow(2.0))
                }
                
                closest?.let { target ->
                    currentPathogens.remove(target)
                    val newIntegrity = (_uiState.value.cellIntegrity + 0.08f).coerceAtMost(1.0f)
                    _uiState.update { it.copy(pathogens = currentPathogens, cellIntegrity = newIntegrity) }
                    
                    if (currentPathogens.isEmpty()) {
                        triggerMedicalSummary()
                    }
                }
            }
        }
    }

    private fun triggerMedicalSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(aiMedicalReport = "Analizez bio-rezonanța post-intervenție...") }
            
            val hr = _uiState.value.heartRate
            val sync = (_uiState.value.bioSyncLevel * 100).toInt()
            
            val prompt = """
                Ești Ana Aslan 2.0, asistent chirurgical SCI-OS.
                Status Pacient:
                - Puls: $hr bpm
                - Bio-Sincronizare: $sync%
                - Integritate Celulară: 100% (Restaurată)
                
                Oferă un scurt diagnostic medical real și o recomandare pentru regenerare.
            """.trimIndent()

            val report = if (localNeuralEngine.isModelLoaded.value) {
                localNeuralEngine.generateResponse(prompt)
            } else {
                geminiService.generateContent(prompt)
            }

            _uiState.update { it.copy(aiMedicalReport = report, isOperationActive = false) }
            ttsService.speak(report)
            globalKnowledge.logEvent("NANO_MED", "Surgical cleanup success. Health notarized in Akasha Ledger.", 4)
        }
    }
}
