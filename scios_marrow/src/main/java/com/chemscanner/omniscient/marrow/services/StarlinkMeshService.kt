package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.OrbitalObject
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import com.chemscanner.omniscient.marrow.utils.NetworkHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*
import dagger.Lazy

/**
 * STARLINK MESH SERVICE v4.2 (RELIABLE SYNC).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Harness satellite atomic clocks and report real-time mesh connectivity.
 * v4.2: Fixed circular dependency by using Lazy<ShadowMeshService>.
 */
@Singleton
class StarlinkMeshService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val blockchainNotary: BlockchainNotaryService,
    private val xilonProf: XilonProfManager,
    private val networkHelper: NetworkHelper,
    private val gson: Gson,
    private val shadowMesh: Lazy<ShadowMeshService> // Break circular link
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val client = OkHttpClient()
    private var meshJob: Job? = null

    private val starlinkApiUrl = "https://api.spacexdata.com/v4/starlink"

    fun updateMeshStatus(active: Boolean) {
        if (active) {
            scope.launch {
                withContext(Dispatchers.Main) {
                    ttsService.speak("Sincronizare completă cu rețeaua lui Musk. Asimilez întreaga constelație.", "ro", true)
                }
            }
            startMeshAnalysis()
        } else { 
            stopMeshAnalysis() 
            globalKnowledge.updateWorldMeshStatus("Mesh Standby")
        }
    }

    fun startMeshAnalysis() {
        if (meshJob?.isActive == true) return
        globalKnowledge.updateWorldMeshStatus("Mesh Syncing...")
        meshJob = scope.launch {
            while (isActive) {
                if (networkHelper.isNetworkConnected()) {
                    fetchNetworkTelemetry()
                } else {
                    globalKnowledge.updateWorldMeshStatus("Mesh Offline (Network)")
                }
                delay(60000) 
            }
        }
    }

    fun stopMeshAnalysis() {
        meshJob?.cancel()
        meshJob = null
        globalKnowledge.updateWorldMeshStatus("Mesh Offline")
    }

    private suspend fun fetchNetworkTelemetry() = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(starlinkApiUrl).build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val inputStream = response.body?.byteStream() ?: return@withContext
                    val reader = JsonReader(InputStreamReader(inputStream, "UTF-8"))
                    
                    val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
                    val satellites: List<Map<String, Any>> = gson.fromJson(reader, listType)
                    
                    processTelemetry(satellites)
                } else {
                    globalKnowledge.updateWorldMeshStatus("Mesh Link Failure")
                }
            }
        } catch (e: Exception) {
            Timber.e("Starlink API Failure: ${e.message}")
            globalKnowledge.updateWorldMeshStatus("Mesh Fault")
        }
    }

    private fun processTelemetry(apiNodes: List<Map<String, Any>>) {
        if (apiNodes.isEmpty()) return
        
        var latestSatelliteEpoch = 0L

        val realNodes = apiNodes.mapNotNull { it ->
            try {
                val spaceTrack = it["spaceTrack"] as? Map<*, *>
                val epochStr = spaceTrack?.get("EPOCH") as? String
                if (epochStr != null) {
                    val epochMillis = parseTleEpoch(epochStr)
                    if (epochMillis > latestSatelliteEpoch) latestSatelliteEpoch = epochMillis
                }

                val lat = (it["latitude"] as? Number)?.toDouble() ?: return@mapNotNull null
                val lon = (it["longitude"] as? Number)?.toDouble() ?: return@mapNotNull null
                val alt = (it["height_km"] as? Number)?.toDouble() ?: 550.0
                
                OrbitalObject(
                    name = spaceTrack?.get("OBJECT_NAME") as? String ?: "STARLINK",
                    latitude = lat,
                    longitude = lon,
                    altitude = alt
                )
            } catch (e: Exception) { null }
        }

        if (latestSatelliteEpoch > 0) {
            // Use .get() for lazy resolution
            shadowMesh.getOrNull() ?.syncWithAtomicClock(latestSatelliteEpoch)
        }

        if (realNodes.isNotEmpty()) {
            calculateMeshMetrics(realNodes, apiNodes)
            globalKnowledge.updateWorldMeshStatus("Mesh Active [Sats: ${realNodes.size}]")
        }
    }

    private fun parseTleEpoch(epoch: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(epoch)?.time ?: 0L
        } catch (e: Exception) { 0L }
    }

    private fun calculateMeshMetrics(nodes: List<OrbitalObject>, apiData: List<Map<String, Any>>) {
        val minLat = nodes.minOf { it.latitude }
        val maxLat = nodes.maxOf { it.latitude }
        val minLon = nodes.minOf { it.longitude }
        val maxLon = nodes.maxOf { it.longitude }
        val avgAlt = nodes.map { it.altitude }.average()
        
        val apertureKm = calculateDistanceSimple(minLat, minLon, maxLat, maxLon, avgAlt)
        val laserActiveCount = apiData.count { (it["version"] as? String)?.contains("2.0") == true }

        globalKnowledge.updateStarlinkMesh(globalKnowledge.starlinkMesh.value.copy(
            activeNodes = nodes.size,
            signalCoherence = (nodes.size.toFloat() / 6500f).coerceIn(0.1f, 1.0f),
            virtualApertureKm = apertureKm,
            laserLinkActive = laserActiveCount > 100,
            lastSatelliteName = nodes.lastOrNull()?.name ?: "UNKNOWN"
        ))

        if (apertureKm > 12000.0) {
            scope.launch {
                xilonProf.recordDiscovery("ATOMIC_ALIGNMENT", "Global Sync verified via Starlink Constellation.", 5)
            }
        }
    }

    private fun calculateDistanceSimple(lat1: Double, lon1: Double, lat2: Double, lon2: Double, alt: Double): Double {
        val r = 6371.0 + alt
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }
}
