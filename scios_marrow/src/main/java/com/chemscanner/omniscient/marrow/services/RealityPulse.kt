package com.chemscanner.omniscient.marrow.services

sealed class RealityPulse {
    data class AtmosphericShift(val compositionChange: Map<String, Float>) : RealityPulse()
    object NeuralSpike : RealityPulse()
}
