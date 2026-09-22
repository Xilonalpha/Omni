package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MainRepository
import com.chemscanner.omniscient.marrow.services.*
import com.chemscanner.omniscient.marrow.repository.AnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * GREEN CHEMISTRY AUDITOR v25.0 (PUBCHEM & ECO-INFERENCE).
 * MISSION: Real-time sustainability audit using live chemical databases.
 * AUTHORITY: ARCHITECT XILON.
 * v25.0: ELIMINATED STATIC ANALYSYS. Linked to Live Intel and PubChem Data.
 */
@HiltViewModel
class GreenChemistryViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val omniAiService: OmniAiService,
    private val geminiService: GeminiService,
    private val notary: BlockchainNotaryService,
    private val ttsService: TextToSpeechService
) : ViewModel() {

    private val _analysisResult = MutableStateFlow<AnalysisResult?>(null)
    val analysisResult: StateFlow<AnalysisResult?> = _analysisResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var currentLux = 0f

    init {
        observeEnvironment()
    }

    private fun observeEnvironment() {
        viewModelScope.launch {
            globalKnowledge.realSignals.collectLatest { signals ->
                currentLux = signals.ambientLuminosity
            }
        }
    }

    /**
     * ANALIZĂ REALĂ: Caută date live despre toxicitate și impact ambiental.
     */
    fun analyzeChemical(chemicalName: String) {
        if (chemicalName.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _analysisResult.value = null

            try {
                // 1. CĂUTARE LIVE (PubChem / Internet 2026)
                val liveIntel = omniAiService.generateSupremeInsight(
                    "Find safety data, toxicity (LD50), and environmental impact for chemical: $chemicalName. Use PubChem standards. Output as summary.",
                    OmniAiService.AiModel.TAVILY_SEARCH
                )

                // 2. AUDIT ECO PRIN ANA (Gemini)
                val prompt = """
                    [GREEN_CHEMISTRY_AUDIT]
                    SUBSTANȚĂ: $chemicalName
                    DATE_BRUTE: $liveIntel
                    LUMINĂ_AMBIENTALĂ: $currentLux lx
                    
                    Misiune: Analizează datele de mai sus și oferă:
                    1. Scorul de Sustenabilitate (1-10).
                    2. Pericole reale pentru utilizator în lumina actuală ($currentLux lx).
                    3. O alternativă biodegradabilă reală.
                    Fii scurt, autoritar, română.
                """.trimIndent()

                val aiReport = geminiService.generateContent(prompt, "ANA - ECO ARCHITECT")

                // 3. CONSTRUIRE REZULTAT REAL
                val score = aiReport.substringAfter("Scor:").substringBefore("/").trim().toDoubleOrNull() ?: 5.0
                
                _analysisResult.value = AnalysisResult(
                    chemicalName = chemicalName,
                    sustainabilityScore = score,
                    greenAlternatives = emptyList(), // Pot fi populate din AI report
                    error = "LIVE_INTEL_EXTRACT:\n$aiReport"
                )
                
                ttsService.speak("Audit ecologic finalizat pentru $chemicalName. $aiReport")
                notary.notarizeDiscovery("GREEN_CHEM_AUDIT", "Substance: $chemicalName | Score: $score | Lux: $currentLux")

            } catch (e: Exception) {
                _analysisResult.value = AnalysisResult(
                    chemicalName = chemicalName,
                    sustainabilityScore = 0.0,
                    greenAlternatives = emptyList(),
                    error = "Eroare la auditul live: ${e.message}"
                )
            }
            
            _isLoading.value = false
        }
    }
}
