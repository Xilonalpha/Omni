package com.chemscanner.omniscient.marrow.config

import timber.log.Timber

/**
 * SECURE API KEY PROVIDER v2.0
 */
object ApiKeyProvider {
    fun getNasaApiKey(): String {
        val key = System.getenv("NASA_API_KEY") ?: ""
        return if (key.isNotEmpty()) key else ""
    }
    
    fun getMistralApiKey(): String {
        val key = System.getenv("MISTRAL_API_KEY") ?: ""
        return if (key.isNotEmpty()) key else ""
    }
    
    fun getGeminiApiKey(): String {
        val key = System.getenv("GEMINI_API_KEY") ?: ""
        return if (key.isNotEmpty()) key else ""
    }
}
