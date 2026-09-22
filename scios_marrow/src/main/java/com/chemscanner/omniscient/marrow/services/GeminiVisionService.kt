package com.chemscanner.omniscient.marrow.services

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GEMINI VISION SERVICE v3.8 (RESTORED LOGIC).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Multimodal sovereign analysis with specific recovery paths.
 * v3.8: RESTORED original generation config and recovery logic (gemini-2.0-flash failover).
 */
@Singleton
class GeminiVisionService @Inject constructor(
    private val keyVault: SovereignKeyVault,
    private val geminiService: GeminiService
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

    private fun getModel(modelName: String, key: String) = GenerativeModel(
        modelName = modelName,
        apiKey = key,
        safetySettings = safetySettings,
        generationConfig = config
    )

    suspend fun getChemicalDataFromImage(bitmap: Bitmap, prompt: String): String? = withContext(Dispatchers.IO) {
        val currentKey = keyVault.getActiveKey()
        try {
            val inputContent = content {
                image(bitmap)
                text(prompt)
            }
            val response = getModel(ModelProvider.VISION_MODEL_NAME, currentKey).generateContent(inputContent)
            response.text
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("429") || msg.contains("quota") || msg.contains("exhausted", ignoreCase = true)) {
                keyVault.reportKeyFailure(currentKey, "VISION_QUOTA")
                // Reîncercăm cu rotație prin motorul central
                geminiService.analyzeImageWithPersona(bitmap, prompt, "VISION_RECOVERY")
            } else if (msg.contains("unexpected") || msg.contains("404")) {
                Timber.e("VISION_CORE: Model error. Initiating specific recovery path...")
                retryVisionAnalysis(bitmap, prompt)
            } else {
                Timber.e(e, "Marrow: Vision Service Fault")
                null
            }
        }
    }

    private suspend fun retryVisionAnalysis(bitmap: Bitmap, prompt: String): String? {
        val currentKey = keyVault.getActiveKey()
        return try {
            val response = getModel("gemini-2.0-flash", currentKey).generateContent(
                content {
                    image(bitmap)
                    text("RECOVERY_ANALYSIS: $prompt")
                }
            )
            response.text
        } catch (e: Exception) {
            Timber.e("VISION_CORE: Critical recovery failure.")
            null
        }
    }
}
