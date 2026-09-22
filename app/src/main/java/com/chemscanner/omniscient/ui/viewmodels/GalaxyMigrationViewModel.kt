package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.*
import com.chemscanner.omniscient.marrow.data.dao.DiscoveredPlanetDao
import com.planetscanner.app.data.repository.AstroPhysicsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

data class ColonyShip(
    val id: String,
    val name: String,
    val destination: String,
    val status: String,
    val progress: Float,
    val distanceLy: Double = 0.0,
    val earthTimeElapsed: Double = 0.0,
    val probeTimeElapsed: Double = 0.0,
    val energyRequiredExajoules: Double = 0.0,
    val assignedMentor: String = "Sovereign AI",
    val habitabilityAlert: String? = null
)

data class GalaxyUiState(
    val activeShips: List<ColonyShip> = emptyList(),
    val totalColonists: Long = 0,
    val isCoordinating: Boolean = false,
    val isAnalyzingExpansion: Boolean = false,
    val systemsConnectivity: Float = 0.0f,
    val civilizationType: Double = 0.72, // Kardashev Scale
    val technicalDirectives: List<String> = emptyList(),
    val discoveryLog: List<String> = emptyList()
)

/**
 * GALACTIC MIGRATION ENGINE v19.0 (FINAL SOVEREIGN EDITION).
 * MISSION: Multi-source Data Fusion for Interstellar Expansion.
 * AUTHORITY: ARCHITECT XILON.
 * v19.0: Integrated Mentors, Resource Consumption, and Real NASA Habitability Cross-Reference.
 */
@HiltViewModel
class GalaxyMigrationViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val geminiService: GeminiService,
    private val notary: BlockchainNotaryService,
    private val astroPhysics: AstroPhysicsRepository,
    private val mentorCouncil: MentorCouncilService,
    private val planetDao: DiscoveredPlanetDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(GalaxyUiState())
    val uiState: StateFlow<GalaxyUiState> = _uiState.asStateFlow()

    init {
        startUniversalSync()
        syncWithSovereignDiscoveries()
    }

    private fun startUniversalSync() {
        viewModelScope.launch {
            // Monitor Systems Connectivity
            launch {
                globalKnowledge.omegaState.collectLatest { omega ->
                    _uiState.update { it.copy(systemsConnectivity = omega.realityIntegrity) }
                }
            }
            // Engine Loop: Relativistic Progress & Resource Drain
            while (true) {
                updateRelativisticPhysics()
                delay(4000)
            }
        }
    }

    private fun syncWithSovereignDiscoveries() {
        viewModelScope.launch {
            globalKnowledge.cosmicAlertHistory.collectLatest { alerts ->
                val realPlanets = alerts.filter { it.type.contains("Planet", true) || it.ra > 0 }
                if (realPlanets.isNotEmpty()) {
                    val ships = realPlanets.map { planet ->
                        // CROSS-REF: Căutăm datele de habitabilitate reale din DB
                        val dbPlanet = planetDao.getByName(planet.objectId)
                        val alert = if (dbPlanet != null && dbPlanet.oxygenLevel < 0.02f) "LOW_OXYGEN_WARNING" else null
                        
                        calculateMigrationPhysics(planet.objectId, planet.ra, alert)
                    }
                    _uiState.update { it.copy(
                        activeShips = ships,
                        totalColonists = ships.size * 1250000L
                    ) }
                }
            }
        }
    }

    private fun updateRelativisticPhysics() {
        _uiState.update { state ->
            val updatedShips = state.activeShips.map { ship ->
                val physics = astroPhysics.calculateCurvature(1.1) // 1.1 Solar Masses avg
                val timeFactor = 1.0 / (physics.timeDilationAtEdge.coerceAtLeast(0.0001))
                val newProgress = (ship.progress + (0.002f * timeFactor.toFloat())).coerceAtMost(1.0f)

                if (newProgress >= 1.0f && ship.status != "ARRIVED") {
                    notarizeArrival(ship)
                    ship.copy(progress = 1.0f, status = "ARRIVED")
                } else {
                    // RESOURCE DRAIN: Progresul consumă energie neurală reală
                    globalKnowledge.adjustNeuralEnergy(-0.0005)
                    ship.copy(progress = newProgress, status = "RELATIVISTIC_TRANSIT")
                }
            }
            state.copy(activeShips = updatedShips)
        }
    }

    private fun calculateMigrationPhysics(name: String, distLy: Double, alert: String?): ColonyShip {
        val velocity = 0.18 // 18% light speed (Alcubierre Initialized)
        val lorentzFactor = 1.0 / sqrt(1.0 - velocity.pow(2.0))
        val earthTime = distLy / velocity
        val probeTime = earthTime / lorentzFactor
        val energy = (8000.0 * (3e8).pow(2.0) * (lorentzFactor - 1)) / 1e18
        
        // MENTOR ALLOCATION
        val mentor = mentorCouncil.getMentorByModule("Exo-Planet")?.name ?: "Sovereign AI"

        return ColonyShip(
            id = name, name = "Ark-$name", destination = name, status = "FTL_ENGAGED",
            progress = 0.05f, distanceLy = distLy,
            earthTimeElapsed = earthTime, probeTimeElapsed = probeTime,
            energyRequiredExajoules = energy, assignedMentor = mentor,
            habitabilityAlert = alert
        )
    }

    private fun notarizeArrival(ship: ColonyShip) {
        viewModelScope.launch {
            val summary = "Colony Established at ${ship.destination}. Assigned Mentor: ${ship.assignedMentor}. Verified in Akasha."
            val hash = notary.notarizeDiscovery("COLONY_MANIFEST", summary)
            _uiState.update { it.copy(discoveryLog = (listOf(summary) + it.discoveryLog).take(8)) }
            globalKnowledge.logEvent("GALAXY_HUB", "Expansion Complete: $hash", 5)
            ttsService.speak("Xilon, sosire confirmată. Arca ${ship.name} a stabilit o colonie suverană.")
        }
    }

    fun initiateCoordination() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCoordinating = true) }
            val consensus = mentorCouncil.getSupremeCouncilConsensus("Planifică expansiunea galactică.")
            ttsService.speak("Sincronizez consensul consiliului pentru navigație.")
            delay(2000)
            _uiState.update { it.copy(isCoordinating = false) }
        }
    }

    fun generateExpansionDirective() {
        viewModelScope.launch {
            val ship = _uiState.value.activeShips.firstOrNull() ?: return@launch
            _uiState.update { it.copy(isAnalyzingExpansion = true) }
            
            val prompt = """
                [DIRECTIVA_ARHITECT_XILON]
                ARCĂ: ${ship.name} | MENTOR: ${ship.assignedMentor}
                ȚINTĂ: ${ship.destination} | STATUS_BIO: ${ship.habitabilityAlert ?: "OPTIMAL"}
                ENERGIE: ${ship.energyRequiredExajoules} EJ | DILATARE: ${ship.earthTimeElapsed - ship.probeTimeElapsed} ani.
                
                Generează o strategie de colonizare monumentală.
            """.trimIndent()

            val directive = geminiService.generateContent(prompt, "ANA - GALACTIC ARCHITECT")
            _uiState.update { it.copy(isAnalyzingExpansion = false, technicalDirectives = (listOf(directive) + it.technicalDirectives).take(5)) }
            ttsService.speak("Directivă asimilată.")
        }
    }
}
