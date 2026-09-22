package com.chemscanner.omniscient.marrow.repository

import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CERN REAL-TIME DATA BRIDGE v1.0.
 * MISSION: Fetch live accelerator status from CERN Vistars / Open Data.
 * AUTHORITY: ARCHITECT XILON.
 */
@Singleton
class CernDataRepository @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    // Public CERN Vistars JSON URL
    private val cernStatusUrl = "https://op-vistar-server.web.cern.ch/vistar/get_lhc_main.php"

    data class LhcStatus(
        val isOperational: Boolean,
        val beamEnergyTev: Float,
        val statusMessage: String,
        val luminosity: Double,
        val fillNumber: Int,
        // Explicit flag so the UI can never mistake a fallback/simulated reading for a live one.
        val isSimulated: Boolean = false
    )

    /**
     * FETCH LIVE LHC STATUS:
     * Connects to the official CERN Operation Vistars.
     */
    suspend fun fetchLiveLhcStatus(): LhcStatus = withContext(Dispatchers.IO) {
        return@withContext try {
            val request = Request.Builder().url(cernStatusUrl).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyText = response.body?.string() ?: "{}"
                    val json = JSONObject(bodyText)
                    // Only treat this as real data if the fields we need actually parsed out of
                    // the response. If CERN changes their JSON schema and the fields go missing,
                    // json.optX(...) would otherwise silently substitute plausible-looking numbers
                    // that look identical to real live data. We check for presence explicitly instead.
                    if (json.has("status") && json.has("energy")) {
                        LhcStatus(
                            isOperational = json.optString("status") == "STABLE BEAMS",
                            beamEnergyTev = json.optDouble("energy", 6.8).toFloat(),
                            statusMessage = json.optString("message", "LHC Beam Process: Stable"),
                            luminosity = json.optDouble("luminosity", 2.0e34),
                            fillNumber = json.optInt("fill", 9842),
                            isSimulated = false
                        )
                    } else {
                        Timber.w("CERN_LINK: Response schema changed / missing expected fields, falling back to simulated status.")
                        getFallbackStatus()
                    }
                } else {
                    Timber.w("CERN_LINK: HTTP ${response.code}, falling back to simulated status.")
                    getFallbackStatus()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "CERN_LINK: Failure to reach Geneva uplink.")
            getFallbackStatus()
        }
    }

    // Used only when the live CERN endpoint is unreachable or its schema doesn't match what we
    // expect. isSimulated = true so callers/UI must surface this as an estimate, never as live data.
    private fun getFallbackStatus(): LhcStatus {
        return LhcStatus(
            isOperational = true,
            beamEnergyTev = 6.8f,
            statusMessage = "LHC: Stable Beams (SIMULATED - live uplink unavailable)",
            luminosity = 1.8e34,
            fillNumber = 9842,
            isSimulated = true
        )
    }
    
    // MODEL STANDARD DATA
    data class RealParticle(val name: String, val massGev: Double, val charge: Int, val spin: String)
    
    fun getStandardModelParticles(): List<RealParticle> {
        return listOf(
            RealParticle("Higgs Boson", 125.1, 0, "0"),
            RealParticle("Top Quark", 172.76, 2, "1/2"),
            RealParticle("Z Boson", 91.187, 0, "1"),
            RealParticle("W Boson", 80.37, 1, "1"),
            RealParticle("Bottom Quark", 4.18, -1, "1/2")
        )
    }
}
