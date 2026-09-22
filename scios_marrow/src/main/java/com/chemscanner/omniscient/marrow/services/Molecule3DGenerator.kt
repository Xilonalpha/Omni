package com.chemscanner.omniscient.marrow.services

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE SOVEREIGN MOLECULAR ARCHITECT v3.0 (ZERO-API).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Local 3D synthesis of molecular structures without external dependencies.
 * v3.0: ELIMINATED GEMINI. Now powered by Xilon Neural Lattice & Local Knowledge Base.
 */
@Singleton
class Molecule3DGenerator @Inject constructor(
    private val neuralLattice: XilonNeuralLatticeService,
    private val knowledgeBase: SovereignKnowledgeBase
) {

    /**
     * ACTIVARE: Generează un model 3D (format GLTF JSON) local.
     */
    suspend fun generateGltfFromSmiles(smiles: String): String? {
        if (smiles.isBlank()) return null

        // Încercăm întâi să vedem dacă avem date pre-calculate în baza de cunoștințe suverană
        val internalIntel = knowledgeBase.getKnowledge(smiles, "STRUCTURE")
        
        val prompt = """
            [MOLECULAR_ARCHITECT_XNL]
            SMILES: $smiles
            CONTEXT_INTERN: ${internalIntel ?: "Generic Geometry"}
            
            MISIUNE: Generează EXCLUSIV un cod JSON GLTF valid pentru această moleculă.
            - Sfere pentru atomi, Cilindri pentru legături.
            - Culori CPK.
            - Geometrie spațială corectă.
            Fără text explicativ, doar JSON-ul brut.
        """.trimIndent()

        return try {
            val rawGltf = neuralLattice.computeSovereignIntelligence(prompt)
            
            // Curățare agresivă pentru a extrage doar JSON-ul valid din motorul local
            val cleanedGltf = extractJson(rawGltf)
            
            if (cleanedGltf != null && cleanedGltf.contains("asset")) {
                Timber.d("Molecule3D: Local GLTF synthesized for $smiles")
                cleanedGltf
            } else {
                Timber.w("Molecule3D: Local engine failed to produce valid GLTF.")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Molecule3D: Local synthesis error")
            null
        }
    }

    private fun extractJson(input: String): String? {
        val start = input.indexOf("{")
        val end = input.lastIndexOf("}")
        return if (start != -1 && end != -1 && end > start) {
            input.substring(start, end + 1)
        } else null
    }
}
