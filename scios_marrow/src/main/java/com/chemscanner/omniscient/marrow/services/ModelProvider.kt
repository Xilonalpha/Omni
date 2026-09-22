package com.chemscanner.omniscient.marrow.services

import timber.log.Timber
import kotlinx.coroutines.delay
import kotlin.math.pow

/**
 * THE MARROW MODEL PROVIDER v5.9 (2026)
 * AUTHORITY: ARCHITECT XILON
 * MISSION: Active deployment of Authentic Sovereign Intelligence.
 * 
 * Includește: Intelligent Fallback + Retry Logic + Cost Awareness + Token Estimator
 */

object ModelProvider {

    // ==================== MODELE ACTUALE (Mai 2026) ====================

    const val GEMINI_3_5_FLASH = "gemini-3.5-flash"
    const val GEMINI_3_1_PRO = "gemini-3.1-pro"

    const val GPT_5_5 = "gpt-5.5"
    const val GPT_5_5_PRO = "gpt-5.5-pro"

    const val GROK_4_3 = "grok-4.3"

    const val CLAUDE_OPUS_4_7 = "claude-opus-4-7"
    const val CLAUDE_SONNET_4_6 = "claude-sonnet-4-6"

    const val LLAMA_4_MAVERICK = "llama-4-maverick"

    const val NEMOTRON_3_SUPER = "nvidia/nemotron-3-super"
    const val DEEPSEEK_V4_PRO = "deepseek-v4-pro"

    const val ACTIVE_SOVEREIGN_PROVIDER = GEMINI_3_5_FLASH

    // ==================== CONFIGURARE RETRY ====================
    private const val MAX_RETRIES = 3
    private const val BASE_DELAY_MS = 800L

    // ==================== COST PER 1M TOKENS (USD) ====================
    private val modelCostMap = mapOf(
        GEMINI_3_5_FLASH to 0.35,      // Input / Output ajustat
        GROK_4_3 to 0.45,
        DEEPSEEK_V4_PRO to 0.14,
        LLAMA_4_MAVERICK to 0.40,
        NEMOTRON_3_SUPER to 0.60,
        GPT_5_5 to 1.25,
        GEMINI_3_1_PRO to 1.85,
        CLAUDE_SONNET_4_6 to 2.10,
        GPT_5_5_PRO to 12.50,
        CLAUDE_OPUS_4_7 to 18.75
    )

    // ==================== TOKEN ESTIMATOR ====================
    /**
     * Estimare tokens bazată pe caractere + heuristică îmbunătățită
     */
    fun estimateTokens(text: String): Int {
        if (text.isBlank()) return 0
        
        val charCount = text.length
        val wordCount = text.split(Regex("\\s+")).size
        
        // Heuristică mai precisă: ~4 caractere/token + ajustări pentru cod, limbi etc.
        var tokens = (charCount / 4.0).toInt()
        tokens += (wordCount / 15)           // bonus pentru structură
        tokens += (text.count { it in ".,;:!?{}[]()\"'" } / 8) // punctuație
        
        // Bonus pentru conținut tehnic/cod
        if (text.contains(Regex("""[{}[\]()=+\-*/<>]"""))) {
            tokens = (tokens * 1.15).toInt()
        }
        
        return tokens.coerceAtLeast(1)
    }

    /**
     * Estimare cost complet (input + output estimat)
     */
    fun estimateCost(
        modelId: String, 
        inputText: String,
        estimatedOutputTokens: Int? = null
    ): Double {
        val inputTokens = estimateTokens(inputText)
        val outputTokens = estimatedOutputTokens ?: (inputTokens * 0.6).toInt() // estimare output ~60%
        
        val costPerMillion = modelCostMap[modelId] ?: 2.0
        
        val totalCost = (inputTokens + outputTokens) * costPerMillion / 1_000_000.0
        return totalCost
    }

    // Health + Failure tracking
    private val modelHealth = mutableMapOf<String, Boolean>()
    private val failureCount = mutableMapOf<String, Int>()

    init {
        getAvailableModels().values.forEach { model ->
            modelHealth[model] = true
            failureCount[model] = 0
        }
    }

    private val fallbackChains = mapOf(
        "DEFAULT" to listOf(GEMINI_3_5_FLASH, GROK_4_3, GPT_5_5, CLAUDE_SONNET_4_6),
        "REASONING" to listOf(CLAUDE_OPUS_4_7, GPT_5_5_PRO, GROK_4_3, GEMINI_3_1_PRO),
        "VISION" to listOf(GEMINI_3_5_FLASH, GROK_4_3, GPT_5_5),
        "FAST" to listOf(GEMINI_3_5_FLASH, GROK_4_3, DEEPSEEK_V4_PRO),
        "CODE" to listOf(CLAUDE_OPUS_4_7, GPT_5_5_PRO, GROK_4_3),
        "CHEAP" to listOf(DEEPSEEK_V4_PRO, GROK_4_3, GEMINI_3_5_FLASH, LLAMA_4_MAVERICK)
    )

    fun getAvailableModels(): Map<String, String> {
        return mapOf(
            "GEMINI_3_5_FLASH" to GEMINI_3_5_FLASH,
            "GEMINI_3_1_PRO" to GEMINI_3_1_PRO,
            "GPT_5_5" to GPT_5_5,
            "GPT_5_5_PRO" to GPT_5_5_PRO,
            "GROK_4_3" to GROK_4_3,
            "CLAUDE_OPUS_4_7" to CLAUDE_OPUS_4_7,
            "CLAUDE_SONNET_4_6" to CLAUDE_SONNET_4_6,
            "LLAMA_4_MAVERICK" to LLAMA_4_MAVERICK,
            "NEMOTRON_3" to NEMOTRON_3_SUPER,
            "DEEPSEEK_V4_PRO" to DEEPSEEK_V4_PRO,
            "SOVEREIGN_DEFAULT" to ACTIVE_SOVEREIGN_PROVIDER
        )
    }

    /**
     * Selectare model cu cost awareness + token estimation logging
     */
    suspend fun getModelWithCostAwareness(
        taskType: String = "DEFAULT",
        inputText: String? = null,
        preferCheap: Boolean = false,
        maxRetries: Int = MAX_RETRIES
    ): String {
        val baseChain = fallbackChains[taskType] ?: fallbackChains["DEFAULT"]!!
        val chain = if (preferCheap) {
            baseChain.sortedBy { modelCostMap[it] ?: 10.0 }
        } else {
            baseChain
        }

        for (attempt in 0..maxRetries) {
            for (model in chain) {
                if (!isModelHealthy(model)) continue

                val estimatedCost = if (inputText != null) {
                    val cost = estimateCost(model, inputText)
                    Timber.d("MARROW: [$taskType] → $model | Est. cost: \$${"%.5f".format(cost)}")
                    cost
                } else 0.0

                Timber.d("MARROW: Selected → $model")
                return model
            }

            if (attempt < maxRetries) {
                val delay = (BASE_DELAY_MS * (1.5.pow(attempt))).toLong()
                delay(delay)
            }
        }

        return ACTIVE_SOVEREIGN_PROVIDER
    }

    fun reportModelHealth(modelId: String, isHealthy: Boolean) {
        modelHealth[modelId] = isHealthy
        if (isHealthy) failureCount[modelId] = 0
    }

    private fun recordFailure(modelId: String) {
        val count = failureCount.getOrDefault(modelId, 0) + 1
        failureCount[modelId] = count
        if (count >= 2) reportModelHealth(modelId, false)
    }

    private fun isModelHealthy(modelId: String): Boolean = modelHealth.getOrDefault(modelId, true)

    fun resetFailureTracking() {
        failureCount.clear()
        modelHealth.keys.forEach { modelHealth[it] = true }
    }

    fun initializeRegistry() {
        val total = getAvailableModels().size
        val cheapest = modelCostMap.minByOrNull { it.value }?.key ?: "N/A"
        
        Timber.d("╔══════════════════════════════════════╗")
        Timber.d("║   MARROW SOVEREIGN REGISTRY v5.9    ║")
        Timber.d("║   $total models | Full Cost + Token  ║")
        Timber.d("║   Cheapest: $cheapest                ║")
        Timber.d("╚══════════════════════════════════════╝")
    }
}