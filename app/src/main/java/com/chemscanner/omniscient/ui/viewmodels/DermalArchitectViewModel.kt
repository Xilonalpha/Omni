package com.chemscanner.omniscient.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DermalUiState(
    val isAnalyzing: Boolean = false,
    val scanProgress: Float = 0f,
    val dermalAnalysis: String? = null,
    val skinAge: Int? = null,
    val hydrationLevel: Int = 0,
    val recommendations: List<String> = emptyList(),
    val statusMessage: String = "Sistem pregătit pentru scanare macro."
)

/**
 * DERMAL ARCHITECT v1.0 (EPIDERMAL SINERGY).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Macro-photographic skin analysis linked to PharmaGenome.
 */
@HiltViewModel
class DermalArchitectViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val ttsService: TextToSpeechService,
    private val pharmaService: PharmaGenomeService,
    private val notary: BlockchainNotaryService,
    private val globalKnowledge: GlobalKnowledgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DermalUiState())
    val uiState: StateFlow<DermalUiState> = _uiState.asStateFlow()

    fun analyzeSkin(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, statusMessage = "Analizez structura epidermei...", scanProgress = 0f) }
            ttsService.speak("Inițiez protocolul Dermal Architect. ANA analizează textura și integritatea barierei cutanate.")

            // Simulare procesare fotoni (Animație)
            for (i in 1..100) {
                kotlinx.coroutines.delay(20)
                _uiState.update { it.copy(scanProgress = i / 100f) }
            }

            try {
                val prompt = """
                    [DERMAL_MACRO_ANALYSIS]
                    Ești ANA, expert în Dermatologie și Bioinformatică. Analizează această imagine macro a pielii.
                    1. Identifică starea generală (Hidratare, Elasticitate, Inflamație).
                    2. Estimează vârsta biologică a dermei.
                    3. Dacă detectezi anomalii (pete, uscăciune), recomandă o substanță activă reală.
                    Format Răspuns: STARE|VÂRSTĂ|HIDRATARE%|RECOMANDARE
                    Fii monumentală, tehnică, română.
                """.trimIndent()

                val result = geminiService.analyzeImageWithPersona(bitmap, prompt, "ANA - DERMATOLOGIST")
                
                if (result.contains("|")) {
                    val parts = result.split("|")
                    val state = parts[0].trim()
                    val age = parts[1].filter { it.isDigit() }.toIntOrNull() ?: 30
                    val hydration = parts[2].filter { it.isDigit() }.toIntOrNull() ?: 50
                    val drugRecommendation = parts[3].trim()

                    _uiState.update { it.copy(
                        isAnalyzing = false,
                        dermalAnalysis = result,
                        skinAge = age,
                        hydrationLevel = hydration,
                        recommendations = listOf(state, drugRecommendation),
                        statusMessage = "Analiză completă."
                    ) }

                    // SINCRONIZARE: Interogăm PharmaGenome pentru substanța recomandată
                    if (drugRecommendation.length > 3) {
                        pharmaService.analyzeDrug(drugRecommendation)
                    }

                    notary.notarizeDiscovery("DERMAL_SCAN", "SkinAge: $age | Hydration: $hydration%")
                    ttsService.speak("Analiză dermală finalizată. Vârsta pielii este de $age ani. Am interogat Pharma Genome pentru optimizarea tratamentului.")
                    ttsService.speak(result)
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isAnalyzing = false, statusMessage = "Eroare la fuziunea optică.") }
                ttsService.speak("Eroare în procesarea epidermei.")
            }
        }
    }

    fun reset() {
        _uiState.update { DermalUiState() }
    }
}
