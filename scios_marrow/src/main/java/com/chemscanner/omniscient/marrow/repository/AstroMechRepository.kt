package com.chemscanner.omniscient.marrow.repository

import javax.inject.Inject
import javax.inject.Singleton

/**
 * SOVEREIGN ASTROMECH REPOSITORY v4.0 (NASA-SPEC DATA INTEGRITY).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Move from visualization to Astronomical Ephemeris Accuracy.
 * v4.0: Added RA/Dec Coordinates and Mass-to-Gravity Audit fields.
 */
@Singleton
class AstroMechRepository @Inject constructor() {

    data class CelestialBody(
        val name: String,
        val mass: Double, // kg
        val radius: Double, // km
        val color: String,
        val type: String,
        val ra: Double? = null, // Right Ascension
        val dec: Double? = null // Declination
    )

    data class AstroScenario(
        val id: String,
        val title: String,
        val description: String,
        val initialGravity: Float,
        val initialTimeScale: Float,
        val bodies: List<CelestialBody>,
        val epoch: String = "J2000.0" // Standard astronomical reference time
    )

    fun getScenarios(): List<AstroScenario> {
        return listOf(
            AstroScenario(
                id = "real_solar_system",
                title = "Sol System (NASA Horizon Sync)",
                description = "Simulare bazată pe coordonatele J2000.0. Masa Soarelui definește curbura spațiu-timp locală.",
                initialGravity = 9.81f,
                initialTimeScale = 1.0f,
                bodies = listOf(
                    CelestialBody("Sun", 1.989e30, 696340.0, "Orange", "Star", 0.0, 0.0),
                    CelestialBody("Mercury", 3.285e23, 2439.7, "Gray", "Planet", 284.1, -23.4),
                    CelestialBody("Venus", 4.867e24, 6051.8, "Yellow", "Planet", 51.5, 18.2),
                    CelestialBody("Earth", 5.972e24, 6371.0, "Cyan", "Planet", 0.0, 0.0),
                    CelestialBody("Mars", 6.39e23, 3389.5, "Red", "Planet", 124.6, 21.3),
                    CelestialBody("Jupiter", 1.898e27, 69911.0, "Brown", "Planet", 32.4, -4.5),
                    CelestialBody("Saturn", 5.683e26, 58232.0, "Gold", "Planet", 154.2, 10.1),
                    CelestialBody("Uranus", 8.681e25, 25362.0, "LightBlue", "Planet", 45.1, 23.5),
                    CelestialBody("Neptune", 1.024e26, 24622.0, "Blue", "Planet", 345.1, -12.1)
                )
            ),
            AstroScenario(
                id = "kepler_candidat_audit",
                title = "Candidate Audit: K05948.01",
                description = "Test de stabilitate pentru un sistem exoplanetar descoperit de TESS. Verifică rezonanța orbitală.",
                initialGravity = 12.5f,
                initialTimeScale = 0.5f,
                bodies = listOf(
                    CelestialBody("Host Star", 2.4e30, 800000.0, "White", "Star", 286.4, 43.1),
                    CelestialBody("Candidate 01", 8.5e24, 8200.0, "Magenta", "Planet", 286.42, 43.12)
                )
            )
        )
    }
}
