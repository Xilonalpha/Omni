package com.chemscanner.omniscient.marrow.repository

import com.chemscanner.omniscient.marrow.data.models.QuizQuestion
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SOVEREIGN QUIZ REPOSITORY v2.0
 * Migrated to MARROW for SDK independence.
 */
@Singleton
class QuizRepository @Inject constructor() {

    fun getChemistryQuiz(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = UUID.randomUUID().toString(),
                question = "What is the atomic symbol for Gold?",
                options = listOf("Gd", "Au", "Ag", "Fe"),
                correctAnswerIndex = 1,
                explanation = "The symbol Au comes from the Latin word for gold, 'aurum'."
            ),
            QuizQuestion(
                id = UUID.randomUUID().toString(),
                question = "Which gas is most abundant in Earth's atmosphere?",
                options = listOf("Oxygen", "Carbon Dioxide", "Nitrogen", "Hydrogen"),
                correctAnswerIndex = 2,
                explanation = "Nitrogen makes up about 78% of Earth's atmosphere."
            ),
            QuizQuestion(
                id = UUID.randomUUID().toString(),
                question = "What is the pH value of pure water?",
                options = listOf("0", "5", "7", "14"),
                correctAnswerIndex = 2,
                explanation = "Pure water is neutral, which corresponds to a pH of 7."
            ),
            QuizQuestion(
                id = UUID.randomUUID().toString(),
                question = "Which element has the highest electronegativity?",
                options = listOf("Oxygen", "Fluorine", "Chlorine", "Neon"),
                correctAnswerIndex = 1,
                explanation = "Fluorine is the most electronegative element on the Pauling scale (3.98)."
            ),
            QuizQuestion(
                id = UUID.randomUUID().toString(),
                question = "What is the main component of natural gas?",
                options = listOf("Ethane", "Propane", "Butane", "Methane"),
                correctAnswerIndex = 3,
                explanation = "Methane (CH4) typically makes up 70-90% of natural gas."
            )
        )
    }
}
