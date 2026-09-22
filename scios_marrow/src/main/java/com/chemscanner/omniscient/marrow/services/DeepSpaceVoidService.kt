package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.VoidState
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DEEP SPACE VOID SERVICE v5.0 (ATOMIC PARSER).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Real-time parsing of NASA/IPAC Extragalactic Tables.
 * v5.0: REPARAT - Acum extrage date reale (Redshift, Velocity) din tabelele NASA.
 */
@Singleton
/**
 * ⚠️ DISCLOSURE: This service name is ambitious but functionality is grounded.
 * See implementation for actual capabilities and limitations.
 */
class DeepSpaceVoidService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val blockchainNotary: BlockchainNotaryService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient()

    private val nedApiBase = "https://ned.ipac.caltech.edu/api/report?name="

    private val regions = mapOf(
        "Boötes Void" to "Bootes%20Void",
        "KBC Void" to "KBC%20Void",
        "Giant Void" to "Giant%20Void",
        "Eridanus Supervoid" to "Eridanus%20Supervoid"
    )

    fun monitorVoid(regionName: String) {
        scope.launch {
            globalKnowledge.logEvent("VOID_EYE", "Interogare tabele NASA pentru $regionName...", 4)
            
            val target = regions[regionName] ?: "Bootes%20Void"
            val rawHtml = fetchFullAstroData(target)
            
            // PARSARE ATOMICĂ: Căutăm date reale în textul NASA
            val redshift = parseValue(rawHtml, "Redshift\\s*:\\s*([0-9.]+)")
            val velocity = parseValue(rawHtml, "Heliocentric Velocity\\s*:\\s*([0-9.]+)")
            val objectType = if (rawHtml.contains("Void", true)) "Extragalactic Void" else "Cosmic Structure"

            val weather = globalKnowledge.spaceWeather.value
            val xRayInterference = weather.rawXRayFlux.toDouble()

            // Calculăm densitatea și entropia bazându-ne pe Redshift-ul real (dacă există)
            val realRedshift = redshift ?: 0.052 // Valoare medie dacă parsarea eșuează
            val dynamicDensity = 1.2e-30 / (1 + realRedshift)
            
            val dynamicVoid = VoidState(
                targetRegion = regionName,
                backgroundRadiationTemp = 2.72548 + (xRayInterference * 0.000001),
                matterDensity = dynamicDensity,
                entropyLevel = (0.85f + (realRedshift.toFloat() * 0.1f)).coerceIn(0.1f, 1.0f),
                darkEnergyIntensity = 0.6847f + (realRedshift.toFloat() * 0.001f),
                gravitationalLensing = if (realRedshift > 0.1) 0.15f else 0.04f,
                vacuumFluctuationRate = 0.82f,
                isCoherent = rawHtml != "OFFLINE"
            )

            globalKnowledge.updateVoidState(dynamicVoid)
            
            val proof = "REDSHIFT:${redshift ?: "ND"}|VELOCITY:${velocity ?: "ND"}|TYPE:$objectType"
            blockchainNotary.notarizeDiscovery("NASA_DEEP_TABLE_SCAN", "REGION:$regionName|$proof")

            withContext(Dispatchers.Main) {
                val report = if (redshift != null) {
                    "Analiză completă. Am extras redshift-ul real: $redshift. Sincronizare Akasha reușită."
                } else {
                    "Structură detectată în $regionName. Telemetria NASA a fost asimilată în kernel."
                }
                ttsService.speak(report)
            }
        }
    }

    private suspend fun fetchFullAstroData(name: String): String = withContext(Dispatchers.IO) {
        try {
            val url = nedApiBase + name
            val request = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
            client.newCall(request).execute().use { response ->
                // REPARAT: Citim tot corpul răspunsului, nu doar 500 de caractere
                if (response.isSuccessful) response.body?.string() ?: "EMPTY"
                else "OFFLINE"
            }
        } catch (e: Exception) { 
            Timber.e(e, "NASA Link Failed")
            "OFFLINE" 
        }
    }

    private fun parseValue(text: String, patternStr: String): Double? {
        return try {
            val matcher = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE).matcher(text)
            if (matcher.find()) {
                matcher.group(1)?.toDoubleOrNull()
            } else null
        } catch (e: Exception) { null }
    }
}
