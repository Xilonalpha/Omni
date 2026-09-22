package com.chemscanner.omniscient.marrow.services

import android.content.Context
import android.graphics.Bitmap
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE LOCAL NEURAL ENGINE v5.2 (SOVEREIGN VISION).
 * AUTHORITY: ARCHITECT XILON.
 * v5.2: Restored analyzeImageLocal for Bio-Age Chronos compatibility.
 */
@Singleton
class LocalNeuralEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val knowledgeBase: SovereignKnowledgeBase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var llmInference: LlmInference? = null
    private val inferenceMutex = Mutex()
    
    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded

    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    private companion object {
        const val MODEL_FILENAME = "gemma-2b-it-cpu-int4.bin"
        const val MIN_MODEL_SIZE = 500L * 1024L * 1024L 
    }

    init {
        checkModelFilePresence()
    }

    private fun checkModelFilePresence() {
        scope.launch(Dispatchers.IO) {
            val targetFile = File(context.filesDir, MODEL_FILENAME)
            if (targetFile.exists() && targetFile.length() > MIN_MODEL_SIZE) {
                _isModelLoaded.value = true
            }
        }
    }

    private suspend fun ensureEngineReady(): Boolean {
        if (llmInference != null) return true
        return inferenceMutex.withLock {
            if (llmInference != null) return@withLock true
            val targetFile = File(context.filesDir, MODEL_FILENAME)
            if (!targetFile.exists()) return@withLock false

            try {
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(targetFile.absolutePath)
                    .setMaxTokens(512)
                    .build()
                llmInference = LlmInference.createFromOptions(context, options)
                _isModelLoaded.value = true
                true
            } catch (e: Exception) {
                Timber.e(e, "Gemma Init Failed")
                false
            }
        }
    }

    /**
     * ANALIZĂ VIZUALĂ LOCALĂ: Transduce imaginea în context suveran.
     */
    suspend fun analyzeImageLocal(bitmap: Bitmap, userPrompt: String): String = withContext(Dispatchers.Default) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val labels = labeler.process(image).await()
            val visualContext = labels.take(5).joinToString(", ") { it.text }
            
            val augmentedPrompt = """
                [VIZIUNE_XNL]: Detectate elemente: $visualContext.
                [CONTEXT_BIOMETRIC]: Se analizează un chip uman pentru markeri epigenetici.
                [UTILIZATOR]: $userPrompt
                MISIUNE: Oferă un verdict suveran bazat pe aceste date vizuale brute.
            """.trimIndent()
            
            return@withContext generateResponse(augmentedPrompt)
        } catch (e: Exception) {
            return@withContext "EROARE VIZIUNE: Nucleul nu a putut decoda matricea vizuală."
        }
    }

    suspend fun generateResponse(prompt: String): String {
        if (prompt.isBlank()) return "Te ascult, Arhitectule."

        val heuristicResponse = knowledgeBase.synthesizeLogic(prompt)
        if (heuristicResponse != null) return heuristicResponse

        if (ensureEngineReady()) {
            try {
                return inferenceMutex.withLock {
                    withContext(Dispatchers.IO) { 
                        llmInference?.generateResponse(prompt) ?: "Eroare sinteză."
                    }
                }
            } catch (e: Exception) { Timber.e("Inference Fault") }
        }

        return "XNL analizează contextul în regim de siguranță. Datele sunt arhivate în Akasha."
    }
}
