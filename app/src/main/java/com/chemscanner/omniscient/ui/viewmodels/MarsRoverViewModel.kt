package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.SystemEvent
import com.chemscanner.omniscient.marrow.services.MarsRoverService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarsRoverUiState(
    val latestRoverImages: List<RoverPhoto> = emptyList(),
    val activeRovers: List<String> = listOf("Curiosity", "Perseverance"),
    val lastSyncTime: Long = 0,
    val isSyncing: Boolean = false,
    val martianEvents: List<SystemEvent> = emptyList()
)

data class RoverPhoto(
    val roverName: String,
    val imgUrl: String,
    val sol: Int,
    val earthDate: String,
    val hash: String = ""
)

@HiltViewModel
class MarsRoverViewModel @Inject constructor(
    private val marsRoverService: MarsRoverService,
    private val globalKnowledge: GlobalKnowledgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarsRoverUiState())
    val uiState: StateFlow<MarsRoverUiState> = _uiState.asStateFlow()

    init {
        observeMartianTelemetry()
    }

    private fun observeMartianTelemetry() {
        viewModelScope.launch {
            globalKnowledge.events.collectLatest { allEvents ->
                val marsEvents = allEvents.filter { it.module == "MARS_ROVER" || it.module == "MARTIAN_VISION" }
                
                // Extragem informațiile despre poze din evenimentele arhivare (XilonProf/Events)
                // În mod normal am avea un Repository de date, dar aici folosim fluxul de evenimente "Marrow"
                val newPhotos = marsEvents.mapNotNull { event ->
                    if (event.description.contains("captured by")) {
                        extractRoverPhoto(event.description, event.timestamp)
                    } else null
                }.distinctBy { it.imgUrl }.take(10)

                _uiState.update { state ->
                    state.copy(
                        latestRoverImages = if (newPhotos.isNotEmpty()) newPhotos else state.latestRoverImages,
                        martianEvents = marsEvents.reversed(),
                        lastSyncTime = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            marsRoverService.startSyncing()
            // Simulăm un delay de feedback vizual
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(isSyncing = false) }
        }
    }

    private fun extractRoverPhoto(description: String, timestamp: Long): RoverPhoto? {
        return try {
            // "New image captured by Perseverance on Sol 1234 (Earth Date: 2024-01-01). Source: http://..."
            val name = description.substringAfter("by ").substringBefore(" on")
            val sol = description.substringAfter("Sol ").substringBefore(" (").toInt()
            val earthDate = description.substringAfter("Date: ").substringBefore(")")
            val url = description.substringAfter("Source: ")
            
            // Folosim timestamp-ul pentru a genera un hash unic dacă URL-ul lipsește sau e identic
            val photoHash = "img_${timestamp}_${name.hashCode()}"
            
            RoverPhoto(name, url, sol, earthDate, hash = photoHash)
        } catch (e: Exception) { null }
    }
}
