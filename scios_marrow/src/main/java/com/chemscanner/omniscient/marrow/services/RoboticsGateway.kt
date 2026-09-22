package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

/**
 * THE SOVEREIGN ROBOTICS GATEWAY v1.1 - REDUNDANCY ACTIVE.
 * AUTHORITY: ARCHITECT XILON.
 * v1.1: Implemented Grid Fallback to prevent kinetic deadlocks on UnknownHost.
 * FIXED: UnknownHostException by attempting Grid Broadcast when Direct Link is severed.
 */
@Singleton
class RoboticsGateway @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val iotBridge: Lazy<QuantumIotBridge>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // SOVEREIGN ENDPOINTS (HARDENED)
    private val CLOUD_ENDPOINT = "https://api.bostondynamics.com/v1/spot/command"
    private val LOCAL_ENDPOINT = "http://192.168.80.3/v1/robot/command" // Default Spot Local Access Point
    private val API_KEY = "BD_SOVEREIGN_KEY_XILON"

    /**
     * COMMAND KINETIC UNIT:
     * Translates high-level directives into actions with multi-path redundancy.
     */
    fun sendCommand(action: String) {
        scope.launch {
            Timber.i("ROBOTICS_GATEWAY: Executing $action sequence.")
            
            val payload = when(action) {
                "FOCUS_ENGAGED" -> "{\"command\": \"stand\", \"params\": {\"height\": 0.5}}"
                "HALT" -> "{\"command\": \"sit\", \"params\": {\"priority\": \"immediate\"}}"
                "PATROL" -> "{\"command\": \"mission\", \"name\": \"area_scan_v1\"}"
                else -> "{\"command\": \"stop\"}"
            }

            // PATH 1: Attempt Cloud/Direct Link
            val cloudSuccess = tryExecute(CLOUD_ENDPOINT, payload, action)
            if (cloudSuccess) return@launch

            // PATH 2: Attempt Local Hardware Link (192.168.x.x)
            Timber.w("ROBOTICS_GATEWAY: Cloud Link severed. Switching to Local Hub...")
            val localSuccess = tryExecute(LOCAL_ENDPOINT, payload, action)
            if (localSuccess) return@launch

            // PATH 3: FINAL FALLBACK - SCI-OS GRID BROADCAST (MQTT)
            // If direct network calls fail, we send the command to the MQTT grid
            // where a local PC Terminal or bridge can execute it.
            Timber.e("ROBOTICS_GATEWAY: Hardware unreachable. Broadcasting to Sci-OS Grid Mesh.")
            iotBridge.getOrNull() ?:.publishSignal("sci_os/robotics/fallback", "CMD:$action|PAYLOAD:$payload")
            globalKnowledge.logEvent("ROBOTICS_FAILOVER", "Command $action rerouted to Grid Mesh.", 5)
        }
    }

    private suspend fun tryExecute(url: String, payload: String, action: String): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .post(payload.toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $API_KEY")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    globalKnowledge.logEvent("ROBOTICS", "Command $action delivered via $url", 4)
                    return true
                }
                false
            }
        } catch (e: Exception) {
            // Log as warning - handled by the fallback chain
            Timber.w("ROBOTICS_GATEWAY: Link $url failed: ${e.message}")
            false
        }
    }
}
