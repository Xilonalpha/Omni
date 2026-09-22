package com.planetscanner.app.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MicroFishResult(
    val speciesName: String,
    val genomicMarkers: List<String>,
    val healthStatus: String,
    val confidence: Float,
    val mutationDetected: Boolean = false,
    val anomalyDescription: String? = null
) : Parcelable
