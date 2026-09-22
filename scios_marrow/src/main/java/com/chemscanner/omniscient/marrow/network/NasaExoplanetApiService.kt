package com.chemscanner.omniscient.marrow.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * THE REAL-WORLD BRIDGE: NASA EXOPLANET ARCHIVE API.
 * Connects SCI-OS to the official human discovery database.
 */
interface NasaExoplanetApiService {

    @GET("cgi-bin/nstedAPI/nph-nstedAPI")
    suspend fun getConfirmedPlanets(
        @Query("table") table: String = "cumulative",
        @Query("format") format: String = "json",
        @Query("where") condition: String? = null,
        @Query("order") order: String = "dec"
    ): List<NasaPlanetResponse>
}

data class NasaPlanetResponse(
    val kepler_name: String?,
    val koi_disposition: String?,
    val koi_period: Double?,
    val koi_prad: Double?,
    val koi_teq: Double?,
    val koi_insol: Double?,
    val ra: Double?,
    val dec: Double?
)
