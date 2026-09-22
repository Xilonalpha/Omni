package com.planetscanner.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExoCreature(
    val name: String,
    val morphology: String, // e.g., "Bilateral Multi-limbed", "Amorphous"
    val skinType: String, // e.g., "Silica-based scales", "Gas-permeable membrane"
    val metabolism: String,
    val homePlanetAtmosphere: List<String>,
    val gravityResistance: Float, // How much g-force it can handle
    val description: String,
    val evolutionStage: Int // 1-10
) : Parcelable
