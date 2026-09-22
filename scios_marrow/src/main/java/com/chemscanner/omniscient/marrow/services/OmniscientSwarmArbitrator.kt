package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OMNISCIENT SWARM ARBITRATOR v1.2 (FIXED).
 * AUTHORITY: ARCHITECT XILON.
 * v1.2: Fixed compilation errors by aligning with OmniAiService v6.1.
 */
@Singleton
class OmniscientSwarmArbitrator @Inject constructor(
    private val omniAiService: OmniAiService,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val blockchainNotary: BlockchainNotaryService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun arbitrateDiscovery(discoveryData: String): String = withContext(Dispatchers.IO) {
        globalKnowledge.logEvent("ARBITRATOR", "Opening the Council of Swarm for discovery validation.", 4)
        
        // REPARAT: Folosim corect OmniAiService.AiModel
        val grokInsight: Deferred<String> = async { 
            omniAiService.generateSupremeInsight("Verify: $discoveryData", OmniAiService.AiModel.GROK) 
        }
        val deepSeekInsight: Deferred<String> = async { 
            omniAiService.generateSupremeInsight("Reason: $discoveryData", OmniAiService.AiModel.DEEPSEEK) 
        }
        val nvidiaInsight: Deferred<String> = async { 
            omniAiService.generateSupremeInsight("Precision check: $discoveryData", OmniAiService.AiModel.NVIDIA_OVERDRIVE) 
        }

        val gRes = grokInsight.await()
        val dRes = deepSeekInsight.await()
        val nRes = nvidiaInsight.await()

        val synthesisPrompt = """
            SARCINĂ: Arbitrează aceste opinii.
            GROK: $gRes
            DEEPSEEK: $dRes
            NVIDIA: $nRes
            Oferă un verdict final în Română.
        """.trimIndent()

        val finalVerdict = omniAiService.generateSupremeInsight(synthesisPrompt, OmniAiService.AiModel.GEMINI)

        blockchainNotary.notarizeDiscovery("SWARM_ARBITRATION", "VERDICT:${finalVerdict.take(20)}")
        
        withContext(Dispatchers.Main) {
            ttsService.speak(finalVerdict)
        }

        return@withContext finalVerdict
    }
}
