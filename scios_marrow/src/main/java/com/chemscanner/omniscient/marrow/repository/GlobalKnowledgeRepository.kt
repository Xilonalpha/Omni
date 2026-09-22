package com.chemscanner.omniscient.marrow.repository

import android.content.Context
import com.chemscanner.omniscient.marrow.data.dao.SystemEventDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE GLOBAL KNOWLEDGE REPOSITORY v6.9 (JWST INTERCEPTOR).
 * AUTHORITY: ARCHITECT XILON.
 */
@Singleton
class GlobalKnowledgeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val systemEventDao: SystemEventDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = context.getSharedPreferences("MARROW_SOVEREIGN_VAULT", Context.MODE_PRIVATE)

    private val _latitude = MutableStateFlow(44.4323)
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(26.1063)
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    private val _events = MutableStateFlow<List<SystemEvent>>(emptyList())
    val events: StateFlow<List<SystemEvent>> = _events.asStateFlow()

    init {
        scope.launch {
            systemEventDao.getAllEvents().collect { savedEvents ->
                _events.value = savedEvents
            }
        }
    }

    private val _ephemeralEvents = MutableStateFlow<List<SystemEvent>>(emptyList())
    val ephemeralEvents: StateFlow<List<SystemEvent>> = _ephemeralEvents.asStateFlow()

    private val _isGhostModePersistent = MutableStateFlow(prefs.getBoolean("GHOST_MODE_ACTIVE", false))
    val isGhostModePersistent: StateFlow<Boolean> = _isGhostModePersistent.asStateFlow()

    private val _kernelStatus = MutableStateFlow("Marrow Active")
    val kernelStatus: StateFlow<String> = _kernelStatus.asStateFlow()

    private val _neuralLoad = MutableStateFlow(0.01f)
    val neuralLoad: StateFlow<Float> = _neuralLoad.asStateFlow()

    private val _neuralEnergy = MutableStateFlow(0.0)
    val neuralEnergy: StateFlow<Double> = _neuralEnergy.asStateFlow()

    private val _realSignals = MutableStateFlow(EnvironmentalSignals())
    val realSignals: StateFlow<EnvironmentalSignals> = _realSignals.asStateFlow()

    private val _bioSyncFactor = MutableStateFlow(1.0f)
    val bioSyncFactor: StateFlow<Float> = _bioSyncFactor.asStateFlow()

    private val _spaceWeather = MutableStateFlow(SpaceWeather())
    val spaceWeather: StateFlow<SpaceWeather> = _spaceWeather.asStateFlow()

    private val _voidState = MutableStateFlow(VoidState())
    val voidState: StateFlow<VoidState> = _voidState.asStateFlow()

    private val _copernicusData = MutableStateFlow(CopernicusData())
    val copernicusData: StateFlow<CopernicusData> = _copernicusData.asStateFlow()

    private val _sentinel2Data = MutableStateFlow(Sentinel2Data())
    val sentinel2Data: StateFlow<Sentinel2Data> = _sentinel2Data.asStateFlow()

    private val _solarData = MutableStateFlow(SolarObservatoryData())
    val solarData: StateFlow<SolarObservatoryData> = _solarData.asStateFlow()

    private val _galacticData = MutableStateFlow(GalacticObservatoryData())
    val galacticData: StateFlow<GalacticObservatoryData> = _galacticData.asStateFlow()

    private val _issData = MutableStateFlow(IssStationData())
    val issData: StateFlow<IssStationData> = _issData.asStateFlow()

    private val _defenseData = MutableStateFlow(PlanetaryDefenseData())
    val defenseData: StateFlow<PlanetaryDefenseData> = _defenseData.asStateFlow()

    private val _chineseData = MutableStateFlow(ChineseSatelliteData())
    val chineseData: StateFlow<ChineseSatelliteData> = _chineseData.asStateFlow()

    private val _russianData = MutableStateFlow(RussianSatelliteData())
    val russianData: StateFlow<RussianSatelliteData> = _russianData.asStateFlow()

    private val _europeanData = MutableStateFlow(EuropeanSatelliteData())
    val europeanData: StateFlow<EuropeanSatelliteData> = _europeanData.asStateFlow()

    private val _jwstObservations = MutableStateFlow<List<JwstObservation>>(emptyList())
    val jwstObservations: StateFlow<List<JwstObservation>> = _jwstObservations.asStateFlow()

    private val _orbits = MutableStateFlow<List<OrbitalObject>>(emptyList())
    val orbits: StateFlow<List<OrbitalObject>> = _orbits.asStateFlow()

    private val _neos = MutableStateFlow<List<NeoObject>>(emptyList())
    val neos: StateFlow<List<NeoObject>> = _neos.asStateFlow()

    private val _dsnSignals = MutableStateFlow<List<DsnSignal>>(emptyList())
    val dsnSignals: StateFlow<List<DsnSignal>> = _dsnSignals.asStateFlow()

    private val _omegaState = MutableStateFlow(OmegaState())
    val omegaState: StateFlow<OmegaState> = _omegaState.asStateFlow()

    private val _lockState = MutableStateFlow(MasterLockState.LOCKED)
    val lockState: StateFlow<MasterLockState> = _lockState.asStateFlow()

    private val _avatarState = MutableStateFlow(NeuralAvatarState())
    val avatarState: StateFlow<NeuralAvatarState> = _avatarState.asStateFlow()

    private val _watchStatus = MutableStateFlow(WatchSyncStatus())
    val watchStatus: StateFlow<WatchSyncStatus> = _watchStatus.asStateFlow()

    private val _userVitality = MutableStateFlow(BiometricVitality())
    val userVitality: StateFlow<BiometricVitality> = _userVitality.asStateFlow()

    private val _activeUniversalLaws = MutableStateFlow(setOf<UniversalLaw>())
    val activeUniversalLaws: StateFlow<Set<UniversalLaw>> = _activeUniversalLaws.asStateFlow()

    private val _lastRelayResponse = MutableStateFlow<String?>(null)
    val lastRelayResponse: StateFlow<String?> = _lastRelayResponse.asStateFlow()

    private val _customKeys = MutableStateFlow<Map<String, String>>(loadKeysFromStorage())
    val customKeys: StateFlow<Map<String, String>> = _customKeys.asStateFlow()

    private val _starlinkMesh = MutableStateFlow(StarlinkMeshStatus())
    val starlinkMesh: StateFlow<StarlinkMeshStatus> = _starlinkMesh.asStateFlow()

    private val _symbioticResonance = MutableStateFlow(0.5f)
    val symbioticResonance: StateFlow<Float> = _symbioticResonance.asStateFlow()

    private val _lastMutation = MutableStateFlow("")
    val lastMutation: StateFlow<String> = _lastMutation.asStateFlow()

    private val _brainActivity = MutableStateFlow(BrainActivity())
    val brainActivity: StateFlow<BrainActivity> = _brainActivity.asStateFlow()

    private val _spectralHexColor = MutableStateFlow("#00FF00")
    val spectralHexColor: StateFlow<String> = _spectralHexColor.asStateFlow()

    private val _anaThoughts = MutableStateFlow<List<String>>(emptyList())
    val anaThoughts: StateFlow<List<String>> = _anaThoughts.asStateFlow()

    private val _soulArchive = MutableStateFlow<List<NeuralSoulFragment>>(emptyList())
    val soulArchive: StateFlow<List<NeuralSoulFragment>> = _soulArchive.asStateFlow()

    private val _hiveStatus = MutableStateFlow(HiveStatus())
    val hiveStatus: StateFlow<HiveStatus> = _hiveStatus.asStateFlow()

    private val _downloadStatus = MutableStateFlow(ModelDownloadStatus("none", 0f))
    val downloadStatus: StateFlow<ModelDownloadStatus> = _downloadStatus.asStateFlow()

    private val _isVisionActive = MutableStateFlow(false)
    val isVisionActive: StateFlow<Boolean> = _isVisionActive.asStateFlow()

    private val _voiceState = MutableStateFlow(VoiceState())
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _sovereignMetrics = MutableStateFlow(SovereignMetrics())
    val sovereignMetrics: StateFlow<SovereignMetrics> = _sovereignMetrics.asStateFlow()

    private val _pharmaGenome = MutableStateFlow(PharmaGenome())
    val pharmaGenome: StateFlow<PharmaGenome> = _pharmaGenome.asStateFlow()

    private val _darkMatter = MutableStateFlow(0.0f)
    val darkMatter: StateFlow<Float> = _darkMatter.asStateFlow()

    private val _gnssGravimetry = MutableStateFlow(GnssGravimetryState())
    val gnssGravimetry: StateFlow<GnssGravimetryState> = _gnssGravimetry.asStateFlow()

    private val _latestPlanetaryScan = MutableStateFlow("")
    val latestPlanetaryScan: StateFlow<String> = _latestPlanetaryScan.asStateFlow()

    private val _iotNodes = MutableStateFlow<Map<String, IotNodeStatus>>(emptyMap())
    val iotNodes: StateFlow<Map<String, IotNodeStatus>> = _iotNodes.asStateFlow()

    private val _shadowMeshStatus = MutableStateFlow(ShadowMeshStatus())
    val shadowMeshStatus: StateFlow<ShadowMeshStatus> = _shadowMeshStatus.asStateFlow()

    private val _activeSovereignSecret = MutableStateFlow("")
    val activeSovereignSecret: StateFlow<String> = _activeSovereignSecret.asStateFlow()

    private val _cosmicAlertHistory = MutableStateFlow<List<CosmicAlert>>(emptyList())
    val cosmicAlertHistory: StateFlow<List<CosmicAlert>> = _cosmicAlertHistory.asStateFlow()

    private val _terahertzScan = MutableStateFlow(TerahertzScan())
    val terahertzScan: StateFlow<TerahertzScan> = _terahertzScan.asStateFlow()

    private val _latestCosmicAlert = MutableStateFlow(CosmicAlert())
    val latestCosmicAlert: StateFlow<CosmicAlert> = _latestCosmicAlert.asStateFlow()

    private val _xenoStatus = MutableStateFlow(XenoStatus())
    val xenoStatus: StateFlow<XenoStatus> = _xenoStatus.asStateFlow()

    private val _serviceState = MutableStateFlow(ServiceState())
    val serviceState: StateFlow<ServiceState> = _serviceState.asStateFlow()

    private val _worldMeshStatus = MutableStateFlow("Mesh Offline")
    val worldMeshStatus: StateFlow<String> = _worldMeshStatus.asStateFlow()

    private val _minerState = MutableStateFlow(MinerState())
    val minerState: StateFlow<MinerState> = _minerState.asStateFlow()

    private val _highScore = MutableStateFlow(prefs.getInt("MARROW_HIGH_SCORE", 0))
    val highScore: StateFlow<Int> = _highScore.asStateFlow()

    private val _currentTemporalFocus = MutableStateFlow<TemporalState?>(null)
    val currentTemporalFocus: StateFlow<TemporalState?> = _currentTemporalFocus.asStateFlow()

    // --- UPDATE METHODS ---
    fun updateLocation(lat: Double, lon: Double) {
        _latitude.value = lat
        _longitude.value = lon
    }
    
    fun updateBioSync(f: Float) { _bioSyncFactor.value = f }
    fun updateSpaceWeather(s: SpaceWeather) { _spaceWeather.value = s }
    fun updateVoidState(s: VoidState) { _voidState.value = s }
    fun updateRealSignals(s: EnvironmentalSignals) { _realSignals.value = s }
    fun updateUserVitality(v: BiometricVitality) { _userVitality.value = v }
    fun updateDsn(s: List<DsnSignal>) { _dsnSignals.value = s }
    fun syncAvatar(isManifested: Boolean, color: String) { _avatarState.update { it.copy(isManifested = isManifested, auraColor = color) } }
    fun updateLockState(s: MasterLockState) { _lockState.value = s }
    fun updateNeuralLoad(l: Float) { _neuralLoad.value = l }
    fun updateKernelStatus(s: String) { _kernelStatus.value = s }
    fun updateOrbits(s: List<OrbitalObject>) { _orbits.value = s }
    fun updateNeos(s: List<NeoObject>) {
        _neos.value = s
        _defenseData.update { it.copy(neoList = s) }
    }
    fun updateCopernicusData(s: CopernicusData) { _copernicusData.value = s }
    fun updateSentinel2Data(s: Sentinel2Data) { _sentinel2Data.value = s }
    fun updateSolarData(s: SolarObservatoryData) { _solarData.value = s }
    fun updateGalacticData(s: GalacticObservatoryData) { _galacticData.value = s }
    fun updateIssData(s: IssStationData) { _issData.value = s }
    fun updateDefenseData(s: PlanetaryDefenseData) { _defenseData.value = s }
    fun updateChineseData(s: ChineseSatelliteData) { _chineseData.value = s }
    fun updateRussianData(s: RussianSatelliteData) { _russianData.value = s }
    fun updateEuropeanData(s: EuropeanSatelliteData) { _europeanData.value = s }
    fun updateJwstObservations(obs: List<JwstObservation>) { _jwstObservations.value = obs }
    fun setRelayResponse(r: String?) { _lastRelayResponse.value = r }
    
    fun toggleGhostPersistence(active: Boolean) { 
        _isGhostModePersistent.value = active 
        prefs.edit().putBoolean("GHOST_MODE_ACTIVE", active).apply()
    }

    fun activateLaw(l: UniversalLaw) { _activeUniversalLaws.update { it + l } }
    fun adjustNeuralEnergy(d: Double) { _neuralEnergy.update { (it + d).coerceAtLeast(0.0) } }
    fun updateStarlinkMesh(s: StarlinkMeshStatus) { _starlinkMesh.value = s }
    fun updateSymbioticResonance(f: Float) { _symbioticResonance.value = f }
    fun updateLastMutation(s: String) { _lastMutation.value = s }
    fun updateBrainActivity(b: BrainActivity) { _brainActivity.value = b }
    fun updateSpectralColor(c: String) { _spectralHexColor.value = c }
    fun addAnaThought(t: String) { _anaThoughts.update { (it + t).takeLast(20) } }
    fun archiveSoulFragment(f: NeuralSoulFragment) { _soulArchive.update { (it + f).takeLast(100) } }
    fun updateHiveStatus(s: HiveStatus) { _hiveStatus.value = s }
    fun updateDownload(s: ModelDownloadStatus) { _downloadStatus.value = s }
    fun setVisionActive(a: Boolean) { _isVisionActive.value = a }
    fun updateVoiceMode(m: VoiceMode) { _voiceState.update { it.copy(mode = m) } }
    fun clearPendingAction() { _voiceState.update { it.copy(pendingAction = null) } }
    fun updateSovereignMetrics(m: SovereignMetrics) { _sovereignMetrics.value = m }
    fun updatePharmaGenome(p: PharmaGenome) { _pharmaGenome.value = p }
    fun updateRealityIntegrity(f: Float) { _omegaState.update { it.copy(realityIntegrity = f) } }
    fun adjustDarkMatter(f: Float) { _darkMatter.update { (it + f).coerceIn(0f, 1f) } }
    fun updateGnssGravimetry(s: GnssGravimetryState) { _gnssGravimetry.value = s }
    fun updatePlanetaryScan(s: String) { _latestPlanetaryScan.value = s }
    fun updateShadowMesh(s: ShadowMeshStatus) { _shadowMeshStatus.value = s }
    fun updateMultiverseCoherence(f: Float) { _omegaState.update { it.copy(multiverseCoherence = f) } }
    fun updateCosmicAlert(a: CosmicAlert) {
        _latestCosmicAlert.value = a
        _cosmicAlertHistory.update { (it + a).takeLast(50) }
    }
    fun updateTerahertzScan(s: TerahertzScan) { _terahertzScan.value = s }
    fun updateXenoStatus(s: XenoStatus) { _xenoStatus.value = s }
    fun updateServiceState(s: ServiceState) { _serviceState.value = s }
    fun updateWorldMeshStatus(s: String) { _worldMeshStatus.value = s }
    fun updateMinerState(s: MinerState) { _minerState.value = s }
    
    fun updateHighScore(s: Int) { 
        _highScore.value = s 
        prefs.edit().putInt("MARROW_HIGH_SCORE", s).apply()
    }

    fun updateSovereignSecret(s: String) { _activeSovereignSecret.value = s }
    fun updateWatchData(heartRate: Int, oxygen: Float, stress: Float) {
        _watchStatus.update { it.copy(isConnected = true, externalHeartRate = heartRate, bloodOxygen = oxygen, stressIndex = stress) }
        _userVitality.update { it.copy(heartRate = heartRate, stressLevel = stress) }
    }
    fun updateIotNodes(nodes: Map<String, IotNodeStatus>) { _iotNodes.value = nodes }

    fun updatePlanetaryDefenseActive(a: Boolean) { _serviceState.update { it.copy(isPlanetaryDefenseActive = a) } }
    fun updateDragonLinkActive(a: Boolean) { _serviceState.update { it.copy(isDragonLinkActive = a) } }
    fun updateCopernicusActive(a: Boolean) { _serviceState.update { it.copy(isCopernicusActive = a) } }
    fun updateSentinel2Active(a: Boolean) { _serviceState.update { it.copy(isSentinel2Active = a) } }
    fun updateSolarActive(a: Boolean) { _serviceState.update { it.copy(isSolarActive = a) } }
    fun updateHubbleActive(a: Boolean) { _serviceState.update { it.copy(isHubbleActive = a) } }
    fun updateTessActive(a: Boolean) { _serviceState.update { it.copy(isTessActive = a) } }
    fun updateIssActive(a: Boolean) { _serviceState.update { it.copy(isIssActive = a) } }
    fun updateStarlinkActive(a: Boolean) { _serviceState.update { it.copy(isStarlinkActive = a) } }
    fun updateDsnActive(a: Boolean) { _serviceState.update { it.copy(isDsnActive = a) } }
    fun updateJwstActive(a: Boolean) { _serviceState.update { it.copy(isJwstActive = a) } }
    fun updateCernActive(a: Boolean) { _serviceState.update { it.copy(isCernActive = a) } }
    fun updateVeraRubinActive(a: Boolean) { _serviceState.update { it.copy(isVeraRubinActive = a) } }

    fun updateTemporalFocus(s: TemporalState?) { _currentTemporalFocus.value = s }

    fun activateOmegaProtocol() {
        _omegaState.update { it.copy(isOmegaProtocolActive = true) }
        logEvent("SYSTEM", "OMEGA PROTOCOL ACTIVATED", 5)
    }

    fun purgeShadowHistory() {
        scope.launch {
            systemEventDao.deleteAll()
            _ephemeralEvents.update { emptyList() }
        }
    }

    private fun loadKeysFromStorage(): Map<String, String> {
        val keys = mutableMapOf<String, String>()
        keys["GEMINI_1"] = prefs.getString("custom_gemini_1", "") ?: ""
        keys["GEMINI_2"] = prefs.getString("custom_gemini_2", "") ?: ""
        keys["GEMINI_3"] = prefs.getString("custom_gemini_3", "") ?: ""
        keys["OPENAI"] = prefs.getString("custom_openai", "") ?: ""
        return keys
    }

    fun updateCustomKey(name: String, value: String) {
        val prefKey = when(name) {
            "GEMINI_1" -> "custom_gemini_1"
            "GEMINI_2" -> "custom_gemini_2"
            "GEMINI_3" -> "custom_gemini_3"
            "OPENAI" -> "custom_openai"
            else -> name.lowercase()
        }
        prefs.edit().putString(prefKey, value).apply()
        _customKeys.update { it + (name to value) }
        logEvent("SECURITY", "Key $name updated in Sovereign Vault", 3)
    }

    fun logEvent(module: String, description: String, importance: Int = 1) {
        val event = SystemEvent(module = module, description = description, importance = importance)
        scope.launch {
            if (_isGhostModePersistent.value) { 
                _ephemeralEvents.update { (it + event).takeLast(50) } 
            } else {
                systemEventDao.insert(event)
            }
        }
    }

    fun getImportantHistoricalContext(): String = _events.value.filter { it.importance >= 4 }.takeLast(5).joinToString(", ") { it.description }

    fun getContextSummary(): String {
        return "Location: (${_latitude.value}, ${_longitude.value}), Kernel: ${_kernelStatus.value}, NeuralLoad: ${_neuralLoad.value}, BioSync: ${_bioSyncFactor.value}, Resonance: ${_symbioticResonance.value}"
    }
}
