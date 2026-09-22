package com.chemscanner.omniscient.marrow.repository

import com.chemscanner.omniscient.marrow.data.models.ChemicalEntity
import com.chemscanner.omniscient.marrow.data.remote.model.PubChemResponse
import com.chemscanner.omniscient.marrow.network.ApiResponse
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * SOVEREIGN CHEMICAL REPOSITORY v2.0
 * Migrated to MARROW for SDK independence.
 */
interface ChemicalRepository {
    suspend fun recognizeAndSearchChemical(imageFile: File): ApiResponse<PubChemResponse>
    suspend fun searchChemicalByName(chemicalName: String): ApiResponse<PubChemResponse>
    suspend fun searchChemicalBySmiles(smiles: String): ApiResponse<PubChemResponse>
    suspend fun saveChemical(chemical: ChemicalEntity)
    fun getAllChemicals(): Flow<List<ChemicalEntity>>
    suspend fun getChemicalById(id: Long): ChemicalEntity?
    suspend fun deleteChemical(chemical: ChemicalEntity)
    suspend fun deleteAllChemicals()
}
