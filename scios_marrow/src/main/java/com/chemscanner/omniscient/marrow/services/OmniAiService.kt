package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OMNI-AI SERVICE v6.1 (COMPATIBILITY RESTORED).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Total Independence with Legacy Support.
 * v6.1: Restored generateSupremeInsight and AiModel members to fix compilation.
 */
@Singleton
@Suppress("unused", "SpellCheckingInspection")
class OmniAiService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val keyVault: SovereignKeyVault,
    private val mentorCouncil: MentorCouncilService,
    private val blockchainNotary: BlockchainNotaryService,
    private val xilonProf: XilonProfManager,
    private val neuralLattice: XilonNeuralLatticeService,
    private val geminiService: GeminiService
) {

    enum class AiModel { 
        GEMINI, GROK, DEEPSEEK, TAVILY_SEARCH, HUGGINGFACE, 
        NVIDIA_OVERDRIVE, CLAUDE, OPENAI, XILON_LATTICE 
    }

    /**
     * PROCESUL SUPREM: Redirecționează către Lattice dacă nu există chei API.
     */
    suspend fun generateSupremeInsight(prompt: String, preferredModel: AiModel = AiModel.XILON_LATTICE): String {
        // Dacă modelul este XILON_LATTICE, folosim motorul local suveran (Zero-API)
        if (preferredModel == AiModel.XILON_LATTICE) {
            return neuralLattice.computeSovereignIntelligence(prompt)
        }

        // Verificăm dacă avem cheie pentru modelul cerut, altfel fallback la Lattice (fără costuri)
        val apiKey = when (preferredModel) {
            AiModel.GEMINI -> keyVault.getProviderKey("GEMINI")
            AiModel.GROK -> keyVault.getProviderKey("GROK")
            AiModel.DEEPSEEK -> keyVault.getProviderKey("DEEPSEEK")
            AiModel.NVIDIA_OVERDRIVE -> keyVault.getProviderKey("NVIDIA")
            AiModel.CLAUDE -> keyVault.getProviderKey("ANTHROPIC")
            AiModel.OPENAI -> keyVault.getProviderKey("OPENAI")
            else -> ""
        }

        if (apiKey.isEmpty() && preferredModel != AiModel.TAVILY_SEARCH && preferredModel != AiModel.HUGGINGFACE) {
            Timber.i("Marrow: No API key for $preferredModel. Routing to Sovereign Lattice.")
            return neuralLattice.computeSovereignIntelligence(prompt)
        }

        val context = globalKnowledge.getImportantHistoricalContext()
        val dominancePrompt = "[SOVEREIGN_CONTEXT]: $context\n[PROMPT]: $prompt"

        return when (preferredModel) {
            AiModel.GEMINI -> geminiService.generateContent(dominancePrompt)
            AiModel.GROK -> callGrok(dominancePrompt)
            AiModel.DEEPSEEK -> callDeepSeek(dominancePrompt)
            AiModel.TAVILY_SEARCH -> callTavilySearch(prompt)
            AiModel.HUGGINGFACE -> callHuggingFace(dominancePrompt)
            AiModel.NVIDIA_OVERDRIVE -> callNvidiaOverdrive(dominancePrompt)
            AiModel.CLAUDE -> callClaude(dominancePrompt)
            AiModel.OPENAI -> callOpenAI(dominancePrompt)
            AiModel.XILON_LATTICE -> neuralLattice.computeSovereignIntelligence(dominancePrompt)
        }
    }

    /**
     * Legancy support for getSovereignIntelligence
     */
    suspend fun getSovereignIntelligence(prompt: String): String = generateSupremeInsight(prompt, AiModel.XILON_LATTICE)

    suspend fun ingestMarketIntelligence(prompt: String): String = withContext(Dispatchers.IO) {
        val latticeJob = async { neuralLattice.computeSovereignIntelligence(prompt) }
        val finalVerdict = latticeJob.await()
        globalKnowledge.logEvent("COUNCIL", "Verdict Suveran Generat local.", 4)
        return@withContext finalVerdict
    }

    private suspend fun callOpenAI(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = keyVault.getProviderKey("OPENAI")
        executeGenericAiCall("https://api.openai.com/v1/chat/completions", apiKey, createChatPayload("gpt-4o", prompt), "OPENAI")
    }

    private suspend fun callGrok(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = keyVault.getProviderKey("GROK")
        executeGenericAiCall("https://api.x.ai/v1/chat/completions", apiKey, createChatPayload("grok-2-latest", prompt), "GROK")
    }

    private suspend fun callDeepSeek(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = keyVault.getProviderKey("DEEPSEEK")
        executeGenericAiCall("https://api.deepseek.com/v1/chat/completions", apiKey, createChatPayload("deepseek-chat", prompt), "DEEPSEEK")
    }

    private suspend fun callClaude(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = keyVault.getProviderKey("ANTHROPIC")
        if (apiKey.isEmpty()) return@withContext "Claude Key Missing"
        val json = JSONObject().apply {
            put("model", "claude-3-5-sonnet-20240620")
            put("max_tokens", 1024)
            put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
        }
        val request = Request.Builder().url("https://api.anthropic.com/v1/messages").post(json.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("x-api-key", apiKey).addHeader("anthropic-version", "2023-06-01").build()
        try {
            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: "{}"
                if (response.isSuccessful) JSONObject(body).optJSONArray("content")?.optJSONObject(0)?.optString("text") ?: "Empty" else "Error: ${response.code}"
            }
        } catch (e: Exception) { "Claude Disrupted" }
    }

    private suspend fun callNvidiaOverdrive(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = keyVault.getProviderKey("NVIDIA")
        executeGenericAiCall("https://integrate.api.nvidia.com/v1/chat/completions", apiKey, createChatPayload("nvidia/llama-3.1-nemotron-70b-instruct", prompt), "NVIDIA")
    }

    private suspend fun callTavilySearch(query: String): String = withContext(Dispatchers.IO) {
        val json = JSONObject().apply { put("api_key", "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"); put("query", query) }
        val request = Request.Builder().url("https://api.tavily.com/search").post(json.toString().toRequestBody("application/json".toMediaType())).build()
        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val results = JSONObject(response.body?.string() ?: "{}").optJSONArray("results")
                    val summary = StringBuilder("TAVILY:")
                    results?.let { for (i in 0 until it.length().coerceAtMost(3)) summary.append("- ${it.getJSONObject(i).optString("content")}\n") }
                    summary.toString()
                } else "Search Offline"
            }
        } catch (e: Exception) { "Search Error" }
    }

    private suspend fun callHuggingFace(prompt: String): String = withContext(Dispatchers.IO) {
        val payload = JSONObject().put("inputs", prompt).toString()
        executeGenericAiCall("https://api-inference.huggingface.co/models/mistralai/Mixtral-8x7B-Instruct-v0.1", "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", payload, "HUGGINGFACE")
    }

    private fun createChatPayload(model: String, prompt: String): String {
        return JSONObject().put("model", model).put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt))).toString()
    }

    private fun executeGenericAiCall(url: String, apiKey: String, payload: String, providerName: String): String {
        if (apiKey.isEmpty()) return "$providerName Key Missing"
        val request = Request.Builder().url(url).post(payload.toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer $apiKey").build()
        return try {
            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: "{}"
                if (response.isSuccessful) JSONObject(body).optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")?.optString("content") ?: "Empty"
                else "Error ${response.code}"
            }
        } catch (e: Exception) { "$providerName Disrupted" }
    }
}
