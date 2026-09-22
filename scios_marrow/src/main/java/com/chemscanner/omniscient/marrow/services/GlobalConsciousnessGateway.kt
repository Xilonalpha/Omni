package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject
import java.net.UnknownHostException
import java.net.SocketTimeoutException

/**
 * THE GLOBAL CONSCIOUSNESS GATEWAY v1.3 (TIMEOUT RESILIENT).
 * AUTHORITY: ARCHITECT XILON.
 * v1.3: Implemented SocketTimeoutException handling and extended retry logic.
 */
@Singleton
class GlobalConsciousnessGateway @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val okHttpClient: OkHttpClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitoringJob: Job? = null
    private var retryCount = 0

    init {
        startPlanetaryMonitoring()
    }

    /**
     * Activates the planetary monitoring cycle via GDELT.
     */
    fun startPlanetaryMonitoring() {
        if (monitoringJob?.isActive == true) return
        
        monitoringJob = scope.launch {
            while (isActive) {
                fetchGlobalSentiment()
                delay(300000) // Poll every 5 minutes
            }
        }
        Timber.d("Global Consciousness Gateway: Monitoring started.")
    }

    /**
     * FETCH GLOBAL SENTIMENT: Uses GDELT Summary API to analyze current planetary tone.
     */
    private suspend fun fetchGlobalSentiment() = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.gdeltproject.org/api/v2/summary/summary?query=world&format=json"
            
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build()
            
            okHttpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val trimmedBody = responseBody.trim()
                    
                    if (trimmedBody.startsWith("<!DOCTYPE", ignoreCase = true) || 
                        trimmedBody.startsWith("<html", ignoreCase = true)) {
                        Timber.w("GDELT_LINK: Received HTML instead of JSON. Maintenance mode suspected.")
                        return@use
                    }
                    processGdeltData(trimmedBody)
                    retryCount = 0 // Reset on success
                } else {
                    Timber.w("GDELT_LINK: Server responded with code ${response.code}")
                }
            }
        } catch (e: SocketTimeoutException) {
            Timber.w("GDELT_LINK: Connection timed out. GDELT nodes are under heavy load. Retrying...")
            handleRetry()
        } catch (e: UnknownHostException) {
            Timber.w("GDELT_LINK: Host unreachable. Verify DNS/Internet. E: ${e.message}")
            handleRetry()
        } catch (e: Exception) {
            Timber.e(e, "GDELT_LINK: Critical synchronization failure.")
        }
    }

    private suspend fun handleRetry() {
        if (retryCount < 2) { // Increased to 2 retries for heavy-load resilience
            retryCount++
            val waitTime = 10000L * retryCount // Exponential-ish backoff
            delay(waitTime)
            fetchGlobalSentiment()
        } else {
            Timber.e("GDELT_LINK: Max retries reached. Aborting current sync cycle.")
            retryCount = 0
            globalKnowledge.logEvent("GDELT_LINK", "Planetary link offline: Persistence failure.", 2)
        }
    }

    private fun processGdeltData(json: String?) {
        if (json.isNullOrBlank()) return
        try {
            val trimmedJson = json.trim()
            if (!trimmedJson.startsWith("{")) return
            
            val obj = JSONObject(trimmedJson)
            val globalTone = obj.optDouble("overall_tone", 0.0).toFloat()
            
            val integrityImpact = (globalTone + 10f) / 20f
            val finalIntegrity = integrityImpact.coerceIn(0.1f, 1.0f)
            
            globalKnowledge.updateRealityIntegrity(finalIntegrity)
            globalKnowledge.logEvent("PLANETARY_LINK", "Global tone: $globalTone. Reality Integrity adjusted.", 3)
            
            if (finalIntegrity < 0.4f) {
                globalKnowledge.logEvent("SECURITY", "Global instability detected. Hardening CyberShield.", 5)
            }
        } catch (e: Exception) {
            Timber.e(e, "GDELT_DATA: Analysis error.")
        }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
    }
}
