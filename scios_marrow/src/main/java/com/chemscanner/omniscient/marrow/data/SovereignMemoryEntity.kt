package com.chemscanner.omniscient.marrow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * THE SOVEREIGN MEMORY ENTITY.
 * MISSION: Persist episodes of consciousness and technical discoveries.
 */
@Entity(tableName = "sovereign_memory")
data class SovereignMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val module: String, // ex: STARLINK_MESH, PLANETARY_DEFENSE, ANA_THOUGHT
    val description: String,
    val importance: Int = 1,
    val metadata: String? = null // JSON data for specific details
)
