package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import java.util.regex.Pattern

/**
 * CERN DATA SERVICE v4.2 (SOVEREIGN).
 * AUTHORITY: ARCHITECT XILON.
 * v4.2: Fixed analyzeCollisionData return type and existence for UI compatibility.
 */
@Singleton
class CernDataService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val xilonProf: XilonProfManager,
    private val resourceAcquisition: ResourceAcquisitionService,
    private val blockchainNotary: BlockchainNotaryService,
    private val neuralLattice: XilonNeuralLatticeService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient()

    private val cernLhcStatusUrl = "https://op-webtools.web.cern.ch/vistar/get_vistar.php?vistar=LHC1"

    fun startMonitoring() {
        scope.launch {
            globalKnowledge.logEvent("CERN_LHC", "Sincronizare Legătură Cuantică Suverană...", 4)
            while (isActive) {
                try {
                    val rawData = fetchLhcRawData()
                    processLhcStatus(rawData)
                } catch (e: Exception) { Timber.e(e, "LHC Link Fault") }
                delay(300000) 
            }
        }
    }

    private suspend fun fetchLhcRawData(): String = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(cernLhcStatusUrl).header("User-Agent", "Mozilla/5.0").build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string() ?: "" else "OFFLINE"
            }
        } catch (e: Exception) { "CONNECTION_ERROR" }
    }

    private suspend fun processLhcStatus(html: String) {
        val isActive = html.contains("BEAM", ignoreCase = true) || html.contains("STABLE", ignoreCase = true)
        val energyMatch = Pattern.compile("(\\d+\\.\\d+)\\s*TeV").matcher(html)
        val currentEnergy = if (energyMatch.find()) energyMatch.group(1)?.toDoubleOrNull() ?: 13.6 else 13.6

        if (isActive) {
            val prompt = "[CERN_ANALYSIS] ENERGIE: $currentEnergy TeV. Analizează stabilitatea grilei."
            val analysis = neuralLattice.computeSovereignIntelligence(prompt)
            globalKnowledge.logEvent("CERN_LHC", "Analiză: $analysis", 4)
            resourceAcquisition.startAcquisition(useExternalNodes = true)
            xilonProf.recordDiscovery("CERN_COLLIDER", analysis, 5)
        }
    }

    /**
     * OBLIGATORIU PENTRU UI: Analizează datele și returnează un verdict String.
     */
    suspend fun analyzeCollisionData(): String = withContext(Dispatchers.Default) {
        val prompt = "Efectuează o analiză instantanee a sinergiei CERN-Marrow."
        return@withContext neuralLattice.computeSovereignIntelligence(prompt)
    }
}
