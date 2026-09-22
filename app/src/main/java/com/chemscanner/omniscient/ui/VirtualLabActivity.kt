package com.chemscanner.omniscient.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chemscanner.omniscient.marrow.repository.MainRepository
import com.chemscanner.omniscient.marrow.services.ReactionSimulator
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import com.chemscanner.omniscient.marrow.utils.LanguageManager
import com.chemscanner.omniscient.databinding.ActivityVirtualLabBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.Description
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SimulationResult(
    val description: String,
    val equation: String,
    val energy_points: List<EnergyPoint>,
    val stability_earth: String,
    val stability_mars: String,
    val hazard_level: String
)

data class EnergyPoint(val coord: Float, val energy: Float)

@AndroidEntryPoint
class VirtualLabActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVirtualLabBinding
    private val gson = Gson()

    @Inject
    lateinit var reactionSimulator: ReactionSimulator

    @Inject
    lateinit var ttsService: TextToSpeechService

    @Inject
    lateinit var mainRepository: MainRepository

    override fun attachBaseContext(newBase: Context) {
        val languageManager = LanguageManager(newBase)
        super.attachBaseContext(languageManager.updateContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVirtualLabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupVirtualLab()

        binding.simulateButton.setOnClickListener {
            executeSimulation()
        }

        binding.speakButtonLab.setOnClickListener {
            val textToSpeak = binding.simulationResultText.text.toString()
            if (textToSpeak.isNotBlank()) {
                ttsService.speak(textToSpeak)
            }
        }
    }

    private fun executeSimulation() {
        val r1 = binding.reactant1Input.text.toString()
        val r2 = binding.reactant2Input.text.toString()

        if (r1.isBlank() || r2.isBlank()) {
            binding.simulationResultText.text = "Error: Alpha and Beta reagents required."
            return
        }

        binding.labProgressBar.visibility = View.VISIBLE
        binding.energyChart.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val rawResult = reactionSimulator.simulateReaction(r1, r2)
                parseAndDisplayResult(rawResult)
            } catch (e: Exception) {
                binding.simulationResultText.text = "Kernel Fault: Unable to synthesize data."
                Timber.e(e)
            } finally {
                binding.labProgressBar.visibility = View.GONE
            }
        }
    }

    private fun parseAndDisplayResult(rawJson: String) {
        try {
            val result = gson.fromJson(rawJson, SimulationResult::class.java)
            
            val displayText = """
                EQUATION: ${result.equation}
                HAZARD: ${result.hazard_level}
                
                ${result.description}
                
                [ PLANETARY STABILITY ]
                EARTH: ${result.stability_earth}
                MARS: ${result.stability_mars}
            """.trimIndent()
            
            binding.simulationResultText.text = displayText
            updateEnergyChart(result.energy_points)
            
        } catch (e: Exception) {
            // Fallback if AI doesn't return pure JSON
            binding.simulationResultText.text = rawJson
            binding.energyChart.visibility = View.GONE
        }
    }

    private fun updateEnergyChart(points: List<EnergyPoint>?) {
        if (points == null || points.isEmpty()) return

        val entries = points.map { Entry(it.coord, it.energy) }
        val dataSet = LineDataSet(entries, "Reaction Energy Profile").apply {
            color = Color.CYAN
            setCircleColor(Color.WHITE)
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextColor = Color.WHITE
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.CYAN
            fillAlpha = 50
        }

        binding.energyChart.apply {
            data = LineData(dataSet)
            val desc = Description()
            desc.text = ""
            desc.isEnabled = false
            description = desc
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            legend.textColor = Color.WHITE
            visibility = View.VISIBLE
            invalidate() // Refresh
        }
    }

    private fun setupVirtualLab() {
        lifecycleScope.launch {
            binding.labProgressBar.visibility = View.VISIBLE
            val scanHistory = mainRepository.getAllScansSortedByDateDesc()
            val userChemicals = scanHistory.map { it.chemicalName }.distinct()
            binding.labProgressBar.visibility = View.GONE
            
            val adapter = ArrayAdapter(this@VirtualLabActivity, android.R.layout.simple_dropdown_item_1line, userChemicals)
            binding.reactant1Input.setAdapter(adapter)
            binding.reactant2Input.setAdapter(adapter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsService.shutdown()
    }
}
