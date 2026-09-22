package com.chemscanner.omniscient.marrow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SovereignMemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: SovereignMemoryEntity)

    @Query("SELECT * FROM sovereign_memory ORDER BY timestamp DESC LIMIT 500")
    fun getRecentMemories(): Flow<List<SovereignMemoryEntity>>

    @Query("SELECT * FROM sovereign_memory WHERE module = :module ORDER BY timestamp DESC LIMIT 100")
    fun getMemoriesByModule(module: String): Flow<List<SovereignMemoryEntity>>

    @Query("SELECT * FROM sovereign_memory WHERE importance >= :minImportance ORDER BY timestamp DESC LIMIT 20")
    suspend fun getImportantMemories(minImportance: Int): List<SovereignMemoryEntity>

    @Query("DELETE FROM sovereign_memory WHERE timestamp < :expiryTime")
    suspend fun purgeOldMemories(expiryTime: Long)
}
