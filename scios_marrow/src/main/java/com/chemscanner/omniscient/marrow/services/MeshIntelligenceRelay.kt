package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.data.SovereignMemoryDao
import com.chemscanner.omniscient.marrow.data.SovereignMemoryEntity
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.*
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MESH INTELLIGENCE RELAY v1.0
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Decentralized Knowledge Exchange (P2P).
 * v1.0: Protocol pentru partajarea memoriilor suverane prin Ghost Mesh.
 */
@Singleton
class MeshIntelligenceRelay @Inject constructor(
    private val ghostP2P: GhostP2PLink,
    private val memoryDao: SovereignMemoryDao,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val haptics: HapticFeedbackService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Propagă o descoperire importantă în rețeaua mesh locală.
     */
    fun broadcastMemory(memory: SovereignMemoryEntity) {
        scope.launch {
            val payload = JSONObject().apply {
                put("type", "KNOWLEDGE_FRAGMENT")
                put("module", memory.module)
                put("data", memory.description)
                put("importance", memory.importance)
                put("timestamp", memory.timestamp)
            }.toString()

            ghostP2P.relaySovereignData(payload)
            globalKnowledge.logEvent("MESH_RELAY", "Fragment de cunoaștere injectat în mesh.", 3)
        }
    }

    /**
     * Procesează datele primite de la alte noduri (device-uri) din apropiere.
     */
    fun onDataReceived(rawPayload: String) {
        scope.launch {
            try {
                val json = JSONObject(rawPayload)
                if (json.getString("type") == "KNOWLEDGE_FRAGMENT") {
                    val desc = json.getString("data")
                    val module = json.getString("module")
                    val importance = json.getInt("importance")

                    // Salvăm în memoria locală dacă este o informație nouă și importantă
                    memoryDao.insertMemory(SovereignMemoryEntity(
                        module = "MESH_FROM_$module",
                        description = desc,
                        importance = importance
                    ))

                    haptics.lightTick()
                    globalKnowledge.logEvent("MESH_RELAY", "Informație asimilată de la un nod apropiat: $desc", 4)
                }
            } catch (e: Exception) {
                Timber.e("Mesh Relay: Payload corupt.")
            }
        }
    }
}
