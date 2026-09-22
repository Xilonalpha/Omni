package com.chemscanner.omniscient.marrow.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

data class XilonDiscovery(
    val timestamp: Long,
    val module: String,
    val content: String,
    val importance: Int,
    val cosmicContext: String? = null
)

@Singleton
class XilonProfManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val fileName = "XilonProf_Discoveries.json"
    private val logFile = File(context.filesDir, fileName)

    private val _discoveries = MutableStateFlow<List<XilonDiscovery>>(emptyList())
    val discoveries: StateFlow<List<XilonDiscovery>> = _discoveries.asStateFlow()

    init {
        loadDiscoveries()
    }

    private fun loadDiscoveries() {
        _discoveries.value = readAllDiscoveries()
    }

    suspend fun recordDiscovery(module: String, content: String, importance: Int, contextSummary: String? = null) = withContext(Dispatchers.IO) {
        try {
            val existingData = _discoveries.value.toMutableList()
            
            // Deduplication logic: avoid recording the same content twice in 1 minute
            val now = System.currentTimeMillis()
            val isDuplicate = existingData.any { 
                it.content == content && (now - it.timestamp) < 60000 
            }

            if (isDuplicate) return@withContext

            val entry = XilonDiscovery(
                timestamp = now,
                module = module,
                content = content,
                importance = importance,
                cosmicContext = contextSummary
            )

            existingData.add(0, entry) // Add at start for latest-first
            _discoveries.value = existingData

            saveToFile(existingData)
        } catch (e: Exception) {
            Timber.e(e, "XilonProf: Error recording discovery")
        }
    }

    private fun readAllDiscoveries(): List<XilonDiscovery> {
        return if (!logFile.exists()) emptyList()
        else {
            try {
                val jsonString = logFile.readText()
                val type = object : com.google.gson.reflect.TypeToken<List<XilonDiscovery>>() {}.type
                gson.fromJson(jsonString, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun saveToFile(data: List<XilonDiscovery>) {
        try {
            FileWriter(logFile).use { writer ->
                gson.toJson(data, writer)
            }
        } catch (e: Exception) {
            Timber.e(e, "XilonProf: Error saving to file")
        }
    }

    fun clearDiscoveries() {
        logFile.delete()
        _discoveries.value = emptyList()
    }
}
