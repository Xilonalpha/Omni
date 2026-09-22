package com.planetscanner.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Planet(
    val id: String,
    val name: String,
    val type: String,
    val mass: Double,
    val gravity: Double,
    val radius: Double,
    val temperature: Double,
    val atmosphere: List<String>,
    val layers: List<GeologicalLayer>,
    val summary: String,
    val hasMagneticField: Boolean
) : Parcelable

@Parcelize
data class GeologicalLayer(
    val name: String,
    val thickness: Double,
    val composition: String,
    val temperature: Double
) : Parcelable
