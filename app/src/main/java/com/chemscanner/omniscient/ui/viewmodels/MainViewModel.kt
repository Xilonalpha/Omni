package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MainRepository
import com.chemscanner.omniscient.marrow.repository.BiometricVitality
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val mainRepository: MainRepository
) : ViewModel() {
    
    // Stări cu frecvență mică
    val downloadStatus = globalKnowledge.downloadStatus
    val lockState = globalKnowledge.lockState
    val omegaState = globalKnowledge.omegaState
    val lastMutation = globalKnowledge.lastMutation
    val iotNodes = globalKnowledge.iotNodes
    val activeLaws = globalKnowledge.activeUniversalLaws
    val hiveStatus = globalKnowledge.hiveStatus
    val worldMeshStatus = globalKnowledge.worldMeshStatus
    val bioSync = globalKnowledge.bioSyncFactor
    val minerState = globalKnowledge.minerState

    // NEW: VITALITY HUD DATA (Real-time Heart Rate and Stress)
    val userVitality: StateFlow<BiometricVitality> = globalKnowledge.userVitality
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), globalKnowledge.userVitality.value)

    val kernelStatus = globalKnowledge.kernelStatus.combine(
        mainRepository.scanningState.asFlow()
    ) { status, scanState ->
        when (scanState) {
            is MainRepository.ScanningState.Processing -> "ANALIZĂ MOLECULARĂ ÎN CURS... | $status"
            is MainRepository.ScanningState.Success -> "DETECȚIE: ${scanState.result.chemicalName} | $status"
            is MainRepository.ScanningState.Error -> "EROARE SCANARE | $status"
            else -> status
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), globalKnowledge.kernelStatus.value)

    init {
        recalibrateSovereignKernel()
    }

    fun recalibrateSovereignKernel() {
        viewModelScope.launch {
            globalKnowledge.logEvent("KERNEL", "Recalibrare sistem declanșată de Arhitect.", 3)
            globalKnowledge.updateKernelStatus("KERNEL RECALIBRATED: SYNCING WITH MAIN REPOSITORY")
            val context = mainRepository.getContext()
            globalKnowledge.logEvent("SYSTEM", "Main Repository Link Active: ${context.packageName}", 1)
        }
    }

    val neuralLoad = globalKnowledge.neuralLoad
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.01f)

    val envSignals = globalKnowledge.realSignals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), globalKnowledge.realSignals.value)
}
