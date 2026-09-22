package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.SystemEvent
import com.chemscanner.omniscient.marrow.services.CernDataService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CernUiState(
    val lhcStatus: String = "INITIALIZING LINK...",
    val beamEnergy: Double = 0.0,
    val luminosity: Int = 0,
    val isStableBeams: Boolean = false,
    val recentDiscoveries: List<String> = emptyList(),
    val events: List<SystemEvent> = emptyList(),
    val isMonitoring: Boolean = false
)

@HiltViewModel
class CernViewModel @Inject constructor(
    private val cernService: CernDataService,
    private val globalKnowledge: GlobalKnowledgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CernUiState())
    val uiState: StateFlow<CernUiState> = _uiState.asStateFlow()

    init {
        observeCernEvents()
    }

    private fun observeCernEvents() {
        // Sync with Global Persistence
        viewModelScope.launch {
            globalKnowledge.serviceState.collectLatest { state ->
                _uiState.update { it.copy(isMonitoring = state.isCernActive) }
                // Auto-restart if it was active
                if (state.isCernActive) {
                    cernService.startMonitoring()
                }
            }
        }

        viewModelScope.launch {
            globalKnowledge.events.collectLatest { allEvents ->
                val cernEvents = allEvents.filter { it.module.contains("CERN") || it.module.contains("LHC") }
                val lastStatus = cernEvents.lastOrNull { it.module == "CERN_LHC" }?.description ?: ""
                
                val energy = extractDouble(lastStatus, "Energy", "TeV")
                val lum = extractInt(lastStatus, "Luminosity", "Hz/ub")
                val stable = lastStatus.contains("Stable Beams")

                _uiState.update { state ->
                    state.copy(
                        lhcStatus = if (lastStatus.isEmpty()) state.lhcStatus else lastStatus,
                        beamEnergy = if (energy > 0) energy else state.beamEnergy,
                        luminosity = if (lum > 0) lum else state.luminosity,
                        isStableBeams = stable,
                        events = cernEvents.takeLast(20).reversed()
                    )
                }
            }
        }
    }

    fun toggleMonitoring(active: Boolean) {
        globalKnowledge.updateCernActive(active)
        if (active) {
            cernService.startMonitoring()
        }
        _uiState.update { it.copy(isMonitoring = active) }
    }

    fun runManualAnalysis() {
        viewModelScope.launch {
            val result = cernService.analyzeCollisionData()
            if (result.contains("candidat")) {
                _uiState.update { it.copy(recentDiscoveries = it.recentDiscoveries + result) }
            }
        }
    }

    private fun extractDouble(text: String, startTag: String, endTag: String): Double {
        return try {
            val start = text.indexOf(startTag) + startTag.length
            val end = text.indexOf(endTag, start)
            text.substring(start, end).trim().replace("@", "").toDouble()
        } catch (e: Exception) { 0.0 }
    }

    private fun extractInt(text: String, startTag: String, endTag: String): Int {
        return try {
            val start = text.indexOf(startTag) + startTag.length
            val end = text.indexOf(endTag, start)
            text.substring(start, end).trim().replace(":", "").toInt()
        } catch (e: Exception) { 0 }
    }
}
