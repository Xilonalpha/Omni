package com.planetscanner.app.data.repository

import com.planetscanner.app.data.models.Planet
import com.planetscanner.app.data.models.GeologicalLayer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanetRepository @Inject constructor() {

    fun getSolarSystemPlanets(): List<Planet> {
        return listOf(
            Planet(
                id = "mercury",
                name = "Mercur",
                type = "Terrestrial",
                mass = 0.055,
                gravity = 3.7,
                radius = 2439.7,
                temperature = 167.0,
                atmosphere = listOf("Oxygen", "Sodium", "Hydrogen"),
                layers = listOf(
                    GeologicalLayer("Crust", 100.0, "Silicates", 400.0),
                    GeologicalLayer("Core", 1800.0, "Iron", 600.0)
                ),
                summary = "Cea mai apropiată planetă de Soare și cea mai mică din Sistemul Solar.",
                hasMagneticField = true
            ),
            Planet(
                id = "venus",
                name = "Venus",
                type = "Terrestrial",
                mass = 0.815,
                gravity = 8.87,
                radius = 6051.8,
                temperature = 464.0,
                atmosphere = listOf("CO2", "Nitrogen"),
                layers = listOf(
                    GeologicalLayer("Crust", 50.0, "Basalt", 460.0),
                    GeologicalLayer("Mantle", 3000.0, "Silicates", 2000.0)
                ),
                summary = "Adesea numită sora Pământului, dar cu o atmosferă extrem de densă și fierbinte.",
                hasMagneticField = false
            ),
            Planet(
                id = "mars",
                name = "Marte",
                type = "Terrestrial",
                mass = 0.107,
                gravity = 3.71,
                radius = 3389.5,
                temperature = -65.0,
                atmosphere = listOf("CO2", "Nitrogen", "Argon"),
                layers = listOf(
                    GeologicalLayer("Crust", 50.0, "Iron-rich basalt", -60.0),
                    GeologicalLayer("Core", 1800.0, "Iron, Nickel, Sulfur", 1500.0)
                ),
                summary = "Planeta Roșie, gazda celor mai mari vulcani din sistemul solar.",
                hasMagneticField = false
            )
        )
    }
}
