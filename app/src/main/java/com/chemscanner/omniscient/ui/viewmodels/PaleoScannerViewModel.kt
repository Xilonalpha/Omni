package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository // FIXED IMPORT
import com.chemscanner.omniscient.marrow.repository.MainRepository // FIXED IMPORT
import com.chemscanner.omniscient.marrow.services.GeminiService // FIXED IMPORT
import com.chemscanner.omniscient.marrow.services.TextToSpeechService // FIXED IMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaleoEra(
    val name: String,
    val description: String,
    val ageMillions: Double,
    val flora: List<String>,
    val fauna: List<String>,
    val historicalPressure: Float // in hPa
)

data class PaleoUiState(
    val currentEraIndex: Int = 0,
    val isScanning: Boolean = false,
    val portalActive: Boolean = false,
    val eraData: List<PaleoEra> = emptyList(),
    val aiHistoricalReport: String? = null,
    val currentPortalDepth: Float = 0f,
    val gapAnalysis: String? = null,
    val atmosphericViability: String = "Analiză în curs...",
    val calibrationSource: String? = null // Molecule used for dating
)

@HiltViewModel
class PaleoScannerViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val ttsService: TextToSpeechService,
    private val globalKnowledge: GlobalKnowledgeRepository, // ADDED: For Barometer
    private val mainRepository: MainRepository // ADDED: For Isotopic Dating
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaleoUiState())
    val uiState: StateFlow<PaleoUiState> = _uiState.asStateFlow()

    private var currentLocalPressure = 1013.25f

    init {
        _uiState.update { it.copy(
            eraData = listOf(
                PaleoEra("Anthropocen", "Era omului modern.", 0.3, listOf("Cultură agricolă"), listOf("Homo Sapiens"), 1013f),
                PaleoEra("Cenozoic", "Ascensiunea mamiferelor.", 23.0, listOf("Iarbă"), listOf("Mamut"), 1010f),
                PaleoEra("Cretacic", "Apogeul dinozaurilor.", 66.0, listOf("Magnolii"), listOf("T-Rex"), 1050f),
                PaleoEra("Jurasic", "Giganții erbivori.", 145.0, listOf("Ferigi"), listOf("Brachiosaurus"), 1100f),
                PaleoEra("Triasic", "Începutul Mezozoicului.", 252.0, listOf("Conifere"), listOf("Coelophysis"), 1080f),
                PaleoEra("Cambrian", "Explozia vieții marine.", 541.0, listOf("Alge"), listOf("Trilobiți"), 980f)
            )
        ) }
        observeAtmosphere()
    }

    private fun observeAtmosphere() {
        viewModelScope.launch {
            globalKnowledge.realSignals.collectLatest { signals ->
                currentLocalPressure = signals.barometricPressure
            }
        }
    }

    /**
     * POINT 1: ISOTOPIC CALIBRATION.
     * Uses the last scanned molecule to find the most likely era.
     */
    fun startDeepScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            
            // Look for a calibration anchor in scan history
            val latestScans = mainRepository.getAllScansSortedByDateDesc()
            val anchor = latestScans.firstOrNull()
            
            _uiState.update { it.copy(calibrationSource = anchor?.chemicalName ?: "Zgomot Cosmic") }
            
            ttsService.speak("Inițiere scanare temporală. Calibrez portalul folosind semnătura de ${anchor?.chemicalName ?: "fond"} ca ancoră izotopică.")
            delay(2500)
            
            // Point 2: Atmospheric Calculation
            analyzeViability()
            
            _uiState.update { it.copy(isScanning = false, portalActive = true) }
            val era = _uiState.value.eraData[_uiState.value.currentEraIndex]
            ttsService.speak("Portal stabilit în era ${era.name}.")
            
            animatePortal()
            requestEraAnalysis()
        }
    }

    private fun analyzeViability() {
        val era = _uiState.value.eraData[_uiState.value.currentEraIndex]
        val diff = Math.abs(currentLocalPressure - era.historicalPressure)
        val viability = if (diff < 50) "VIABILĂ: Presiune compatibilă." else "CRITICĂ: Atmosferă prea densă/rară."
        _uiState.update { it.copy(atmosphericViability = viability) }
    }

    fun analyzeEvolutionaryGap() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            val prompt = """
                Explică 'crepanta' evolutivă dintre dinozauri și oameni.
                Ține cont că Arhitectul operează la o presiune de $currentLocalPressure hPa.
                Cum ar fi supraviețuit biologia sa în acea perioadă de tranziție?
            """.trimIndent()

            try {
                val result = geminiService.generateContent(prompt)
                _uiState.update { it.copy(isScanning = false, gapAnalysis = result) }
                ttsService.speak(result)
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false) }
            }
        }
    }

    private fun animatePortal() {
        viewModelScope.launch {
            for (i in 0..100) {
                delay(20)
                _uiState.update { it.copy(currentPortalDepth = i / 100f) }
            }
        }
    }

    fun changeEra(index: Int) {
        _uiState.update { it.copy(currentEraIndex = index, aiHistoricalReport = null, gapAnalysis = null) }
        analyzeViability()
        ttsService.speak("Recalibrare portal către era ${_uiState.value.eraData[index].name}.")
        requestEraAnalysis()
    }

    private fun requestEraAnalysis() {
        viewModelScope.launch {
            val era = _uiState.value.eraData[_uiState.value.currentEraIndex]
            val prompt = "Ești un paleontolog Sci-OS. Era: ${era.name}. Presiune locală: $currentLocalPressure hPa. Viabilitate: ${_uiState.value.atmosphericViability}. Descrie mediul dincolo de portal."
            val report = geminiService.generateContent(prompt)
            _uiState.update { it.copy(aiHistoricalReport = report) }
            ttsService.speak(report)
        }
    }
}
