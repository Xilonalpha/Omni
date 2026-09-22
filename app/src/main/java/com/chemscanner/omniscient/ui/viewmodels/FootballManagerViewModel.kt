package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemscanner.omniscient.marrow.data.models.Stadium
import com.chemscanner.omniscient.marrow.data.models.Team
import com.chemscanner.omniscient.marrow.repository.FootballRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManagerUiState(
    val teams: List<Team> = emptyList(),
    val stadiums: List<Stadium> = emptyList(),
    val selectedUserTeam: Team? = null,
    val selectedOpponentTeam: Team? = null,
    val selectedStadium: Stadium? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class FootballManagerViewModel @Inject constructor(
    private val repository: FootballRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagerUiState())
    val uiState: StateFlow<ManagerUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.teams.collect { teams ->
                _uiState.value = _uiState.value.copy(
                    teams = teams,
                    stadiums = repository.getStadiums(),
                    selectedUserTeam = teams.firstOrNull(),
                    selectedOpponentTeam = teams.getOrNull(1),
                    selectedStadium = repository.getStadiums().firstOrNull()
                )
            }
        }
    }

    fun selectUserTeam(team: Team) {
        _uiState.value = _uiState.value.copy(selectedUserTeam = team)
    }

    fun selectOpponentTeam(team: Team) {
        _uiState.value = _uiState.value.copy(selectedOpponentTeam = team)
    }

    fun selectStadium(stadium: Stadium) {
        _uiState.value = _uiState.value.copy(selectedStadium = stadium)
    }

    fun executeTransfer(playerId: String, fromTeamId: String, toTeamId: String) {
        viewModelScope.launch {
            repository.transferPlayer(playerId, fromTeamId, toTeamId)
        }
    }
}
