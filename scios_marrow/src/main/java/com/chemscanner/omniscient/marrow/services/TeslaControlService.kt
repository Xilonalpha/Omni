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

/**
 * TESLA CONTROL SERVICE: SOVEREIGN VEHICLE INTERFACE v2.0.
 * AUTHORITY: ARCHITECT XILON.
 * v2.0: ACTIVATED - Acum preia VIN-ul și Token-ul securizat din SovereignKeyVault.
 */
@Singleton
class TeslaControlService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val keyVault: SovereignKeyVault // REPARAT: Legat de seiful de chei
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val TESLA_API_BASE = "https://fleet-api.prd.na.vn.cloud.tesla.com/api/1/vehicles/"

    fun executeVehicleCommand(command: String) {
        val vehicleId = keyVault.getKey("TESLA_VIN")
        val accessToken = keyVault.getKey("TESLA_TOKEN")

        if (vehicleId.isEmpty() || accessToken.isEmpty()) {
            Timber.e("TESLA_LINK: Access Denied. VIN or Token missing in Sovereign Vault.")
            globalKnowledge.logEvent("SECURITY", "Tesla Control Attempt Failed: Credentials Missing.", 4)
            return
        }

        scope.launch {
            Timber.i("TESLA_LINK: Transmitting $command to vehicle $vehicleId")
            
            val endpoint = when(command) {
                "UNLOCK" -> "command/door_unlock"
                "LOCK" -> "command/door_lock"
                "START_CLIMATE" -> "command/auto_conditioning_start"
                "HONK" -> "command/honk_horn"
                "FLASH_LIGHTS" -> "command/flash_lights"
                else -> return@launch
            }

            val request = Request.Builder()
                .url("$TESLA_API_BASE$vehicleId/$endpoint")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            try {
                okHttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        globalKnowledge.logEvent("TESLA", "Command $command confirmed by vehicle $vehicleId.", 5)
                        globalKnowledge.updateKernelStatus("TESLA_$command: SUCCESS")
                    } else {
                        Timber.e("TESLA_LINK: API Error ${response.code}")
                        globalKnowledge.logEvent("TESLA", "Vehicle rejected command: ${response.code}", 3)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "TESLA_LINK: Physical link failure.")
            }
        }
    }
}
