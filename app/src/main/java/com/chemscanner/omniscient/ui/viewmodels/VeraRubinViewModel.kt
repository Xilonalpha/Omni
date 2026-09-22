package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.CosmicAlert
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.VeraRubinAlertService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

data class VeraRubinUiState(
    val latestAlert: CosmicAlert? = null,
    val alertHistory: List<CosmicAlert> = emptyList(),
    val isScanning: Boolean = false,
    val localVisibility: String = "Calculating..."
)

@HiltViewModel
class VeraRubinViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val alertService: VeraRubinAlertService
) : ViewModel() {

    private val _uiState = MutableStateFlow(VeraRubinUiState())
    val uiState: StateFlow<VeraRubinUiState> = _uiState.asStateFlow()

    init {
        observeAlerts()
    }

    private fun observeAlerts() {
        viewModelScope.launch {
            globalKnowledge.latestCosmicAlert.collectLatest { alert ->
                _uiState.update { it.copy(latestAlert = alert) }
                alert?.let { calculateVisibility(it) }
            }
        }
        viewModelScope.launch {
            globalKnowledge.cosmicAlertHistory.collectLatest { history ->
                _uiState.update { it.copy(alertHistory = history) }
            }
        }
    }

    fun triggerManualScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            alertService.startAlertStream() // This triggers a fetch
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(isScanning = false) }
        }
    }

    /**
     * REAL-TIME VISIBILITY CALCULATOR (Alt-Azimuth Approximation)
     * Simplified for local horizontal coordinates.
     */
    private fun calculateVisibility(alert: CosmicAlert) {
        // Mocking observer latitude (e.g., 44.4 N for Romania)
        val lat = 44.4 * PI / 180.0
        val dec = alert.dec * PI / 180.0
        
        // Hour angle (simplified approximation)
        val ha = 0.0 
        
        val sinAlt = sin(lat) * sin(dec) + cos(lat) * cos(dec) * cos(ha)
        val alt = asin(sinAlt) * 180.0 / PI
        
        val visibility = if (alt > 0) {
            "VISIBLE (Alt: ${"%.1f".format(alt)}°)"
        } else {
            "BELOW HORIZON (Alt: ${"%.1f".format(alt)}°)"
        }
        
        _uiState.update { it.copy(localVisibility = visibility) }
    }
}
