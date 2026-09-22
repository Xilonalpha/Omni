package com.chemscanner.omniscient.marrow.services

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransScaleBridge @Inject constructor() {
    private val _pulses = MutableSharedFlow<RealityPulse>()
    val pulses: SharedFlow<RealityPulse> = _pulses.asSharedFlow()

    suspend fun emitPulse(pulse: RealityPulse) {
        _pulses.emit(pulse)
    }
}
