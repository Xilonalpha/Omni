package com.chemscanner.omniscient.marrow.data.dao

import androidx.room.*
import com.chemscanner.omniscient.marrow.data.models.*
import com.chemscanner.omniscient.marrow.repository.SystemEvent
import kotlinx.coroutines.flow.Flow

/**
 * THE SOVEREIGN DAOS HUB.
 * Consolidated for SCI-OS Marrow SDK (.aar)
 */

@Dao
interface SystemEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: SystemEvent)

    @Query("SELECT * FROM system_events ORDER BY timestamp DESC LIMIT 200")
    fun getAllEvents(): Flow<List<SystemEvent>>

    @Query("SELECT * FROM system_events WHERE importance >= :minImportance ORDER BY timestamp DESC")
    suspend fun getImportantEvents(minImportance: Int): List<SystemEvent>

    @Query("DELETE FROM system_events")
    suspend fun deleteAll()
}

@Dao
interface ScanHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scan: ScanHistory): Long

    @Query("SELECT * FROM scan_history ORDER BY scanDate DESC")
    fun getAllScans(): Flow<List<ScanHistory>>

    @Query("SELECT * FROM scan_history ORDER BY scanDate DESC")
    suspend fun getAllScansSortedByDateDescSync(): List<ScanHistory>

    @Query("SELECT * FROM scan_history WHERE id = :id")
    suspend fun getScanById(id: Long): ScanHistory?

    @Query("DELETE FROM scan_history")
    suspend fun deleteAll()
}

@Dao
interface ToxicityDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: ToxicityData)

    @Query("SELECT * FROM toxicity_data WHERE chemicalName = :name")
    suspend fun getByName(name: String): ToxicityData?
}

@Dao
interface ChemicalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chemical: ChemicalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChemical(chemical: ChemicalEntity)

    @Query("SELECT * FROM chemicals WHERE name LIKE :name LIMIT 1")
    suspend fun getChemicalByName(name: String): ChemicalEntity?

    @Query("SELECT * FROM chemicals WHERE id = :id")
    suspend fun getChemicalById(id: Long): ChemicalEntity?

    @Query("SELECT * FROM chemicals WHERE spectralHex = :hex LIMIT 1")
    suspend fun getBySpectralSignature(hex: String): ChemicalEntity?

    @Query("SELECT * FROM chemicals")
    fun getAllChemicals(): Flow<List<ChemicalEntity>>

    @Delete
    suspend fun deleteChemical(chemical: ChemicalEntity)

    @Query("DELETE FROM chemicals")
    suspend fun deleteAll()

    @Query("DELETE FROM chemicals")
    suspend fun deleteAllChemicals()
}

@Dao
interface CachedReactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reaction: CachedReaction)

    @Query("SELECT * FROM cached_reactions WHERE (reactant1 = :r1 AND reactant2 = :r2) OR (reactant1 = :r2 AND reactant2 = :r1) LIMIT 1")
    suspend fun getReaction(r1: String, r2: String): CachedReaction?

    @Query("DELETE FROM cached_reactions")
    suspend fun deleteAll()
}

@Dao
interface BlockchainDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BlockchainEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(entry: BlockchainEntry)

    @Query("SELECT blockHash FROM blockchain_ledger ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastBlockHash(): String?

    @Query("SELECT * FROM blockchain_ledger ORDER BY timestamp DESC")
    fun getFullLedger(): Flow<List<BlockchainEntry>>

    @Query("SELECT COUNT(*) FROM blockchain_ledger")
    suspend fun getBlockCount(): Int
}

@Dao
interface MatchResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: MatchResultEntity)

    @Query("SELECT * FROM match_results ORDER BY timestamp DESC")
    fun getMatchHistory(): Flow<List<MatchResultEntity>>
}

@Dao
interface DiscoveredPlanetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(planet: DiscoveredPlanetEntity)

    @Query("SELECT * FROM discovered_planets")
    fun getDiscoveredPlanets(): Flow<List<DiscoveredPlanetEntity>>

    @Query("SELECT * FROM discovered_planets")
    suspend fun getDiscoveredPlanetsSync(): List<DiscoveredPlanetEntity>

    @Query("SELECT * FROM discovered_planets WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): DiscoveredPlanetEntity?
}

@Dao
interface MaterialBlueprintDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialBlueprintEntity)

    @Query("SELECT * FROM material_blueprints ORDER BY timestamp DESC")
    fun getAllMaterials(): Flow<List<MaterialBlueprintEntity>>
}

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessage)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getChatMessages(sessionId: String): Flow<List<ChatMessage>>

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteHistory(sessionId: String)
}

@Dao
interface GenomicOrganismDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(organism: GenomicOrganismEntity)

    @Query("SELECT * FROM genomic_organisms")
    fun getAllOrganisms(): Flow<List<GenomicOrganismEntity>>

    @Query("SELECT * FROM genomic_organisms WHERE id = :id")
    suspend fun getById(id: String): GenomicOrganismEntity?

    @Delete
    suspend fun delete(organism: GenomicOrganismEntity)
}

@Dao
interface FootballDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeagues(leagues: List<League>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<Team>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<FootballPlayer>)

    @Query("SELECT * FROM football_leagues")
    fun getAllLeagues(): Flow<List<League>>

    @Query("SELECT * FROM football_teams")
    fun getAllTeams(): Flow<List<Team>>

    @Query("SELECT * FROM football_players WHERE teamId = :teamId")
    fun getPlayersByTeam(teamId: String): Flow<List<FootballPlayer>>

    @Query("UPDATE football_players SET teamId = :toTeamId WHERE id = :playerId")
    suspend fun updatePlayerTeam(playerId: String, toTeamId: String)

    @Update
    suspend fun updatePlayer(player: FootballPlayer)

    @Update
    suspend fun updateTeam(team: Team)

    @Query("SELECT * FROM football_teams WHERE id = :teamId LIMIT 1")
    suspend fun getTeamById(teamId: String): Team?
}
