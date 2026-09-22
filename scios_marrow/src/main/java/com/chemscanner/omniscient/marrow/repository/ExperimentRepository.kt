package com.chemscanner.omniscient.marrow.repository

import com.chemscanner.omniscient.marrow.data.models.Experiment
import com.chemscanner.omniscient.marrow.data.models.ExperimentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE SOVEREIGN EXPERIMENT REPOSITORY v100.
 * REPAIRED: Fully synchronized with v17.0 data models.
 * NASA-FIX: Removed explicit getter to avoid JVM signature clash with experiments property.
 */
@Singleton
class ExperimentRepository @Inject constructor() {

    private val _experiments = MutableStateFlow<List<Experiment>>(emptyList())
    val experiments: StateFlow<List<Experiment>> = _experiments.asStateFlow()

    init {
        loadStaticExperiments()
    }

    private fun loadStaticExperiments() {
        val list = listOf(
            Experiment(
                id = "exp_001",
                title = "Sinteza Carbonului",
                description = "Simularea legăturilor atomice în vid.",
                category = "Fizică Cuantică",
                materials = listOf("Carbon", "Energie Neurală"),
                steps = listOf("Inițiază motorul", "Pliază spațiul"),
                type = ExperimentType.HANDS_ON
            ),
            Experiment(
                id = "exp_002",
                title = "Analiză Xeno",
                description = "Monitorizarea semnalelor magnetice exogene.",
                category = "Xeno-Biologie",
                materials = listOf("Senzor EMF", "Ana Istla"),
                steps = listOf("Calibrează", "Analizează"),
                type = ExperimentType.AR_DEMO
            )
        )
        _experiments.value = list
    }
}
