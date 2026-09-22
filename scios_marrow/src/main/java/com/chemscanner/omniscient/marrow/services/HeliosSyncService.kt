package com.chemscanner.omniscient.marrow.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.data.dao.BlockchainDao
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy
import kotlin.math.abs

/**
 * THE HELIOS-SYNC PROTOCOL v1.8 (NATURAL PHOTONIC SYNC).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Instant Global Communication anchored to natural light variance.
 * v1.8: Fixed missing 'abs' import.
 */
@Singleton
class HeliosSyncService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val blockchainNotary: BlockchainNotaryService,
    private val blockchainDao: BlockchainDao,
    private val shadowMesh: Lazy<ShadowMeshService>
) : SensorEventListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var isHeliosActive = false
    
    private var lastLux = 0f
    private var isEnvironmentActive = false

    private val database = FirebaseDatabase.getInstance()
    private val heliosFlux = database.getReference("marrow_helios_flux")

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        attachGalacticListener()
    }

    fun initiateHeliosLink(active: Boolean) {
        isHeliosActive = active
        if (active) {
            sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            globalKnowledge.logEvent("HELIOS", "Natural Helios Link Active. Synchronizing with ambient entropy.", 5)
        } else {
            sensorManager?.unregisterListener(this)
            globalKnowledge.logEvent("HELIOS", "Helios Link Standby.", 2)
        }
    }

    private fun attachGalacticListener() {
        heliosFlux.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isHeliosActive || !snapshot.exists()) return
                
                snapshot.children.forEach { child ->
                    val data = child.child("payload").value as? String ?: return@forEach
                    val packetKey = child.key ?: return@forEach
                    
                    if (isEnvironmentActive) {
                        processIncomingGalacticPacket(data, packetKey)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Timber.e("Helios: Galactic Sync Fault")
            }
        })
    }

    private fun processIncomingGalacticPacket(encryptedData: String, packetKey: String) {
        scope.launch {
            val history = globalKnowledge.events.value
            if (history.none { it.description.contains(encryptedData.take(15)) }) {
                withContext(Dispatchers.Main) {
                    ttsService.speak("Xilon, recepție prin fluxul fotonic natural.")
                    globalKnowledge.logEvent("HELIOS", "Packet consumed from Galactic Ledger via Light Sync.", 4)
                }
                shadowMesh.getOrNull() ?:.decryptAndHandle(encryptedData)
                heliosFlux.child(packetKey).removeValue()
            }
        }
    }

    fun injectSignalIntoFlux(payload: String) {
        scope.launch {
            try {
                val packetId = heliosFlux.push().key ?: return@launch
                val packetData = mapOf(
                    "payload" to payload,
                    "timestamp" to System.currentTimeMillis()
                )
                heliosFlux.child(packetId).setValue(packetData).addOnSuccessListener {
                    globalKnowledge.logEvent("HELIOS", "Signal pulsed globally via Natural Sync.", 5)
                }
                blockchainNotary.notarizeDiscovery("HELIOS_NATURAL", payload)
            } catch (e: Exception) {
                Timber.e(e, "Helios Injection Fault")
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isHeliosActive || event == null) return
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val currentLux = event.values[0]
            val delta = abs(currentLux - lastLux)
            isEnvironmentActive = currentLux > 10f || delta > 5f
            lastLux = currentLux
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
