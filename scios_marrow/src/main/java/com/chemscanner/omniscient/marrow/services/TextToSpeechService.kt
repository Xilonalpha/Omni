package com.chemscanner.omniscient.marrow.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TEXT TO SPEECH SERVICE v3.2 (HYBRID RESILIENCE).
 * AUTHORITY: ARCHITECT XILON.
 * v3.2: Restored English Fallback for extreme reliability while maintaining RO as primary.
 */
@Singleton
class TextToSpeechService @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts = TextToSpeech(context, this)
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setLanguage("ro") 
        } else {
            Timber.e("Marrow: TTS Init failed")
        }
    }

    fun setLanguage(langCode: String) {
        val locale = when (langCode.lowercase()) {
            "ro" -> Locale("ro", "RO")
            "it" -> Locale.ITALIAN
            "fr" -> Locale.FRENCH
            "en" -> Locale.ENGLISH
            else -> Locale("ro", "RO")
        }
        
        tts?.let { engine ->
            val result = engine.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // XILON: Restoring English Fallback as requested for maximum safety
                engine.setLanguage(Locale.US)
                Timber.w("TTS: $langCode not supported, falling back to US English for safety.")
            }
            engine.setPitch(0.95f) // Voce ușor mai profundă pentru XILON
            engine.setSpeechRate(1.0f)
        }
    }

    /**
     * Curăță textul de simboluri Markdown (asteriscuri, diezuri) înainte de a vorbi.
     */
    private fun cleanTextForSpeech(text: String): String {
        return text.replace("*", "")
            .replace("#", "")
            .replace("_", "")
            .replace("`", "")
            .trim()
    }

    fun speak(text: String, langCode: String = "ro", useQueue: Boolean = false) {
        if (!isInitialized || text.isBlank()) return
        
        val cleanText = cleanTextForSpeech(text)
        setLanguage(langCode)
        
        val queueMode = if (useQueue) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH
        tts?.speak(cleanText, queueMode, null, "MARROW_VOICE_${System.currentTimeMillis()}")
    }

    fun stop() { 
        tts?.stop() 
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        isInitialized = false
        _isSpeaking.value = false
    }
}
