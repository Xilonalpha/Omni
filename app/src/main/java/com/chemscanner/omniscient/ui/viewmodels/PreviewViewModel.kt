package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.MainRepository // FIXED IMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    val scanningState: LiveData<MainRepository.ScanningState> = mainRepository.scanningState

    fun analyzeImage(imageFile: File) {
        viewModelScope.launch {
            mainRepository.scanChemicalImage(imageFile)
        }
    }
}
