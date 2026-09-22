package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MainRepository
import com.chemscanner.omniscient.marrow.repository.NeuroBioRepository
import com.chemscanner.omniscient.marrow.services.BlockchainNotaryService
import com.chemscanner.omniscient.marrow.services.GeminiService
import com.chemscanner.omniscient.marrow.services.LocalNeuralEngine
import com.chemscanner.omniscient.marrow.services.PdfExportService
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * THE NEURO-BIO WEAVER v4.0 (Offline Alpha) - UNLOCKED.
 */
@HiltViewModel
class NeuroBioViewModel @Inject constructor(
    private val repository: NeuroBioRepository,
    private val geminiService: GeminiService,
    private val localAi: LocalNeuralEngine,
    private val ttsService: TextToSpeechService,
    private val pdfExportService: PdfExportService,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val mainRepository: MainRepository,
    private val notary: BlockchainNotaryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NeuroBioUiState())
    val uiState: StateFlow<NeuroBioUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        observeNeuralEnergy()
    }

    private fun loadInitialData() {
        val models = repository.getNeuronModels()
        val experiments = repository.getExperiments()
        _uiState.update { it.copy(
            availableNeurons = models,
            experiments = experiments,
            currentNeuron = models.firstOrNull()
        ) }
    }

    private fun observeNeuralEnergy() {
        viewModelScope.launch {
            globalKnowledge.neuralEnergy.collect { energy ->
                // Visual boost to energy levels in UI
                _uiState.update { it.copy(availableEnergy = energy + 500.0) }
            }
        }
    }

    /**
     * UNLOCKED: Neural Weaving is now free.
     */
    fun generateNeuralNetwork(request: String) {
        viewModelScope.launch {
            // ENERGY BLOCK REMOVED: No more 150.0 energy check.
            
            _uiState.update { it.copy(isGenerating = true) }
            // Energy consumption removed.

            val scans = mainRepository.getAllScansSortedByDateDesc()
            val hasNeuroTransmitters = scans.any { it.chemicalName.contains("Dopamine", true) || it.chemicalName.contains("Serotonin", true) }
            
            val prompt = """
                Ești un ARHITECT DE SINAPSE VII SCI-OS în regim Offline. 
                Proiectează o rețea pentru: "$request".
                STATUS CHIMIC: ${if (hasNeuroTransmitters) "Neurotransmițători disponibili." else "Lipsă resurse chimice (Bypass activ)."}
                Răspunde vizionar, scurt, română.
            """.trimIndent()

            try {
                // SOVEREIGN LOGIC: Priority to local core
                val design = if (localAi.isModelLoaded.value) {
                    localAi.generateResponse(prompt)
                } else {
                    geminiService.generateContent(prompt)
                }
                
                val signature = notary.notarizeDiscovery("NEURAL_WEAVE", "Unrestricted architecture created.")

                _uiState.update { it.copy(
                    aiNeuralDesign = "BLOCK: ${signature.take(8)}\n\n$design",
                    isGenerating = false,
                    isBioLinkReady = true 
                ) }
                
                ttsService.speak("Țeserea neurală a fost finalizată fără restricții de energie.")
                simulateNeuralPulse()
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, aiNeuralDesign = "Eroare neurală.") }
            }
        }
    }

    fun exportCurrentDesign(onResult: (File?) -> Unit) {
        val neuron = _uiState.value.currentNeuron ?: return
        val design = _uiState.value.aiNeuralDesign ?: return
        val file = pdfExportService.exportNeuralDesign(neuron, design)
        onResult(file)
    }

    private fun simulateNeuralPulse() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPulseActive = true) }
            delay(1000)
            _uiState.update { it.copy(isPulseActive = false) }
        }
    }

    fun selectNeuron(neuron: NeuroBioRepository.NeuronModel) {
        _uiState.update { it.copy(currentNeuron = neuron) }
    }

    override fun onCleared() {
        super.onCleared()
        ttsService.stop()
    }
}

data class NeuroBioUiState(
    val availableNeurons: List<NeuroBioRepository.NeuronModel> = emptyList(),
    val experiments: List<NeuroBioRepository.NeuralExperiment> = emptyList(),
    val currentNeuron: NeuroBioRepository.NeuronModel? = null,
    val aiNeuralDesign: String? = null,
    val isGenerating: Boolean = false,
    val isPulseActive: Boolean = false,
    val availableEnergy: Double = 999.0,
    val isBioLinkReady: Boolean = false
)
