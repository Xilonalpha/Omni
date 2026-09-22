package com.chemscanner.omniscient.marrow.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chemscanner.omniscient.marrow.data.dao.*
import com.chemscanner.omniscient.marrow.data.models.*
import com.chemscanner.omniscient.marrow.data.converters.MarrowConverters
import com.chemscanner.omniscient.marrow.repository.SystemEvent

/**
 * THE SOVEREIGN AKASHA DATABASE v101.
 * MISSION: Persist episodes of consciousness and technical discoveries via SovereignMemory.
 * v101: Added SovereignMemoryEntity and SovereignMemoryDao for long-term AI memory.
 * v102: Added Football entities and Dao to sync with DatabaseModule.
 * AUTHORITY: ARCHITECT XILON.
 */
@Database(
    entities = [
        ScanHistory::class,
        BlockchainEntry::class,
        ChemicalEntity::class,
        DiscoveredPlanetEntity::class,
        GenomicOrganismEntity::class,
        MaterialBlueprintEntity::class,
        ToxicityData::class,
        ChatMessage::class,
        CachedReaction::class,
        MatchResultEntity::class,
        SovereignMemoryEntity::class,
        League::class,
        Team::class,
        FootballPlayer::class,
        Stadium::class,
        ChemicalProperty::class,
        SystemEvent::class // REPARAT: Adăugat SystemEvent pentru persistență reală
    ],
    version = 8, // REPARAT: Bump version to 8
    exportSchema = false
)
@TypeConverters(MarrowConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun blockchainDao(): BlockchainDao
    abstract fun chemicalDao(): ChemicalDao
    abstract fun toxicityDataDao(): ToxicityDataDao
    abstract fun discoveredPlanetDao(): DiscoveredPlanetDao
    abstract fun genomicOrganismDao(): GenomicOrganismDao
    abstract fun materialBlueprintDao(): MaterialBlueprintDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun cachedReactionDao(): CachedReactionDao
    abstract fun matchResultDao(): MatchResultDao
    abstract fun sovereignMemoryDao(): SovereignMemoryDao
    abstract fun footballDao(): FootballDao
    abstract fun systemEventDao(): SystemEventDao // REPARAT: Adăugat DAO pentru evenimente
}
