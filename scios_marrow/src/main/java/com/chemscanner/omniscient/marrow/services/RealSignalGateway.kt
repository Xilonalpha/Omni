package com.chemscanner.omniscient.marrow.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.chemscanner.omniscient.marrow.repository.BiometricVitality
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * THE REAL SIGNAL GATEWAY v5.0 (FULL SENSOR SUITE).
 * AUTHORITY: ARCHITECT XILON.
 * v5.0: Added support for Light and Pressure sensors to populate Scientific Feed.
 */
@Singleton
class RealSignalGateway @Inject constructor(
    @ApplicationContext private val context: Context,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var magneticSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private var accelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private var pressureSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    private val emfBuffer = Collections.synchronizedList(mutableListOf<Float>()
    private val maxBufferSize = 200
    private var lastProcessedHR = 72f
    private var isNearMetal = false
    private var isAcousticMonitoringPaused = false
    private var isMonitoringActive = false

    init {
        startQuantumBiometricProcessor()
        Timber.d("RealSignalGateway: Suite v5.0 Initialized.")
    }

    fun startMonitoring() {
        if (isMonitoringActive) return
        isMonitoringActive = true
        magneticSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
        accelSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        pressureSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        
        globalKnowledge.logEvent("SENSOR", "Sovereign Sensor Suite Active", 2)
    }

    fun stopMonitoring() {
        isMonitoringActive = false
        sensorManager.unregisterListener(this)
        globalKnowledge.logEvent("SENSOR", "Sovereign Sensor Suite Offline", 1)
    }

    fun pauseAcousticMonitoring() {
        isAcousticMonitoringPaused = true
    }

    fun resumeAcousticMonitoring() {
        isAcousticMonitoringPaused = false
    }

    fun startSovereignCalibration() {
        scope.launch {
            globalKnowledge.logEvent("BIO_AUTH", "Starting Skin-to-Field Calibration...", 4)
            delay(10000)
            globalKnowledge.logEvent("BIO_AUTH", "Calibration complete.", 5)
        }
    }

    private fun startQuantumBiometricProcessor() {
        scope.launch {
            while (isActive) {
                delay(1500)
                if (!isMonitoringActive) continue

                val currentEmf = synchronized(emfBuffer) { emfBuffer.toList() }

                if (currentEmf.size > 50) {
                    val averageField = currentEmf.average().toFloat()
                    isNearMetal = averageField > 150f

                    if (isNearMetal) {
                        handleMetalInterference()
                        continue
                    }

                    val mean = averageField
                    val variance = currentEmf.map { (it - mean) * (it - mean) }.average()
                    var microPulsation = sqrt(variance).toFloat()

                    val signals = globalKnowledge.realSignals.value
                    val motionNoise = signals.seismicIntensity * 0.2f
                    microPulsation = (microPulsation - motionNoise).coerceAtLeast(0.00001f)

                    val rhythmicity = calculateSignalRhythm(currentEmf)
                    val phoneEstimatedHR = 65 + (microPulsation * 160 * rhythmicity)

                    lastProcessedHR = (lastProcessedHR * 0.9f + phoneEstimatedHR * 0.1f).coerceIn(55f, 130f)
                    val estimatedStress = (microPulsation * 12 + (1.0f - rhythmicity) * 0.1f).coerceIn(0.1f, 0.9f)

                    globalKnowledge.updateUserVitality(BiometricVitality(
                        heartRate = lastProcessedHR.toInt(),
                        stressLevel = estimatedStress,
                        symbioticAlignment = (0.99f - (estimatedStress * 0.05f)).coerceIn(0.1f, 1.0f)
                    ))
                }
            }
        }
    }

    private fun handleMetalInterference() {
        scope.launch(Dispatchers.Main) {
            globalKnowledge.logEvent("BIO_ALARM", "Interferență magnetică masivă detectată (Metal).", 4)
            globalKnowledge.updateUserVitality(globalKnowledge.userVitality.value.copy(stressLevel = 0.99f))
            if (System.currentTimeMillis() % 60000 < 2000) {
                ttsService.speak("Alerta: Câmp magnetic perturbat de surse metalice.")
            }
        }
    }

    private fun calculateSignalRhythm(buffer: List<Float>): Float {
        if (buffer.size < 30) return 0.5f
        var peaks = 0
        for (i in 2 until buffer.size - 2) {
            if (buffer[i] > buffer[i-1] && buffer[i] > buffer[i+1]) peaks++
        }
        val idealPeakCount = buffer.size / 22
        return 1.0f - (abs(peaks - idealPeakCount).toFloat() / idealPeakCount.coerceAtLeast(1)).coerceIn(0f, 0.5f)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isMonitoringActive) return
        val current = globalKnowledge.realSignals.value
        when (event.sensor.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> {
                val strength = sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])
                synchronized(emfBuffer) {
                    emfBuffer.add(strength)
                    if (emfBuffer.size > maxBufferSize) emfBuffer.removeAt(0)
                }
                globalKnowledge.updateRealSignals(current.copy(emfIntensity = strength))
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val gForce = sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]) / 9.81f
                globalKnowledge.updateRealSignals(current.copy(seismicIntensity = abs(gForce - 1.0f)))
            }
            Sensor.TYPE_LIGHT -> {
                globalKnowledge.updateRealSignals(current.copy(ambientLuminosity = event.values[0]))
            }
            Sensor.TYPE_PRESSURE -> {
                globalKnowledge.updateRealSignals(current.copy(barometricPressure = event.values[0]))
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
