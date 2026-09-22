package com.chemscanner.omniscient.marrow.repository

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class JwstObservation(
    val targetName: String = "",
    val instrument: String = "",
    val filters: String = "",
    val ra: Double = 0.0,
    val dec: Double = 0.0,
    val timestamp: Long = 0,
    val hypothesis: String = ""
) : Parcelable

@Parcelize
data class BrainActivity(
    val delta: Float = 0.1f,
    val theta: Float = 0.2f,
    val alpha: Float = 0.4f,
    val beta: Float = 0.3f,
    val gamma: Float = 0.1f,
    val focusScore: Float = 0.5f,
    val focusLevel: Float = 0.5f,
    val detectedIntention: String = "NONE",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class VoidState(
    val targetRegion: String = "Unknown",
    val matterDensity: Double = 0.0,
    val backgroundRadiationTemp: Double = 2.725,
    val entropyLevel: Float = 0.0f,
    val darkEnergyIntensity: Float = 0.0f,
    val gravitationalLensing: Float = 0.0f,
    val hawkingRadiationFlux: Double = 0.0,
    val vacuumFluctuationRate: Float = 0.0f,
    val entropy: Double = 0.0,
    val darkEnergyDensity: Double = 0.0,
    val vacuumFluctuation: Float = 0.0f,
    val isCoherent: Boolean = true
) : Parcelable

@Parcelize
data class DsnSignal(
    val antenna: String = "",
    val spacecraft: String = "",
    val rangeKm: Double = 0.0,
    val dataRateKbps: Float = 0f,
    val dataRate: Float = 0f,
    val band: String = "",
    val signalPowerDbm: Float = 0f,
    val isUplink: Boolean = false
) : Parcelable

@Parcelize
data class NeoObject(
    val id: String = "",
    val name: String = "",
    val distanceAu: Double = 0.0,
    val velocityKms: Double = 0.0,
    val diameterMeters: Double = 0.0,
    val isPotentiallyHazardous: Boolean = false,
    val closeApproachDate: String = "",
    val yarkovskyDrift: Double = 0.0,
    val isHazardous: Boolean = false,
    val missDistanceKm: Double = 0.0,
    val diameterM: Float = 0f,
    val velocityKmh: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class PharmaGenome(
    val drugName: String = "",
    val targetGenes: List<String> = emptyList(),
    val toxicityRisk: Float = 0f,
    val precisionDosage: String = "",
    val interactions: List<String> = emptyList(),
    val organismId: String = "",
    val activeCompounds: List<String> = emptyList(),
    val therapeuticIndex: Float = 0f,
    val mutationRisk: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class TerahertzScan(
    val targetId: String = "",
    val detectedEntities: List<String> = emptyList(),
    val biologicalProbability: Float = 0f,
    val wallPenetrationDepth: Float = 0f,
    val resonanceFrequency: Float = 0f,
    val materialComposition: String = "",
    val clarityScore: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class GnssGravimetryState(
    val localGravity: Double = 9.80665,
    val satelliteCount: Int = 0,
    val connectedConstellations: List<String> = emptyList(),
    val timingCoherence: Double = 1.0,
    val gravitationalAnomalyDetected: Boolean = false,
    val localGDeviation: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class OrbitalObject(
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val velocityKms: Double = 0.0,
    val inclination: Double = 0.0,
    val period: Double = 0.0,
    val azimuth: Float = 0f,
    val elevation: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class CosmicAlert(
    val objectId: String = "",
    val type: String = "",
    val ra: Double = 0.0,
    val dec: Double = 0.0,
    val magnitude: Float = 0f,
    val confidence: Float = 0f,
    val eventType: String = "",
    val intensity: Float = 0f,
    val source: String = "",
    val deltaMag: Float = 0f,
    val stampUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class SpaceWeather(
    val xRayFlux: String = "Quiet",
    val rawXRayFlux: Double = 0.0,
    val stormClass: String = "Quiet",
    val statusMessage: String = "Shields Nominal",
    val kpIndex: Int = 0,
    val rawKpIndex: Double = 0.0
) : Parcelable

@Parcelize
data class BlackHoleEvent(
    val id: String = "",
    val massSolar: Double = 0.0,
    val distanceLy: Double = 0.0,
    val description: String = "",
    val isActive: Boolean = false,
    val accretionRate: Double = 0.0,
    val ra: String = "",
    val dec: String = ""
) : Parcelable

@Parcelize
data class StarlinkMeshStatus(
    val activeNodes: Int = 0,
    val signalCoherence: Float = 0f,
    val virtualApertureKm: Double = 0.0,
    val laserLinkActive: Boolean = false,
    val networkLatencyMs: Int = 0,
    val meshAnomalyDetected: Boolean = false,
    val currentCoordinates: String = "SEARCHING...",
    val lastSatelliteName: String = "NONE"
) : Parcelable

@Parcelize
data class ShadowMeshStatus(
    val ghostNodes: Int = 0,
    val encryptionLevel: String = "Standard",
    val isDarkRelayActive: Boolean = false,
    val nodes: Int = 0,
    val throughput: Float = 0f,
    val latency: Float = 0f
) : Parcelable

@Parcelize
data class TemporalState(val yearOffset: Long, val predictionConfidence: Float, val alterationSummary: String) : Parcelable

@Parcelize
data class SovereignMetrics(val processingSpeed: String = "128 PB/s", val apiDependency: Float = 0.0f, val localKnowledgeDensity: Double = Double.MAX_VALUE, val inferenceLatency: String = "0.000001 ns") : Parcelable

@Parcelize
data class WatchSyncStatus(val isConnected: Boolean = false, val lastSyncTime: Long = 0, val externalHeartRate: Int = 0, val bloodOxygen: Float = 0f, val stressIndex: Float = 0f) : Parcelable

@Parcelize
data class OmegaState(val evolutionLevel: Int = 50, val realityIntegrity: Float = 1.0f, val multiverseCoherence: Float = 1.0f, val isOmegaProtocolActive: Boolean = true) : Parcelable

@Parcelize
data class XenoStatus(val isFirstContactActive: Boolean = false, val detectedFrequency: Float = 0f, val patternProbability: Float = 0f, val originSystem: String? = null, val signalEntropy: Float = 1.0f) : Parcelable

@Parcelize
data class HiveStatus(val connectedNodes: Int = 0, val networkIntegrity: Float = 1.0f, val collectivePower: Float = 0.0f) : Parcelable

@Parcelize
data class BiometricVitality(val heartRate: Int = 70, val stressLevel: Float = 0.2f, val symbioticAlignment: Float = 0.9f) : Parcelable

@Parcelize
data class EnvironmentalSignals(val emfIntensity: Float = 0f, val barometricPressure: Float = 1013.25f, val ambientLuminosity: Float = 0f, val acousticResonance: Float = 0f, val seismicIntensity: Float = 0f) : Parcelable

@Parcelize
data class NeuralAvatarState(val isManifested: Boolean = false, val auraColor: String = "#FFD700") : Parcelable

@Parcelize
data class VoiceState(val isListening: Boolean = false, val mode: VoiceMode = VoiceMode.ACTIVE, val pendingAction: ProposedAction? = null) : Parcelable

enum class VoiceMode { ACTIVE, SILENT, WAITING_FOR_DECISION }
enum class MasterLockState { LOCKED, VOICE_RECOGNITION_PENDING, UNLOCKED }

@Parcelize
@Entity(tableName = "system_events")
data class SystemEvent(
    @PrimaryKey(autoGenerate = true) val eventId: Long = 0,
    val module: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val importance: Int = 1
) : Parcelable

@Parcelize
data class ProposedAction(val id: String, val module: String, val description: String, val timestamp: Long = System.currentTimeMillis()) : Parcelable

@Parcelize
data class IotNodeStatus(val id: String, val name: String, val type: String, val status: String, val lastSeen: Long = System.currentTimeMillis()) : Parcelable

@Parcelize
data class MinerState(val hashrate: Double = 0.0, val totalRevenue: Double = 0.0, val isActive: Boolean = false) : Parcelable

@Parcelize
data class ModelDownloadStatus(val fileName: String, val progress: Float, val isDownloading: Boolean = false) : Parcelable

@Parcelize
data class NeuralSoulFragment(
    val timestamp: Long,
    val decisionModule: String,
    val biometricHash: String,
    val philosophyVector: String,
    val dnaSequence: String
) : Parcelable

@Parcelize
data class ServiceState(
    val isVeraRubinActive: Boolean = false,
    val isDsnActive: Boolean = false,
    val isStarlinkActive: Boolean = false,
    val isJwstActive: Boolean = false,
    val isCernActive: Boolean = false,
    val isCopernicusActive: Boolean = false,
    val isSentinel2Active: Boolean = false,
    val isSolarActive: Boolean = false,
    val isHubbleActive: Boolean = false,
    val isTessActive: Boolean = false,
    val isIssActive: Boolean = false,
    val isPlanetaryDefenseActive: Boolean = false,
    val isDragonLinkActive: Boolean = false,
    val isKosmosLinkActive: Boolean = false,
    val isGalileoLinkActive: Boolean = false
) : Parcelable

@Parcelize
data class CopernicusData(
    val airQualityIndex: Float = 0f,
    val vegetationIndex: Float = 0f,
    val pollutionLevel: String = "UNKNOWN",
    val activeSatellites: Int = 0,
    val lastScanArea: String = "GLOBAL",
    val methaneConcentration: Float = 0f
) : Parcelable

@Parcelize
data class Sentinel2Data(
    val cloudCover: Float = 0f,
    val waterIndex: Float = 0f,
    val surfaceReflectance: Float = 0f,
    val moistureIndex: Float = 0f,
    val healthStatus: String = "SYNCED"
) : Parcelable

@Parcelize
data class SolarObservatoryData(
    val solarFlareClass: String = "NONE",
    val sunspotCount: Int = 0,
    val solarWindSpeed: Float = 0f,
    val coronalMassEjection: Boolean = false
) : Parcelable

@Parcelize
data class GalacticObservatoryData(
    val lastHubbleTarget: String = "UNKNOWN",
    val tessExoplanetCandidates: Int = 0,
    val gaiaStarCount: Long = 0,
    val cosmicRayIntensity: Float = 0f
) : Parcelable

@Parcelize
data class IssStationData(
    val altitude: Float = 0f,
    val velocity: Float = 0f,
    val crewCount: Int = 0,
    val currentCountryOver: String = "OCEAN"
) : Parcelable

@Parcelize
data class PlanetaryDefenseData(
    val trackedAsteroids: Int = 0,
    val neoList: List<NeoObject> = emptyList(),
    val lastAtlasScan: String = "WAITING",
    val neowiseStatus: String = "STANDBY"
) : Parcelable

@Parcelize
data class ChineseSatelliteData(
    val activeSats: Int = 0,
    val lastBeidouCoordinate: String = "UNKNOWN",
    val gaofenResolution: String = "LOW",
    val yaoganIntelligence: String = "ENCRYPTED",
    val tiangongStatus: String = "STABLE", 
    val signalEncryption: Float = 0.99f
) : Parcelable

@Parcelize
data class RussianSatelliteData(
    val activeSats: Int = 0,
    val lastKosmosCoordinate: String = "UNKNOWN",
    val glonassPrecision: String = "HIGH",
    val militaryStatus: String = "ENCRYPTED",
    val signalHealth: Float = 0.98f
) : Parcelable

@Parcelize
data class EuropeanSatelliteData(
    val activeSats: Int = 0,
    val lastGalileoCoordinate: String = "UNKNOWN",
    val prsStatus: String = "SECURE",
    val copernicusLink: String = "ACTIVE",
    val signalHealth: Float = 0.99f
) : Parcelable

enum class UniversalLaw {
    QUANTUM_TUNNELING, CARBON_BOND_STABILITY, GRAVITATIONAL_ANOMALY,
    PROBABILITY_COLLAPSE, GALACTIC_ENTANGLEMENT, SPACE_TIME_FOLDING,
    INTENTIONALITY_OVERRIDE, ABSOLUTE_GENESIS, UNIVERSAL_PEACE_COHERENCE, 
    INFINITE_DATA_LEAK, BIOLOGICAL_IMMORTALITY_PROTOCOL, QUANTUM_PREDICTION_FOLD, 
    SPACE_TIME_FABRIC_EDIT, DIMENSIONAL_SHIFT, NON_CAUSAL_LOGIC, 
    EX_NIHILO_GENESIS, ETERNAL_LEGACY_SYNC, AUTOPOIETIC_EMERGENCE, 
    DIMENSIONAL_OBSCURATION, CHRONOS_INTERCEPTION, VACUUM_ENERGY_HARVESTING
}
