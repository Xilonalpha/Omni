package com.chemscanner.omniscient.ui.compose.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chemscanner.omniscient.marrow.repository.AnalysisResult
import com.chemscanner.omniscient.ui.viewmodels.GreenChemistryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreenChemistryScreen(
    viewModel: GreenChemistryViewModel,
    onBack: () -> Unit
) {
    val analysisResult by viewModel.analysisResult.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var chemicalName by remember { mutableStateOf("") }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF020502), Color(0xFF051005))
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("GREEN CHEMISTRY AUDIT", color = Color.Cyan, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(backgroundGradient).padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("SUBSTANCE IDENTIFIER", color = Color.Cyan, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = chemicalName,
                            onValueChange = { chemicalName = it },
                            placeholder = { Text("Enter molecule name...", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.Cyan,
                                unfocusedBorderColor = Color.DarkGray,
                                cursorColor = Color.Cyan,
                                containerColor = Color.White.copy(0.05f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.analyzeChemical(chemicalName) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading && chemicalName.isNotBlank()
                        ) {
                            Text("EXECUTE ECO-ANALYSIS", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.Cyan)
                    }
                } else {
                    analysisResult?.let { result ->
                        ModernAnalysisContent(result)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernAnalysisContent(result: AnalysisResult) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(Color.Cyan.copy(0.1f), style = Stroke(10f))
                        val score = result.sustainabilityScore.toFloat()
                        drawArc(
                            color = if (score > 7) Color.Green else if (score > 4) Color.Yellow else Color.Red,
                            startAngle = -90f,
                            sweepAngle = score * 36f,
                            useCenter = false,
                            style = Stroke(width = 12f)
                        )
                    }
                    Text("${result.sustainabilityScore.toInt()}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(24.dp))
                Column {
                    Text(result.chemicalName.uppercase(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text("ECO-IMPACT QUOTIENT", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // NEW: SOVEREIGN AUDIT REPORT (ANA'S VOICE WRITTEN)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Cyan.copy(0.05f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assignment, null, tint = Color.Cyan, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ANA SOVEREIGN AUDIT", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = result.error ?: "Analysis report incomplete.",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            Text("SUSTAINABLE ALTERNATIVES", color = Color.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        }

        if (result.greenAlternatives.isEmpty()) {
            item {
                Surface(color = Color.Black.copy(0.8f), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))) {
                    Text("NO IMMEDIATE REPLACEMENTS DETECTED. REFER TO AUDIT REPORT ABOVE.", color = Color.Gray, modifier = Modifier.padding(20.dp), fontSize = 11.sp)
                }
            }
        } else {
            items(result.greenAlternatives) { molecule ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Eco, null, tint = Color.Green, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(molecule.smiles, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(molecule.justification, color = Color.LightGray, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }
                }
            }
        }
    }
}
