package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.*
import com.chemscanner.omniscient.marrow.services.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CelestialUiState(
    val latestCosmicAlert: CosmicAlert? = null,
    val dsnSignals: List<DsnSignal> = emptyList(),
    val starlinkStatus: StarlinkMeshStatus = StarlinkMeshStatus(),
    val lastJwstDiscovery: String = "Waiting for deep space sync...",
    val isVeraRubinActive: Boolean = false,
    val isDsnActive: Boolean = false,
    val isStarlinkActive: Boolean = false,
    val isJwstActive: Boolean = false
)

@HiltViewModel
class CelestialViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val veraRubinService: VeraRubinAlertService,
    private val dsnService: DsnLiveService,
    private val starlinkService: StarlinkMeshService,
    private val jwstService: JamesWebbSpaceTelescopeService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CelestialUiState())
    val uiState: StateFlow<CelestialUiState> = _uiState.asStateFlow()

    init {
        observeSpaceTelemetry()
    }

    private fun observeSpaceTelemetry() {
        // Sync UI state with Global Repository (Persistence)
        viewModelScope.launch {
            globalKnowledge.serviceState.collectLatest { state ->
                _uiState.update { it.copy(
                    isVeraRubinActive = state.isVeraRubinActive,
                    isDsnActive = state.isDsnActive,
                    isStarlinkActive = state.isStarlinkActive,
                    isJwstActive = state.isJwstActive
                )}
            }
        }

        viewModelScope.launch {
            globalKnowledge.latestCosmicAlert.collectLatest { alert ->
                _uiState.update { it.copy(latestCosmicAlert = alert) }
            }
        }

        viewModelScope.launch {
            globalKnowledge.dsnSignals.collectLatest { signals ->
                _uiState.update { it.copy(dsnSignals = signals) }
            }
        }

        viewModelScope.launch {
            globalKnowledge.starlinkMesh.collectLatest { mesh ->
                _uiState.update { it.copy(starlinkStatus = mesh) }
            }
        }

        viewModelScope.launch {
            globalKnowledge.events.collectLatest { events ->
                val jwstEvent = events.findLast { it.module == "JWST" || it.module == "DEEP_SPACE_VISION" }
                jwstEvent?.let { event ->
                    _uiState.update { it.copy(lastJwstDiscovery = event.description) }
                }
            }
        }
    }

    fun toggleVeraRubin(active: Boolean) {
        globalKnowledge.updateVeraRubinActive(active)
        if (active) veraRubinService.startAlertStream()
    }

    fun toggleDsn(active: Boolean) {
        globalKnowledge.updateDsnActive(active)
        if (active) dsnService.startDsnMonitoring()
    }

    fun toggleStarlink(active: Boolean) {
        globalKnowledge.updateStarlinkActive(active)
        if (active) starlinkService.startMeshAnalysis()
    }

    fun toggleJwst(active: Boolean) {
        globalKnowledge.updateJwstActive(active)
        if (active) jwstService.startDeepSpaceSync()
    }
}
