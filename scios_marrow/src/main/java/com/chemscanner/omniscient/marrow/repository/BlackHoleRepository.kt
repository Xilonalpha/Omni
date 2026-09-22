package com.chemscanner.omniscient.marrow.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * THE SCI-OS COSMIC SINGULARITY ENGINE.
 * MIGRATED TO MARROW KERNEL.
 */
@Singleton
class BlackHoleRepository @Inject constructor() {

    data class BlackHoleMetrics(
        val massSolar: Double,
        val schwarzschildRadiusKm: Double,
        val hawkingTemperatureK: Double,
        val entropy: Double,
        val evaporationTimeYears: Double
    )

    data class TimeDilationResult(
        val r: Double,
        val dilationFactor: Double
    )

    private val g = 6.67430e-11
    private val c = 299792458.0
    private val mSolar = 1.989e30
    private val hBar = 1.0545718e-34
    private val kB = 1.380649e-23

    fun calculateMetrics(massSolar: Double): BlackHoleMetrics {
        val massKg = massSolar * mSolar
        val rs = (2 * g * massKg) / c.pow(2)
        val temp = (hBar * c.pow(3)) / (8 * PI * g * massKg * kB)
        val area = 4 * PI * rs.pow(2)
        val entropy = (area * kB * c.pow(3)) / (4 * g * hBar)
        val timeSec = (5120 * PI * g.pow(2) * massKg.pow(3)) / (hBar * c.pow(4))
        val years = timeSec / (365.25 * 24 * 3600)

        return BlackHoleMetrics(
            massSolar = massSolar,
            schwarzschildRadiusKm = rs / 1000.0,
            hawkingTemperatureK = temp,
            entropy = entropy,
            evaporationTimeYears = years
        )
    }

    fun calculateTimeDilation(massSolar: Double, distanceKm: Double): TimeDilationResult {
        val massKg = massSolar * mSolar
        val r = distanceKm * 1000.0
        val rs = (2 * g * massKg) / c.pow(2)
        val factor = if (r > rs) sqrt(1.0 - (rs / r)) else 0.0
        return TimeDilationResult(distanceKm, factor)
    }
}
