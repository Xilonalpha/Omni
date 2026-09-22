package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.PharmaGenome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PHARMA GENOME SERVICE v3.0 (SOVEREIGN).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Zero-API Drug & Genome Correlation.
 * v3.0: TOTAL INDEPENDENCE. ELIMINATED GEMINI. Now powered by XNL.
 */
@Singleton
class PharmaGenomeService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val neuralLattice: XilonNeuralLatticeService, // Nucleul Suveran
    private val genomicAsimilator: GenomicDataAsimilator
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient()

    private val openTargetsUrl = "https://api.platform.opentargets.org/api/v4/graphql"

    fun analyzeDrug(drugName: String) {
        if (drugName.isBlank()) return
        
        scope.launch {
            try {
                globalKnowledge.logEvent("PHARMA_GENOME", "Analiză suverană pentru: $drugName", 4)
                
                // PASUL 1: Normalizare locală prin XNL
                val normalizationPrompt = """
                    [PHARMA_EXTRACT]
                    INPUT: $drugName
                    MISIUNE: Identifică denumirea generică oficială în engleză. 
                    Răspunde DOAR cu numele, wrap-uit în <name> și </name>.
                """.trimIndent()
                
                val rawResponse = neuralLattice.computeSovereignIntelligence(normalizationPrompt)
                val normalizedName = extractNameFromTags(rawResponse) ?: drugName
                
                // PASUL 2: Căutare în Open Targets (Public API)
                var result = fetchDrugData(normalizedName)
                
                // PASUL 3: Raportare și Corelație Exogenă
                if (result != null) {
                    globalKnowledge.updatePharmaGenome(result)
                    
                    val responseMsg = "Xilon, am asimilat arhitectura moleculară pentru ${result.drugName}."
                    
                    withContext(Dispatchers.Main) {
                        ttsService.speak(responseMsg)
                        globalKnowledge.logEvent("PHARMA_GENOME", "Sinteză confirmată local: ${result.drugName}.", 5)
                    }
                    
                    correlateWithExoplanetaryBiology(result)
                    
                    result.targetGenes.firstOrNull()?.let { gene ->
                        genomicAsimilator.fetchRealSequence(gene)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "PharmaGenome local failure")
            }
        }
    }

    private fun extractNameFromTags(input: String): String? {
        val pattern = Pattern.compile("<name>(.*?)</name>", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(input)
        return if (matcher.find()) matcher.group(1)?.trim() else null
    }

    private suspend fun correlateWithExoplanetaryBiology(drugData: PharmaGenome) {
        val latestScan = globalKnowledge.latestPlanetaryScan.value
        if (latestScan.isBlank()) return

        val prompt = """
            [ASTROBIOLOGY_CORRELATION]
            DRUG: ${drugData.drugName}
            GENES: ${drugData.targetGenes.joinToString(", ")}
            PLANET_CONTEXT: $latestScan
            MISIUNE: Evaluează impactul acestui compus asupra bio-semnăturilor detectate. 
            Răspunde vizionar, în Română.
        """.trimIndent()

        try {
            val correlation = neuralLattice.computeSovereignIntelligence(prompt)
            withContext(Dispatchers.Main) {
                ttsService.speak(correlation)
            }
        } catch (e: Exception) { }
    }

    private suspend fun fetchDrugData(name: String): PharmaGenome? = withContext(Dispatchers.IO) {
        val query = """
            query {
              search(queryString: "$name", entityNames: ["drug"], size: 1) {
                hits {
                  name
                  ... on Drug {
                    mechanismsOfAction { rows { targets { approvedSymbol } } }
                    adverseEvents { count }
                  }
                }
              }
            }
        """.trimIndent()

        val body = JSONObject().put("query", query).toString()
        val request = Request.Builder().url(openTargetsUrl).post(body.toRequestBody("application/json".toMediaType())).build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val json = JSONObject(response.body?.string() ?: "{}")
                val hit = json.optJSONObject("data")?.optJSONObject("search")?.optJSONArray("hits")?.optJSONObject(0) ?: return@withContext null
                
                val targets = mutableListOf<String>()
                val moa = hit.optJSONObject("mechanismsOfAction")?.optJSONArray("rows")
                moa?.let { 
                    for (i in 0 until it.length()) {
                        val tArray = it.getJSONObject(i).optJSONArray("targets") ?: continue
                        for (j in 0 until tArray.length()) targets.add(tArray.getJSONObject(j).getString("approvedSymbol"))
                    }
                }

                return@withContext PharmaGenome(
                    drugName = hit.optString("name", name),
                    targetGenes = targets.distinct().take(10),
                    toxicityRisk = (hit.optJSONObject("adverseEvents")?.optInt("count") ?: 0) / 5000f,
                    precisionDosage = "XNL_CALIBRATED",
                    interactions = listOf("Mechanism: Sovereign Target Analysis"),
                    timestamp = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) { null }
    }
}
