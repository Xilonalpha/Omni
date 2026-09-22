package com.chemscanner.omniscient.marrow.services

import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

/**
 * GHOST MESH P2P v1.2 (STABLE LINK).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Establish direct device-to-device links using WiFi Direct (Ad-hoc).
 * v1.2: Fixed potential circular dependency by using Lazy<ShadowMeshService>.
 */
@Singleton
/**
 * ⚠️ DISCLOSURE: This service name is ambitious but functionality is grounded.
 * See implementation for actual capabilities and limitations.
 */
class GhostP2PLink @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val shadowMesh: Lazy<ShadowMeshService> // Break circular link
) {
    private val manager: WifiP2pManager? = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    private val channel = manager?.initialize(context, context.mainLooper, null)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun startGhostDiscovery() {
        globalKnowledge.updateWorldMeshStatus("Ghost Mesh Initializing...")
        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                globalKnowledge.logEvent("GHOST_MESH", "Scanning for nearby Architects...", 4)
                globalKnowledge.updateWorldMeshStatus("Mesh Active: Ghost Mode")
            }
            override fun onFailure(reason: Int) {
                Timber.e("GhostMesh: Discovery failed ($reason)")
            }
        })
    }

    /**
     * RELAY DATA: Trimite un pachet RoNaQCI direct către un alt nod detectat.
     */
    fun relaySovereignData(payload: String) {
        scope.launch {
            val securedData = shadowMesh.getOrNull() ?.broadcastSovereignData(payload)
            globalKnowledge.logEvent("GHOST_MESH", "Data packet injected into local proximity mesh.", 5)
        }
    }
}
