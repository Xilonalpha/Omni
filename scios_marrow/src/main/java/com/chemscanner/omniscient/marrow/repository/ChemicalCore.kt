package com.chemscanner.omniscient.marrow.repository

/**
 * THE SCI-OS CHEMICAL CORE (MARROW GENOME).
 * Pure domain model for ANA ISTLA to process chemical logic without Android dependencies.
 */
data class ChemicalCore(
    val name: String,
    val formula: String?,
    val smiles: String? = null,
    val molecularWeight: Double? = null,
    val iupacName: String? = null,
    val description: String? = null,
    val cid: String,
    val sustainabilityScore: Double? = null
)
