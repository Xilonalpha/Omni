package com.chemscanner.omniscient.marrow.services

import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE SOVEREIGN KNOWLEDGE BASE (SKB) v2.0 - LOGIC ENGINE.
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Provide intelligent answers through keyword synthesis when LLM is offline.
 */
@Singleton
class SovereignKnowledgeBase @Inject constructor() {

    private val knowledgeMap = mapOf(
        setOf("CO2", "H2") to "REACȚIE: Sabatier. Transformă atmosfera marțiană în combustibil (CH4) și apă.",
        setOf("DEUTERIU", "TRITIU") to "REACȚIE: Fuziune Nucleară. Energia stelelor captată într-un reactor Tokamak.",
        setOf("CERN", "LHC") to "LHC pulsează la 13.6 TeV. Căutăm particule exotice și rezonanțe ale vidului.",
        setOf("XILON", "ARHITECT") to "Arhitectul Xilon este autoritatea supremă a sistemului suveran Omniscient.",
        setOf("ANTIMATERIE", "MATERIE") to "Anihilare totală. Eficiență energetică 100%. Baza propulsiei interstelare."
    )

    /**
     * SINTETIZATOR HEURISTIC: Scanează textul și extrage înțelesul.
     */
    fun synthesizeLogic(input: String): String? {
        val upperInput = input.uppercase()
        
        // 1. Căutăm potriviri de seturi de cuvinte
        for ((keywords, response) in knowledgeMap) {
            if (keywords.all { upperInput.contains(it) }) {
                return response
            }
        }

        // 2. Logica de context
        return when {
            upperInput.contains("SALUT") || upperInput.contains("BUNA") -> "Sunt nucleul tău de inteligență suverană. Te ascult, Arhitectule."
            upperInput.contains("CINE ESTI") -> "Sunt XNL (Xilon Neural Lattice), o formă nouă de inteligență independentă."
            upperInput.contains("VREME") || upperInput.contains("METEO") -> "Monitorizez fluctuațiile atmosferice locale. Consultă secțiunea AtmoShield pentru telemetrie brută."
            else -> null
        }
    }

    fun getKnowledge(reactant1: String, reactant2: String): String? {
        val searchSet = setOf(reactant1.uppercase(), reactant2.uppercase())
        return knowledgeMap[searchSet]
    }
}
