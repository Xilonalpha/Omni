package com.chemscanner.omniscient.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.data.models.ScanHistory
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.*
import com.chemscanner.omniscient.marrow.utils.ImageProcessing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * MOLECULAR SCANNER v28.1 (VISION-DRIVEN).
 * MISSION: Real-time chemical structure identification via ANA Vision.
 * AUTHORITY: ARCHITECT XILON.
 * v28.1: FIXED Gallery picking and Flash synchronization.
 */
@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val geminiService: GeminiService,
    private val ttsService: TextToSpeechService,
    private val notary: BlockchainNotaryService,
    private val pharmaService: PharmaGenomeService 
) : ViewModel() {

    sealed class ScanState {
        object Idle : ScanState()
        object Processing : ScanState()
        data class Success(val chemicalName: String, val formula: String, val report: String) : ScanState()
        data class Error(val message: String) : ScanState()
    }

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _flashState = MutableStateFlow(false)
    val flashState: StateFlow<Boolean> = _flashState.asStateFlow()

    fun toggleFlash() {
        _flashState.update { !it }
    }

    /**
     * CAPTURĂ DIN GALERIE.
     */
    fun analyzeFromBitmap(bitmap: Bitmap) {
        processImage(bitmap)
    }

    /**
     * CAPTURĂ REALĂ DIN CAMERĂ.
     */
    fun captureAndAnalyze(imageProxy: ImageProxy) {
        val bitmap = ImageProcessing.imageProxyToBitmap(imageProxy)
        imageProxy.close()
        processImage(bitmap)
    }

    private fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _scanState.value = ScanState.Processing
            ttsService.speak("Inițiez sinteza foton-moleculă. ANA analizează structura chimică.")

            try {
                val prompt = """
                    Ești ANA, expert în Chimie Moleculară. Analizează această imagine (etichetă de produs, formulă chimică sau substanță).
                    1. Identifică denumirea chimică principală.
                    2. Oferă formula moleculară.
                    3. Specifică riscul de toxicitate și un fapt interesant.
                    Răspunde în format: [NUME]| [FORMULA]| [RAPORT]. 
                    Fii monumentală, română.
                """.trimIndent()

                val analysis = geminiService.analyzeImageWithPersona(bitmap, prompt, "ANA - CHEMIST")
                
                if (analysis.contains("|")) {
                    val parts = analysis.split("|")
                    val name = parts[0].trim().replace("[", "").replace("]", "")
                    val formula = parts[1].trim()
                    val report = parts[2].trim()

                    _scanState.value = ScanState.Success(name, formula, report)
                    
                    pharmaService.analyzeDrug(name)
                    
                    globalKnowledge.logEvent("MOLECULAR_SCAN", "Identified: $name ($formula)", 5)
                    notary.notarizeDiscovery("MOLECULAR_IDENTIFICATION", "Substance: $name | Formula: $formula")
                    
                    ttsService.speak("Identificare reușită: $name. Formula moleculară: $formula. Am trimis datele către Pharma Genome.")
                } else {
                    throw Exception("Analiza vizuală nu a putut structura datele.")
                }

            } catch (e: Exception) {
                Timber.e(e, "Scanner Failure")
                _scanState.value = ScanState.Error("Eșec la identificarea vizuală: ${e.localizedMessage}")
                ttsService.speak("Nu am putut identifica structura moleculară. Vă rugăm să recalibrați unghiul.")
            }
        }
    }

    fun resetState() {
        _scanState.value = ScanState.Idle
    }
}
