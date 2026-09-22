package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.chemscanner.omniscient.marrow.data.models.Experiment
import com.chemscanner.omniscient.marrow.repository.ExperimentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ExperimentARViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val experimentRepository: ExperimentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExperimentARUiState())
    val uiState: StateFlow<ExperimentARUiState> = _uiState.asStateFlow()

    private val experimentId: String? = savedStateHandle.get<String>("experimentId")

    init {
        experimentId?.let { id ->
            val experiment = experimentRepository.experiments.value.find { it.id == id }
            _uiState.update { it.copy(experiment = experiment, remainingLabels = experiment?.materials ?: emptyList()) }
        }
    }

    fun onLabelPlaced(label: String) {
        _uiState.update {
            val remaining = it.remainingLabels.toMutableList()
            remaining.remove(label)
            it.copy(placedLabels = it.placedLabels + label, remainingLabels = remaining)
        }
    }
}

data class ExperimentARUiState(
    val experiment: Experiment? = null,
    val placedLabels: List<String> = emptyList(),
    val remainingLabels: List<String> = emptyList()
)
