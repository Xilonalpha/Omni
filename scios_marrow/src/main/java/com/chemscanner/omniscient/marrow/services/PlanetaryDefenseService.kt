package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.NeoObject
import com.chemscanner.omniscient.marrow.utils.NetworkHelper
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

/**
 * THE PLANETARY DEFENSE SHIELD v3.0 (SOVEREIGN).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Total Autonomy in Threat Assessment.
 * v3.0: TOTAL INDEPENDENCE. ELIMINATED GEMINI. Now powered by XNL.
 */
@Singleton
class PlanetaryDefenseService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val neuralLattice: XilonNeuralLatticeService, // Nucleul Suveran
    private val ttsService: TextToSpeechService,
    private val haptics: HapticFeedbackService,
    private val xilonProf: XilonProfManager,
    private val networkHelper: NetworkHelper,
    private val gson: Gson,
    private val keyVault: SovereignKeyVault
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient()
    private var defenseJob: Job? = null

    fun updateServiceStatus(active: Boolean) {
        if (active) {
            scope.launch(Dispatchers.Main) {
                ttsService.speak("Scutul suveran este activ. Monitorizăm amenințările fără intermediari.")
            }
            startMonitoring()
        } else { stopMonitoring() }
    }

    private fun startMonitoring() {
        defenseJob?.cancel()
        defenseJob = scope.launch {
            while (isActive) {
                try {
                    fetchRealNasaData()
                } catch (e: Exception) { Timber.e(e, "Planetary Shield Disturbance") }
                delay(1800000) // 30 min
            }
        }
    }

    private fun stopMonitoring() {
        defenseJob?.cancel()
        defenseJob = null
    }

    private suspend fun fetchRealNasaData() = withContext(Dispatchers.IO) {
        if (!networkHelper.isNetworkConnected()) return@withContext

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val apiKey = keyVault.getKey("NASA")

        val url = "https://api.nasa.gov/neo/rest/v1/feed?start_date=$today&end_date=$today&api_key=$apiKey"
        
        val request = Request.Builder().url(url).build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val nearEarthObjectsJson = json.getAsJsonObject("near_earth_objects")?.getAsJsonArray(today)
                    
                    if (nearEarthObjectsJson != null) {
                        val fullNeoList = mutableListOf<NeoObject>()
                        nearEarthObjectsJson.forEach { element ->
                            val neo = element.asJsonObject
                            val closeData = neo.getAsJsonArray("close_approach_data")
                            if (closeData != null && closeData.size() > 0) {
                                val approach = closeData.get(0).asJsonObject
                                val distKm = approach.getAsJsonObject("miss_distance").get("kilometers").asDouble
                                val velKms = approach.getAsJsonObject("relative_velocity").get("kilometers_per_second").asDouble
                                val diaMax = neo.getAsJsonObject("estimated_diameter").getAsJsonObject("meters").get("estimated_diameter_max").asDouble

                                fullNeoList.add(NeoObject(
                                    id = neo.get("id").asString,
                                    name = neo.get("name").asString,
                                    distanceAu = distKm / 149597870.7, 
                                    velocityKms = velKms, 
                                    diameterMeters = diaMax, 
                                    isHazardous = neo.get("is_potentially_hazardous_asteroid").asBoolean,
                                    closeApproachDate = today,
                                    timestamp = System.currentTimeMillis()
                                ))
                            }
                        }
                        
                        val sortedList = fullNeoList.sortedBy { it.distanceAu }
                        globalKnowledge.updateNeos(sortedList)
                        sortedList.firstOrNull()?.let { auditSovereignThreat(it) }
                    }
                }
            }
        } catch (e: Exception) { Timber.e(e, "NASA SSD Sync Fault") }
    }

    private suspend fun auditSovereignThreat(neo: NeoObject) {
        if (!neo.isHazardous) return

        val signals = globalKnowledge.realSignals.value
        val prompt = """
            [AUDIT_APARARE_PLANETARA]
            THREAT: ${neo.name}
            DISTANTA: ${"%.0f".format(neo.missDistanceKm)} km
            VITEZA: ${"%.2f".format(neo.velocityKms)} km/s
            EFECT_MAGNETIC: ${signals.emfIntensity} uT
            
            MISIUNE: Evaluează riscul real folosind logica ta suverană. 
            Generează o recomandare tactică pentru Arhitectul Xilon. 
            Răspunde monumental în Română.
        """.trimIndent()

        try {
            val report = neuralLattice.computeSovereignIntelligence(prompt)
            globalKnowledge.logEvent("PLANETARY_DEFENSE", "Audit Suveran: $report", 5)

            withContext(Dispatchers.Main) {
                haptics.heavyImpact()
                ttsService.speak(report)
            }
            xilonProf.recordDiscovery("THREAT_AUDIT_SOVEREIGN", report, 5)
        } catch (e: Exception) { }
    }
}
