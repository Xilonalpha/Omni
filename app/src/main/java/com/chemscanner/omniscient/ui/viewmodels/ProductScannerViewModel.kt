package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.text.Text
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ProductScannerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ProductScannerUiState())
    val uiState: StateFlow<ProductScannerUiState> = _uiState.asStateFlow()

    fun onBarcodeScanned(barcode: Barcode) {
        val scannedValue = barcode.rawValue
        _uiState.update { it.copy(scannedBarcode = scannedValue) }
    }

    fun onTextRecognized(text: Text) {
        val recognizedText = text.text
        _uiState.update { it.copy(recognizedText = recognizedText) }
    }

    fun clearResults() {
        _uiState.update { it.copy(scannedBarcode = null, recognizedText = null) }
    }
}

data class ProductScannerUiState(
    val scannedBarcode: String? = null,
    val recognizedText: String? = null
)
