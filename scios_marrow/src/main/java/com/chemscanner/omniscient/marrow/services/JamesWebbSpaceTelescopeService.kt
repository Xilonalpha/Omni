package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JWST SPACE SERVICE v2.2 (SOVEREIGN).
 * AUTHORITY: ARCHITECT XILON.
 * v2.2: Fixed CommandCenter compatibility by restoring forceImmediateIntercept.
 */
@Singleton
class JamesWebbSpaceTelescopeService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val neuralLattice: XilonNeuralLatticeService,
    private val ttsService: TextToSpeechService,
    private val xilonProf: XilonProfManager,
    private val blockchainNotary: BlockchainNotaryService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient()

    fun startDeepSpaceSync() {
        scope.launch {
            globalKnowledge.logEvent("JWST_CORE", "Sincronizare Orizont de Evenimente (XNL Active).", 5)
            while (isActive) {
                try { fetchLatestCosmicData() } catch (e: Exception) { Timber.e(e) }
                delay(43200000) 
            }
        }
    }

    /**
     * REPARAT: Metodă cerută de CommandCenterActivity.
     */
    suspend fun forceImmediateIntercept() = withContext(Dispatchers.IO) {
        globalKnowledge.logEvent("JWST_MANUAL", "Forțare interceptare date JWST...", 4)
        fetchLatestCosmicData()
    }

    private suspend fun fetchLatestCosmicData() {
        val request = Request.Builder()
            .url("https://api.jwstapi.com/all/type/jpg?page=1")
            .addHeader("X-API-KEY", "2c57849e-646e-4f35-8656-785d038f830c")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return
                    val json = JSONObject(body)
                    val dataArray = json.optJSONArray("data") ?: return
                    
                    if (dataArray.length() > 0) {
                        val description = dataArray.getJSONObject(0).optString("details", "Deep Space Nebula")
                        val analysis = neuralLattice.computeSovereignIntelligence("Analizează JWST: $description")
                        
                        xilonProf.recordDiscovery("JWST_DEEP_SPACE", analysis, 5)
                        withContext(Dispatchers.Main) { ttsService.speak("Xilon, interceptare JWST reușită.") }
                    }
                }
            }
        } catch (e: Exception) { Timber.e(e) }
    }
}
