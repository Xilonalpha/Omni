package com.chemscanner.omniscient.marrow.repository

import android.graphics.BitmapFactory
import com.chemscanner.omniscient.marrow.analysis.LocalKnowledgeSynthesizer
import com.chemscanner.omniscient.marrow.data.dao.ChemicalDao
import com.chemscanner.omniscient.marrow.data.models.ChemicalEntity
import com.chemscanner.omniscient.marrow.data.remote.model.Properties
import com.chemscanner.omniscient.marrow.data.remote.model.PropertyTable
import com.chemscanner.omniscient.marrow.data.remote.model.PubChemResponse
import com.chemscanner.omniscient.marrow.network.ApiResponse
import com.chemscanner.omniscient.marrow.services.GeminiVisionService
import com.chemscanner.omniscient.marrow.services.MoleculeRenderService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.net.URLEncoder
import javax.inject.Inject

/**
 * SOVEREIGN CHEMICAL REPOSITORY IMPLEMENTATION v2.0
 * Fully integrated into MARROW SDK.
 */
class ChemicalRepositoryImpl @Inject constructor(
    private val chemicalDao: ChemicalDao,
    private val gson: Gson,
    private val geminiVisionService: GeminiVisionService,
    private val synthesizer: LocalKnowledgeSynthesizer,
    private val okHttpClient: OkHttpClient,
    private val moleculeRenderService: MoleculeRenderService
) : ChemicalRepository {

    override suspend fun recognizeAndSearchChemical(imageFile: File): ApiResponse<PubChemResponse> {
        return try {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath) ?: return ApiResponse.Error("Failed to decode image")
            
            val prompt = """
            Identify chemical compound in image. 
            Provide JSON: { "PropertyTable": { "Properties": [ { "Title": "Name", "MolecularFormula": "H2O", "CanonicalSMILES": "O" } ] } }.
            """
            val rawResponse = geminiVisionService.getChemicalDataFromImage(bitmap, prompt)
            if (rawResponse.isNullOrBlank()) return ApiResponse.Error("Empty response")
            val jsonString = rawResponse.substringAfter("{").substringBeforeLast("}")
            val pubChemResponse = gson.fromJson("{$jsonString}", PubChemResponse::class.java)
            synthesizer.synthesizeAndStore(pubChemResponse)
            
            // ACTIVATE: Trigger render request for the recognized chemical
            pubChemResponse.propertyTable.properties.firstOrNull()?.let { props ->
                val entity = ChemicalEntity(
                    name = props.title ?: "Unknown",
                    formula = props.molecularFormula,
                    smiles = props.smiles,
                    cid = props.cid?.toString()
                )
                moleculeRenderService.requestRender(entity)
            }
            
            ApiResponse.Success(pubChemResponse)
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Recognition failed")
        }
    }

    override suspend fun searchChemicalByName(chemicalName: String): ApiResponse<PubChemResponse> {
        val localResult = synthesizer.findLocalKnowledge(chemicalName)
        if (localResult != null) {
            val props = Properties(
                localResult.cid?.toIntOrNull(), 
                localResult.formula, 
                localResult.molecularWeight.toString(), 
                localResult.name, 
                null, 
                localResult.smiles, 
                null
            )
            val propertyTable = PropertyTable(listOf(props))
            
            // ACTIVATE: Trigger render request for the local result
            moleculeRenderService.requestRender(localResult)
            
            return ApiResponse.Success(PubChemResponse(propertyTable))
        }

        Timber.d("No local knowledge for $chemicalName. Fetching from API.")
        val apiResponse = fetchFromPubChem("name", URLEncoder.encode(chemicalName, "UTF-8"))
        
        if (apiResponse is ApiResponse.Success) {
            synthesizer.synthesizeAndStore(apiResponse.data)
            
            // ACTIVATE: Trigger render request for the API result
            apiResponse.data.propertyTable.properties.firstOrNull()?.let { props ->
                val entity = ChemicalEntity(
                    name = props.title ?: "Unknown",
                    formula = props.molecularFormula,
                    smiles = props.smiles,
                    cid = props.cid?.toString()
                )
                moleculeRenderService.requestRender(entity)
            }
        }
        
        return apiResponse
    }

    override suspend fun searchChemicalBySmiles(smiles: String): ApiResponse<PubChemResponse> {
        val apiResponse = fetchFromPubChem("smiles", URLEncoder.encode(smiles, "UTF-8"))
        if (apiResponse is ApiResponse.Success) {
            synthesizer.synthesizeAndStore(apiResponse.data)
            
            // ACTIVATE: Trigger render request for the API result
            apiResponse.data.propertyTable.properties.firstOrNull()?.let { props ->
                val entity = ChemicalEntity(
                    name = props.title ?: "Unknown",
                    formula = props.molecularFormula,
                    smiles = props.smiles,
                    cid = props.cid?.toString()
                )
                moleculeRenderService.requestRender(entity)
            }
        }
        return apiResponse
    }

    private fun fetchFromPubChem(type: String, value: String): ApiResponse<PubChemResponse> {
        return try {
            val url = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/$type/$value/property/Title,MolecularFormula,MolecularWeight,CanonicalSMILES,IUPACName/JSON"
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                ApiResponse.Success(gson.fromJson(response.body?.string(), PubChemResponse::class.java))
            } else {
                ApiResponse.Error("Not found")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "API Error")
        }
    }

    override suspend fun saveChemical(chemical: ChemicalEntity) = chemicalDao.insertChemical(chemical)
    override fun getAllChemicals(): Flow<List<ChemicalEntity>> = chemicalDao.getAllChemicals()
    
    override suspend fun getChemicalById(id: Long): ChemicalEntity? = chemicalDao.getChemicalById(id)

    override suspend fun deleteChemical(chemical: ChemicalEntity) = chemicalDao.deleteChemical(chemical)
    
    override suspend fun deleteAllChemicals() = chemicalDao.deleteAllChemicals()
}
