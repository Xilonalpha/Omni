package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.data.models.ScanHistory // FIXED IMPORT
import com.chemscanner.omniscient.marrow.repository.MainRepository // FIXED IMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HistoryDetailUiState {
    object Loading : HistoryDetailUiState()
    data class Success(val scan: ScanHistory) : HistoryDetailUiState()
    data class Error(val message: String) : HistoryDetailUiState()
}

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve scanId as String from navigation arguments
    private val scanId: String? = savedStateHandle.get<String>("scanId") ?: savedStateHandle.get<String>("EXTRA_SCAN_ID")

    private val _uiState = MutableStateFlow<HistoryDetailUiState>(HistoryDetailUiState.Loading)
    val uiState: StateFlow<HistoryDetailUiState> = _uiState.asStateFlow()

    init {
        loadScanDetails()
    }

    private fun loadScanDetails() {
        viewModelScope.launch {
            _uiState.value = HistoryDetailUiState.Loading
            
            val targetId: Long? = scanId?.toLongOrNull() ?: run {
                val history = mainRepository.getAllScansSortedByDateDesc()
                history.firstOrNull()?.id
            }

            if (targetId == null) {
                _uiState.value = HistoryDetailUiState.Error("Arhiva Akasha este goală. Te rog asimilează o moleculă mai întâi.")
                return@launch
            }

            try {
                val scan = mainRepository.getScanById(targetId)
                if (scan != null) {
                    _uiState.value = HistoryDetailUiState.Success(scan)
                } else {
                    _uiState.value = HistoryDetailUiState.Error("Descoperirea a fost pierdută în fluxul temporal.")
                }
            } catch (e: Exception) {
                _uiState.value = HistoryDetailUiState.Error("Eroare de acces: ${e.message}")
            }
        }
    }
}
