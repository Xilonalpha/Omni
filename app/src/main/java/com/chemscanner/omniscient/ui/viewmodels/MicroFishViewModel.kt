package com.chemscanner.omniscient.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.services.GeminiService
import com.chemscanner.omniscient.marrow.services.GenomicDataAsimilator
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import com.planetscanner.app.data.models.MicroFishResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MicroFishUiState(
    val isScanning: Boolean = false,
    val result: MicroFishResult? = null,
    val realDnaSequence: String? = null,
    val analysisReport: String? = null,
    val error: String? = null
)

/**
 * MICRO-FISH SCANNER v23.0 (VISION-NCBI INTEGRATED).
 * MISSION: Use AI Vision to identify microscopic species and link with real DNA data.
 * AUTHORITY: ARCHITECT XILON.
 * v23.0: ELIMINATED SIMULATION. Using Gemini Vision for species identification.
 */
@HiltViewModel
class MicroFishViewModel @Inject constructor(
    private val ttsService: TextToSpeechService,
    private val geminiService: GeminiService,
    private val genomicAsimilator: GenomicDataAsimilator
) : ViewModel() {

    private val _uiState = MutableStateFlow(MicroFishUiState())
    val uiState: StateFlow<MicroFishUiState> = _uiState.asStateFlow()

    fun processMicroscopeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null, result = null, realDnaSequence = null) }
            ttsService.speak("Inițiez analiza vizuală Micro-FISH. ANA analizează morfologia specimenului.")

            try {
                // 1. ANALIZĂ VIZUALĂ REALĂ (Gemini Vision)
                val visionPrompt = """
                    Analizează această imagine microscopică. 
                    Identifică specia sau tipul de microorganism/celulă prezentat. 
                    Dacă este o analiză FISH, observă semnalele fluorescente și detectează posibile anomalii cromozomiale.
                    Răspunde în format JSON: {"species": "nume_stiintific", "health": "Optimal/Sub-optimal/Critical", "confidence": 0.XX, "anomaly": "descriere_sau_null"}
                """.trimIndent()

                val visionJson = geminiService.analyzeImageWithPersona(bitmap, visionPrompt, "ANA - MICROBIOLOGIST")
                
                // Extragem datele din răspunsul AI (simplificat)
                val speciesName = visionJson.substringAfter("\"species\": \"").substringBefore("\"")
                val health = visionJson.substringAfter("\"health\": \"").substringBefore("\"")
                val confidence = visionJson.substringAfter("\"confidence\": ").substringBefore(",").toFloatOrNull() ?: 0.9f
                val anomaly = visionJson.substringAfter("\"anomaly\": \"").substringBefore("\"").let { if (it == "null") null else it }

                // 2. ASIMILARE GENOMICĂ REALĂ (NCBI)
                val realRecord = genomicAsimilator.fetchRealSequence(speciesName)
                
                val scanResult = MicroFishResult(
                    speciesName = speciesName,
                    genomicMarkers = listOf("IDENTIFIED_BY_VISION"),
                    healthStatus = health,
                    confidence = confidence,
                    mutationDetected = anomaly != null,
                    anomalyDescription = anomaly
                )

                _uiState.update { it.copy(
                    isScanning = false,
                    result = scanResult,
                    realDnaSequence = realRecord?.sequence?.take(500),
                    analysisReport = "ANALIZĂ ANA: Specie detectată vizual. Secvență NCBI asimilată: ${realRecord?.id ?: "N/A"}"
                ) }

                ttsService.speak("Analiză finalizată. Specie identificată: $speciesName. Confidență vizuală: ${(confidence * 100).toInt()} procente.")
                
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, error = "Eșec analiză vizuală: ${e.localizedMessage}") }
                ttsService.speak("Eroare la procesarea optică a specimenului.")
            }
        }
    }

    fun resetScanner() {
        _uiState.update { MicroFishUiState() }
    }
}
