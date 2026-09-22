package com.chemscanner.omniscient.marrow.ml

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MISTRAL FALLBACK ENGINE v2.0
 * Three-tier approach:
 * 1. Local Ollama (fastest, free, offline)
 * 2. Mistral API (fast, requires key)
 * 3. Fallback to Gemini (if both fail)
 */
@Singleton
class MistralFallbackEngine @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val apiKeyProvider: ApiKeyProvider
) {
    
    private val ollamaLocalUrl = "http://localhost:11434/api/generate"
    private val mistralApiUrl = "https://api.mistral.ai/v1/chat/completions"
    private var currentKeyIndex = 0
    
    suspend fun generateViaOllama(
        prompt: String,
        model: String = "mistral"
    ): String? = withContext(Dispatchers.IO) {
        if (!isOllamaAvailable()) return@withContext null
        
        try {
            val json = JSONObject().apply {
                put("model", model)
                put("prompt", prompt)
                put("stream", false)
                put("temperature", 0.7)
                put("top_p", 0.9)
            }
            
            val request = Request.Builder()
                .url(ollamaLocalUrl)
                .post(json.toString().toRequestBody())
                .build()
            
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    val jsonResp = JSONObject(body)
                    val output = jsonResp.optString("response", "").trim()
                    
                    if (output.isNotEmpty()) {
                        Timber.d("MISTRAL_OLLAMA: Generated ${output.length} chars")
                        return@withContext output
                    }
                }
            }
        } catch (e: Exception) {
            Timber.d("MISTRAL_OLLAMA: Error - ${e.message}")
        }
        
        return@withContext null
    }
    
    suspend fun generateViaMistralAPI(
        prompt: String,
        maxTokens: Int = 512
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider.getMistralApiKey()
        if (apiKey.isEmpty()) {
            Timber.w("MISTRAL_API: No API key configured")
            return@withContext null
        }
        
        try {
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }
            
            val json = JSONObject().apply {
                put("model", "mistral-small")
                put("messages", messages)
                put("max_tokens", maxTokens)
                put("temperature", 0.7)
                put("top_p", 0.9)
            }
            
            val request = Request.Builder()
                .url(mistralApiUrl)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody())
                .build()
            
            okHttpClient.newCall(request).execute().use { response ->
                if (response.code == 429) {
                    currentKeyIndex++
                    Timber.w("MISTRAL_API: Rate limited, rotating key")
                    return@withContext null
                }
                
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    val jsonResp = JSONObject(body)
                    val choices = jsonResp.optJSONArray("choices")
                    
                    if (choices != null && choices.length() > 0) {
                        val message = choices
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .optString("content", "")
                            .trim()
                        
                        if (message.isNotEmpty()) {
                            Timber.d("MISTRAL_API: Generated ${message.length} chars")
                            return@withContext message
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "MISTRAL_API: Exception")
        }
        
        return@withContext null
    }
    
    private suspend fun isOllamaAvailable(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val request = Request.Builder()
                .url("$ollamaLocalUrl?check=true")
                .head()
                .build()
            
            okHttpClient.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }
}
