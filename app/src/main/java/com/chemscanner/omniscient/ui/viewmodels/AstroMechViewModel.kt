package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.data.dao.DiscoveredPlanetDao
import com.chemscanner.omniscient.marrow.repository.AstroMechRepository
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*
import kotlin.random.Random

data class ActiveProbe(val id: Int, var x: Double, var y: Double, val isWarpActive: Boolean, var angle: Double, var properTime: Double = 0.0, var isStretched: Boolean = false)

data class HawkingParticle(val id: Int, var x: Double, var y: Double, val vx: Double, val vy: Double, var life: Float = 1.0f)

data class SimCelestialBody(
    val name: String,
    val mass: Double,
    val radius: Double,
    val color: String,
    val type: String,
    var x: Double = 0.0,
    var y: Double = 0.0,
    var angle: Double = 0.0,
    var orbitDist: Double = 0.0,
    val ra: Double? = null,
    val dec: Double? = null
)

data class AstroMechUiState(
    val isBlackHoleMode: Boolean = false,
    val gravity: Float = 10f,
    val timeScale: Float = 1f,
    val planetTrail: List<Pair<Float, Float>> = emptyList(),
    val schwarzschildRadius: Float = 0.5f,
    val activeBodies: List<SimCelestialBody> = emptyList(),
    val activeProbes: List<ActiveProbe> = emptyList(),
    val hawkingParticles: List<HawkingParticle> = emptyList(),
    val currentRelativityFactor: Float = 1.0f,
    val vacuumInstability: Float = 0.0f,
    val stationTime: Double = 0.0,
    val totalTimeDilationEffect: Double = 0.0,
    val hawkingRadiationIntensity: Float = 0.0f,
    val lastDiscoveryName: String? = null,
    val deviceAzimuth: Float = 0f, // SKY-SYNC: Device orientation
    val devicePitch: Float = 0f,
    val isSkySyncActive: Boolean = false
)

@HiltViewModel
class AstroMechViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val astroRepo: AstroMechRepository,
    private val ttsService: TextToSpeechService,
    private val planetDao: DiscoveredPlanetDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AstroMechUiState())
    val uiState: StateFlow<AstroMechUiState> = _uiState.asStateFlow()

    private var internalStationTime = 0.0
    private var particleCounter = 0

    init {
        initializeSolarSystem()
        startOrbitalSimulation()
        observeDeviceOrientation()
    }

    private fun initializeSolarSystem() {
        val scenario = astroRepo.getScenarios().find { it.id == "real_solar_system" }
        scenario?.let { sc ->
            val simBodies = sc.bodies.mapIndexed { index, body ->
                SimCelestialBody(
                    name = body.name,
                    mass = body.mass,
                    radius = body.radius,
                    color = body.color,
                    type = body.type,
                    angle = Random.nextDouble() * 2.0 * PI,
                    orbitDist = if (body.type == "Star") 0.0 else 0.4 + (index * 0.25),
                    ra = body.ra,
                    dec = body.dec
                )
            }
            _uiState.update { it.copy(activeBodies = simBodies) }
        }
    }

    private fun observeDeviceOrientation() {
        viewModelScope.launch {
            // Placeholder: În realitate s-ar lega la un SensorManager în Activity
            // Aici doar pregătim câmpul în UI State pentru a fi gata de primire
        }
    }

    fun updateDeviceOrientation(azimuth: Float, pitch: Float) {
        _uiState.update { it.copy(deviceAzimuth = azimuth, devicePitch = pitch, isSkySyncActive = true) }
    }

    fun syncWithRealDiscovery() {
        viewModelScope.launch {
            val lastDiscovery = planetDao.getDiscoveredPlanets().first().firstOrNull()
            if (lastDiscovery != null) {
                val realGravity = (lastDiscovery.gravity * 10f).coerceIn(5f, 250f)
                _uiState.update { it.copy(gravity = realGravity, lastDiscoveryName = lastDiscovery.name) }
                ttsService.speak("Xilon, datele NASA sunt sincronizate. Gravitația este reglată.", "ro", true)
            }
        }
    }

    private fun startOrbitalSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(16)
                val state = _uiState.value
                val dt = 0.016 * state.timeScale
                internalStationTime += dt

                val relativity = 1.0f + (state.gravity / 50f)

                // 1. UPDATE ALL CELESTIAL BODIES (Real Keplerian Logic)
                val updatedBodies = state.activeBodies.map { body ->
                    if (body.type == "Star") {
                        body
                    } else {
                        val orbitalVelocity = 2.0 / (body.orbitDist.pow(1.5).coerceAtLeast(0.1))
                        val newAngle = body.angle + (dt / relativity) * orbitalVelocity

                        // IF SKY SYNC IS ACTIVE, we could override X/Y based on RA/Dec and device rotation
                        // For now, we maintain the orbital math but store the position
                        body.copy(
                            angle = newAngle,
                            x = cos(newAngle) * body.orbitDist,
                            y = sin(newAngle) * body.orbitDist
                        )
                    }
                }

                // 2. PROBES & SINGULARITY (Original Logic Restored)
                val currentProbes = state.activeProbes.toMutableList()
                val iterator = currentProbes.iterator()
                var massIncrease = 0f
                
                while (iterator.hasNext()) {
                    val probe = iterator.next()
                    val speed = if (probe.isWarpActive) 2.0 else 0.5
                    val distToCenter = sqrt(probe.x.pow(2) + probe.y.pow(2))

                    if (state.isBlackHoleMode && distToCenter < state.schwarzschildRadius * 0.1) {
                        iterator.remove()
                        massIncrease += 2.5f
                        ttsService.speak("Sondă asimilată.", "ro", true)
                        continue
                    }

                    val localDilation = 1.0 + (state.gravity / (distToCenter * 50.0).coerceAtLeast(1.0))
                    val probeDt = dt / localDilation
                    val newAngle = probe.angle + probeDt * speed
                    val orbitDist = 0.5 + (probe.id * 0.1)

                    probe.angle = newAngle
                    probe.x = cos(newAngle) * orbitDist
                    probe.y = sin(newAngle) * orbitDist
                    probe.properTime += probeDt
                    probe.isStretched = state.isBlackHoleMode && distToCenter < state.schwarzschildRadius * 0.4
                }

                // 3. HAWKING RADIATION (Original Logic)
                val currentParticles = state.hawkingParticles.toMutableList()
                var hawkingIntensity = 0f
                if (state.isBlackHoleMode) {
                    hawkingIntensity = (1.0f / (state.gravity / 100f)).coerceIn(0.1f, 1.0f)
                    if (Random.nextFloat() < hawkingIntensity * 0.3f) {
                        val angle = Random.nextDouble() * 2.0 * PI
                        currentParticles.add(HawkingParticle(particleCounter++, cos(angle) * 0.05, y = sin(angle) * 0.05, vx = cos(angle) * 0.5, vy = sin(angle) * 0.5))
                    }
                }
                val aliveParticles = currentParticles.map { it.copy(x = it.x + it.vx * dt, y = it.y + it.vy * dt, life = it.life - 0.02f * state.timeScale) }.filter { it.life > 0 }

                // 4. TRAIL (Linked to Earth or first non-star body)
                val earthPos = updatedBodies.find { it.name == "Earth" } ?: updatedBodies.getOrNull(1)
                val currentTrail = state.planetTrail.toMutableList()
                if (currentTrail.size > 100) currentTrail.removeAt(0)
                earthPos?.let { currentTrail.add(it.x.toFloat() to it.y.toFloat()) }

                _uiState.update { currentState ->
                    currentState.copy(
                        activeBodies = updatedBodies,
                        activeProbes = currentProbes,
                        hawkingParticles = aliveParticles,
                        planetTrail = currentTrail,
                        currentRelativityFactor = relativity,
                        stationTime = internalStationTime,
                        totalTimeDilationEffect = (internalStationTime - (currentProbes.firstOrNull()?.properTime ?: internalStationTime)),
                        hawkingRadiationIntensity = hawkingIntensity,
                        gravity = (state.gravity + massIncrease).coerceIn(5f, 250f)
                    )
                }
            }
        }
    }

    fun toggleBlackHole() {
        _uiState.update { it.copy(isBlackHoleMode = !it.isBlackHoleMode) }
        ttsService.speak(if (_uiState.value.isBlackHoleMode) "Singularitate activată." else "Sistem stabilizat.")
    }

    fun updateGravity(value: Float) { _uiState.update { it.copy(gravity = value) } }
    fun updateTimeScale(value: Float) { _uiState.update { it.copy(timeScale = value) } }

    fun launchSovereignProbe(isWarp: Boolean) {
        val newProbe = ActiveProbe(id = _uiState.value.activeProbes.size, x = 0.0, y = 0.0, isWarpActive = isWarp, angle = 0.0, properTime = 0.0)
        _uiState.update { it.copy(activeProbes = it.activeProbes + newProbe) }
        ttsService.speak(if (isWarp) "Sondă Warp activă." else "Sondă lansată.")
    }
}
