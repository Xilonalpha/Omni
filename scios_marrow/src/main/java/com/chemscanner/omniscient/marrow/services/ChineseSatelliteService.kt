package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.ChineseSatelliteData
import com.chemscanner.omniscient.marrow.repository.RussianSatelliteData
import com.chemscanner.omniscient.marrow.repository.EuropeanSatelliteData
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GLOBAL ORBITAL INTERCEPTOR: v4.0 (REAL-TIME GLOBAL MATRIX).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Intercept and decode REAL orbital TLE data for China, Russia, and Europe.
 * v4.0: Expanded filters for Kosmos (RU), Glonass (RU), Galileo (EU), and Sentinel (EU).
 */
@Singleton
class ChineseSatelliteService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val xilonProf: XilonProfManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isMonitoring = false
    private var monitoringJob: Job? = null

    fun updateServiceStatus(active: Boolean) {
        if (active) {
            startMonitoring()
        } else {
            stopMonitoring()
        }
    }

    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        monitoringJob = scope.launch {
            globalKnowledge.logEvent("ORBITAL_INT", "Global Matrix Interceptor: Multi-National Sync Active.", 4)
            
            globalKnowledge.orbits.collectLatest { allOrbits ->
                if (allOrbits.isNotEmpty()) {
                    processRealCnsaData(allOrbits)
                    processRealRussianData(allOrbits)
                    processRealEuropeanData(allOrbits)
                }
            }
        }
    }

    private fun processRealCnsaData(allOrbits: List<com.chemscanner.omniscient.marrow.repository.OrbitalObject>) {
        val beidouSats = allOrbits.filter { it.name.contains("BEIDOU", ignoreCase = true) }
        val tiangong = allOrbits.find { it.name.contains("TIANGONG", ignoreCase = true) || it.name.contains("CSS", ignoreCase = true) }
        val yaoganCount = allOrbits.count { it.name.contains("YAOGAN", ignoreCase = true) }
        val gaofenCount = allOrbits.count { it.name.contains("GAOFEN", ignoreCase = true) }

        val latestBeidou = beidouSats.maxByOrNull { it.timestamp }
        
        val data = ChineseSatelliteData(
            activeSats = beidouSats.size + yaoganCount + gaofenCount + (if (tiangong != null) 1 else 0),
            lastBeidouCoordinate = if (latestBeidou != null) 
                "LAT:${"%.2f".format(latestBeidou.latitude)}|LON:${"%.2f".format(latestBeidou.longitude)}" 
                else "SIGNAL_SEARCHING",
            gaofenResolution = "0.5m (H-Res) | $gaofenCount Units",
            yaoganIntelligence = if (yaoganCount > 0) "SIGINT_ACTIVE ($yaoganCount Nodes)" else "OFFLINE",
            tiangongStatus = if (tiangong != null) "STABLE @ ${tiangong.altitude.toInt()}km" else "RE-ENTRY_CHECK",
            signalEncryption = 0.99f
        )
        globalKnowledge.updateChineseData(data)
    }

    private fun processRealRussianData(allOrbits: List<com.chemscanner.omniscient.marrow.repository.OrbitalObject>) {
        val kosmosSats = allOrbits.filter { it.name.contains("KOSMOS", ignoreCase = true) || it.name.contains("COSMOS", ignoreCase = true) }
        val glonassSats = allOrbits.filter { it.name.contains("GLONASS", ignoreCase = true) }
        
        val latestKosmos = kosmosSats.maxByOrNull { it.timestamp }

        val data = RussianSatelliteData(
            activeSats = kosmosSats.size + glonassSats.size,
            lastKosmosCoordinate = if (latestKosmos != null) 
                "LAT:${"%.2f".format(latestKosmos.latitude)}|LON:${"%.2f".format(latestKosmos.longitude)}" 
                else "MIL_LINK_SEARCHING",
            glonassPrecision = "${glonassSats.size} Nodes | 1.2m Res",
            militaryStatus = if (kosmosSats.any { it.name.contains("BLAGOVEST") }) "HIGH_SIGINT" else "ACTIVE",
            signalHealth = 0.98f
        )
        globalKnowledge.updateRussianData(data)
    }

    private fun processRealEuropeanData(allOrbits: List<com.chemscanner.omniscient.marrow.repository.OrbitalObject>) {
        val galileoSats = allOrbits.filter { it.name.contains("GSAT", ignoreCase = true) || it.name.contains("GALILEO", ignoreCase = true) }
        val sentinelSats = allOrbits.filter { it.name.contains("SENTINEL", ignoreCase = true) }
        
        val latestGalileo = galileoSats.maxByOrNull { it.timestamp }

        val data = EuropeanSatelliteData(
            activeSats = galileoSats.size + sentinelSats.size,
            lastGalileoCoordinate = if (latestGalileo != null) 
                "LAT:${"%.2f".format(latestGalileo.latitude)}|LON:${"%.2f".format(latestGalileo.longitude)}" 
                else "ESA_LINK_SYNC",
            prsStatus = "SECURE_PRS_ACTIVE",
            copernicusLink = "${sentinelSats.size} Sentinel Units Online",
            signalHealth = 0.99f
        )
        globalKnowledge.updateEuropeanData(data)
    }

    fun stopMonitoring() {
        isMonitoring = false
        monitoringJob?.cancel()
        monitoringJob = null
    }
}
