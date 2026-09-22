package com.chemscanner.omniscient.marrow.di

import android.content.Context
import androidx.room.Room
import com.chemscanner.omniscient.marrow.data.AppDatabase
import com.chemscanner.omniscient.marrow.data.SovereignMemoryDao
import com.chemscanner.omniscient.marrow.data.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * THE SOVEREIGN DATABASE PROVIDER.
 * Part of the SOVEREIGN MARROW SDK (.aar).
 */
@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "omniscient_marrow_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideScanHistoryDao(db: AppDatabase): ScanHistoryDao = db.scanHistoryDao()

    @Provides
    fun provideBlockchainDao(db: AppDatabase): BlockchainDao = db.blockchainDao()

    @Provides
    fun provideChemicalDao(db: AppDatabase): ChemicalDao = db.chemicalDao()

    @Provides
    fun provideToxicityDataDao(db: AppDatabase): ToxicityDataDao = db.toxicityDataDao()

    @Provides
    fun provideMatchResultDao(db: AppDatabase): MatchResultDao = db.matchResultDao()

    @Provides
    fun provideDiscoveredPlanetDao(db: AppDatabase): DiscoveredPlanetDao = db.discoveredPlanetDao()

    @Provides
    fun provideCachedReactionDao(db: AppDatabase): CachedReactionDao = db.cachedReactionDao()

    @Provides
    fun provideMaterialBlueprintDao(db: AppDatabase): MaterialBlueprintDao = db.materialBlueprintDao()

    @Provides
    fun provideChatMessageDao(db: AppDatabase): ChatMessageDao = db.chatMessageDao()

    @Provides
    fun provideFootballDao(db: AppDatabase): FootballDao = db.footballDao()

    @Provides
    fun provideSystemEventDao(db: AppDatabase): SystemEventDao = db.systemEventDao()

    @Provides
    fun provideSovereignMemoryDao(db: AppDatabase): SovereignMemoryDao = db.sovereignMemoryDao()

    @Provides
    fun provideGenomicOrganismDao(db: AppDatabase): GenomicOrganismDao = db.genomicOrganismDao()
}
