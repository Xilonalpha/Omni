package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.data.models.*
import com.chemscanner.omniscient.marrow.repository.FootballRepository
import com.chemscanner.omniscient.marrow.repository.NegotiationResult
import com.chemscanner.omniscient.marrow.services.CompetitionService
import com.chemscanner.omniscient.marrow.services.LeagueEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CareerUiState(
    val myTeam: Team? = null,
    val leagueTable: List<LeagueEntry> = emptyList(),
    val transferList: List<FootballPlayer> = emptyList(),
    val nextOpponent: Team? = null,
    val budget: Long = 0,
    val currentLeagueName: String = "",
    val myPlayers: List<FootballPlayer> = emptyList(),
    val negotiatingPlayer: FootballPlayer? = null,
    val negotiationResult: NegotiationResult? = null
)

@HiltViewModel
class CareerViewModel @Inject constructor(
    private val repository: FootballRepository,
    private val competitionService: CompetitionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CareerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCareerData()
    }

    private fun loadCareerData() {
        viewModelScope.launch {
            combine(
                repository.teams,
                competitionService.currentLeagueTable
            ) { teams, table ->
                val myTeam = teams.find { it.id == "RO1" } // Default to FCSB
                val league = repository.leagues.value.find { it.id == myTeam?.leagueId }
                val myPlayers = myTeam?.players?.sortedByDescending { it.isStartingXI } ?: emptyList()
                
                _uiState.value.copy(
                    myTeam = myTeam,
                    leagueTable = table,
                    transferList = teams.flatMap { it.players }.filter { it.teamId != myTeam?.id }.shuffled().take(10),
                    nextOpponent = teams.filter { it.id != myTeam?.id }.randomOrNull(),
                    budget = myTeam?.budget ?: 0,
                    currentLeagueName = league?.name ?: "Unknown League",
                    myPlayers = myPlayers
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun togglePlayerSelection(player: FootballPlayer) {
        viewModelScope.launch {
            repository.toggleStartingStatus(player.id)
        }
    }

    fun startNegotiation(player: FootballPlayer) {
        _uiState.update { it.copy(negotiatingPlayer = player, negotiationResult = null) }
    }

    fun cancelNegotiation() {
        _uiState.update { it.copy(negotiatingPlayer = null, negotiationResult = null) }
    }

    fun submitOffer(offer: Long) {
        val player = _uiState.value.negotiatingPlayer ?: return
        val myTeam = _uiState.value.myTeam ?: return
        
        viewModelScope.launch {
            val result = repository.negotiateTransfer(player.id, myTeam.id, offer)
            _uiState.update { it.copy(negotiationResult = result) }
            
            if (result is NegotiationResult.Accepted) {
                // Transfer completed, close dialog after a short delay
                loadCareerData() // Refresh to see updated budget/roster
            }
        }
    }

    fun buyPlayer(player: FootballPlayer) {
        val myTeam = _uiState.value.myTeam ?: return
        if (myTeam.budget >= player.marketValue) {
            viewModelScope.launch {
                repository.transferPlayer(player.id, player.teamId, myTeam.id)
            }
        }
    }
}
