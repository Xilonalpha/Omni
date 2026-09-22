package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.StarlinkMeshStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StarlinkUiState(
    val mesh: StarlinkMeshStatus = StarlinkMeshStatus()
)

@HiltViewModel
class StarlinkViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository
) : ViewModel() {
    val uiState: StateFlow<StarlinkUiState> = globalKnowledge.starlinkMesh.map { mesh ->
        StarlinkUiState(mesh = mesh)
    }.stateIn(viewModelScope, SharingStarted.Lazily, StarlinkUiState())
}
