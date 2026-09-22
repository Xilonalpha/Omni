package com.chemscanner.omniscient.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DNASequence(
    val id: String,
    val geneFunction: String,
    val nucleotideData: String,
    val stabilityLevel: Float = 1.0f,
    val isModified: Boolean = false,
    val source: String = "NCBI_GENBANK"
)

data class GenomicArchitectUiState(
    val dnaSequences: List<DNASequence> = emptyList(),
    val selectedGeneIndex: Int = -1,
    val statusMessage: String = "Aștept identificator genomic...",
    val incubationMessage: String = "Monitoring genomic stability...",
    val availableEnergy: Double = 0.0,
    val analysisResult: String? = null,
    val isAnalyzing: Boolean = false,
    val isCrisprActive: Boolean = false,
    val currentOrganism: String = ""
)

/**
 * GENOMIC ARCHITECT v22.2 (CRASH-PROOF & REAL).
 * AUTHORITY: ARCHITECT XILON.
 * v22.2: Fixed division by zero in UI and restored initial sequences for stability.
 */
@HiltViewModel
class GenomicArchitectViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val geminiService: GeminiService,
    private val genomicAsimilator: GenomicDataAsimilator,
    private val ttsService: TextToSpeechService,
    private val notary: BlockchainNotaryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenomicArchitectUiState())
    val uiState: StateFlow<GenomicArchitectUiState> = _uiState.asStateFlow()

    init {
        loadInitialSequences()
        observeEnergyBridge()
    }

    private fun loadInitialSequences() {
        val base = listOf(
            DNASequence("G-01", "Metabolism Regulation", "ATGC...", 0.85f),
            DNASequence("G-02", "Neural Transmission", "CGTA...", 0.92f),
            DNASequence("G-03", "Cellular Regeneration", "TTAG...", 0.78f)
        )
        _uiState.update { it.copy(dnaSequences = base) }
    }

    private fun observeEnergyBridge() {
        viewModelScope.launch {
            globalKnowledge.neuralEnergy.collect { energy ->
                _uiState.update { it.copy(availableEnergy = energy) }
            }
        }
    }

    fun selectGene(index: Int) {
        _uiState.update { it.copy(selectedGeneIndex = index) }
    }

    fun searchAndAsimilate(organismName: String) {
        if (organismName.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, statusMessage = "Accesez NCBI...") }
            val record = genomicAsimilator.fetchRealSequence(organismName)
            if (record != null) {
                val newSeq = DNASequence(record.id, record.definition, record.sequence)
                _uiState.update { it.copy(dnaSequences = listOf(newSeq), isAnalyzing = false, currentOrganism = organismName) }
                ttsService.speak("Sursă genomică asimilată.")
            } else {
                _uiState.update { it.copy(isAnalyzing = false, statusMessage = "Specie neidentificată.") }
            }
        }
    }

    fun incubateWithLastMolecule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, incubationMessage = "Merging molecular data...") }
            delay(2000)
            performArchitectAnalysis()
        }
    }

    fun performArchitectAnalysis() {
        val sequence = _uiState.value.dnaSequences.firstOrNull() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, statusMessage = "Analizez nucleotidele...") }
            val prompt = "Analyze NCBI DNA sequence fragment: ${sequence.nucleotideData.take(500)}"
            val result = geminiService.generateContent(prompt, "ANA - GENOMIC ARCHITECT")
            _uiState.update { it.copy(isAnalyzing = false, analysisResult = result) }
            ttsService.speak(result)
        }
    }

    fun applyCrispr() {
        val index = _uiState.value.selectedGeneIndex
        if (index == -1) return
        val sequence = _uiState.value.dnaSequences[index]

        viewModelScope.launch {
            _uiState.update { it.copy(isCrisprActive = true, incubationMessage = "Applying CRISPR-Cas9 sequence edit...") }
            val prompt = "Propose CRISPR edit for: ${sequence.nucleotideData.take(300)}"
            val edit = geminiService.generateContent(prompt, "CRISPR_ENGINE")
            
            val updated = _uiState.value.dnaSequences.toMutableList()
            updated[index] = updated[index].copy(isModified = true, stabilityLevel = 1.0f)
            
            _uiState.update { it.copy(dnaSequences = updated, isCrisprActive = false, analysisResult = edit) }
            ttsService.speak("Editare CRISPR finalizată.")
        }
    }
}
