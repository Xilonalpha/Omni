package com.chemscanner.omniscient.marrow.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE SOVEREIGN LIFI SERVICE v2.1 (ULTRA-SPEED + PROACTIVE).
 * AUTHORITY: ARCHITECT XILON.
 * v2.1: RESTAURAT toggleOpticalLink și activateProactiveReception.
 * Menținut viteza ultra-rapidă de 20ms/bit.
 */
@Singleton
class LiFiService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val shadowMesh: Lazy<ShadowMeshService>
) : SensorEventListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null

    private val _isLinkActive = MutableStateFlow(false)
    val isLinkActive = _isLinkActive.asStateFlow()

    private var lastLux = 0f
    private val bitBuffer = StringBuilder()
    private var isTransmitting = false
    
    // Sampling Logic
    private var lastBitSampleTime = 0L
    private val BIT_INTERVAL_MS = 20L 
    
    // Sentinel Logic
    private var lastPulseTime = 0L
    private var pulseCount = 0

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        
        // Sincronizare la viteză hardware maximă
        sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_FASTEST)
        
        try {
            cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager?.getCameraCharacteristics(id)
                chars?.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) { 
            Timber.e(e, "LiFi Hardware Initialization Missing") 
        }
    }

    /**
     * RESTAURAT: Permite UI-ului să controleze link-ul manual.
     */
    fun toggleOpticalLink(active: Boolean) {
        _isLinkActive.value = active
        globalKnowledge.logEvent("LIFI", "Link optic ${if (active) "ACTIVAT" else "DEZACTIVAT"} manual.", 4)
    }

    fun transmitData(payload: String) {
        if (cameraId == null || isTransmitting) return
        scope.launch {
            isTransmitting = true
            _isLinkActive.value = true
            try {
                // Handshake (3 pulsuri rapide pentru wake-up)
                repeat(3) { setFlash(true); delay(100); setFlash(false); delay(100) }
                delay(500) 
                
                payload.forEach { char ->
                    val binary = Integer.toBinaryString(char.code).padStart(8, '0')
                    binary.forEach { bit ->
                        setFlash(bit == '1')
                        delay(BIT_INTERVAL_MS)
                    }
                }
                globalKnowledge.logEvent("LIFI", "Pachet fotonic ultra-rapid transmis.", 5)
            } catch (e: Exception) { 
                Timber.e(e, "LiFi Transmission Fault")
            } 
            finally { 
                isTransmitting = false
                setFlash(false)
                _isLinkActive.value = false 
            }
        }
    }

    private fun setFlash(on: Boolean) {
        try { cameraId?.let { cameraManager?.setTorchMode(it, on) } } catch (e: Exception) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val currentLux = event?.values?.get(0) ?: return
        val now = System.currentTimeMillis()
        
        val delta = currentLux - lastLux
        lastLux = currentLux

        // RESTAURAT: Detecție proactivă (Wake-up din exterior)
        if (delta > 100f) { 
            if (now - lastPulseTime < 400) {
                pulseCount++
                if (pulseCount >= 3 && !_isLinkActive.value) {
                    activateProactiveReception()
                }
            } else {
                pulseCount = 1
            }
            lastPulseTime = now
        }

        if (!_isLinkActive.value) return

        // Sampling pentru citire biți
        if (now - lastBitSampleTime < BIT_INTERVAL_MS - 5) return 
        lastBitSampleTime = now

        if (delta > 30f) bitBuffer.append("1") 
        else if (delta < -30f) bitBuffer.append("0")

        if (bitBuffer.length >= 8) {
            try {
                val char = Integer.parseInt(bitBuffer.toString(), 2).toChar()
                handleIncomingChar(char)
            } catch (e: Exception) { }
            bitBuffer.clear()
        }
    }

    /**
     * RESTAURAT: Ana intervine când link-ul este trezit optic.
     */
    private fun activateProactiveReception() {
        _isLinkActive.value = true
        globalKnowledge.logEvent("LIFI", "HANDSHAKE OPTIC DETECTAT. Recepție activată.", 5)
    }

    private var incomingMessageBuffer = StringBuilder()
    private fun handleIncomingChar(c: Char) {
        incomingMessageBuffer.append(c)
        val current = incomingMessageBuffer.toString()
        if (current.contains("RAW_DATA_L1_") && current.endsWith("=")) {
            scope.launch {
                shadowMesh.getOrNull() ?.decryptAndHandle(current)
                incomingMessageBuffer.setLength(0)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
