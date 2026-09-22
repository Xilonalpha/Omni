package com.chemscanner.omniscient.marrow.services

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GEMINI LIVE S2S SERVICE v1.5 (FIXED HILT INJECTION).
 * AUTHORITY: ARCHITECT XILON.
 */
@Singleton
class GeminiLiveService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val masterVoice: MasterVoiceManager, // FIXED: Changed Service to Manager
    private val keyVault: SovereignKeyVault
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _isLiveActive = MutableStateFlow(false)
    val isLiveActive = _isLiveActive.asStateFlow()

    private val SAMPLE_RATE = 16000
    private val CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO
    private val CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE_IN = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG_IN, AUDIO_FORMAT)
    
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var liveJob: Job? = null

    private fun getSovereignModel(): GenerativeModel {
        return GenerativeModel(
            modelName = ModelProvider.GEMINI_MODEL_NAME,
            apiKey = keyVault.getActiveKey()
        )
    }

    @SuppressLint("MissingPermission")
    fun startSovereignLiveLink() {
        if (_isLiveActive.value) return
        _isLiveActive.value = true
        
        liveJob = scope.launch {
            try {
                audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG_IN, AUDIO_FORMAT, BUFFER_SIZE_IN)
                
                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    throw IllegalStateException("AudioRecord initialization failed.")
                }

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANT).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                    .setAudioFormat(AudioFormat.Builder().setEncoding(AUDIO_FORMAT).setSampleRate(SAMPLE_RATE).setChannelMask(CHANNEL_CONFIG_OUT).build())
                    .setBufferSizeInBytes(BUFFER_SIZE_IN)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioRecord?.startRecording()
                audioTrack?.play()

                val buffer = ByteArray(BUFFER_SIZE_IN)
                while (isActive && _isLiveActive.value) {
                    audioRecord?.read(buffer, 0, buffer.size)
                    yield()
                }
            } catch (e: Exception) {
                Timber.e(e, "S2S Failure")
                stopLiveLink()
            }
        }
    }

    fun stopLiveLink() {
        _isLiveActive.value = false
        liveJob?.cancel()
        
        try {
            audioRecord?.let {
                if (it.state == AudioRecord.STATE_INITIALIZED) {
                    if (it.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        it.stop()
                    }
                }
                it.release()
            }
        } catch (e: Exception) { Timber.v("AudioRecord cleanup: ${e.message}") }
        audioRecord = null
        
        try {
            audioTrack?.let {
                if (it.state == AudioTrack.STATE_INITIALIZED) {
                    it.stop()
                    it.release()
                }
            }
        } catch (e: Exception) { Timber.v("AudioTrack cleanup: ${e.message}") }
        audioTrack = null
        
        masterVoice.startListening()
    }
}
