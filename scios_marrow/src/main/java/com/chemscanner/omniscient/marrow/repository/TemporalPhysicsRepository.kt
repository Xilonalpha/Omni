package com.chemscanner.omniscient.marrow.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * THE TEMPORAL PHYSICS ENGINE v2.0.
 * RESTORED FROM FOLDER 1 (Baseline of Truth).
 * Implements Lorentz Transformations and Time Paradox Simulations.
 */
@Singleton
class TemporalPhysicsRepository @Inject constructor() {

    data class RelativisticState(
        val velocityPercentage: Double,
        val lorentzFactor: Double,
        val lengthContraction: Double,
        val timeDilation: Double
    )

    data class TwinParadoxResult(
        val earthYears: Double,
        val travelerYears: Double,
        val ageDifference: Double
    )

    data class TimeParadoxPuzzle(
        val id: String,
        val title: String,
        val description: String,
        val targetGamma: Double
    )

    fun calculateLorentz(velocityFraction: Double): RelativisticState {
        val v2c2 = velocityFraction * velocityFraction
        val gamma = 1.0 / sqrt(1.0 - v2c2)
        return RelativisticState(
            velocityPercentage = velocityFraction * 100,
            lorentzFactor = gamma,
            lengthContraction = 1.0 / gamma,
            timeDilation = gamma
        )
    }

    fun calculateTwinParadox(velocityFraction: Double, earthYears: Double): TwinParadoxResult {
        val state = calculateLorentz(velocityFraction)
        val travelerYears = earthYears / state.lorentzFactor
        return TwinParadoxResult(earthYears, travelerYears, earthYears - travelerYears)
    }

    fun getPuzzles(): List<TimeParadoxPuzzle> {
        return listOf(
            TimeParadoxPuzzle("twin_paradox", "The Twin Paradox", "Adjust velocity scale.", 2.0),
            TimeParadoxPuzzle("muon_decay", "Atmospheric Muons", "Particle decay calc.", 10.0)
        )
    }
}
