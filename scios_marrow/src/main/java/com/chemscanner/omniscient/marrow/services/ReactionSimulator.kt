package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.data.dao.CachedReactionDao
import com.chemscanner.omniscient.marrow.data.models.CachedReaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReactionSimulator @Inject constructor(
    private val geminiTextService: GeminiTextService,
    private val cachedReactionDao: CachedReactionDao,
    private val sovereignKnowledge: SovereignKnowledgeBase // INJECTED: The Human Knowledge Base
) {
    suspend fun simulateReaction(reactant1: String, reactant2: String): String = withContext(Dispatchers.IO) {
        val r1 = reactant1.trim()
        val r2 = reactant2.trim()
        if (r1.isBlank() || r2.isBlank()) return@withContext "Provide reactants."

        // 1. Check Sovereign Human Knowledge Base first (Offline & Accurate)
        val knownFact = sovereignKnowledge.getKnowledge(r1, r2)
        if (knownFact != null) return@withContext knownFact

        // 2. Check Local Cache (Previous Simulations)
        val cachedResult = cachedReactionDao.getReaction(r1, r2)
        if (cachedResult != null) return@withContext cachedResult.result

        // 3. Fallback to AI (Gemini) for complex or unknown reactions
        val prompt = """
            Ești Inteligența Artificială SCI-OS (Ana Istla). 
            Simulează reacția chimică dintre $r1 și $r2. 
            Oferă ecuația chimică, rezultatul și o explicație scurtă despre utilitatea sa în lumea reală.
        """.trimIndent()
        
        val onlineResult = geminiTextService.generateContent(prompt)
        
        return@withContext onlineResult ?: "Simulation failed. Check Zero-Link connectivity."
    }
}
