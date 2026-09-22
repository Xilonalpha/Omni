package com.chemscanner.omniscient.marrow.di

import android.content.Context
import com.chemscanner.omniscient.marrow.data.dao.FootballDao
import com.chemscanner.omniscient.marrow.data.dao.ScanHistoryDao
import com.chemscanner.omniscient.marrow.repository.ChemicalRepository
import com.chemscanner.omniscient.marrow.repository.ChemicalRepositoryImpl
import com.chemscanner.omniscient.marrow.repository.FootballRepository
import com.chemscanner.omniscient.marrow.repository.MainRepository
import com.chemscanner.omniscient.marrow.repository.TemporalPhysicsRepository
import com.chemscanner.omniscient.marrow.repository.UserRepository
import com.chemscanner.omniscient.marrow.services.GeminiService
import com.chemscanner.omniscient.marrow.services.GeminiVisionService
import com.chemscanner.omniscient.marrow.services.GreenChemistryService
import com.chemscanner.omniscient.marrow.services.NeuralImmunityService
import com.chemscanner.omniscient.marrow.services.SovereignKeyVault
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MarrowMasterModule {

    @Binds
    @Singleton
    abstract fun bindChemicalRepository(
        chemicalRepositoryImpl: ChemicalRepositoryImpl
    ): ChemicalRepository

    companion object {
        @Provides
        @Singleton
        fun provideHttpClient(): HttpClient {
            return HttpClient(OkHttp) {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    })
                }
            }
        }

        @Provides
        @Singleton
        fun provideMainRepository(
            @ApplicationContext context: Context,
            chemicalRepository: ChemicalRepository,
            scanHistoryDao: ScanHistoryDao,
            greenChemistryService: GreenChemistryService,
            userRepository: UserRepository,
            geminiService: GeminiService,
            geminiVisionService: GeminiVisionService
        ): MainRepository {
            return MainRepository(context, chemicalRepository, scanHistoryDao, greenChemistryService, userRepository, geminiService, geminiVisionService)
        }

        @Provides
        @Singleton
        fun provideUserRepository(firebaseAuth: FirebaseAuth): UserRepository = UserRepository(firebaseAuth)

        @Provides
        @Singleton
        fun provideFootballRepository(footballDao: FootballDao): FootballRepository = FootballRepository(footballDao)

        @Provides
        @Singleton
        fun provideTemporalPhysicsRepository(): TemporalPhysicsRepository = TemporalPhysicsRepository()

        @Provides
        @Singleton
        fun provideNeuralImmunityService(): NeuralImmunityService = NeuralImmunityService()
    }
}
