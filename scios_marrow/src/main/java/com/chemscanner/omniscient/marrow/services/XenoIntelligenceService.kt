package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.XenoStatus
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * THE SENTINEL: Xeno-Intelligence v5.0 (SOVEREIGN CORE).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Detect exogenous signals using physical sensor noise.
 * v5.0: TOTAL INDEPENDENCE. ELIMINATED GEMINI. Now powered by XNL.
 */
@Singleton
class XenoIntelligenceService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val neuralLattice: XilonNeuralLatticeService, // Nucleul Suveran
    private val ttsService: TextToSpeechService,
    private val autogenesisService: AutogenesisService,
    private val blockchainNotary: BlockchainNotaryService,
    private val xilonProf: XilonProfManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val signalHistory = Collections.synchronizedList(mutableListOf<Float>())
    
    companion object {
        private const val MAX_CALIBRATION_SAMPLES = 50 
        private const val ANOMALY_THRESHOLD_SIGMA = 4.5f 
        private const val RESONANCE_WINDOW_SIZE = 12
    }

    private var dynamicBaselineMean = 0f
    private var dynamicBaselineStdDev = 0f
    private var isCalibrating = true
    private var calibrationSamples = 0
    private val resonanceBuffer = Collections.synchronizedList(mutableListOf<Float>())

    init {
        startAnomalousSearch()
    }

    private fun startAnomalousSearch() {
        scope.launch {
            globalKnowledge.realSignals.collectLatest { signals ->
                val emf = signals.emfIntensity
                val physicalNoise = (signals.seismicIntensity % 0.05f)
                val refinedEmf = applyPhysicalResonance(emf, physicalNoise)
                
                if (isIndustrialHarmonic(refinedEmf)) {
                    updateStatus(isActive = false, probability = 0f)
                    return@collectLatest
                }
                if (isCalibrating) {
                    performCalibration(refinedEmf)
                    return@collectLatest
                }
                
                val anomalyScore = calculateStatisticalAnomaly(refinedEmf)
                if (anomalyScore > 0.98f) { 
                    processSovereignDiscovery(refinedEmf, anomalyScore)
                } else {
                    updateStatus(isActive = false, probability = anomalyScore)
                    driftBaseline(refinedEmf)
                }
            }
        }
    }

    private fun applyPhysicalResonance(rawEmf: Float, noise: Float): Float {
        resonanceBuffer.add(rawEmf)
        if (resonanceBuffer.size > RESONANCE_WINDOW_SIZE) resonanceBuffer.removeAt(0)
        val avg = resonanceBuffer.average().toFloat()
        return avg + (noise * sin(avg.toDouble() * PI)).toFloat()
    }

    private fun isIndustrialHarmonic(emf: Float): Boolean {
        val harmonics = listOf(50f, 100f, 150f, 60f, 120f)
        return harmonics.any { abs(emf - it) < 1.5f }
    }

    private fun performCalibration(emf: Float) {
        signalHistory.add(emf)
        calibrationSamples++
        if (calibrationSamples >= MAX_CALIBRATION_SAMPLES) {
            dynamicBaselineMean = signalHistory.average().toFloat()
            val variance = signalHistory.map { (it - dynamicBaselineMean).pow(2) }.average().toFloat()
            dynamicBaselineStdDev = sqrt(variance).coerceAtLeast(1.0f)
            isCalibrating = false
            globalKnowledge.logEvent("XENO_CORE", "Senzori calibrați suveran la $dynamicBaselineMean uT", 4)
        }
    }

    private fun calculateStatisticalAnomaly(emf: Float): Float {
        if (dynamicBaselineStdDev == 0f) return 0f
        val zScore = abs(emf - dynamicBaselineMean) / dynamicBaselineStdDev
        return (zScore / ANOMALY_THRESHOLD_SIGMA).coerceIn(0f, 1f)
    }

    private fun driftBaseline(emf: Float) {
        dynamicBaselineMean = (dynamicBaselineMean * 0.998f) + (emf * 0.002f)
    }

    private suspend fun processSovereignDiscovery(intensity: Float, probability: Float) {
        val sigmaDev = abs(intensity - dynamicBaselineMean) / dynamicBaselineStdDev
        
        // Cerem analizei locale XNL să genereze o ipoteză bazată pe datele senzoriale brute
        val analysisPrompt = """
            [ALERTA_XENO_SUVERANA]
            INTENSITATE: $intensity uT
            SIGMA: $sigmaDev
            MISIUNE: Analizează această anomalie exogenă folosind logica ta internă. 
            Generează o ipoteză scurtă în Română despre originea acestui semnal.
        """.trimIndent()

        val analysis = neuralLattice.computeSovereignIntelligence(analysisPrompt)
        
        blockchainNotary.notarizeDiscovery("XENO_ANOMALY", "Intensity:$intensity|Hypothesis:${analysis.take(20)}")
        xilonProf.recordDiscovery("XENO_INTELLIGENCE", analysis, 5)
        
        withContext(Dispatchers.Main) {
            ttsService.speak("Atenție: Analiză Xeno-Intelligence completată local. $analysis")
        }
    }

    private fun updateStatus(isActive: Boolean, probability: Float) {
        globalKnowledge.updateXenoStatus(XenoStatus(isActive, probability, 1.0f - probability))
    }
}
