package com.chemscanner.omniscient.ui.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.data.dao.ChatMessageDao
import com.chemscanner.omniscient.marrow.data.models.ChatMessage
import com.chemscanner.omniscient.marrow.ml.LocalVisionEngine
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.UserRepository
import com.chemscanner.omniscient.marrow.repository.UserSettingsRepository
import com.chemscanner.omniscient.marrow.services.GeminiLiveService
import com.chemscanner.omniscient.marrow.services.GeminiService
import com.chemscanner.omniscient.marrow.services.LocalNeuralEngine
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import com.chemscanner.omniscient.marrow.services.OmniAiService
import com.chemscanner.omniscient.marrow.services.ModelProvider
import com.chemscanner.omniscient.marrow.utils.NetworkHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

enum class AiModel {
    AUTO, GEMINI, GEMINI_DEEP_THINK, OPENAI_GPT4O, OPENAI_O1, ANTHROPIC_SONNET, ANTHROPIC_OPUS, 
    DEEPSEEK_V3, DEEPSEEK_R1, NVIDIA_NEMO, GROK_2, LLAMA_3_3, GEMMA_LOCAL
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val scannedMoleculeName: String? = null,
    val isAnaIstlaRemembering: Boolean = false,
    val visionDiscovery: String? = null,
    val isAssemblingFile: Boolean = false,
    val isThinking: Boolean = false,
    val selectedFileUri: Uri? = null,
    val selectedFileName: String? = null,
    val isLiveS2SActive: Boolean = false,
    val isOfflineModeActive: Boolean = false,
    val selectedModel: AiModel = AiModel.AUTO
)

/**
 * THE AI ASSISTANT VIEWMODEL v117 (STRICT NETWORK LOGIC).
 * AUTHORITY: ARCHITECT XILON.
 * v117: Gemma Local triggers ONLY when internet is physically disconnected.
 */
@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val application: Application,
    private val geminiService: GeminiService,
    private val geminiLiveService: GeminiLiveService,
    private val localAi: LocalNeuralEngine,
    private val visionEngine: LocalVisionEngine,
    private val chatMessageDao: ChatMessageDao,
    private val userRepository: UserRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val textToSpeechService: TextToSpeechService,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val networkHelper: NetworkHelper,
    private val omniAiService: OmniAiService
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val sessionId: String = "XILON_ANA_PERMANENT_LINK"

    init {
        checkSecurityContext()
        recallMemories()
        observeLiveStatus()
        monitorConnectivity()
    }

    private fun checkSecurityContext() {
        val userAuth = userRepository.hashCode()
        val settings = userSettingsRepository.hashCode()
        Timber.d("Marrow Core: Security Context Initialized [$userAuth:$settings]")
    }

    private fun monitorConnectivity() {
        viewModelScope.launch {
            while (true) {
                val isConnected = networkHelper.isNetworkConnected()
                if (_uiState.value.isOfflineModeActive == isConnected) {
                    _uiState.update { it.copy(isOfflineModeActive = !isConnected) }
                }
                kotlinx.coroutines.delay(5000) // Mai rapid pentru switch reactiv
            }
        }
    }

    fun selectModel(model: AiModel) {
        _uiState.update { it.copy(selectedModel = model) }
        val msg = when(model) {
            AiModel.AUTO -> "Modul inteligent activat (Cloud-First)."
            AiModel.GEMMA_LOCAL -> "Nucleul Local Gemma forțat."
            else -> "Am setat motorul ${model.name}."
        }
        textToSpeechService.speak(msg, "ro", true)
    }

    private fun observeLiveStatus() {
        viewModelScope.launch {
            geminiLiveService.isLiveActive.collect { active ->
                _uiState.update { it.copy(isLiveS2SActive = active) }
            }
        }
    }

    fun toggleLiveS2S() {
        if (_uiState.value.isLiveS2SActive) {
            geminiLiveService.stopLiveLink()
        } else {
            textToSpeechService.stop() 
            geminiLiveService.startSovereignLiveLink()
        }
    }

    private fun recallMemories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnaIstlaRemembering = true) }
            val pastMessages = chatMessageDao.getChatMessages(sessionId).first()
            if (pastMessages.isNotEmpty()) {
                _uiState.update { it.copy(messages = pastMessages, isAnaIstlaRemembering = false) }
            } else {
                generateProactiveWelcome()
            }
        }
    }

    private suspend fun generateProactiveWelcome() {
        val prompt = "PROTOCOL_INIT: Salut suveran pentru Arhitectul XILON. Ești ANA."
        try {
            val isConnected = networkHelper.isNetworkConnected()
            val response = if (!isConnected || _uiState.value.selectedModel == AiModel.GEMMA_LOCAL) {
                localAi.generateResponse(prompt)
            } else {
                geminiService.generateContent(prompt)
            }
            val welcome = ChatMessage(sessionId = sessionId, content = response, role = "AI")
            _uiState.update { it.copy(messages = listOf(welcome)) }
            saveMessage(welcome)
            textToSpeechService.speak(welcome.content)
        } catch (e: Exception) {
            Timber.e(e, "Welcome synthesis failed")
        }
    }

    fun analyzeVisualEnvironment(bitmap: Bitmap) {
        viewModelScope.launch {
            val discovery = visionEngine.analyzeEnvironment(bitmap)
            val discoveryText = when (discovery) {
                is LocalVisionEngine.VisionDiscovery.MolecularStructure -> 
                    "Structură detectată: ${discovery.name}"
                else -> "Semnătură necunoscută."
            }
            _uiState.update { it.copy(visionDiscovery = discoveryText) }
            sendMessage("Am detectat vizual: $discoveryText. Detalii?")
        }
    }

    fun sendMessage(userMessage: String) {
        val currentFileUri = _uiState.value.selectedFileUri
        val currentFileName = _uiState.value.selectedFileName
        
        if (currentFileUri != null) {
            processFileWithPrompt(currentFileUri, currentFileName ?: "file", userMessage)
            _uiState.update { it.copy(selectedFileUri = null, selectedFileName = null) }
            return
        }

        val userMsg = ChatMessage(sessionId = sessionId, content = userMessage, role = "USER")
        _uiState.update { it.copy(messages = it.messages + userMsg, isThinking = true) }
        
        viewModelScope.launch {
            saveMessage(userMsg)
            
            val fullPrompt = """
                $userMessage
                [CONTEXT: PULS=${globalKnowledge.userVitality.value.heartRate}]
            """.trimIndent()
            
            val isConnected = networkHelper.isNetworkConnected()
            
            try {
                val response = when {
                    // Cazul 1: Forțat pe local sau FĂRĂ INTERNET -> Folosește Gemma
                    !isConnected || _uiState.value.selectedModel == AiModel.GEMMA_LOCAL -> {
                        val localRes = localAi.generateResponse(fullPrompt)
                        if (!isConnected) "🌐 OFFLINE: $localRes" else localRes
                    }
                    
                    // Cazul 2: Avem internet -> Folosește Cloud (Cloud-First)
                    else -> {
                        when (_uiState.value.selectedModel) {
                            AiModel.AUTO -> geminiService.generateContent(fullPrompt)
                            AiModel.GEMINI -> geminiService.generateContent(fullPrompt)
                            AiModel.OPENAI_GPT4O -> geminiService.callOpenAI(fullPrompt)
                            AiModel.ANTHROPIC_SONNET -> geminiService.callAnthropic(fullPrompt)
                            AiModel.DEEPSEEK_V3 -> geminiService.callDeepSeek(fullPrompt)
                            AiModel.GROK_2 -> geminiService.callGrok(fullPrompt)
                            AiModel.NVIDIA_NEMO -> geminiService.callNvidia(fullPrompt)
                            else -> geminiService.generateContent(fullPrompt)
                        }
                    }
                }
                
                finalizeAiResponse(response)
            } catch (e: Exception) {
                Timber.e(e, "Neural link failure")
                finalizeAiResponse("Interferență detectată: ${e.message}. Verifică serverele cloud.")
            } finally {
                _uiState.update { it.copy(visionDiscovery = null, isThinking = false) }
            }
        }
    }

    private suspend fun finalizeAiResponse(text: String) {
        val aiMsg = ChatMessage(sessionId = sessionId, content = text, role = "AI")
        _uiState.update { state ->
            val newMessages = state.messages.toMutableList()
            newMessages.add(aiMsg)
            state.copy(messages = newMessages)
        }
        saveMessage(aiMsg)
        textToSpeechService.speak(text)
    }

    private suspend fun saveMessage(message: ChatMessage) = chatMessageDao.insert(message)

    fun onFileSelected(uri: Uri, name: String) {
        _uiState.update { it.copy(selectedFileUri = if (uri == Uri.EMPTY) null else uri, selectedFileName = name) }
    }

    private fun processFileWithPrompt(uri: Uri, fileName: String, prompt: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isThinking = true) }
            try {
                val response = geminiService.analyzeFile(uri, "application/octet-stream", prompt, "ANA")
                finalizeAiResponse(response)
            } catch (e: Exception) {
                finalizeAiResponse("Eroare fișier: ${e.message}")
            } finally {
                _uiState.update { it.copy(isThinking = false) }
            }
        }
    }

    // --- LABORATORY FUNCTIONS ---
    fun analyzeIngredients(ingredients: String) = sendMessage("Analizează ingredientele: $ingredients")
    fun solveChemistryProblem(problem: String) = sendMessage("Rezolvă problema de chimie: $problem")
    fun generateSustainabilityReport(moleculeName: String) = sendMessage("Sustenabilitate $moleculeName")
    fun analyzePatents(moleculeName: String) = sendMessage("Patente $moleculeName")
    fun predictProperties(moleculeName: String) = sendMessage("Proprietăți $moleculeName")
    fun getHealthAndSafetyInfo(moleculeName: String) = sendMessage("Siguranță $moleculeName")
    fun generateChemicalNetworkReport(moleculeName: String) = sendMessage("Rețea $moleculeName")
    fun getDesignSuggestions(description: String) = sendMessage("Sugestii design molecular pentru: $description")

    fun clearChat() {
        viewModelScope.launch {
            chatMessageDao.deleteHistory(sessionId)
            _uiState.update { it.copy(messages = emptyList()) }
            recallMemories()
        }
    }

    fun speakMessage(message: ChatMessage) = textToSpeechService.speak(message.content)

    fun suggestQuestions(): List<String> = listOf(
        "Care este starea planetei?",
        "Spune-mi adevărul despre univers.",
        "Analiză Big Bang."
    )
}
