package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.DsnSignal
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class VoyagerUiState(
    val voyagerSignal: DsnSignal? = null,
    val kernelStatus: String = "Link Stabilized"
)

@HiltViewModel
class VoyagerViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository
) : ViewModel() {
    
    // REPARAT: Combinăm semnalele DSN cu statusul Kernel pentru actualizare în timp real
    val uiState: StateFlow<VoyagerUiState> = combine(
        globalKnowledge.dsnSignals,
        globalKnowledge.kernelStatus
    ) { signals, status ->
        val vgr1 = signals.find { it.spacecraft.contains("VGR1", ignoreCase = true) }
        VoyagerUiState(
            voyagerSignal = vgr1,
            kernelStatus = status
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VoyagerUiState())
}
