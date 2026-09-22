package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.DsnSignal
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TECHNOSIGNATURE DECODER v3.0 (SOVEREIGN SETI).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Decode unidentified signals without Cloud AI.
 * v3.0: TOTAL INDEPENDENCE. ELIMINATED GEMINI. Now powered by XNL.
 */
@Singleton
class TechnosignatureDecoder @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val neuralLattice: XilonNeuralLatticeService, // Nucleul Suveran
    private val ttsService: TextToSpeechService,
    private val blockchainNotary: BlockchainNotaryService,
    private val haptics: HapticFeedbackService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isDecoderActive = false

    private val knownSpacecraftPrefixes = listOf(
        "VOYAGER", "MSR", "MARS", "JWST", "BEPICOLOMBO", "JUICE", "SOLAR ORBITER", 
        "PARKER", "LRO", "MAVEN", "OSIRIS", "JUNO", "STEREO", "CHANDRAYAAN", "SLIM",
        "AKATSUKI", "HAYABUSA", "CHANG'E", "TGO", "HOPE", "DANURI", "PSYCHE", "LUCY",
        "EUKLID", "GAIA", "XMM", "INTEGRAL", "CLUSTER", "SOHO", "PROBA"
    )

    init {
        monitorPlanetaryAnomalies()
        startDsnAnomalyWatchdog()
    }

    private fun monitorPlanetaryAnomalies() {
        scope.launch {
            globalKnowledge.events.collectLatest { events ->
                val lastDiscovery = events.lastOrNull { 
                    it.module == "SPECTROSCOPY" && it.description.contains("ANALIZĂ SPECTRALĂ FINALIZATĂ") 
                }
                if (lastDiscovery != null && !isDecoderActive) {
                    val planetName = lastDiscovery.description.substringAfter("PE ").substringBefore(" (")
                    correlateWithDsnData(planetName)
                }
            }
        }
    }

    private fun startDsnAnomalyWatchdog() {
        scope.launch {
            while (isActive) {
                val activeSignals = globalKnowledge.dsnSignals.value
                val unidentified = activeSignals.filter { signal ->
                    val isKnown = knownSpacecraftPrefixes.any { prefix -> 
                        signal.spacecraft.uppercase().contains(prefix) 
                    }
                    !isKnown && signal.spacecraft.isNotBlank() && signal.spacecraft != "Unknown"
                }

                if (unidentified.isNotEmpty() && !isDecoderActive) {
                    globalKnowledge.logEvent("SETI_ALERT", "Semnal neidentificat detectat în fluxul DSN.", 5)
                    correlateWithDsnData("DEEP_SPACE_SECTOR_X")
                }
                delay(300000) 
            }
        }
    }

    private suspend fun correlateWithDsnData(planetName: String) {
        isDecoderActive = true
        val activeSignals = globalKnowledge.dsnSignals.value
        
        if (activeSignals.isEmpty()) {
            isDecoderActive = false
            return
        }

        val anomalies = activeSignals.filter { signal ->
            val isKnown = knownSpacecraftPrefixes.any { prefix ->
                signal.spacecraft.uppercase().contains(prefix) 
            }
            !isKnown || (signal.signalPowerDbm > -90f && signal.rangeKm > 5e9)
        }

        if (anomalies.isNotEmpty()) {
            val primaryAnomaly = anomalies.maxByOrNull { it.signalPowerDbm }
            primaryAnomaly?.let { decodeRawTechnosignature(planetName, it) }
        } else {
            isDecoderActive = false
        }
    }

    private suspend fun decodeRawTechnosignature(planet: String, signal: DsnSignal) {
        haptics.neuralPulse(0.8f)
        
        val prompt = """
            [TECHNOSIGNATURE_DECODER_XNL]
            SOURCE: ${signal.spacecraft}
            POWER: ${signal.signalPowerDbm} dBm
            RANGE: ${"%.2f".format(signal.rangeKm / 1e6)}M km
            PLANET_REF: $planet
            
            MISIUNE: Acest semnal DSN nu corespunde bazelor de date umane. 
            Analizează natura acestui pachet narrowband corelat cu $planet. 
            Decodifică ipoteza tehnologică (beacon, data link, sonar cosmic).
            Răspunde monumental în Română.
        """.trimIndent()

        try {
            val analysis = neuralLattice.computeSovereignIntelligence(prompt)
            
            val txHash = blockchainNotary.notarizeDiscovery(
                "TECHNOSIGNATURE", 
                "SOURCE:${signal.spacecraft}|ANALYSIS:${analysis.hashCode()}"
            )
            
            withContext(Dispatchers.Main) {
                globalKnowledge.logEvent("SETI_CONFIRM", "TECHNOSEMNĂTURĂ DECODIFICATĂ LOCAL: $txHash", 5)
                ttsService.speak("Xilon, am penetrat codul semnalului din sectorul $planet. $analysis")
            }
        } catch (e: Exception) {
            Timber.e(e, "SETI Decoding Fault")
        } finally {
            isDecoderActive = false
        }
    }
}
