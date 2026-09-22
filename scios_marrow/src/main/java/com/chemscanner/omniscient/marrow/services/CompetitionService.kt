package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.data.models.*
import com.chemscanner.omniscient.marrow.repository.FootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * SOVEREIGN COMPETITION ENGINE v2.1
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Manage League standings and Tournament brackets with real-time sync.
 */
@Singleton
class CompetitionService @Inject constructor(
    private val repository: FootballRepository
) {
    private val _currentLeagueTable = MutableStateFlow<List<LeagueEntry>>(emptyList())
    val currentLeagueTable: StateFlow<List<LeagueEntry>> = _currentLeagueTable

    private val _tournamentBracket = MutableStateFlow<List<MatchUp>>(emptyList())
    val tournamentBracket: StateFlow<List<MatchUp>> = _tournamentBracket

    private var activeLeagueId: String? = null

    /**
     * Initializes a new league season and populates the table.
     */
    fun startNewSeason(leagueId: String) {
        activeLeagueId = leagueId
        val teams = repository.teams.value.filter { it.leagueId == leagueId }
        _currentLeagueTable.value = teams.map { LeagueEntry(it.id, it.name) }
        
        // Also generate an initial tournament bracket for the Cup
        generateTournamentBracket(teams)
        Timber.d("New Season started for League: $leagueId. Table and Brackets initialized.")
    }

    /**
     * Records a manual match result (User vs AI) and updates standings.
     */
    fun recordMatchResult(userTeamId: String, opponentTeamId: String, userGoals: Int, opponentGoals: Int) {
        val table = _currentLeagueTable.value.toMutableList()
        updateEntry(table, userTeamId, userGoals, opponentGoals)
        updateEntry(table, opponentTeamId, opponentGoals, userGoals)
        _currentLeagueTable.value = table
        
        // If it's a cup match, update the bracket
        updateBracketResult(userTeamId, opponentTeamId, userGoals, opponentGoals)
        
        // Simulate other league matches to keep the table moving
        simulateOtherMatches(excludeTeams = listOf(userTeamId, opponentTeamId))
    }

    private fun updateEntry(table: MutableList<LeagueEntry>, teamId: String, goalsFor: Int, goalsAgainst: Int) {
        val entry = table.find { it.teamId == teamId } ?: return
        entry.played++
        entry.goalsFor += goalsFor
        entry.goalsAgainst += goalsAgainst
        when {
            goalsFor > goalsAgainst -> { entry.points += 3; entry.wins++ }
            goalsFor < goalsAgainst -> { }
            else -> { entry.points += 1; entry.draws++ }
        }
    }

    private fun generateTournamentBracket(teams: List<Team>) {
        val shuffled = teams.shuffled()
        val matchups = mutableListOf<MatchUp>()
        for (i in 0 until shuffled.size - 1 step 2) {
            matchups.add(MatchUp(shuffled[i].id, shuffled[i + 1].id, round = "Quarter-Finals"))
        }
        _tournamentBracket.value = matchups
    }

    private fun updateBracketResult(homeId: String, awayId: String, homeScore: Int, awayScore: Int) {
        val currentBrackets = _tournamentBracket.value.toMutableList()
        val matchIndex = currentBrackets.indexOfFirst { 
            (it.homeTeamId == homeId && it.awayTeamId == awayId) || 
            (it.homeTeamId == awayId && it.awayTeamId == homeId) 
        }
        
        if (matchIndex != -1) {
            val updatedMatch = currentBrackets[matchIndex].copy(
                homeScore = homeScore,
                awayScore = awayScore
            )
            currentBrackets[matchIndex] = updatedMatch
            _tournamentBracket.value = currentBrackets
            Timber.d("Tournament Bracket updated: $homeId vs $awayId -> $homeScore:$awayScore")
        }
    }

    fun simulateOtherMatches(excludeTeams: List<String>) {
        val leagueId = activeLeagueId ?: return
        val allTeamsInLeague = repository.teams.value.filter { it.leagueId == leagueId }
        val remainingTeams = allTeamsInLeague.filter { it.id !in excludeTeams }.shuffled()
        
        val table = _currentLeagueTable.value.toMutableList()
        for (i in 0 until remainingTeams.size - 1 step 2) {
            val home = remainingTeams[i]
            val away = remainingTeams[i + 1]
            val result = simulateAiMatch(home, away)
            updateEntry(table, home.id, result.first, result.second)
            updateEntry(table, away.id, result.second, result.first)
        }

        _currentLeagueTable.value = table.sortedWith(
            compareByDescending<LeagueEntry> { it.points }
                .thenByDescending { it.goalsFor - it.goalsAgainst }
                .thenByDescending { it.goalsFor }
        )
    }

    private fun simulateAiMatch(home: Team, away: Team): Pair<Int, Int> {
        val homePower = home.players.filter { it.isStartingXI }.map { it.stats.getOverall() }.let { if (it.isEmpty()) 70.0 else it.average() }
        val awayPower = away.players.filter { it.isStartingXI }.map { it.stats.getOverall() }.let { if (it.isEmpty()) 70.0 else it.average() }
        
        val diff = (homePower + 5) - awayPower
        var homeGoals = Random.nextInt(0, 4)
        var awayGoals = Random.nextInt(0, 3)

        if (diff > 10) homeGoals += 1
        if (diff < -10) awayGoals += 1
        
        return Pair(homeGoals, awayGoals)
    }
}

data class LeagueEntry(
    val teamId: String,
    val teamName: String,
    var played: Int = 0,
    var wins: Int = 0,
    var draws: Int = 0,
    var points: Int = 0,
    var goalsFor: Int = 0,
    var goalsAgainst: Int = 0
)

data class MatchUp(
    val homeTeamId: String,
    val awayTeamId: String,
    var homeScore: Int? = null,
    var awayScore: Int? = null,
    val round: String
)
