package com.chemscanner.omniscient.marrow.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MasterLockState
import com.chemscanner.omniscient.marrow.repository.VoiceMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE SENTIENT MASTER VOICE MANAGER v2.0 (SOVEREIGN UPDATE).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Zero-API Sentient Dialogue.
 * v2.0: ELIMINATED GOOGLE GEMINI. Now powered by Xilon Neural Lattice.
 */
@Singleton
class MasterVoiceManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val neuralLattice: XilonNeuralLatticeService, // Suveranitatea locală
    private val bioBridge: BioBridgeService,
    private val securityService: BiometricSecurityService,
    private val realSignalGateway: RealSignalGateway,
    private val systemAuthority: AndroidSystemAuthority
) : RecognitionListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    
    private var isListening = false
    private var isAnaTalking = false
    private var lastRmsValue: Float = 0f

    private val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ro-RO")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    init {
        observeAnaSpeech()
    }

    private fun observeAnaSpeech() {
        scope.launch {
            ttsService.isSpeaking.collectLatest { speaking ->
                isAnaTalking = speaking
                if (!speaking) {
                    delay(500)
                    startListening()
                } else {
                    stopListeningInternal()
                }
            }
        }
    }

    fun startListening() {
        if (isAnaTalking || isListening) return
        
        mainHandler.post {
            try { 
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(this@MasterVoiceManager)
                    }
                }
                isListening = true
                realSignalGateway.pauseAcousticMonitoring()
                speechRecognizer?.startListening(recognizerIntent) 
                globalKnowledge.updateVoiceMode(VoiceMode.ACTIVE)
            } catch (e: Exception) {
                isListening = false
                Timber.e("Start Listening Fault")
            }
        }
    }

    private fun stopListeningInternal() {
        isListening = false
        mainHandler.post {
            speechRecognizer?.stopListening()
        }
    }

    override fun onResults(results: Bundle?) {
        isListening = false
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull() ?: ""
        
        realSignalGateway.resumeAcousticMonitoring()
        
        if (text.isNotBlank()) {
            processMasterVoice(text)
        }
        
        if (!isAnaTalking) {
            mainHandler.postDelayed({ startListening() }, 800)
        }
    }

    fun processMasterVoice(text: String) {
        val lowerText = text.lowercase(Locale.ROOT)
        
        if (globalKnowledge.lockState.value == MasterLockState.VOICE_RECOGNITION_PENDING) {
            val frequencyProxy = 80f + (lastRmsValue * 10f).coerceIn(0f, 170f)
            securityService.verifyVoiceSignature(text, frequencyProxy)
        }

        // Logică de confirmare acțiuni
        val pendingAction = globalKnowledge.voiceState.value.pendingAction
        if (pendingAction != null) {
            if (lowerText.contains("da") || lowerText.contains("confirm")) {
                if (pendingAction.module == "BIO_BRIDGE") {
                    bioBridge.executeSynthesis(pendingAction.id.removePrefix("SYNTH_"))
                }
                globalKnowledge.clearPendingAction()
                return
            }
        }

        if (lowerText.contains("taci") || lowerText.contains("stop")) {
            ttsService.stop()
            return
        }

        // Generăm dialog prin rețeaua suverană
        generateSovereignDialogue(text)
    }

    private fun generateSovereignDialogue(userInput: String) {
        scope.launch {
            try {
                // Cerem Lattice-ului un răspuns care să includă și datele senzoriale curente
                val response = neuralLattice.computeSovereignIntelligence(userInput)
                if (response.isNotBlank()) {
                    val cleanResponse = extractAndExecuteAction(response)
                    ttsService.speak(cleanResponse)
                }
            } catch (e: Exception) {
                Timber.e(e, "Sovereign Dialogue Error")
            }
        }
    }

    private fun extractAndExecuteAction(response: String): String {
        return try {
            val startIdx = response.indexOf("{")
            val endIdx = response.lastIndexOf("}")
            
            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                val jsonStr = response.substring(startIdx, endIdx + 1)
                val json = JSONObject(jsonStr)
                val action = json.getString("action")
                val paramsObj = json.optJSONObject("params")
                
                val params = mutableMapOf<String, String>()
                paramsObj?.keys()?.forEach { key -> params[key] = paramsObj.getString(key) }
                
                mainHandler.post { systemAuthority.executeAction(action, params) }
                
                response.substring(0, startIdx).trim() + " " + response.substring(endIdx + 1).trim()
            } else {
                response
            }
        } catch (e: Exception) {
            response
        }
    }

    override fun onRmsChanged(rmsdB: Float) { lastRmsValue = rmsdB }
    override fun onError(error: Int) { 
        isListening = false
        mainHandler.postDelayed({ if (!isAnaTalking) startListening() }, 1000)
    }
    
    override fun onReadyForSpeech(p0: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onBufferReceived(p0: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(p0: Bundle?) {}
    override fun onEvent(p0: Int, p1: Bundle?) {}

    fun stop() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
    }
}
