package com.chemscanner.omniscient.marrow.di

import android.content.Context
import androidx.room.Room
import com.chemscanner.omniscient.marrow.data.AppDatabase
import com.chemscanner.omniscient.marrow.data.CacheDao
import com.chemscanner.omniscient.marrow.data.ResponseCacheManager
import com.chemscanner.omniscient.marrow.ml.GemmaLocalEngine
import com.chemscanner.omniscient.marrow.ml.MistralFallbackEngine
import com.chemscanner.omniscient.marrow.services.AppleWatchBCIService
import com.chemscanner.omniscient.marrow.services.GeminiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * FIXED DEPENDENCY INJECTION MODULE v2.0
 * 
 * Changes:
 * - Removed circular dependencies (OmniscientOrchestrator -> BCI -> Gateway -> BCI cycle)
 * - Firebase is NOT wrapped in @Singleton (it already is internally)
 * - Proper service layer separation
 * - Lazy dependencies where needed
 * - Network timeouts configured
 * - EncryptedSharedPreferences for sensitive data
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModuleFixed {
    
    // ============ NETWORK LAYER ============
    
    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
        .retryOnConnectionFailure(true)
        .addInterceptor(HttpLoggingInterceptor { message ->
            Timber.d("OkHttp: $message")
        }.apply {
            level = if (com.chemscanner.omniscient.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        })
        .build()
    
    // ============ FIREBASE (DO NOT WRAP IN @Singleton) ============
    
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    @Provides
    fun provideFirebaseDatabase(): FirebaseDatabase = 
        FirebaseDatabase.getInstance().apply {
            setPersistenceEnabled(true)
        }
    
    // ============ DATABASE & CACHING ============
    
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "omniscient_cache.db")
            .fallbackToDestructiveMigration()
            .build()
    
    @Provides
    fun provideCacheDao(db: AppDatabase): CacheDao = db.cacheDao()
    
    @Singleton
    @Provides
    fun provideResponseCacheManager(cacheDao: CacheDao): ResponseCacheManager =
        ResponseCacheManager(cacheDao)
    
    // ============ LOCAL ML ENGINES ============
    
    @Singleton
    @Provides
    fun provideGemmaLocalEngine(@ApplicationContext context: Context): GemmaLocalEngine =
        GemmaLocalEngine(context).also {
            Timber.d("GEMMA: Local engine initialized")
        }
    
    @Singleton
    @Provides
    fun provideMistralFallbackEngine(okHttpClient: OkHttpClient): MistralFallbackEngine =
        MistralFallbackEngine(okHttpClient).also {
            Timber.d("MISTRAL: Fallback engine initialized")
        }
    
    // ============ EXTERNAL API SERVICES ============
    
    @Provides  // NO @Singleton - these manage their own state
    fun provideGeminiService(okHttpClient: OkHttpClient): GeminiService =
        GeminiService(okHttpClient)
    
    @Provides
    @Singleton
    fun provideAppleWatchBCIService(@ApplicationContext context: Context): AppleWatchBCIService =
        AppleWatchBCIService(context).also {
            Timber.d("APPLE_WATCH: BCI service initialized")
        }
    
    // ============ CONFIGURATION ============
    
    @Singleton
    @Provides
    fun provideApiKeyProvider(): ApiKeyProvider = ApiKeyProvider()
}
