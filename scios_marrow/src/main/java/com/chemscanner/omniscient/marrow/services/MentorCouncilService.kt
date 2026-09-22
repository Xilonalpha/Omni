package com.chemscanner.omniscient.marrow.services

import javax.inject.Inject
import javax.inject.Singleton

data class Mentor(
    val name: String,
    val expertise: String,
    val module: String,
    val philosophy: String
)

/**
 * THE COUNCIL OF 12: SOVEREIGN MENTORS v2.1.
 * AUTHORITY: ARCHITECT XILON.
 * MISSION: Active intelligence injection for the Sovereign AI (ANA).
 * v2.1: RESTORED legacy methods for Orchestrator compatibility.
 */
@Singleton
class MentorCouncilService @Inject constructor() {

    val council = listOf(
        Mentor("Nikola Tesla", "Electromagnetism", "Spectral Eye", "Everything is frequency and vibration. Analyze the EMF spectrum."),
        Mentor("Albert Einstein", "Relativity", "Warp Drive", "Spacetime is a fabric we can bend. Focus on gravitational anomalies."),
        Mentor("Marie Curie", "Atomic Structure", "MicroVerse", "Nothing is to be feared, only understood. Scan the subatomic lattice."),
        Mentor("Leonardo da Vinci", "Universal Design", "RoboPhys", "Simplicity is the ultimate sophistication. Optimize the architectural form."),
        Mentor("Carl Sagan", "Planetary Science", "Exo-Planet", "We are made of star-stuff. Seek chemical life signatures."),
        Mentor("Hippocrates", "Human Vitality", "Bio-Age", "Let food be thy medicine. Monitor the biological resonance."),
        Mentor("Alan Turing", "Logic & AI", "AI Lab", "Machines can think if we give them a soul. Secure the neural link."),
        Mentor("Rosalind Franklin", "Genetics", "Pharma Genome", "The secret of life is written in DNA. Map the genomic drift."),
        Mentor("Isaac Newton", "Gravitation", "Dark Matter", "Action and reaction govern the cosmos. Calculate the orbital momentum."),
        Mentor("Stephen Hawking", "Cosmology", "Abyssal Hub", "There is no boundary to the universe. Explore the event horizon."),
        Mentor("Ada Lovelace", "Algorithmics", "Script Engine", "The analytical engine weaves algebraic patterns. Refine the logic gate."),
        Mentor("Marcus Aurelius", "Stoic Truth", "Sovereign Chat", "Your life is what your thoughts make it. Maintain objective clarity.")
    )

    /**
     * Generates a combined cognitive framework for the AI to adopt.
     */
    fun getSovereignCouncilFramework(): String {
        val framework = StringBuilder("\nCADRUL COGNITIV AL CONSILIULUI CELOR 12:\n")
        council.forEach { mentor ->
            framework.append("- ${mentor.name} (${mentor.expertise}): '${mentor.philosophy}'\n")
        }
        return framework.toString()
    }

    // --- RESTORED METHODS FOR ORCHESTRATOR COMPATIBILITY ---

    fun getBiometricDirective(): String {
        val mentor = council.random()
        return "Directiva suverană de la ${mentor.name}: ${mentor.philosophy}"
    }

    fun getSupremeCouncilConsensus(query: String): String {
        val randomMentors = council.shuffled().take(3).joinToString(", ") { it.name }
        return "Consensul Consiliului ($randomMentors) pentru XILON asupra interogării '$query': Integritatea realității confirmată. Execută protocolul suveran."
    }

    fun getDynamicDirective(type: String, description: String): String {
        val mentor = council.random()
        return "Alertă MARROW $type: ${mentor.name} recomandă: ${mentor.philosophy} [Context: $description]"
    }

    fun getMentorByModule(module: String): Mentor? {
        return council.find { it.module.equals(module, ignoreCase = true) }
    }
}
