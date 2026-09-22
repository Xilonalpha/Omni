package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE VOYAGER REAL-TIME INTERCEPTOR v5.0 (SOVEREIGN).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Interstellar Telemetry Analysis without Cloud AI.
 * v5.0: TOTAL INDEPENDENCE. ELIMINATED GEMINI. Now powered by XNL.
 */
@Singleton
class VoyagerDirectLink @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val neuralLattice: XilonNeuralLatticeService, // Nucleul Suveran
    private val ttsService: TextToSpeechService,
    private val spaceLinkInjection: SpaceLinkInjection
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var isGhostLinkActive = false

    private val speedOfLight = 299792.458 // km/s
    private var vgr1BaseDistanceKm = 24385102441.0 
    private val vgr1VelocityKms = 16.999 

    fun igniteGhostLink() {
        if (isGhostLinkActive) return
        isGhostLinkActive = true
        
        globalKnowledge.logEvent("SPACE_LINK", "Voyager 1 Deep Intercept Active (XNL Powered).", 5)

        scope.launch {
            while (isGhostLinkActive) {
                val dsnSignals = globalKnowledge.dsnSignals.value
                val vgrSignal = dsnSignals.find { it.spacecraft.contains("VGR1", ignoreCase = true) || it.spacecraft.contains("Voyager 1", ignoreCase = true) }
                
                if (vgrSignal != null && vgrSignal.rangeKm > 0) {
                    vgr1BaseDistanceKm = vgrSignal.rangeKm
                }

                val timeMillis = System.currentTimeMillis()
                val offset = (timeMillis % 60000) / 1000.0 
                val currentDistance = vgr1BaseDistanceKm + (vgr1VelocityKms * offset)
                val signalTransitSeconds = currentDistance / speedOfLight
                val owltHours = signalTransitSeconds / 3600.0

                // Analiză Suverană la fiecare 15 minute
                if ((timeMillis / 1000) % 900 == 0L) { 
                    try {
                        val status = if (vgrSignal != null) "LINK_LIVE" else "PREDICTIVE_TRACKING"
                        val prompt = """
                            [VOYAGER_INTERSTELLAR_ANALYSIS]
                            DISTANTA: $currentDistance km
                            OWLT: $owltHours ore
                            STATUS: $status
                            MISIUNE: Analizează telemetria acestei sonde interstelare. 
                            Generează o scurtă observație despre starea ei la granița sistemului solar. 
                            Răspunde monumental în Română.
                        """.trimIndent()

                        val analysis = neuralLattice.computeSovereignIntelligence(prompt)
                        globalKnowledge.logEvent("VOYAGER_DEEP_SPACE", analysis, 4)
                        
                        withContext(Dispatchers.Main) {
                            ttsService.speak("Xilon, analiză Voyager completată local. $analysis")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Voyager Telemetry Analysis Fault")
                    }
                }

                val dishInfo = if (vgrSignal != null) " [Antenna: ${vgrSignal.antenna}]" else ""
                globalKnowledge.updateKernelStatus("VGR1: ${"%.3f".format(owltHours)}h$dishInfo")
                
                delay(10000) 
            }
        }
    }

    fun triggerManualInjection(directive: String) {
        scope.launch {
            globalKnowledge.logEvent("SPACE_LINK", "Directivă Suverană injectată către VGR1: $directive", 5)
            spaceLinkInjection.initiateSovereignInjection(directive)
        }
    }

    fun severGhostLink() {
        isGhostLinkActive = false
        globalKnowledge.updateKernelStatus("VGR1_LINK: ENCRYPTED")
    }
}
