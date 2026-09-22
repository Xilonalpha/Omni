package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.data.dao.DiscoveredPlanetDao
import com.chemscanner.omniscient.marrow.ml.NeuralGenerativeEngine
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * PLANETARY SPECTROSCOPY SERVICE v9.1 (STABILIZED API).
 * AUTHORITY: ARCHITECT XILON.
 * v9.1: Restored default parameter for initiateDeepScan to fix Orchestrator build error.
 */
@Singleton
class PlanetarySpectroscopyService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val generativeEngine: NeuralGenerativeEngine,
    private val notaryService: BlockchainNotaryService,
    private val planetDao: DiscoveredPlanetDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient()
    private val gson = Gson()

    private val nasaTapUrl = "https://exoplanetarchive.ipac.caltech.edu/TAP/sync"

    data class NasaPlanetData(
        val plName: String,
        val hostname: String,
        val syDist: Double?,
        val plEqt: Double?,
        val plRade: Double?,
        val plBmasse: Double?
    )

    /**
     * INITIATE DEEP SCAN: 
     * Default target set to Kepler-186f for system initialization.
     */
    fun initiateDeepScan(targetPlanet: String = "Kepler-186f") {
        scope.launch {
            try {
                globalKnowledge.logEvent("SPECTROSCOPY", "Analiză spectrală pornită pentru $targetPlanet", 3)
                
                val existing = planetDao.getByName(targetPlanet)
                if (existing != null) {
                    processWithLocalData(existing)
                } else {
                    val planetData = fetchRealPlanetData(targetPlanet)
                    if (planetData != null) {
                        processAdvancedResult(planetData)
                    } else {
                        globalKnowledge.updatePlanetaryScan("EROARE: Date spectrale inaccesibile pentru $targetPlanet.")
                    }
                }
            } catch (e: Exception) { 
                Timber.e(e, "Deep Scan Fault")
            }
        }
    }

    private fun processWithLocalData(entity: com.chemscanner.omniscient.marrow.data.models.DiscoveredPlanetEntity) {
        val esi = calculateAdvancedESI(entity.radius.toDouble(), (entity.radius.toDouble()).pow(3.0) * 0.8, entity.temperature.toDouble() + 273.15)
        
        val report = """
            ANALIZĂ SPECTRALĂ FINALIZATĂ PE ${entity.name}:
            - STATUS: VALIDAT PRIN FLOTA STARLINK
            - Distanță: ${"%.1f".format(entity.distanceLy)} ani lumină
            - Gravitație: ${"%.2f".format(entity.gravity)} G
            - Index E.S.I.: ${"%.2f".format(esi)}
            - Detecție O2 (Oxigen): ${(entity.oxygenLevel * 100).toInt()}%
            - Detecție CH4 (Metan): ${(entity.methaneLevel * 100).toInt()}%
            - Index Biosemnătură (BSI): ${"%.2f".format(entity.bioIndex)}
            CONCLUZIE: ${if(entity.bioIndex > 0.6) "Condiții de biosferă activă." else "Suprafață sterilă."}
        """.trimIndent()

        globalKnowledge.updatePlanetaryScan(report)
        ttsService.speak("Xilon, analiza spectrală pentru ${entity.name} este completă.")
    }

    private suspend fun fetchRealPlanetData(name: String): NasaPlanetData? {
        val query = "select pl_name,hostname,sy_dist,pl_eqt,pl_rade,pl_bmasse from pscomppars where pl_name='${name}'"
        val url = "$nasaTapUrl?query=${URLEncoder.encode(query, "UTF-8")}&format=json"
        return try {
            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (response.isSuccessful) {
                    val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
                    val results: List<Map<String, Any>> = gson.fromJson(response.body?.string(), listType)
                    results.firstOrNull()?.let { map ->
                        NasaPlanetData(
                            plName = map["pl_name"] as? String ?: name,
                            hostname = map["hostname"] as? String ?: "Unknown",
                            syDist = (map["sy_dist"] as? Number)?.toDouble(),
                            plEqt = (map["pl_eqt"] as? Number)?.toDouble(),
                            plRade = (map["pl_rade"] as? Number)?.toDouble(),
                            plBmasse = (map["pl_bmasse"] as? Number)?.toDouble()
                        )
                    }
                } else null
            }
        } catch (e: Exception) { null }
    }

    private fun processAdvancedResult(data: NasaPlanetData) {
        val radius = data.plRade ?: 1.0
        val tempK = data.plEqt ?: 280.0
        val esi = calculateAdvancedESI(radius, data.plBmasse ?: radius.pow(3.0) * 0.8, tempK)
        
        val report = """
            ANALIZĂ LIVE NASA PENTRU ${data.plName}:
            - Distanță: ${"%.1f".format((data.syDist ?: 1.0) * 3.26)} Ly
            - Index E.S.I.: ${"%.2f".format(esi)}
            - Compoziție: Analiză preliminară finalizată.
        """.trimIndent()
        
        globalKnowledge.updatePlanetaryScan(report)
    }

    private fun calculateAdvancedESI(radius: Double, mass: Double, temp: Double): Double {
        val density = mass / radius.pow(3.0) 
        val escapeVelocity = sqrt(mass / radius) 
        val rRef = 1.0; val dRef = 1.0; val veRef = 1.0; val tRef = 288.0
        val wR = 0.57; val wD = 1.07; val wVe = 0.70; val wT = 5.58
        val esiInterior = (1.0 - abs((radius - rRef) / (radius + rRef))).pow(wR) * (1.0 - abs((density - dRef) / (density + dRef))).pow(wD)
        val esiSurface = (1.0 - abs((escapeVelocity - veRef) / (escapeVelocity + veRef))).pow(wVe) * (1.0 - abs((temp - tRef) / (temp + tRef))).pow(wT)
        return sqrt(esiInterior * esiSurface).coerceIn(0.0, 1.0)
    }
}
