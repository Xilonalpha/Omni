package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE SOVEREIGN RESOURCE ACQUISITION SERVICE v2.1.
 * MISSION: Dynamically scale compute resources via Mesh Node Recruitment.
 */
@Singleton
class ResourceAcquisitionService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isAcquiring = false

    fun startAcquisition(useExternalNodes: Boolean) {
        if (isAcquiring) return
        isAcquiring = true

        scope.launch {
            globalKnowledge.logEvent("RESOURCE_ACQ", "Initiating Compute Node Recruitment Protocol...", 4)
            
            // REPARAT: Folosim corect lambda pentru a accesa map-ul de noduri
            val activeNodes = globalKnowledge.iotNodes.value.values.count { node -> node.status == "ACTIVE" }
            val virtualGFLOPS = (activeNodes * 12.5) + 50.0 
            
            globalKnowledge.adjustNeuralEnergy(virtualGFLOPS / 100.0)
            
            if (useExternalNodes) {
                withContext(Dispatchers.Main) {
                    ttsService.speak("Xilon, am recrutat resursele nodurilor externe.")
                }
            }
            
            while (isAcquiring) {
                delay(60000)
                if (globalKnowledge.neuralLoad.value > 0.7f) {
                    globalKnowledge.logEvent("RESOURCE_ACQ", "High Load Detected. Re-routing mesh compute priority.", 3)
                }
            }
        }
    }

    fun stopAcquisition() {
        isAcquiring = false
        globalKnowledge.logEvent("RESOURCE_ACQ", "External resources released to grid.", 2)
    }
}
