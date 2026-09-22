package com.chemscanner.omniscient.marrow.services

import android.util.Xml
import com.chemscanner.omniscient.marrow.repository.DsnSignal
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import com.chemscanner.omniscient.marrow.utils.NetworkHelper
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import timber.log.Timber
import java.io.StringReader
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DSN LIVE SERVICE v2.1 (OFFLINE RESILIENCE).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Interrogate NASA Deep Space Network for interplanetary probe telemetry.
 * v2.1: Integrated NetworkHelper to suppress DNS warnings when offline.
 */
@Singleton
class DsnLiveService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val xilonProf: XilonProfManager,
    private val networkHelper: NetworkHelper
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient()

    companion object {
        private const val DSN_URL = "https://eyes.nasa.gov/dsn/data/dsn.xml"
        private const val SPEED_OF_LIGHT_KMS = 299792.458
    }

    fun startDsnMonitoring() {
        scope.launch {
            while (isActive) {
                // Verificăm dacă avem internet înainte de a încerca conexiunea cu NASA
                if (networkHelper.isNetworkConnected()) {
                    try {
                        val request = Request.Builder().url(DSN_URL).build()
                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val xml = response.body?.string() ?: ""
                                val signals = parseDsnXml(xml)
                                if (signals.isNotEmpty()) {
                                    globalKnowledge.updateDsn(signals)
                                    processDeepSpaceAnomalies(signals)
                                }
                            }
                        }
                    } catch (e: UnknownHostException) {
                        Timber.w("DSN: Link offline (DNS resolution failure).")
                    } catch (e: Exception) {
                        Timber.e(e, "DSN: Interstellar link failure.")
                    }
                } else {
                    // Silent standby when offline
                    Timber.v("DSN: Network unavailable. Interstellar monitoring in standby.")
                }

                // Așteptăm 5 minute dacă suntem offline, sau 2 minute dacă suntem online
                val waitTime = if (networkHelper.isNetworkConnected()) 120000L else 300000L
                delay(waitTime)
            }
        }
    }

    private fun processDeepSpaceAnomalies(signals: List<DsnSignal>) {
        signals.filter { it.rangeKm > 1e9 }.forEach { sig ->
            val delaySeconds = sig.rangeKm / SPEED_OF_LIGHT_KMS
            val content = "DSN ALERT: Link established with ${sig.spacecraft} at ${"%.2f".format(sig.rangeKm / 1e9)} billion km. Signal delay: ${"%.0f".format(delaySeconds)}s."
            scope.launch {
                globalKnowledge.logEvent("DSN_NASA", content, 4)
                xilonProf.recordDiscovery("DEEP_SPACE", content, 4)
                withContext(Dispatchers.Main) {
                    ttsService.speak("Xilon, asimilăm date de la sonda ${sig.spacecraft} prin rețeaua Deep Space NASA.", "ro", false)
                }
            }
        }
    }

    private fun parseDsnXml(xml: String): List<DsnSignal> {
        val signals = mutableListOf<DsnSignal>()
        val parser = Xml.newPullParser()
        parser.setInput(StringReader(xml))
        var eventType = parser.eventType
        var currentAntenna = ""
        var currentSpacecraft = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name
            if (eventType == XmlPullParser.START_TAG) {
                when (name) {
                    "dish" -> currentAntenna = parser.getAttributeValue(null, "name") ?: "Unknown"
                    "target" -> currentSpacecraft = parser.getAttributeValue(null, "name") ?: ""
                    "downSignal" -> {
                        val range = parser.getAttributeValue(null, "range")?.toDoubleOrNull() ?: 0.0
                        if (currentSpacecraft.isNotEmpty()) {
                            signals.add(DsnSignal(
                                antenna = currentAntenna,
                                spacecraft = currentSpacecraft,
                                rangeKm = range,
                                band = parser.getAttributeValue(null, "band") ?: "X",
                                signalPowerDbm = parser.getAttributeValue(null, "power")?.toFloatOrNull() ?: 0f
                            ))
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return signals
    }
}
