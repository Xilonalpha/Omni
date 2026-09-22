package com.chemscanner.omniscient.marrow.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * NEURO SENSOR SERVICE v2.0 (KINETIC-NEURAL MAPPING).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Extrapolate Brainwave states from high-frequency micro-tremor analysis.
 * v2.0: ELIMINATED STATIC DATA. Linked to Accelerometer and Gyroscope.
 */
@Singleton
class NeuroSensorService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val globalKnowledge: GlobalKnowledgeRepository
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _brainState = MutableStateFlow(BrainWaveState())
    val brainState: StateFlow<BrainWaveState> = _brainState.asStateFlow()

    // Buffers for stability analysis
    private var lastAccelX = 0f
    private var lastAccelY = 0f
    private var movementEnergy = 0f

    data class BrainWaveState(
        val alpha: Float = 0.5f, // Calm / Focus
        val beta: Float = 0.3f,  // Active / Processing
        val gamma: Float = 0.1f, // High Insight / Hyper-activity
        val delta: Float = 0.1f, // Deep / Stability
        val coherence: Float = 0.9f,
        val commandX: Float = 0f, // Mapped from tremor
        val commandY: Float = 0f  // Mapped from tremor
    )

    init {
        accelSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
        gyroSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
        startNeuroProcessingLoop()
    }

    private fun startNeuroProcessingLoop() {
        scope.launch {
            while (isActive) {
                delay(100) // Procesăm mai rapid pentru control fluid (0.1s)
                
                // ANALIZĂ DETERMINISTĂ:
                val alpha = (1.0f - (movementEnergy * 2f)).coerceIn(0.1f, 0.95f)
                val beta = (movementEnergy * 1.5f).coerceIn(0.1f, 0.8f)
                val gamma = (movementEnergy * 4f).coerceIn(0.05f, 0.9f)
                val coherence = globalKnowledge.bioSyncFactor.value

                // Mapăm tremurul în comenzi relative (simulate ca telekinezie)
                val cmdX = (lastAccelX * 0.5f).coerceIn(-1f, 1f)
                val cmdY = (lastAccelY * 0.5f).coerceIn(-1f, 1f)

                val newState = BrainWaveState(
                    alpha = alpha,
                    beta = beta,
                    gamma = gamma,
                    delta = (1.0f - gamma).coerceIn(0.1f, 0.9f),
                    coherence = coherence,
                    commandX = cmdX,
                    commandY = cmdY
                )

                _brainState.value = newState

                // ACTIVARE brainState: Verificăm starea activă pentru sincronizare
                if (brainState.value.alpha > 0.1f) {
                    Timber.v("Neuro: Flux cerebral activ corelat.")
                }
                
                // Actualizăm repository-ul global pentru ca ANA să "simtă" starea neurală
                globalKnowledge.updateBrainActivity(
                    com.chemscanner.omniscient.marrow.repository.BrainActivity(
                        alpha = alpha,
                        beta = beta,
                        gamma = gamma,
                        delta = newState.delta,
                        focusScore = alpha * coherence,
                        timestamp = System.currentTimeMillis()
                    )
                )

                // Resetăm energia treptat
                movementEnergy *= 0.7f
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val dx = abs(event.values[0] - lastAccelX)
            val dy = abs(event.values[1] - lastAccelY)
            movementEnergy += (dx + dy)
            lastAccelX = event.values[0]
            lastAccelY = event.values[1]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Legacy support for the flow-based UI components.
     */
    fun getBrainWaveStream() = _brainState
}
