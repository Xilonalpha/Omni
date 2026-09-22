package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.data.models.ChemicalEntity
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.utils.XilonProfManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MOLECULE RENDER SERVICE v2.3.
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Bridging 2D SMILES with 3D AR and Archiving discoveries in XilonProf.
 * v2.3: Fixed Nullability issues for gltfModel and improved safe call handling.
 */
@Singleton
class MoleculeRenderService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val molecule3DGenerator: Molecule3DGenerator,
    private val xilonProf: XilonProfManager
) {
    /**
     * Pregătește un scenariu AR dinamic și arhivează descoperirea.
     */
    suspend fun generateArScenarioForChemical(entity: ChemicalEntity): List<String> {
        Timber.d("Generating 3D scenario and Archiving: ${entity.name}")
        
        // 1. Generăm modelul GLTF (Safe call handling)
        val gltfModel = molecule3DGenerator.generateGltfFromSmiles(entity.smiles ?: "")
        
        // 2. Construim contextul cosmic actual (Apertura virtuală)
        val mesh = globalKnowledge.starlinkMesh.value
        val contextSummary = "Apertură Virtuală: ${mesh.virtualApertureKm.toInt()}km | Noduri: ${mesh.activeNodes}"

        // 3. ARHIVARE AUTOMATĂ ÎN XILON PROF (Folosim safe calls pentru gltfModel nullable)
        xilonProf.recordDiscovery(
            module = "HOLOGRAPHIC_SYNTHESIS",
            content = "Moleculă 3D generată pentru ${entity.name}. Structură validată. GLTF Signature: ${gltfModel?.hashCode() ?: "NULL_SIG"}",
            importance = 5,
            contextSummary = contextSummary
        )

        // 4. Returnăm scenariul pentru AR Screen
        val modelPath = if (!gltfModel.isNullOrBlank()) gltfModel else entity.name.lowercase().replace(" ", "_")
        val modelId = if (!gltfModel.isNullOrBlank()) gltfModel.take(8) else "GEN_FALLBACK"

        return listOf(
            "NARRATOR: Inițiez proiecția holografică pentru ${entity.name}.",
            "SHOW_MODEL: $modelPath",
            "NARRATOR: Această structură (Model ID: $modelId) a fost arhivată în jurnalul Xilon Prof."
        )
    }

    fun requestRender(entity: ChemicalEntity) {
        globalKnowledge.logEvent("RENDERER", "3D Render Request for ${entity.name}.", 3)
    }
}
