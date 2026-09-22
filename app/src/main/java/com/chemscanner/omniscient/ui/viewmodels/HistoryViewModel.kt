package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.data.models.ScanHistory
import com.chemscanner.omniscient.marrow.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val scans: List<ScanHistory>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            try {
                val scans = mainRepository.getAllScansSortedByDateDesc()
                _uiState.value = HistoryUiState.Success(scans)
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error("Failed to load history: ${e.message}")
            }
        }
    }

    suspend fun getScanById(id: Long): ScanHistory? {
        return mainRepository.getScanById(id)
    }
}
