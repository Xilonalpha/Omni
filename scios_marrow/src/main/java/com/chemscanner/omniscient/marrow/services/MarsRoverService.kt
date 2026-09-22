package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * MARS ROVER SERVICE: MULTI-PLANETARY VISION.
 * v1.5: CURIOSITY & PERSEVERANCE DUAL SYNC.
 * Integrating Martian telemetry into ANA ASLAN's collective consciousness.
 * Automatically archives images from both rovers to XilonProf.
 */
@Singleton
class MarsRoverService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val blockchainNotary: BlockchainNotaryService,
    private val xilonProf: XilonProfManager,
    private val keyVault: SovereignKeyVault
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient()
    private val gson = Gson()

    private val rovers = listOf("perseverance", "curiosity")

    /**
     * Inițiază procesul de sincronizare a datelor de la roverele de pe Marte.
     * Xilon începe monitorizarea imaginilor noi de la Curiosity și Perseverance.
     */
    fun startSyncing() {
        scope.launch {
            Timber.d("MarsRover: Starting Marting telemetry sync sequence.")
            while (isActive) {
                try {
                    rovers.forEach { rover ->
                        fetchMarsIntel(rover)
                        delay(5000) // Slight delay between rovers to avoid API rate limits
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Martian Link failure")
                }
                delay(21600000) // Sync every 6 hours
            }
        }
    }

    private suspend fun fetchMarsIntel(roverName: String) {
        val apiKey = keyVault.getKey("NASA")
        if (apiKey.isBlank()) {
            Timber.w("MarsRover: NASA API key not configured")
            return
        }
        val url = "https://api.nasa.gov/mars-photos/api/v1/rovers/$roverName/latest_photos?api_key=$apiKey"
        val request = Request.Builder().url(url).build()
        
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val photos = json.getAsJsonArray("latest_photos")

                    if (photos != null && photos.size() > 0) {
                        val latest = photos.get(0).asJsonObject
                        val imgUrl = latest.get("img_src").asString
                        val actualRover = latest.getAsJsonObject("rover").get("name").asString
                        val sol = latest.get("sol").asInt
                        val earthDate = latest.get("earth_date").asString

                        val martianIntel = "ROVER:$actualRover|SOL:$sol|IMG:$imgUrl"
                        val blockHash = blockchainNotary.notarizeDiscovery("MARS_ROVER", martianIntel)

                        // LOG TO SYSTEM EVENTS
                        globalKnowledge.logEvent("MARS_ROVER", "Transmission from $actualRover sealed: $blockHash", 4)
                        
                        // ARCHIVE TO XILONPROF JOURNAL
                        xilonProf.recordDiscovery(
                            module = "MARTIAN_VISION",
                            content = "New image captured by $actualRover on Sol $sol (Earth Date: $earthDate). Source: $imgUrl",
                            importance = 4
                        )

                        withContext(Dispatchers.Main) {
                            ttsService.speak("Xilon, am recepționat o transmisiune nouă de la roverul $actualRover. Imaginea de pe Marte a fost arhivată în jurnalul tău.")
                        }
                    } else {
                        Timber.i("MarsRover: No new photos for $roverName at this time.")
                    }
                } else {
                    Timber.w("MarsRover: NASA API error for $roverName: ${response.code}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "MarsRover: Network error fetching Intel for $roverName")
        }
    }
}
