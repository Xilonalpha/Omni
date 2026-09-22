package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.data.models.ChatMessage
import com.chemscanner.omniscient.marrow.services.GeminiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    private val geminiService: GeminiService
) : ViewModel() {

    private val _chatHistory = MutableLiveData<List<ChatMessage>>(emptyList())
    val chatHistory: LiveData<List<ChatMessage>> = _chatHistory

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val sessionId = UUID.randomUUID().toString()

    init {
        // Add initial greeting message from the AI
        val initialMessage = ChatMessage(
            content = "Hello! I am your chemistry assistant. How can I help you today?",
            role = "AI",
            sessionId = sessionId
        )
        _chatHistory.value = listOf(initialMessage)
    }

    fun sendMessage(messageText: String) {
        val userMessage = ChatMessage(content = messageText, role = "USER", sessionId = sessionId)
        _chatHistory.value = _chatHistory.value?.plus(userMessage)

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = geminiService.generateContent(messageText)
                val aiMessage = ChatMessage(content = response, role = "AI", sessionId = sessionId)
                _chatHistory.value = _chatHistory.value?.plus(aiMessage)
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    content = "Sorry, I encountered an error: ${e.message}",
                    role = "AI",
                    sessionId = sessionId
                )
                _chatHistory.value = _chatHistory.value?.plus(errorMessage)
            }
            _isLoading.value = false
        }
    }

    fun generateSustainabilityReport(moleculeName: String) {
        val prompt = """
        Generate a 'Sustainability Report' for the molecule: $moleculeName.
        The report should be structured and cover the following points in detail:

        1.  **Green Chemistry & Production:**
            *   Describe known 'green' synthesis methods.
            *   Mention traditional methods and their environmental impact for comparison.

        2.  **Petrochemical & Clean Energy Applications:**
            *   Detail its role in sustainable petrochemistry.
            *   Discuss its applications in clean energy sectors (e.g., green hydrogen production/storage, carbon capture, advanced batteries).

        3.  **Advanced & Sustainable Materials:**
            *   Suggest its use in creating high-end, sustainable materials (e.g., bio-based polymers for luxury construction, yachts, or eco-friendly buildings).
            *   Provide examples of alternative, greener materials that could serve a similar purpose.

        4.  **Lifecycle & Ecological Impact:**
            *   Information on toxicity, biodegradability, and overall environmental footprint.

        Please provide a comprehensive but concise report.
        """

        val userMessage = ChatMessage(content = "Generating sustainability report for $moleculeName...", role = "USER", sessionId = sessionId)
        _chatHistory.value = _chatHistory.value?.plus(userMessage)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = geminiService.generateContent(prompt)
                val aiMessage = ChatMessage(content = response, role = "AI", sessionId = sessionId)
                _chatHistory.value = _chatHistory.value?.plus(aiMessage)
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    content = "Sorry, I couldn't generate the sustainability report: ${e.message}",
                    role = "AI",
                    sessionId = sessionId
                )
                _chatHistory.value = _chatHistory.value?.plus(errorMessage)
            }
            _isLoading.value = false
        }
    }
}
