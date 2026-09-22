package com.chemscanner.omniscient.marrow.repository

import com.chemscanner.omniscient.marrow.data.dao.FootballDao
import com.chemscanner.omniscient.marrow.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.random.Random

sealed class NegotiationResult {
    data class Accepted(val finalPrice: Long) : NegotiationResult()
    data class CounterOffer(val suggestedPrice: Long) : NegotiationResult()
    object Rejected : NegotiationResult()
}

@Singleton
class FootballRepository @Inject constructor(
    private val footballDao: FootballDao
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams
    private val _leagues = MutableStateFlow<List<League>>(emptyList())
    val leagues: StateFlow<List<League>> = _leagues
    private val _competitions = MutableStateFlow<List<Competition>>(emptyList())
    val competitions: StateFlow<List<Competition>> = _competitions

    init { repositoryScope.launch { syncWithDatabase() } }

    private suspend fun syncWithDatabase() {
        val savedLeagues = footballDao.getAllLeagues().first()
        if (savedLeagues.isEmpty()) { 
            loadAndPersistInitialData() 
        } else {
            val savedTeams = footballDao.getAllTeams().first()
            _leagues.value = savedLeagues
            _teams.value = savedTeams.map { team -> 
                team.copy(players = footballDao.getPlayersByTeam(team.id).first()) 
            }
        }
        initializeCompetitions()
    }

    private fun initializeCompetitions() {
        _competitions.value = listOf(
            Competition("C1", "UEFA Champions League", CompetitionType.INTERNATIONAL_ELITE, 
                _teams.value.filter { it.id in listOf("PL1", "PL2", "LL1", "LL2", "SA1", "RO1", "BL1", "FR1") }.map { it.id }),
            Competition("C2", "UEFA Europa League", CompetitionType.INTERNATIONAL_SECONDARY, 
                _teams.value.filter { it.id in listOf("PL7", "LL3", "SA3", "RO2", "BL4") }.map { it.id })
        )
    }

    private suspend fun loadAndPersistInitialData() {
        val leaguesList = listOf(
            League("L1", "Premier League", "England"), League("L2", "La Liga", "Spain"),
            League("L3", "Serie A", "Italy"), League("L4", "Superliga României", "Romania"),
            League("L5", "Bundesliga", "Germany"), League("L6", "Ligue 1", "France")
        )
        footballDao.insertLeagues(leaguesList)
        _leagues.value = leaguesList

        val allTeams = mutableListOf<Team>()
        val allPlayers = mutableListOf<FootballPlayer>()

        val roTeams = listOf(
            Triple("RO1", "FCSB", 0xFFC8102EL), Triple("RO2", "CFR Cluj", 0xFF800000L),
            Triple("RO3", "Univ. Craiova", 0xFF0000FFL), Triple("RO4", "Rapid București", 0xFF800000L),
            Triple("RO5", "Farul Constanța", 0xFF000080L), Triple("RO6", "Sepsi OSK", 0xFFCC0000L),
            Triple("RO7", "Oțelul Galați", 0xFF0000FFL), Triple("RO8", "Hermannstadt", 0xFF000000L),
            Triple("RO9", "UTA Arad", 0xFFEE0000L), Triple("RO10", "Petrolul Ploiești", 0xFFFFFF00L),
            Triple("RO11", "U Cluj", 0xFF000000L), Triple("RO12", "Dinamo București", 0xFFEE0000L),
            Triple("RO13", "Poli Iași", 0xFF0000FFL), Triple("RO14", "FC Botoșani", 0xFF000080L),
            Triple("RO15", "Unirea Slobozia", 0xFFFFFF00L), Triple("RO16", "FCU 1948", 0xFF0000FFL)
        )
        roTeams.forEach { (id, name, color) -> 
            allTeams.add(Team(id, name, "L4", color, 0xFFFFFFFFL, budget = 5_000_000L))
            allPlayers.addAll(generatePlayers2526(id, name, 18))
        }

        val eliteTeams = listOf(
            Triple("PL1", "Man City", 0xFF6CABDDL), Triple("PL2", "Arsenal", 0xFFEF0107L),
            Triple("LL1", "Real Madrid", 0xFFFFFFFFL), Triple("LL2", "FC Barcelona", 0xFF004D98L),
            Triple("SA1", "Inter Milan", 0xFF0066B2L), Triple("BL1", "Bayern Munich", 0xFFDC052DL),
            Triple("FR1", "PSG", 0xFF004170L)
        )
        eliteTeams.forEach { (id, name, color) ->
            val lId = when { id.startsWith("PL") -> "L1"; id.startsWith("LL") -> "L2"; id.startsWith("SA") -> "L3"; id.startsWith("BL") -> "L5"; else -> "L6" }
            allTeams.add(Team(id, name, lId, color, 0xFFFFFFFFL, budget = 100_000_000L))
            allPlayers.addAll(generatePlayers2526(id, name, 18))
        }

        footballDao.insertTeams(allTeams)
        footballDao.insertPlayers(allPlayers)
        syncWithDatabase()
    }

    private fun generatePlayers2526(teamId: String, teamName: String, count: Int): List<FootballPlayer> {
        val baseNames = when(teamName) {
            "FCSB" -> listOf("Olaru", "Tănase", "Bîrligea", "Miculescu", "Chiricheș", "Târnovanu", "Radunovic", "Dawa", "Ngezana", "Crețu", "Sut")
            "Real Madrid" -> listOf("Mbappe", "Bellingham", "Vinicius", "Rodrygo", "Valverde", "Courtois", "Camavinga", "Tchouameni", "Militao", "Endrick", "Guler")
            "Man City" -> listOf("Haaland", "De Bruyne", "Rodri", "Foden", "Bernardo", "Ederson", "Dias", "Walker")
            else -> List(11) { "Star ${it + 1}" }
        }
        return List(count) { i ->
            val isElite = !teamId.startsWith("RO")
            val baseStat = if (isElite) 75f + Random.nextInt(15) else 60f + Random.nextInt(15)
            FootballPlayer(
                id = "${teamId}_P$i", name = if (i < baseNames.size) baseNames[i] else "Reserve ${i - 10}",
                role = when { i == 5 || i == 11 -> PlayerRole.GOALKEEPER; i in 0..4 || i in 12..14 -> PlayerRole.DEFENDER; i in 6..8 || i in 15..16 -> PlayerRole.MIDFIELDER; else -> PlayerRole.STRIKER },
                stats = PlayerStats(
                    speed = baseStat + Random.nextInt(10),
                    shooting = baseStat + Random.nextInt(10),
                    passing = baseStat + Random.nextInt(10),
                    dribbling = baseStat + Random.nextInt(10),
                    defending = baseStat + Random.nextInt(10),
                    physical = baseStat + Random.nextInt(10),
                    power = 70f + Random.nextInt(20),
                    reaction = 75f + Random.nextInt(15)
                ),
                marketValue = if (isElite) (40_000_000L + Random.nextInt(80_000_000).toLong()) else (1_000_000L + Random.nextInt(4_000_000).toLong()),
                teamId = teamId, isStartingXI = i < 11
            )
        }
    }

    fun getTeamById(id: String): Team? = _teams.value.find { it.id == id }

    suspend fun toggleStartingStatus(playerId: String) {
        withContext(Dispatchers.IO) {
            val teams = _teams.value
            val player = teams.flatMap { it.players }.find { it.id == playerId } ?: return@withContext
            footballDao.updatePlayer(player.copy(isStartingXI = !player.isStartingXI))
            syncWithDatabase()
        }
    }

    suspend fun negotiateTransfer(playerId: String, userTeamId: String, offerAmount: Long): NegotiationResult {
        return withContext(Dispatchers.IO) {
            val teams = _teams.value
            val player = teams.flatMap { it.players }.find { it.id == playerId } ?: return@withContext NegotiationResult.Rejected
            if (offerAmount >= player.marketValue) {
                completeTransfer(player, userTeamId, offerAmount)
                NegotiationResult.Accepted(offerAmount)
            } else NegotiationResult.Rejected
        }
    }

    suspend fun transferPlayer(playerId: String, fromTeamId: String, toTeamId: String) {
        val player = _teams.value.find { it.id == fromTeamId }?.players?.find { it.id == playerId }
        if (player != null) completeTransfer(player, toTeamId, player.marketValue)
    }

    private suspend fun completeTransfer(player: FootballPlayer, toTeamId: String, price: Long) {
        val buyer = footballDao.getTeamById(toTeamId) ?: return
        val seller = footballDao.getTeamById(player.teamId) ?: return
        footballDao.updateTeam(buyer.copy(budget = buyer.budget - price))
        footballDao.updateTeam(seller.copy(budget = seller.budget + price))
        footballDao.updatePlayerTeam(player.id, toTeamId)
        syncWithDatabase()
    }

    suspend fun developPlayers(teamId: String, matchRating: Float) {
        withContext(Dispatchers.IO) {
            val team = _teams.value.find { it.id == teamId } ?: return@withContext
            val updatedPlayers = team.players.map { p ->
                val xp = p.experience + (matchRating * 1.5f).toInt()
                if (xp >= 100) {
                    val s = p.stats.copy(
                        speed = min(99f, p.stats.speed + 0.5f), 
                        shooting = min(99f, p.stats.shooting + 0.5f),
                        passing = min(99f, p.stats.passing + 0.5f),
                        dribbling = min(99f, p.stats.dribbling + 0.5f)
                    )
                    p.copy(experience = 0, level = p.level + 1, stats = s, marketValue = p.marketValue + 500_000L)
                } else p.copy(experience = xp)
            }
            
            updatedPlayers.forEach { footballDao.updatePlayer(it) }
            syncWithDatabase()
        }
    }

    fun getStadiums(): List<Stadium> = listOf(Stadium("S1", "Etihad", "Manchester", 55000), Stadium("S5", "Arena Națională", "Bucharest", 55600))
}
