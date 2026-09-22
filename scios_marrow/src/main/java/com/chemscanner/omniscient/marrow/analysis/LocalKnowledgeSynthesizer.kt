package com.chemscanner.omniscient.marrow.analysis

import com.chemscanner.omniscient.marrow.data.dao.ChemicalDao
import com.chemscanner.omniscient.marrow.data.models.ChemicalEntity
import com.chemscanner.omniscient.marrow.data.remote.model.PubChemResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalKnowledgeSynthesizer @Inject constructor(
    private val chemicalDao: ChemicalDao
) {
    suspend fun findLocalKnowledge(name: String): ChemicalEntity? {
        return chemicalDao.getChemicalByName(name)
    }

    suspend fun synthesizeAndStore(response: PubChemResponse) {
        response.propertyTable.properties.forEach { props ->
            val entity = ChemicalEntity(
                name = props.title ?: "Unknown",
                formula = props.molecularFormula,
                smiles = props.smiles,
                cid = props.cid?.toString()
            )
            chemicalDao.insert(entity)
        }
    }
}
