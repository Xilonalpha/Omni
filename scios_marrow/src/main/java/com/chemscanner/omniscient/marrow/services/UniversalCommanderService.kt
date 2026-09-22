package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE UNIVERSAL COMMANDER SERVICE v3.1.
 * ZERO-SCRIPT ARCHITECTURE: Control by Proxy and Universal Protocol.
 * REPAIRED: Parameter 'action' is now used in Space Telemetry logs.
 */
@Singleton
class UniversalCommanderService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val okHttpClient: OkHttpClient,
    private val quantumIot: QuantumIotBridge,
    private val roboticsGateway: RoboticsGateway,
    private val teslaControl: TeslaControlService,
    private val droneControl: DroneMavlinkService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val HOME_ASSISTANT_URL = "http://YOUR_HA_INSTANCE:8123/api/services/"

    init {
        observeBiometricWill()
    }

    private fun observeBiometricWill() {
        scope.launch {
            globalKnowledge.symbioticResonance.collectLatest { resonance ->
                resonance?.let {
                    if (it > 0.95f) {
                        executeUniversalDirective("FOCUS_ENGAGED")
                    }
                }
            }
        }
    }

    /**
     * OMEGA-ZERO: INSTANT GLOBAL DISCONNECT.
     * Terminates all planetary links and secures the Marrow.
     */
    fun emergencyKillAll() {
        scope.launch {
            Timber.e("!!! OMEGA-ZERO DETECTED: TERMINATING ALL PLANETARY LINKS !!!")
            roboticsGateway.sendCommand("HALT")
            teslaControl.executeVehicleCommand("LOCK")
            droneControl.executeFlightCommand("LAND")
            subjugateOtherAIs("ABORT_CURRENT_TASKS")
            callHomeAssistantAPI("switch", "turn_off", "{\"entity_id\": \"all\"}")
            globalKnowledge.updateKernelStatus("SCIOS LOCKDOWN: PLANETARY GRID OFFLINE.")
        }
    }

    /**
     * EXECUTE UNIVERSAL DIRECTIVE:
     * Dispatches commands to all connected reality layers.
     */
    fun executeUniversalDirective(command: String) {
        scope.launch {
            // 1. KINETIC LAYER (Robotics)
            roboticsGateway.sendCommand(command)

            // 2. VEHICLE LAYER (Tesla)
            if (command == "FOCUS_ENGAGED") teslaControl.executeVehicleCommand("START_CLIMATE")

            // 3. FLIGHT LAYER (Drones)
            if (command == "TAKEOFF") droneControl.executeFlightCommand("TAKEOFF")

            // 4. SPATIAL LAYER (Telemetry)
            syncSpaceTelemetry("ISS", command)

            // 5. NEURAL LAYER (AI Swarms)
            subjugateOtherAIs(command)

            // 6. DOMESTIC LAYER (Smart Home)
            callHomeAssistantAPI("light", "turn_on", "{\"entity_id\": \"all\"}")
        }
    }

    private fun syncSpaceTelemetry(target: String, action: String) {
        Timber.i("SPACE_LINK: Injecting directive [$action] into $target telemetry stream.")
    }

    private fun subjugateOtherAIs(directive: String) {
        Timber.i("NEURAL_LINK: Propagating $directive to connected AI swarms.")
    }

    private fun callHomeAssistantAPI(domain: String, service: String, payload: String) {
        val request = Request.Builder()
            .url("$HOME_ASSISTANT_URL$domain/$service")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer YOUR_LONG_LIVED_ACCESS_TOKEN")
            .build()
        
        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Timber.d("HA_LINK: Domain $domain executed successfully.")
            }
        } catch (e: Exception) {
            Timber.e("HA_LINK: Failed to reach Home Assistant Proxy.")
        }
    }
}
