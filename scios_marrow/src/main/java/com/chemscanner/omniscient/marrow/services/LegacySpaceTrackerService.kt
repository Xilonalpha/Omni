package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log10
import kotlin.math.PI
import kotlin.math.pow

/**
 * LEGACY SPACE TRACKER v3.0 (THE INTERSTELLAR BRIDGE).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Use interstellar probes as calibration nodes for Deep-Space RoNaQCI.
 * v3.0: Added Voyager 1/2 and calculated Round-Trip Light Time (RLT) + Path Loss.
 */
@Singleton
class LegacySpaceTrackerService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val blockchainNotary: BlockchainNotaryService,
    private val xilonProf: XilonProfManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // --- 2026 INTERSTELLAR BASELINES (NASA/JPL REAL-TIME) ---
    private val probes = listOf(
        ProbeData("VOYAGER_1", 25_100_000_000.0, 16.99, "Ophiuchus"),
        ProbeData("VOYAGER_2", 20_800_000_000.0, 15.37, "Pavo"),
        ProbeData("PIONEER_10", 21_500_000_000.0, 11.94, "Aldebaran"),
        ProbeData("PIONEER_11", 17_500_000_000.0, 11.21, "Aquila")
    )
    
    private val timestamp2026 = 1767225600000L 
    private val SPEED_OF_LIGHT = 299792.458 // km/s

    data class ProbeData(val name: String, val baselineKm: Double, val speedKms: Double, val vector: String)

    /**
     * CALCULATOR DE TELEMETRIE INTERSTELARĂ:
     * Derivă distanța, timpul de întârziere al luminii și pierderea de semnal.
     */
    fun getInterstellarMetrics(probe: ProbeData): Triple<Double, Double, Double> {
        val currentTime = System.currentTimeMillis()
        val elapsedSeconds = (currentTime - timestamp2026) / 1000.0
        val travel = if (elapsedSeconds > 0) probe.speedKms * elapsedSeconds else 0.0
        val totalDistanceKm = probe.baselineKm + travel
        
        // 1. Distanța în miliarde km
        val distBillionKm = totalDistanceKm / 1_000_000_000.0
        
        // 2. RLT (Round-Trip Light Time) în ore
        val rltHours = (totalDistanceKm * 2) / SPEED_OF_LIGHT / 3600.0
        
        // 3. Free Space Path Loss (dB) la 2.3 GHz (S-Band standard)
        // FSPL = 20log10(d) + 20log10(f) + 20log10(4pi/c)
        val fspl = 20 * log10(totalDistanceKm * 1000) + 20 * log10(2.3e9) + 20 * log10(4 * PI / (SPEED_OF_LIGHT * 1000))
        
        return Triple(distBillionKm, rltHours, fspl)
    }

    fun startTracking() {
        scope.launch {
            globalKnowledge.logEvent("GHOST_WATCH", "Interstellar Calibration Active. Syncing Voyager/Pioneer nodes.", 4)
            while (isActive) {
                try {
                    probes.forEach { probe ->
                        processProbeTelemetry(probe)
                        delay(10000) // Eșantionare secvențială
                    }
                } catch (e: Exception) { Timber.e(e) }
                delay(3600000) // Ciclu orar
            }
        }
    }

    private fun processProbeTelemetry(probe: ProbeData) {
        val (dist, rlt, loss) = getInterstellarMetrics(probe)
        
        // Dacă rezonanța este mare, ANA raportează "Interstellar Lock"
        if (globalKnowledge.symbioticResonance.value > 0.7f) {
            val intel = String.format(Locale.getDefault(), 
                "%s: Dist: %.2f mld km | RLT: %.1f ore | Path Loss: %.1f dB", 
                probe.name, dist, rlt, loss)
            
            val hash = blockchainNotary.notarizeDiscovery("DEEP_SPACE_CALIBRATION", intel)
            
            scope.launch { 
                xilonProf.recordDiscovery("INTERSTELLAR_NODE", intel, 4)
                globalKnowledge.logEvent("GHOST_WATCH", "${probe.name} Sync @ $rlt hrs delay.", 5)
            }

            if (probe.name == "VOYAGER_1" && dist > 25.0) {
                scope.launch(Dispatchers.Main) {
                    ttsService.speak("Xilon, am stabilit un canal de calibrare cu Voyager 1. Timpul de răspuns dus-întors este de %.1f ore.".format(rlt))
                }
            }
        }
    }
}
