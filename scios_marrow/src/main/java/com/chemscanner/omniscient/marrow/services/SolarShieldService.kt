package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.utils.NetworkHelper
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Locale
import timber.log.Timber

/**
 * SOLAR SHIELD SERVICE v1.7.1 (STABILIZED).
 * AUTHORITY: ARCHITECT XILON.
 * v1.7.1: Fixed coroutine context error for recordDiscovery.
 */
@Singleton
class SolarShieldService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val blockchainNotary: BlockchainNotaryService,
    private val xilonProf: XilonProfManager,
    private val networkHelper: NetworkHelper
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient()
    private val gson = Gson()

    private val noaaXrayUrl = "https://services.swpc.noaa.gov/json/goes/primary/xray-fluxes.json"
    private val secondaryXrayUrl = "https://services.swpc.noaa.gov/json/goes/secondary/xray-fluxes.json"
    private val summaryXrayUrl = "https://services.swpc.noaa.gov/json/goes/primary/xrays-7-day.json"
    private val noaaKpUrl = "https://services.swpc.noaa.gov/products/noaa-scales.json"

    fun startMonitoring() {
        scope.launch {
            while (isActive) {
                if (networkHelper.isNetworkConnected()) {
                    try {
                        if (!tryFetchSolarData(noaaXrayUrl)) {
                            if (!tryFetchSolarData(secondaryXrayUrl)) {
                                tryFetchSolarData(summaryXrayUrl)
                            }
                        }
                        fetchKpIndex()
                    } catch (e: Exception) {
                        Timber.e(e, "SolarShield Fault")
                    }
                }
                delay(300000) 
            }
        }
    }

    private suspend fun tryFetchSolarData(url: String): Boolean {
        val request = Request.Builder().url(url).build()
        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return false
                    processSolarBody(body)
                    true
                } else false
            }
        } catch (e: Exception) { false }
    }

    private fun processSolarBody(body: String) {
        try {
            val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
            val data: List<Map<String, Any>> = gson.fromJson(body, listType)
            val latest = data.lastOrNull() ?: return
            
            val fluxValue = (latest["flux"] as? Double) ?: 0.0
            val stormClass = when {
                fluxValue >= 1e-4 -> "X-Class"
                fluxValue >= 1e-5 -> "M-Class"
                fluxValue >= 1e-6 -> "C-Class"
                else -> "Quiet"
            }

            val currentWeather = globalKnowledge.spaceWeather.value
            globalKnowledge.updateSpaceWeather(currentWeather.copy(
                xRayFlux = String.format(Locale.getDefault(), "%.2e", fluxValue),
                rawXRayFlux = fluxValue,
                stormClass = stormClass
            ))

            if (stormClass != "Quiet") {
                // FIXED: recordDiscovery is a suspend function, now launched in scope
                scope.launch {
                    xilonProf.recordDiscovery("SOLAR_ACTIVITY", "Class $stormClass detected. Flux: $fluxValue", 4)
                }
            }
        } catch (e: Exception) { }
    }

    private suspend fun fetchKpIndex() {
        val request = Request.Builder().url(noaaKpUrl).build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return
                    val rawKp = if (body.contains("current_value")) {
                        body.substringAfter("current_value\":").substringBefore(",").trim().toDoubleOrNull() ?: 0.0
                    } else 0.0
                    globalKnowledge.updateSpaceWeather(globalKnowledge.spaceWeather.value.copy(
                        kpIndex = rawKp.toInt(), rawKpIndex = rawKp
                    ))
                }
            }
        } catch (e: Exception) { }
    }
}
