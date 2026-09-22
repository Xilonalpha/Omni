package com.chemscanner.omniscient.marrow.services

import android.annotation.SuppressLint
import com.chemscanner.omniscient.marrow.repository.CosmicAlert
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.net.InetAddress
import java.net.URLEncoder
import java.net.UnknownHostException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * VERA RUBIN ALERT SERVICE v3.0 (REAL PLANET HUNTER).
 * AUTHORITY: ARCHITECT XILON.
 * v3.0: Added "Microlensing candidate" stream to identify real, undocumented planets.
 */
@Singleton
class VeraRubinAlertService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val gateway: OmnipresenceGateway,
    private val ttsService: TextToSpeechService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = getUnsafeOkHttpClient()
    private val gson = Gson()

    private val finkApiUrl = "https://fink-portal.org/api/v1/latests"

    private var alertJob: Job? = null

    @SuppressLint("CustomX509TrustManager", "TrustAllX509TrustManager")
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )
        return try {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .retryOnConnectionFailure(true)
                .build()
        } catch (e: Exception) {
            OkHttpClient()
        }
    }

    fun startAlertStream() {
        if (alertJob?.isActive == true) return
        alertJob = scope.launch {
            while (isActive) {
                try {
                    // Căutăm Planete prin Microlensing (Metoda cea mai reală pentru planete rătăcitoare)
                    fetchLatestCosmicEvents("Microlensing candidate")
                    delay(20000)
                    fetchLatestCosmicEvents("Unknown") 
                    delay(20000)
                    fetchLatestCosmicEvents("Supernova candidate")
                } catch (e: Exception) {
                    if (e !is CancellationException) delay(60000)
                }
                delay(300000) 
            }
        }
    }

    private suspend fun fetchLatestCosmicEvents(objectClass: String) = withContext(Dispatchers.IO) {
        val encodedClass = URLEncoder.encode(objectClass, "UTF-8")
        val url = "$finkApiUrl?class=$encodedClass&n=5"
        val request = Request.Builder().url(url).get().build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()?.trim() ?: return@withContext
                    if (body.startsWith("<!DOCTYPE") || body.startsWith("<html")) return@withContext

                    val jsonElement = try { gson.fromJson(body, JsonElement::class.java) } catch (e: Exception) { null }

                    if (jsonElement is JsonArray) {
                        jsonElement.forEach { element ->
                            if (element.isJsonObject) processAlert(element.asJsonObject, objectClass)
                        }
                    }
                }
            }
        } catch (e: Exception) { Timber.v("Fink Link Halted") }
    }

    private fun processAlert(json: JsonObject, originalClass: String) {
        val objectId = json.get("i:objectId")?.asString ?: "Unknown"
        val type = json.get("v:classification")?.asString ?: originalClass
        val ra = json.get("i:ra")?.asDouble ?: 0.0
        val dec = json.get("i:dec")?.asDouble ?: 0.0
        val mag = json.get("i:magpsf")?.asFloat ?: 0.0f

        val alert = CosmicAlert(
            objectId = objectId, type = type, ra = ra, dec = dec,
            magnitude = mag, confidence = json.get("v:probability")?.asFloat ?: 0.0f,
            timestamp = System.currentTimeMillis()
        )

        val history = globalKnowledge.cosmicAlertHistory.value
        if (history.none { it.objectId == objectId }) {
            globalKnowledge.updateCosmicAlert(alert)
            
            if (type.contains("Microlensing", true)) {
                globalKnowledge.logEvent("PLANET_HUNTER", "ANOMALIE DE GRAVITAȚIE DETECTATĂ: Posibilă planetă nouă la RA:$ra", 5)
                ttsService.speak("Xilon, am detectat o curbură gravitațională suspectă. Datele sugerează o planetă negăsită în sectorul $ra.")
            } else {
                globalKnowledge.logEvent("VERA_RUBIN", "Intel Real: $objectId ($type)", 4)
            }
            
            gateway.ingestCosmicAlert(type, "Sector:$ra", mag)
        }
    }
}
