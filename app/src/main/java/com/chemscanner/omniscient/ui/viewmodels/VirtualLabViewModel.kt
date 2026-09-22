package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.data.models.ChemicalEntity
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MainRepository
import com.chemscanner.omniscient.marrow.services.MoleculeRenderService
import com.chemscanner.omniscient.marrow.services.ReactionSimulator
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VirtualLabUiState(
    val isSimulating: Boolean = false,
    val result: String? = null,
    val availableChemicals: List<String> = emptyList(),
    val availableEnergy: Double = 999999.9,
    val currentEnvironmentTemp: Float = 25f,
    val activeAstroGens: List<String> = emptyList(),
    val pendingArScenario: List<String> = emptyList() // Scenariul AR pregătit
)

@HiltViewModel
class VirtualLabViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val reactionSimulator: ReactionSimulator,
    private val ttsService: TextToSpeechService,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val renderService: MoleculeRenderService
) : ViewModel() {

    private val _uiState = MutableStateFlow(VirtualLabUiState())
    val uiState: StateFlow<VirtualLabUiState> = _uiState.asStateFlow()

    init {
        loadChemicals()
        observeNeuralEcosystem()
        observePharmaSignals()
    }

    private fun loadChemicals() {
        viewModelScope.launch {
            val scans = mainRepository.getAllScansSortedByDateDesc()
            val names = scans.map { it.chemicalName }.distinct()
            _uiState.update { it.copy(availableChemicals = names) }
        }
    }

    private fun observeNeuralEcosystem() {
        viewModelScope.launch {
            globalKnowledge.neuralEnergy.collectLatest { _ ->
                _uiState.update { it.copy(availableEnergy = Double.MAX_VALUE) }
            }
        }
        viewModelScope.launch {
            globalKnowledge.realSignals.collectLatest { signals ->
                _uiState.update { it.copy(currentEnvironmentTemp = 20f + (signals.ambientLuminosity / 100f)) }
            }
        }
    }

    private fun observePharmaSignals() {
        viewModelScope.launch {
            globalKnowledge.pharmaGenome.collectLatest { pharma ->
                if (pharma.targetGenes.isNotEmpty()) {
                    _uiState.update { it.copy(activeAstroGens = pharma.targetGenes) }
                    initiateAstroPharmaSynthesis(pharma.drugName, pharma.targetGenes.first())
                }
            }
        }
    }

    private fun initiateAstroPharmaSynthesis(baseDrug: String, targetGene: String) {
        viewModelScope.launch {
            val msg = "Xilon, am detectat gena $targetGene. Inițiez sinteza hibridă."
            globalKnowledge.logEvent("ASTRO_LAB", msg, 5)
            ttsService.speak(msg)
            
            simulateReaction(baseDrug, "Exo-Agent-$targetGene")
        }
    }

    fun simulateReaction(a: String, b: String) {
        if (a.isBlank() || b.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSimulating = true, result = null) }
            val reactionResult = reactionSimulator.simulateReaction(a, b)
            kotlinx.coroutines.delay(1500) 

            _uiState.update { it.copy(isSimulating = false, result = reactionResult) }
            
            // AUTOMATIZARE 3D: Dacă avem un rezultat valid, pregătim vizualizarea AR
            val dummyEntity = ChemicalEntity(name = "Agent $a-$b", smiles = "SYNTHETIC_RESULT_$a$b")
            val arScenario = renderService.generateArScenarioForChemical(dummyEntity)
            
            _uiState.update { it.copy(pendingArScenario = arScenario) }
            
            globalKnowledge.logEvent("VIRTUAL_LAB", "Sinteză finalizată. Scenariul holografic este gata.", 4)
            ttsService.speak("Sinteza este gata. Modelul 3D a fost generat în planul AR.")
        }
    }

    fun speakResult() {
        _uiState.value.result?.let { ttsService.speak(it) }
    }
}
