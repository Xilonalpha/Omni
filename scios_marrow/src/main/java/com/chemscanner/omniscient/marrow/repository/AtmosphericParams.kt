package com.chemscanner.omniscient.marrow.repository

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AtmosphericParams(
    val temperature: Float = 20f,
    val pressure: Float = 1013.25f,
    val humidity: Float = 50f,
    val windSpeed: Float = 0f,
    val visibility: Float = 10f
) : Parcelable
