package com.chemscanner.omniscient.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

data class BioAgeUiState(
    val isScanning: Boolean = false,
    val faceMeshActive: Boolean = false,
    val scanProgress: Float = 0f,
    val biologicalAge: Int? = null,
    val aiBioReport: String? = null,
    val longevityMarkers: List<String> = emptyList(),
    val statusMessage: String = "Sovereign Shield Active.",
    // Neither the local nor the cloud path uses a validated biometric age-estimation model -
    // both derive a number from a generic LLM's text response to an image-labeling prompt.
    // This must stay true so the UI never presents the number as a medical/clinical result.
    val isEstimateOnly: Boolean = true
)

/**
 * BIO-AGE CHRONOS v29.1 (STEALTH BIO-ANALYTICS).
 * AUTHORITY: ARCHITECT XILON.
 * v29.1: Forced Local Engine priority to prevent face-data extraction by external AI providers.
 */
@HiltViewModel
class BioAgeChronosViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val geminiService: GeminiService,
    private val localNeuralEngine: LocalNeuralEngine,
    private val shadowMesh: ShadowMeshService,
    private val ttsService: TextToSpeechService,
    private val notary: BlockchainNotaryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(BioAgeUiState())
    val uiState: StateFlow<BioAgeUiState> = _uiState.asStateFlow()

    fun startBioScan(faceBitmap: Bitmap? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, faceMeshActive = true, scanProgress = 0f, biologicalAge = null) }

            // Verificăm dacă suntem în mod suveran (Shadow Mesh activ)
            val isStealth = globalKnowledge.shadowMeshStatus.value.isDarkRelayActive

            if (isStealth) {
                ttsService.speak("Modul STEALTH activ. Analiză biometrică restricționată la nucleul local pentru protecția identității.")
            } else {
                ttsService.speak("Inițiez scanarea epigenetică.")
            }

            for (i in 1..100) {
                delay(20)
                _uiState.update { it.copy(scanProgress = i / 100f) }
            }

            if (faceBitmap != null) {
                // Dacă suntem în Stealth, folosim DOAR motorul local, chiar dacă avem internet
                if (isStealth) {
                    performLocalBioAnalysis(faceBitmap)
                } else {
                    performCloudBioAnalysis(faceBitmap)
                }
            }
        }
    }

    private suspend fun performLocalBioAnalysis(faceBitmap: Bitmap) {
        _uiState.update { it.copy(statusMessage = "Analiză LOCALĂ activă. Zero-Link către cloud.") }

        // Apel motor local (Gemma pe dispozitiv, prompt augmentat cu etichete ML Kit)
        val result = localNeuralEngine.analyzeImageLocal(faceBitmap, "Analyze facial biomarkers for age.")

        // Extragem vârsta din răspunsul modelului local, la fel ca pe calea cloud, în loc să
        // afișăm mereu aceeași valoare fixă indiferent de imagine sau de răspunsul motorului.
        val estimatedAge = result.filter { it.isDigit() }.take(2).toIntOrNull()

        _uiState.update { it.copy(
            isScanning = false,
            biologicalAge = estimatedAge,
            aiBioReport = "[LOCAL_PROTECTED]: $result",
            statusMessage = if (estimatedAge != null)
                "Analiză suverană finalizată pe dispozitiv (estimare, nu diagnostic medical)."
            else
                "Analiză suverană finalizată, dar motorul local nu a produs o valoare numerică clară."
        ) }
        notary.notarizeDiscovery("BIO_LOCAL_LOCK", "Face analysis completed with Zero-Link integrity.")
    }

    private suspend fun performCloudBioAnalysis(faceBitmap: Bitmap) {
        try {
            // Folosim Obfuscator-ul de prompt-uri înainte de trimitere
            val rawPrompt = "Analyze this face for biological age and oxidative stress."
            val securedPrompt = shadowMesh.secureAiPrompt(rawPrompt)

            val analysis = geminiService.analyzeImageWithPersona(faceBitmap, securedPrompt, "ANA - BIO-ARCHITECT")
            val estimatedAge = analysis.filter { it.isDigit() }.take(2).toIntOrNull() ?: 30

            _uiState.update { it.copy(
                isScanning = false,
                biologicalAge = estimatedAge,
                aiBioReport = analysis
            ) }

            notary.notarizeDiscovery("BIO_AGE_SYNC", "Encrypted Cloud Analysis complete.")
            ttsService.speak("Sinteză finalizată prin tunel securizat.")
        } catch (e: Exception) {
            _uiState.update { it.copy(isScanning = false, statusMessage = "Eroare securitate.") }
        }
    }

    fun resetScanner() {
        _uiState.update { BioAgeUiState() }
    }
}
