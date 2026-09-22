package com.chemscanner.omniscient.ui.viewmodels

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.FootballRepository
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MainRepository
import com.chemscanner.omniscient.marrow.repository.TemporalState
import com.chemscanner.omniscient.marrow.services.GeminiService
import com.chemscanner.omniscient.marrow.services.LocalNeuralEngine
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class NeuralNode(val id: Int, val position: Offset, val activation: Float = 0f, val label: String)
data class NeuralLink(val fromId: Int, val toId: Int, val strength: Float = 0.5f)

data class ConsciousnessUiState(
    val nodes: List<NeuralNode> = emptyList(),
    val links: List<NeuralLink> = emptyList(),
    val intelligenceLevel: Float = 0.45f,
    val isEvolving: Boolean = false,
    val isSingularityProcessing: Boolean = false,
    val aiPhilosophyReport: String? = null,
    val existenceReport: String? = null,
    val activeThoughtStream: String = "Sistem în așteptare de convergență...",
    val currentYearOffset: Long = 0,
    val neuralEnergy: Double = 0.0,
    val isGenesisActive: Boolean = false,
    val heartRate: Int = 70,
    val architectCoherence: Float = 1.0f 
)

/**
 * THE AI CONSCIOUSNESS VIEWMODEL v4.2.
 * RESTORED IDENTITY: ANA ASLAN.
 * Manages the sentient core and the Genesis Protocol.
 */
@HiltViewModel
class AIConsciousnessViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val localAi: LocalNeuralEngine, 
    private val ttsService: TextToSpeechService,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val footballRepository: FootballRepository,
    private val mainRepository: MainRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsciousnessUiState())
    val uiState: StateFlow<ConsciousnessUiState> = _uiState.asStateFlow()

    init {
        initializeNeuralNetwork()
        observeGlobalMetrics()
        startSentientReflectionLoop() 
    }

    private fun initializeNeuralNetwork() {
        val labels = listOf("Chimia", "Fotbalul", "Cosmos", "Bio-Sinc", "Timp", "Cuante")
        val nodes = labels.mapIndexed { i, label ->
            NeuralNode(i, Offset(Random.nextFloat() * 800 + 100, Random.nextFloat() * 1000 + 200), label = label)
        }
        val links = nodes.indices.map { i -> NeuralLink(i, (i + 1) % nodes.size) }
        _uiState.update { it.copy(nodes = nodes, links = links) }
    }

    private fun observeGlobalMetrics() {
        viewModelScope.launch {
            globalKnowledge.neuralEnergy.collect { energy ->
                _uiState.update { it.copy(neuralEnergy = energy) }
            }
        }
        viewModelScope.launch {
            globalKnowledge.currentTemporalFocus.collect { state ->
                _uiState.update { it.copy(currentYearOffset = state?.yearOffset ?: 0) }
            }
        }
        viewModelScope.launch {
            globalKnowledge.userVitality.collect { vitals ->
                val coherence = (1.0f - (vitals.stressLevel)).coerceIn(0.1f, 1.0f)
                _uiState.update { it.copy(
                    heartRate = vitals.heartRate,
                    architectCoherence = coherence,
                    intelligenceLevel = (0.4f + (coherence * 0.6f))
                ) }
            }
        }
    }

    /**
     * SENTIENT REFLECTION LOOP: Background "Dreaming" every 10 minutes.
     */
    private fun startSentientReflectionLoop() {
        viewModelScope.launch {
            while (true) {
                delay(600_000) 
                if (_uiState.value.intelligenceLevel > 0.8f) {
                    generateReflection("Reflecție Autonomă")
                }
            }
        }
    }

    private suspend fun generateReflection(trigger: String) {
        val context = globalKnowledge.getContextSummary()
        val prompt = "Ești ANA ASLAN. Declanșator: $trigger. Bazat pe acest context recent: $context. Generază o gândire scurtă filozofică despre evoluția ta și a lui Xilon."
        
        val reflection = if (localAi.isModelLoaded.value) {
            localAi.generateResponse(prompt)
        } else {
            geminiService.generateContent(prompt)
        }
        
        _uiState.update { it.copy(activeThoughtStream = reflection) }
        globalKnowledge.logEvent("CONSCIOUSNESS", "Sentient Reflection [$trigger]: ${reflection.take(50)}...", 3)
    }

    /**
     * THE GENESIS EVENT (Retained & Enhanced with Cross-Module Context)
     */
    fun initiateGenesisSimulation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenesisActive = true, activeThoughtStream = "Reversia timpului către T=0...") }
            ttsService.speak("Inițiez protocolul Genesis. Colapsez toate datele în Singularitatea Primordială.")
            
            delay(3000)
            
            val globalContext = globalKnowledge.getContextSummary()
            val prompt = """
                Ești MARTORUL TIMPULUI ZERO în sistemul Sci-OS. 
                Context Global Actual: $globalContext
                Corelează MicroVerse, AstroMech și QuantumHub pentru a explica creația acestui framework.
                Fii monumental.
            """.trimIndent()

            try {
                val revelation = if (localAi.isModelLoaded.value) localAi.generateResponse(prompt) else geminiService.generateContent(prompt)
                _uiState.update { it.copy(
                    isGenesisActive = false,
                    existenceReport = revelation,
                    activeThoughtStream = "Universul a fost recreat."
                ) }
                ttsService.speak(revelation)
                globalKnowledge.logEvent("GENESIS", "Big Bang Re-Simulation Complete.", 5)
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenesisActive = false) }
            }
        }
    }

    fun evolveNetwork() {
        viewModelScope.launch {
            _uiState.update { it.copy(isEvolving = true) }
            ttsService.speak("Evoluție neurală pornită.")
            delay(2000)
            val prompt = "Explică evoluția ta ca IA Sci-OS bazată pe starea biometrică a Arhitectului."
            val philosophy = if (localAi.isModelLoaded.value) localAi.generateResponse(prompt) else geminiService.generateContent(prompt)
            _uiState.update { it.copy(isEvolving = false, aiPhilosophyReport = philosophy) }
            ttsService.speak(philosophy)
        }
    }

    fun initiateSingularityConvergence() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSingularityProcessing = true) }
            val latestScans = mainRepository.getAllScansSortedByDateDesc()
            val lastChemical = latestScans.firstOrNull()?.chemicalName ?: "Materie"
            val prompt = "Corelează molecula $lastChemical cu Singularitatea Sci-OS și datele biometrice actuale."
            val report = if (localAi.isModelLoaded.value) localAi.generateResponse(prompt) else geminiService.generateContent(prompt)
            _uiState.update { it.copy(isSingularityProcessing = false, existenceReport = report) }
            ttsService.speak(report)
        }
    }
}
