package com.chemscanner.omniscient.marrow.services

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE GEMINI TEXT SERVICE v3.7 (RE-ARMED LOGIC).
 * AUTHORITY: ARCHITECT XILON.
 * v3.7: RESTORED original generation config while keeping failure resilience.
 */
@Singleton
class GeminiTextService @Inject constructor(
    private val keyVault: SovereignKeyVault,
    private val geminiService: GeminiService // Pentru failover la alte AI-uri
) {
    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
    )

    private val config = generationConfig {
        temperature = 0.9f
        topK = 40
        topP = 0.95f
        maxOutputTokens = 8192
    }

    private fun getModel(key: String) = GenerativeModel(
        modelName = ModelProvider.GEMINI_MODEL_NAME,
        apiKey = key,
        safetySettings = safetySettings,
        generationConfig = config
    )

    suspend fun generateContent(prompt: String): String? = withContext(Dispatchers.IO) {
        val currentKey = keyVault.getActiveKey()
        try {
            val response = getModel(currentKey).generateContent(prompt)
            response.text
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("429") || msg.contains("quota") || msg.contains("exhausted", ignoreCase = true)) {
                keyVault.reportKeyFailure(currentKey, "TEXT_QUOTA")
                // Dacă Gemini e la pământ, folosim motorul central pentru failover la Grok/Gemma
                geminiService.generateContent(prompt, "MARROW_TEXT_UNIT")
            } else {
                Timber.e(e, "Marrow: Text Service Fault")
                null
            }
        }
    }
}
