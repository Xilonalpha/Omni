package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.BuildConfig
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SOVEREIGN MASTER KEY VAULT v3.7 (NO-BLOCK LOGIC).
 * AUTHORITY: ARCHITECT XILON.
 * v3.7: Removed aggressive blacklisting. Keys recover in 15s instead of 3m.
 */
@Singleton
class SovereignKeyVault @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository
) {
    private val vaultScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val currentGeminiIndex = AtomicInteger(0)
    private val keyHealth = ConcurrentHashMap<String, Boolean>()

    fun getKey(provider: String): String {
        val p = provider.uppercase()
        if (p == "GEMINI") return getActiveGeminiKey()
        
        val custom = globalKnowledge.customKeys.value
        val customKey = custom[p] ?: custom["custom_$p".lowercase()]
        if (!customKey.isNullOrBlank()) return customKey

        return when(p) {
            "NASA" -> BuildConfig.NASA_API_KEY
            "OPENAI" -> BuildConfig.OPENAI_API_KEY
            "GROK" -> BuildConfig.GROK_API_KEY
            "DEEPSEEK" -> BuildConfig.DEEPSEEK_API_KEY
            "NVIDIA" -> BuildConfig.NVIDIA_API_KEY
            "ANTHROPIC", "CLAUDE" -> BuildConfig.ANTHROPIC_API_KEY
            else -> ""
        }
    }

    private fun getAllGeminiPool(): List<String> {
        val custom = globalKnowledge.customKeys.value
        val pool = mutableSetOf<String>()
        
        fun addIfValid(key: String?) {
            if (!key.isNullOrBlank() && key.length > 10) pool.add(key.trim())
        }
        
        addIfValid(custom["GEMINI_1"])
        addIfValid(custom["GEMINI_2"])
        addIfValid(custom["GEMINI_3"])
        addIfValid(BuildConfig.GEMINI_API_KEY)
        addIfValid(BuildConfig.GEMINI_API_KEY_2)
        addIfValid(BuildConfig.GEMINI_API_KEY_3)
        
        return pool.toList()
    }

    fun getPoolSize(): Int = getAllGeminiPool().size

    private fun getActiveGeminiKey(): String {
        val pool = getAllGeminiPool()
        if (pool.isEmpty()) return ""
        
        val index = currentGeminiIndex.get() % pool.size
        // Try to find a healthy key, but if all fail, return the next one anyway (No hard block)
        var attempts = 0
        while (attempts < pool.size) {
            val key = pool[(index + attempts) % pool.size]
            if (keyHealth[key] != false) return key
            attempts++
        }
        return pool[index] // Return even if "unhealthy" to avoid skipping
    }

    fun reportKeyFailure(key: String, reason: String) {
        if (key.isEmpty()) return
        keyHealth[key] = false
        Timber.w("KEY_VAULT: Key ${key.take(5)} reported error: $reason. Rotating.")
        currentGeminiIndex.incrementAndGet()
        
        vaultScope.launch {
            delay(15000) // Fast recovery (15s) for network jitters
            keyHealth[key] = true
        }
    }

    fun getActiveKey(): String = getActiveGeminiKey()
    fun getProviderKey(p: String): String = getKey(p)
}
