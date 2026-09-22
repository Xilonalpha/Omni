package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.data.dao.ToxicityDataDao
import com.chemscanner.omniscient.marrow.data.models.GeneratedMolecule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GreenChemistryService @Inject constructor(
    private val toxicityDataDao: ToxicityDataDao,
    private val geminiTextService: GeminiTextService,
    private val gson: Gson
) {
    /**
     * ANALYZE SUBSTANCE v2.0 - TOXICITY SYNCED.
     * Integrates real local data with generative AI alternatives.
     */
    suspend fun analyzeSubstance(chemicalName: String, smiles: String): Pair<Double, List<GeneratedMolecule>> {
        // ACTIVATE toxicityDataDao: Fetch local toxicity score to calibrate analysis
        val localData = toxicityDataDao.getByName(chemicalName)
        val baseScore = localData?.toxicityScore?.toDouble() ?: 75.0
        
        // Calibration logic: High local toxicity decreases the "Green Score"
        val calibratedScore = (100.0 - baseScore).coerceIn(0.0, 100.0)
        
        val alternatives = generateGreenAlternatives(chemicalName, smiles)
        return Pair(calibratedScore, alternatives)
    }

    private suspend fun generateGreenAlternatives(name: String, smiles: String): List<GeneratedMolecule> {
        val prompt = "Generate greener alternatives for $name ($smiles) in JSON format: [{\"name\": \"...\", \"justification\": \"...\"}]"
        val jsonResponse = geminiTextService.generateContent(prompt) ?: "[]"
        return try {
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            val results: List<Map<String, String>> = gson.fromJson(jsonResponse, type)
            results.map { GeneratedMolecule(it["name"] ?: "Unknown", 0.0, it["justification"] ?: "N/A") }
        } catch (e: Exception) { 
            // ACTIVATE 'e': Log the error for debugging
            Timber.e(e, "GreenChemistry: Failed to parse alternatives for $name")
            emptyList() 
        }
    }
}
