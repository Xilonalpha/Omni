package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.EnvironmentalSignals
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE PREDICTIVE REALITY ENGINE v3.0 (SOVEREIGN EDITION).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Zero-API Geofence & Seismic Correlation.
 * v3.0: ELIMINATED GEMINI. Now uses XNL for real-time anomaly synthesis.
 */
@Singleton
class PredictiveRealityEngine @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val neuralLattice: XilonNeuralLatticeService // Nucleul Suveran
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient()
    private val signalHistory = Collections.synchronizedList(mutableListOf<EnvironmentalSignals>())
    
    private val usgsApiUrl = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/4.5_day.geojson"

    init {
        monitorAndForecast()
        startGlobalSeismicWatch()
    }

    private fun startGlobalSeismicWatch() {
        scope.launch {
            while (isActive) {
                try {
                    val request = Request.Builder().url(usgsApiUrl).build()
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val body = response.body?.string() ?: return@use
                            processRealEarthquakes(body)
                        }
                    }
                } catch (e: Exception) { Timber.e("Seismic Link Interrupted") }
                delay(300000) // 5 min
            }
        }
    }

    private suspend fun processRealEarthquakes(json: String) {
        try {
            val features = JSONObject(json).getJSONArray("features")
            if (features.length() > 0) {
                val latest = features.getJSONObject(0).getJSONObject("properties")
                val mag = latest.getDouble("mag")
                val place = latest.getString("place")
                
                if (mag > 5.5) {
                    // Solicităm Lattice-ului o analiză de corelație cu senzorii locali
                    val signals = globalKnowledge.realSignals.value
                    val prompt = "DETECTIE: Cutremur $mag in $place. Senzori locali: EMF=${signals.emfIntensity}uT. Exista corelatie sau risc local?"
                    
                    val analysis = neuralLattice.computeSovereignIntelligence(prompt)
                    globalKnowledge.logEvent("PREDICTIVE_REALITY", "Analiză Seismică: $analysis", 5)
                    
                    withContext(Dispatchers.Main) {
                        ttsService.speak("Xilon, alertă globală: $analysis")
                    }
                }
            }
        } catch (e: Exception) { }
    }

    private fun monitorAndForecast() {
        scope.launch {
            globalKnowledge.realSignals.collectLatest { current ->
                updateHistory(current)
                if (signalHistory.size > 20) {
                    analyzeTrendsWithLattice()
                }
            }
        }
    }

    private fun updateHistory(signal: EnvironmentalSignals) {
        signalHistory.add(signal)
        if (signalHistory.size > 100) signalHistory.removeAt(0)
    }

    private suspend fun analyzeTrendsWithLattice() {
        val lastSignals = signalHistory.takeLast(5)
        val trendStr = lastSignals.joinToString { "${it.emfIntensity}uT" }
        
        // Lattice-ul detectează anomalii în pattern-ul de date brute
        val prompt = "Analizează acest trend de zgomot magnetic și prezice dacă urmează o fluctuație majoră: $trendStr"
        val prediction = neuralLattice.computeSovereignIntelligence(prompt)
        
        if (prediction.contains("RISC") || prediction.contains("ANOMALIE")) {
            globalKnowledge.logEvent("MARROW_PRED", "Predicție: $prediction", 4)
        }
    }
}
