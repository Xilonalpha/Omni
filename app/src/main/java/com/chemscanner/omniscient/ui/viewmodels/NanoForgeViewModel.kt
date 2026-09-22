package com.chemscanner.omniscient.ui.viewmodels

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.data.dao.MaterialBlueprintDao
import com.chemscanner.omniscient.marrow.data.models.MaterialBlueprintEntity
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MainRepository
import com.chemscanner.omniscient.marrow.services.BlockchainNotaryService
import com.chemscanner.omniscient.marrow.services.GeminiService
import com.chemscanner.omniscient.marrow.services.LocalNeuralEngine
import com.chemscanner.omniscient.marrow.services.SovereignKnowledgeBase
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random

data class Atom(
    val id: Int,
    val type: String, 
    val position: Offset,
    val color: Int,
    val stability: Float = 1.0f 
)

data class NanoForgeUiState(
    val atoms: List<Atom> = emptyList(),
    val materialName: String = "Unknown Lattice",
    val stabilityIndex: Float = 0f,
    val hardness: Float = 0f, // 0-10 Mohs Scale Equivalent
    val conductivity: Float = 0f, // % IACS
    val thermalResistance: Float = 0f, // K
    val isTesting: Boolean = false,
    val aiMaterialReport: String? = null,
    val gridActive: Boolean = true,
    val selectedAtomType: String = "Carbon",
    val availableMaterials: Set<String> = setOf("Carbon", "Gold", "Silicon", "Iron", "Titanium", "Uranium", "Graphene"), 
    val currentGForce: Float = 0f 
)

@HiltViewModel
class NanoForgeViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val localAi: LocalNeuralEngine,
    private val ttsService: TextToSpeechService,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val mainRepository: MainRepository,
    private val materialBlueprintDao: MaterialBlueprintDao,
    private val notary: BlockchainNotaryService,
    private val sovereignKnowledge: SovereignKnowledgeBase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NanoForgeUiState())
    val uiState: StateFlow<NanoForgeUiState> = _uiState.asStateFlow()

    private var atomCounter = 0

    init {
        // UNLOCKED: No more scan requirement for base materials
        startInertialWatchdog()
    }

    private fun startInertialWatchdog() {
        viewModelScope.launch {
            globalKnowledge.realSignals.collectLatest { signals ->
                val gForce = signals.seismicIntensity
                _uiState.update { it.copy(currentGForce = gForce) }
                
                if (gForce > 1.2f && _uiState.value.atoms.isNotEmpty()) {
                    triggerStructuralFailure(gForce)
                }
            }
        }
    }

    private fun triggerStructuralFailure(force: Float) {
        _uiState.update { state ->
            val updatedAtoms = state.atoms.map { it.copy(stability = (it.stability - force * 0.1f).coerceAtLeast(0f)) }
            state.copy(atoms = updatedAtoms, stabilityIndex = (state.stabilityIndex - force * 0.05f).coerceAtLeast(0f))
        }
    }

    fun addAtom(position: Offset) {
        val type = _uiState.value.selectedAtomType
        
        val color = when (type) {
            "Carbon" -> 0xFF444444.toInt()
            "Silicon" -> 0xFF90A4AE.toInt()
            "Gold" -> 0xFFFFD600.toInt()
            "Iron" -> 0xFFBDBDBD.toInt()
            "Titanium" -> 0xFFE0E0E0.toInt()
            "Uranium" -> 0xFF76FF03.toInt()
            "Graphene" -> 0xFF212121.toInt()
            else -> 0xFF00E5FF.toInt()
        }
        
        val newAtom = Atom(atomCounter++, type, position, color)
        _uiState.update { it.copy(atoms = it.atoms + newAtom) }
        calculateMaterialProperties()
    }

    private fun calculateMaterialProperties() {
        val atoms = _uiState.value.atoms
        if (atoms.isEmpty()) return

        val count = atoms.size.toFloat()
        val carbonCount = atoms.count { it.type == "Carbon" || it.type == "Graphene" }
        val goldCount = atoms.count { it.type == "Gold" }
        val ironCount = atoms.count { it.type == "Iron" }
        val siliconCount = atoms.count { it.type == "Silicon" }
        val titaniumCount = atoms.count { it.type == "Titanium" }
        val uraniumCount = atoms.count { it.type == "Uranium" }

        // 1. HARDNESS (Enhanced with Titanium)
        val calculatedHardness = (carbonCount * 0.8f + ironCount * 0.6f + titaniumCount * 0.9f) / count * 10f
        
        // 2. CONDUCTIVITY (Gold & Graphene)
        val calculatedConductivity = (goldCount * 1.0f + carbonCount * 0.4f) / count * 100f
        
        // 3. THERMAL RESISTANCE (Uranium & Titanium impacts)
        val calculatedThermal = (carbonCount * 3500f + ironCount * 1800f + titaniumCount * 1900f + uraniumCount * 1400f) / count

        // 4. STABILITY INDEX
        val avgAtomStability = atoms.map { it.stability }.average().toFloat()
        val baseStability = (count * 0.05f).coerceAtMost(1.0f)
        
        _uiState.update { it.copy(
            stabilityIndex = baseStability * avgAtomStability,
            hardness = calculatedHardness.coerceIn(0.5f, 10.0f),
            conductivity = calculatedConductivity.coerceIn(0.1f, 100f),
            thermalResistance = calculatedThermal
        ) }
    }

    fun runStressTest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true) }
            ttsService.speak("Inițiere audit de materiale SCI-OS. Toate elementele sunt deblocate.")
            
            val state = _uiState.value
            val atomsStr = state.atoms.groupBy { it.type }.map { "${it.key}: ${it.value.size}" }.joinToString(", ")
            
            val prompt = """
                Ești ANALISTUL DE MATERIALE SCI-OS (Ana Istla). 
                Analiză Rețea Atomică (Sovereign Unlocked):
                - Compoziție: $atomsStr
                - Duritate: ${"%.1f".format(state.hardness)} Mohs
                - Conductibilitate: ${"%.1f".format(state.conductivity)}% IACS
                - Rezistență Termică: ${state.thermalResistance.toInt()} K
                - Stabilitate Structurală: ${(state.stabilityIndex * 100).toInt()}%
                
                Explică proprietățile acestui material rar și utilitatea sa în ingineria de Tip II Kardashev.
                Răspunde autoritar, scurt, în română.
            """.trimIndent()
            
            val report = if (localAi.isModelLoaded.value) localAi.generateResponse(prompt) else geminiService.generateContent(prompt)
            
            if (state.stabilityIndex > 0.1f) {
                val signature = notary.notarizeDiscovery("NANO_FORGE", "Created Lattice: $atomsStr")
                materialBlueprintDao.insertMaterial(
                    MaterialBlueprintEntity(
                        materialName = "OmniMaterial-${System.currentTimeMillis().toString().takeLast(4)}",
                        atomicComposition = atomsStr,
                        stabilityIndex = state.stabilityIndex,
                        maxGForceResisted = state.currentGForce,
                        notarizationHash = signature
                    )
                )
            }

            _uiState.update { it.copy(aiMaterialReport = report, isTesting = false) }
            ttsService.speak(report)
        }
    }

    fun selectAtomType(type: String) {
        _uiState.update { it.copy(selectedAtomType = type) }
        ttsService.speak("Sintetizator configurat pentru $type. Material deblocat.")
    }

    fun clearForge() {
        _uiState.update { it.copy(atoms = emptyList(), stabilityIndex = 0f, hardness = 0f, conductivity = 0f, thermalResistance = 0f) }
        atomCounter = 0
        ttsService.speak("Matrice atomică curățată.")
    }
}
