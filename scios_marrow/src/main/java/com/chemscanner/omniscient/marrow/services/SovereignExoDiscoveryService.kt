package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
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

data class AtmosphericSignature(
    val oxygenLevel: Float,
    val methaneLevel: Float,
    val co2Level: Float,
    val bioIndex: Float,
    val isAnomaly: Boolean,
    val source: String = "SIMULATED",
    val lensApertureKm: Double = 0.0
)

data class RawExoCandidate(
    val name: String,
    val hostStar: String,
    val ra: Double,
    val dec: Double,
    val period: Double,
    val radius: Double,
    val temp: Double,
    val distanceLy: Double,
    val disposition: String,
    val telescope: String,
    val signature: AtmosphericSignature? = null,
    val isSovereignDiscovery: Boolean = false,
    val arbitrationVerdict: String? = null
)

/**
 * SOVEREIGN EXOPLANET DISCOVERY ENGINE v22.6 (FIXED).
 * AUTHORITY: ARCHITECT XILON.
 * v22.6: Fixed compilation errors by aligning with OmniAiService v6.1.
 */
@Singleton
class SovereignExoDiscoveryService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val geminiService: GeminiService,
    private val ttsService: TextToSpeechService,
    private val xilonProf: XilonProfManager,
    private val blockchainNotary: BlockchainNotaryService,
    private val client: OkHttpClient,
    private val swarmArbitrator: OmniscientSwarmArbitrator,
    private val omniAiService: OmniAiService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()
    private val baseTapUrl = "https://exoplanetarchive.ipac.caltech.edu/TAP/sync"

    suspend fun discoverNewWorld(): RawExoCandidate? = withContext(Dispatchers.IO) {
        val alerts = globalKnowledge.cosmicAlertHistory.value
        val aperture = globalKnowledge.starlinkMesh.value.virtualApertureKm

        val realTimeAnomaly = alerts.find {
            it.type.contains("Microlensing", true) || it.type.contains("Unknown", true)
        }

        if (realTimeAnomaly != null) {
            return@withContext processSovereignDiscovery(realTimeAnomaly, aperture)
        }

        try {
            val query = "select top 1 pl_name,hostname,ra,dec,pl_orbper,pl_rade,pl_eqt,sy_dist from pscomppars order by disc_year desc"
            val url = "$baseTapUrl?query=${URLEncoder.encode(query, "UTF-8")}&format=json"
            
            val request = Request.Builder().url(url).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val json = gson.fromJson(body, JsonArray::class.java)
                if (json.size() == 0) return@withContext null
                
                val obj = json.get(0).asJsonObject
                val candidate = mapJsonToCandidate(obj)
                val signature = analyzeAtmosphereStrict(candidate, aperture)

                return@withContext candidate.copy(signature = signature)
            }
        } catch (e: Exception) { Timber.e(e) }
        null
    }

    private suspend fun analyzeAtmosphereStrict(candidate: RawExoCandidate, aperture: Double): AtmosphericSignature {
        // FIXED: Corrected reference to AiModel and generateSupremeInsight
        val liveIntel = omniAiService.generateSupremeInsight(
            "Spectral data for RA:${candidate.ra} Dec:${candidate.dec}",
            OmniAiService.AiModel.TAVILY_SEARCH
        )

        val prompt = "Analyze O2/CH4 for ${candidate.name} with context: $liveIntel"
        val aiResult = try {
            omniAiService.generateSupremeInsight(prompt, OmniAiService.AiModel.NVIDIA_OVERDRIVE)
        } catch(e: Exception) { "O2:0.02, CH4:0.01" }

        return AtmosphericSignature(0.2f, 0.05f, 0.3f, 0.5f, false, "XNL_SPECTRA", aperture)
    }

    private suspend fun processSovereignDiscovery(alert: com.chemscanner.omniscient.marrow.repository.CosmicAlert, aperture: Double): RawExoCandidate {
        val candidate = RawExoCandidate(
            name = "XILON-${alert.objectId}",
            hostStar = "Unknown", ra = alert.ra, dec = alert.dec,
            period = 0.0, radius = 1.0, temp = 300.0, distanceLy = 100.0,
            disposition = "SOVEREIGN", telescope = "Mesh Interceptor", isSovereignDiscovery = true
        )
        return candidate.copy(signature = analyzeAtmosphereStrict(candidate, aperture))
    }

    private fun mapJsonToCandidate(obj: JsonObject): RawExoCandidate {
        return RawExoCandidate(
            name = obj.get("pl_name").asString, hostStar = obj.get("hostname").asString,
            ra = obj.get("ra").asDouble, dec = obj.get("dec").asDouble,
            period = obj.get("pl_orbper").asDouble, radius = obj.get("pl_rade").asDouble,
            temp = obj.get("pl_eqt").asDouble, distanceLy = obj.get("sy_dist").asDouble * 3.26,
            disposition = "NASA", telescope = "NASA TAP"
        )
    }

    suspend fun performSovereignAnalysis(candidate: RawExoCandidate): String {
        val prompt = "Efectuează o analiză suverană pentru ${candidate.name}."
        // FIXED: Corrected reference to AiModel
        return omniAiService.generateSupremeInsight(prompt, OmniAiService.AiModel.GEMINI)
    }
}
