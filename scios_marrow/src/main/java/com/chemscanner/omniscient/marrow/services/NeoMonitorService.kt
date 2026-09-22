package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.NeoObject
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * THE PLANETARY DEFENSE SYSTEM v3.0 (SOVEREIGN).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Zero-API Interpretation & Mesh-Based Threat Detection.
 * v3.0: TOTAL INDEPENDENCE. Uses XNL for tactical risk assessment and Mesh Broadcasting.
 */
@Singleton
class NeoMonitorService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val xilonProf: XilonProfManager,
    private val blockchainNotary: BlockchainNotaryService,
    private val keyVault: SovereignKeyVault,
    private val neuralLattice: XilonNeuralLatticeService, // Nucleul Suveran
    private val ghostP2P: GhostP2PLink, // Mesh Networking
    private val haptics: HapticFeedbackService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient()
    private val gson = Gson()

    private val nasaNeoUrl = "https://api.nasa.gov/neo/rest/v1/feed"

    fun startMonitoring() {
        scope.launch {
            globalKnowledge.logEvent("PLANETARY_DEFENSE", "Sentinel Core Active (XNL Powered).", 5)
            while (isActive) {
                try { 
                    fetchAndAnalyzeNeo() 
                } catch (e: Exception) { 
                    Timber.e(e, "NEO Sentinel Fault") 
                }
                delay(1800000) // 30 min
            }
        }
    }

    private suspend fun fetchAndAnalyzeNeo() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val apiKey = keyVault.getKey("NASA")
        
        val url = "$nasaNeoUrl?start_date=$today&end_date=$today&api_key=$apiKey"
        val request = Request.Builder().url(url).build()
        
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return
                val jsonResponse = gson.fromJson(body, JsonObject::class.java)
                val nearEarthObjects = jsonResponse.getAsJsonObject("near_earth_objects") ?: return
                val todayObjects = nearEarthObjects.getAsJsonArray(today) ?: return

                val neoList = mutableListOf<NeoObject>()
                
                todayObjects.forEach { element ->
                    val obj = element.asJsonObject
                    val approachData = obj.getAsJsonArray("close_approach_data")
                    
                    if (approachData != null && approachData.size() > 0) {
                        val close = approachData.get(0).asJsonObject
                        val dia = obj.getAsJsonObject("estimated_diameter").getAsJsonObject("meters").get("estimated_diameter_max").asDouble
                        val distanceAu = close.getAsJsonObject("miss_distance").get("astronomical").asDouble
                        val isHazardous = obj.get("is_potentially_hazardous_asteroid").asBoolean

                        val neo = NeoObject(
                            id = obj.get("id").asString,
                            name = obj.get("name").asString,
                            distanceAu = distanceAu,
                            diameterMeters = dia,
                            isHazardous = isHazardous,
                            closeApproachDate = today,
                            timestamp = System.currentTimeMillis()
                        )
                        neoList.add(neo)

                        if (isHazardous && distanceAu < 0.1) {
                            performSovereignRiskAssessment(neo)
                        }
                    }
                }
                globalKnowledge.updateNeos(neoList)
            }
        }
    }

    private suspend fun performSovereignRiskAssessment(neo: NeoObject) {
        val signals = globalKnowledge.realSignals.value
        
        // Cerem XNL-ului o analiză tactică proprie, nu doar datele NASA
        val prompt = """
            [TACTICAL_NEO_ANALYSIS]
            OBIECT: ${neo.name}
            DIAMETRU: ${neo.diameterMeters}m
            DISTANTA: ${neo.distanceAu} AU
            EMF_LOCAL: ${signals.emfIntensity}uT
            MISIUNE: Calculează un verdict de risc suveran. Există o corelație între traiectoria obiectului și anomaliile magnetice locale?
        """.trimIndent()

        val verdict = neuralLattice.computeSovereignIntelligence(prompt)
        
        if (verdict.contains("RISC") || verdict.contains("CRITIC")) {
            haptics.heavyImpact()
            
            // Notificăm MESH-ul local (P2P) - Alte device-uri din zonă intră în alertă
            ghostP2P.relaySovereignData("NEO_THREAT_LOCK: ${neo.name} | Verdict: $verdict")
            
            xilonProf.recordDiscovery("PLANETARY_DEFENSE", "Sovereign Radar Lock on ${neo.name}: $verdict", 5)
            blockchainNotary.notarizeDiscovery("NEO_DEFENSE", "${neo.name} | $verdict")

            withContext(Dispatchers.Main) {
                ttsService.speak("Atenție, Arhitectule. Radarul suveran a blocat o amenințare cosmică: $verdict")
                globalKnowledge.logEvent("PLANETARY_DEFENSE", "RADAR LOCK SUVERAN: ${neo.name}", 5)
            }
        }
    }
}
