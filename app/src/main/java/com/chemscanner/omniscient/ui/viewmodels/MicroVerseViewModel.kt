package com.chemscanner.omniscient.ui.viewmodels

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MainRepository
import com.chemscanner.omniscient.marrow.repository.MicroVerseRepository
import com.chemscanner.omniscient.marrow.services.GeminiService
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

data class Nanobot(
    val id: Int,
    var x: Float,
    var y: Float,
    var targetX: Float,
    var targetY: Float,
    val efficiency: Float = 1.0f
)

@HiltViewModel
class MicroVerseViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val ttsService: TextToSpeechService,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val mainRepository: MainRepository,
    private val microVerseRepository: MicroVerseRepository // ACTIVATED: Added repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MicroVerseUiState())
    val uiState: StateFlow<MicroVerseUiState> = _uiState.asStateFlow()

    private var currentStressLevel = 0.2f

    init {
        loadMicroVerseData()
        observeArchitectVitals()
    }

    private fun loadMicroVerseData() {
        // ACTIVARE getCellStructures și getChallenges
        val structures = microVerseRepository.getCellStructures()
        val challenges = microVerseRepository.getChallenges()
        
        _uiState.update { it.copy(
            cellStructures = structures,
            challenges = challenges,
            currentStructure = structures.firstOrNull(),
            identifiedOrganelles = structures.firstOrNull()?.organelles ?: emptyList()
        ) }
        
        Timber.d("MicroVerse: Loaded ${structures.size} structures and ${challenges.size} challenges.")
    }

    private fun observeArchitectVitals() {
        viewModelScope.launch {
            globalKnowledge.userVitality.collectLatest { vitals ->
                currentStressLevel = vitals.stressLevel
                _uiState.update { it.copy(architectCalmness = 1.0f - vitals.stressLevel) }
            }
        }
    }

    fun updateZoom(newZoom: Float) = _uiState.update { it.copy(currentZoom = newZoom) }
    
    fun toggleInfectionMode(active: Boolean) {
        _uiState.update { it.copy(isInfectionModeActive = active) }
        if (active) {
            // ACTIVARE CellType.VIRUS: Simulăm detecția unei structuri virale
            val virusInfo = "Pathogen detected: ${MicroVerseRepository.CellType.VIRUS.name} strain alpha-X."
            globalKnowledge.logEvent("MICROVERSE", virusInfo, 4)
            ttsService.speak("Alertă bio-hazard. Detectat ${MicroVerseRepository.CellType.VIRUS.name}. Inițiez protocol de carantină.")
        }
    }

    fun toggleThermalVision() = _uiState.update { it.copy(isThermalVisionActive = !_uiState.value.isThermalVisionActive) }
    fun updateProbePosition(offset: Offset) = _uiState.update { it.copy(probePosition = offset) }

    fun scanPoint(x: Float, y: Float) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProbeAnalyzing = true) }
            val structureName = _uiState.value.currentStructure?.name ?: "Unknown"
            val prompt = "Ești Ana Aslan. Analizează zona X=$x, Y=$y în $structureName. Stare arhitect: ${if(currentStressLevel < 0.4) "Focalizat" else "Agitat"}. Ce observi?"
            val analysis = geminiService.generateContent(prompt)
            _uiState.update { it.copy(isProbeAnalyzing = false, aiCellAnalysis = analysis) }
            ttsService.speak(analysis)
        }
    }

    fun deployNanobots(x: Float, y: Float) {
        viewModelScope.launch {
            val scans = mainRepository.getAllScansSortedByDateDesc()
            val hasResources = scans.any { it.chemicalName.contains("Polymer", true) || it.chemicalName.contains("Carbon", true) }

            if (!hasResources) {
                ttsService.speak("Resurse insuficiente. Scanați un polimer sau carbon pentru a asambla nanoboții.")
                return@launch
            }

            val efficiency = (1.0f - currentStressLevel).coerceIn(0.1f, 1.0f)
            val newBots = List(8) { i ->
                Nanobot(id = i + Random.nextInt(1000), x = x, y = y, 
                    targetX = x + (Random.nextFloat() * 400 - 200 + (i * 10)) * efficiency, 
                    targetY = y + (Random.nextFloat() * 400 - 200 - (i * 10)) * efficiency,
                    efficiency = efficiency
                )
            }
            _uiState.update { it.copy(activeNanobots = it.activeNanobots + newBots) }
            val msg = if (efficiency > 0.8) "Sincronizare bio-neurală perfectă. Nanoboți lansați." else "Interferență de stres detectată. Eficiență redusă."
            ttsService.speak(msg)
        }
    }

    fun injectAntibiotic() {
        viewModelScope.launch {
            val scans = mainRepository.getAllScansSortedByDateDesc()
            val hasAntibiotic = scans.any { it.chemicalName.contains("Antibiotic", true) || it.chemicalName.contains("Penicillin", true) }

            if (!hasAntibiotic) {
                ttsService.speak("Protocol blocat. Nu ați asimilat nicio moleculă de tip antibiotic în baza de date Akasha.")
                return@launch
            }

            _uiState.update { it.copy(isInjectingAntibiotic = true) }
            ttsService.speak("Se injectează compusul asimilat. Decontaminare pornită.")
            delay(2500)
            _uiState.update { it.copy(isInjectingAntibiotic = false, isInfectionModeActive = false) }
            ttsService.speak("Integritate biologică restabilită prin sinergie moleculară.")
        }
    }

    fun analyzeCellWithAi() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiAnalyzing = true) }
            val structure = _uiState.value.currentStructure
            val analysis = geminiService.generateContent("Realizează o scanare SEM a structurii ${structure?.name} (${structure?.type}) sub influența biorezonanței arhitectului.")
            _uiState.update { it.copy(isAiAnalyzing = false, aiCellAnalysis = analysis) }
            ttsService.speak(analysis)
        }
    }
}

data class MicroVerseUiState(
    val currentZoom: Float = 100f,
    val identifiedOrganelles: List<String> = emptyList(),
    val isInfectionModeActive: Boolean = false,
    val isThermalVisionActive: Boolean = false,
    val isInjectingAntibiotic: Boolean = false,
    val isAiAnalyzing: Boolean = false,
    val isProbeAnalyzing: Boolean = false,
    val aiCellAnalysis: String? = null,
    val cellStructures: List<MicroVerseRepository.CellStructure> = emptyList(),
    val challenges: List<MicroVerseRepository.MicroscopyChallenge> = emptyList(),
    val currentStructure: MicroVerseRepository.CellStructure? = null,
    val activeNanobots: List<Nanobot> = emptyList(),
    val probePosition: Offset = Offset(500f, 500f),
    val architectCalmness: Float = 1.0f
)
