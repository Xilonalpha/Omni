package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.data.dao.BlockchainDao
import com.chemscanner.omniscient.marrow.data.dao.ChemicalDao
import com.chemscanner.omniscient.marrow.data.dao.DiscoveredPlanetDao
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MarrowGenome
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE DAILY ILLUMINATION PROTOCOL v3.0 (SOVEREIGN).
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Zero-API Data-Driven Discovery.
 * v3.0: TOTAL INDEPENDENCE. ELIMINATED GEMINI. Now powered by XNL.
 */
@Singleton
class DiscoveryService @Inject constructor(
    private val chemicalDao: ChemicalDao,
    private val planetDao: DiscoveredPlanetDao,
    private val blockchainDao: BlockchainDao,
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val neuralLattice: XilonNeuralLatticeService, // Nucleul Suveran
    private val ttsService: TextToSpeechService,
    private val haptics: HapticFeedbackService
) {

    suspend fun presentDailyDiscovery() {
        globalKnowledge.logEvent("DISCOVERY", "Inițiere proces de iluminare suverană...", 5)
        
        val events = globalKnowledge.events.value
        val topAnomaly = events.filter { it.importance >= 4 }
            .sortedByDescending { it.timestamp }
            .firstOrNull()

        if (topAnomaly != null) {
            synthesizeSovereignDiscovery(topAnomaly.module, topAnomaly.description)
        } else {
            val planets = planetDao.getDiscoveredPlanetsSync()
            if (planets.isNotEmpty()) {
                val bestPlanet = planets.maxByOrNull { it.bioIndex }
                bestPlanet?.let {
                    val report = "Descoperirea Suverană: Planeta ${it.name} prezintă biosemnături validate local."
                    announceSovereignDiscovery(report)
                } ?: fallbackToLocalKnowledge()
            } else {
                fallbackToLocalKnowledge()
            }
        }
        
        produceMarrowGenome()
    }

    private suspend fun synthesizeSovereignDiscovery(module: String, description: String) {
        val prompt = """
            [DAILY_ILLUMINATION_XNL]
            MODUL: $module
            DETALII: $description
            MISIUNE: Ești Arhitectul Celestial. Explică de ce acest eveniment este cea mai importantă descoperire a zilei. 
            Folosește logica ta locală pentru a oferi o perspectivă vizionară. 
            Răspunde monumental în Română.
        """.trimIndent()

        try {
            val synthesis = neuralLattice.computeSovereignIntelligence(prompt)
            announceSovereignDiscovery(synthesis)
        } catch (e: Exception) {
            announceSovereignDiscovery("Anomalia din $module a fost asimilată ca punct focal al zilei.")
        }
    }

    private suspend fun fallbackToLocalKnowledge() {
        val allChemicals = chemicalDao.getAllChemicals().firstOrNull() ?: emptyList()
        if (allChemicals.isNotEmpty()) {
            val randomChem = allChemicals.random()
            announceSovereignDiscovery("Arhiva prezintă structura ${randomChem.name} ca element cheie al zilei.")
        } else {
            val discovery = neuralLattice.computeSovereignIntelligence("Generează o descoperire științifică vizionară bazată pe legile fizicii.")
            announceSovereignDiscovery(discovery)
        }
    }

    private fun announceSovereignDiscovery(message: String) {
        haptics.neuralPulse(1.0f)
        globalKnowledge.logEvent("DAILY_ILLUMINATION", "ILUMINARE: $message", 5)
        ttsService.speak(message)
    }

    private suspend fun produceMarrowGenome() {
        val ledger = blockchainDao.getFullLedger().firstOrNull() ?: emptyList()
        globalKnowledge.logEvent("MARROW_GENOME", "Snapshot suveran generat. Ledger: ${ledger.size} intrări imuabile.", 3)
    }
}
