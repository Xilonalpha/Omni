package com.chemscanner.omniscient.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.MainRepository
import com.chemscanner.omniscient.marrow.services.BlockchainNotaryService
import com.chemscanner.omniscient.marrow.services.TextToSpeechService
import com.chemscanner.omniscient.marrow.services.GeminiService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * STOICHIOMETRY PRO v2.1
 * Analiză chimică cantitativă cu integrare Blockchain.
 * FIX: lifecycleScope reference resolved.
 */
@AndroidEntryPoint
class StoichiometryActivity : AppCompatActivity() {

    @Inject lateinit var globalKnowledge: GlobalKnowledgeRepository
    @Inject lateinit var mainRepository: MainRepository
    @Inject lateinit var notary: BlockchainNotaryService
    @Inject lateinit var ttsService: TextToSpeechService
    @Inject lateinit var geminiService: GeminiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            StoichiometryProScreen(
                onBack = { finish() },
                onCalculate = { equation -> runCalculation(equation) }
            )
        }
    }

    private var resultState = mutableStateOf<String?>(null)
    private var isCalculating = mutableStateOf(false)

    private fun runCalculation(equation: String) {
        if (equation.isBlank()) return
        
        isCalculating.value = true
        lifecycleScope.launch {
            try {
                val prompt = """
                    Efectuează analiza stoechiometrică pentru ecuația: $equation.
                    1. Balansează ecuația.
                    2. Calculează masele molare.
                    3. Explică raportul molar.
                    Fii extrem de precis și academic.
                """.trimIndent()
                
                val result = geminiService.generateContent(prompt)
                resultState.value = result
                ttsService.speak("Analiză stoechiometrică finalizată. Am calculat raportul molar pentru $equation.")
                
                // Notarizare automată a calculului
                notary.notarizeDiscovery("STOICHIOMETRY", "Equation: $equation | Result: $result")
                globalKnowledge.logEvent("STOICHIOMETRY", "Calcul complex finalizat și notarizat.", 4)
                
            } catch (e: Exception) {
                resultState.value = "Eroare la calcul: ${e.message}"
            } finally {
                isCalculating.value = false
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun StoichiometryProScreen(onBack: () -> Unit, onCalculate: (String) -> Unit) {
        var equation by remember { mutableStateOf("") }
        val scrollState = rememberScrollState()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("STOICHIOMETRY PRO", color = Color.Cyan, fontWeight = FontWeight.Black, fontSize = 16.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
                )
            },
            containerColor = Color.Black
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Input Section
                Surface(
                    color = Color.White.copy(0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ENTER CHEMICAL EQUATION:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = equation,
                            onValueChange = { equation = it },
                            placeholder = { Text("e.g. H2 + O2 = H2O", color = Color.DarkGray) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.Cyan,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White
                            )
                        )
                        Button(
                            onClick = { onCalculate(equation) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isCalculating.value
                        ) {
                            if (isCalculating.value) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                            else {
                                Icon(Icons.Default.Calculate, null)
                                Spacer(Modifier.width(8.dp))
                                Text("EXECUTE ANALYTIC CALCULATION")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Result Section
                if (resultState.value != null) {
                    Text("SYSTEM OUTPUT:", color = Color.Yellow, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = Color.DarkGray.copy(0.3f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(resultState.value!!, color = Color.White, fontSize = 13.sp, lineHeight = 20.sp)
                            
                            Spacer(Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDone, null, tint = Color.Green, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("NOTARIZED IN AKASHA LEDGER", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("Waiting for equation input...", color = Color.DarkGray, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
