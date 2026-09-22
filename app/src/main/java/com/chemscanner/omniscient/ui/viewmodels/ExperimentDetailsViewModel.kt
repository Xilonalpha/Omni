package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.data.models.Experiment
import com.chemscanner.omniscient.marrow.repository.ExperimentRepository
import com.chemscanner.omniscient.marrow.services.GeminiService
import com.chemscanner.omniscient.marrow.services.PdfExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ExperimentDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val experimentRepository: ExperimentRepository,
    private val geminiService: GeminiService,
    private val pdfExportService: PdfExportService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExperimentDetailsUiState())
    val uiState: StateFlow<ExperimentDetailsUiState> = _uiState.asStateFlow()

    private val experimentId: String? = savedStateHandle.get<String>("experimentId")

    init {
        loadExperimentDetails()
    }

    private fun loadExperimentDetails() {
        experimentId?.let { id ->
            val experiment = experimentRepository.experiments.value.find { it.id == id }
            _uiState.update { it.copy(experiment = experiment) }
        }
    }

    fun startExperiment() {
        val experiment = _uiState.value.experiment ?: return
        val prompt = """
        Act as a friendly and knowledgeable chemistry teacher. Your task is to guide a user through a hands-on experiment: "${experiment.title}".

        1.  **Generate Step-by-Step Instructions:** Provide clear, numbered, step-by-step instructions for the experiment.
        2.  **Explain the Chemistry:** After each step, briefly explain the chemical reaction or scientific principle being demonstrated in a simple and easy-to-understand way.
        3.  **Emphasize Safety:** Include safety warnings where appropriate (e.g., "Be careful with the heat source").

        The user has the following materials: ${experiment.materials.joinToString(", ")}.
        Present the full set of instructions in a single, comprehensive response.
        """

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val response = geminiService.generateContent(prompt)
                val steps = response.lines().filter { it.isNotBlank() }
                _uiState.update { it.copy(guidedSteps = steps, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                // Handle error
            }
        }
    }

    fun exportReport(onResult: (File?) -> Unit) {
        val experiment = _uiState.value.experiment ?: return
        val steps = _uiState.value.guidedSteps
        if (steps.isNotEmpty()) {
            val file = pdfExportService.exportExperimentReport(experiment, steps)
            onResult(file)
        }
    }

    fun startArDemonstration() {
        val experiment = _uiState.value.experiment ?: return
        val scenario = when (experiment.id) {
            "aspirin_synthesis_demo" -> listOf(
                "NARRATOR: Welcome to the virtual synthesis of Aspirin! First, we place our primary reactant, Salicylic Acid, into the flask.",
                "SHOW_MODEL: salicylic_acid",
                "NARRATOR: Next, we add our second reactant, Acetic Anhydride.",
                "SHOW_MODEL: acetic_anhydride",
                "NARRATOR: A catalyst, Phosphoric Acid, is added to speed up the reaction.",
                "NARRATOR: Heat is applied. The molecules react.",
                "HIDE_MODELS: salicylic_acid, acetic_anhydride",
                "SHOW_MODEL: aspirin",
                "NARRATOR: And there we have it! The final product, Acetylsalicylic Acid, or Aspirin, has been formed."
            )
            else -> emptyList()
        }
        _uiState.update { it.copy(arScenario = scenario) }
    }
}

data class ExperimentDetailsUiState(
    val experiment: Experiment? = null,
    val guidedSteps: List<String> = emptyList(),
    val arScenario: List<String> = emptyList(),
    val isLoading: Boolean = false
)
