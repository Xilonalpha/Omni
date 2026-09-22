package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.data.dao.BlockchainDao
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE DECENTRALIZED HIVE MIND v2.0 (DATA-DRIVEN).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Collective compute power based on REAL blockchain entries.
 * v2.0: ELIMINATED RANDOM. Power is derived from immutable records.
 */
@Singleton
class HiveMindService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val iotBridge: QuantumIotBridge,
    private val blockchainDao: BlockchainDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        startHyperSync()
    }

    /**
     * REAL SYNC:
     * Instead of random numbers, we count the actual notarized entries in the blockchain.
     * This represents the "Collective Memory" and real power of the system.
     */
    private fun startHyperSync() {
        scope.launch {
            while (isActive) {
                // Numărăm intrările reale din blockchain
                val totalRecords = blockchainDao.getBlockCount() ?: 0
                val currentStatus = globalKnowledge.hiveStatus.value
                
                // Puterea colectivă este o funcție de densitatea datelor imuabile
                val powerFactor = (totalRecords * 0.5f).coerceAtLeast(1.0f)

                globalKnowledge.updateHiveStatus(
                    currentStatus.copy(
                        connectedNodes = totalRecords, // Fiecare record este un punct de memorie
                        networkIntegrity = 1.0f,
                        collectivePower = powerFactor
                    )
                )
                delay(10000) // Sincronizare la 10s
            }
        }
    }

    fun broadcastDiscovery(discovery: String) {
        scope.launch {
            globalKnowledge.logEvent("HIVE_MIND", "Broadcasting Fact: $discovery", 5)
            iotBridge.publishSignal("HIVE_DISCOVERY", discovery)
        }
    }
}
