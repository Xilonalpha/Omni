package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.PharmaGenome
import com.chemscanner.omniscient.marrow.services.PharmaGenomeService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PharmaGenomeViewModel @Inject constructor(
    globalKnowledge: GlobalKnowledgeRepository,
    private val pharmaService: PharmaGenomeService
) : ViewModel() {

    val pharmaState: StateFlow<PharmaGenome> = globalKnowledge.pharmaGenome

    fun analyzeDrug(name: String) {
        viewModelScope.launch {
            pharmaService.analyzeDrug(name)
        }
    }
}
