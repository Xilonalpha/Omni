package com.chemscanner.omniscient.marrow.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * THE MARROW CORE PROVIDER.
 * Ensures the SDK has its own internal tools.
 */
@Module
@InstallIn(SingletonComponent::class)
object MarrowCoreModule {

    // Gson is already provided in the app module's AppModule.kt
}
