package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

/**
 * THE GENOMIC DATA ASIMILATOR v1.1.
 * Integration: NCBI (National Center for Biotechnology Information) / GenBank.
 * Associates digital synthetic life with real biological DNA sequences.
 * v1.1: Integrated with BioBridgeService to activate DNA Archiving.
 */
@Singleton
class GenomicDataAsimilator @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val bioBridge: BioBridgeService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val ncbiBaseUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils"

    init {
        // ACTIVARE SCOPE: Verificăm conexiunea la baza de date genomică la inițializare
        scope.launch {
            globalKnowledge.logEvent("GENOMIC_LINK", "Genomic Data Asimilator initialized. Scope active.", 2)
        }
    }

    data class GeneticRecord(
        val id: String,
        val definition: String,
        val sequence: String,
        val source: String = "NCBI GenBank"
    )

    /**
     * ASSIMILATE ORGANISM:
     * Searches NCBI for a real genetic sequence based on the organism's name.
     */
    suspend fun fetchRealSequence(organismName: String): GeneticRecord? = withContext(Dispatchers.IO) {
        return@withContext try {
            // 1. Search for the ID (esearch)
            val searchUrl = "$ncbiBaseUrl/esearch.fcgi?db=nucleotide&term=$organismName&retmode=json"
            val searchRequest = Request.Builder().url(searchUrl).build()
            
            val id = okHttpClient.newCall(searchRequest).execute().use { response ->
                val body = response.body?.string()
                val json = JSONObject(body ?: "{}")
                json.getJSONObject("esearchresult")
                    .getJSONArray("idlist")
                    .optString(0)
            }

            if (id.isNullOrBlank()) {
                Timber.w("GENOMIC_LINK: No record found for $organismName")
                return@withContext null
            }

            // 2. Fetch the actual record (efetch)
            val fetchUrl = "$ncbiBaseUrl/efetch.fcgi?db=nucleotide&id=$id&rettype=fasta&retmode=text"
            val fetchRequest = Request.Builder().url(fetchUrl).build()

            okHttpClient.newCall(fetchRequest).execute().use { response ->
                val fastaData = response.body?.string() ?: ""
                val lines = fastaData.lines()
                val definition = lines.firstOrNull()?.removePrefix(">") ?: organismName
                val sequence = lines.drop(1).joinToString("")
                
                globalKnowledge.logEvent("GENOMIC_LINK", "Real DNA sequence for $organismName assimilated.", 4)
                
                // ACTIVARE BIO-BRIDGE: Transducție secvență digitală în arhivă biologică
                bioBridge.initiateDnaArchiving(sequence)
                
                GeneticRecord(id, definition, sequence.take(1000)) // Cap sequence for mobile memory
            }
        } catch (e: Exception) {
            Timber.e(e, "GENOMIC_LINK: Failed to access NCBI database for $organismName.")
            null
        }
    }
}
