package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.UserSettingsRepository // FIXED IMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthProfileViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthProfileUiState())
    val uiState: StateFlow<HealthProfileUiState> = _uiState.asStateFlow()

    init {
        loadAllergies()
    }

    private fun loadAllergies() {
        viewModelScope.launch {
            val allergies = userSettingsRepository.getAlergies()
            _uiState.update { it.copy(allergies = allergies.toList().sorted()) }
        }
    }

    fun addAllergy(allergy: String) {
        if (allergy.isNotBlank()) {
            viewModelScope.launch {
                userSettingsRepository.addAllergy(allergy.trim())
                loadAllergies()
            }
        }
    }

    fun removeAllergy(allergy: String) {
        viewModelScope.launch {
            userSettingsRepository.removeAllergy(allergy)
            loadAllergies()
        }
    }
}

data class HealthProfileUiState(
    val allergies: List<String> = emptyList()
)
