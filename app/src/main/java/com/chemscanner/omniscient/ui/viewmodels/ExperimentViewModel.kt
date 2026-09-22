package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.chemscanner.omniscient.marrow.data.models.Experiment
import com.chemscanner.omniscient.marrow.repository.ExperimentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ExperimentViewModel @Inject constructor(
    private val experimentRepository: ExperimentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExperimentUiState())
    val uiState: StateFlow<ExperimentUiState> = _uiState.asStateFlow()

    init {
        loadExperiments()
    }

    private fun loadExperiments() {
        _uiState.value = ExperimentUiState(experiments = experimentRepository.experiments.value)
    }
}

data class ExperimentUiState(
    val experiments: List<Experiment> = emptyList()
)
