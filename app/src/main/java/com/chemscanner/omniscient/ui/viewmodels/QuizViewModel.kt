package com.chemscanner.omniscient.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.QuizRepository
import com.chemscanner.omniscient.marrow.repository.UniversalLaw
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class Question(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val associatedLaw: UniversalLaw? = null
)

data class QuizUiState(
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val isAnswered: Boolean = false,
    val selectedOptionIndex: Int = -1,
    val isQuizFinished: Boolean = false
) {
    val totalQuestions: Int get() = questions.size
    val isLastQuestion: Boolean get() = currentQuestionIndex == questions.size - 1
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState(questions = getInitialQuestionSet()))
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private fun getInitialQuestionSet(): List<Question> {
        // ACTIVARE getChemistryQuiz: Combinăm întrebările hardcoded cu cele din repository-ul suveran
        val repoQuestions = quizRepository.getChemistryQuiz().map { q ->
            Question(
                question = q.question,
                options = q.options,
                correctAnswerIndex = q.correctAnswerIndex,
                explanation = q.explanation,
                associatedLaw = when {
                    q.question.contains("atomic") -> UniversalLaw.QUANTUM_TUNNELING
                    q.question.contains("electronegativity") -> UniversalLaw.CARBON_BOND_STABILITY
                    q.question.contains("natural gas") -> UniversalLaw.VACUUM_ENERGY_HARVESTING
                    else -> null
                }
            )
        }
        return (repoQuestions + getExtendedQuestionSet()).shuffled()
    }

    fun onOptionSelected(index: Int) {
        if (_uiState.value.isAnswered) return
        
        val currentQuestion = _uiState.value.questions[_uiState.value.currentQuestionIndex]
        val isCorrect = index == currentQuestion.correctAnswerIndex

        if (isCorrect && currentQuestion.associatedLaw != null) {
            globalKnowledge.activateLaw(currentQuestion.associatedLaw)
        }
        
        _uiState.update { state ->
            state.copy(
                isAnswered = true,
                selectedOptionIndex = index,
                score = if (isCorrect) state.score + 1 else state.score
            )
        }
    }

    fun nextQuestion() {
        _uiState.update { state ->
            if (state.isLastQuestion) {
                state.copy(isQuizFinished = true)
            } else {
                state.copy(
                    currentQuestionIndex = state.currentQuestionIndex + 1,
                    isAnswered = false,
                    selectedOptionIndex = -1
                )
            }
        }
    }

    fun restartQuiz() {
        _uiState.update { QuizUiState(questions = getInitialQuestionSet()) }
    }

    private fun getExtendedQuestionSet(): List<Question> {
        return listOf(
            Question("Care este elementul cu cel mai mic număr atomic?", listOf("Heliu", "Hidrogen", "Litiu", "Oxigen"), 1, "Hidrogenul are numărul atomic 1."),
            Question("Ce tip de legătură presupune partajarea electronilor?", listOf("Ionică", "Metalică", "Covalentă", "Van der Waals"), 2, "Legătura covalentă se bazează pe punerea în comun a electronilor.", UniversalLaw.CARBON_BOND_STABILITY),
            Question("Care este pH-ul unei soluții neutre la 25°C?", listOf("0", "14", "1", "7"), 3, "pH-ul 7 este considerat neutru."),
            Question("Cine a descoperit Poloniul și Radiul?", listOf("Einstein", "Mendeleev", "Marie Curie", "Newton"), 2, "Marie Curie a fost pionieră în studiul radioactivității."),
            Question("Ce gaz este eliberat în reacția dintre un acid și un metal activ?", listOf("Oxigen", "Hidrogen", "Azot", "Clor"), 1, "Reacția produce sare și degajă Hidrogen."),
            Question("Care este formula chimică a glucozei?", listOf("C12H22O11", "CH4", "C6H12O6", "C2H5OH"), 2, "C6H12O6 este formula standard a glucozei."),
            Question("Ce element se găsește în toate substanțele organice?", listOf("Azot", "Aur", "Carbon", "Fier"), 2, "Carbonul este elementul de bază al vieții și al chimiei organice."),
            Question("Cum se numește trecerea unei substanțe din stare solidă direct în stare gazoasă?", listOf("Topire", "Sublimare", "Vaporizare", "Condensare"), 1, "Sublimarea este procesul direct solid-gaz."),
            Question("Care este cel mai abundent gaz din atmosfera Pământului?", listOf("Oxigen", "Dioxid de Carbon", "Argon", "Azot"), 3, "Azotul reprezintă aproximativ 78% din atmosferă."),
            Question("Ce particulă subatomică are sarcină negativă?", listOf("Proton", "Neutron", "Electron", "Pozitron"), 2, "Electronii orbitează nucleul și au sarcină negativă.", UniversalLaw.QUANTUM_TUNNELING),
            Question("Care este unitatea de măsură pentru cantitatea de substanță?", listOf("Kilogram", "Litru", "Mol", "Metru"), 2, "Molul este unitatea fundamentală în SI pentru cantitate."),
            Question("Ce culoare are flacăra în prezența cuprului?", listOf("Roșu", "Verde-Albastru", "Galben", "Violet"), 1, "Cuprul arde cu o flacără caracteristică verde-albastră."),
            Question("Care este simbolul chimic pentru Aur?", listOf("Ag", "Fe", "Au", "Pb"), 2, "Au provine din latinescul 'Aurum'."),
            Question("Ce lege afirmă că volumul unui gaz este invers proporțional cu presiunea la temperatură constantă?", listOf("Legea lui Charles", "Legea lui Boyle", "Legea lui Avogadro", "Legea lui Dalton"), 1, "Este legea Boyle-Mariotte."),
            Question("Ce element are configurația electronică 1s² 2s² 2p⁶?", listOf("Neon", "Oxigen", "Fluor", "Sodiu"), 0, "Neonul are 10 electroni, completând stratul 2."),
            Question("Care este cel mai electronegativ element din tabelul periodic?", listOf("Oxigen", "Clor", "Fluor", "Fransiu"), 2, "Fluorul are cea mai mare afinitate pentru electroni."),
            Question("Ce substanță este supranumită 'solventul universal'?", listOf("Alcoolul", "Apa", "Benzenul", "Acetona"), 1, "Apa poate dizolva o varietate imensă de substanțe."),
            Question("Din ce este format nucleul unui atom de Hidrogen (Protiu)?", listOf("Un proton și un neutron", "Doar un neutron", "Un proton și un electron", "Doar un proton"), 3, "Hidrogenul simplu are doar un proton în nucleu."),
            Question("Care este valoarea numărului lui Avogadro?", listOf("6.022 x 10^23", "3.14 x 10^10", "1.6 x 10^-19", "9.81"), 0, "Acesta reprezintă numărul de particule într-un mol."),
            Question("Ce element este lichid la temperatura camerei?", listOf("Magneziu", "Mercur", "Mangan", "Molibden"), 1, "Mercurul (Hg) este singurul metal lichid în condiții standard."),
            Question("Ce este un izotop?", listOf("Atomi cu același nr. de neutroni", "Atomi cu același nr. de protoni, dar neutroni diferiți", "Atomi cu mase identice", "Molecule cu aceeași formulă"), 1, "Izotopii au același Z, dar A diferit."),
            Question("Care este principala componentă a diamantului?", listOf("Siliciu", "Carbon", "Carbonat de calciu", "Aur"), 1, "Diamantul este o formă alotropică a carbonului."),
            Question("Ce proteină transportă oxigenul în sânge?", listOf("Insulina", "Hemoglobina", "Colagenul", "Keratina"), 1, "Hemoglobina conține fier și leagă oxigenul."),
            Question("Ce acid se găsește în stomac?", listOf("Acid sulfuric", "Acid clorhidric", "Acid azotic", "Acid acetic"), 1, "HCl ajută la digestia alimentelor."),
            Question("Care este cel mai ușor metal?", listOf("Sodiu", "Litiu", "Aluminiu", "Magneziu"), 1, "Litiul are cea mai mică densitate dintre metale."),
            Question("Ce proces transformă zahărul în alcool?", listOf("Oxidare", "Fermentație", "Hidroliză", "Condensare"), 1, "Fermentația alcoolică este produsă de drojdii."),
            Question("Care este simbolul pentru Argint?", listOf("Ag", "Ar", "Au", "Si"), 0, "Ag vine de la 'Argentum'."),
            Question("Ce particule formează nucleul atomic?", listOf("Protoni și electroni", "Protoni și neutroni", "Neutroni și electroni", "Doar protoni"), 1, "Nucleul este format din nucleoni (p+ și n0)."),
            Question("Care este formula apei oxigenate?", listOf("H2O", "HO2", "H2O2", "H3O"), 2, "Peroxidul de hidrogen este H2O2."),
            Question("Ce tip de reacție eliberează căldură?", listOf("Endotermă", "Exotermă", "Izotermă", "Adiabatică"), 1, "Reacțiile exoterme produc energie termică."),
            Question("Cine a propus primul model atomic modern (bile de biliard)?", listOf("Thomson", "Rutherford", "Dalton", "Bohr"), 2, "John Dalton a reintrodus conceptul de atom."),
            Question("Ce metal este folosit în bateriile telefoanelor moderne?", listOf("Plumb", "Cadmiu", "Litiu", "Nichel"), 2, "Bateriile Li-Ion sunt standardul actual."),
            Question("Care este componenta principală a gazului natural?", listOf("Etan", "Metan", "Propan", "Butan"), 1, "Metanul (CH4) predomină în gazul natural."),
            Question("Ce element are simbolul 'K'?", listOf("Kripton", "Calciu", "Potasiu", "Cobalt"), 2, "K vine de la 'Kalium'."),
            Question("Cum se numește amestecul de cupru și cositor?", listOf("Alamă", "Bronz", "Oțel", "Duraluminiu"), 1, "Bronzul este o aliaj clasic Cu-Sn."),
            Question("Ce scară măsoară intensitatea unui cutremur?", listOf("Celsius", "Richter", "Kelvin", "pH"), 1, "Scara Richter este logaritmică pentru magnitudine."),
            Question("Care este viteza luminii în vid?", listOf("300.000 km/s", "150.000 km/s", "1.000.000 km/s", "340 m/s"), 0, "Viteza luminii este de aprox. 3x10^8 m/s."),
            Question("Ce forță ne ține pe Pământ?", listOf("Magnetică", "Centrifugă", "Gravitațională", "Nucleară"), 2, "Gravitația este atracția universală a masei.", UniversalLaw.GRAVITATIONAL_ANOMALY),
            Question("Care este punctul de fierbere al apei la presiune normală?", listOf("0°C", "50°C", "100°C", "200°C"), 2, "Apa fierbe la 100 grade Celsius."),
            Question("Ce element este esențial pentru oase?", listOf("Fier", "Iod", "Calciu", "Zinc"), 2, "Calciul asigură densitatea osoasă."),
            Question("Care este cea mai mică unitate de viață?", listOf("Atomul", "Molecula", "Celula", "Organul"), 2, "Celula este unitatea structurală de bază."),
            Question("Ce gaz absorb plantele în timpul fotosintezei?", listOf("Oxigen", "Azot", "Dioxid de carbon", "Metan"), 2, "Plantele folosesc CO2 pentru a produce glucoză."),
            Question("Cine a scris 'Teoria Relativității'?", listOf("Isaac Newton", "Stephen Hawking", "Albert Einstein", "Nikola Tesla"), 2, "Einstein a publicat relativitatea restrânsă și generală."),
            Question("Ce metal este ruginit?", listOf("Cupru", "Aluminiu", "Fier", "Argint"), 2, "Rugina este oxidul de fier hidratat."),
            Question("Care este simbolul chimic pentru Fier?", listOf("F", "Fi", "Fe", "Ir"), 2, "Fe vine de la 'Ferrum'."),
            Question("Ce instrument măsoară presiunea atmosferică?", listOf("Termometru", "Barometru", "Higrometru", "Anemometru"), 1, "Barometrul indică presiunea aerului."),
            Question("Care este cea mai apropiată stea de Pământ?", listOf("Sirius", "Proxima Centauri", "Soarele", "Polaris"), 2, "Soarele este steaua sistemului nostru."),
            Question("Ce element are numărul atomic 6?", listOf("Oxigen", "Azot", "Carbon", "Bor"), 2, "Carbonul are 6 protoni."),
            Question("Cum se numește un ion cu sarcină pozitivă?", listOf("Anion", "Cation", "Izotop", "Pozitron"), 1, "Cationii sunt atrași de catod."),
            Question("Ce culoare are soluția de sulfat de cupru?", listOf("Roșie", "Galbenă", "Albastră", "Verde"), 2, "Piatra vânătă are o culoare albastră intensă.")
        )
    }
}
